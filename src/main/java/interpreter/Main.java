package interpreter;

import java.io.FileReader;
import java.io.StringReader;
import java.util.Scanner;
import java.util.HashMap;

import parser.FProlog;
import parser.ast.FPProg;

public class Main {
    public static void main(String[] args) {
        try(Scanner s = new Scanner(new FileReader(args[0]));) {

          StringBuilder sb = new StringBuilder();
          while (s.hasNext()) {
          sb.append(s.next());
        }
        String str = sb.toString();

        // create new abstract syntax tree
        FPProg ast = new FProlog(new StringReader(str)).P();

        System.out.println(ast.toString());


        // create new knowledge base
        KnowledgeBase kb = new KnowledgeBase();

        for (int i = 0; i < ast.cs.size(); i++) {
            kb.addClause(ast.cs.get(i));

//            System.out.println("added "+ast.cs.get(i).head.name+" to kb");
        }

        System.out.println(kb.toString());

    } catch (Exception e) {

      System.err.println(e.toString());
    }
  }
}