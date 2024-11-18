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

       return switch (x) {
        case FPTerm t when t.equals(y) -> true;

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
       
    //    else if (x.equals(y)) {
    //        return true;
    //    } else if (x instanceof FPVar) {
    //        return unifyVar((FPVar) x, y, theta);
    //    } else if (y instanceof FPVar) {
    //        return unifyVar((FPVar) y, x, theta);
    //    } else if (x instanceof FPFunc && y instanceof FPFunc) {
    //        FPFunc fx = (FPFunc) x;
    //        FPFunc fy = (FPFunc) y;
    //        if (fx.name.equals(fy.name) && fx.args.size() == fy.args.size()) {
    //            for (int i = 0; i < fx.args.size(); i++) {
    //                if (!unify(fx.args.get(i), fy.args.get(i), theta)) {
    //                    return false;
    //                }
    //            }
    //            return true;
    //        }
    //    }
    //    return false;
    }

    private static boolean unifyVar(FPTerm var, FPTerm x, Map<String, FPTerm> theta) {
        if (theta.containsKey(var.name)) {
            return unify(theta.get(var.name), x, theta);
        } else if (x.kind == TKind.IDENT && theta.containsKey(x.name)) {
            return unify(var, theta.get(x.name), theta);
        } else {
            theta.put(var.name, x);
            return true;
        }
    }
}