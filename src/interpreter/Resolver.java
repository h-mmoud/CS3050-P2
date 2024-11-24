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
    public boolean hasCut;
    public boolean hasWrite;

    public ArrayList<Node> children;
    public Map<String, FPTerm> theta;

    public Node(ArrayList<FPTerm> goals, Node parent, Map<String, FPTerm> substitution) {
        this.goals = goals;
        this.parent = parent;
        this.substitution = substitution;
        this.hasCut = parent != null ? parent.hasCut : false;
        this.hasWrite = false;
    }
}

public class Resolver {
    // private final FPClause query;
    private final KnowledgeBase kb;
    private final boolean traceEnabled;
    private StringBuilder output;

    private Map<String, FPTerm> visited;
    private Map<String, FPTerm> bindings;
    private String orignalQueryName;
    private LinkedHashSet<FPClause> clauses;
    private FPClause query;

    private int depth = 0;



    private static Set<FPClause> previousUnifications;
    private static FPClause previousQuery;

    // private Node resolutionRoot; 

    public Resolver(KnowledgeBase kb, boolean trace) {
        // this.query = query;
        this.kb = kb;
        this.traceEnabled = trace;
        this.visited = new HashMap<>(); // Visited nodes
        this.bindings = new HashMap<>(); // Bindings for the current resolution
        this.clauses = new LinkedHashSet<>();
        this.output = new StringBuilder();
        this.query = null;

        // static variables to store previous query and unifications for backtracking
        Resolver.previousQuery = null;
        Resolver.previousUnifications = new HashSet<>();
    }

    private void printTrace(String prefix, FPTerm goal, Map<String, FPTerm> substitution) {
    if (!traceEnabled) {
        return;
    }
    
    String indent = "";
    StringBuilder sb = new StringBuilder(indent + prefix + ": ");
    
    // Create copy of goal with new resolved args list
    ArrayList<FPTerm> resolvedArgs = new ArrayList<>();
    if (goal.args != null) {
        for (FPTerm arg : goal.args) {
;
            if (arg.kind == TKind.IDENT && substitution.containsKey(arg.name)) {
                resolvedArgs.add(substitution.get(arg.name));
            } else {
                return;
            }
        }
    }
    
    FPTerm resolvedGoal = new FPTerm(goal.kind, goal.name, resolvedArgs);
    sb.append(resolvedGoal.toString());
    System.out.println(sb.toString());
    
    }
    
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
            String substitutions = this.bindings.entrySet().stream() 
                .map(entry -> entry.getKey() + " = " + entry.getValue())
                .collect(Collectors.joining(", "));
            this.output.append(substitutions);
        } else {
            this.output.append("yes");
        }
    } else {
        this.output.append("no");
    }
        System.out.println(this.output.toString());
        return resolution;
    }

    // depth-first search to resolve the query
    private boolean resolve(Node node) {
        ArrayList<FPTerm> goals = node.goals;


        if (goals.isEmpty()) {
            this.bindings = node.substitution;

            return true;
        }

        FPTerm currentGoal = goals.get(0);
        // System.out.println("Current goal: " + currentGoal.toString());
        if (currentGoal.name.equals("write")) {
            write(node, currentGoal, this.bindings);
            goals.remove(0);
            return resolve(node);
        }

        if (currentGoal.name.equals("cut")) {
            node.hasCut = true;
            goals.remove(0);
            resolve(node);
        }

        if (visited.containsKey(currentGoal.toString()) && node.parent != null) {
            return false;
        }
        visited.put(currentGoal.toString(), currentGoal);

        clauses.addAll(kb.getClauses(currentGoal.name));

        for (FPClause clause : kb.getClauses(currentGoal.name)) {
            
            UnificationResult unificationResult = tryUnify(currentGoal, clause, node);
            if (!unificationResult.success) {
                printTrace("Fail", currentGoal, node.substitution);
                continue;
            }
            printTrace("Call", currentGoal, unificationResult.bindings);

            if (previousUnifications.contains(clause)) {
                printTrace("Call", currentGoal, node.substitution);
                continue;
            }

            ArrayList<FPTerm> newGoals = new ArrayList<>(goals);
            newGoals.remove(0);

            if (clause.body != null){
                newGoals.addAll(0, clause.body.ts);
            }


            Node child = new Node(newGoals, node, unificationResult.bindings);
            if (resolve(child)) {
                if (newGoals.isEmpty() && query.body.ts.get(0).args.get(0).kind == TKind.IDENT) {
                    previousUnifications.add(clause);
                }
                return true;
            } else if (child.hasCut) {
                return false;
            }
            printTrace("Redo", currentGoal, node.substitution);
        }
        
        visited.remove(currentGoal.toString());
        return false;
    }

    // Generate a unique key for the query
    private boolean unifyGoalWithHead(FPTerm goal, FPHead head, Map<String, FPTerm> theta) {
        // Check if the functor names are the same
        boolean success = false;
        // printTrace("Call", goal, theta);

        if (!goal.name.equals(head.name)) {
            return false;
        }

        // Check if the arity matches
        int goalArity = goal.args != null ? goal.args.size() : 0;
        int headArity = head.params != null ? head.params.size() : 0;
        if (goalArity != headArity) {
            return false;
        }

        Map<String, FPTerm> newTheta = new HashMap<>(theta);
        
        // Unify each corresponding argument
        for (int i = 0; i < goalArity; i++) {
            FPTerm goalArg = goal.args.get(i);
            FPTerm headParam = head.params.get(i);
            if (!Unifier.unify(goalArg, headParam, newTheta)) {
                return false;
            }
        }
        theta.putAll(newTheta);

        // printTrace("Call", goal, theta);
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

            // for (FPClause predicate : kb.getClauses(writeGoal.name)) {
            FPClause predicate = kb.getClauses(writeGoal.name).get(0);    
            printTrace("Call", goal, node.substitution);
            System.out.println(predicate.head.params.get(0));
            // }
            System.out.println();
            depth--;
        }
    }

    private record UnificationResult(boolean success, Map<String, FPTerm> bindings) {}

    private UnificationResult tryUnify(FPTerm goal, FPClause clause, Node node) {
        // Try direct unification first
        Node currentNode = node;
        Map<String, FPTerm> newBindings = new HashMap<>(node.substitution);
        if (unifyGoalWithHead(goal, clause.head, newBindings)) {
            return new UnificationResult(true, newBindings);
        }
        // printTrace("Fail", goal, newBindings);
        // Try alternative bindings if direct unification fails
        // currentNode = node.parent;

        // for (String var : node.substitution.keySet()) {
        //     newBindings = new HashMap<>(node.substitution);
        //     newBindings.remove(var);
        //     if (unifyGoalWithHead(goal, clause.head, newBindings)) {
        //         printTrace("Redo", node.parent.goals.get(0), newBindings);
        //         return new UnificationResult(true, newBindings);
        //     }
        // }
        
        return new UnificationResult(false, null);
    }
}