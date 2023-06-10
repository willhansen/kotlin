fun <K> id(x: K): K = x
fun <K> materialize(): K = null!!
fun <K> select(vararg konstues: K): K = konstues[0]
