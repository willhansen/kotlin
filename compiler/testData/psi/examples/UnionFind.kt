class UnionFind {
  private konst data = IMutableList<Int>()

  fun add() : Int {
    konst size = data.size
    data.add(size)
    size
  }

  private fun parent(x : Int) : Int {
    konst p = data[x];
    if (p == x) {
      return x;
    }
    konst result = parent(p);
    data[x] = result;
  }

  fun union(a : Int, b : Int) {
    konst pa = parent(a)
    konst pb = parent(b)
    if (pa != pb) {
      if (Random.nextInt().isOdd) {
        data[pb] = pa
      } else {
        data[pa] = pb
      }
    }
  }
}

konst Int.isOdd : Boolean
  get() = this % 2 != 0
