package engine

import agent.LingsAgent
import agent.LingsAgent.{AgentState, EmptyState}
import client.LingsProtocol.{InMessage, OutMessage}
import engine.TurnClock.{IntToTicks, Ticks, TurnListener}

case class LingsEngine(agent: LingsAgent) extends TurnListener {
  var agentState: AgentState     = EmptyState
  var send: (OutMessage) => Unit = identity _
  val turnClock: TurnClock       = TurnClock(this)

  println(agentState)

  def perceive(m: InMessage): Unit = {
    println("Received: " + m)
    agentState = agent.perceive(m)(agentState)
    println(agentState)
  }

  def register(send: (OutMessage) => Unit): Unit = {
    this.send = send
  }

  override def onTurn: Option[Ticks] = {
    val nextActionOpt = agent.nextAction(agentState)
    nextActionOpt.foreach { action =>
      println("Sending:  " + action)
      send(action)
    }

    nextActionOpt.map { _ => 15.ticks }
  }
}
