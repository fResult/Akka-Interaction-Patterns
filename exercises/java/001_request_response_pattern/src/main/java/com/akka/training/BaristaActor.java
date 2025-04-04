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
    /* We spawn the CoffeeMachine as child actor in the private constructor where we have access to both context and class fields.
     * It returns an ActorRef that we need to keep in the state, we will need it to interact with the CoffeeMachine.
     */
    coffeeMachine = context.spawn(CoffeeMachineActor.create(), "coffee-machine");
  }

  public static Behavior<BaristaCommand> create() {
    return Behaviors.setup(BaristaActor::new);
  }

  // Format the orders into expected format [whom1->coffee1,whom2->coffee2]
  static String printOrders(Set<Map.Entry<String, Coffee>> orders) {
    return orders.stream()
        .map(kv -> String.format("%s->%s", kv.getKey(), kv.getValue()))
        .reduce((acc, s) -> acc + "," + s)
        .map(s -> "[" + s + "]")
        .orElse("[" + "]");
  }

  @Override
  public Receive<BaristaCommand> createReceive() {
    return newReceiveBuilder()
        .onMessage(OrderCoffee.class, this::onOrderCoffee)
        .onMessage(CoffeeReady.class, this::onCoffeeReady)
        .build();
  }

  private Behavior<BaristaCommand> onOrderCoffee(OrderCoffee command) {
    orders.put(command.whom, command.coffee);
    getContext().getLog().info("Orders: {}", printOrders(orders.entrySet()));

    // Send brew command to coffee machine
    coffeeMachine.tell(new CoffeeMachineActor.BrewCoffee(command.coffee, getContext().getSelf()));

    return this;
  }

  private Behavior<BaristaCommand> onCoffeeReady(CoffeeReady command) {
    getContext().getLog().info("Barista: Picking up {}", command.coffee);

    coffeeMachine.tell(new CoffeeMachineActor.PickupCoffee());

    return this;
  }

  // <- Protocol definition
  public interface BaristaCommand {}

  public static final class OrderCoffee implements BaristaCommand {
    public final String whom;
    public final Coffee coffee;

    public OrderCoffee(String whom, Coffee coffee) {
      this.whom = whom;
      this.coffee = coffee;
    }
  }

  // Represents the signal sent to the Barista when a coffee is ready to be picked up
  public static final class CoffeeReady implements BaristaCommand {
    public final Coffee coffee;

    public CoffeeReady(Coffee coffee) {
      this.coffee = coffee;
    }
  }
  // Protocol definition ->
}
