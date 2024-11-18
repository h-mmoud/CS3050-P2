package interpreter;

import java.io.FileReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import parser.FProlog;
import parser.ast.FPProg;
import parser.ast.FPTerm;
import parser.ast.TKind;

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
  
    private static void testUnification() {
        // Test case 1: Identical terms
        FPTerm x1 = new FPTerm(TKind.IDENT, "x");
        FPTerm x2 = new FPTerm(TKind.IDENT, "x");
        Map<String, FPTerm> theta1 = new HashMap<>();
        System.out.println("Test 1 - Identical terms: " + Unification.unify(x1, x2, theta1));
        
        // Test case 2: Variable binding
        FPTerm var = new FPTerm(TKind.IDENT, "X");
        FPTerm val = new FPTerm(TKind.IDENT, "a");
        Map<String, FPTerm> theta2 = new HashMap<>();
        System.out.println("Test 2 - Variable binding: " + Unification.unify(var, val, theta2));
        System.out.println("Binding: X = " + theta2.get("X"));
        
        // Test case 3: Complex terms
        // FPTerm f1 = new FPTerm(TKind.CTERM, "f", 
        //     List.of(new FPTerm(TKind.IDENT, "X")));
        // FPTerm f2 = new FPTerm(TKind.CTERM, "f", 
        //     List.of(new FPTerm(TKind.IDENT, "a")));
        // Map<String, FPTerm> theta3 = new HashMap<>();
        // System.out.println("Test 3 - Complex terms: " + Unification.unify(f1, f2, theta3));
        // System.out.println("Binding: X = " + theta3.get("X"));
    }
}