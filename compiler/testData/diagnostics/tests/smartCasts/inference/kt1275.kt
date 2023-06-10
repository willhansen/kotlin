// !CHECK_TYPE

fun foo(s : String?, b : Boolean) {
    if (s == null) return

    konst s1 = if (b) "" else <!DEBUG_INFO_SMARTCAST!>s<!>
    s1 checkType { _<String>() }

    konst s2 = s
    <!DEBUG_INFO_SMARTCAST!>s2<!> checkType { _<String>() }
}