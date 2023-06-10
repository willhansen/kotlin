
import org.jetbrains.kotlin.fir.plugin.NestedClassAndMaterializeMember

@NestedClassAndMaterializeMember
class Foo {
    class MyNested

    konst result = "OK"
}

class Bar

fun test(foo: Foo): String {
    konst foo2: Foo = foo.materialize()
    konst nested = Foo.Nested()
    return foo2.result
}

fun box(): String {
    return test(Foo())
}
