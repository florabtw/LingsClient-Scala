package engine

import agent.LingsAgent
import agent.LingsAgent.{AgentState, EmptyState}
import akka.actor.ActorSystem
import client.LingsProtocol.{InMessage, OutMessage}
import engine.LingsEngine.StateHolder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object LingsEngine {
  case class StateHolder(var state: AgentState)
}

sealed trait Engine

case class PerceptEngine(agent: LingsAgent) extends Engine {
  val stateHolder = StateHolder(EmptyState)

  def perceive(m: InMessage): Unit = {
    println("Received: " + m)
    stateHolder.state = agent.perceive(m)(stateHolder.state)
  }

  def register(send: (OutMessage) => Unit): Unit = {
    LingsEngine(agent, send, stateHolder)
  }
}

case class LingsEngine[T](agent: LingsAgent, send: (OutMessage) => Unit, stateHolder: StateHolder) extends Engine {

  implicit val system = ActorSystem()

  system.scheduler.schedule(3.seconds, 1.seconds) {
    agent.nextAction(stateHolder.state).foreach { message =>
      println("Sending:  " + message)
      send(message)
    }
  }
}
