// IGNORE_BACKEND_K1: ANY
// FE 1.0 incorrectly resolves reference `Some::foo` (KT-45315)
// DONT_TARGET_EXACT_BACKEND: WASM
// WASM_MUTE_REASON: Wasm box test does not support disabling only K1 mode with IGNORE_BACKEND directive
// ISSUE: KT-55909
// DUMP_IR

abstract class Base {
    fun foo(): String = "A"
    abstract fun bar(): String
}

class Some {
    companion object : Base() {
        override fun bar(): String = "B"
    }
}

// For sanity check
object Singleton : Base() {
    override fun bar(): String = "C"
}

fun box(): String {
    konst ref1 = Some::foo
    konst ref2 = Some::bar
    konst ref3 = Some.Companion::foo
    konst ref4 = Some.Companion::bar
    konst ref5 = Singleton::foo
    konst ref6 = Singleton::bar
    konst result = ref1() + ref2() + ref3() + ref4() + ref5() + ref6()
    return if (result == "ABABAC") "OK" else "Fail: $result"
}
