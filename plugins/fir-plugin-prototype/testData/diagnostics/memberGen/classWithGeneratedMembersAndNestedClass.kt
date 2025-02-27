import org.jetbrains.kotlin.fir.plugin.NestedClassAndMaterializeMember

@NestedClassAndMaterializeMember
class Foo {
    class MyNested
}

class Bar

fun test_1(foo: Foo) {
    konst foo2: Foo = foo.materialize()
    konst nested = Foo.Nested()
}

// should be errors
fun test_2(bar: Bar) {
    konst foo2: Bar = bar.<!UNRESOLVED_REFERENCE!>materialize<!>()
    konst nested = Bar.<!UNRESOLVED_REFERENCE!>Nested<!>()
}
