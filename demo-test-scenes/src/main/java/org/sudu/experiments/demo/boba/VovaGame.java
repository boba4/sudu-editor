package org.sudu.experiments.demo.boba;


import org.sudu.experiments.Scene0;
import org.sudu.experiments.SceneApi;
import org.sudu.experiments.input.KeyCode;
import org.sudu.experiments.input.KeyEvent;
import org.sudu.experiments.input.MouseEvent;
import org.sudu.experiments.input.MouseListener;
import org.sudu.experiments.math.Color;
import org.sudu.experiments.math.V2i;
import org.sudu.experiments.math.V4f;

public class VovaGame extends Scene0 implements MouseListener {

  final World world = new World();

  private boolean left;
  private boolean right;
  private boolean up;
  private boolean down;
  private double lastTimeStamp;

  public VovaGame(SceneApi api) {
    super(api);
    api.input.onMouse.add(this);
    api.input.onKeyPress.add(this::onKey);
    api.window.timeNow();
  }

  @Override
  public void paint() {
    super.paint();
    world.paint(api);
  }

  @Override
  public boolean update(double timestamp) {
    float dT = (float) (timestamp - lastTimeStamp);
    updatePlayerControls();
    boolean changed = world.update(dT);
    lastTimeStamp = timestamp;
    return changed;
  }

  private void updatePlayerControls() {
    int xForce = (right ? 1 : 0) + (left ? -1 : 0);
//    realX += dT * playerSpeed * xForce;
    int yForce = (down ? 1 : 0) + (up ? -1 : 0);
//    realY += dT * playerSpeed * yForce;
    world.player.speed.set(World.playerSpeed * xForce, World.playerSpeed * yForce);
  }

//  private boolean updateBlinking(double timestamp) {
//    double f = timestamp * frequency;
//    int f1 = ((int)f) % 2;
//    boolean clr = f1 > 0;
//    V4f newColor = clr ? color1 : color2;
//    boolean changed = color != newColor;
//    if (changed) color = newColor;
//    return changed;
//  }

  @Override
  public void onResize(V2i newSize, float dpr) {
    if (this.dpr == 0) {
      world.initScene(newSize);
    }
    super.onResize(newSize, dpr);
    world.onResize(size);
  }


  public boolean onKey(KeyEvent event) {
    if (event.keyCode == KeyCode.ARROW_LEFT) left = event.isPressed;
    if (event.keyCode == KeyCode.ARROW_RIGHT) right = event.isPressed;
    if (event.keyCode == KeyCode.ARROW_DOWN) down = event.isPressed;
    if (event.keyCode == KeyCode.ARROW_UP) up = event.isPressed;
//    System.out.println("event = " + event);
    if (event.isPressed) {
      switch (event.keyCode) {
        case KeyCode.C -> System.out.println("C");
        case KeyCode.V -> System.out.println("V");
      }
    }
    return false;
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

  static V4f rngColor() {
    return Color.Cvt.fromHSV(Math.random(), 1, 0.5);
  }
}
