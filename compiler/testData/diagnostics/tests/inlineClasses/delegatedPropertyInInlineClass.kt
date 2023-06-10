// !LANGUAGE: +InlineClasses, -JvmInlineValueClasses

class Val {
    operator fun getValue(thisRef: Any?, kProp: Any?) = 1
}

class Var {
    operator fun getValue(thisRef: Any?, kProp: Any?) = 2
    operator fun setValue(thisRef: Any?, kProp: Any?, konstue: Int) {}
}


object ValObject {
    operator fun getValue(thisRef: Any?, kProp: Any?) = 1
}

object VarObject {
    operator fun getValue(thisRef: Any?, kProp: Any?) = 2
    operator fun setValue(thisRef: Any?, kProp: Any?, konstue: Int) {}
}

inline class Z(konst data: Int) {
    konst testVal <!DELEGATED_PROPERTY_INSIDE_VALUE_CLASS!>by Val()<!>
    var testVar <!DELEGATED_PROPERTY_INSIDE_VALUE_CLASS!>by Var()<!>

    konst testValBySingleton <!DELEGATED_PROPERTY_INSIDE_VALUE_CLASS!>by ValObject<!>
    var testVarBySingleton <!DELEGATED_PROPERTY_INSIDE_VALUE_CLASS!>by VarObject<!>
}