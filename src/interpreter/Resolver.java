package interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import parser.ast.FPClause;
import parser.ast.FPHead;
import parser.ast.FPTerm;

class Node {
    public ArrayList<FPTerm> goal;
    public Node parent;
    public ArrayList<Node> children;
    public Map<String, FPTerm> substitution;

    public Node(ArrayList<FPTerm> goal, Node parent, Map<String, FPTerm> substitution) {
        this.goal = goal;
        this.parent = parent;
        this.substitution = substitution;
    }
}

public class Resolver {
    private final FPClause query;
    private final KnowledgeBase kb;
    private Node resolutionRoot; 

    public Resolver(FPClause query, KnowledgeBase kb) {
        this.query = query;
        this.kb = kb;

        // Extract goals from the query

        if (query.head != null) {
            System.out.println("Not a query: " + query.toString());
            return;
        }

        ArrayList<FPTerm> goal = query.body != null ? query.body.ts : new ArrayList<>();
        System.out.println("Goal: " + goal.toString());

        this.resolutionRoot = new Node(goal, null, new HashMap<>());
    }
    
    public boolean resolve() {
        System.out.println("Resolving " + query.toString());
        return resolve(resolutionRoot);
    }

    // Depth-first search to resolve the query
    private boolean resolve(Node node) {

        // Check if the goal is empty
        if (node.goal.isEmpty()) {
            return true;
        }

        // check the knowledge base for the goal
        ArrayList<FPClause> clauses = new ArrayList<>();
        for (FPTerm goal : node.goal) {
            System.out.println("Resolving goal: " + goal.toString());
            clauses.addAll(kb.getClauses(goal.name));
        }

        // Try to resolve the goal with the clauses
        for (FPClause clause : clauses) {
            Map<String, FPTerm> newTheta = new HashMap<>();

            // Unify the head of the clause with the goal
            for (FPTerm goal : node.goal) {
                if (unifyGoalWithHead(goal, clause.head, newTheta)) {
                    // Create a new goal
                    ArrayList<FPTerm> newGoal = new ArrayList<>(node.goal);
                    newGoal.remove(0);
                    newGoal.addAll(clause.body != null ? clause.body.ts : new ArrayList<>());

                    // Create a new node
                    Node newNode = new Node(newGoal, node, newTheta);

                    // Recursively resolve the new node
                    if (resolve(newNode)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // Generate a unique key for the query
    private boolean unifyGoalWithHead(FPTerm goal, FPHead head, Map<String, FPTerm> theta) {
        // Check if the functor names are the same
        if (!goal.name.equals(head.name)) {
            return false;
        }

        // Check if the arity matches
        int goalArity = goal.args != null ? goal.args.size() : 0;
        int headArity = head.params != null ? head.params.size() : 0;
        if (goalArity != headArity) {
            return false;
        }

        // Unify each corresponding argument
        for (int i = 0; i < goalArity; i++) {
            FPTerm goalArg = goal.args.get(i);
            FPTerm headParam = head.params.get(i);
            if (!Unifier.unify(goalArg, headParam, theta)) {
                return false;
            }
        }

        return true;
    }

}