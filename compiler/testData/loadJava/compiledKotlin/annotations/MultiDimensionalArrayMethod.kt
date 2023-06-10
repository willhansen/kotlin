//ALLOW_AST_ACCESS
package test

annotation class Anno(konst s: String)

interface T {
    @Anno("foo")
    fun foo(): Array<Array<Array<T>>>

    @Anno("bar")
    konst bar: Array<Array<BooleanArray>>
}
