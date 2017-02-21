package engine

import akka.actor.ActorSystem
import engine.TurnClock.TurnListener

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object TurnClock {
  trait TurnListener {
    def onTurn: Option[FiniteDuration]
  }
}

case class TurnClock(listener: TurnListener) {
  private          val tickDuration = 100.millis
  private implicit val system       = ActorSystem()

  scheduleTurn(tickDuration)

  private def scheduleTurn(duration: FiniteDuration): Unit = {
    system.scheduler.scheduleOnce(duration) {
      val turnDuration = listener.onTurn
      val nextDuration = turnDuration.getOrElse(tickDuration)
      scheduleTurn(nextDuration)
    }
  }
}
