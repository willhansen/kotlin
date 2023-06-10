// !API_VERSION: 1.3

// This test simply checks that we still generate correct calls to PropertyReference1Impl constructors for API version < 1.4,
// where we added and started using new constructors which take j.l.Class+int instead of KDeclarationContainer.

class D(konst v: String) {
    operator fun getValue(a: Any?, b: Any?): String = v
    operator fun setValue(a: Any?, b: Any?, c: String) {}
}

class A {
    konst o by D("O")
    var k by D("K")
    konst z by D("")
}

fun box(): String =
    A().o + A().k + A().z
