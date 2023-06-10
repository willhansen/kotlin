// TARGET_BACKEND: JVM
// WITH_REFLECT

class A(konst result: String)

fun box(): String {
    konst a = (::A).call("OK")
    return a.result
}
