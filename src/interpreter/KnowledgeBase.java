package interpreter;


import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import parser.ast.FPProg;
import parser.ast.FPClause;

public class KnowledgeBase {
    private final Map<String, List<FPClause>> knowledgeBase;

    public KnowledgeBase() {
        this.knowledgeBase = new HashMap<>();
    }

    public void addClause(FPClause clause) {
        if (clause.head == null) {
            return;
        }
        String functor = clause.head.name;
        System.out.println("KnowledgeBase.addClause() functor: " + functor);
        knowledgeBase.computeIfAbsent(functor, k -> new ArrayList<>()).add(clause);

    }

    public List<FPClause> getClauses(String functor) {
        return knowledgeBase.getOrDefault(functor, new ArrayList<>());
    }

    public String toString() {
        System.out.println("KnowledgeBase.toString()");
        StringBuilder sb = new StringBuilder();
        for (List<FPClause> clauses : knowledgeBase.values()) {
            for (FPClause clause : clauses) {
                sb.append(clause.head).append(".\n");
            }
        }
        return sb.toString();
    }
}