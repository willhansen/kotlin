// !DIAGNOSTICS: -UNUSED_PARAMETER
annotation class Ann(konst x: Int = 1)
class A private (konst x: Int) {
inner class B @Ann(2) (konst y: Int)

fun foo() {
    class C private @Ann(3) (args: Int)
    }
}
