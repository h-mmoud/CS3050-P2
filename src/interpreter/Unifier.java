package interpreter;

import java.util.Map;
import java.util.HashMap;

import parser.ast.FPTerm;
import parser.ast.TKind;

public class Unifier {
    private Map<String, FPTerm> theta;

    public Unifier(Map<String, FPTerm> theta) {
        this.theta = theta;
    }

    public Unifier() {
        this.theta = new HashMap<String, FPTerm>();
    }

    /*
     * Unify two terms x and y with respect to the substitution theta. If the 
     * unification is successful, the substitution theta is updated with the
     * new bindings. If the unification fails, the substitution theta is not
     * modified.
     */
    public static boolean unify(FPTerm x, FPTerm y, Map<String, FPTerm> theta) {
       if (theta == null) {
           return false;
       }

       Map<String, FPTerm> thetaCopy = new HashMap<>(theta);

    //    System.out.println("Unifying " + x + " with " + y);
    //    System.out.println("x kind: " + x.kind);
    //     System.out.println("y kind: " + y.kind);

    //    System.out.println("Theta: " + theta);

       return switch (x) {
        case FPTerm t when t.equals(y) -> true;

        case FPTerm t when t.kind == TKind.IDENT -> unifyVar(t, y, thetaCopy);

        case FPTerm t when t.kind == TKind.CTERM && y.kind == TKind.CTERM -> {
            if (t.name.equals(y.name) && t.args.size() == y.args.size()) {
                for (int i = 0; i < t.args.size(); i++) {
                    if (!unify(t.args.get(i), y.args.get(i), thetaCopy)) {
                        yield false;
                    }
                }
                yield true;
            }
            yield false;
        }

        default -> y.kind == TKind.IDENT ? unifyVar(y, x, thetaCopy) : false;
       };
    }

    private static boolean unifyVar(FPTerm var, FPTerm x, Map<String, FPTerm> theta) {
        // System.out.println("Unifying variable " + var + " with " + x);

        return switch (x) {
            case FPTerm t when theta.containsKey(var.name) -> unify(theta.get(var.name), t, theta);

            case FPTerm t when t.kind == TKind.IDENT && theta.containsKey(t.name) -> unify(var, theta.get(t.name), theta);

            case FPTerm t when occursCheck(var, t, theta) -> false;

            default -> {
                theta.put(var.name, x);
                yield true;
            }
        };
    }

    // TODO: Implement occurs check with switch statements
    private static boolean occursCheck(FPTerm var, FPTerm term, Map<String, FPTerm> theta) {
        if (term.kind == TKind.IDENT) {
            // System.out.println("Checking occurs check for " + var + " and " + term + " in " + theta);

            if (theta.containsKey(term.name)) {
                // If term is bound, check the binding
                // System.out.println("Term is bound");
                return occursCheck(var, theta.get(term.name), theta);
            } else {
                // If term is unbound, only fails occurs check if var and term are different
                // System.out.println("Checking if " + var + " is equal to " + term + ": " + var.equals(term));
                return var.equals(term);
            }
        } else if (term.kind == TKind.CONST) {
            return false;
        } else if (term.kind == TKind.CTERM) {
            for (FPTerm arg : term.args) {
                if (occursCheck(var, arg, theta)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    public Map<String, FPTerm> getTheta() {
        return theta;
    }
}