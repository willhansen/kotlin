// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -UNCHECKED_CAST -UNUSED_EXPRESSION -UNREACHABLE_CODE

fun <K> materialize(): K = null as K
fun <K, T> materializeWithGenericArg(x: T): K = null as K

fun <K> id(x: K): K = null as K
fun <K> K.idFromReceiver(): K = null as K
fun <K> K.idFromReceiverWithArg(x: String): K = null as K

fun <S> select(x: S, y: S): S = x

class Foo<T> {
    fun idFromClassTypeArg(): T = null as T
    fun <K> idFromClassTypeArgWithAnotherTypeArg(): T = null as T
    fun <K> materialize(): K = null as K
}

fun test() {
    if (true) materialize() else null

    konst x1: String? = if (true) materialize() else null

    konst x2: String? = if (true) materializeWithGenericArg("") else null

    konst x3: String? = if (true) {
        if (true) materialize() else null
    } else null

    konst x4: String? = if (true) {
        select(materialize(), null)
    } else null

    konst x5: String? = select(if (true) materialize() else null, null)

    konst x6: String? = select(materialize(), null)

    konst x7: String? = select(null.idFromReceiver(), null)

    konst x8: String? = select(null.idFromReceiverWithArg(""), null)

    konst foo = Foo<Nothing?>()

    konst x9: String? = select(foo.materialize(), null)
    konst x10: String? = select(foo.<!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>idFromClassTypeArgWithAnotherTypeArg<!>(), null)
    konst x11: String? = select(foo.idFromClassTypeArg(), null)

    foo.run {
        konst x12: String? = select(materialize(), null)
        konst x13: String? = select(<!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>idFromClassTypeArgWithAnotherTypeArg<!>(), null)
        konst x14: String? = select(idFromClassTypeArg(), null)
    }

    konst boolean: Boolean? = true

    konst x15: String? = when (boolean) {
        true -> select(materialize(), null)
            false -> select(materialize(), null)
            null -> null
    }

    konst x16: String? = when (boolean) {
        true -> null
        false -> materialize()
        null -> null
    }

    konst x17: String? = when (boolean) {
        true -> if (true) null else materialize()
        false -> if (true) materialize() else null
            null -> if (true) null else null
    }

    konst x18: String? = try {
        materialize()
    } catch (e: Exception) {
        null
    }

    konst x19: String? = if (true) materialize<Nothing?>() else null

    konst x20: String? = if (true) materialize<String?>() else null

    konst x21: String? = if (true) materialize() else TODO()

    konst x22: String? = if (true) return else materialize()

    konst x23: String? = if (true) id(null) else null

    foo1(if (true) materialize() else null)

    konst x24 = id(foo1(if (true) materialize() else null))

    konst x25 = select(materialize(), null).foo2()

    // TODO
    konst x26 = select(materialize(), null).run { foo2() }

    konst x27: () -> String? = {
        id(id(if (true) materialize() else null))
    }
}

fun foo1(x: String?) {}
fun String?.foo2() {}
