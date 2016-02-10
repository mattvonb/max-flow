import java.io.*;
import java.util.*;

/**
 * Solves this problem by reducing it into a max flow problem. I then use... well I started with Ford-Fulkerson
 * but that was proving to be too slow, so I replaced the recursive depth-first path search with a breadth-first search,
 * which I guess makes it Edmonds-Karp...
 * 
 * How this works:
 *
 * Basically, for each W, we use a breadth-first search to find all F and M that can be reached from it.
 * We add an edge from each of those Fs to that W, and an edge from that W to each of those Ms.
 * We also add an ultimate source and sink node, so each F gets an edge from that source and each M gets an edge to that sink.
 * Finally, to enforce the fact that each resource can be utilized by just a single ant, we convert each node in the graph 
 * (other than the ultimate source & sink) to a pair of nodes with one edge between them having a capacity of 1.
 */
class TestFlow {
    private static class Point {
        public final int row;
        public final int col;
        public Point(int row, int col) {
            this.row = row;
            this.col = col;
        }
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }
            if (!(o instanceof Point)) {
                return false;
            }
            Point that = (Point)o;
            return this.row == that.row && this.col == that.col;
        }
        public int hashCode() {            
            return (17+row) * (31+col);
        }

        public String toString() {
            return "(" + col + ", " + row + ")";
        }
    }
    
    private static class Edge {
        public final Point source;
        public final Point sink;
        public final int capacity;
        public Edge residualEdge;
        
        public Edge(Point source, Point sink, int capacity) {
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
            
            return this.source.equals(that.source) && this.sink.equals(that.sink) && this.capacity == that.capacity;
        }

        public int hashCode() {
            int hash = 17 + source.hashCode();
            hash = hash * 31 + sink.hashCode();
            hash = hash * 31 + capacity;
            return hash;
        }
    }

    private static class FlowGraph {
        private Map<Point, Set<Edge>> edges;
        private Map<Edge, Integer> flows;
        public final Point source;
        public final Point sink;

        public FlowGraph(Point source, Point sink) {
            edges = new HashMap<>();
            flows = new HashMap<>();
            this.source = source;
            this.sink = sink;
        }

        private void addEdge(Point node, Edge e) {
            if (!edges.containsKey(node)) {
                edges.put(node, new HashSet<>());
            }
            edges.get(node).add(e);
        }

        public Set<Edge> getEdges(Point node) {
            if (edges.containsKey(node)) {
                return edges.get(node);
            }
            return new HashSet<>();
        }

        /**
         * Simulate node capacity by replacing each node (besides our ultimate source and sink)
         * with a pair of nodes with a single edge with a single capacity between them.
         */
        public void addEdge(Point source, Point sink) {
            //            System.out.println("virtual edge: [" + source + " " + sink + "]");
            Point sourceIn = new Point(source.row, ~source.col);
            Point sourceOut = new Point(~source.row, source.col);

            Point sinkIn = new Point(sink.row, ~sink.col);
            Point sinkOut = new Point(~sink.row, sink.col);

            if (source.equals(this.source) && sink.equals(this.sink)) {
                addEdge(source, sink, 1);
            }
            else if (source.equals(this.source)) {
                addEdge(source, sinkIn, 1);
                addEdge(sinkIn, sinkOut, 1);
            }
            else if (sink.equals(this.sink)) {
                addEdge(sourceIn, sourceOut, 1);
                addEdge(sourceOut, sink, 1);
            }
            else {
                addEdge(sourceIn, sourceOut, 1);
                addEdge(sourceOut, sinkIn, 1);
                addEdge(sinkIn, sinkOut, 1);
            }
        }

        private void addEdge(Point source, Point sink, int capacity) {
            //            System.out.println("real edge: [" + source + " " + sink + "]");
            if (source.equals(sink)) {
                throw new IllegalArgumentException("source can't equal sink.");
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

         private List<Edge> toPath(Map<Point, Edge> prev, Point sink) {
            LinkedList<Edge> path = new LinkedList<>();
            Edge e = null;
            while ((e = prev.get(sink)) != null) {
                path.addFirst(e);
                sink = e.source;
            }
            return path;
        }
        
        private List<Edge> findPath(Point source, Point sink) {
            if (source == sink) {
                return null;
            }

            Map<Point, Edge> prev = new HashMap<>();
            prev.put(source, null);

            HashSet<Point> visited = new HashSet<>();
            Deque<Point> q = new LinkedList<>();
            q.addFirst(source);
            visited.add(source);

            while (!q.isEmpty()) {
                Point node = q.removeLast();
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

        public int maxFlow(Point source, Point sink) {
            List<Edge> path = findPath(source, sink);
            while (path != null) {
                List<Integer> residuals = new ArrayList<>();
                for (Edge e : path) {
                    residuals.add(e.capacity - flows.get(e));
                }
                int flow = Collections.min(residuals);

                for (Edge e : path) {
                    flows.put(e, flows.get(e) + flow);
                    flows.put(e.residualEdge, flows.get(e.residualEdge) - flow);
                }
                path = findPath(source, sink);
            }
            
            int sum = 0;
            for (Edge e : getEdges(source)) {
                sum += flows.get(e);
            }
            return sum;
        }
    }
        
    private static final char GRASS = '.';
    private static final char ROCKS = 'X';
    private static final char FRUIT = 'F';
    private static final char MEAT  = 'M';
    private static final char WORKPLACE  = 'W';
    
    // might've been quicker to use regex to do this, but whatever, it's done for now...
    public static int parseRowCount(String input) {
        return Integer.parseInt(input.substring(0, input.indexOf(',')));
    }
    
    public static int parseColumnCount(String input) {
        final String delimiter = ", ";
        int start = input.indexOf(delimiter)+delimiter.length();
        return Integer.parseInt(input.substring(start, input.indexOf(',', start)));
    }
    
    public static int parseDistance(String input) {
        final String delimiter = ", ";
        int start = input.lastIndexOf(delimiter)+delimiter.length();
        return Integer.parseInt(input.substring(start));
    }
    
    public static char[][] parseMatrix(int NUM_ROWS, int NUM_COLS, BufferedReader in) throws Exception {
        char[][] matrix = new char[NUM_ROWS][NUM_COLS]; 
        for (int i = 0; i < NUM_ROWS; i++) {
            matrix[i] = in.readLine().toCharArray();
        }
        return matrix;
    }

    public static void search(FlowGraph g, char[][] matrix, Point workplace, int K) {
        // keep track of all the fruit and meet we can reach from +workplace+
        Set<Point> fruits = new HashSet<>();
        Set<Point> meats = new HashSet<>();

        int N = matrix.length;
        int M = matrix[0].length;

        Set<Point> visited = new HashSet<>();

        Deque<Point> q1 = new LinkedList<>();
        Deque<Point> q2 = new LinkedList<>();

        Deque<Point> curQ = q1;
        Deque<Point> nextQ = q2;
        int curDist = 1;
        
        curQ.addFirst(workplace);
        visited.add(workplace);

        do {
            Point p = curQ.removeLast();
            Point[] neighbors = new Point[] { new Point(p.row-1, p.col), new Point(p.row, p.col-1), new Point(p.row, p.col+1), new Point(p.row+1, p.col)};
            for (Point next : neighbors) {
                if (next.row >= 0 && next.row < N && next.col >= 0 && next.col < M) {
                    char item = matrix[next.row][next.col];
                    if (item == MEAT) {
                        meats.add(next);
                    }
                    else if (item == FRUIT) {
                        fruits.add(next);
                    }
                    else if (item == GRASS && !visited.contains(next)) {
                        visited.add(next);
                        nextQ.addFirst(next);
                    }
                }
            }
            if (curQ.isEmpty()) {
                curQ = nextQ;
                nextQ = new LinkedList<>();
                curDist++;
            }
        } while (curDist <= K && !curQ.isEmpty());

        // create edges for all the potential {fruit, meat, workplace} paths we found
        // and add them to the graph
        // for (Point fruit : fruits) {
        //     g.addEdge(g.source, fruit);
        //     for (Point meat : meats) {
        //         g.addEdge(fruit, meat);
        //     }
        // }

        // for (Point meat : meats) {
        //     g.addEdge(meat, workplace);
        // }

        // g.addEdge(workplace, g.sink);

        for (Point fruit : fruits) {
            g.addEdge(g.source, fruit);
            g.addEdge(fruit, workplace);
        }

        for (Point meat : meats) {
            g.addEdge(workplace, meat);
            g.addEdge(meat, g.sink);
        }
    }
    
    
    public static void constructEdges(FlowGraph g, char[][] matrix, int workplaceRow, int workplaceCol, int K) {
        Point start = new Point(workplaceRow, workplaceCol);
        search(g, matrix, start, K);
    }

    public static int countAnts(char[][] matrix, int NUM_ROWS, int NUM_COLS, int K) {
        int count = 0;

        // doesn't matter what our source and sink are, as long as they are unique
        // in the graph and can be referenced later.
        FlowGraph g = new FlowGraph(new Point(-1, -1), new Point(-2, -2));

        for (int row = 0; row < NUM_ROWS; row++) {
            for (int col = 0; col < NUM_COLS; col++) {
                if (matrix[row][col] == WORKPLACE) {
                    constructEdges(g, matrix, row, col, K);
                }
            }
        }

	    return g.maxFlow(g.source, g.sink);
    }
    

    // public static void main(String [] args) throws Exception{
    //     BufferedReader in = new BufferedReader(
    //         new InputStreamReader(System.in));
    //     String str = in.readLine();
    //     int NUM_ROWS = parseRowCount(str);
    //     int NUM_COLS   = parseColumnCount(str);
    //     int K = parseDistance(str);
    //     char[][] matrix = parseMatrix(NUM_ROWS, NUM_COLS, in);
    //     System.out.println(countAnts(matrix, NUM_ROWS, NUM_COLS, K));
    // }

    public static void main(String [] args) throws Exception {
        String[] files = { "world.txt", "world2.txt", "world3.txt", "world4.txt", "world5.txt", "world6.txt", "world7.txt", "world8.txt", "world9.txt", "world10.txt",  "world11.txt", "world13.txt", "world14.txt" };
        int[] expectedResults = {1, 2, 1, 3, 1, 135, 120, 117, 124, 0, 2, 2, 1};

        // String[] files = {"world7.txt", "world14.txt"};
        // int[] expectedResults = {120, 1};

        // String[] files = { "world.txt", "world2.txt", "world3.txt", "world4.txt", "world5.txt", "world6.txt", "world10.txt", "world11.txt", "world13.txt" };
        // int[] expectedResults = {1, 2, 1, 3, 1, 135, 0, 2, 2};
        
        for (int i = 0; i < files.length; i++) {
            BufferedReader in = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(new File(files[i]))));
            String str = in.readLine();
            int NUM_ROWS = parseRowCount(str);
            int NUM_COLS   = parseColumnCount(str);
            int K = parseDistance(str);
            char[][] matrix = parseMatrix(NUM_ROWS, NUM_COLS, in);
            //            System.out.println(dumpMatrix(matrix));
            int antCount = countAnts(matrix, NUM_ROWS, NUM_COLS, K);
            System.out.println(files[i] + ": count: " + antCount + ", expected: " + expectedResults[i]);
            assert antCount == expectedResults[i];
        }
    }
                           
    public static String dumpMatrix(char[][] matrix) {
        StringBuilder out = new StringBuilder();
        out.append("[");
        int n = matrix.length;
        int m = matrix[0].length;
        for (int i = 0; i < n; i++) {
            if (i > 0) {
                out.append(" ");
            }
						
            for (int j = 0; j < m; j++) {
                out.append(matrix[i][j]);
                if (j < m-1) {
                    out.append("\t");
                }
            }
            if (i < n-1) {
                out.append("\n");
            }
            else {
                out.append("]");
            }
        }

        return out.toString();
    }

    private static <T extends Object> String dumpList(List<T> l) {
        StringBuilder buf = new StringBuilder("[");
        int i = 0;
        for (T e : l) {
            buf.append(e.toString());
            if (i != l.size() - 1) {
                buf.append(" ");
            }
            i++;
        }
        buf.append("]");
        return buf.toString();
    }
}
