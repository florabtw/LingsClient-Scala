package agent

import agent.MoveRightAgent._
import agent.LingsAgent.{AgentState, NoState}
import client.Lings._

object LingsAgent {
  sealed trait AgentState
  case object NoState extends AgentState
}

sealed trait LingsAgent {
  def perceive: InMessage => AgentState => AgentState

  def nextAction: AgentState => Option[OutMessage]
}

object MoveRightAgent {
  case class Food(x: Int, y: Int)

  case class Entity(id: Int, x: Int, y: Int)

  sealed trait LingsWorld
  case object NoWorld extends LingsWorld
  case class World(rows: Int, columns: Int, map: String) extends LingsWorld

  case class State(map: LingsWorld, agents: List[Entity], foods: List[Food], ids: List[Int]) extends AgentState
}

case class MoveRightAgent() extends LingsAgent {
  override def perceive: InMessage => AgentState => AgentState = {
    case map:   MapMessage    => perceiveMap(map)
    case agent: EntityMessage => perceiveAgent(agent)
    case id:    IdMessage     => perceiveId(id)
    case m                    => perceiveMessage(m)
  }

  private def perceiveMessage(m: InMessage): AgentState => AgentState = {
    case NoState => NoState
    case state   => state
  }

  private def perceiveAgent(agent: EntityMessage): AgentState => AgentState = {
    case NoState => State(NoWorld, List(Entity(agent.id, agent.x, agent.y)), Nil, Nil)
    case state: State => state.copy(agents = state.agents :+ Entity(agent.id, agent.x, agent.y))
  }

  private def perceiveMap(m: MapMessage): AgentState => AgentState = {
    case NoState            => State(World(m.rows, m.columns, m.map), Nil, Nil, Nil)
    case state: State => state.copy(map = World(m.rows, m.columns, m.map))
  }

  private def perceiveId(id: IdMessage): AgentState => AgentState = {
    case NoState            => State(NoWorld, Nil, Nil, List(id.id))
    case state: State => state.copy(ids = state.ids :+ id.id)
  }

  override def nextAction: AgentState => Option[OutMessage] = {
    case NoState                            => None
    case State(_, _, _, Nil)          => None
    case State(_, agents, _, id :: _) =>
      val ownedAgent = agents.find(_.id == id)
      ownedAgent.map { a => EntityMoveMessage(a.id, a.x + 1, a.y) }
  }
}
