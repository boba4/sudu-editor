package org.sudu.experiments.demo;

import org.sudu.experiments.*;
import org.sudu.experiments.fonts.FontDesk;
import org.sudu.experiments.math.V2i;
import org.sudu.experiments.math.V4f;

public class LineNumbersTexture implements Disposable {

  private GL.Texture lineTexture;

  private final V2i texturePos;
  private final V2i textureSize;
  private final int numberOfLines;
  private final int lineHeight;

  private final V2i rectSize = new V2i();
  private final V4f rectRegion = new V4f();

  private final int baseline;

  public LineNumbersTexture(
    V2i texturePos,
    int numberOfLines,
    int textureWidth,
    int lineHeight,
    FontDesk fontDesk
  ) {
    this.texturePos = texturePos;
    this.numberOfLines = numberOfLines;
    this.lineHeight = lineHeight;
    this.textureSize = new V2i(textureWidth, this.numberOfLines * lineHeight);
    this.baseline = CodeLineRenderer.baselineShift(fontDesk, lineHeight);
  }

  public int updateTexture(
      Canvas textureCanvas, Canvas updateCanvas,
      int curFirstLine, int firstLine, int updateOn, double devicePR
  ) {
    if (firstLine > curFirstLine)
      return scrollDown(textureCanvas, updateCanvas, firstLine, updateOn, curFirstLine, devicePR);
    else
      return scrollUp(textureCanvas, updateCanvas, firstLine, updateOn, curFirstLine, devicePR);
  }

  public void createTexture(final WglGraphics g) {
    if (lineTexture == null) lineTexture = g.createTexture();
  }

  public int initTexture(Canvas textureCanvas, int startNum, int toNum, int texturesSize, double devicePR) {
    textureCanvas.clear();

    for (int i = 0; i < numberOfLines; i++) {
      if (startNum - texturesSize * numberOfLines >= toNum)
        startNum -= texturesSize * numberOfLines;

      String lineNum = String.valueOf(startNum++ + 1);
      drawLine(textureCanvas, lineNum, lineHeight * i + baseline, devicePR);
    }

    lineTexture.setContent(textureCanvas);
    return startNum;
  }

  public void draw(
      V2i dXdY, int componentHeight, int scrollPos, int fullTexturesSize,
      LineNumbersColors colorScheme, WglGraphics g
  ) {
    int height = textureSize.y;
    int yPos = ((texturePos.y - (scrollPos % fullTexturesSize)) + fullTexturesSize) % fullTexturesSize;

    if ((yPos + height) <= componentHeight) {
      rectSize.set(lineTexture.width(), height);
      rectRegion.set(0, 0, lineTexture.width(), height);

      draw(g, yPos, dXdY, colorScheme.textColor, colorScheme.bgColor);
    } else {
      if (yPos + height > componentHeight && yPos < componentHeight) {
        int topHeight = Math.max(componentHeight - yPos, 0);
        rectSize.set(lineTexture.width(), topHeight);
        rectRegion.set(0, 0, lineTexture.width(), topHeight);

        draw(g, yPos, dXdY, colorScheme.textColor, colorScheme.bgColor);
      }
      if (yPos + height > fullTexturesSize) {
        height = (yPos + height) % fullTexturesSize;
        height = Math.min(height, componentHeight);
        rectSize.set(lineTexture.width(), height);
        rectRegion.set(0, scrollPos % lineTexture.height(), lineTexture.width(), height);

        draw(g, 0, dXdY, colorScheme.textColor, colorScheme.bgColor);
      }
    }
  }

  void drawCurrentLine(
      WglGraphics g, V2i dXdY,
      int scrollPos, int fullTexturesSize, int caretLine,
      LineNumbersColors colorScheme
  ) {
    int height = textureSize.y;
    int yPos = ((texturePos.y - (scrollPos % fullTexturesSize)) + fullTexturesSize) % fullTexturesSize;
    if (yPos + height > fullTexturesSize) yPos = -(scrollPos % lineTexture.height());
    yPos += (caretLine % numberOfLines) * lineHeight;
    if (yPos < -lineHeight) yPos += fullTexturesSize;

    rectSize.set(textureSize.x, lineHeight);
    rectRegion.set(0, (caretLine % numberOfLines) * lineHeight, textureSize.x, lineHeight);

    draw(g, yPos, dXdY, colorScheme.caretTextColor, colorScheme.caretBgColor);
  }

  private void draw(WglGraphics g, int yPos, V2i dXdY, V4f textColor, V4f bgColor) {
    g.drawText(texturePos.x + dXdY.x, yPos + dXdY.y,
      rectSize,
      rectRegion,
      lineTexture,
      textColor, bgColor, 0f);
  }

  private int scrollDown(
      Canvas textureCanvas, Canvas updateCanvas,
      int firstLine, int updateOn, int curFirstLine, double devicePR
  ) {
    int stNum = (curFirstLine / numberOfLines) * numberOfLines;
    int endNum = stNum + numberOfLines;

    if (curFirstLine + numberOfLines / 3 < Math.min(endNum, firstLine - 1)) {
      textureCanvas.clear();
      for (int i = 0; i < numberOfLines; i++) {
        int line = stNum++ + 1;
        if (line > firstLine) line -= updateOn;

        String lineNum = String.valueOf(line + updateOn);
        drawLine(textureCanvas, lineNum, lineHeight * i + baseline, devicePR);
      }
      lineTexture.setContent(textureCanvas);
      return Math.min(endNum, firstLine);
    }

    while (curFirstLine < firstLine) {
      updateCanvas.clear();

      String lineNum = String.valueOf(curFirstLine + updateOn + 1);
      int yPos = (curFirstLine++ * lineHeight) % textureSize.y;

      drawLine(updateCanvas, lineNum, baseline, devicePR);
      lineTexture.update(updateCanvas, 0, yPos);
      if (curFirstLine % numberOfLines == 0) break;
    }
    return curFirstLine;
  }

  private int scrollUp(
      Canvas textureCanvas, Canvas updateCanvas,
      int firstLine, int updateOn, int curFirstLine, double devicePR
  ) {
    int stNum = (curFirstLine / numberOfLines) * numberOfLines;
    int endNum = stNum + numberOfLines;

    if (curFirstLine - numberOfLines / 3 > Math.max(stNum, firstLine)) {
      textureCanvas.clear();
      for (int i = numberOfLines - 1; i >= 0; i--) {
        int line = endNum--;
        if (line <= firstLine) line += updateOn;

        String lineNum = String.valueOf(line);
        drawLine(textureCanvas, lineNum, lineHeight * i + baseline, devicePR);
      }
      lineTexture.setContent(textureCanvas);
      return Math.max(stNum, firstLine);
    }

    while (curFirstLine > firstLine) {
      updateCanvas.clear();

      String lineNum = String.valueOf(curFirstLine--);
      int yPos = (curFirstLine * lineHeight) % textureSize.y;

      drawLine(updateCanvas, lineNum, baseline, devicePR);
      lineTexture.update(updateCanvas, 0, yPos);
      if (curFirstLine % numberOfLines == 0) break;
    }
    return curFirstLine;
  }

  private void drawLine(Canvas canvas, String lineNumber, int yPos, double devicePR) {
    int padding = EditorConst.LINE_NUMBERS_RIGHT_PADDING;
    canvas.drawText(lineNumber, (float) (textureSize.x - padding * devicePR), yPos);
  }

  @Override
  public void dispose() {
    lineTexture = Disposable.assign(lineTexture, null);
  }

}
