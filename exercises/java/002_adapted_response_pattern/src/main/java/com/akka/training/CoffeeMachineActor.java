package com.akka.training;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

public class CoffeeMachineActor {
  public static Behavior<CoffeeMachineCommand> create() {
    return Behaviors.setup(CoffeeMachineActor::idle);
  }

  private static Behavior<CoffeeMachineCommand> idle(
      final ActorContext<CoffeeMachineCommand> context) {

    context.getLog().info("CoffeeMachine: IDLE");

    return Behaviors.receive(CoffeeMachineCommand.class)
        .onMessage(BrewCoffee.class, command -> brewing(context, command))
        /* Can't pick up coffee until coffee is ready, stay in same behavior (equivalent to ignore the message) */
        .onMessage(PickupCoffee.class, command -> Behaviors.same())
        .build();
  }

  private static Behavior<CoffeeMachineCommand> brewing(
      final ActorContext<CoffeeMachineCommand> context, final BrewCoffee command) {

    context.getLog().info("CoffeeMachine: Brewing 1 {}", command.coffee());
    // Warn: Don't Thread.sleep in Akka actors, it utilizes a thread from the Thread pool.
    // We will see how to replace Thread.sleep by proper non-blocking scheduling in a further
    // exercise.
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    return coffeeReady(context, command.coffee());
  }

  private static Behavior<CoffeeMachineCommand> coffeeReady(
      final ActorContext<CoffeeMachineCommand> context, final Coffee coffee) {

    context.getLog().info("CoffeeMachine: Coffee {} is ready", coffee);
    return Behaviors.receive(CoffeeMachineCommand.class)
        /* Can't brew a new coffee until the ready one is picked-up, stay in same behavior (equivalent to ignore the message) */
        .onMessage(BrewCoffee.class, command -> Behaviors.same())
        .onMessage(PickupCoffee.class, command -> idle(context))
        .build();
  }

  // <- Protocol definition
  public sealed interface CoffeeMachineCommand permits BrewCoffee, CoffeeReady, PickupCoffee {}

  public record BrewCoffee(Coffee coffee, ActorRef<CoffeeReady> replyTo)
      implements CoffeeMachineCommand {}

  public record CoffeeReady(Coffee coffee) implements CoffeeMachineCommand {}

  /* Represents the Barista picking up the coffee and resetting the coffee machine, so that it's ready for the next coffee */
  public record PickupCoffee() implements CoffeeMachineCommand {}
  // Protocol definition ->
}
