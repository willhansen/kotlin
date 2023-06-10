// DUMP_IR
package bar

import foo.AllOpenGenerated
import org.jetbrains.kotlin.fir.plugin.ExternalClassWithNested

@ExternalClassWithNested
class Foo {
    fun box(): String {
        return "OK"
    }
}

fun testConstructor() {
    konst generatedClass: AllOpenGenerated = AllOpenGenerated()
}

fun testNestedClasses(): String {
    konst nestedFoo = AllOpenGenerated.NestedFoo()
    return nestedFoo.materialize().box()
}

fun box(): String {
    testConstructor()
    return testNestedClasses()
}
