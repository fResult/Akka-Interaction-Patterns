package com.akka.training;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BaristaActor extends AbstractBehavior<BaristaActor.BaristaCommand> {
  // Orders <Whom, Coffee>
  private final Map<String, Coffee> orders = new HashMap<>();
  // reference to the coffee-machine child actor, allowing to send messages to coffee machine
  private final ActorRef<CoffeeMachineActor.CoffeeMachineCommand> coffeeMachine;

  private BaristaActor(ActorContext<BaristaCommand> context) {
    super(context);
    // We spawn the CoffeeMachine as child actor in the private constructor where we have access to
    // both context and class fields.
    // It returns an ActorRef that we need to keep in the state, we will need it to interact with
    // the CoffeeMachine.
    coffeeMachine = context.spawn(CoffeeMachineActor.create(), "coffee-machine");
  }

  public static Behavior<BaristaCommand> create() {
    return Behaviors.setup(BaristaActor::new);
  }

  // Format the orders into expected format [whom1->coffee1,whom2->coffee2]
  public static String printOrders(Set<Map.Entry<String, Coffee>> orders) {
    return orders.stream()
        .map(kv -> String.format("%s->%s", kv.getKey(), kv.getValue()))
        .reduce((acc, s) -> acc + "," + s)
        .map(s -> "[" + s + "]")
        .orElse("[" + "]");
  }

  @Override
  public Receive<BaristaCommand> createReceive() {
    return newReceiveBuilder().onMessage(OrderCoffee.class, this::onOrderCoffee).build();
  }

  private Behavior<BaristaCommand> onOrderCoffee(OrderCoffee command) {
    orders.put(command.whom, command.coffee);
    getContext().getLog().info("Orders: {}", printOrders(orders.entrySet()));

    return this;
  }

  // <- Protocol definition
  public sealed interface BaristaCommand permits OrderCoffee {}

  public record OrderCoffee(String whom, Coffee coffee) implements BaristaCommand {}
  // Protocol definition ->
}
