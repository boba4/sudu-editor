package org.sudu.experiments.parser;

import org.antlr.v4.runtime.*;

public class ErrorRecognizerListener extends ConsoleErrorListener {

  public boolean errorOccurred = false;

  @Override
  public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
    super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e);
    errorOccurred = true;
  }

}
