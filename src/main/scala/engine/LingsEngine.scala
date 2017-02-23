package engine

import agent.LingsAgent
import agent.LingsAgent.{AgentState, EmptyState}
import client.LingsProtocol.{InMessage, OutMessage}
import engine.LingsEngine.SendMessage
import engine.TurnClock.{IntToTicks, Ticks, TurnListener}

object LingsEngine {
  type SendMessage = (OutMessage => Unit)
}

case class LingsEngine(agent: LingsAgent) extends TurnListener[SendMessage] {
  private var agentState: AgentState = EmptyState

  println(agentState)

  def perceive(m: InMessage): Unit = {
    println("Received: " + m)
    agentState = agent.perceive(m)(agentState)
    println(agentState)
  }

  def register(send: SendMessage): Unit = {
    TurnClock(this, send).start()
  }

  override def onTurn(send: SendMessage): Option[Ticks] = {
    val nextActionOpt = agent.nextAction(agentState)

    nextActionOpt.foreach { action =>
      println("Sending:  " + action)
      send(action)
      agentState = agent.perceive(action)(agentState)
    }

    nextActionOpt.map { _ => 15.ticks }
  }
}
