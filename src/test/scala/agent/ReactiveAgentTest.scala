package agent

import agent.LingsAgent.EmptyState
import agent.ReactiveAgent.{Agent, Food, State, WorldMap}
import brain.ReactiveBrain
import client.LingsProtocol.{AgentMessage, FoodMessage, IdMessage, MapMessage}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterEach, Inside, Matchers, WordSpec}

class ReactiveAgentTest extends WordSpec with Matchers with BeforeAndAfterEach with MockitoSugar with Inside {

  val brain: ReactiveBrain = mock[ReactiveBrain]
  var reactiveAgent: ReactiveAgent = _

  override def beforeEach(): Unit = {
    reactiveAgent = ReactiveAgent(brain)
  }

  "Reactive Agent" when {
    "without state" should {
      "initialize state when map perceived" in {
        val output = reactiveAgent.perceive(MapMessage(3, 3, "........."))(EmptyState)

        inside(output) { case State(map, _, _, _) =>
          map should have ('rows (3))
          map should have ('columns (3))
          map should have ('repr ("........."))
        }
      }

      "initialize state when agent perceived" in {
        val output = reactiveAgent.perceive(AgentMessage(0, 0, 0))(EmptyState)

        inside(output) { case State(_, agents, _, _) =>
          agents should have length 1
          agents.head should have ('id (0))
          agents.head should have ('x (0))
          agents.head should have ('y (0))
        }
      }

      "initialize state when food perceived" in {
        val output = reactiveAgent.perceive(FoodMessage(0, 0))(EmptyState)

        inside(output) { case State(_, _, foods, _) =>
          foods should have length 1
          foods.head should have ('x (0))
          foods.head should have ('y (0))
        }
      }

      "initialize state when id perceived" in {
        val output = reactiveAgent.perceive(IdMessage(0))(EmptyState)

        inside(output) { case State(_, _, _, ids) =>
          ids should have length 1
          ids.head shouldBe 0
        }
      }
    }
  }
}
