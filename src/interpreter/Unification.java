package interpreter;

import java.util.Hashmap;
import java.util.Map;

import parser.ast.FPProg;
import parser.ast.FPClause;

public class Unification {

    public static boolean unify(FPTerm x, FPTerm y, Map<String, FPTerm> theta) {

    }
//        if (theta == null) {
//            return false;
//        } else if (x.equals(y)) {
//            return true;
//        } else if (x instanceof FPVar) {
//            return unifyVar((FPVar) x, y, theta);
//        } else if (y instanceof FPVar) {
//            return unifyVar((FPVar) y, x, theta);
//        } else if (x instanceof FPFunc && y instanceof FPFunc) {
//            FPFunc fx = (FPFunc) x;
//            FPFunc fy = (FPFunc) y;
//            if (fx.name.equals(fy.name) && fx.args.size() == fy.args.size()) {
//                for (int i = 0; i < fx.args.size(); i++) {
//                    if (!unify(fx.args.get(i), fy.args.get(i), theta)) {
//                        return false;
//                    }
//                }
//                return true;
//            }
//        }
//        return false;
    }

    private static boolean unifyVar(FPVar var, FPTerm x, Map<String, FPTerm> theta) {
        if (theta.containsKey(var.name)) {
            return unify(theta.get(var.name), x, theta);
        } else if (x instanceof FPVar && theta.containsKey(((FPVar) x).name)) {
            return unify(var, theta.get(((FPVar) x).name), theta);
        } else {
            theta.put(var.name, x);
            return true;
        }
    }