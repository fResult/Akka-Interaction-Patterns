package com.akka.training;

import static com.akka.training.TestsUtils.*;
import static org.junit.Assert.assertEquals;

import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import akka.actor.testkit.typed.javadsl.TestInbox;
import org.junit.Test;

public class CoffeeMachineActorTests {
  final Coffee AKKACINO = new Coffee.Akkaccino();
  final Coffee MOCHA_PLAY = new Coffee.MochaPlay();
  final Coffee CAFFE_JAVA = new Coffee.CaffeJava();

  // CoffeeMachine should transition from IDLE to Brewing on receiving BrewCoffee message
  @Test
  public void brewing() {
    final var testKit = BehaviorTestKit.create(CoffeeMachineActor.create());
    final var baristaMessageAdapter = TestInbox.<CoffeeMachineActor.CoffeeReady>create();

    assertEquals(expectedInfoLog("CoffeeMachine: IDLE"), lastCapturedLogEvent(testKit));
    testKit.clearLog();
    testKit.run(new CoffeeMachineActor.BrewCoffee(AKKACINO, baristaMessageAdapter.getRef()));
    assertEquals(
        expectedInfoLog(String.format("CoffeeMachine: Brewing 1 %s", AKKACINO)),
        offsetCapturedLogEvent(testKit, 1));
  }

  // CoffeeMachine should transition between its states along the full cycle
  @Test
  public void fullCycle() {
    final var testKit = BehaviorTestKit.create(CoffeeMachineActor.create());
    final var baristaMessageAdapter = TestInbox.<CoffeeMachineActor.CoffeeReady>create();

    assertEquals(expectedInfoLog("CoffeeMachine: IDLE"), lastCapturedLogEvent(testKit));
    testKit.clearLog();
    testKit.run(new CoffeeMachineActor.BrewCoffee(AKKACINO, baristaMessageAdapter.getRef()));
    assertEquals(
        expectedInfoLog(String.format("CoffeeMachine: Brewing 1 %s", AKKACINO)),
        offsetCapturedLogEvent(testKit, 1));
    assertEquals(
        expectedInfoLog(String.format("CoffeeMachine: Coffee %s is ready", AKKACINO)),
        lastCapturedLogEvent(testKit));

    testKit.run(new CoffeeMachineActor.PickupCoffee());

    assertEquals(expectedInfoLog("CoffeeMachine: IDLE"), lastCapturedLogEvent(testKit));
  }

  // CoffeeMachine should be able to start a new cycle after completing one
  @Test
  public void restartCycle() {
    final var testKit = BehaviorTestKit.create(CoffeeMachineActor.create());
    final var baristaMessageAdapter = TestInbox.<CoffeeMachineActor.CoffeeReady>create();

    testKit.run(
        new CoffeeMachineActor.BrewCoffee(new Coffee.Akkaccino(), baristaMessageAdapter.getRef()));
    testKit.run(new CoffeeMachineActor.PickupCoffee());
    assertEquals(expectedInfoLog("CoffeeMachine: IDLE"), lastCapturedLogEvent(testKit));
    testKit.run(new CoffeeMachineActor.BrewCoffee(MOCHA_PLAY, baristaMessageAdapter.getRef()));
    assertEquals(
        expectedInfoLog(String.format("CoffeeMachine: Brewing 1 %s", MOCHA_PLAY)),
        offsetCapturedLogEvent(testKit, 1));
    assertEquals(
        expectedInfoLog(String.format("CoffeeMachine: Coffee %s is ready", MOCHA_PLAY)),
        lastCapturedLogEvent(testKit));
  }

  @Test
  public void shouldSendReadySignalToBarista() {
    final var testKit = BehaviorTestKit.create(CoffeeMachineActor.create());
    final var baristaMessageAdapter = TestInbox.<CoffeeMachineActor.CoffeeReady>create();

    testKit.run(new CoffeeMachineActor.BrewCoffee(CAFFE_JAVA, baristaMessageAdapter.getRef()));

    final var coffeesReady = baristaMessageAdapter.getAllReceived();
    assertEquals(1, coffeesReady.size());

    CoffeeMachineActor.CoffeeReady readyCoffee = coffeesReady.getFirst();
    assertEquals(CAFFE_JAVA, readyCoffee.coffee());
  }

  @Test
  public void shouldNotProcessCoffeesUntilReset() {
    final var testKit = BehaviorTestKit.create(CoffeeMachineActor.create());
    final var baristaMessageAdapter = TestInbox.<CoffeeMachineActor.CoffeeReady>create();

    testKit.run(new CoffeeMachineActor.BrewCoffee(MOCHA_PLAY, baristaMessageAdapter.getRef()));
    testKit.run(new CoffeeMachineActor.BrewCoffee(AKKACINO, baristaMessageAdapter.getRef()));

    final var readyCoffees = baristaMessageAdapter.getAllReceived();
    assertEquals(1, readyCoffees.size());

    final var firstReadyCoffee = readyCoffees.getFirst();
    assertEquals(MOCHA_PLAY, firstReadyCoffee.coffee());
  }
}
