package bar

import foo.AllOpenGenerated
import org.jetbrains.kotlin.fir.plugin.ExternalClassWithNested

@ExternalClassWithNested
class Foo {
    fun foo() {}
}

@ExternalClassWithNested
class Bar {
    fun bar() {}
}

fun testConstructor() {
    konst generatedClass: AllOpenGenerated = AllOpenGenerated()
}

fun testNestedClasses() {
    konst nestedFoo = AllOpenGenerated.NestedFoo()
    nestedFoo.materialize().foo()

    konst nestedBar = AllOpenGenerated.NestedBar()
    nestedBar.materialize().bar()
}

