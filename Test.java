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
class Test {
    public static void main(String [] args) throws Exception {
        String[] files = { "world.txt", "world2.txt", "world3.txt", "world4.txt", "world5.txt", "world6.txt", "world7.txt", "world8.txt", "world9.txt", "world10.txt",  "world11.txt", "world13.txt", "world14.txt" };
        int[] expectedResults = {1, 2, 1, 3, 1, 135, 120, 117, 124, 0, 2, 2, 1};      
        for (int i = 0; i < files.length; i++) {
            World world = parseWorld(files[i]);
            
            int antCount = countAnts(world);
            System.out.println(files[i] + ": count: " + antCount + ", expected: " + expectedResults[i]);
            assert antCount == expectedResults[i];
        }
    }

    private static World parseWorld(String file) throws Exception {
        BufferedReader in = new BufferedReader(
            new InputStreamReader(
                new FileInputStream(new File(file))));
        String firstLine = in.readLine();
        int numRows = parseRowCount(firstLine);
        int numCols = parseColumnCount(firstLine);
        int maxDist = parseDistance(firstLine);
        return new World(numRows, numCols, maxDist, parseMatrix(numRows, numCols, in));
    }

    public static int countAnts(World world) {
        List<Workplace> workplaces = new ArrayList<>();
        for (int row = 0; row < world.NUM_ROWS; row++) {
            for (int col = 0; col < world.NUM_COLS; col++) {
                if (world.matrix[row][col] == WORKPLACE) {
                    Point start = new Point(row, col);
                    workplaces.add(search(world, start));
                }
            }
        }

        // construct a flow graph in such a way that calculating the max flow
        // calculates the number of ants we can allocate.
        FlowGraph<Point> g = new FlowGraph<>();

        // doesn't matter what our source and sink are, as long as they are unique
        // in the graph and can be referenced later.
        Point flowSource = new Point(-1, -1);
        Point flowSink = new Point(-2, -2);
        
        for (Workplace workplace : workplaces) {
            // create edges for all the potential {fruit, meat, workplace} paths we found
            // and add them to the graph
            for (Point fruit : workplace.fruit) {
                addEdge(g, flowSource, flowSink, flowSource, fruit);
                addEdge(g, flowSource, flowSink, fruit, workplace.workplace);
            }
            
            for (Point meat : workplace.meat) {
                addEdge(g, flowSource, flowSink, workplace.workplace, meat);
                addEdge(g, flowSource, flowSink, meat, flowSink);
            }
        }
        return g.maxFlow(flowSource, flowSink);
    }

    /**
     * Find all the F and M that can be reached from +workplace+
     */
    public static Workplace search(World world, Point start) {
        Workplace workplace = new Workplace(start);

        // Use BFS to find all the fruit and meat that can be reached from 
        // the given W.
        Set<Point> visited = new HashSet<>();

        Deque<Point> curQ = new LinkedList<>();
        Deque<Point> nextQ = new LinkedList<>();
        int curDist = 1;
        
        curQ.addFirst(workplace.workplace);
        visited.add(workplace.workplace);

        do {
            Point p = curQ.removeLast();
            Point[] neighbors = new Point[] {new Point(p.row-1, p.col),
                                             new Point(p.row, p.col-1),
                                             new Point(p.row, p.col+1),
                                             new Point(p.row+1, p.col)};
            for (Point next : neighbors) {
                if (next.row >= 0 && next.row < world.NUM_ROWS && next.col >= 0 && next.col < world.NUM_COLS) {
                    char item = world.matrix[next.row][next.col];
                    if (item == MEAT) {
                        workplace.meat.add(next);
                    }
                    else if (item == FRUIT) {
                        workplace.fruit.add(next);
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
        } while (curDist <= world.MAX_DIST && !curQ.isEmpty());
        
        return workplace;
    }

    /**
     * Simulate node capacity by replacing each node (besides our ultimate source and sink)
     * with a pair of nodes that have a single edge between them with a capacity of 1.
     */
    private static void addEdge(FlowGraph<Point> g, Point flowSource, Point flowSink, Point newSource, Point newSink) {
        Point newSourceIn = new Point(newSource.row, ~newSource.col);
        Point newSourceOut = new Point(~newSource.row, newSource.col);

        Point newSinkIn = new Point(newSink.row, ~newSink.col);
        Point newSinkOut = new Point(~newSink.row, newSink.col);

        if (newSource.equals(flowSource) && newSink.equals(flowSink)) {
            g.addEdge(newSource, newSink, 1);
        }
        else if (newSource.equals(flowSource)) {
            g.addEdge(newSource, newSinkIn, 1);
            g.addEdge(newSinkIn, newSinkOut, 1);
        }
        else if (newSink.equals(flowSink)) {
            g.addEdge(newSourceIn, newSourceOut, 1);
            g.addEdge(newSourceOut, newSink, 1);
        }
        else {
            g.addEdge(newSourceIn, newSourceOut, 1);
            g.addEdge(newSourceOut, newSinkIn, 1);
            g.addEdge(newSinkIn, newSinkOut, 1);
        }
    }


    /**
     * Data model for max flow problem.
     */
     public static class Point {
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

    /**
     * Stores the coordinates of a W in the world and the coordinates of all the F and M
     * reachable from it.
     */
    private static class Workplace {
        public final Point workplace;
        public final Set<Point> fruit;
        public final Set<Point> meat;
        public Workplace(Point workplace) {
            this.workplace = workplace;
            fruit = new HashSet<>();
            meat = new HashSet<>();
        }
    }

    /**
     * Data model for ant world and code to read and construct it from input.
     */
    private static final char GRASS = '.';
    private static final char ROCKS = 'X';
    private static final char FRUIT = 'F';
    private static final char MEAT  = 'M';
    private static final char WORKPLACE  = 'W';
    
    private static class World {
        public final int NUM_ROWS;
        public final int NUM_COLS;
        public final int MAX_DIST;
        public final char[][] matrix;
        public World(int numRows, int numCols, int maxDist, char[][] matrix) {
            NUM_ROWS = numRows;
            NUM_COLS = numCols;
            MAX_DIST = maxDist;
            this.matrix = matrix;
        }
    }

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
}
