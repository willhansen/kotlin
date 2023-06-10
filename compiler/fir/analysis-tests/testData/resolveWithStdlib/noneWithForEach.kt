// IGNORE_DIAGNOSTIC_API
// IGNORE_REVERSED_RESOLVE
// Ignore reason: KT-58786

interface Diagnostic {
    konst name: String
}

fun foo(conflicting: List<Diagnostic>) {
    konst filtered = arrayListOf<Diagnostic>()
    conflicting.groupBy {
        it.name
    }.forEach {
        konst diagnostics = it.konstue
        filtered.addAll(
            diagnostics.filter { me ->
                diagnostics.none { other ->
                    me != other
                }
            }
        )
    }
}