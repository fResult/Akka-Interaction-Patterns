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

public class Barista extends AbstractBehavior<Barista.BaristaCommand> {

    interface BaristaCommand { }

    public static final class OrderCoffee implements BaristaCommand {
        public final String whom;
        public final Coffee coffee;
        public OrderCoffee(String whom, Coffee coffee) { this.whom = whom; this.coffee = coffee; }
    }

    public static final class WrappedCoffeeMachineCoffeeIsReady implements BaristaCommand {
        public final CoffeeMachine.CoffeeIsReady coffeeReady;
        public WrappedCoffeeMachineCoffeeIsReady(CoffeeMachine.CoffeeIsReady coffeeReady) {
            this.coffeeReady = coffeeReady;
        }
    }

    // Orders <Whom, Coffee>
    private final Map<String, Coffee> orders = new HashMap<>();

    // reference to the coffee-machine child actor, allowing to send messages to coffee machine
    private final ActorRef<CoffeeMachine.CoffeeMachineCommand> coffeeMachine;
    private final ActorRef<CoffeeMachine.CoffeeIsReady> coffeeMachineMessageAdapter;

    public static Behavior<BaristaCommand> create() {
        return Behaviors.setup(Barista::new);
    }

    private Barista(ActorContext<BaristaCommand> context) {
        super(context);
        // We spawn the CoffeeMachine as child actor in the private constructor where we have access to both
        // context and class fields.
        // It returns an ActorRef that we need to keep in the state,
        // we will need it to interact with the CoffeeMachine.
        coffeeMachine = context.spawn(CoffeeMachine.create(), "coffee-machine");

        coffeeMachineMessageAdapter = context.messageAdapter(
            CoffeeMachine.CoffeeIsReady.class, WrappedCoffeeMachineCoffeeIsReady::new
        );
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

        coffeeMachine.tell(new CoffeeMachine.BrewCoffee(command.coffee, coffeeMachineMessageAdapter));

        return this;
    }

    private Behavior<BaristaCommand> onWrappedCoffeeMachineCoffeeReady(WrappedCoffeeMachineCoffeeIsReady wrappedCoffeeReady) {
        CoffeeMachine.CoffeeIsReady coffeeReady = wrappedCoffeeReady.coffeeReady;

        getContext().getLog().info("Barista: Picking up {}", coffeeReady.coffee);

        coffeeMachine.tell(new CoffeeMachine.PickupCoffee());

        return this;
    }

    // Format the orders into expected format [whom1->coffee1,whom2->coffee2]
    static String printOrders(Set<Map.Entry<String,Coffee>> orders) {
        return orders.stream()
            .map(kv -> String.format("%s->%s", kv.getKey(), kv.getValue()))
            .reduce((acc, s) -> acc + "," + s)
            .map(s -> "[" + s + "]").orElse("[" + "]");
    }
}
