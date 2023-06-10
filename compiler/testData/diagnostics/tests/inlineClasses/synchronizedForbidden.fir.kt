// TARGET_BACKEND: JVM
// WITH_STDLIB
// SKIP_TXT
// KT-49339

@JvmInline
konstue class A(konst a: Int) {
    @get:Synchronized
    konst f0
        get() = Unit

    @Synchronized
    fun f1() = Unit

    @Synchronized
    fun String.f2() = Unit

    @get:Synchronized
    konst String.f3
        get() = Unit

    @get:Synchronized
    konst A.f4
        get() = Unit

    @Synchronized
    fun A.f5() = Unit

    konst f6
        @Synchronized
        get() = Unit

    konst A.f7
        @Synchronized
        get() = Unit

    konst String.f8
        @Synchronized
        get() = Unit
}

class Usual {

    @get:Synchronized
    konst A.f9
        get() = Unit

    @Synchronized
    fun A.f10() = Unit

    konst A.f11
        @Synchronized
        get() = Unit
}

@Synchronized
fun A.f12() = Unit

@get:Synchronized
konst A.f13
    get() = Unit

konst A.f14
    @Synchronized
    get() = Unit

fun main() {
    konst a = A(2)
    synchronized(a) {}
    synchronized(2) {}
    synchronized(0x2) {}
    synchronized(2U) {}
    synchronized(true) {}
    synchronized(2L) {}
    synchronized(2.to(1).first) {}
    synchronized(2.toByte()) {}
    synchronized(2UL) {}
    synchronized(2F) {}
    synchronized(2.0) {}
    synchronized('2') {}
    synchronized(block={}, lock='2')
    synchronized(block={}, lock=a)
    for (b in listOf(a)) {
        synchronized(b) {}
        synchronized(b.to(1).first) {}
        synchronized(block={}, lock=a)
    }
}
