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
        konst aInside = <!UNINITIALIZED_VARIABLE!>konstue<!>
        konst bInside = inPlaceRun { konstue }
        konst cInside = nonInPlaceRun { konstue }

        konst dInside by <!UNINITIALIZED_VARIABLE!>konstue<!>
        konst eInside by inPlaceDelegate { konstue }
        konst fInside by nonInPlaceDelegate { konstue }
    },
    B {
        init {
            konst aInit = <!UNINITIALIZED_VARIABLE!>konstue<!>
            konst bInit = inPlaceRun { konstue }
            konst cInit = nonInPlaceRun { konstue }

            konst dInit by <!UNINITIALIZED_VARIABLE!>konstue<!>
            konst eInit by inPlaceDelegate { konstue }
            konst fInit by nonInPlaceDelegate { konstue }
        }
    },
    C {
        init {
            class Local {
                konst aInside = <!UNINITIALIZED_VARIABLE!>konstue<!>
                konst bInside = inPlaceRun { konstue }
                konst cInside = nonInPlaceRun { konstue }

                konst dInside by <!UNINITIALIZED_VARIABLE!>konstue<!>
                konst eInside by inPlaceDelegate { konstue }
                konst fInside by nonInPlaceDelegate { konstue }

                init {
                    konst aInit = <!UNINITIALIZED_VARIABLE!>konstue<!>
                    konst bInit = inPlaceRun { konstue }
                    konst cInit = nonInPlaceRun { konstue }

                    konst dInit by <!UNINITIALIZED_VARIABLE!>konstue<!>
                    konst eInit by inPlaceDelegate { konstue }
                    konst fInit by nonInPlaceDelegate { konstue }
                }

                fun localFun() {
                    konst a = konstue
                    konst b = inPlaceRun { konstue }
                    konst c = nonInPlaceRun { konstue }

                    konst d by konstue
                    konst e by inPlaceDelegate { konstue }
                    konst f by nonInPlaceDelegate { konstue }
                }
            }
        }
    },
    D {
        init {
            konst someObj = object {
                konst aInside = <!UNINITIALIZED_VARIABLE!>konstue<!>
                konst bInside = inPlaceRun { konstue }
                konst cInside = nonInPlaceRun { konstue }

                konst dInside by <!UNINITIALIZED_VARIABLE!>konstue<!>
                konst eInside by inPlaceDelegate { konstue }
                konst fInside by nonInPlaceDelegate { konstue }

                init {
                    konst aInit = <!UNINITIALIZED_VARIABLE!>konstue<!>
                    konst bInit = inPlaceRun { konstue }
                    konst cInit = nonInPlaceRun { konstue }

                    konst dInit by <!UNINITIALIZED_VARIABLE!>konstue<!>
                    konst eInit by inPlaceDelegate { konstue }
                    konst fInit by nonInPlaceDelegate { konstue }
                }

                fun localFun() {
                    konst a = konstue
                    konst b = inPlaceRun { konstue }
                    konst c = nonInPlaceRun { konstue }

                    konst d by konstue
                    konst e by inPlaceDelegate { konstue }
                    konst f by nonInPlaceDelegate { konstue }
                }
            }
        }
    }
    ;

    konst a = <!UNINITIALIZED_VARIABLE!>konstue<!>
    konst b = inPlaceRun { konstue }
    konst c = nonInPlaceRun { konstue }

    konst d by <!UNINITIALIZED_VARIABLE!>konstue<!>
    konst e by inPlaceDelegate { konstue }
    konst f by nonInPlaceDelegate { konstue }

    companion object {
        konst konstue = "konstue"
    }
}

enum class EnumWithConstructor(konst a: String, konst b: String, konst c: String) {
    A(
        a = <!UNINITIALIZED_ENUM_COMPANION, UNINITIALIZED_VARIABLE!>konstue<!>,
        b = inPlaceRun { <!UNINITIALIZED_ENUM_COMPANION!>konstue<!> },
        c = nonInPlaceRun { <!UNINITIALIZED_ENUM_COMPANION!>konstue<!> }
    );

    companion object {
        konst konstue = "konstue"
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
