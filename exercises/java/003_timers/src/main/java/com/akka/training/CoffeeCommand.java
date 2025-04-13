package com.akka.training;

import java.io.Serializable;

public interface CoffeeCommand extends Serializable {
  final class Akkaccino implements CoffeeCommand {
    @Override
    public String toString() {
      return this.getClass().getSimpleName();
    }

    @Override
    public boolean equals(Object o) {
      return o == this || o instanceof Akkaccino;
    }
  }

  final class CaffeJava implements CoffeeCommand {
    @Override
    public String toString() {
      return this.getClass().getSimpleName();
    }

    @Override
    public boolean equals(Object o) {
      return o == this || o instanceof CaffeJava;
    }
  }

  final class MochaPlay implements CoffeeCommand {
    @Override
    public String toString() {
      return this.getClass().getSimpleName();
    }

    @Override
    public boolean equals(Object o) {
      return o == this || o instanceof MochaPlay;
    }
  }
}
