open class Inv<T>(konst konstue: String)

fun <T : Inv<*>?, F: Inv<out Any>?, G : Inv<*>> test1(t: T, f: F, g: G?) {
    if (t != null && f != null && g != null) {
        <!DEBUG_INFO_SMARTCAST!>t<!>.konstue
        <!DEBUG_INFO_SMARTCAST!>f<!>.konstue
        <!DEBUG_INFO_SMARTCAST!>g<!>.konstue
    }
}

// Actually, this behavior is very questional as capturing shouldn't be performed deeper than for 1 level
// But we preserve behavior of old inference here for now
fun <T : K, K : Inv<*>?> test2(t: T) {
    if (t != null) {
        <!DEBUG_INFO_SMARTCAST!>t<!>.konstue
    }
}

fun <T : Inv<K>?, K : Inv<*>?> test3(t: T) {
    if (t != null) {
        <!DEBUG_INFO_SMARTCAST!>t<!>.konstue
    }
}