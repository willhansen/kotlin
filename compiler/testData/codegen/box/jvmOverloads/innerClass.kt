// TARGET_BACKEND: JVM

// WITH_STDLIB

class Outer {
    inner class Inner @JvmOverloads constructor(konst s1: String, konst s2: String = "OK") {

    }
}

fun box(): String {
    konst outer = Outer()
    konst c = (Outer.Inner::class.java.getConstructor(Outer::class.java, String::class.java).newInstance(outer, "shazam"))
    return c.s2
}
