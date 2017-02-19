package engine

import agent.LingsAgent
import agent.LingsAgent.{AgentState, EmptyState}
import akka.actor.ActorSystem
import client.LingsProtocol.{InMessage, OutMessage}
import engine.LingsEngine.StateHolder

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

sealed trait LingsEngine

object LingsEngine {
  case class StateHolder(var state: AgentState)
}

case class PerceptEngine(agent: LingsAgent) extends LingsEngine {
  val stateHolder = StateHolder(EmptyState)

  def perceive(m: InMessage): Unit = {
    println("Received: " + m)
    stateHolder.state = agent.perceive(m)(stateHolder.state)
  }

  def register(send: (OutMessage) => Unit): Unit = {
    GameEngine(agent, send, stateHolder)
  }
}

case class GameEngine[T](agent: LingsAgent, send: (OutMessage) => Unit, stateHolder: StateHolder) extends LingsEngine {

  implicit val system = ActorSystem()

  system.scheduler.schedule(3.seconds, 1.seconds) {
    agent.nextAction(stateHolder.state).foreach { message =>
      println("Sending:  " + message)
      send(message)
    }
  }
}
