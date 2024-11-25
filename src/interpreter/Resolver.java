package interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != getClass()) return false;
        
        Node node = (Node) o;
        return Objects.equals(goals, node.goals) &&
            // Avoid parent comparison to prevent infinite recursion
            Objects.equals(substitution, node.substitution) &&
            Objects.equals(hasCut, node.hasCut);
    }

    @Override
    public int hashCode() {
        // Don't include parent in hashCode to prevent infinite recursion
        return Objects.hash(goals, substitution, hasCut);
    }
}

public class Resolver {
    // private final FPClause query;
    private final KnowledgeBase kb;
    private final boolean traceEnabled;
    private StringBuilder output;

    private Set<Node> visited;
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
        this.visited = new HashSet<>(); // Visited nodes
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
            if (arg.kind == TKind.IDENT && substitution.containsKey(arg.name)) {
                resolvedArgs.add(substitution.get(arg.name));
            } else if (arg.kind == TKind.CONST && substitution.isEmpty()) {
                resolvedArgs.add(arg);
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
        this.output = new StringBuilder();
        // this.visited.clear(); // clear the visted nodes each time we resolve a new query

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
        // Create the root node for the resolution tree
        Node resolutionRoot = new Node(goals, null, new HashMap<>());
        
        boolean resolution = resolve(resolutionRoot);
        
        // Print result based on query type and resolution success
        if (resolution) {
            // Check if query contains any variables
            boolean hasVariables = this.query.body.ts.get(0).args.stream()
                .anyMatch(arg -> arg.kind == TKind.IDENT);
            
            if (hasVariables) {
                // Filter and format only variable substitutions
                String substitutions = this.bindings.entrySet().stream()
                    .filter(entry -> entry.getKey().matches("[A-Z].*")) // Prolog variables start with uppercase
                    .map(entry -> entry.getKey() + " = " + entry.getValue())
                    .collect(Collectors.joining(", "));
                this.output.append(substitutions);
            } else {
                // Query has only constants
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
            return resolve(node);
        }

        // if (visited.containsKey(currentGoal.toString()) && node.parent != null) {
        //     printTrace("Redo", currentGoal, node.substitution);
        //     return false;
        // }
        // visited.put(currentGoal.toString(), node.substitution);

        if (kb.getFact(currentGoal) != null) {
            FPClause fact = kb.getFact(currentGoal);
            UnificationResult unificationResult = tryUnify(currentGoal, fact, node);

            if (!unificationResult.success) {
                return false;
            }

            ArrayList<FPTerm> newGoals = new ArrayList<>(goals);
            newGoals.remove(0);
            Node child = new Node(newGoals, node, unificationResult.bindings);
            return resolve(child);
        }

        // System.out.println(currentGoal.name);

        clauses.addAll(kb.getClauses(currentGoal.name));
        // System.out.println(kb.getClauses(currentGoal.name));
        for (FPClause clause : kb.getClauses(currentGoal.name)) {
            if (previousUnifications.contains(clause)) {
                continue;
            }

            UnificationResult unificationResult = tryUnify(currentGoal, clause, node);
            if (!unificationResult.success) {
                // printTrace("Fail", currentGoal, node.substitution);
                continue;
            }
            // printTrace("Call", currentGoal, unificationResult.bindings);


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
            // printTrace("Redo", currentGoal, node.substitution);
        }
        
        visited.remove(node);
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
        theta.clear();
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
            this.output.append(node.substitution.get(goal.args.get(0).name) + "\n");
            // this.output.append("\n");
            depth--;
        }
    }

    private record UnificationResult(boolean success, Map<String, FPTerm> bindings) {}

    private UnificationResult tryUnify(FPTerm goal, FPClause clause, Node node) {
        // Try direct unification first
        // System.out.println("Unifying: " + goal.toString() + " with " + clause.toString());
        Node currentNode = node;
        Map<String, FPTerm> newBindings = new HashMap<>(node.substitution);
        if (unifyGoalWithHead(goal, clause.head, newBindings)) {
            if (visited.contains(node)){
                printTrace("Redo", goal, newBindings);
            } else {
                visited.add(node);
                printTrace("Call", goal, newBindings);
            }
            // System.out.println(visited.containsKey(goal.toString()));
            // printTrace("Call", goal, newBindings);
            return new UnificationResult(true, newBindings);
        }
        printTrace("Fail", goal, node.substitution);

        return new UnificationResult(false, null);
    }
}