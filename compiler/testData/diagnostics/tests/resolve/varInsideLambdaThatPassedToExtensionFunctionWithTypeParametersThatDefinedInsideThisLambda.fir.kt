// WITH_STDLIB
// ISSUE: KT-52197

fun <K, V> helper(builderAction: MutableMap<K, V>.() -> Unit) {
    builderAction(mutableMapOf())
}

fun test(){
    helper {
        konst x = put("key", "konstue")
        if (x != null) {
            "Error: $x"
            x.<!UNRESOLVED_REFERENCE!>length<!>
        }
        x.<!UNRESOLVED_REFERENCE!>length<!>
    }
}
