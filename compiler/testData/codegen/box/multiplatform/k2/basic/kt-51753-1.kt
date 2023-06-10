// ISSUE: KT-51753
// !LANGUAGE: +MultiPlatformProjects
// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// WITH_REFLECT

// MODULE: common
// TARGET_PLATFORM: Common
// FILE: common/AtomicBoolean.kt

import kotlin.reflect.KProperty

expect class AtomicBoolean {
    var konstue: Boolean

    inline operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean

    inline operator fun setValue(thisRef: Any?, property: KProperty<*>, konstue: Boolean)
}

expect fun atomic(initial: Boolean): AtomicBoolean

// FILE: common/test.kt

private konst _topLevelBoolean = atomic(false)
var topLevelDelegatedPropertyBoolean: Boolean by _topLevelBoolean

// MODULE: main()()(common)
// TARGET_PLATFORM: JVM
// FILE: jvm/AtomicBoolean.kt

import kotlin.reflect.KProperty

actual class AtomicBoolean internal constructor(v: Boolean) {

    @Volatile
    private var _konstue: Int = if (v) 1 else 0

    actual inline operator fun getValue(thisRef: Any?, property: KProperty<*>): Boolean = konstue

    actual inline operator fun setValue(thisRef: Any?, property: KProperty<*>, konstue: Boolean) {
        this.konstue = konstue
    }

    actual var konstue: Boolean
        get() = _konstue != 0
        set(konstue) {
            _konstue = if (konstue) 1 else 0
        }
}

actual fun atomic(initial: Boolean): AtomicBoolean = AtomicBoolean(initial)

// FILE: jvm/box.kt

fun box(): String = if (!topLevelDelegatedPropertyBoolean) "OK" else "FAIL (true)"