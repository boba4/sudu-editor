package org.sudu.experiments.parser.java.model;

import org.sudu.experiments.parser.common.Pos;

public class JavaField extends ClassBodyDecl {

  public JavaField(String name, Pos position, boolean isStatic) {
    super(name, position, isStatic);
  }
}
