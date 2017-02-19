package agent

import agent.LingsAgent.AgentState
import client.LingsProtocol._

trait LingsAgent {
  def perceive: InMessage => AgentState => AgentState

  def nextAction: AgentState => Option[OutMessage]
}

object LingsAgent {
  trait AgentState
  case object EmptyState extends AgentState
}
