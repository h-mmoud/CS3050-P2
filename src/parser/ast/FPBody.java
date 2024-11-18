package parser.ast;

import java.util.ArrayList;

import parser.ast.FPTerm;

public class FPBody {
  public final ArrayList<FPTerm> ts;

  public FPBody(ArrayList<FPTerm> ts) {
    this.ts = ts;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ts.size(); i++ ) {
      if (i > 0) { sb.append(", "); };
      sb.append(ts.get(i));
    }
    return sb.toString();
  }
}