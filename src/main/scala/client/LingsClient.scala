package client

import akka.Done
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.TextMessage.Strict
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest, WebSocketUpgradeResponse}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source, SourceQueueWithComplete}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, OverflowStrategy, Supervision}
import client.Parser._
import com.typesafe.config.{Config, ConfigFactory}
import engine.LingsEngine

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

case class LingsClient(engine: LingsEngine) {

  val config: Config = ConfigFactory.load()
  val host: String   = config.getString("lings.host")

  val supervisionDecider: Supervision.Decider = { e =>
    e.printStackTrace()
    Supervision.stop
  }

  private implicit val system       = ActorSystem()
  private val materializerSettings  = ActorMaterializerSettings(system).withSupervisionStrategy(supervisionDecider)
  private implicit val materializer = ActorMaterializer(materializerSettings)(system)

  private val httpFlow: Flow[Message, Message, Future[WebSocketUpgradeResponse]] = {
    Http().webSocketClientFlow(WebSocketRequest(host))
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

  connected.onComplete {
    case Success(_) => println("Connection success!")
    case Failure(e) => println(e)
  }
}
