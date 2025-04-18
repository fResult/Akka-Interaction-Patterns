package com.akka.training;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

public class CoffeeMachine {

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
    // Warn: Don't Thread.sleep in Akka actors, it utilizes a thread from the Thread pool.
    // We will see how to replace Thread.sleep by proper non-blocking scheduling in a further
    // exercise.
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    brewingCoffee.replyTo.tell(new Barista.CoffeeIsReady(brewingCoffee.coffee));
    return coffeeReady(context);
  }

  // Protocol definition ->

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

  // <- Protocol definition
  public interface CoffeeMachineCommand {}

  public static final class BrewCoffee implements CoffeeMachineCommand {
    public final Coffee coffee;
    public final ActorRef<Barista.BaristaCommand> replyTo;

    public BrewCoffee(Coffee coffee, ActorRef<Barista.BaristaCommand> replyTo) {
      this.coffee = coffee;
      this.replyTo = replyTo;
    }
  }

  /*
  Represents the Barista picking up the coffee and resetting the coffee machine, so that it's ready
  for the next coffee
   */
  public static final class PickupCoffee implements CoffeeMachineCommand {}
}
