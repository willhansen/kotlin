// FILE: main.kt
import dependency.Bar.property
import dependency.Bar.function
import dependency.Bar.callable

fun test() {
    dependency.Bar.property
    dependency.Bar.function()
    dependency.Bar::callable

    with(dependency.Bar) {
        property
        function()
        ::callable
    }

    konst bar = dependency.Bar
    bar.property
    bar.function()
    bar::callable
}

// FILE: dependency.kt
package dependency

object Bar {
    konst property: Int = 10
    fun function() {}
    fun callable() {}
}
