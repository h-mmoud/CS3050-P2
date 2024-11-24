package interpreter;


import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import parser.ast.FPTerm;
import parser.ast.TKind;
import parser.ast.FPClause;

public class KnowledgeBase {
    private final Map<String, ArrayList<FPClause>> knowledgeBase;

    public KnowledgeBase() {
        this.knowledgeBase = new HashMap<>();
    }

    public void addClause(FPClause clause) {
        if (clause.head == null) {
            return;
        }
        String functor = clause.head.name;
        knowledgeBase.computeIfAbsent(functor, k -> new ArrayList<>()).add(clause);

    }

    public ArrayList<FPClause> getClauses(String functor) {
        return knowledgeBase.getOrDefault(functor, new ArrayList<>());
    }

    public ArrayList<FPClause> getAllClauses() {
        return knowledgeBase.values().stream().collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll);
    }

    public FPClause getFact(FPTerm goal) {
        // Get query functor name and args
        String functor = goal.name;
        ArrayList<FPTerm> args = goal.args;

        // Get all clauses with matching functor
        ArrayList<FPClause> clauses = knowledgeBase.get(functor);
        if (clauses == null || clauses.isEmpty()) {
            return null;
        }

        // Look for fact (clause with only head) that matches query args exactly
        for (FPClause clause : clauses) {
            // Check if it's a fact (no body)
            if (clause.body == null) {
                // Check if args match
                if (matchesArgs(goal.args, clause.head.params)) {
                    return clause;
                }
            }
        }
        return null;
    }

    private boolean matchesArgs(ArrayList<FPTerm> queryArgs, ArrayList<FPTerm> factArgs) {
        if (queryArgs.size() != factArgs.size()) {
            return false;
        }
        
        // Check each argument matches exactly
        for (int i = 0; i < queryArgs.size(); i++) {
            FPTerm queryArg = queryArgs.get(i);
            FPTerm factArg = factArgs.get(i);
            
            // For facts, we only match constants
            if (queryArg.kind != TKind.CONST || 
                factArg.kind != TKind.CONST ||
                !queryArg.name.equals(factArg.name)) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        // System.out.println("KnowledgeBase.toString()");
        StringBuilder sb = new StringBuilder();
        for (ArrayList<FPClause> clauses : knowledgeBase.values()) {
            for (FPClause clause : clauses) {
                sb.append(clause.head).append(".\n");
            }
        }
        return sb.toString();
    }
}