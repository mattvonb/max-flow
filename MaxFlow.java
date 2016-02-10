import java.util.*;

public class MaxFlow {
    private static class Edge {
        public char source;
        public char sink;
        public int capacity;
        public Edge residualEdge;

        public Edge(char source, char sink, int capacity) {
            this.source = source;
            this.sink = sink;
            this.capacity = capacity;
            this.residualEdge = null;
        }
        
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            
            if (!(o instanceof Edge)) {
                return false;
            }
            Edge that = (Edge)o;
            
            return this.source == that.source && this.sink == that.sink && this.capacity == that.capacity;
        }

        public int hashCode() {
            int hash = 17 + (int)source;
            hash = hash * 31 + (int)sink;
            hash = hash * 31 + capacity;
            return hash;
        }
    }

    private static class Graph {
        private Map<Character, Set<Edge>> edges = new HashMap<>();
        private Map<Edge, Integer> flows = new HashMap<>();

        private void addEdge(char node, Edge e) {
            if (!edges.containsKey(node)) {
                edges.put(node, new HashSet<>());
            }
            edges.get(node).add(e);
        }

        public Set<Edge> getEdges(char node) {
            if (edges.containsKey(node)) {
                return edges.get(node);
            }
            return new HashSet<>();
        }

        public void addEdge(char source, char sink, int capacity) {
            if (source == sink) {
                throw new IllegalArgumentException("source can't equals sink.");
            }
            Edge e = new Edge(source, sink, capacity);
            Edge re = new Edge(sink, source, 0);
            e.residualEdge = re;
            re.residualEdge = e;
            
            addEdge(source, e);
            addEdge(sink, re);

            flows.put(e, 0);
            flows.put(re, 0);
        }
        
        private List<Edge> toPath(Map<Character, Edge> prev, char sink) {
            LinkedList<Edge> path = new LinkedList<>();
            Edge e = null;
            while ((e = prev.get(sink)) != null) {
                path.addFirst(e);
                sink = e.source;
            }
            return path;
        }
        
        private List<Edge> findPath(char source, char sink) {
            if (source == sink) {
                return null;
            }

            Map<Character, Edge> prev = new HashMap<>();
            prev.put(source, null);

            HashSet<Character> visited = new HashSet<>();
            Deque<Character> q = new LinkedList<>();
            q.addFirst(source);
            visited.add(source);

            while (!q.isEmpty()) {
                Character node = q.removeLast();
                for (Edge e : getEdges(node)) {
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

        public int maxFlow(char source, char sink) {
            List<Edge> path = findPath(source, sink);
            while (path != null) {
                List<Integer> residuals = new ArrayList<>();
                for (Edge e : path) {
                    residuals.add(e.capacity - this.flows.get(e));
                }
                int flow = Collections.min(residuals);

                for (Edge e : path) {
                    this.flows.put(e, this.flows.get(e) + flow);
                    this.flows.put(e.residualEdge, this.flows.get(e.residualEdge) - flow);
                }
                path = findPath(source, sink);
            }
            
            int sum = 0;
            for (Edge e : getEdges(source)) {
                sum += this.flows.get(e);
            }
            return sum;
        }
    }

    public static void main(String[] args) {
        Graph g = new Graph();
        g.addEdge('s','a',1);
        g.addEdge('s','b',1);
        g.addEdge('a','c',1);
        g.addEdge('b','c',1);
        g.addEdge('b','d',1);
        g.addEdge('c','e',1);
        g.addEdge('c','f',1);
        g.addEdge('d','f',1);
        g.addEdge('e','t',1);
        g.addEdge('f','t',1);
        System.out.println(g.maxFlow('s', 't'));

        g = new Graph();
        g.addEdge('s','o',3);
        g.addEdge('s','p',3);
        g.addEdge('o','p',2);
        g.addEdge('o','q',3);
        g.addEdge('p','r',2);
        g.addEdge('r','t',3);
        g.addEdge('q','r',4);
        g.addEdge('q','t',2);
        System.out.println(g.maxFlow('s', 't'));

        g = new Graph();
        g.addEdge('s','a',1);
        g.addEdge('s','b',1);
        g.addEdge('s','c',1);
        g.addEdge('s','d',1);

        g.addEdge('a','m',1);
        g.addEdge('b','m',1);
        g.addEdge('c','m',1);
        g.addEdge('d','m',1);

        g.addEdge('a','n',1);
        g.addEdge('b','n',1);
        g.addEdge('c','n',1);
        g.addEdge('d','n',1);

        g.addEdge('m','w',1);
        g.addEdge('m','x',1);
        g.addEdge('m','y',1);
        g.addEdge('m','z',1);

        g.addEdge('n','w',1);
        g.addEdge('n','x',1);
        g.addEdge('n','y',1);
        g.addEdge('n','z',1);

        g.addEdge('w','t',1);
        g.addEdge('x','t',1);
        g.addEdge('y','t',1);
        g.addEdge('z','t',1);
        System.out.println(g.maxFlow('s', 't'));

        g = new Graph();
        g.addEdge('s','a',1);
        g.addEdge('s','b',1);
        g.addEdge('s','c',1);
        g.addEdge('s','d',1);

        g.addEdge('a','w',1);
        g.addEdge('a','x',1);
        g.addEdge('a','y',1);
        g.addEdge('a','z',1);

        g.addEdge('b','w',1);
        g.addEdge('b','x',1);
        g.addEdge('b','y',1);
        g.addEdge('b','z',1);

        g.addEdge('c','w',1);
        g.addEdge('c','x',1);
        g.addEdge('c','y',1);
        g.addEdge('c','z',1);

        g.addEdge('d','w',1);
        g.addEdge('d','x',1);
        g.addEdge('d','y',1);
        g.addEdge('d','z',1);

        g.addEdge('w','m',1);
        g.addEdge('x','m',1);
        g.addEdge('y','m',1);
        g.addEdge('z','m',1);

        g.addEdge('w','n',1);
        g.addEdge('x','n',1);
        g.addEdge('y','n',1);
        g.addEdge('z','n',1);

        g.addEdge('m','t',1);
        g.addEdge('n','t',1);
        System.out.println(g.maxFlow('s', 't'));

        
        g = new Graph();
        g.addEdge('s','a',1);
        g.addEdge('s','b',1);

        g.addEdge('a','w',1);
        g.addEdge('b','w',1);

        g.addEdge('w','m',1);
        g.addEdge('x','m',1);
        g.addEdge('y','m',1);

        g.addEdge('w','n',1);

        g.addEdge('m','t',1);
        g.addEdge('n','t',1);
        System.out.println(g.maxFlow('s', 't'));

        g = new Graph();
        // a: ! - @
        // b: # - $
        // w: % - ^
        // x: & - *
        // y: ( - )
        // m: _ - =
        // n: - - +
        g.addEdge('s','!',1);
        g.addEdge('!','@',1);

        g.addEdge('s','#',1);
        g.addEdge('#','$',1);

        g.addEdge('@','%',1);
        g.addEdge('%','^',1);
        g.addEdge('$','%',1);

        g.addEdge('^','_',1);
        g.addEdge('_','=',1);
        g.addEdge('&','*',1);
        g.addEdge('*','_',1);
        g.addEdge('(',')',1);
        g.addEdge(')','_',1);


        g.addEdge('^','-',1);
        g.addEdge('-','+',1);

        g.addEdge('=','t',1);
        g.addEdge('+','t',1);
        System.out.println(g.maxFlow('s', 't'));


    }
}
