package client

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.TextMessage.Strict
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest, WebSocketUpgradeResponse}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, OverflowStrategy}
import client.Parser._
import engine.PerceptEngine

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class LingsClient(engine: PerceptEngine) {

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  private val httpFlow: Flow[Message, Message, Future[WebSocketUpgradeResponse]] = {
    Http().webSocketClientFlow(WebSocketRequest("ws://localhost:8080"))
  }

  private val (queue, upgradeResponse): (SourceQueueWithComplete[Strict], Future[WebSocketUpgradeResponse]) = Source.queue[TextMessage.Strict](10, OverflowStrategy.dropNew)
    .viaMat(httpFlow)(Keep.both)
    .filter(_.isText)
    .map(_.asTextMessage)
    .filter(_.isStrict)
    .map(m => toLingsMessage(m.getStrictText))
    .toMat(Sink.foreach(engine.perceive))(Keep.left)
    .run()

  engine.register(m => queue.offer(toTextMessage(m)))

  private val connected = upgradeResponse.map { upgrade =>
    if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
      Done
    } else {
      throw new RuntimeException("Connection failed!")
    }
  }

  connected.onComplete { _ => println("Connection success!") }
}
