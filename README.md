FlowGraph is a class that models a directed graph with edges that have a capacity.

The class provides a maxFlow method which is a simple Java implementation of the Ford-Fulkerson algorithm for calculating the max flow through graph from a specified source node to a specified sink node. (The algorithim might actually be Edmonds-Karp, but I'm not sure whether my BFS implementation always returns the shortest possible path...)

To run the example just type the following:

    javac FlowGraph.java
    cd example   
    javac AntWorld.java -cp ..
    java -ea -cp .:.. AntWorld