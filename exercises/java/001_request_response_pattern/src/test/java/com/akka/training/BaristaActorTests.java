package com.akka.training;

import static com.akka.training.TestsUtils.expectedInfoLog;
import static com.akka.training.TestsUtils.lastCapturedLogEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import akka.actor.testkit.typed.CapturedLogEvent;
import akka.actor.testkit.typed.Effect;
import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import akka.actor.testkit.typed.javadsl.TestInbox;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class BaristaActorTests {

  // Barista should log the work in progress orders when it receives a new order (OrderCoffee
  // message)
  @Test
  public void baristaShouldLogReceivingOrder() {
    final var whom1 = "Bart";
    final var coffee1 = new Coffee.Akkaccino();
    final var whom2 = "Lisa";
    final var coffee2 = new Coffee.MochaPlay();

    BehaviorTestKit<BaristaActor.BaristaCommand> testKit =
        BehaviorTestKit.create(BaristaActor.create());

    testKit.clearLog();
    testKit.run(new BaristaActor.OrderCoffee(whom1, coffee1));
    testKit.run(new BaristaActor.OrderCoffee(whom2, coffee2));
    List<CapturedLogEvent> allLogEntries = testKit.getAllLogEntries();

    Map<String, Coffee> expectedOrders = new HashMap<>();
    expectedOrders.put(whom1, coffee1);
    expectedOrders.put(whom2, coffee2);
    CapturedLogEvent expectedLogEvent =
        TestsUtils.expectedInfoLog(
            String.format("Orders: %s", BaristaActor.printOrders(expectedOrders.entrySet())));
    System.out.println(allLogEntries);

    assertEquals(allLogEntries.get(1), expectedLogEvent);
  }

  // Barista should spawn a child actor CoffeeMachine with as actor name 'coffee-machine'
  @Test
  public void spawnCoffeeMachineChild() {
    BehaviorTestKit<BaristaActor.BaristaCommand> testKit =
        BehaviorTestKit.create(BaristaActor.create());
    assertEquals("coffee-machine", testKit.expectEffectClass(Effect.Spawned.class).childName());
  }

  @Test
  public void baristaShouldSendRequest() {
    final var whom = "Ben";
    final var coffee = new Coffee.Akkaccino();

    BehaviorTestKit<BaristaActor.BaristaCommand> testKit =
        BehaviorTestKit.create(BaristaActor.create());
    TestInbox<CoffeeMachineActor.CoffeeMachineCommand> coffeeMachineInbox =
        testKit.childInbox("coffee-machine");

    testKit.run(new BaristaActor.OrderCoffee(whom, coffee));

    var messages = coffeeMachineInbox.getAllReceived();

    assertEquals(1, messages.size());

    CoffeeMachineActor.BrewCoffee brewCoffee = (CoffeeMachineActor.BrewCoffee) messages.get(0);

    assertEquals(brewCoffee.coffee, coffee);
    assertEquals(brewCoffee.replyTo, testKit.getRef());
  }

  @Test
  public void baristaShouldHandleResponse() {
    final var coffee = new Coffee.CaffeJava();

    BehaviorTestKit<BaristaActor.BaristaCommand> testKit =
        BehaviorTestKit.create(BaristaActor.create());

    testKit.run(new BaristaActor.CoffeeReady(coffee));

    List<CapturedLogEvent> allLogEntries = testKit.getAllLogEntries();
    assertEquals(1, allLogEntries.size());

    assertEquals(expectedInfoLog("Barista: Picking up CaffeJava"), lastCapturedLogEvent(testKit));
  }

  @Test
  public void baristaShouldPickupCoffee() {
    final var coffee = new Coffee.MochaPlay();

    BehaviorTestKit<BaristaActor.BaristaCommand> testKit =
        BehaviorTestKit.create(BaristaActor.create());
    TestInbox<CoffeeMachineActor.CoffeeMachineCommand> coffeeMachineInbox =
        testKit.childInbox("coffee-machine");

    testKit.run(new BaristaActor.CoffeeReady(coffee));

    var messages = coffeeMachineInbox.getAllReceived();
    assertEquals(1, messages.size());

    assertTrue(messages.get(0) instanceof CoffeeMachineActor.PickupCoffee);
  }
}
