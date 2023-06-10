// DIAGNOSTICS: -UNUSED_VARIABLE
// WITH_STDLIB
// ISSUE: KT-57456, KT-57608
@file:OptIn(ExperimentalContracts::class)

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

enum class Enum {
    A {
        konst aInside = foo()
        konst bInside = inPlaceRun { foo() }
        konst cInside = nonInPlaceRun { foo() }

        konst dInside by foo()
        konst eInside by inPlaceDelegate { foo() }
        konst fInside by nonInPlaceDelegate { foo() }
    },
    B {
        init {
            konst aInit = foo()
            konst bInit = inPlaceRun { foo() }
            konst cInit = nonInPlaceRun { foo() }

            konst dInit by foo()
            konst eInit by inPlaceDelegate { foo() }
            konst fInit by nonInPlaceDelegate { foo() }
        }
    },
    C {
        init {
            class Local {
                konst aInside = foo()
                konst bInside = inPlaceRun { foo() }
                konst cInside = nonInPlaceRun { foo() }

                konst dInside by foo()
                konst eInside by inPlaceDelegate { foo() }
                konst fInside by nonInPlaceDelegate { foo() }

                init {
                    konst aInit = foo()
                    konst bInit = inPlaceRun { foo() }
                    konst cInit = nonInPlaceRun { foo() }

                    konst dInit by foo()
                    konst eInit by inPlaceDelegate { foo() }
                    konst fInit by nonInPlaceDelegate { foo() }
                }

                fun localFun() {
                    konst a = foo()
                    konst b = inPlaceRun { foo() }
                    konst c = nonInPlaceRun { foo() }

                    konst d by foo()
                    konst e by inPlaceDelegate { foo() }
                    konst f by nonInPlaceDelegate { foo() }
                }
            }
        }
    },
    D {
        init {
            konst someObj = object {
                konst aInside = foo()
                konst bInside = inPlaceRun { foo() }
                konst cInside = nonInPlaceRun { foo() }

                konst dInside by foo()
                konst eInside by inPlaceDelegate { foo() }
                konst fInside by nonInPlaceDelegate { foo() }

                init {
                    konst aInit = foo()
                    konst bInit = inPlaceRun { foo() }
                    konst cInit = nonInPlaceRun { foo() }

                    konst dInit by foo()
                    konst eInit by inPlaceDelegate { foo() }
                    konst fInit by nonInPlaceDelegate { foo() }
                }

                fun localFun() {
                    konst a = foo()
                    konst b = inPlaceRun { foo() }
                    konst c = nonInPlaceRun { foo() }

                    konst d by foo()
                    konst e by inPlaceDelegate { foo() }
                    konst f by nonInPlaceDelegate { foo() }
                }
            }
        }
    }
    ;

    konst a = foo()
    konst b = inPlaceRun { foo() }
    konst c = nonInPlaceRun { foo() }

    konst d by foo()
    konst e by inPlaceDelegate { foo() }
    konst f by nonInPlaceDelegate { foo() }

    companion object {
        fun foo(): String = "foo()"
    }
}

enum class EnumWithConstructor(konst a: String, konst b: String, konst c: String) {
    A(
        a = <!UNINITIALIZED_ENUM_COMPANION!>foo()<!>,
        b = inPlaceRun { <!UNINITIALIZED_ENUM_COMPANION!>foo()<!> },
        c = nonInPlaceRun { <!UNINITIALIZED_ENUM_COMPANION!>foo()<!> }
    );

    companion object {
        fun foo(): String = "foo()"
    }
}

operator fun <T> T.provideDelegate(thisRef: Any?, prop: KProperty<*>): ReadOnlyProperty<Any?, T> = ReadOnlyProperty { _, _ -> this }

inline fun <T> inPlaceRun(block: () -> T): T {
    contract { callsInPlace(block) }
    return block()
}

fun <T> nonInPlaceRun(block: () -> T): T {
    return block()
}

inline fun <T> inPlaceDelegate(block: () -> T): ReadOnlyProperty<Any?, T> {
    contract { callsInPlace(block) }
    konst konstue = block()
    return ReadOnlyProperty { _, _ -> konstue }
}

fun <T> nonInPlaceDelegate(block: () -> T): ReadOnlyProperty<Any?, T> {
    return ReadOnlyProperty { _, _ -> block() }
}
