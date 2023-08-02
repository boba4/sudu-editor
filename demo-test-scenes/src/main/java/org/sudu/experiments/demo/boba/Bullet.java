package org.sudu.experiments.demo.boba;

import org.sudu.experiments.math.V2f;

public class Bullet extends MovingRectangle {
  private final World world;

  public Bullet(World world, V2f position, V2f speed) {
    this.world = world;
    int size1 = world.bulletSize;
    this.size.set(size1, size1);
    this.position.set(position);
    this.speed.set(speed);
  }

  @Override
  boolean update(float dT) {
    super.update(dT);
    boolean isAlive = true;
    V2f playerPosition = world.player.position;
    V2f playerSize = world.player.size;
    return isAlive;
  }
}
