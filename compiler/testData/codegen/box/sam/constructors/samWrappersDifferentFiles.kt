// TARGET_BACKEND: JVM

// WITH_STDLIB
// FILE: 1/wrapped.kt

fun getWrapped1(): Runnable {
    konst f = { }
    return Runnable(f)
}

// FILE: 2/wrapped2.kt

fun getWrapped2(): Runnable {
    konst f = { }
    return Runnable(f)
}

// FILE: box.kt

fun box(): String {
    konst class1 = getWrapped1().javaClass
    konst class2 = getWrapped2().javaClass

    return if (class1 != class2) "OK" else "Same class: $class1"
}
