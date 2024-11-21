package interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import parser.ast.FPClause;
import parser.ast.FPHead;
import parser.ast.FPTerm;
import parser.ast.TKind;

class Node {
    public FPTerm goal;
    public Node parent;
    public Map<String, FPTerm> substitution;

    public ArrayList<Node> children;
    public Map<String, FPTerm> theta;

    public Node(FPTerm goal, Node parent, Map<String, FPTerm> substitution) {
        this.goal = goal;
        this.parent = parent;
        this.substitution = substitution;
    }
}

public class Resolver {
    // private final FPClause query;
    private final KnowledgeBase kb;

    private Map<String, FPTerm> visited;
    private Map<String, FPTerm> bindings;
    private static Set<FPClause> previousUnifications;
    private static FPClause previousQuery;

    // private Node resolutionRoot; 

    public Resolver(KnowledgeBase kb) {
        // this.query = query;
        this.kb = kb;
        this.visited = new HashMap<>(); // Visited nodes
        this.bindings = new HashMap<>(); // Bindings for the current resolution

        Resolver.previousQuery = null;
        Resolver.previousUnifications = new HashSet<>();
        // Extract goals from the query

    //     if (query.head != null) {
    //         System.out.println("Not a query: " + query.toString());
    //         return;
    //     }

    //     ArrayList<FPTerm> goal = query.body != null ? query.body.ts : new ArrayList<>();
    //     System.out.println("Goal: " + goal.toString());

    //     this.resolutionRoot = new Node(goal, null, new HashMap<>());
    }
    
    // 
    public boolean resolve(FPClause query) {
        FPTerm goal;
        TKind goalKind;

        Map<String, FPTerm> bindings = new HashMap<>();        

        if (query.head != null) {
            System.out.println("Not a query: " + query.toString());
            return false;
        }

        if (query.body == null || query.body.ts.size() == 0) {
            try {
                goal = previousQuery.body.ts.get(0);
                // System.out.println("Goal: " + goal.toString());
                // System.out.println("goal kind: " + goal.kind);
                // System.out.println("goal arg types: " + goal.args.get(0).kind);
            } catch (Exception e) {
                System.out.println("no");
                return false;
            }
        } else {
            goal = query.body.ts.get(0);
            // System.out.println("Goal: " + goal.toString());
            Resolver.previousQuery = query;
        }

        
        Node resolutionRoot = new Node(goal, null, new HashMap<>());

        boolean resolution = resolve(resolutionRoot, bindings);
        if (resolution) {
            String result = "";

//             for (FPTerm arg : goal.args) {
//                 if (arg.kind == TKind.IDENT) {
//                     result += bindings.toString() + ", ";
//                 } else {
//                     result += "yes" + " ";
//                 }
//             }
// V            System.out.println(result);
            // System.out.println("Resolution successful");
            if (this.bindings.size() > 0) {
                System.out.println(this.bindings.toString());
            } else {
                System.out.println("yes");
            }
        } else {
            System.out.println("no");
        }

        return resolution;
    }

    // Depth-first search to resolve the query
private boolean resolve(Node node, Map<String, FPTerm> bindings) {
        FPTerm goal = node.goal;

        // Check if the goal is empty
        if (goal == null) {
            return true;
        }

        if (visited.containsKey(goal.toString()) && node.parent != null) {
            return false;
        }

        visited.put(goal.toString(), goal);

        // check the knowledge base for the goal
        ArrayList<FPClause> clauses = new ArrayList<>();
        
        // System.out.println("Resolving goal: " + goal.toString());
        clauses.addAll(kb.getClauses(goal.name));

        if (clauses.size() == 0) {
            return false;
        }
        // System.out.println("Clauses: " + clauses.toString());
        

        // Try to resolve the goal with the clauses
        for (FPClause clause : clauses) {
            if (previousUnifications.contains(clause)) {
                continue;
            }

            Map<String, FPTerm> newBindings = new HashMap<>(bindings);

            // Unify the head of the clause with the goal
            if (unifyGoalWithHead(goal, clause.head, newBindings)) {
                // Create a new goal
                FPTerm newGoal = (clause.body != null ? clause.body.ts.get(0) : null);

                // Create a new node
                Node newNode = new Node(newGoal, node, newBindings);

                // Recursively resolve the new node
                if (resolve(newNode, newBindings)) {
                    goal.args.forEach(arg -> {
                        if (arg.kind == TKind.IDENT) {
                            previousUnifications.add(clause);
                        }
                        
                    });
                    return true;
                }
            }
        }
    
        visited.remove(goal.toString());
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
        this.bindings.putAll(theta);
        return true;
    }

}