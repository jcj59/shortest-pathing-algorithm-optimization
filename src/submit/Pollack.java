package submit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import graph.FindState;
import graph.Finder;
import graph.FleeState;
import graph.Node;
import graph.NodeStatus;

/** A solution with find-the-Orb optimized and flee getting out as fast as possible. */
public class Pollack extends Finder {

    /** Get to the orb in as few steps as possible. <br>
     * Once you get there, you must return from the function in order to pick it up. <br>
     * If you continue to move after finding the orb rather than returning, it will not count.<br>
     * If you return from this function while not standing on top of the orb, it will count as <br>
     * a failure.
     *
     * There is no limit to how many steps you can take, but you will receive<br>
     * a score bonus multiplier for finding the orb in fewer steps.
     *
     * At every step, you know only your current tile's ID and the ID of all<br>
     * open neighbor tiles, as well as the distance to the orb at each of <br>
     * these tiles (ignoring walls and obstacles).
     *
     * In order to get information about the current state, use functions<br>
     * currentLoc(), neighbors(), and distanceToOrb() in FindState.<br>
     * You know you are standing on the orb when distanceToOrb() is 0.
     *
     * Use function moveTo(long id) in FindState to move to a neighboring<br>
     * tile by its ID. Doing this will change state to reflect your new position.
     *
     * A suggested first implementation that will always find the orb, but <br>
     * likely won't receive a large bonus multiplier, is a depth-first search. <br>
     * Some modification is necessary to make the search better, in general. */
    @Override
    public void find(FindState state) {
        // TODO 1: Walk to the orb
        HashSet<Long> visited= new HashSet<>();
        // Stack<Long> path= new Stack<>();
        // dfsWalk(state, visited, path);
        // dfsWalkOptimized(state, visited);
        dfsWalkBest(state, visited);
    }

    /** Returns true if contains path to orb. */
    public boolean dfsWalk(FindState state, HashSet<Long> visited, Stack<Long> path) {
        if (state.distanceToOrb() == 0) return true;
        long u= state.currentLoc();
        visited.add(u);
        // path.add(u);
        for (NodeStatus n : state.neighbors()) {
            var w= n.getId();
            if (!visited.contains(w)) {
                path.add(u);
                state.moveTo(w);
                if (dfsWalk(state, visited, path)) return true;
            }
        }
        state.moveTo(path.pop());
        return false;
    }

    /** Returns true if contains path to orb. */
    public boolean dfsWalkOptimized(FindState state, HashSet<Long> visited) {
        if (state.distanceToOrb() == 0) return true;
        long u= state.currentLoc();
        visited.add(u);
        // path.add(u);
        Heap<Long> h= new Heap<>(true);
        for (NodeStatus n : state.neighbors()) {
            long w= n.getId();
            if (!visited.contains(w)) {
                h.insert(w, n.getDistanceToTarget());
            }
        }
        while (h.size != 0) {
//            path.add(u);
            state.moveTo(h.poll());
            if (dfsWalkOptimized(state, visited)) return true;
            state.moveTo(u);
        }
        return false;
    }

    /** Returns true if path contains orb. */
    public boolean dfsWalkBest(FindState state, HashSet<Long> visited) {
        if (state.distanceToOrb() == 0) return true;
        long current= state.currentLoc();
        visited.add(current);
        List<NodeStatus> neighborList= new ArrayList<>();
        for (NodeStatus neighbor : state.neighbors()) {
            neighborList.add(neighbor);
        }
        insertionSort(neighborList);
        for (NodeStatus neighbor : neighborList) {
            if (!visited.contains(neighbor.getId())) {
                state.moveTo(neighbor.getId());
                if (dfsWalkBest(state, visited)) return true;
                state.moveTo(current);
            }
        }
        return false;
    }

    /** InsertionSort: sorts list in stable way with expected time complexity of O(n^2). */
    public void insertionSort(List<NodeStatus> unsorted) {
        int n= unsorted.size();
        for (int i= 1; i < n; i++ ) {
            NodeStatus keyNode= unsorted.get(i);
            int val= keyNode.getDistanceToTarget();
            int j= i - 1;
            while (j >= 0 && unsorted.get(j).getDistanceToTarget() > val) {
                unsorted.set(j + 1, unsorted.get(j));
                j= j - 1;

            }
            unsorted.set(j + 1, keyNode);
        }
    }

