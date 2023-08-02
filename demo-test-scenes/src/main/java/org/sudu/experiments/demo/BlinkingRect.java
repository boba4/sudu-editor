package org.sudu.experiments.demo;


import org.sudu.experiments.Scene0;
import org.sudu.experiments.SceneApi;
import org.sudu.experiments.input.InputListener;
import org.sudu.experiments.input.KeyCode;
import org.sudu.experiments.input.KeyEvent;
import org.sudu.experiments.input.MouseEvent;
import org.sudu.experiments.math.Color;
import org.sudu.experiments.math.V2i;
import org.sudu.experiments.math.V4f;

public class BlinkingRect extends Scene0 implements InputListener {
  public BlinkingRect(SceneApi api) {
    super(api);
    api.input.addListener(this);
    api.window.timeNow();
  }

  @Override
  public void paint() {
    super.paint();
    V4f color = Color.Cvt.fromHSV(Math.random(), 1, 0.5);
    api.graphics.drawRect(
        size.x / 3, size.y / 3,
        new V2i(size.x / 3, size.y / 3),
        color);
  }

  @Override
  public boolean update(double timestamp) {
    return false;
  }

  @Override
  public void onResize(V2i newSize, double dpr) {
    super.onResize(newSize, dpr);
  }

  @Override
  public boolean onKey(KeyEvent event) {
    System.out.println("event = " + event);
    if (event.isPressed) {
      switch (event.keyCode) {
        case KeyCode.C -> System.out.println("C");
        case KeyCode.V -> System.out.println("V");
      }
    }
    return true;
  }

  @Override
  public boolean onMousePress(MouseEvent event, int button, boolean press, int clickCount) {
    if (press) {
      switch (button) {
        case MOUSE_BUTTON_LEFT -> System.out.println("left");
        case MOUSE_BUTTON_RIGHT -> System.out.println("right");
      }
    }
    return true;
  }
}
