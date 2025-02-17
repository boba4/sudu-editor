package org.sudu.experiments.demo;

import org.sudu.experiments.Debug;
import org.sudu.experiments.demo.worker.IntervalTree;
import org.sudu.experiments.math.ArrayOp;
import org.sudu.experiments.math.V2i;
import org.sudu.experiments.parser.Interval;
import org.sudu.experiments.parser.common.Pos;

import java.util.*;
import java.util.function.BiConsumer;

public class Document {
  public static final char newLine = '\n';

  CodeLine[] document;
  public IntervalTree tree;
  public final Map<Pos, Pos> usageToDef = new HashMap<>();
  public final Map<Pos, List<Pos>> defToUsages = new HashMap<>();
  List<Diff[]> diffs = new ArrayList<>();

  int currentVersion;
  int lastParsedVersion;
  double lastDiffTimestamp;

  public Document() {
    document = CodeLine.singleElementLine("");
    currentVersion = lastParsedVersion = 0;
    tree = initialInterval();
  }

  public Document(CodeLine ... data) {
    if (data.length == 0) throw new IllegalArgumentException();
    document = data;
    currentVersion = lastParsedVersion = 0;
    tree = initialInterval();
  }

  public Document(CodeLine[] data, List<Interval> intervalList) {
    if (data.length == 0) throw new IllegalArgumentException();
    document = data;
    currentVersion = lastParsedVersion = 0;
    tree = new IntervalTree(intervalList);
  }

  public Document(int n) {
    this(TestText.document(n, false));
    tree = initialInterval();
  }

  public Document(String[] text) {
    this(CodeLine.makeLines(text));
  }

  private IntervalTree initialInterval() {
    return IntervalTree.singleInterval(0, getFullLength(), 0);
  }

  public CodeLine line(int i) {
    return document[i];
  }

  public char getChar(int line, int pos) {
    return line(line).getChar(pos);
  }

  public void invalidateFont() {
    for (CodeLine codeLine : document) {
      codeLine.invalidateCache();
    }
  }

  public int length() {
    return document.length;
  }

  public int getFullLength() {
    return getLineStartInd(length());
  }

  public void clear() {
    diffs.clear();
    usageToDef.clear();
    defToUsages.clear();

    currentVersion = lastParsedVersion = 0;
    tree = initialInterval();
  }

  public void setContent(String[] newLines) {
    diffs.clear();
    usageToDef.clear();
    defToUsages.clear();
    document = CodeLine.makeLines(newLines);
    currentVersion = lastParsedVersion = 0;
    tree = initialInterval();
  }

  public int strLength(int i) {
    return document[i].totalStrLength;
  }

  public void setLine(int ind, CodeLine line) {
    document[ind] = line;
  }

  public void newLineOp(int caretLine, int caretCharPos) {
    CodeLine line = document[caretLine];

    document = Arrays.copyOf(document, document.length + 1);
    for (int pos = document.length - 1; pos - 1 > caretLine; pos--) {
      document[pos] = document[pos - 1];
    }

    if (caretCharPos == 0) {
      document[caretLine] = new CodeLine();
      document[caretLine + 1] = line;
    } else if (caretCharPos == line.totalStrLength) {
      document[caretLine] = line;
      document[caretLine + 1] = new CodeLine();
    } else {
      CodeLine[] split = line.split(caretCharPos);
      document[caretLine] = split[0];
      document[caretLine + 1] = split[1];
    }

    makeDiff(caretLine, caretCharPos, false, "\n");
  }

  public void concatLines(int caretLine) {
    concatLinesOp(caretLine);
    makeDiff(caretLine, strLength(caretLine), true, "\n");
  }

  private void concatLinesOp(int caretLine) {
    CodeLine newLine = CodeLine.concat(document[caretLine], document[caretLine + 1]);
    CodeLine[] doc = deleteLineOp(caretLine);
    doc[caretLine] = newLine;
    document = doc;
  }

  public void deleteLine(int caretLine) {
    String deleted = line(caretLine).makeString().concat("\n");
    if (document.length > 1) {
      document = deleteLineOp(caretLine);
    } else {
      document[0].delete(0);
    }

    makeDiff(caretLine, 0, true, deleted);
  }

  public String copyLine(int caretLine) {
    return document[caretLine].makeString().concat("\n");
  }

  private CodeLine[] deleteLineOp(int caretLine) {
    if (caretLine >= document.length || caretLine < 0) throw new RuntimeException();
    CodeLine[] doc = new CodeLine[document.length - 1];
    ArrayOp.remove(document, caretLine, doc);
    return doc;
  }

  public void deleteLines(int fromLine, int toLine) {
    document = deleteLinesOp(fromLine, toLine);
  }

  private CodeLine[] deleteLinesOp(int fromLine, int toLine) {
    if (fromLine >= document.length || fromLine < 0) throw new RuntimeException();
    if (toLine > document.length || toLine < 0) throw new RuntimeException();

    CodeLine[] doc = new CodeLine[document.length - toLine + fromLine];
    ArrayOp.remove(document, fromLine, toLine, doc);
    return doc;
  }

