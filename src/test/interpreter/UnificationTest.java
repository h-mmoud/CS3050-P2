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

public class UnificationTest {

    @BeforeEach
    public void setUp() {
        System.out.println("|-----------------|");
        System.out.println("UnifierTest\n");

    }

    @AfterEach
    public void tearDown() {

        System.out.println("\n|________________|\n");
    }
    /*
     * Test unification of identical terms. The terms are of the form x and x. The
     * unification should succeed and the substitution theta should be updated
     */
    @Test
    public void testUnifyIdenticalTerms() {
        System.out.println("UnifierTest.testUnifyIdenticalTerms()");
        FPTerm term1 = new FPTerm(TKind.IDENT, "X");
        FPTerm term2 = new FPTerm(TKind.IDENT, "X");
        Map<String, FPTerm> theta = new HashMap<>();
        assertTrue(Unifier.unify(term1, term2, theta));
    }

    /*
     * Test unification of different terms. The terms are of the form x and y.
     * The unification should succeed and the substitution theta should be updated
     */
    @Test
    public void testUnifyDifferentVariableTerms() {
        System.out.println("UnifierTest.testUnifyDifferentIdentTerms()");
        FPTerm term1 = new FPTerm(TKind.IDENT, "X");
        FPTerm term2 = new FPTerm(TKind.IDENT, "Y");
        Map<String, FPTerm> theta = new HashMap<>();
        assertTrue(Unifier.unify(term1, term2, theta));
        assertEquals(term2, theta.get("X"));
    }

    /*
     * Test unification of complex terms. The terms are of the form f(x) and f(y).
     * The unification should succeed and the substitution theta should be updated
     */
    @Test
    public void testUnifyComplexTerms() {
        System.out.println("UnifierTest.testUnifyComplexTerms()");
        FPTerm term1 = new FPTerm(TKind.CTERM, "f", new ArrayList<>(List.of(new FPTerm(TKind.IDENT, "X"))));
        FPTerm term2 = new FPTerm(TKind.CTERM, "f", new ArrayList<>(List.of(new FPTerm(TKind.IDENT, "Y"))));
        Map<String, FPTerm> theta = new HashMap<>();
        assertTrue(Unifier.unify(term1, term2, theta));
        // assertEquals(new FPTerm(TKind.IDENT, "y"), theta.get("x"));
    }

    /*
     * Test unification of terms with different functions. The terms are of the form
     * f(x) and g(x). The unification should fail
     */
    @Test
    public void testUnifyFailDifferentFunctions() {
        System.out.println("UnifierTest.testUnifyFailDifferentFunctions()");
        FPTerm term1 = new FPTerm(TKind.CTERM, "f", new ArrayList<>(List.of(new FPTerm(TKind.IDENT, "x"))));
        FPTerm term2 = new FPTerm(TKind.CTERM, "g", new ArrayList<>(List.of(new FPTerm(TKind.IDENT, "x"))));
        Map<String, FPTerm> theta = new HashMap<>();
        assertFalse(Unifier.unify(term1, term2, theta));
    }

    /*
     * Test unification of terms with different arity. The terms are of the form
     * f(x) and f(x, y). The unification should fail
     */
    @Test
    public void testUnifyFailDifferentArity() {
        System.out.println("UnifierTest.testUnifyFailDifferentArity()");
        FPTerm term1 = new FPTerm(TKind.CTERM, "f", new ArrayList<>(List.of(new FPTerm(TKind.IDENT, "x"))));
        FPTerm term2 = new FPTerm(TKind.CTERM, "f", new ArrayList<>(List.of(new FPTerm(TKind.IDENT, "x"), new FPTerm(TKind.IDENT, "y"))));
        Map<String, FPTerm> theta = new HashMap<>();
        assertFalse(Unifier.unify(term1, term2, theta));
    }

