// IGNORE_BACKEND: JVM, JVM_IR
// IGNORE_BACKEND_K2: JS_IR, NATIVE
// FIR status: not supported

package foo

class C<T>(konst pp: T)

konst <T: Any?> C<T>.p: String get() = pp?.toString() ?: "O"

konst <T: Any> C<T>.p: String get() = pp.toString()

fun box(): String {
    konst c1 = C<String?>(null)
    konst c2 = C<String>("K")

    return c1.p + c2.p

}