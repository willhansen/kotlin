// EXPECTED_REACHABLE_NODES: 1373
object Foo {
    konst konstue = "O"
}
class Bar(konst anotherValue: String)

konst Foo.prop: String
    get() = konstue

konst Bar.prop: String
    get() = anotherValue

fun box() = Foo.prop + Bar("K").prop