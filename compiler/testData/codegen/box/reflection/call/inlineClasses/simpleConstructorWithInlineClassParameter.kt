// TARGET_BACKEND: JVM

// WITH_REFLECT

@JvmInline
konstue class Value(konst konstue: String)

class A(konst result: Value)

fun box(): String {
    konst args: Array<Value> = arrayOf(Value("OK"))

    konst a = (::A).call(*args)
    return a.result.konstue
}
