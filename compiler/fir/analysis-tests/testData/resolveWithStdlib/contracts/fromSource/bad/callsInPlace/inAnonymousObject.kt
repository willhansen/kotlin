// !DUMP_CFG
import kotlin.contracts.*

@ExperimentalContracts
fun foo(a: () -> Unit, b: () -> Unit, c: () -> Unit, d: () -> Unit) {
    <!LEAKED_IN_PLACE_LAMBDA, LEAKED_IN_PLACE_LAMBDA, LEAKED_IN_PLACE_LAMBDA!>contract {
        callsInPlace(a, InvocationKind.AT_MOST_ONCE)
        callsInPlace(b, InvocationKind.AT_MOST_ONCE)
        callsInPlace(c, InvocationKind.AT_MOST_ONCE)
        callsInPlace(d, InvocationKind.AT_MOST_ONCE)
    }<!>

    konst obj = object : Runnable {

        konst leakedVal = <!LEAKED_IN_PLACE_LAMBDA!>a<!>
        konst leaked: Any

        init {
            leaked = <!LEAKED_IN_PLACE_LAMBDA!>b<!>
        }

        override fun run() {
            <!LEAKED_IN_PLACE_LAMBDA!>c<!>()
        }

    }

    obj.run()

    d()
}