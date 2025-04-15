package com.akka.training.coffeeMachine;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

public class CoffeeMachineActor {
  private static final int BREWING_DURATION_SECONDS = 10;

  public static Behavior<CoffeeMachineCommand> create() {
    return Behaviors.setup(CoffeeMachineActor::idle);
  }

  private static Behavior<CoffeeMachineCommand> idle(
      final ActorContext<CoffeeMachineCommand> context) {

    context.getLog().info("CoffeeMachine: IDLE");
    return Behaviors.receive(CoffeeMachineCommand.class)
        .onMessage(CoffeeMachineCommand.BrewCoffee.class, command -> brewing(context, command))
        // Can't pick up coffee until coffee is ready, stay in same behavior (equivalent to ignore
        // the message)
        .onMessage(CoffeeMachineCommand.PickupCoffee.class, command -> Behaviors.same())
        .build();
  }

  private static Behavior<CoffeeMachineCommand> brewing(
      final ActorContext<CoffeeMachineCommand> context, CoffeeMachineCommand.BrewCoffee command) {

    context.getLog().info("CoffeeMachine: Brewing 1 {}", command.coffee().toString());

    // TODO Implement the brewing duration (10 seconds) WITHOUT using Thread.sleep
    // (Currently the brewing is immediate, CoffeeIsReady is immediately sent to the Barista)
    command.replyTo().tell(new CoffeeMachineCommand.CoffeeReady(command.coffee()));

    return coffeeReady(context);
  }

  private static Behavior<CoffeeMachineCommand> coffeeReady(
      final ActorContext<CoffeeMachineCommand> context) {

    context.getLog().info("CoffeeMachine: Coffee is ready");
    return Behaviors.receive(CoffeeMachineCommand.class)
        // Can't brew a new coffee until the ready one is picked-up, stay in same behavior
        // (equivalent to ignore the message)
        .onMessage(CoffeeMachineCommand.BrewCoffee.class, command -> Behaviors.same())
        .onMessage(CoffeeMachineCommand.PickupCoffee.class, command -> idle(context))
        .build();
  }
}
