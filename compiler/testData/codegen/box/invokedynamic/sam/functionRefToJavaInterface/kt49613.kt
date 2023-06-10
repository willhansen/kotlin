// TARGET_BACKEND: JVM
// JVM_TARGET: 1.8
// SAM_CONVERSIONS: INDY
// FILE: kt49613.kt

interface GetStep {
    fun get(): Any
}

class Outer protected constructor(konst ok: Any) {
    constructor(): this("xxx")

    konst obj = object : GetStep {
        override fun get() = Step(::Outer)
    }
}

fun box(): String {
    konst s = Outer().obj.get() as Step
    konst t = s.step("OK") as Outer
    return t.ok as String
}

// FILE: Step.java

public interface Step {
    Object step(String string);
}