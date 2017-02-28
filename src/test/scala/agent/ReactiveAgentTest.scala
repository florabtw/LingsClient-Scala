package agent

import agent.LingsAgent.EmptyState
import agent.ReactiveAgent.{Agent, Food, State, WorldMap}
import brain.ReactiveBrain
import client.LingsProtocol._
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

    "with state" when {
      val existingMap   = WorldMap(3, 3, ".........")
      val existingAgent = Agent(0, 0, 0)
      val existingFood  = Food(0, 0)
      val existingId    = 0
      val existingState = ReactiveAgent.State(existingMap, List(existingAgent), List(existingFood), List(existingId))

      "a map is perceived" should {
        "update the world map" in {
          val output = reactiveAgent.perceive(MapMessage(4, 4, "................"))(existingState)

          inside(output) { case State(map, _, _, _) =>
            map should have ('rows (4))
            map should have ('columns (4))
            map should have ('repr ("................"))
          }
        }
      }

      "an agent is perceived" should {
        "add a new agent" in {
          val output = reactiveAgent.perceive(AgentMessage(1, 1, 1))(existingState)

          inside(output) { case State(_, agents, _, _) =>
            agents should have length 2
            agents should contain theSameElementsAs List(existingAgent, Agent(1, 1, 1))
          }
        }

        "update an existing agent" in {
          val output = reactiveAgent.perceive(AgentMessage(0, 1, 1))(existingState)

          inside(output) { case State(_, agents, _, _) =>
            agents should have length 1
            agents should contain (Agent(0, 1, 1))
          }
        }
      }

      "a food is perceived" should {
        "add a new food" in {
          val output = reactiveAgent.perceive(FoodMessage(1, 1))(existingState)

          inside(output) { case State(_, _, foods, _) =>
            foods should have length 2
            foods should contain theSameElementsAs List(existingFood, Food(1,1))
          }
        }
      }

      "an id is perceived" should {
        "add a new id" in {
          val output = reactiveAgent.perceive(IdMessage(1))(existingState)

          inside(output) { case State(_, _, _, ids) =>
            ids should have length 2
            ids should contain theSameElementsAs List(existingId, 1)
          }
        }

        "not add a duplicate id" in {
          val output = reactiveAgent.perceive(IdMessage(0))(existingState)

          inside(output) { case State(_, _, _, ids) =>
            ids should have length 1
            ids.head shouldBe existingId
          }
        }
      }

      "a move action is perceived" should {
        "move an existing agent" in {
          val output = reactiveAgent.perceive(AgentMoveMessage(0, 1, 1))(existingState)

          inside(output) { case State(_, agents, _, _) =>
            agents.head should have ('id (0))
            agents.head should have ('x (1))
            agents.head should have ('y (1))
          }
        }
      }

      "an eat action is perceived" should {
        "eat food if food intersects with agent" in {
          val output = reactiveAgent.perceive(AgentEatMessage(0))(existingState)

          inside(output) { case State(_, _, foods, _) =>
            foods should be (empty)
          }
        }
      }
    }
  }
}
