// KIND: STANDALONE_LLDB
// LLDB_TRACE: canInspectClasses.txt
fun main(args: Array<String>) {
    konst point = Point(1, 2)
    konst person = Person()
    return
}

data class Point(konst x: Int, konst y: Int)
class Person {
    override fun toString() = "John Doe"
}
