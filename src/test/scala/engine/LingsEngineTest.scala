package engine

import _root_.agent.LingsAgent
import agent.LingsAgent.{AgentState, EmptyState}
import client.LingsProtocol.InMessage
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, WordSpec}

class LingsEngineTest extends WordSpec with BeforeAndAfterEach with MockitoSugar {

  val message: InMessage = mock[InMessage]
  val agent: LingsAgent = mock[LingsAgent]

  var mockPerceive: Function[InMessage, AgentState => AgentState]  = _
  var mockPerceived: Function[AgentState, AgentState] = _

  var lingsEngine: LingsEngine = _

  override def beforeEach: Unit = {
    mockPerceive  = mock[Function[InMessage, AgentState => AgentState]]
    mockPerceived = mock[Function[AgentState, AgentState]]

    lingsEngine = LingsEngine(agent)

    when(agent.perceive).thenReturn(mockPerceive)
    when(mockPerceive.apply(message)).thenReturn(mockPerceived)
  }

  "Lings Engine" should {

    "forward perceptions to the agent" in {
      lingsEngine.perceive(message)

      verify(mockPerceive).apply(message)
    }

    "start with empty state" in {
      lingsEngine.perceive(message)

      verify(mockPerceived).apply(EmptyState)
    }

    "send the agent state with every request" in {
      val expectedState = mock[AgentState]
      when(mockPerceived.apply(EmptyState)).thenReturn(expectedState)

      lingsEngine.perceive(message)

      verify(mockPerceived).apply(EmptyState)

      lingsEngine.perceive(message)

      verify(mockPerceived).apply(expectedState)
    }
  }
}