    /** Get out the cavern before the ceiling collapses, trying to collect as <br>
     * much gold as possible along the way. Your solution must ALWAYS get out <br>
     * before steps runs out, and this should be prioritized above collecting gold.
     *
     * You now have access to the entire underlying graph, which can be accessed <br>
     * through FleeState state. <br>
     * currentNode() and exit() will return Node objects of interest, and <br>
     * allsNodes() will return a collection of all nodes on the graph.
     *
     * Note that the cavern will collapse in the number of steps given by <br>
     * stepsLeft(), and for each step this number is decremented by the <br>
     * weight of the edge taken. <br>
     * Use stepsLeft() to get the steps still remaining, and <br>
     * moveTo() to move to a destination node adjacent to your current node.
     *
     * You must return from this function while standing at the exit. <br>
     * Failing to do so before steps runs out or returning from the wrong <br>
     * location will be considered a failed run.
     *
     * You will always have enough steps to flee using the shortest path from the <br>
     * starting position to the exit, although this will not collect much gold. <br>
     * For this reason, using Dijkstra's to plot the shortest path to the exit <br>
     * is a good starting solution
     *
     * Here's another hint. Whatever you do you will need to traverse a given path. It makes sense
     * to write a method to do this, perhaps with this specification:
     *
     * // Traverse the nodes in moveOut sequentially, starting at the node<br>
     * // pertaining to state <br>
     * // public void moveAlong(FleeState state, List<Node> moveOut) */
    @Override
    public void flee(FleeState state) {
        // TODO 2. Get out of the cavern in time, picking up as much gold as possible.

        // Create a hashmap that contains each node and a TileInfo object that
        // contains the relevant information pertaining to the node.
        HashMap<Node, TileInfo> tiles= new HashMap<>();
        for (Node n : state.allNodes()) {
            tiles.put(n, new TileInfo(n, state));
        }

        // Create a list of the tiles that can be reached safely.
        ArrayList<TileInfo> target= new ArrayList<>();
        for (TileInfo i : tiles.values()) {
            if (i.pathLength < state.stepsLeft() && i.goldToLengthRatio > 0) {
                target.add(i);
            }
        }

        // Sort the list so that the highest valued tile is in position 0.
        Collections.sort(target, Collections.reverseOrder());

        // Create a hashset that contains the nodes visited.
        HashSet<TileInfo> visited= new HashSet<>();
        visited.add(tiles.get(state.currentNode()));

        // While there are still unvisited tiles that contain gold and are reachable,
        // move towards the tile with the highest goldToLengthRatio
        while (target.size() > 0) {
            // Move towards the tile with highest goldToLengthRatio
            state.moveTo(target.get(0).shortestPath.get(1));

            // Mark current node as visited
            visited.add(tiles.get(state.currentNode()));

            // Update each tile in target and remove tiles that either have no
            // gold on their path, have been visited, or are unsafe to travel to
            for (int j= 0; j < target.size();) {
                TileInfo i= target.get(j);
                i.update(state);
                if (visited.contains(i) || i.goldToLengthRatio == 0 ||
                    i.pathLength >= state.stepsLeft()) {
                    target.remove(j);
                } else j++ ;

            }

            // Resort target
            Collections.sort(target, Collections.reverseOrder());
        }

        // Take shortest path to exit
        panic(state);
    }

    /** Traverse the nodes in moveOut sequentially, starting at the node<br>
     * pertaining to state. */
    public void moveAlong(FleeState state, List<Node> moveOut) {
        moveOut.remove(0);
        for (Node n : moveOut) {
            state.moveTo(n);
        }
    }

    /** Moves along the shortest path from current tile to exit. */
    public void panic(FleeState state) {
        List<Node> exitPath= Path.shortestPath(state.currentNode(), state.exit());
        moveAlong(state, exitPath);
    }

    /** Returns the shortest path from current tile to all other tiles. */
    public HashMap<Node, List<Node>> shortestPathToAll(FleeState state) {
        Collection<Node> allNodes= state.allNodes();
        Node current= state.currentNode();
        HashMap<Node, List<Node>> allPaths= new HashMap<>();
        for (Node n : allNodes) {
            allPaths.put(n, Path.shortestPath(current, n));
        }
        return allPaths;
    }

    /** Returns a HashMap containing the shortest path from each tile to the exit node with the tile
     * as a key and the path as a value. */
    public HashMap<Node, List<Node>> shortestExitFromAll(FleeState state) {
        HashMap<Node, List<Node>> result= new HashMap<>();
        for (Node n : state.allNodes()) {
            result.put(n, Path.shortestPath(n, state.exit()));
        }
        return result;
    }

    /** Returns the total amount of gold along the path */
    public int goldOnPath(List<Node> path) {
        int sum= 0;
        for (Node i : path) {
            sum= sum + i.getTile().gold();
        }
        return sum;
    }

    /** An instance contains information about the corresponding node: Node n: the corresponding
     * node List<Node> shortestExit: the shortest path from n to exit List<Node> shortestPath: the
     * shortest path from current node in state to n int gold: the gold value of the Node */
    private class TileInfo implements Comparable<TileInfo> {
        /** the corresponding node */
        private Node n;

        /** the shortest path from n to exit */
        private List<Node> shortestExit;

        /** the shortest path from current to n */
        private List<Node> shortestPath;

        /** the gold value along shortest path from current node to n */
        private int gold;

        /** the distance from the current tile to n + the length of the escape path */
        private int pathLength;

        /** the ratio between gold and length of shortestPath */
        private long goldToLengthRatio;

        /** Constructs a TileInfo for Node tile given the current FleeState. */
        private TileInfo(Node tile, FleeState state) {
            n= tile;
            shortestExit= Path.shortestPath(n, state.exit());
            shortestPath= Path.shortestPath(state.currentNode(), n);
            int length= Path.pathSum(shortestPath);
            gold= goldOnPath(shortestPath);
            goldToLengthRatio= length > 0 ? gold / length : 0;
            pathLength= Path.pathSum(shortestExit) + length;
        }

        /** Updates fields based off of current FleeState. */
        public void update(FleeState state) {
            shortestPath= Path.shortestPath(state.currentNode(), n);
            int length= Path.pathSum(shortestPath);
            gold= goldOnPath(shortestPath);
            goldToLengthRatio= length > 0 ? gold / length : 0;
            pathLength= Path.pathSum(shortestExit) + length;
        }

        /** Returns a positive int if goldToLengthRatio > obj.goldToLengthRatio a negative int if
         * less than, and 0 if equal to each other. */
        @Override
        public int compareTo(TileInfo obj) {
            if (goldToLengthRatio > obj.goldToLengthRatio) {
                return 1;
            } else if (goldToLengthRatio < obj.goldToLengthRatio) {
                return -1;
            } else return pathLength - obj.pathLength;
        }

    }
}
