package foo

class A {
    <!JS_NAME_IS_NOT_ON_ALL_ACCESSORS!>var x: Int<!>
        @JsName("get_x") get() = 23
        set(<!UNUSED_PARAMETER!>konstue<!>) {}

    <!JS_NAME_IS_NOT_ON_ALL_ACCESSORS!>var y: Int<!>
        get() = 23
        @JsName("set_y") set(<!UNUSED_PARAMETER!>konstue<!>) {}

    var z: Int
        @JsName("get_z") get() = 23
        @JsName("set_z") set(<!UNUSED_PARAMETER!>konstue<!>) {}
}

<!JS_NAME_IS_NOT_ON_ALL_ACCESSORS!>var xx: Int<!>
    @JsName("get_xx") get() = 23
    set(<!UNUSED_PARAMETER!>konstue<!>) {}

<!JS_NAME_IS_NOT_ON_ALL_ACCESSORS!>var A.ext: Int<!>
    @JsName("get_ext") get() = 23
    set(<!UNUSED_PARAMETER!>konstue<!>) {}