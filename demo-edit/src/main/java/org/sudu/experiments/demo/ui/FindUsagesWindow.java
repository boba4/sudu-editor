package org.sudu.experiments.demo.ui;

import org.sudu.experiments.Const;
import org.sudu.experiments.demo.*;
import org.sudu.experiments.fonts.FontDesk;
import org.sudu.experiments.input.KeyCode;
import org.sudu.experiments.input.KeyEvent;
import org.sudu.experiments.math.V2i;
import org.sudu.experiments.parser.common.Pos;

import java.util.List;
import java.util.Objects;

public class FindUsagesWindow implements DprChangeListener, Focusable {
  private final FindUsagesDialog view = new FindUsagesDialog();
  private final UiContext context;
  private FontDesk font;
  private Runnable onClose = Const.emptyRunnable;
  private DialogItemColors theme;

  public FindUsagesWindow(UiContext context) {
    this.context = context;
    context.dprListeners.add(this);
    view.onClickOutside(this::hide);
  }

  // todo: change font and size if dpr changed on
  public void setFont(FontDesk f) {
    font = f;
    view.setFont(f);
  }

  public void setTheme(DialogItemColors dialogItemColors) {
    theme = dialogItemColors;
    view.setTheme(dialogItemColors);
  }

  public void display(V2i mousePos, FindUsagesItem[] actions, Runnable onClose) {
    if (font == null || isVisible()) {
      throw new IllegalArgumentException();
    }
    view.setItems(actions);
    view.measure(context);
    view.setScreenLimitedPosition(mousePos.x, mousePos.y, context.windowSize);
    context.setFocus(this);
    this.onClose = onClose;
  }

  public boolean hide() {
    if (isVisible()) {
      context.removeFocus(this);
      onClose.run();
      onClose = Const.emptyRunnable;
      view.dispose();
      return true;
    }
    return false;
  }

  @Override
  public void onDprChanged(float oldDpr, float newDpr) {
    view.measure(context);
  }

  public void center(V2i newSize) {
    V2i usageSize = view.size();
    view.setPos((newSize.x - usageSize.x) / 2, ((newSize.y - usageSize.y) / 2));
  }

  public void paint() {
    view.render(context);
  }

  public boolean onMouseMove(V2i mouse) {
    return view.onMouseMove(mouse, context.windowCursor);
  }

  public boolean onMousePress(V2i position, int button, boolean press, int clickCount) {
    return view.onMousePress(position, button, press, clickCount);
  }

  public boolean isVisible() {
    return !view.isEmpty();
  }

  public void dispose() {
    onClose = Const.emptyRunnable;
    context.removeFocus(this);
    view.dispose();
  }

  public final FindUsagesItem[] buildUsagesItems(List<Pos> usages, EditorComponent editorComponent) {
    return buildItems(usages, null, editorComponent);
  }

  public final FindUsagesItem[] buildDefItems(Location[] defs, EditorComponent editorComponent) {
    return buildItems(null, defs, editorComponent);
  }

  private String fileName(Uri uri) {
    return uri != null ? uri.getFileName() : "";
  }

  private FindUsagesItem[] buildItems(List<Pos> usages, Location[] defs, EditorComponent edit) {
    Model model = edit.model();
    Objects.requireNonNull(theme);

    FindUsagesItemBuilder tbb = new FindUsagesItemBuilder();
    int cnt = 0;
    int itemsLength = defs == null ? usages.size() : defs.length;
    for (int i = 0; i < itemsLength; i++) {
      int intLineNumber;
      String codeContent;
      String fileName;
      if (defs == null) {
        intLineNumber = usages.get(i).line;
        codeContent = model.document.line(intLineNumber).makeString().trim();
        fileName = fileName(model.uri);
      } else {
        intLineNumber = defs[i].range.startLineNumber;
        codeContent = Objects.equals(model.uri, defs[i].uri)
            ? model.document.line(intLineNumber).makeString().trim() : "";

        fileName = fileName(defs[i].uri);
      }
      // TODO: Move to FindUsagesDialog, implement formatter
      String codeContentFormatted = codeContent.length() > 43
              ? codeContent.substring(0, 40) + "..." : codeContent;
      String fileNameFormatted = fileName.length() > 43
              ? fileName.substring(0, 40) + "..." : fileName;
      String lineNumber = String.valueOf(intLineNumber + 1);

      if (++cnt > EditorConst.MAX_SHOW_USAGES_NUMBER) {
        tbb.addItem(
            "... and " + (itemsLength - (cnt - 1)) + " more usages",
            "",
            "",
            theme.findUsagesColorsContinued,
            () -> {
            }
        );
        break;
      }
      Location def;
      Pos pos;
      if (defs == null) {
        def = null;
        pos = usages.get(i);
      } else {
        pos = null;
        def = defs[i];
      }
      Runnable action = hideAnd(defs == null
          ? () -> edit.gotoUsage(pos)
          : () -> edit.gotoDefinition(def));
      tbb.addItem(
          fileNameFormatted,
          lineNumber,
          codeContentFormatted,
          theme.findUsagesColors,
          action
      );
    }
    return tbb.items();
  }

  private Runnable hideAnd(Runnable r) {
    return  () -> {
      hide();
      r.run();
    };
  }

  @Override
  public boolean onKeyPress(KeyEvent event) {
    if (!isVisible()) return false;
    return switch (event.keyCode) {
      case KeyCode.ESC -> hide();
      case KeyCode.ARROW_DOWN, KeyCode.ARROW_UP, KeyCode.ARROW_LEFT, KeyCode.ARROW_RIGHT ->
          view.onKeyArrow(event.keyCode);
      case KeyCode.ENTER -> view.goToSelectedItem();
      default -> false;
    };
  }
}
