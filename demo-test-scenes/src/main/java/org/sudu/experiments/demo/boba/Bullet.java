package org.sudu.experiments.demo.boba;

import org.sudu.experiments.math.V2f;

public class Bullet extends MovingRectangle {
  private final World world;
  float lifeTime;

  public Bullet(World world, V2f position, V2f speed, float lifeTime) {
    this.world = world;
    this.lifeTime = lifeTime;
    int size1 = world.bulletSize;
    this.size.set(size1, size1);
    this.position.set(position);
    this.speed.set(speed);
  }

  @Override
  boolean update(float dT) {
    super.update(dT);
    lifeTime -= dT;
    boolean isAlive = lifeTime > 0;
    V2f playerPosition = world.player.position;
    V2f playerSize = world.player.size;
    return isAlive;
  }
}
