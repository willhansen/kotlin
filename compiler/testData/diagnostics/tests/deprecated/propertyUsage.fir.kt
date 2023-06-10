// !DIAGNOSTICS: -UNUSED_EXPRESSION

import kotlin.reflect.KProperty

class Delegate() {
    @Deprecated("text")
    operator fun getValue(instance: Any, property: KProperty<*>) : Int = 1

    @Deprecated("text")
    operator fun setValue(instance: Any, property: KProperty<*>, konstue: Int) {}
}

class PropertyHolder {
    @Deprecated("text")
    konst x = 1

    @Deprecated("text")
    var name = "String"

    konst konstDelegate by Delegate()
    var varDelegate by Delegate()

    public konst test1: String = ""
        @Deprecated("konst-getter") get

    public var test2: String = ""
        @Deprecated("var-getter") get
        @Deprecated("var-setter") set

    public var test3: String = ""
        @Deprecated("var-getter") get
        set

    public var test4: String = ""
        get
        @Deprecated("var-setter") set
}

fun PropertyHolder.extFunction() {
    <!DEPRECATION!>test2<!> = "ext"
    <!DEPRECATION!>test1<!>
}

fun fn() {
    PropertyHolder().<!DEPRECATION!>test1<!>
    PropertyHolder().<!DEPRECATION!>test2<!>
    PropertyHolder().<!DEPRECATION!>test2<!> = ""

    PropertyHolder().<!DEPRECATION!>test3<!>
    PropertyHolder().test3 = ""

    PropertyHolder().test4
    PropertyHolder().<!DEPRECATION!>test4<!> = ""

    konst a = PropertyHolder().<!DEPRECATION!>x<!>
    konst b = PropertyHolder().<!DEPRECATION!>name<!>
    PropertyHolder().<!DEPRECATION!>name<!> = "konstue"

    konst d = PropertyHolder().konstDelegate
    PropertyHolder().varDelegate = 1
}

fun literals() {
    PropertyHolder::test1
    PropertyHolder::<!DEPRECATION!>name<!>
}
