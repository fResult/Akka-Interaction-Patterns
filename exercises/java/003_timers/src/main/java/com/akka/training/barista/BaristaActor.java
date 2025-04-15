package com.akka.training.barista;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.akka.training.Coffee;
import com.akka.training.coffeeMachine.CoffeeMachineActor;
import com.akka.training.coffeeMachine.CoffeeMachineCommand;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BaristaActor extends AbstractBehavior<BaristaActor.BaristaCommand> {
  // Orders <Whom, Coffee>
  private final Map<String, Coffee> orders = new HashMap<>();
  // reference to the coffee-machine child actor, allowing to send messages to coffee machine
  private final ActorRef<CoffeeMachineCommand> coffeeMachine;
  private final ActorRef<CoffeeMachineCommand.CoffeeReady> coffeeMachineMessageAdapter;

  private BaristaActor(ActorContext<BaristaCommand> context) {
    super(context);
    // We spawn the CoffeeMachine as child actor in the private constructor where we have access to
    // both context and class fields.
    // It returns an ActorRef that we need to keep in the state, we will need it to interact with
    // the CoffeeMachine.
    coffeeMachine = context.spawn(CoffeeMachineActor.create(), "coffee-machine");

    coffeeMachineMessageAdapter =
        context.messageAdapter(
            CoffeeMachineCommand.CoffeeReady.class, WrappedCoffeeMachineCoffeeIsReady::new);
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
    return newReceiveBuilder()
        .onMessage(OrderCoffee.class, this::onOrderCoffee)
        .onMessage(WrappedCoffeeMachineCoffeeIsReady.class, this::onWrappedCoffeeMachineCoffeeReady)
        .build();
  }

  private Behavior<BaristaCommand> onOrderCoffee(OrderCoffee command) {
    orders.put(command.whom, command.coffee);

    getContext().getLog().info("Orders:{}", printOrders(orders.entrySet()));

    coffeeMachine.tell(
        new CoffeeMachineCommand.BrewCoffee(command.coffee, coffeeMachineMessageAdapter));

    return this;
  }

  private Behavior<BaristaCommand> onWrappedCoffeeMachineCoffeeReady(
      WrappedCoffeeMachineCoffeeIsReady wrappedCoffeeReady) {

    CoffeeMachineCommand.CoffeeReady coffeeReady = wrappedCoffeeReady.coffeeReady;

    getContext().getLog().info("Barista: Picking up {}", coffeeReady.coffee());

    coffeeMachine.tell(new CoffeeMachineCommand.PickupCoffee());

    return this;
  }

  public interface BaristaCommand {}

  public record OrderCoffee(String whom, Coffee coffee) implements BaristaCommand {}

  public record WrappedCoffeeMachineCoffeeIsReady(CoffeeMachineCommand.CoffeeReady coffeeReady)
      implements BaristaCommand {}
}
