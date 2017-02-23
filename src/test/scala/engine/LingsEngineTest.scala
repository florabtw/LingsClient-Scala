package engine

import _root_.agent.LingsAgent
import agent.LingsAgent.{AgentState, EmptyState}
import client.LingsProtocol.{InMessage, OutMessage}
import engine.LingsEngine.SendMessage
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class LingsEngineTest extends WordSpec with Matchers with BeforeAndAfterEach with MockitoSugar {

  val inMessage:  InMessage  = mock[InMessage]
  var outMessage: OutMessage = mock[OutMessage]

  val agent: LingsAgent = mock[LingsAgent]

  var perceive:  InMessage => AgentState => AgentState = _
  var perceived: AgentState => AgentState = _

  var nextAction: AgentState => Option[OutMessage] = _
  var send:       SendMessage = _

  var lingsEngine: LingsEngine = _

  override def beforeEach: Unit = {

    lingsEngine = LingsEngine(agent)

    send       = mock[SendMessage]
    perceive   = mock[InMessage => AgentState => AgentState]
    perceived  = mock[AgentState => AgentState]
    nextAction = mock[AgentState => Option[OutMessage]]
    when(agent.perceive).thenReturn(perceive)
    when(perceive.apply(inMessage)).thenReturn(perceived)
    when(perceive.apply(outMessage)).thenReturn(perceived)
    when(agent.nextAction).thenReturn(nextAction)
    when(nextAction.apply(EmptyState)).thenReturn(Some(outMessage))
  }

  "Lings Engine" when {
    "perceiving message" should {
      "forward perceptions to the agent" in {
        lingsEngine.perceive(inMessage)

        verify(perceive).apply(inMessage)
      }

      "start with empty state" in {
        lingsEngine.perceive(inMessage)

        verify(perceived).apply(EmptyState)
      }

      "update agent state" in {
        val expectedState = mock[AgentState]
        when(perceived.apply(EmptyState)).thenReturn(expectedState)

        lingsEngine.perceive(inMessage)

        verify(perceived).apply(EmptyState)

        lingsEngine.perceive(inMessage)

        verify(perceived).apply(expectedState)
      }

      "ignore perceptions sent by own agent" in {
        val expectedState = mock[AgentState]
        when(perceived.apply(EmptyState)).thenReturn(expectedState)

        lingsEngine.onTurn(send)

        verify(perceived).apply(EmptyState)

        lingsEngine.perceive(outMessage)

        verify(perceived, never()).apply(expectedState)
      }
    }

    "taking turn" should {
      "take 15 ticks per turn" in {
        val ticks = lingsEngine.onTurn(send)

        ticks shouldBe defined
        ticks.get.value shouldBe 15
      }

      "send the agent action during a turn" in {
        lingsEngine.onTurn(send)

        verify(send).apply(outMessage)
      }

      "update agent state" in {
        val expectedState = mock[AgentState]
        when(perceived.apply(EmptyState)).thenReturn(expectedState)
        when(nextAction.apply(expectedState)).thenReturn(Some(outMessage))

        lingsEngine.onTurn(send)

        verify(nextAction).apply(EmptyState)

        lingsEngine.onTurn(send)

        verify(nextAction).apply(expectedState)
      }
    }
  }
}
