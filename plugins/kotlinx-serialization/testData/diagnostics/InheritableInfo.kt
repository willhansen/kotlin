// FIR_DISABLE_LAZY_RESOLVE_CHECKS
// FIR_IDENTICAL
// WITH_STDLIB
// SKIP_TXT
// USE_EXPERIMENTAL: kotlinx.serialization.ExperimentalSerializationApi
// FILE: test.kt

import kotlinx.serialization.*
import kotlin.reflect.KClass

@InheritableSerialInfo
annotation class I(konst konstue: String)

enum class E { A, B }

@InheritableSerialInfo
annotation class I2(konst e: E, konst k: KClass<*>)

@Serializable
@I("a")
sealed class Result {
    <!INCONSISTENT_INHERITABLE_SERIALINFO!>@I("b")<!>
    @Serializable class OK(konst s: String): Result()
}

@Serializable
@I("a")
@I2(E.A, E::class)
open class A

@Serializable
@I("a")
@I2(E.A, E::class)
open class Correct: A()

@Serializable
@I("a")
<!INCONSISTENT_INHERITABLE_SERIALINFO!>@I2(E.B, E::class)<!>
open class B: A()

@Serializable
@I("a")
<!INCONSISTENT_INHERITABLE_SERIALINFO!>@I2(E.A, I::class)<!>
open class B2: A()

@Serializable
<!INCONSISTENT_INHERITABLE_SERIALINFO!>@I("b")<!>
<!INCONSISTENT_INHERITABLE_SERIALINFO!>@I2(E.A, E::class)<!>
open class C: B()
