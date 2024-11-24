package interpreter;

import java.io.FileReader;
import java.io.StringReader;
import java.util.Scanner;

import parser.FProlog;
import parser.ast.FPProg;

public class Main {
  static boolean trace = false;
  static String filename = null;
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--trace")) {
                trace = true;
            } else {
                filename = args[i];
            }
        }


        try(Scanner s = new Scanner(new FileReader(filename));) {


          StringBuilder sb = new StringBuilder();
          while (s.hasNext()) {
          sb.append(s.next());
        }
        String str = sb.toString();

        // create new abstract syntax tree
        FPProg ast = new FProlog(new StringReader(str)).P();

        // System.out.println(ast.toString());


        // create new knowledge base
        KnowledgeBase kb = new KnowledgeBase();
        Resolver r = new Resolver(kb, trace);
        for (int i = 0; i < ast.cs.size(); i++) {
          if (ast.cs.get(i).head != null) {
            kb.addClause(ast.cs.get(i));
            r = new Resolver(kb, trace);
          } else {
            r.resolve(ast.cs.get(i));
          }
        }

        
        // System.out.println(kb.toString());

        // create new resolver
        // Resolver r = new Resolver(kb);
        // System.out.println(r.resolve(ast.cs.get(1)));

    } catch (Exception e) {
      throw new RuntimeException(e);
      // System.err.println(e.toString());
    }
  }
}