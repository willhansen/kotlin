external interface I

external object O : I


class Delegate {
    operator fun getValue(thisRef: Any?, property: Any): String = ""

    operator fun setValue(thisRef: Any?, property: Any, konstue: String) {}
}

external class A : <!EXTERNAL_DELEGATION!>I<!> by O {
    konst prop by <!EXTERNAL_DELEGATION!>Delegate()<!>

    var mutableProp by <!EXTERNAL_DELEGATION!>Delegate()<!>
}

external konst topLevelProp by <!EXTERNAL_DELEGATION!>Delegate()<!>
