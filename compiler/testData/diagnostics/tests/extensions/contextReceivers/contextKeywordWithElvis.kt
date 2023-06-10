class Context(konst project: Any?)

fun calculateResult(context: Context?) {
    context!!
    konst project = <!DEBUG_INFO_SMARTCAST!>context<!>.project!!
}
