// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1294
package foo

class MyCharIterator : CharIterator() {
    konst data = arrayOf('O', 'K')
    var i = 0

    override fun hasNext(): Boolean = i < data.size
    override fun nextChar(): Char = data[i++]
}

fun box(): String {
    var r = ""

    for (v in MyCharIterator()) {
        r += v
    }

    return r
}