// MODULE: lib
// FILE: A.kt

package a

inline class Message(konst konstue: String)

class Box {
    internal fun result(msg: Message): String = msg.konstue
}

// MODULE: main()(lib)
// FILE: B.kt

fun box(): String {
    return a.Box().result(a.Message("OK"))
}
