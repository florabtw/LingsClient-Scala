package client

import akka.actor.ActorSystem
import client.Messages.LingsMessage

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Engine {
  sealed trait Engine

  case class LingsState(messages: List[LingsMessage])

  case class StateHolder(var state: LingsState)

  case class PerceptEngine() extends Engine {
    val stateHolder = StateHolder(LingsState(Nil))

    def perceive(m: LingsMessage): Unit = {
      println("Received: " + m)
      stateHolder.state = LingsState(stateHolder.state.messages :+ m)
    }

    def register(send: (LingsMessage) => Unit): Unit = {
      LingsEngine(send, stateHolder)
    }
  }

  case class LingsEngine(send: (LingsMessage) => Unit, stateHolder: StateHolder) extends Engine {

    implicit val system = ActorSystem()

    system.scheduler.schedule(3.seconds, 1.seconds) {
      stateHolder.state.messages.foreach { message =>
        println("Sending:  " + message)
        send(message)
      }
    }
  }
}
