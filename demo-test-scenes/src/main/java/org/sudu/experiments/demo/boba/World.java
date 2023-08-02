package org.sudu.experiments.demo.boba;

import org.sudu.experiments.SceneApi;
import org.sudu.experiments.math.Color;
import org.sudu.experiments.math.V2f;
import org.sudu.experiments.math.V2i;
import org.sudu.experiments.math.XorShiftRandom;

public class World {
  public static final float mobSpeed = 300;
  public static final float mobWorldExtend = -20;
  public static final float playerSpeed = 175;

  public final V2f size = new V2f();
  final MovingRectangle player = new MovingRectangle();
  final MovingRectangle[] mobs = new MovingRectangle[5];

  public World() {
    for (int i = 0; i < mobs.length; i++) {
      mobs[i] = new Mob(this);
    }
  }

  public void initScene(V2i newSize) {
    player.position.set(newSize.x * .5f, newSize.y * .5f);
    XorShiftRandom r = new XorShiftRandom();
    for (int i = 0; i < mobs.length; i++) {
      MovingRectangle mob = mobs[i];
      mob.position.set(
          r.nextFloat() * newSize.x,
          r.nextFloat() * newSize.y);
      Color.Cvt.fromHSV(i * 1.0 / (mobs.length + 1), 1, 1,
          mob.color);
      mob.speed.set(
          (r.nextFloat() - .5f) * mobSpeed,
          (r.nextFloat() - .5f) * mobSpeed);
    }
    Color.Cvt.fromHSV(mobs.length * 1.0 / (mobs.length + 1), 1, 1,
        player.color);
  }


  public void paint(SceneApi api) {
    player.draw(api.graphics);
    for (MovingRectangle mob : mobs) {
      mob.draw(api.graphics);
    }
  }

  public boolean update(float dT) {
    boolean changed = player.update(dT);
    for (MovingRectangle mob : mobs) {
      changed = mob.update(dT) || changed;
    }
    return changed;
  }
  public void onResize(V2i size) {
    this.size.set(size.x, size.y);
    int minSize = Math.min(size.x, size.y);
    int rectSize = minSize / 20;
    player.size.set(rectSize, rectSize);
    for (MovingRectangle mob : mobs) {
      mob.size.set(rectSize, rectSize);
    }
  }
}
