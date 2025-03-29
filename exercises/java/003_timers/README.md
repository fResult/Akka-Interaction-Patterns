# Exercise - Scheduling Messages

## Objective

In this exercise, you will reintroduce a brewing duration for the **CoffeeMachine** actor. Instead of using a blocking
`Thread.sleep()` call, you will implement the delay using actor timers, ensuring non-blocking and efficient execution.

## Key Changes

- The **CoffeeMachine** actor's brewing process should include a 10-second duration using an internal timer.
- The previous blocking implementation with `Thread.sleep()` has been removed and must not be reintroduced.

## Instructions

1. **Edit `CoffeeMachine.java`**:
    - Use a timer to implement the brewing duration of 10 seconds.
    - Ensure the brewing state transitions automatically after the timer expires.
    - Consider:
        - Sending a private message to the actor itself to handle the timer.
        - Maintaining appropriate state in the state machine for brewing and ready states.

2. **Run the Tests**:
    - Validate your implementation by running the unit tests:

      ```bash
      mvn clean test
      ```

3. **Hints**:
    - If you need to review the completed implementation, navigate to:
      `akka-interaction-patterns-for-java/solutions/003_timers`
    - An actor can send a message to itself. This can be useful for scheduling operations like timers.
    - You might need to add new states to the current state machine to handle brewing and post-brewing transitions.

## Reflection

- Should the timer message be private or public in your protocol? Why?
- How does using actor timers improve the scalability and efficiency of the **CoffeeMachine**?
- Compare this approach to the earlier blocking implementation. How does it affect the actor's responsiveness and system
  resource usage?
