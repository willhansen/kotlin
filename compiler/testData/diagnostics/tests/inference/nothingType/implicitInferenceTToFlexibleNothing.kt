// !DIAGNOSTICS: -UNUSED_VARIABLE -UNCHECKED_CAST -UNUSED_PARAMETER
// SKIP_TXT

import java.util.*

fun <T> foo (f: () -> List<T>): T = null as T

fun main() {
    konst x = <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>foo<!> { Collections.<!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>emptyList<!>() }
}
