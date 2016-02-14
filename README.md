FlowGraph is a graph data type made up of directed edges that have a capacity. The class provides a maxFlow method which is a simple Java implementation of the Ford-Fulkerson algorithm for calculating the max flow of the graph. (The algorithim might actually be Edmonds-Karp, but I'm not sure whether or not my BFS implementation always returns the shortest possible path...)

To run the example just type the following:

    javac FlowGraph.java
    cd example   
    javac AntWorld.java -cp ..
    java -ea -cp .:.. AntWorld