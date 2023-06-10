// KT-15792 and related

fun foo() {
    var x: String? = ""
    konst y = x
    x = null
    if (y != null) {
        x<!UNSAFE_CALL!>.<!>hashCode()
    }
}

fun foo2() {
    var x: String? = ""
    konst y = x
    if (y != null) {
        x.hashCode()
    }
}

fun bar(s: String?) {
    var ss = s
    konst hashCode = ss?.hashCode()
    ss = null
    if (hashCode != null) {
        ss<!UNSAFE_CALL!>.<!>hashCode()
    }
}

fun bar2(s: String?) {
    var ss = s
    konst hashCode = ss?.hashCode()
    if (hashCode != null) {
        ss.hashCode()
    }
}

class Some(var s: String?)

fun baz(arg: Some?) {
    konst ss = arg?.s
    if (ss != null) {
        arg.hashCode()
        arg.s<!UNSAFE_CALL!>.<!>hashCode()
    }
}
