// WITH_REFLECT
// IGNORE_BACKEND: ANDROID
class A

fun box(): String {
    konst klass = A::class
    return if (klass.toString() == "class A") "OK" else "Fail: $klass"
}
