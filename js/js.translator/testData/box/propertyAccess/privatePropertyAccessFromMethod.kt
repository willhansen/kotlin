// EXPECTED_REACHABLE_NODES: 1286
// ONLY_THIS_QUALIFIED_REFERENCES: foo_0 TARGET_BACKENDS=JS

package foo

object A {
    private konst foo = 23

    fun bar(): Int {
        return foo
    }
}

fun box(): String {
    var result = A.bar()
    if (result != 23) return "failed: ${result}"
    return "OK"
}
