package org.sudu.experiments.demo.ui;

import org.sudu.experiments.*;
import org.sudu.experiments.demo.SetCursor;
import org.sudu.experiments.fonts.FontDesk;
import org.sudu.experiments.input.KeyEvent;
import org.sudu.experiments.math.V2i;

public class UiContext {

  public final WglGraphics graphics;
  public final Window window;
  public final V2i windowSize = new V2i();
  public final SetCursor windowCursor;
  public float dpr;

  private Focusable focused;

  public final Subscribers<DprChangeListener> dprListeners
      = new Subscribers<>(new DprChangeListener[0]);

  public UiContext(SceneApi api) {
    this.graphics = api.graphics;
    this.window = api.window;
    windowCursor = SetCursor.wrap(api.window);
    api.input.onFocus.add(this::sendFocusGain);
    api.input.onBlur.add(this::sendFocusLost);
  }

  public void onResize(V2i newSize, float newDpr) {
    windowSize.set(newSize);
    if (dpr != newDpr) {
      float oldDpr = dpr;
      dpr = newDpr;
      for (DprChangeListener listener : dprListeners.array()) {
        if (listener != null) listener.onDprChanged(oldDpr, newDpr);
      }
    }
  }

  public boolean onKeyPress(KeyEvent event) {
    return focused != null && focused.onKeyPress(event);
  }

  public void sendFocusGain() {
    if (focused != null) {
      focused.onFocusGain();
    }
  }

  public void sendFocusLost() {
    if (focused != null) {
      focused.onFocusLost();
    }
  }

  public void initFocus(Focusable f) {
    boolean hasFocus = window.hasFocus();
    if (hasFocus) sendFocusLost();
    focused = f;
    if (hasFocus) sendFocusGain();
  }

  public void setFocus(Focusable f) {
    sendFocusLost();
    focused = f;
    sendFocusGain();
  }

  public void removeFocus(Focusable f) {
    if (focused == f) {
      focused = null;
    }
  }

  public boolean isFocused(Focusable f) {
    return f == focused;
  }

  public Focusable focused() {
    return focused;
  }

  public Canvas mCanvas() { return graphics.mCanvas; }

  public void requireWindowVisible() {
    if (windowSize.x * windowSize.y == 0 || dpr == 0) {
      throw new IllegalStateException(
          "trying to display with unknown screen size and dpr");
    }
  }

  public FontDesk fontDesk(UiFont font) {
    return graphics.fontDesk(font.familyName, font.size, dpr);
  }
}
