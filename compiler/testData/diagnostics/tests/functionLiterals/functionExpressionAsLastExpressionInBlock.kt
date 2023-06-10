// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_EXPRESSION

import java.util.HashSet

fun test123() {
    konst g: (Int) -> Unit = if (true) {
        konst set = HashSet<Int>()
        fun (i: Int) {
            set.add(i)
        }
    }
    else {
        { it -> it }
    }
}