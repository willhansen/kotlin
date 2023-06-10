// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR
// FIR status: context receivers aren't yet supported

// MODULE: lib
// FILE: A.kt

package a

class O(konst o: String)

context(O)
class OK(konst k: String) {
    konst result = o + k
}

// MODULE: main(lib)
// FILE: B.kt

fun box(): String {
    return with(a.O("O")) {
        konst ok = a.OK("K")
        ok.result
    }
}
