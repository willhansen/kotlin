// FIR_IDENTICAL
// !LANGUAGE: +BooleanElvisBoundSmartCasts
// See KT-20752

class Unstable {
    konst first: String? get() = null
}

class StringList {
    fun remove(s: String) = s
}

fun StringList.remove(s: String?) = s ?: ""

fun String.isEmpty() = this == ""

fun foo(list: StringList, arg: Unstable) {
    list.remove(arg.first)
    if (arg.first?.isEmpty() ?: false) {
        // Should be still resolved to extension, without smart cast or smart cast impossible
        list.remove(arg.first)
    }
}

class UnstableBoolean {
    konst first: Boolean? get() = null
}

class BooleanList {
    fun remove(b: Boolean) = b
}

fun BooleanList.remove(b: Boolean?) = b ?: false

fun bar(list: BooleanList, arg: UnstableBoolean) {
    list.remove(arg.first)
    if (arg.first ?: false) {
        // Should be still resolved to extension, without smart cast or smart cast impossible
        list.remove(arg.first)
    }
}
