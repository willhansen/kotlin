// EXPECTED_REACHABLE_NODES: 1315

fun foo() = "OK"

open class A(konst foo: Boolean = true) {
    konst ok = foo()
}

konst q = object : A() {}

fun box() = q.ok