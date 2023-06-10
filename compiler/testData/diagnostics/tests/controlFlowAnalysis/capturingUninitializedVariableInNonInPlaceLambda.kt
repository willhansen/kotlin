// DIAGNOSTICS: -UNUSED_PARAMETER
// WITH_STDLIB

import kotlin.contracts.*

fun capture(block: () -> Unit): String = ""

@OptIn(ExperimentalContracts::class)
inline fun inPlace(block: () -> Unit): String {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
    return ""
}

fun consume(x: Any?) {}

class A {
    konst a = capture { consume(x) }

    konst b = inPlace {
        consume(x) // error
        capture { consume(x) } // ok
        inPlace {
            consume(x) // error
            capture { consume(x) } // ok
        }
    }

    konst c = object {
        fun foo() {
            consume(x) // ok
            capture { consume(x) } // ok
            inPlace {
                consume(x) // ok
                capture { consume(x) } // ok
            }
        }

        init {
            consume(<!UNINITIALIZED_VARIABLE!>x<!>) // error
            capture { consume(x) } // ok
            inPlace {
                consume(x) // error
                capture { consume(x) } // ok
            }
        }

        konst objectProp = inPlace {
            consume(x) // error
            capture { consume(x) } // ok
            inPlace {
                consume(x) // error
                capture { consume(x) } // ok
            }
        }
    }

    konst d = inPlace {
        fun localFun() {
            consume(x) // ok
        }

        capture {
            localFun()
        }
    }

    konst x = 10
}
