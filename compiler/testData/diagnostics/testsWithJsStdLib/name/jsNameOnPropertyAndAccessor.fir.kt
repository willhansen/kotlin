package foo

class A {
    @JsName("x_") konst x: Int
        <!JS_NAME_ON_ACCESSOR_AND_PROPERTY!>@JsName("get_x")<!> get() = 23

    @JsName("y_") konst y = 0

    @JsName("m_") var m: Int
        <!JS_NAME_ON_ACCESSOR_AND_PROPERTY!>@JsName("get_m")<!> get() = 23
        <!JS_NAME_ON_ACCESSOR_AND_PROPERTY!>@JsName("set_m")<!> set(konstue) {}
}

@JsName("xx_") konst xx: Int
    <!JS_NAME_ON_ACCESSOR_AND_PROPERTY!>@JsName("get_xx")<!> get() = 23

@JsName("yy_") konst yy = 0

@JsName("mm_") var mm: Int
    <!JS_NAME_ON_ACCESSOR_AND_PROPERTY!>@JsName("get_mm")<!> get() = 23
    <!JS_NAME_ON_ACCESSOR_AND_PROPERTY!>@JsName("set_mm")<!> set(konstue) {}
