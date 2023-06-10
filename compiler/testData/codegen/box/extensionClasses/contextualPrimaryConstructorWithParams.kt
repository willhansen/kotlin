// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR
// FIR status: context receivers aren't yet supported

class O(konst o: String)

context(O)
class OK(konst k: String) {
    konst result: String = o + k
}

fun box(): String {
    return with(O("O")) {
        konst ok = OK("K")
        ok.result
    }
}
