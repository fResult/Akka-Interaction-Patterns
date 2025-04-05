package com.lightbend.training;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import akka.actor.testkit.typed.CapturedLogEvent;
import akka.actor.testkit.typed.Effect;
import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import com.akka.training.BaristaActor;
import com.akka.training.Coffee;
import com.akka.training.CoffeeMachineActor;
import java.util.HashMap;
import org.junit.Test;

public class BaristaActorTests {

  /* Barista should log the work in progress orders when it receives a new order (OrderCoffee
   * message)
   */
  @Test
  public void baristaShouldLogReceivingOrder() {
    final var whom1 = "Bart";
    final var coffee1 = new Coffee.Akkaccino();
    final var whom2 = "Lisa";
    final var coffee2 = new Coffee.MochaPlay();

    final var testKit = BehaviorTestKit.create(BaristaActor.create());

    testKit.clearLog();
    testKit.run(new BaristaActor.OrderCoffee(whom1, coffee1));
    testKit.run(new BaristaActor.OrderCoffee(whom2, coffee2));
    final var allLogEntries = testKit.getAllLogEntries();

    final var expectedOrders = new HashMap<String, Coffee>();
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
    final var testKit = BehaviorTestKit.create(BaristaActor.create());
    final var effects = testKit.getAllEffects();

    final var spawnedEffectOpt =
        effects.stream()
            // .filter( e -> e instanceof Effect.Spawned)
            .filter(Effect.Spawned.class::isInstance)
            .map(Effect.Spawned.class::cast)
            .findFirst();

    /*
     NOTE: Move `Effect.Spawned` casting to the precedent lines
     spawnEffectOpt.ifPresent((effect) -> {
       var spawnedEffect = (Effect.Spawned) effect;
       assertEquals("coffee-machine", spawnedEffect.childName());
     });
    */
    spawnedEffectOpt.ifPresent(effect -> assertEquals("coffee-machine", effect.childName()));
  }

  @Test
  public void baristaShouldCreateMessageAdapter() {
    final var testKit = BehaviorTestKit.create(BaristaActor.create());
    final var effects = testKit.getAllEffects();

    // assertTrue(effects.stream().anyMatch(o -> o instanceof Effect.MessageAdapter));
    assertTrue(effects.stream().anyMatch(Effect.MessageAdapter.class::isInstance));
  }

  @Test
  public void baristaShouldSendRequest() {
    final var whom = "Ben";
    final var coffee = new Coffee.Akkaccino();
    final var testKit = BehaviorTestKit.create(BaristaActor.create());
    final var coffeeMachineInbox =
        testKit.<CoffeeMachineActor.CoffeeMachineCommand>childInbox("coffee-machine");

    testKit.run(new BaristaActor.OrderCoffee(whom, coffee));

    final var messages = coffeeMachineInbox.getAllReceived();

    assertEquals(1, messages.size());

    final var brewCoffee = (CoffeeMachineActor.BrewCoffee) messages.getFirst();

    assertEquals(brewCoffee.coffee(), coffee);
    // message adapters have deterministic anonymous names, in the same way as a regular child
    // for reference see:
    // https://doc.akka.io/docs/akka/current/typed/testing-sync.html#sending-messages
    assertEquals(brewCoffee.replyTo(), testKit.childInbox("$a-adapter").getRef());
  }
}
