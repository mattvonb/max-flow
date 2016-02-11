import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;

public class FlowGraph <T> {
    private Map<T, Set<FlowEdge<T>>> edges = new HashMap<>();
    private Map<FlowEdge<T>, Integer> flows = new HashMap<>();

    public Set<FlowEdge<T>> getEdges(T node) {
        if (edges.containsKey(node)) {
            return edges.get(node);
        }
        return new HashSet<>();
    }

    public void addEdge(T source, T sink, int capacity) {
        if (source.equals(sink)) {
            throw new IllegalArgumentException("source can't equal sink.");
        }
        FlowEdge<T> e = new FlowEdge<>(source, sink, capacity);
        FlowEdge<T> re = new FlowEdge<>(sink, source, 0);
        e.residualEdge = re;
        re.residualEdge = e;
            
        addEdge(source, e);
        addEdge(sink, re);

        flows.put(e, 0);
        flows.put(re, 0);
    }

    private void addEdge(T node, FlowEdge<T> e) {
        if (!edges.containsKey(node)) {
            edges.put(node, new HashSet<>());
        }
        edges.get(node).add(e);
    }
    
    public int maxFlow(T source, T sink) {
        List<FlowEdge<T>> path = findPath(source, sink);
        while (path != null) {
            List<Integer> residuals = new ArrayList<>();
            for (FlowEdge<T> e : path) {
                residuals.add(e.capacity - flows.get(e));
            }
            int flow = Collections.min(residuals);

            for (FlowEdge<T> e : path) {
                int newFlow = flows.get(e) + flow;
                int backFlow = flows.get(e.residualEdge) - flow;

                flows.put(e, newFlow);
                flows.put(e.residualEdge, backFlow);
            }
            path = findPath(source, sink);
        }
            
        int sum = 0;
        for (FlowEdge<T> e : getEdges(source)) {
            sum += flows.get(e);
        }
        return sum;
    }
        
    /**
     * BFS from +source+ to +sink+. At the end of the search walk
     * backwards through +prev+ starting with sink to 
     * get the path.
     */
    private List<FlowEdge<T>> findPath(T source, T sink) {
        if (source == sink) {
            return null;
        }

        Map<T, FlowEdge<T>> prev = new HashMap<>();
        prev.put(source, null);

        HashSet<T> visited = new HashSet<>();
        Deque<T> q = new LinkedList<>();
        q.addFirst(source);
        visited.add(source);

        while (!q.isEmpty()) {
            T node = q.removeLast();
            for (FlowEdge<T> e : getEdges(node)) {
                int residual = e.capacity - flows.get(e);
                if (residual > 0 && !visited.contains(e.sink)) {
                    prev.put(e.sink, e);
                    if (e.sink == sink) {
                        return toPath(prev, sink);
                    }

                    visited.add(e.sink);
                    q.addFirst(e.sink);
                }
            }
        }
        return null;
    }

    private List<FlowEdge<T>> toPath(Map<T, FlowEdge<T>> prev, T sink) {
        LinkedList<FlowEdge<T>> path = new LinkedList<>();
        FlowEdge<T> e = null;
        while ((e = prev.get(sink)) != null) {
            path.addFirst(e);
            sink = e.source;
        }
        return path;
    }
}
    
