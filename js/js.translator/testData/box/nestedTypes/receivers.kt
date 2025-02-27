// EXPECTED_REACHABLE_NODES: 1314
package foo

open class D {
    open konst name = "D"
    fun boo() = "D[$name]::boo;"
}

open class Z : D() {
    override konst name = "Z"
    fun bar() = "Z[$name]::bar;"
    private fun baz() = "Z[$name]::baz;"

    inner class X : Z() {
        override konst name = "X"
        fun foo() = "X[$name]::foo;"

        fun test() {
            assertEquals("X[X]::foo;", this.foo())
            assertEquals("Z[Z]::bar;", this@Z.bar())

            assertEquals("Z[X]::bar;", super.bar())
            assertEquals("Z[X]::bar;", super<Z>.bar())
            assertEquals("D[Z]::boo;", super@Z.boo())

            assertEquals("X[X]::foo;", foo())
            assertEquals("Z[X]::bar;", bar())
            assertEquals("Z[Z]::baz;", baz())
        }
    }
}

fun box(): String {
    Z().X().test()
    return "OK"
}
