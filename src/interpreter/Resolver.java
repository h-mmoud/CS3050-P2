package interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
            if (!this.bindings.isEmpty()) {
                String formatted = this.bindings.entrySet().stream()
                    .map(entry -> entry.getKey() + " = " + entry.getValue())
                    .collect(Collectors.joining(", "));
                System.out.println(formatted);
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

        // Check if the goal is empty
        if (goals.isEmpty()) {
            this.bindings = node.substitution;
            return true;
        }

        // Get the first goal
        FPTerm currentGoal = goals.get(0);

        if (visited.containsKey(currentGoal.toString()) && node.parent != null) {
            return false;
        }
        visited.put(currentGoal.toString(), currentGoal);
        
        // System.out.println("Resolving goal: " + currentGoal.toString());
        clauses.addAll(kb.getClauses(currentGoal.name));

        if (clauses.isEmpty()) {
            return false;
        }

        // Try to resolve the goal with the clauses
        for (FPClause clause : clauses) {
            
            if (previousUnifications.contains(clause)) {
                continue;
            }

            Map<String, FPTerm> newBindings = new HashMap<>(node.substitution);

            // Unify the head of the clause with the goal
            if (unifyGoalWithHead(currentGoal, clause.head, newBindings)) {
                // Create a new goal
                ArrayList<FPTerm> newGoals = new ArrayList<>(goals);
                newGoals.remove(0);

                // Add the body of the clause to the new goals
                if (clause.body != null) {
                    newGoals.addAll(0, clause.body.ts);
                }

                Node newNode = new Node(newGoals, node, newBindings);

                // Recursively resolve the new node
                if (resolve(newNode, newBindings)) { // TODO: 
                    if (newGoals.isEmpty() && query.body.ts.get(0).args.get(0).kind == TKind.IDENT) {
                        previousUnifications.add(clause);
                    }
                    return true;
                }
            } else {
                if (tryAlternativeUnification(currentGoal, clause, node.substitution)) {
                    this.bindings = node.substitution;
                    return true;
                }
            }
        }
        visited.remove(currentGoal.toString());
        return false;
    }

    private boolean tryAlternativeUnification(FPTerm goal, FPClause clause, Map<String, FPTerm> originalBindings) {
        for (String var : originalBindings.keySet()) {
            Map<String, FPTerm> tempBindings = new HashMap<>(originalBindings);
            tempBindings.remove(var);  // Remove one binding

            if (unifyGoalWithHead(goal, clause.head, tempBindings)) {
                originalBindings.clear();
                originalBindings.putAll(tempBindings);
                return true;
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

        Map<String, FPTerm> thetaCopy = new HashMap<>(theta);

        // Unify each corresponding argument
        for (int i = 0; i < goalArity; i++) {
            FPTerm goalArg = goal.args.get(i);
            FPTerm headParam = head.params.get(i);
            if (!Unifier.unify(goalArg, headParam, thetaCopy)) {
                return false;
            }
        }
        theta.clear();
        theta.putAll(thetaCopy);
        System.out.println("Unification successful: " + theta.toString());


        return true;
    }

}