    /*
     * Test unification of a constant term and a variable term
     * The terms are of the form a and X. The unification should succeed
     */
    @Test
    public void testUnifyConstantAndVariable() {
        System.out.println("UnifierTest.testUnifyConstantAndVariable()");
        FPTerm term1 = new FPTerm(TKind.CONST, "a");
        FPTerm term2 = new FPTerm(TKind.IDENT, "X");
        Map<String, FPTerm> theta = new HashMap<>();
        assertTrue(Unifier.unify(term1, term2, theta));
        assertEquals(term1, theta.get("X"));
    }

    @Test
    public void testOccursCheck() {
        System.out.println("UnifierTest.testOccursCheck()");
        FPTerm term1 = new FPTerm(TKind.IDENT, "X");
        FPTerm term2 = new FPTerm(TKind.CTERM, "f", new ArrayList<>(List.of(new FPTerm(TKind.IDENT, "X"))));
        Map<String, FPTerm> theta = new HashMap<>();
        assertFalse(Unifier.unify(term1, term2, theta));
    }

    /*
     * Test unification when the substitution theta already contains bindings
     */
    @Test
    public void testUnifyWithExistingSubstitutions() {
        System.out.println("UnifierTest.testUnifyWithExistingSubstitutions()");
        FPTerm term1 = new FPTerm(TKind.IDENT, "X");
        FPTerm term2 = new FPTerm(TKind.IDENT, "Y");
        Map<String, FPTerm> theta = new HashMap<>();
        theta.put("Y", new FPTerm(TKind.CONST, "a"));
        assertTrue(Unifier.unify(term1, term2, theta));
        assertEquals(new FPTerm(TKind.CONST, "a"), theta.get("X"));
        assertEquals(new FPTerm(TKind.CONST, "a"), theta.get("Y"));
    }

    /*
     * Test unification of terms with different names. The terms are of the form
     * f(a) and g(a). The unification should fail
     */
    @Test
    public void testUnifyFunctionsWithDifferentNames() {
        System.out.println("UnifierTest.testUnifyFunctionsWithDifferentNames()");
        FPTerm term1 = new FPTerm(TKind.CTERM, "f", new ArrayList<>(List.of(
            new FPTerm(TKind.CONST, "a")
        )));
        FPTerm term2 = new FPTerm(TKind.CTERM, "g", new ArrayList<>(List.of(
            new FPTerm(TKind.CONST, "a")
        )));
        Map<String, FPTerm> theta = new HashMap<>();
        assertFalse(Unifier.unify(term1, term2, theta));
    }


    /*
     * Test unification of nested terms. The terms are of the form f(g(X), Y) and
     * f(Z, h(W)). The unification should succeed and the substitution theta should
     * be updated
     */
    @Test
    public void testNestedUnifier() {
        System.out.println("UnifierTest.testNestedUnifier()");
        // Term f(g(X), Y)
        FPTerm term1 = new FPTerm(TKind.CTERM, "f", new ArrayList<>(List.of(
            new FPTerm(TKind.CTERM, "g", new ArrayList<>(List.of(new FPTerm(TKind.IDENT, "X")))),
            new FPTerm(TKind.IDENT, "Y")
        )));
        // Term f(Z, h(W))
        FPTerm term2 = new FPTerm(TKind.CTERM, "f", new ArrayList<>(List.of(
            new FPTerm(TKind.IDENT, "Z"),
            new FPTerm(TKind.CTERM, "h", new ArrayList<>(List.of(new FPTerm(TKind.IDENT, "W"))))
        )));
        Map<String, FPTerm> theta = new HashMap<>();
        assertTrue(Unifier.unify(term1, term2, theta));
        // Check the substitutions made
        assertEquals(new FPTerm(TKind.CTERM, "g", new ArrayList<>(List.of(new FPTerm(TKind.IDENT, "X")))), theta.get("Z"));
        assertEquals(new FPTerm(TKind.CTERM, "h", new ArrayList<>(List.of(new FPTerm(TKind.IDENT, "W")))), theta.get("Y"));
    }
}