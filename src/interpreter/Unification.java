package interpreter;

import java.util.Map;

import parser.ast.FPTerm;
import parser.ast.TKind;

public class Unification {


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

       System.out.println("Unifying " + x + " with " + y);
       System.out.println("Theta: " + theta);

       return switch (x) {
        case FPTerm t when x.equals(y) -> true; // why wouldnt this be x.equals(y) ?

        case FPTerm t when t.kind == TKind.IDENT -> unifyVar(t, y, theta);

        case FPTerm t when t.kind == TKind.CTERM && y.kind == TKind.CTERM -> {
            if (t.name.equals(y.name) && t.args.size() == y.args.size()) {
                for (int i = 0; i < t.args.size(); i++) {
                    if (!unify(t.args.get(i), y.args.get(i), theta)) {
                        yield false;
                    }
                }
                yield true;
            }
            yield false;
        }

        default -> y.kind == TKind.IDENT ? unifyVar(y, x, theta) : false;
       };
    }

    private static boolean unifyVar(FPTerm var, FPTerm x, Map<String, FPTerm> theta) {
        System.out.println("Unifying variable " + var + " with " + x);

        return switch (x) {
            case FPTerm t when theta.containsKey(var.name) -> unify(theta.get(var.name), t, theta);

            case FPTerm t when t.kind == TKind.IDENT && theta.containsKey(t.name) -> unify(var, theta.get(t.name), theta);

            // case FPTerm t when !occursCheck(var, t, theta) -> true;

            default -> {
                if (occursCheck(var, x, theta)){
                    yield false;
                }
                theta.put(var.name, x);
                System.out.println("Theta: " + theta);

                yield true;
            }
        };
    }

    private static boolean occursCheck(FPTerm var, FPTerm term, Map<String, FPTerm> theta) {
        if (term.kind == TKind.IDENT) {
            System.out.println("Checking occurs check for " + var + " and " + term + " in " + theta);

            if (theta.containsKey(term.name)) {
                // If term is bound, check the binding
                System.out.println("Term is bound");
                return occursCheck(var, theta.get(term.name), theta);
            } else {
                // If term is unbound, only fails occurs check if var and term are different
                System.out.println("Checking if " + var + " is equal to " + term + ": " + var.equals(term));
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


        // if (term.kind == TKind.IDENT || term.kind == TKind.CONST) {
        //     return var.name.equals(term.name);
        // } else if (term.kind == TKind.CTERM) {
        //     for (FPTerm arg : term.args) {
        //         if (occursCheck(var, arg)) {
        //             return true;
        //         }
        //     }
        // }
        // return false;
    }
}