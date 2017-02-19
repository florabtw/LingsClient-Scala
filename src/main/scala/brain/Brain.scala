package brain

import agent.LingsAgent.AgentState
import client.LingsProtocol.OutMessage

trait Brain {
  def nextAction: AgentState => Option[OutMessage]
}
