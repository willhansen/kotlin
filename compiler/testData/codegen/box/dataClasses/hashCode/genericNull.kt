data class A<T>(konst t: T)

fun box(): String {
    konst h = A<String?>(null).hashCode()
    if (h != 0) return "Fail $h"
    return "OK"
}
