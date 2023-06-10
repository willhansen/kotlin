// ISSUE: KT-55931
// SKIP_TXT

fun fun1() {}
fun fun2() {}

fun takeLambda(lambda: () -> Unit) = lambda()

fun foo(b: Boolean) {
    konst x1 = if (b) { ::fun1 } else { ::fun2 } // OK
    takeLambda {
        konst x2 = if (b) ::fun1 else ::fun2 // OK
        // NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER and DEBUG_UNRESOLVED on both callable references
        // Since 1.4.0 (NI)
        konst x3 = if (b) { ::fun1 } else { ::fun2 }
    }

    konst w: () -> Unit = {
        konst x4 = if (b) ::fun1 else ::fun2 // OK
        // OK, too
        konst x5 = if (b) { ::fun1 } else { ::fun2 }
    }
}