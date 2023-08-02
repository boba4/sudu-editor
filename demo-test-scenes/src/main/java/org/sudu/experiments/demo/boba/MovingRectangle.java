package org.sudu.experiments.demo.boba;

import org.sudu.experiments.WglGraphics;
import org.sudu.experiments.math.V2f;
import org.sudu.experiments.math.V4f;

class MovingRectangle {
  final V2f position = new V2f();
  final V2f size = new V2f();
  final V2f speed = new V2f();
  final V4f color = VovaGame.rngColor();

  boolean update(float dT) {
    position.x += speed.x * dT;
    position.y += speed.y * dT;
    return speed.x != 0 || speed.y != 0;
  }
  boolean isIntersected(MovingRectangle r) {
    float xA0 = position.x;
    float xA1 = position.x + size.x;
    float xB0 = r.position.x;
    float xB1 = r.position.x + r.size.x;
    
    float yA0 = position.y;
    float yA1 = position.y + size.y;
    float yB0 = r.position.y;
    float yB1 = r.position.y + r.size.y;

    boolean bx = (Math.min(xA1, xA0) <= Math.max(xB1, xB0) && Math.min(xA1, xA0) >= Math.min(xB1, xB0)) ||
        (Math.max(xA1, xA0) <= Math.max(xB1, xB0) && Math.max(xA1, xA0) >= Math.min(xB1, xB0));
    boolean by = (Math.min(yA1, yA0) <= Math.max(yB1, yB0) && Math.min(yA1, yA0) >= Math.min(yB1, yB0)) ||
        (Math.max(yA1, yA0) <= Math.max(yB1, yB0) && Math.max(yA1, yA0) >= Math.min(yB1, yB0));
    return bx && by;
  }

  void draw(WglGraphics g) {
    g.drawRect(position.x, position.y, size, color);
  }

}
