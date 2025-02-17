package org.sudu.experiments.parser.cpp.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.IterativeParseTreeWalker;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.sudu.experiments.parser.ErrorHighlightingStrategy;
import org.sudu.experiments.parser.Interval;
import org.sudu.experiments.parser.common.BaseFullParser;
import org.sudu.experiments.parser.cpp.gen.CPP14Lexer;
import org.sudu.experiments.parser.cpp.gen.CPP14Parser;
import org.sudu.experiments.parser.cpp.parser.highlighting.CppLexerHighlighting;
import org.sudu.experiments.parser.cpp.walker.CppWalker;
import org.sudu.experiments.parser.cpp.walker.CppClassWalker;

import java.util.Collections;
import java.util.List;

import static org.sudu.experiments.parser.ParserConstants.*;
import static org.sudu.experiments.parser.ParserConstants.TokenTypes.*;

public class CppFullParser extends BaseFullParser {

  public int[] parse(String source) {
    long parsingTime = System.currentTimeMillis();

    initLexer(source);
    return parseWithLexer(parsingTime);
  }

  public int[] parse(char[] source) {
    long parsingTime = System.currentTimeMillis();

    initLexer(source);
    return parseWithLexer(parsingTime);
  }

  private int[] parseWithLexer(long parsingTime) {
    CPP14Parser parser = new CPP14Parser(tokenStream);
    parser.setErrorHandler(new ErrorHighlightingStrategy());
    parser.removeErrorListeners();
    parser.addErrorListener(parserRecognitionListener);

    var transUnit = parser.translationUnit();
    if (parserErrorOccurred()) CppLexerHighlighting.highlightTokens(allTokens, tokenTypes);
    else highlightTokens();

    ParseTreeWalker walker = new IterativeParseTreeWalker();
    CppClassWalker classWalker = new CppClassWalker();
    int[] result;
    try {
      walker.walk(classWalker, transUnit);

      CppWalker cppWalker = new CppWalker(tokenTypes, tokenStyles, classWalker.current, usageToDefinition);
      walker.walk(cppWalker, transUnit);

      classWalker.intervals.add(new Interval(0, fileSourceLength, IntervalTypes.Cpp.TRANS_UNIT));

      result = getInts(classWalker.intervals);
    } catch (Exception e) {
      e.printStackTrace();
      result = getInts(List.of(defaultInterval()));
    }

    System.out.println("Parsing full cpp time: " + (System.currentTimeMillis() - parsingTime) + "ms");
    return result;
  }

  @Override
  protected List<Token> splitToken(Token token) {
    int tokenType = token.getType();
    if (isMultilineToken(tokenType)) return splitTokenByLine(token);
    if (tokenType == CPP14Lexer.Directive || tokenType == CPP14Lexer.MultiLineMacro) return CppDirectiveSplitter.divideDirective(token);
    return Collections.singletonList(token);
  }

  @Override
  protected boolean isMultilineToken(int tokenType) {
    return tokenType == CPP14Lexer.BlockComment
        || tokenType == CPP14Lexer.StringLiteral;
  }

  @Override
  protected boolean isComment(int tokenType) {
    return tokenType == CPP14Lexer.BlockComment
        || tokenType == CPP14Lexer.LineComment;
  }

  @Override
  protected Lexer initLexer(CharStream stream) {
    return new CPP14Lexer(stream);
  }

  @Override
  protected boolean tokenFilter(Token token) {
    int type = token.getType();
    return type != CPP14Lexer.Newline
        && type != CPP14Lexer.EOF;
  }

  @Override
  protected void highlightTokens() {
    for (var token : allTokens) {
      int ind = token.getTokenIndex();
      if (isComment(token.getType())) tokenTypes[ind] = COMMENT;
      else if (isDirective(token.getType())) tokenTypes[ind] = ANNOTATION;
    }
  }

  public static boolean isDirective(int tokenType) {
    return tokenType == CPP14Lexer.Directive
        || tokenType == CPP14Lexer.MultiLineMacro;
  }
}