  public void deleteChar(int caretLine, int caretCharPos) {
    if (isLastPosition(caretLine, caretCharPos)) {
      // do nothing at the document end
      if (caretLine != document.length - 1) {
        concatLinesOp(caretLine);
      }
    } else {
      makeDiff(caretLine, caretCharPos, true, String.valueOf(getChar(caretLine, caretCharPos)));
      document[caretLine].deleteAt(caretCharPos);
    }
  }

  public void insertAt(int line, int pos, String value) {
    document[line].insertAt(pos, value);
  }

  public void insertLines(int line, int pos, String[] lines) {
    insertLinesOp(line, pos, lines);
    makeDiff(line, pos, false, String.join("\n", lines));
  }

  private void insertLinesOp(int line, int pos, String[] lines) {
    if (lines.length == 0) return;
    if (lines.length == 1) {
      document[line].insertAt(pos, lines[0]);
      return;
    }
    int len = lines.length - 1;

    CodeLine[] splited = document[line].split(pos);
    CodeLine[] doc = Arrays.copyOf(document, document.length + len);

    for (int p = doc.length - 1; p - len > line; p--) {
      doc[p] = doc[p - len];
    }
    splited[0].insertToEnd(lines[0]);
    doc[line] = splited[0];
    for (int i = 1; i < len; i++) {
      CodeLine newLine;
      if (!lines[i].isEmpty())
        newLine = new CodeLine(new CodeElement(lines[i], 0, 0));
      else
        newLine = new CodeLine();
      doc[line + i] = newLine;
    }
    splited[1].insertToBegin(lines[len]);
    doc[line + len] = splited[1];
    document = doc;
  }

  private boolean isLastPosition(int caretLine, int caretCharPos) {
    return caretCharPos >= document[caretLine].totalStrLength;
  }

  public String copy(Selection selection, boolean isCut) {
    String result = getSelectedText(selection);
    if (isCut) deleteSelected(selection, result);
    return result;
  }

  public String getSelectedText(Selection selection) {
    Selection.SelPos leftPos = selection.getLeftPos();
    Selection.SelPos rightPos = selection.getRightPos();

    if (leftPos.line == rightPos.line) {
      String line = document[leftPos.line].makeString();
      return line.substring(leftPos.charInd, rightPos.charInd);
    } else {
      StringBuilder selected = new StringBuilder();

      String firstLine = document[leftPos.line]
          .makeString(leftPos.charInd);
      selected.append(firstLine).append('\n');

      Arrays.stream(document, leftPos.line + 1, rightPos.line)
          .forEach(line -> selected.append(line.makeString()).append('\n'));

      String lastLine = document[rightPos.line]
          .makeString(0, rightPos.charInd);
      selected.append(lastLine);
      return selected.toString();
    }
  }

  public void deleteSelected(Selection selection) {
    deleteSelected(selection, getSelectedText(selection));
  }

  private void deleteSelected(Selection selection, String selected) {
    deleteSelectedOp(selection);
    Selection.SelPos leftPos = selection.getLeftPos();
    makeDiff(leftPos.line, leftPos.charInd, true, selected);
  }

  private void deleteSelectedOp(Selection selection) {
    Selection.SelPos leftPos = selection.getLeftPos();
    Selection.SelPos rightPos = selection.getRightPos();
    if (leftPos.line == rightPos.line) {
      document[leftPos.line].delete(leftPos.charInd, rightPos.charInd);
    } else {
      document[leftPos.line].delete(leftPos.charInd);
      document[rightPos.line].delete(0, rightPos.charInd);
      deleteLines(leftPos.line + 1, rightPos.line);
      concatLinesOp(leftPos.line);
    }
  }

  public boolean hasDefOrUsagesForElementPos(Pos elementPos) {
    return usageToDef.containsKey(elementPos) || defToUsages.containsKey(elementPos);
  }

  public Pos getDefinition(Pos pos) {
    return usageToDef.get(pos);
  }

  public List<Pos> getUsagesList(Pos pos) {
    return defToUsages.get(pos);
  }

  public Pos getElementStart(int line, int charPos) {
    int elemPos = line(line).getElementStart(charPos);
    return new Pos(line, elemPos);
  }

  public void moveToElementStart(Pos pos) {
    pos.pos = line(pos.line).getElementStart(pos.pos);
  }

  public V2i getLine(int ind) {
    for (int i = 0, sum = 0; i < document.length; i++) {
      if (sum + line(i).totalStrLength >= ind) return new V2i(i, ind - sum);
      sum += line(i).totalStrLength + 1;
    }
    return new V2i(document.length, 0);
  }

  public int getLineStartInd(int firstLine) {
    int result = 0;
    int lines = document.length;
    for (int i = 0; i < firstLine;) {
      result += strLength(i);
      if (++i < lines) result++;
    }
    return result;
  }

  public int getVpEnd(int lastLine) {
    int result = 0;
    int limit = Math.min(lastLine + 1, document.length);
    for (int i = 0; i < limit; i++) {
      result += strLength(i);
      if (i != document.length - 1) result++;
    }
    return result;
  }

