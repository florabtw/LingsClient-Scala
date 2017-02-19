package brain

import agent.LingsAgent.AgentState
import client.LingsProtocol.OutMessage

trait LingsBrain {
  def nextAction: AgentState => Option[OutMessage]
}
