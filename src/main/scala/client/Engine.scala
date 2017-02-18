package client

import akka.actor.ActorSystem
import client.Lings.{InMessage, LingsMessage, OutMessage}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Engine {
  sealed trait Engine

  case class LingsState(messages: List[LingsMessage])

  case class StateHolder(var state: LingsState)

  case class PerceptEngine() extends Engine {
    val stateHolder = StateHolder(LingsState(Nil))

    def perceive(m: InMessage): Unit = {
      println("Received: " + m)
      stateHolder.state = LingsState(stateHolder.state.messages :+ m)
    }

    def register(send: (OutMessage) => Unit): Unit = {
      LingsEngine(send, stateHolder)
    }
  }

  case class LingsEngine(send: (OutMessage) => Unit, stateHolder: StateHolder) extends Engine {

    implicit val system = ActorSystem()

    system.scheduler.schedule(3.seconds, 1.seconds) {
      stateHolder.state.messages.foreach {
        case outMessage: OutMessage =>
          println("Sending:  " + outMessage)
          send(outMessage)
        case _ =>
      }
    }
  }
}
