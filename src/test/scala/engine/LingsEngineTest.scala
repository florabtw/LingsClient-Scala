package engine

import _root_.agent.LingsAgent
import agent.LingsAgent.{AgentState, EmptyState}
import client.LingsProtocol.{InMessage, OutMessage}
import engine.LingsEngine.SendMessage
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Matchers, WordSpec}

class LingsEngineTest extends WordSpec with Matchers with BeforeAndAfterEach with MockitoSugar {

  val inMessage: InMessage = mock[InMessage]
  val agent: LingsAgent = mock[LingsAgent]

  var mockPerceive:  InMessage => AgentState => AgentState = _
  var mockPerceived: AgentState => AgentState = _

  var nextAction: AgentState => Option[OutMessage] = _
  var send:       SendMessage = _
  var outMessage: OutMessage = _

  var lingsEngine: LingsEngine = _

  override def beforeEach: Unit = {
    mockPerceive  = mock[Function[InMessage, AgentState => AgentState]]
    mockPerceived = mock[Function[AgentState, AgentState]]

    lingsEngine = LingsEngine(agent)

    send = mock[SendMessage]
    outMessage = mock[OutMessage]
    nextAction = mock[AgentState => Option[OutMessage]]
    when(agent.nextAction).thenReturn(nextAction)
    when(nextAction.apply(EmptyState)).thenReturn(Some(outMessage))
    when(agent.perceive).thenReturn(mockPerceive)
    when(mockPerceive.apply(inMessage)).thenReturn(mockPerceived)
  }

  "Lings Engine" should {
    "forward perceptions to the agent" in {
      lingsEngine.perceive(inMessage)

      verify(mockPerceive).apply(inMessage)
    }

    "start with empty state" in {
      lingsEngine.perceive(inMessage)

      verify(mockPerceived).apply(EmptyState)
    }

    "send the agent state with every request" in {
      val expectedState = mock[AgentState]
      when(mockPerceived.apply(EmptyState)).thenReturn(expectedState)

      lingsEngine.perceive(inMessage)

      verify(mockPerceived).apply(EmptyState)

      lingsEngine.perceive(inMessage)

      verify(mockPerceived).apply(expectedState)
    }

    "take 15 ticks per turn" in {
      val ticks = lingsEngine.onTurn(send)

      ticks           shouldBe defined
      ticks.get.value shouldBe 15
    }

    "send the agent action during a turn" in {
      lingsEngine.onTurn(send)

      verify(send).apply(outMessage)
    }
  }
}
