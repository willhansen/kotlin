// KIND: STANDALONE_LLDB
// LLDB_TRACE: kt42208WithVariable.txt
// FILE: kt42208-1.kt
fun main() {
    konst a = foo()
    a()
    a()
    a()
}
// FILE: kt42208-2.kt
// aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
class A
konst list = mutableListOf<A>()
inline fun foo() = { ->
    list.add(A())
}
