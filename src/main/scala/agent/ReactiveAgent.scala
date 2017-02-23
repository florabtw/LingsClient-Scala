package agent

import agent.LingsAgent.{AgentState, EmptyState}
import agent.ReactiveAgent.{Agent, EmptyMap, State, WorldMap, _}
import brain.LingsBrain
import client.LingsProtocol._

object ReactiveAgent {
  case class State(map: LingsMap, agents: List[Agent], foods: List[Food], ids: List[Int]) extends AgentState {
    override def toString: String = map match {
      case EmptyMap => "(no map here)"
      case WorldMap(rows, cols, _) =>
        val foodMap  = foods.map(f => (f.x, f.y) -> "F").toMap
        val agentMap = agents.map(a => (a.x, a.y) -> a.id.toString)
        val mapMap   = (foodMap ++ agentMap).withDefaultValue(".")
        val tiles    = for {
          y <- 0 until cols
          x <- 0 until rows
        } yield mapMap((x, y))

        tiles.grouped(cols).map(_.mkString(" ")).mkString("\n")
    }
  }

  sealed trait LingsMap
  case object EmptyMap extends LingsMap
  case class WorldMap(rows: Int, columns: Int, repr: String) extends LingsMap

  case class Agent(id: Int, x: Int, y: Int)

  case class Food(x: Int, y: Int)

  case class Move(id: Int, x: Int, y: Int)

  case class Eat(id: Int)

  def toMap(msg: MapMessage): WorldMap = WorldMap(msg.rows, msg.columns, msg.map)

  def toAgent(msg: AgentMessage): Agent = Agent(msg.id, msg.x, msg.y)
  def toAgent(msg: AgentMoveMessage): Agent = Agent(msg.id, msg.x, msg.y)

  def toFood(msg: FoodMessage): Food = Food(msg.x, msg.y)

  def toEat(msg: AgentEatMessage): Eat = Eat(msg.id)
}

case class ReactiveAgent(brain: LingsBrain) extends LingsAgent {
  override def perceive: InMessage => AgentState => AgentState = {
    case msg: MapMessage       => perceiveMap(toMap(msg))
    case msg: AgentMessage     => perceiveAgent(toAgent(msg))
    case msg: FoodMessage      => perceiveFood(toFood(msg))
    case msg: IdMessage        => perceiveId(msg.id)
    case msg: AgentMoveMessage => perceiveAgent(toAgent(msg))
    case msg: AgentEatMessage  => perceiveEat(toEat(msg))
  }

  private def perceiveMap(map: WorldMap): AgentState => AgentState = {
    case EmptyState   => State(map, Nil, Nil, Nil)
    case state: State => state.copy(map = map)
  }

  private def perceiveAgent(agent: Agent): AgentState => AgentState = {
    case EmptyState   => State(EmptyMap, List(agent), Nil, Nil)
    case state: State =>
      val filtered = state.agents.filterNot(_.id == agent.id)
      val nextAgents = filtered :+ agent
      state.copy(agents = nextAgents)
  }

  private def perceiveFood(food: Food): AgentState => AgentState = {
    case EmptyState   => State(EmptyMap, Nil, List(food), Nil)
    case state: State => state.copy(foods = state.foods :+ food)
  }

  private def perceiveId(id: Int): AgentState => AgentState = {
    case EmptyState   => State(EmptyMap, Nil, Nil, List(id))
    case state: State => state.copy(ids = state.ids :+ id)
  }

  private def perceiveEat(eat: Eat): AgentState => AgentState = {
    case EmptyState   => EmptyState
    case state: State =>
      val eaterOpt  = state.agents.find(_.id == eat.id)
      val nextFoods = eaterOpt.fold(state.foods)(eatFood(state.foods))
      state.copy(foods = nextFoods)
  }

  private def eatFood(foods: List[Food])(agent: Agent): List[Food] = {
    foods.filterNot(food => intersects(agent, food))
  }

  private def intersects(agent: Agent, food: Food) = agent.x == food.x && agent.y == food.y

  override def nextAction: AgentState => Option[OutMessage] = brain.nextAction
}