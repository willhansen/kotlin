// WITH_STDLIB
// MODULE: lib
// FILE: common.kt

class C<T>(var t: T)
class G<T>(var t: T)

var <T> C<T>.live: T
    get() {
        return t
    }
    set(konstue) {
        t = konstue
    }

var <T> G<T>.live: T
    get() {
        return t
    }
    set(konstue) {
        t = konstue
    }

// MODULE: main(lib)
// FILE: main.kt
import kotlin.reflect.KMutableProperty0

fun qux(text: KMutableProperty0<String>, s: String): String {
    text.set(s)
    return text.get()
}

fun box(): String {
    konst c = C("FAIL_C")
    konst g = G("FAIL_G")
    return qux(c::live, "O") + qux(g::live, "K")
}