  public String makeString() {
    return new String(getChars());
  }

  public CodeElement getCodeElement(Pos pos) {
    return line(pos.line).getCodeElement(pos.pos);
  }

  public char[] getChars() {
    char[] dst = new char[getFullLength()];
    int docLength = document.length;
    for (int i = 0, pos = 0; i < docLength; ) {
      pos = document[i].toCharArray(dst, pos);
      if (++i < docLength) dst[pos++] = newLine;
    }
    return dst;
  }

  public int[] getIntervals() {
    List<Interval> intervalList = tree.toList();
    int[] intervals = new int[3 * intervalList.size()];
    for (int i = 0, ind = 0; i < intervals.length; ind++) {
      var interval = intervalList.get(ind);
      intervals[i++] = interval.start;
      intervals[i++] = interval.stop;
      intervals[i++] = interval.intervalType;
    }
    return intervals;
  }

  public void makeDiff(int line, int from, boolean isDelete, String change) {
    currentVersion++;

    diffs.add(ArrayOp.array(new Diff(line, from, isDelete, change)));
    makeDiffOp(line, from, isDelete, change);
  }

  public void makeDiffWithCaretReturn(
      int line,
      int from,
      boolean isDelete,
      String change,
      Pos caretPos
  ) {
    currentVersion++;

    diffs.add(ArrayOp.array(new Diff(line, from, isDelete, change, caretPos.line, caretPos.pos)));
    makeDiffOp(line, from, isDelete, change);
  }

  public void makeComplexDiff(
      int[] lines,
      int[] from,
      boolean[] areDeletes,
      String[] changes,
      Pos caretPos,
      BiConsumer<Integer, String> editorAction
  ) {
    currentVersion++;

    Diff[] temp = new Diff[lines.length];
    for (int i = 0; i < lines.length; i++) {
      temp[i] = new Diff(lines[i], from[i], areDeletes[i], changes[i], caretPos.line, caretPos.pos);
    }
    diffs.add(temp);
    for (int i = 0; i < lines.length; i++) {
      makeDiffOp(lines[i], from[i], areDeletes[i], changes[i]);
      editorAction.accept(lines[i], changes[i]);
    }
  }

  void makeDiffOp(int line, int from, boolean isDelete, String change) {
    int posInDoc = getLineStartInd(line) + from;
    if (isDelete) tree.makeDeleteDiff(posInDoc, change.length());
    else tree.makeInsertDiff(posInDoc, change.length());
  }

  public V2i undoLastDiff() {
    currentVersion++;

    if (diffs.size() == 0) return null;
    Diff[] complexDiff = diffs.remove(diffs.size() - 1);
    V2i res = undoSingleDiff(complexDiff[0]);
    for (int i = 1; i < complexDiff.length; i++) {
      undoSingleDiff(complexDiff[i]);
    }

    return res;
  }

  private V2i undoSingleDiff(Diff diff) {
    String[] lines = diff.change.split("\n", -1);
    if (diff.isDelete) {
      insertLinesOp(diff.line, diff.pos, lines);
      tree.makeInsertDiff(getLineStartInd(diff.line) + diff.pos, diff.change.length());

    } else {
      Selection selection = new Selection();
      selection.startPos.set(diff.line, diff.pos);

      if (lines.length == 1) {
        selection.endPos.set(diff.line, diff.pos + lines[0].length());
      } else {
        selection.endPos.set(diff.line + lines.length - 1, lines[lines.length - 1].length());
      }

      deleteSelectedOp(selection);
      tree.makeDeleteDiff(getLineStartInd(diff.line) + diff.pos, diff.change.length());

    }
    return diff.caretReturn;
  }

  public void setLastDiffTimestamp(double timestamp) {
    lastDiffTimestamp = timestamp;
  }

  public void printIntervals() {
    Debug.consoleInfo("Current Version: " + currentVersion);
    Debug.consoleInfo("Last Parsed Version: " + lastParsedVersion);
    tree.printIntervals(makeString());
  }

  public boolean needReparse(double timestamp) {
    return (lastParsedVersion != currentVersion) && (timestamp - lastDiffTimestamp > EditorConst.TYPING_STOP_TIME);
  }

  public void onReparse() {
    lastParsedVersion = currentVersion;
  }

  public Pos getPositionAt(int offset) {
    int lineOffset = 0;
    for (int line = 0; line < document.length; ++line) {
      CodeLine codeLine = document[line];
      int lineLength = codeLine.totalStrLength;
      if (offset <= lineOffset + lineLength) {
        return new Pos(line, offset - lineOffset);
      }
      lineOffset += lineLength + 1;
    }
    return new Pos(document.length, 0);
  }

  public int getOffsetAt(Pos pos) {
    return getOffsetAt(pos.line, pos.pos);
  }

  public int getOffsetAt(int lineNumber, int column) {
    int position = 0;
    for (int i = 0; i < lineNumber;) {
      position += document[i].totalStrLength;
      if (++i < document.length) position ++;
      else break;
    }
    return position + column;
  }
}
