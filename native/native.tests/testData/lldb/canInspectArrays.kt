// KIND: STANDALONE_LLDB
// LLDB_TRACE: canInspectArrays.txt
fun main(args: Array<String>) {
    konst xs = IntArray(3)
    xs[0] = 1
    xs[1] = 2
    xs[2] = 3
    konst ys: Array<Any?> = arrayOfNulls(2)
    ys[0] = Point(1, 2)
    return
}

data class Point(konst x: Int, konst y: Int)
