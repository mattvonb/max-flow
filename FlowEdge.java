public class FlowEdge <T> {
    public final T source;
    public final T sink;
    public final int capacity;
    public FlowEdge<T> residualEdge;
        
    public FlowEdge(T source, T sink, int capacity) {
        this.source = source;
        this.sink = sink;
        this.capacity = capacity;
        this.residualEdge = null;
    }
        
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }

        if (!(o instanceof FlowEdge<?>)) {
            return false;
        }
        FlowEdge<?> that = (FlowEdge<?>)o;
            
        return this.source.equals(that.source) && this.sink.equals(that.sink) && this.capacity == that.capacity;
    }

    public int hashCode() {
        int hash = 17 + source.hashCode();
        hash = hash * 31 + sink.hashCode();
        hash = hash * 31 + capacity;
        return hash;
    }

    public String toString() {
        return "[" + source + " " + sink + " " + capacity + "]";
    }
}
