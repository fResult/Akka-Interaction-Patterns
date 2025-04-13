package com.akka.training;

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
    var whom1 = "Bart";
    var coffee1 = new Coffee.Akkaccino();
    var whom2 = "Lisa";
    var coffee2 = new Coffee.MochaPlay();

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
            String.format("Orders:%s", BaristaActor.printOrders(expectedOrders.entrySet())));
    System.out.println(allLogEntries);

    assertEquals(allLogEntries.get(1), expectedLogEvent);
  }

  // Barista should spawn a child actor CoffeeMachine with as actor name 'coffee-machine'
  @Test
  public void spawnCoffeeMachineChild() {
    BehaviorTestKit<BaristaActor.BaristaCommand> testKit =
        BehaviorTestKit.create(BaristaActor.create());

    var effects = testKit.getAllEffects();

    var spawnEffectOpt = effects.stream().filter(e -> e instanceof Effect.Spawned).findFirst();

    spawnEffectOpt.ifPresent(
        effect -> {
          var spawnEffect = (Effect.Spawned) effect;
          assertEquals("coffee-machine", spawnEffect.childName());
        });
  }

  @Test
  public void baristaShouldCreateMessageAdapter() {
    BehaviorTestKit<BaristaActor.BaristaCommand> testKit =
        BehaviorTestKit.create(BaristaActor.create());

    var effects = testKit.getAllEffects();

    assertTrue(effects.stream().anyMatch(o -> o instanceof Effect.MessageAdapter));
  }

  @Test
  public void baristaShouldSendRequest() {
    var whom = "Ben";
    var coffee = new Coffee.Akkaccino();

    BehaviorTestKit<BaristaActor.BaristaCommand> testKit =
        BehaviorTestKit.create(BaristaActor.create());
    TestInbox<CoffeeMachineActor.CoffeeMachineCommand> coffeeMachineInbox =
        testKit.childInbox("coffee-machine");

    testKit.run(new BaristaActor.OrderCoffee(whom, coffee));

    var messages = coffeeMachineInbox.getAllReceived();

    assertEquals(1, messages.size());

    CoffeeMachineActor.BrewCoffee brewCoffee = (CoffeeMachineActor.BrewCoffee) messages.get(0);

    assertEquals(brewCoffee.coffee, coffee);
    // message adapters have deterministic anonymous names, in the same way as a regular child
    // for reference see:
    // https://doc.akka.io/docs/akka/current/typed/testing-sync.html#sending-messages
    assertEquals(brewCoffee.replyTo, testKit.childInbox("$a-adapter").getRef());
  }
}
