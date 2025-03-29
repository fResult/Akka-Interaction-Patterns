package com.akka.training;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

public class CoffeeMachine {

  private static final int BREWING_DURATION_SECONDS = 10;

  public static Behavior<CoffeeMachineCommand> create() {
    return Behaviors.setup(context -> idle(context));
  }

  private static Behavior<CoffeeMachineCommand> idle(
      final ActorContext<CoffeeMachineCommand> context) {
    context.getLog().info("CoffeeMachine: IDLE");
    return Behaviors.receive(CoffeeMachineCommand.class)
        .onMessage(BrewCoffee.class, command -> brewing(context, command))
        // Can't pick up coffee until coffee is ready, stay in same behavior (equivalent to ignore
        // the message)
        .onMessage(PickupCoffee.class, command -> Behaviors.same())
        .build();
  }

  private static Behavior<CoffeeMachineCommand> brewing(
      final ActorContext<CoffeeMachineCommand> context, BrewCoffee brewingCoffee) {
    context.getLog().info("CoffeeMachine: Brewing 1 {}", brewingCoffee.coffee.toString());

    // TODO Implement the brewing duration (10 seconds) WITHOUT using Thread.sleep
    // (Currently the brewing is immediate, CoffeeIsReady is immediately sent to the Barista)
    brewingCoffee.replyTo.tell(new CoffeeIsReady(brewingCoffee.coffee));

    return coffeeReady(context);
  }

  private static Behavior<CoffeeMachineCommand> coffeeReady(
      final ActorContext<CoffeeMachineCommand> context) {
    context.getLog().info("CoffeeMachine: Coffee is ready");
    return Behaviors.receive(CoffeeMachineCommand.class)
        // Can't brew a new coffee until the ready one is picked-up, stay in same behavior
        // (equivalent to ignore the message)
        .onMessage(BrewCoffee.class, command -> Behaviors.same())
        .onMessage(PickupCoffee.class, command -> idle(context))
        .build();
  }

  // Protocol definition ->

  // <- Protocol definition
  public interface CoffeeMachineCommand {}

  public static final class BrewCoffee implements CoffeeMachineCommand {
    public final Coffee coffee;
    public final ActorRef<CoffeeIsReady> replyTo;

    public BrewCoffee(Coffee coffee, ActorRef<CoffeeIsReady> replyTo) {
      this.coffee = coffee;
      this.replyTo = replyTo;
    }
  }

  public static final class CoffeeIsReady implements CoffeeMachineCommand {
    public final Coffee coffee;

    public CoffeeIsReady(Coffee coffee) {
      this.coffee = coffee;
    }
  }

  /*
  Represents the Barista picking up the coffee and resetting the coffee machine, so that it's ready
  for the next coffee
   */
  public static final class PickupCoffee implements CoffeeMachineCommand {}
}
