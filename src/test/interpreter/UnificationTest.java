package interpreter;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import parser.*;
import parser.ast.*;

public class UnificationTest {

    @Test
    public void testUnifyIdenticalTerms() {
        FPTerm term1 = new FPTerm(TKind.IDENT, "x");
        FPTerm term2 = new FPTerm(TKind.IDENT, "x");
        Map<String, FPTerm> theta = new HashMap<>();
        assertTrue(Unification.unify(term1, term2, theta));
    }

    @Test
    public void testUnifyDifferentIdentTerms() {
        FPTerm term1 = new FPTerm(TKind.IDENT, "x");
        FPTerm term2 = new FPTerm(TKind.IDENT, "y");
        Map<String, FPTerm> theta = new HashMap<>();
        assertTrue(Unification.unify(term1, term2, theta));
        assertEquals(term2, theta.get("x"));
    }

    // @Test
    // public void testUnifyComplexTerms() {
    //     FPTerm term1 = new FPTerm(TKind.CTERM, "f", List.of(new FPTerm(TKind.IDENT, "x")));
    //     FPTerm term2 = new FPTerm(TKind.CTERM, "f", List.of(new FPTerm(TKind.IDENT, "y")));
    //     Map<String, FPTerm> theta = new HashMap<>();
    //     assertTrue(Unification.unify(term1, term2, theta));
    //     assertEquals(new FPTerm(TKind.IDENT, "y"), theta.get("x"));
    // }

    // @Test
    // public void testUnifyFailDifferentFunctions() {
    //     FPTerm term1 = new FPTerm(TKind.CTERM, "f", List.of(new FPTerm(TKind.IDENT, "x")));
    //     FPTerm term2 = new FPTerm(TKind.CTERM, "g", List.of(new FPTerm(TKind.IDENT, "x")));
    //     Map<String, FPTerm> theta = new HashMap<>();
    //     assertFalse(Unification.unify(term1, term2, theta));
    // }

    // @Test
    // public void testUnifyFailDifferentArity() {
    //     FPTerm term1 = new FPTerm(TKind.CTERM, "f", List.of(new FPTerm(TKind.IDENT, "x")));
    //     FPTerm term2 = new FPTerm(TKind.CTERM, "f", List.of(new FPTerm(TKind.IDENT, "x"), new FPTerm(TKind.IDENT, "y")));
    //     Map<String, FPTerm> theta = new HashMap<>();
    //     assertFalse(Unification.unify(term1, term2, theta));
    // }
}