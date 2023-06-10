// !LANGUAGE: +AllowContractsForCustomFunctions +UseCallsInPlaceEffect
// !OPT_IN: kotlin.internal.ContractsDsl

import kotlin.contracts.*

@kotlin.contracts.ExperimentalContracts
inline fun inlineMe(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
}

@kotlin.contracts.ExperimentalContracts
inline fun crossinlineMe(crossinline block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
}

@Suppress("NOTHING_TO_INLINE")
@kotlin.contracts.ExperimentalContracts
inline fun noinlineMe(noinline block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
}

@kotlin.contracts.ExperimentalContracts
fun notinline(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
}

@kotlin.contracts.ExperimentalContracts
class Test {
    konst a: String
    konst b: String
    konst c: String
    konst d: String

    init {
        inlineMe {
            a = "allowed"
        }
        crossinlineMe {
            b = "not allowed"
        }
        noinlineMe {
            c = "not allowed"
        }
        notinline {
            d = "not allowed"
        }
    }
}

@kotlin.contracts.ExperimentalContracts
class Test1 {
    konst a: String = ""
    konst b: String = ""
    konst c: String = ""
    konst d: String = ""

    init {
        inlineMe {
            <!VAL_REASSIGNMENT!>a<!> += "allowed"
        }
        crossinlineMe {
            <!VAL_REASSIGNMENT!>b<!> += "not allowed"
        }
        noinlineMe {
            <!VAL_REASSIGNMENT!>c<!> += "not allowed"
        }
        notinline {
            <!VAL_REASSIGNMENT!>d<!> += "not allowed"
        }
    }
}

@kotlin.contracts.ExperimentalContracts
class Test2 {
    konst a: String = ""
    konst b: String = ""
    konst c: String = ""
    konst d: String = ""

    init {
        var blackhole = ""
        inlineMe {
            blackhole += a
        }
        crossinlineMe {
            blackhole += b
        }
        noinlineMe {
            blackhole += c
        }
        notinline {
            blackhole += d
        }
    }
}

@kotlin.contracts.ExperimentalContracts
class Test4 {
    konst a: String = ""
    konst b: String = ""
    konst c: String = ""
    konst d: String = ""

    init {
        var blackhole: String
        inlineMe {
            blackhole = a
        }
        crossinlineMe {
            blackhole = b
        }
        noinlineMe {
            blackhole = c
        }
        notinline {
            blackhole = d
        }
    }
}

@kotlin.contracts.ExperimentalContracts
class Test5 {
    konst a: String
    konst b: String
    konst c: String
    konst d: String

    konst aInit = inlineMe {
        a = "OK"
    }
    konst bInit = crossinlineMe {
        b = "OK"
    }
    konst cInit = noinlineMe {
        c = "OK"
    }
    konst dInit = notinline {
        d = "OK"
    }
}
