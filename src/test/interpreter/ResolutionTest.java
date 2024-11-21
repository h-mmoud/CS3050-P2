
package interpreter;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.beans.Transient;
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
        
        Resolver resolver = new Resolver(kb);
        assertTrue(resolver.resolve(query));
    }

    /*
     * Test case for resolving a query with a single clause with one arity that is a variable.
     * p(a)
     * ?- p(X).
     */
    @Test
    public void testResolveVariableClauses() {
        FPClause clause = new FPClause(new FPHead("p", new ArrayList<FPTerm>(List.of(new FPTerm(TKind.CONST, "a")))));
        
        System.out.println("Clause: " + clause.toString());    

        KnowledgeBase kb = new KnowledgeBase();
        kb.addClause(clause);

        FPClause query = new FPClause(null, new FPBody(new ArrayList<FPTerm>(List.of(new FPTerm(TKind.CONST, "p", new ArrayList<FPTerm>(List.of(new FPTerm(TKind.IDENT, "X"))))))));
        System.out.println("Query: " + query.toString());

        
        
        Resolver resolver = new Resolver(kb);
        assertTrue(resolver.resolve(query));
        // assertEquals(new FPTerm(TKind.CONST, "a"), resolver.resolutionRoot.substitution.get("X"));
    }
    

    @Test
    public void testBacktracking() {
        System.out.println("Resolution test: testBacktracking()");

        FPClause clause1 = new FPClause(new FPHead("p", new ArrayList<FPTerm>(List.of(new FPTerm(TKind.CONST, "a")))));
        FPClause clause2 = new FPClause(new FPHead("p", new ArrayList<FPTerm>(List.of(new FPTerm(TKind.CONST, "b")))));
        
        System.out.println("Clause 1: " + clause1.toString());
        System.out.println("Clause 2: " + clause2.toString());

        KnowledgeBase kb = new KnowledgeBase();
        kb.addClause(clause1);
        kb.addClause(clause2);

        FPClause query = new FPClause(null, new FPBody(new ArrayList<FPTerm>(List.of(new FPTerm(TKind.CONST, "p", new ArrayList<FPTerm>(List.of(new FPTerm(TKind.IDENT, "X"))))))));
        System.out.println("Query: " + query.toString());


        FPClause emptyQuery = new FPClause(null, null);
        System.out.println("Query: " + emptyQuery.toString());
        
        Resolver resolver = new Resolver(kb);
        assertTrue(resolver.resolve(query));
        assertTrue(resolver.resolve(emptyQuery));
        assertFalse(resolver.resolve(emptyQuery));
    }
}