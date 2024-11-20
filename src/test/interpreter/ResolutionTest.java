
package interpreter;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import java.util.ArrayList;
import java.util.Arrays;


import parser.*;
import parser.ast.*;

public class ResolutionTest{

    @BeforeEach
    public void setUp() {
        System.out.println("|-----------------|");
        System.out.println("Resolution test: \n");
        KnowledgeBase kb = new KnowledgeBase();
    }

    @AfterEach
    public void tearDown() {
        System.out.println("\n|________________|\n");
    }
    
    /*
     * Test case for resolving a query with a single clause with one arity that is a constant.
     * p(a)
     * ?- p(a).
     */
    @Test
    public void testResolveIdenticalClauses() {
        FPClause clause = new FPClause(new FPHead("p", new ArrayList<FPTerm>(List.of(new FPTerm(TKind.CONST, "a")))));
        System.out.println("Clause: " + clause.toString());        


        KnowledgeBase kb = new KnowledgeBase();
        kb.addClause(clause);
        
        FPClause query = new FPClause(null, new FPBody(new ArrayList<FPTerm>(List.of(new FPTerm(TKind.CONST, "p", new ArrayList<FPTerm>(List.of(new FPTerm(TKind.CONST, "a"))))))));
        System.out.println("Query: " + query.toString());
        
        Resolver resolver = new Resolver(query, kb);
        assertTrue(resolver.resolve());
    }

    /*
     * Test case for resolving a query with a single clause with one arity that is a variable.
     * p(a)
     * ?- p(X).
     */
    @Test
    public void testResolveVariableClauses() {
        FPClause clause = new FPClause(new FPHead("p", new ArrayList<FPTerm>(List.of(new FPTerm(TKind.CONST, "a")))));
        
        FPClause clause2 = new FPClause(new FPHead("p", new ArrayList<FPTerm>(List.of(new FPTerm(TKind.CONST, "b")))));

        System.out.println("Clause: " + clause.toString());        

        KnowledgeBase kb = new KnowledgeBase();
        kb.addClause(clause);
        kb.addClause(clause2);

        FPClause query = new FPClause(null, new FPBody(new ArrayList<FPTerm>(List.of(new FPTerm(TKind.CONST, "p", new ArrayList<FPTerm>(List.of(new FPTerm(TKind.IDENT, "X"))))))));
        System.out.println("Query: " + query.toString());

        
        
        Resolver resolver = new Resolver(query, kb);
        assertTrue(resolver.resolve());
        // assertEquals(new FPTerm(TKind.CONST, "a"), resolver.resolutionRoot.substitution.get("X"));
    }
    


}