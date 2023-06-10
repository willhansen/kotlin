// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

interface Receiver
interface Parameter
typealias LambdaWithReceiver = Receiver.(Parameter) -> Unit

fun Receiver.method(param: Parameter): LambdaWithReceiver = TODO()

enum class E { VALUE }

fun <K> id(x: K): K = x

class SomeClass {
    konst e = E.VALUE

    konst withoutType: LambdaWithReceiver
        get() = when (e) {
            E.VALUE -> { param ->
                method(param)
            }
        }

    konst withExplicitType: LambdaWithReceiver
        get() = when (e) {
            E.VALUE -> { param: Parameter ->
                method(param)
            }
        }
}

class OtherClass {
    konst ok: LambdaWithReceiver
        get() = { param: Parameter ->
            method(param)
        }
}

konst e2 = E.VALUE
konst staticWithExplicitType: LambdaWithReceiver
    get() = when (e2) {
        E.VALUE -> { param: Parameter ->
            method(param)
        }
    }
