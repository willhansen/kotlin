// FIR_IDENTICAL
// ISSUE: KT-57968

class Some(konst child: Some?)

konst Some.foo get(): Int =
    if ((child?.foo ?: 0) > 1) {
        0
    } else {
        1
    }
