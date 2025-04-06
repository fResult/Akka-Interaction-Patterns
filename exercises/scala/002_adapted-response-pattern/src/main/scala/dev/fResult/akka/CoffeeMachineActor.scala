package dev.fResult.akka

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object CoffeeMachineActor {
  def apply(): Behavior[CoffeeMachineCommand] = {
    println("Coffee Machine Actor is worked")
    Behaviors.same
  }
}
