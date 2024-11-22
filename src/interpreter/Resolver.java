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

        // static variables to store previous query and unifications for backtracking
        Resolver.previousQuery = null;
        Resolver.previousUnifications = new HashSet<>();
    }
    
    // depth-first search to resolve the query
    public boolean resolve(FPClause query) {
        ArrayList<FPTerm> goals;
        this.visited.clear(); // clear the visted nodes each time we resolve a new query

        // Check if the query is a query
        if (query.head != null) {
            System.out.println("Not a query: " + query.toString());
            return false;
        }

        // If attempting resolve the same query, find a different result
        // If there is no previous query, return false
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
            this.query = query;
            Resolver.previousQuery = query;
        }

        // Create the root node for the resolution tree
        Node resolutionRoot = new Node(goals, null, new HashMap<>());
        
        boolean resolution = resolve(resolutionRoot);

        // Print the bindings or yes/no depending on the query.
        if (resolution) {
            FPTerm queryArg = this.query.body.ts.get(0).args.get(0);
            if (queryArg.kind == TKind.IDENT) {
                // Format the substitutions
                String output = this.bindings.entrySet().stream() 
                    .map(entry -> entry.getKey() + " = " + entry.getValue())
                    .collect(Collectors.joining(", "));
                System.out.println(output);
            } else {
                System.out.println("yes");
            }
        } else {
            System.out.println("no");
        }
        return resolution;
    }

    // Recursively resolve the query
    private boolean resolve(Node node) {
        ArrayList<FPTerm> goals = node.goals;

        // Check if the goal is empty (Base case)
        if (goals.isEmpty()) {
            this.bindings = node.substitution;
            return true;
        }

        // If the goal is the primitive predicate write, print the terms that succeeds
        if (goals.get(0).name.equals("write")) {
            write(node, goals.get(0), node.substitution);

            // Remove the goal so it doesn't get resolved
            goals.remove(0);
        }

        // Start with the first goal
        FPTerm currentGoal = goals.get(0);

        // if the goal has been visited before, return false to avoid infinite loops
        if (visited.containsKey(currentGoal.toString()) && node.parent != null) {
            return false;
        }
        visited.put(currentGoal.toString(), currentGoal);

        // Add the clauses for the current goal to the linked hashset of clauses (allows for ordering and no duplicates)
        clauses.addAll(kb.getClauses(currentGoal.name));

        // If there are no clauses for the goal in the knowledge base, return false
        if (clauses.isEmpty()) {
            return false;
        }

        // Depth first search to resolve the goal
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

                // Create a new node with the new goals and bindings
                Node newNode = new Node(newGoals, node, newBindings);

                // Recursively resolve the new node
                if (resolve(newNode)) {
                    if (newGoals.isEmpty() && query.body.ts.get(0).args.get(0).kind == TKind.IDENT) {
                        previousUnifications.add(clause);
                    }
                    this.bindings.putAll(newNode.substitution);
                    return true;
                }
            } else {
                if (tryAlternativeUnification(currentGoal, clause, node.substitution)) {
                    return true;
                }
            }
        }
        visited.remove(currentGoal.toString());
        return false;
    }

    private boolean tryAlternativeUnification(FPTerm goal, FPClause clause, Map<String, FPTerm> nodeBindings) {
        for (String var : nodeBindings.keySet()) {
            Map<String, FPTerm> tempBindings = new HashMap<>(nodeBindings); 
            tempBindings.remove(var);  // Remove one binding

            if (unifyGoalWithHead(goal, clause.head, tempBindings)) {
                nodeBindings.putAll(tempBindings);
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

    private void write(Node node, FPTerm goal, Map<String, FPTerm> theta) {
        Node parentNode = node.parent;
        FPTerm writeGoal = null;
        
        if (parentNode != null) {
            ArrayList<FPTerm> parentGoals = parentNode.goals;

            for (FPTerm subGoal : parentGoals) { // Write all the matching 
                if (subGoal.name.equals("write")) {
                    break;
                }
               writeGoal = subGoal;
            }

            for (FPClause predicate : kb.getClauses(writeGoal.name)) {
                System.out.println(predicate.head.params.get(0));
            }
            System.out.println();
        }
    }
}