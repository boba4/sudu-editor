package org.sudu.experiments.demo.ui;

import org.sudu.experiments.*;
import org.sudu.experiments.demo.DemoRect;
import org.sudu.experiments.demo.SetCursor;
import org.sudu.experiments.demo.TextRect;
import org.sudu.experiments.fonts.FontDesk;
import org.sudu.experiments.math.Rect;
import org.sudu.experiments.math.V2i;
import org.sudu.experiments.math.V4f;

import java.util.Objects;

public class Toolbar {
  static final int dpMargin = 3;
  static final int dpBorder = 2;
  static final int textHorizontalMargin = 12;
  static final float textHeightScale = 1.25f;

  private FontDesk font;
  private final DemoRect rect = new DemoRect();
  private final V2i textureSize = new V2i();
  private final V2i v2i = new V2i();
  private ShadowParameters shadowParameters;
  private ToolbarItem[] items = ToolbarItemBuilder.items0;
  private GL.Texture texture;
  private int border, margin, textXPad;
  private int hoverItemId = -1;
  boolean isVertical;

  private Runnable onClickOutside;
  private HoverCallback onEnter;
  private HoverCallback onLeave;

  public interface HoverCallback {
    void event(V2i mouse, int index, ToolbarItem item);
  }

  public void setItems(ToolbarItem[] actions) {
    invalidateTexture();
    items = actions;
  }

  public void onClickOutside(Runnable action) {
    onClickOutside = action;
  }

  public void onEnter(HoverCallback callback) {
    onEnter = callback;
  }

  public void onLeave(HoverCallback callback) {
    onLeave = callback;
  }

  private void setBgColor(V4f bgColor) {
    rect.color.set(bgColor);
  }

  private void setFrameColor(V4f bgColor) {
    rect.bgColor.set(bgColor);
  }

  public void setFont(FontDesk font) {
    this.font = font;
    invalidateTexture();
  }

  public void setTheme(DialogItemColors dialogItemColors) {
    shadowParameters = dialogItemColors.shadowParameters;
    setBgColor(dialogItemColors.toolbarItemColors.bgColor);
    setFrameColor(dialogItemColors.dialogBorderColor);
    for (int i = 0; i < items.length; i++) {
      items[i].setTheme(dialogItemColors.toolbarItemColors);
      if (hoverItemId == i) items[i].setHover(true);
    }
  }

  public void dispose() {
    disposeTexture();
    items = ToolbarItemBuilder.items0;
    hoverItemId = -1;
    rect.makeEmpty();
  }

  private void disposeTexture() {
    texture = Disposable.assign(texture, null);
    textureSize.set(0, 0);
  }

  public void measure(UiContext uiContext) {
    Canvas mCanvas = uiContext.mCanvas();
    float devicePR = uiContext.dpr;
    Objects.requireNonNull(font);
    mCanvas.setFont(font);
    int textHeight = font.lineHeight(textHeightScale), maxW = 0;
    border = DprUtil.toPx(dpBorder, devicePR);
    margin = DprUtil.toPx(dpMargin, devicePR);
    textXPad = DprUtil.toPx(textHorizontalMargin, devicePR);
    int tw = 0;
    for (ToolbarItem item : items) {
      int m = (int)(mCanvas.measureText(item.text) + 7.f / 8);
      int w = textXPad + m + textXPad;
      maxW = Math.max(maxW, w);

      item.tRect.pos.x = tw;
      item.tRect.pos.y = 0;
      item.tRect.size.x = w;
      item.tRect.size.y = textHeight;
      item.tRect.textureRegion.set(tw, 0, w, textHeight);
      tw += w;
    }
    textureSize.x = tw;
    textureSize.y = textHeight;
    rect.size.x = isVertical
        ? maxW + border * 2 + margin * 2
        : tw + border + border * items.length;
    rect.size.y = isVertical
        ? (textHeight + border) * items.length + border + margin * 2
        : textHeight + border * 2;
  }

  public void setPos(int x, int y) {
    rect.pos.set(x, y);
    int localX = border + margin, localY = border + margin;
    for (ToolbarItem item : items) {
      TextRect tRect = item.tRect;
      tRect.pos.x = x + localX;
      tRect.pos.y = y + localY;
      if (isVertical) {
        if (tRect.size.y == 0) tRectWarning();
        localY += tRect.size.y + border;
      } else {
        if (tRect.size.x == 0) tRectWarning();
        localX += tRect.size.x + border;
      }
    }
  }

  private static void tRectWarning() {
    Debug.consoleInfo("Toolbar.setPos: tRect.size == 0");
  }

  public V2i size() {
    if (textureSize.x == 0 || textureSize.y == 0) {
      throw new RuntimeException("toolbar size is unknown");
    }
    return rect.size;
  }

  private void renderTexture(WglGraphics g) {
    Canvas canvas = g.createCanvas(textureSize.x, textureSize.y);
    canvas.setFont(font);
    int textMargin = font.lineHeight(textHeightScale * .5f - .5f);
    float baseline = textMargin + font.fAscent - (font.fAscent + font.fDescent) / 16;

    for (ToolbarItem item : items) {
      canvas.drawText(item.text, item.tRect.textureRegion.x + textXPad, baseline);
    }
    texture = Disposable.assign(texture, g.createTexture());
    texture.setContent(canvas);
    canvas.dispose();
  }

