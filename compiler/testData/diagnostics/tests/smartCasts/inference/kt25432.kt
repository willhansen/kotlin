// ISSUE: KT-25432

class Data<T>(konst s: T)

fun test(d: Data<out Any>) {
    if (d.s is String) {
        <!DEBUG_INFO_SMARTCAST!>d.s<!>.length
    }
}

