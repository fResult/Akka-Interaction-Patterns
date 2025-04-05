package com.akka.training;

import static com.akka.training.TestsUtils.*;
import static org.junit.Assert.assertEquals;

import akka.actor.testkit.typed.javadsl.BehaviorTestKit;
import akka.actor.testkit.typed.javadsl.TestInbox;
import java.util.List;
import org.junit.Test;

public class CoffeeMachineActorTests {
  private final Coffee AKKACINO = new Coffee.Akkaccino();
  private final Coffee MOCHA_PLAY = new Coffee.MochaPlay();

  // CoffeeMachine should transition from IDLE to Brewing on receiving BrewCoffee message
  @Test
  public void brewing() {
    BehaviorTestKit<CoffeeMachineActor.CoffeeMachineCommand> testKit =
        BehaviorTestKit.create(CoffeeMachineActor.create());
    TestInbox<BaristaActor.BaristaCommand> baristaInbox = TestInbox.create();

    assertEquals(expectedInfoLog("CoffeeMachine: IDLE"), lastCapturedLogEvent(testKit));
    testKit.clearLog();
    testKit.run(new CoffeeMachineActor.BrewCoffee(AKKACINO, baristaInbox.getRef()));
    assertEquals(
        expectedInfoLog(String.format("CoffeeMachine: Brewing 1 %s", AKKACINO)),
        offsetCapturedLogEvent(testKit, 1));
  }

  // CoffeeMachine should transition between its states along the full cycle
  @Test
  public void fullCycle() {
    BehaviorTestKit<CoffeeMachineActor.CoffeeMachineCommand> testKit =
        BehaviorTestKit.create(CoffeeMachineActor.create());
    TestInbox<BaristaActor.BaristaCommand> baristaInbox = TestInbox.create();
    assertEquals(expectedInfoLog("CoffeeMachine: IDLE"), lastCapturedLogEvent(testKit));
    testKit.clearLog();
    testKit.run(new CoffeeMachineActor.BrewCoffee(AKKACINO, baristaInbox.getRef()));
    assertEquals(
        expectedInfoLog("CoffeeMachine: Brewing 1 Akkaccino"), offsetCapturedLogEvent(testKit, 1));
    assertEquals(
        expectedInfoLog(String.format("CoffeeMachine: Coffee %s is ready", AKKACINO)),
        lastCapturedLogEvent(testKit));

    testKit.run(new CoffeeMachineActor.PickupCoffee());

    assertEquals(expectedInfoLog("CoffeeMachine: IDLE"), lastCapturedLogEvent(testKit));
  }

  // CoffeeMachine should be able to start a new cycle after completing one
  @Test
  public void restartCycle() {
    BehaviorTestKit<CoffeeMachineActor.CoffeeMachineCommand> testKit =
        BehaviorTestKit.create(CoffeeMachineActor.create());
    TestInbox<BaristaActor.BaristaCommand> baristaInbox = TestInbox.create();

    testKit.run(new CoffeeMachineActor.BrewCoffee(AKKACINO, baristaInbox.getRef()));
    testKit.run(new CoffeeMachineActor.PickupCoffee());
    assertEquals(expectedInfoLog("CoffeeMachine: IDLE"), lastCapturedLogEvent(testKit));
    testKit.run(new CoffeeMachineActor.BrewCoffee(MOCHA_PLAY, baristaInbox.getRef()));
    assertEquals(
        expectedInfoLog(String.format("CoffeeMachine: Brewing 1 %s", MOCHA_PLAY)),
        offsetCapturedLogEvent(testKit, 1));
    assertEquals(
        expectedInfoLog(String.format("CoffeeMachine: Coffee %s is ready", MOCHA_PLAY)),
        lastCapturedLogEvent(testKit));
  }

  @Test
  public void shouldSendReadySignalToBarista() {
    var coffee = new Coffee.CaffeJava();

    BehaviorTestKit<CoffeeMachineActor.CoffeeMachineCommand> testKit =
        BehaviorTestKit.create(CoffeeMachineActor.create());
    TestInbox<BaristaActor.BaristaCommand> baristaInbox = TestInbox.create();

    testKit.run(new CoffeeMachineActor.BrewCoffee(coffee, baristaInbox.getRef()));

    List<BaristaActor.BaristaCommand> coffeesReady = baristaInbox.getAllReceived();
    assertEquals(1, coffeesReady.size());

    BaristaActor.CoffeeReady readyCoffee = (BaristaActor.CoffeeReady) coffeesReady.get(0);
    assertEquals(coffee, readyCoffee.coffee);
  }

  @Test
  public void shouldNotProcessCoffeesUntilReset() {
    var coffee1 = new Coffee.MochaPlay();
    var coffee2 = new Coffee.Akkaccino();

    BehaviorTestKit<CoffeeMachineActor.CoffeeMachineCommand> testKit =
        BehaviorTestKit.create(CoffeeMachineActor.create());
    TestInbox<BaristaActor.BaristaCommand> baristaInbox = TestInbox.create();

    testKit.run(new CoffeeMachineActor.BrewCoffee(coffee1, baristaInbox.getRef()));
    testKit.run(new CoffeeMachineActor.BrewCoffee(coffee2, baristaInbox.getRef()));

    List<BaristaActor.BaristaCommand> coffeesReady = baristaInbox.getAllReceived();
    assertEquals(1, coffeesReady.size());

    BaristaActor.CoffeeReady readyCoffee = (BaristaActor.CoffeeReady) coffeesReady.get(0);
    assertEquals(coffee1, readyCoffee.coffee);
  }
}
