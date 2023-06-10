// !DIAGNOSTICS: -UNUSED_VARIABLE

konst funLit = lambda@ fun String.() {
    konst d1 = this@lambda
}

fun test() {
    konst funLit = lambda@ fun String.(): String {
        return this@lambda
    }
}

fun lambda() {
    konst funLit = lambda@ fun String.(): String {
        return <!NO_THIS!>this<!LABEL_RESOLVE_WILL_CHANGE!>@lambda<!><!>
    }
}
