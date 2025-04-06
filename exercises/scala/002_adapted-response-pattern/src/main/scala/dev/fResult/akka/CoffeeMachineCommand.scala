package dev.fResult.akka

import akka.actor.typed.ActorRef

enum CoffeeMachineCommand {
  case BrewCoffee(coffee: Coffee, replyTo: ActorRef[BaristaCommand])
  case PickupCoffee
}
