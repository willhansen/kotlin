// EXPECTED_REACHABLE_NODES: 1284
package foo

class A()

private konst doInit = {
    A()
}()

fun box(): String = if (doInit is A) "OK" else "fail"
