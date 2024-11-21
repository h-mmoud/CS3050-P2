package interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import parser.ast.FPClause;
import parser.ast.FPHead;
import parser.ast.FPTerm;
import parser.ast.TKind;

class Node {
    public ArrayList<FPTerm> goals;
    public Node parent;
    public Map<String, FPTerm> substitution;

    public ArrayList<Node> children;
    public Map<String, FPTerm> theta;

    public Node(ArrayList<FPTerm> goals, Node parent, Map<String, FPTerm> substitution) {
        this.goals = goals;
        this.parent = parent;
        this.substitution = substitution;
    }
}

public class Resolver {
    // private final FPClause query;
    private final KnowledgeBase kb;

    private Map<String, FPTerm> visited;
    private Map<String, FPTerm> bindings;
    private String orignalQueryName;
    private LinkedHashSet<FPClause> clauses;
    private FPClause query;

    private static Set<FPClause> previousUnifications;
    private static FPClause previousQuery;

    // private Node resolutionRoot; 

    public Resolver(KnowledgeBase kb) {
        // this.query = query;
        this.kb = kb;
        this.visited = new HashMap<>(); // Visited nodes
        this.bindings = new HashMap<>(); // Bindings for the current resolution
        this.clauses = new LinkedHashSet<>();
        this.query = null;

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
        ArrayList<FPTerm> goals;

;

        Map<String, FPTerm> bindings = new HashMap<>();        
        this.visited.clear();

        if (query.head != null) {
            System.out.println("Not a query: " + query.toString());
            return false;
        }

        if (query.body == null || query.body.ts.isEmpty()) {
            try {
                this.query = previousQuery;
                goals = previousQuery.body.ts;
                // System.out.println("Goal: " + goal.toString());
                // System.out.println("goal kind: " + goal.kind);
                // System.out.println("goal arg types: " + goal.args.get(0).kind);
            } catch (Exception e) {
                System.out.println("no");
                return false;
            }
        } else {
            goals = query.body.ts;
            // System.out.println("Goal: " + goal.toString());
            this.query = query;
            Resolver.previousQuery = query;
        }
        Node resolutionRoot = new Node(goals, null, new HashMap<>());

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
        ArrayList<FPTerm> goals = node.goals;

        // System.out.println("Resolving: " + goals.toString());
        // Check if the goal is empty
        if (goals.isEmpty()) {
            return true;
        }

        // Get the first goal
        FPTerm currentGoal = goals.get(0);

        if (visited.containsKey(currentGoal.toString()) && node.parent != null) {
            // System.out.println("Visited: " + goal.toString());
            return false;
        }

        visited.put(currentGoal.toString(), currentGoal);

        // check the knowledge base for the goal
        // ArrayList<FPClause> clauses = new ArrayList<>();
        
        // System.out.println("Resolving goal: " + currentGoal.toString());
        clauses.addAll(kb.getClauses(currentGoal.name));

        if (clauses.isEmpty()) {
            return false;
        }
        // System.out.println("Clauses: " + clauses.toString());
        

        // Try to resolve the goal with the clauses
        for (FPClause clause : clauses) {
            // System.out.println("Clause: " + clause.toString());
            if (previousUnifications.contains(clause)) {
                // System.out.println("Skipping: " + clause.toString());
                continue;
            }

            // System.out.println("Trying: " + clause.toString());
            Map<String, FPTerm> newBindings = new HashMap<>(bindings);

            // Unify the head of the clause with the goal
            // System.out.println("Unifying: " + currentGoal.toString() + " with " + clause.head.toString());
            if (unifyGoalWithHead(currentGoal, clause.head, newBindings)) {
                // Create a new goal
                ArrayList<FPTerm> newGoals = new ArrayList<>(goals);
                newGoals.remove(0);
                // Create a new node

                if (clause.body != null) {
                    // System.out.println("Adding: " + clause.body.ts.toString());
                    newGoals.addAll(0, clause.body.ts);
                }

                Node newNode = new Node(newGoals, node, newBindings);

                // Recursively resolve the new node
                if (resolve(newNode, newBindings)) {

                    if (newGoals.isEmpty() && query.body.ts.get(0).args.get(0).kind == TKind.IDENT) {
                        previousUnifications.add(clause);
                    }

                    return true;
                }
                
            } else {
                // System.out.println("Failed: " + clause.toString());
            }
        }

        visited.remove(currentGoal.toString());
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
            System.out.println("Unifying: " + goalArg.toString() + " with " + headParam.toString());
            System.out.println("Theta: " + theta.toString());
            if (!Unifier.unify(goalArg, headParam, theta)) {
                return false;
            }
        }

        System.out.println("Unification successful: " + theta.toString());

        return true;
    }

}