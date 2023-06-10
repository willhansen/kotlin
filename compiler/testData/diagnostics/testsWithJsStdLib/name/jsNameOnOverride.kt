// FIR_IDENTICAL
package foo

open class A {
    @JsName("foo_") open fun foo() = 23

    @JsName("bar_") open konst bar = 123

    open konst baz: Int
        @JsName("getBaz_") get() = 55
}

class B : A() {
    <!JS_NAME_PROHIBITED_FOR_OVERRIDE!>@JsName("foo__")<!> override fun foo() = 42

    <!JS_NAME_PROHIBITED_FOR_OVERRIDE!>@JsName("bar__")<!> override konst bar = 142

    override konst baz: Int
        <!JS_NAME_PROHIBITED_FOR_OVERRIDE!>@JsName("getBaz__")<!> get() = 155
}