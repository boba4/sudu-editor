package org.sudu.experiments.demo.boba;

public class Mob extends MovingRectangle {
  private final World world;

  public Mob(World world) {
    this.world = world;
  }

  @Override
  boolean update(float dT) {
    boolean update = super.update(dT);
    float x0 = 0 - World.mobWorldExtend;
    float x1 = world.size.x + World.mobWorldExtend;
    float y0 = 0 - World.mobWorldExtend;
    float y1 = world.size.y + World.mobWorldExtend;
    if (position.x <= x0) {
      speed.x *= -1;
      position.x = 2 * x0 - position.x;
    }
    if (position.y <= y0) {
      speed.y *= -1;
      position.y = 2 * y0 - position.y;
    }
    if (position.x + size.x >= x1) {
      speed.x *= -1;
      float x2 = 2 * x1 - (position.x + size.x);
      position.x = x2 - size.x;
    }
    if (position.y + size.y >= y1) {
      speed.y *= -1;
      float y2 = 2 * y1 - (position.y + size.y);
      position.y = y2  - size.y;
    }
    return update;
  }
}
