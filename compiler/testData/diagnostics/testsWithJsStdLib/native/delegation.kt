external interface I

external object O : I


class Delegate {
    operator fun getValue(thisRef: Any?, property: Any): String = ""

    operator fun setValue(thisRef: Any?, property: Any, konstue: String) {}
}

external class A : <!EXTERNAL_DELEGATION!>I by O<!> {
    konst prop <!EXTERNAL_DELEGATION!>by Delegate()<!>

    var mutableProp <!EXTERNAL_DELEGATION!>by Delegate()<!>
}

external konst topLevelProp <!EXTERNAL_DELEGATION!>by Delegate()<!>