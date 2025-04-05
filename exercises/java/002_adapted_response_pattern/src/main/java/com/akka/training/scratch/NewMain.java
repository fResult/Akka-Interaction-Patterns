package com.akka.training.scratch;

import static com.akka.training.scratch.ClassUtils.simpleName;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class NewMain {
  public static void main(String... args) {
    final var baristaActor = ActorSystem.create(BaristaAct.create(), "barista");

    baristaActor.tell(new BaristaCommand.OrderCoffee("Wick", new Coffee.CaffeJava()));
    baristaActor.tell(new BaristaCommand.OrderCoffee("Anderson", new Coffee.Akkacino()));

    try {
      System.out.println(">>> Please ENTER to exit <<<");
      System.out.println(System.in.read());
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    } finally {
      baristaActor.terminate();
    }
  }
}

class BaristaAct extends AbstractBehavior<BaristaCommand> {
  private final Map<String, Coffee> orders = new HashMap<>();
  private final ActorRef<CoffeeMachineCommand> coffeeMachineActorRef;
  private final ActorRef<CoffeeMachineCommand.CoffeeReady> coffeeMachineMessageAdapter;

  public BaristaAct(ActorContext<BaristaCommand> context) {
    super(context);

    coffeeMachineActorRef = context.spawn(CoffeeMachineAct.create(), "coffee-machine");
    coffeeMachineMessageAdapter =
        context.messageAdapter(
            CoffeeMachineCommand.CoffeeReady.class,
            BaristaCommand.WrappedCoffeeMachineCoffeeReady::new);
  }

  public static Behavior<BaristaCommand> create() {
    return Behaviors.setup(BaristaAct::new);
  }

  @Override
  public Receive<BaristaCommand> createReceive() {
    return newReceiveBuilder()
        .onMessage(BaristaCommand.OrderCoffee.class, this::onOrderCoffee)
        .onMessage(
            BaristaCommand.WrappedCoffeeMachineCoffeeReady.class,
            this::onWrappedCoffeeMachineCoffeeReady)
        .build();
  }

  private Behavior<BaristaCommand> onWrappedCoffeeMachineCoffeeReady(
      BaristaCommand.WrappedCoffeeMachineCoffeeReady wrappedCommand) {

    final var coffeeReadyCommand = wrappedCommand.command();
    getContext().getLog().info("Barista: Picking up {}", simpleName(coffeeReadyCommand.coffee()));

    coffeeMachineActorRef.tell(new CoffeeMachineCommand.PickupCoffee());

    return this;
  }

  public static String printOrders(final Set<Map.Entry<String, Coffee>> orders) {
    return orders.stream()
        .map(kv -> String.format("%s->%s", kv.getKey(), simpleName(kv.getValue())))
        .reduce((acc, s) -> acc + "," + s)
        .map(s -> "[" + s + "]")
        .orElse("[" + "]");
  }

  private Behavior<BaristaCommand> onOrderCoffee(BaristaCommand.OrderCoffee command) {
    orders.put(command.whom(), command.coffee());
    getContext().getLog().info("Orders: {}", printOrders(orders.entrySet()));

    coffeeMachineActorRef.tell(
        new CoffeeMachineCommand.BrewCoffee(command.coffee(), coffeeMachineMessageAdapter));

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
        .onMessage(CoffeeMachineCommand.BrewCoffee.class, command -> brewCoffee(command, context))
        .onMessage(CoffeeMachineCommand.PickupCoffee.class, command -> Behaviors.same())
        .build();
  }

  private static Behavior<CoffeeMachineCommand> brewCoffee(
      CoffeeMachineCommand.BrewCoffee command, ActorContext<CoffeeMachineCommand> context) {
    context.getLog().info("CoffeeMachine: Brewing 1 {}", simpleName(command.coffee()));

    try {
      Thread.sleep(3000);
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }

    command.replyTo().tell(new CoffeeMachineCommand.CoffeeReady(command.coffee()));

    return coffeeReady(command, context);
  }

  private static Behavior<CoffeeMachineCommand> coffeeReady(
      CoffeeMachineCommand.BrewCoffee command, ActorContext<CoffeeMachineCommand> context) {
    context.getLog().info("CoffeeMachine: Coffee {} is ready", simpleName(command.coffee()));

    return Behaviors.receive(CoffeeMachineCommand.class)
        .onMessage(CoffeeMachineCommand.BrewCoffee.class, brewCoffeeCommand2 -> Behaviors.same())
        .onMessage(CoffeeMachineCommand.PickupCoffee.class, pickupCoffeeCommand -> idle(context))
        .build();
  }
}

sealed interface BaristaCommand
    permits BaristaCommand.OrderCoffee, BaristaCommand.WrappedCoffeeMachineCoffeeReady {
  record OrderCoffee(String whom, Coffee coffee) implements BaristaCommand {}

  record WrappedCoffeeMachineCoffeeReady(CoffeeMachineCommand.CoffeeReady command)
      implements BaristaCommand {}
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

class ClassUtils {
  public static <T> String simpleName(T instance) {
    return instance.getClass().getSimpleName();
  }
}
