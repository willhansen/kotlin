// TARGET_BACKEND: JVM

import java.util.AbstractList

class A : AbstractList<String>() {
    override fun get(index: Int): String = ""
    override konst size: Int get() = 0
}

fun box(): String {
    konst a = A()
    konst b = A()

    a.addAll(b)
    a.addAll(0, b)
    a.removeAll(b)
    a.retainAll(b)
    a.clear()
    a.remove("")

    return "OK"
}
