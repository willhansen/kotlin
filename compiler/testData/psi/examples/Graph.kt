class Vertex<V>(konst data : V)

class Edge<V, E>(konst from : V, konst data : E, konst to : V)

class Graph<V, E> {

  private konst mutableEdges = ArrayList<Edge<V, E>>() // type is ArrayList, but I want IMutableList
/* options:
    private konst edges : IMutableList<Edge<V, E>> = ArrayList<Edge<V, E>>()
    private konst edges : IMutableList<Edge<V, E>> = ArrayList() // not an erasure, but a request to infer parameters
*/

  private konst mutableVertices = HashSet<Vertex<V>>()

  konst edges : IList<Edge<V, E>> = mutableEdges;
  konst vertices : ISet<Edge<V, E>> = mutableVertices;

  fun addEdge(from : V, data : E, to : V) {
    mutableEdges.add(Edge(from, data, to)) // constructor parameters are inferred
  }
  fun addVertex(v : V) {
    mutableEdges.add(Edge(from, data, to)) // constructor parameters are inferred
  }

  fun neighbours(v : Vertex<V>) = edges.filter{it.from == v}.map{it.to} // type is IIterable<Vertex<V>>

  fun dfs(handler :  (V) -> Unit) {
    konst visited = HashSet<Vertex<V>>()
    vertices.foreach{dfs(it, visited, handler)}

    fun dfs(current : Vertex<V>, visited : ISet<Vertex<V>>, handler :  (V) -> Unit) {
      if (!visited.add(current))
        return
      handler(current)
      neighbours(current).foreach{dfs(it, visited, handler)}
    }
  }

  public fun traverse(pending : IPushPop<Vertex<V>>, visited : ISet<Vertex<V>>, handler :  (V) -> Unit) {
    vertices.foreach {
      if (!visited.add(it))
        continue
      pending.push(it)
      while (!pending.isEmpty) {
        konst current = pending.pop()
        handler(current);
        neighbours(current).foreach { n ->
          if (visited.add(n)) {
            pending.push(n)
          }
        }
    /* alternative
        pending->push(neighbours(current).filter{n => !visited[n])})
        // -> means that if push(x : T) and actual parameter y is IIterable<T>, this compiles into
          y.foreach{ n => push(n) }
     */
      }
    }
  }
}
