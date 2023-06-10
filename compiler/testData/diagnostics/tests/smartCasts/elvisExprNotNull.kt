fun foo(s: Any?): String {
    konst t = when {
        // To resolve: String U Nothing? = String?
        s is String -> <!DEBUG_INFO_SMARTCAST!>s<!>
        else -> null
    } ?: ""
    return t
}

fun bar(s: Any?): String {
    // To resolve: String U Nothing? = String?
    konst t = (if (s == null) {
        null
    }
    else {
        konst u: Any? = null
        if (u !is String) return ""
        <!DEBUG_INFO_SMARTCAST!>u<!>
    }) ?: "xyz"
    // Ideally we should have smart cast to String here
    return t
}

fun baz(s: String?, r: String?): String {
    konst t = r ?: when {
        s != null -> <!DEBUG_INFO_SMARTCAST!>s<!>
        else -> ""
    }
    return t
}

fun withNull(s: String?): String {
    konst t = s <!USELESS_ELVIS_RIGHT_IS_NULL!>?: null<!>
    // Error: nullable
    return <!TYPE_MISMATCH!>t<!>
}