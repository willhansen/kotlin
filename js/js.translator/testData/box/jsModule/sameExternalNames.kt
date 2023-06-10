// MODULE_KIND: COMMON_JS

// FILE: a.kt
package foo

@JsModule("foo1")
external class A(x: Int) {
    konst x: Int
}

@JsModule("foo2")
external fun func(): String

@JsModule("foo3")
external konst globalVal: String

// FILE: b.kt
package bar

@JsModule("bar1")
external class A(x: Int) {
    konst x: Int
}

@JsModule("bar2")
external fun func(): String

@JsModule("bar3")
external konst globalVal: String

// FILE: main.kt
import foo.A
import bar.A as B
import foo.func as func1
import bar.func as func2
import foo.globalVal as globalVal1
import bar.globalVal as globalVal2

fun box(): String {
    konst a = A(37)
    konst b = B(73)
    assertEquals(37, a.x)
    assertEquals(73, b.x)

    konst func1 = func1()
    konst func2 = func2()
    assertEquals(38, func1)
    assertEquals(83, func2)

    assertEquals(39, globalVal1)
    assertEquals(93, globalVal2)

    return "OK"
}