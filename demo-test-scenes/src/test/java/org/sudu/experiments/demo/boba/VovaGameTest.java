package org.sudu.experiments.demo.boba;

import org.junit.jupiter.api.Assertions;
import org.sudu.experiments.demo.boba.VovaGame;

class VovaGameTest {
  @org.junit.jupiter.api.Test
  void intersect() {
    MovingRectangle m1 = new MovingRectangle();
    m1.position.set(0, 0);
    m1.size.set(1, 1);
    MovingRectangle m2 = new MovingRectangle();
    m2.size.set(1, 1);
    m2.position.set(2, 2);
    Assertions.assertTrue(m1.isIntersected(m1));
    Assertions.assertFalse(m1.isIntersected(m2));
    Assertions.assertFalse(m2.isIntersected(m1));
    m2.position.set(1, 1);
    Assertions.assertTrue(m1.isIntersected(m2));
    Assertions.assertTrue(m2.isIntersected(m1));
  }
}