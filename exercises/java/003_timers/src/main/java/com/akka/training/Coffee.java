package com.akka.training;

import java.io.Serializable;

public interface Coffee extends Serializable {

  final class Akkaccino implements Coffee {
    @Override
    public String toString() {
      return this.getClass().getSimpleName();
    }

    @Override
    public boolean equals(Object o) {
      return o == this || o instanceof Akkaccino;
    }
  }

  final class CaffeJava implements Coffee {
    @Override
    public String toString() {
      return this.getClass().getSimpleName();
    }

    @Override
    public boolean equals(Object o) {
      return o == this || o instanceof CaffeJava;
    }
  }

  final class MochaPlay implements Coffee {
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
