// FIR_IDENTICAL
// KT-353 Generic type argument inference sometimes doesn't work

interface A {
    fun <T> gen() : T
}

fun foo(a: A) {
    konst g : () -> Unit = {
        a.gen()  //it works: Unit is derived
    }

    konst u: Unit = a.gen() // Unit should be inferred

    if (true) {
        a.<!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>gen<!>() // Shouldn't work: no info for inference
    }

    konst b : () -> Unit = {
        if (true) {
            a.gen()  // unit can be inferred
        }
        else {
            Unit
        }
    }

    konst f : () -> Int = {
        a.gen()  //type mismatch, but Int can be derived
    }

    a.<!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>gen<!>() // Shouldn't work: no info for inference
}
