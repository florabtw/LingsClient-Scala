package brain

import agent.ReactiveAgent.State
import client.LingsProtocol.OutMessage

trait ReactiveBrain extends LingsBrain[State] {
  override def nextAction: State => Option[OutMessage]
}
