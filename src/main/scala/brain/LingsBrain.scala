package brain

import agent.LingsAgent.AgentState
import client.LingsProtocol.OutMessage

trait LingsBrain[S <: AgentState] {
  def nextAction: S => Option[OutMessage]
}
