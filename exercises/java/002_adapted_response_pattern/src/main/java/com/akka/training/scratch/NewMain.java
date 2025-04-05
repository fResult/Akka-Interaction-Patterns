package com.akka.training.scratch;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NewMain {
  public static void main(String... args) {
    final var baristaActor = ActorSystem.create(BaristaAct.create(), "barista");

    baristaActor.tell(new BaristaCommand.OrderCoffee("Wick", new Coffee.CaffeJava()));
    baristaActor.tell(new BaristaCommand.OrderCoffee("Anderson", new Coffee.Akkacino()));
  }
}

class BaristaAct extends AbstractBehavior<BaristaCommand> {
  private final Map<String, Coffee> orders = new HashMap<>();
  private final ActorRef<CoffeeMachineCommand> coffeeMachineActorRef;

  public BaristaAct(ActorContext<BaristaCommand> context) {
    super(context);

    coffeeMachineActorRef = context.spawn(CoffeeMachineAct.create(), "coffee-machine");
  }

  public static Behavior<BaristaCommand> create() {
    return Behaviors.setup(BaristaAct::new);
  }

  @Override
  public Receive<BaristaCommand> createReceive() {
    return newReceiveBuilder()
        .onMessage(BaristaCommand.OrderCoffee.class, this::onOrderCoffee)
        .build();
  }

  public static String printOrders(final Set<Map.Entry<String, Coffee>> orders) {
    return orders.stream()
        .map(kv -> String.format("%s->%s", kv.getKey(), kv.getValue()))
        .reduce((acc, s) -> acc + "," + s)
        .map(s -> "[" + s + "]")
        .orElse("[" + "]");
  }

  private Behavior<BaristaCommand> onOrderCoffee(BaristaCommand.OrderCoffee command) {
    orders.put(command.whom(), command.coffee());
    getContext().getLog().info("Orders: {}", printOrders(orders.entrySet()));

    return Behaviors.same();
  }
}

class CoffeeMachineAct {
  public static Behavior<CoffeeMachineCommand> create() {
    return Behaviors.setup(CoffeeMachineAct::idle);
  }

  private static Behavior<CoffeeMachineCommand> idle(ActorContext<CoffeeMachineCommand> context) {
    context.getLog().info("CoffeeMachine: IDLE");

    return Behaviors.receive(CoffeeMachineCommand.class)
        .onMessage(
            CoffeeMachineCommand.BrewCoffee.class,
            brewCoffeeCommand -> brewCoffee(brewCoffeeCommand, context))
        .onMessage(CoffeeMachineCommand.PickupCoffee.class, pickupCommand -> Behaviors.same())
        .build();
  }

  private static Behavior<CoffeeMachineCommand> brewCoffee(
      CoffeeMachineCommand.BrewCoffee command, ActorContext<CoffeeMachineCommand> context) {
    context.getLog().info("CoffeeMachine: Brewing 1 {}", command.coffee());

    try {
      Thread.sleep(3000);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }

    command.replyTo().tell(new CoffeeMachineCommand.CoffeeReady(command.coffee()));
    context.getLog().info("CoffeeMachine: Coffee {} is ready", command.coffee());
    return Behaviors.receive(CoffeeMachineCommand.class)
        .onMessage(CoffeeMachineCommand.BrewCoffee.class, brewCoffeeCommand2 -> Behaviors.same())
        .onMessage(CoffeeMachineCommand.PickupCoffee.class, pickupCoffeeCommand -> idle(context))
        .build();
  }
}

sealed interface BaristaCommand permits BaristaCommand.OrderCoffee, BaristaCommand.CoffeeReady {
  record OrderCoffee(String whom, Coffee coffee) implements BaristaCommand {}

  record CoffeeReady(Coffee coffee) implements BaristaCommand {}
}

sealed interface CoffeeMachineCommand
    permits CoffeeMachineCommand.BrewCoffee,
        CoffeeMachineCommand.CoffeeReady,
        CoffeeMachineCommand.PickupCoffee {

  record BrewCoffee(Coffee coffee, ActorRef<CoffeeReady> replyTo) implements CoffeeMachineCommand {}

  record CoffeeReady(Coffee coffee) implements CoffeeMachineCommand {}

  record PickupCoffee() implements CoffeeMachineCommand {}
}

sealed interface Coffee permits Coffee.Akkacino, Coffee.MochaPlay, Coffee.CaffeJava {
  record Akkacino() implements Coffee {}

  record MochaPlay() implements Coffee {}

  record CaffeJava() implements Coffee {}
}
