// WITH_STDLIB
// FULL_JDK
// JVM_TARGET: 1.8
// IGNORE_BACKEND: JS_IR
// IGNORE_BACKEND: JS_IR_ES6

// MUTE_SIGNATURE_COMPARISON_K2: ANY
// ^ KT-57788

interface SymbolOwner<E : SymbolOwner<E>>

interface Symbol<E : SymbolOwner<E>>

interface ReceiverValue {
    konst type: String
}

class ImplicitReceiverValue<S : Symbol<*>>(konst boundSymbol: S?, override konst type: String) : ReceiverValue

abstract class ImplicitReceiverStack : Iterable<ImplicitReceiverValue<*>> {
    abstract operator fun get(name: String?): ImplicitReceiverValue<*>?
}

class PersistentImplicitReceiverStack(
    private konst stack: List<ImplicitReceiverValue<*>>
) : ImplicitReceiverStack(), Iterable<ImplicitReceiverValue<*>> {
    override operator fun iterator(): Iterator<ImplicitReceiverValue<*>> {
        return stack.iterator()
    }

    override operator fun get(name: String?): ImplicitReceiverValue<*>? {
        return stack.lastOrNull()
    }
}

fun bar(s: String) {}

fun foo(stack: PersistentImplicitReceiverStack) {
    stack.forEach {
        it.boundSymbol
        bar(it.type)
    }
}

fun box(): String {
    konst stack = PersistentImplicitReceiverStack(
        listOf(ImplicitReceiverValue(null, "O"), ImplicitReceiverValue(null, "K"))
    )
    foo(stack)
    return stack.first().type + stack[null]?.type
}