  public void invalidateTexture() {
    textureSize.set(0, 0);
  }

  public void render(UiContext context) {
    WglGraphics g = context.graphics;
    if (items.length == 0) return;
    if (texture == null || textureSize.x * textureSize.y == 0) {
      if (textureSize.x * textureSize.y == 0) measure(context);
      if (textureSize.x * textureSize.y == 0) return;
      renderTexture(g);
    }

    if (!rect.isEmpty()) {
      drawFrameAndShadow(g, context);
    }

    for (ToolbarItem item : items) {
      item.tRect.drawText(g, texture, 0, 0, 0);
    }
    if (isVertical) {
      for (ToolbarItem item : items) {
        TextRect tRect = item.tRect;
        v2i.x = rect.size.x - border * 2 - margin * 2 - tRect.size.x;
        v2i.y = tRect.size.y;
        if (v2i.x > 0) {
          g.drawRect(tRect.pos.x + tRect.size.x, tRect.pos.y,
              v2i, tRect.bgColor);
        }
      }
    }
  }

  private void drawFrameAndShadow(WglGraphics g, UiContext context) {
    int shadowSize = shadowParameters.getShadowSize(context.dpr);

    // frame
    v2i.x = rect.size.x;
    v2i.y = border;
    g.drawRect(rect.pos.x, rect.pos.y, v2i, rect.bgColor);
    g.drawRect(rect.pos.x, rect.pos.y + rect.size.y - border, v2i, rect.bgColor);

    v2i.x = border;
    v2i.y = rect.size.y - border - border;
    g.drawRect(rect.pos.x, rect.pos.y + border, v2i, rect.bgColor);
    g.drawRect(rect.pos.x + rect.size.x - border, rect.pos.y + border, v2i, rect.bgColor);

    // body
    v2i.x = rect.size.x - border - border;
    v2i.y = rect.size.y - border - border;
    g.drawRect(rect.pos.x + border, rect.pos.y + border, v2i, rect.color);

    // shadow
    if (isVertical) {
      v2i.x = rect.size.x;
      v2i.y = shadowSize;
      g.drawRect(rect.pos.x + shadowSize, rect.pos.y + rect.size.y, v2i, shadowParameters.color);
      g.drawRect(rect.pos.x + shadowSize, rect.pos.y + rect.size.y, v2i, shadowParameters.color);
      g.drawRect(rect.pos.x + shadowSize * 2, rect.pos.y + rect.size.y + shadowSize, v2i, shadowParameters.color);

      v2i.x = shadowSize;
      v2i.y = rect.size.y - shadowSize;
      g.drawRect(rect.pos.x + rect.size.x, rect.pos.y + shadowSize, v2i, shadowParameters.color);
      g.drawRect(rect.pos.x + rect.size.x, rect.pos.y + shadowSize, v2i, shadowParameters.color);
      g.drawRect(rect.pos.x + rect.size.x + shadowSize, rect.pos.y + shadowSize * 2, v2i, shadowParameters.color);
    }
  }

  public boolean onMouseMove(V2i pos, SetCursor setCursor) {
    boolean inside = rect.isInside(pos);
    int mouseItem = inside ? find(pos) : -1;

    if (hoverItemId != mouseItem) {
      if (hoverItemId >= 0) {
        ToolbarItem oldItem = items[hoverItemId];
        oldItem.setHover(false);
        if (onLeave != null) onLeave.event(pos, hoverItemId, oldItem);
      }
      if (mouseItem >= 0) {
        ToolbarItem newItem = items[mouseItem];
        newItem.setHover(true);
        if (onEnter != null) onEnter.event(pos, mouseItem, newItem);
      }
      hoverItemId = mouseItem;
    }
    return inside && setCursor.setDefault();
  }

  public boolean onMousePress(V2i pos, int button, boolean press, int clickCount) {
    if (!rect.isInside(pos)) {
      if (press && !rect.isEmpty() && onClickOutside != null) onClickOutside.run();
      return false;
    }
    if (clickCount == 1 && press) {
      int index = find(pos);
      if (index >= 0) {
        ToolbarItem item = items[index];
        if (!item.isSubmenu()) {
          item.action.run();
        }
      }
    }
    return true;
  }

  private int find(V2i pos) {
    for (int i = 0; i < items.length; i++) {
      ToolbarItem item = items[i];
      TextRect tRect = item.tRect;
      if (tRect.isInside(pos)) {
        return i;
      }
      if (isVertical) {
        int x = tRect.pos.x + tRect.size.x;
        int y = tRect.pos.y;
        v2i.x = rect.size.x - border * 2 - tRect.size.x;
        v2i.y = tRect.size.y;
        if (Rect.isInside(pos, x, y, v2i)) {
          return i;
        }
      }
    }
    return -1;
  }

  public void setLayoutVertical() {
    isVertical = true;
  }

  public int border() {
    return border;
  }

  public int margin() {
    return margin;
  }

}
