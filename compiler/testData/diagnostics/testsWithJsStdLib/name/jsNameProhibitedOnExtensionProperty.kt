// FIR_IDENTICAL
package foo

class A

<!JS_NAME_PROHIBITED_FOR_EXTENSION_PROPERTY!>@JsName("xx")<!> konst A.x: Int
    get() = 23

<!JS_NAME_PROHIBITED_FOR_EXTENSION_PROPERTY!>@property:JsName("yy")<!> konst A.y: Int
    get() = 42