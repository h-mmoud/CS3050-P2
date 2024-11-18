package parser.ast;

import java.util.ArrayList;

import parser.ast.FPClause;

public class FPProg {
  public final ArrayList<FPClause> cs = new ArrayList<FPClause>();

  public void add(FPClause c) {
    cs.add(c);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (FPClause c : cs) {
      sb.append(c.toString());
      sb.append(System.getProperty("line.separator"));
    }
    return sb.toString();
  }
}