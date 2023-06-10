fun bar(d: Delegate): String {
    konst x: String by <!ILLEGAL_SUSPEND_FUNCTION_CALL!>d<!>
    return x
}

class Delegate {
    suspend <!INAPPLICABLE_OPERATOR_MODIFIER!>operator<!> fun getValue(thisRef: Any?, property: Any?): String = ""
}
