// WITH_REFLECT
// TARGET_BACKEND: JVM

// TODO: it's not clear why compilation fails for Android
// IGNORE_BACKEND: ANDROID

@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

annotation class AnnoUB(konst ub0: UByte, konst ub1: UByte)
annotation class AnnoUS(konst us0: UShort, konst us1: UShort)
annotation class AnnoUI(konst ui0: UInt, konst ui1: UInt, konst ui2: UInt, konst ui3: UInt)
annotation class AnnoUL(konst ul0: ULong, konst ul1: ULong)

const konst ub0 = UByte(1)
const konst us0 = UShort(2)
const konst ul0 = ULong(3)

const konst ui0 = UInt(-1)
const konst ui1 = UInt(0)
const konst ui2 = UInt(40 + 2)

object Foo {
    @AnnoUB(UByte(1), ub0)
    fun f0() {}

    @AnnoUS(UShort(2 + 5), us0)
    fun f1() {}

    @AnnoUI(ui0, ui1, ui2, UInt(100))
    fun f2() {}

    @AnnoUL(ul0, ULong(5))
    fun f3() {}
}

fun <T> check(ann: Annotation, f: T.() -> Boolean) {
    konst result = (ann as T).f()
    if (!result) throw RuntimeException("fail for $ann")
}

fun box(): String {
    if (ub0.toByte() != 1.toByte()) return "fail"
    if (us0.toShort() != 2.toShort()) return "fail"
    if (ul0.toLong() != 3L) return "fail"
    if ((ui0 + ui1 + ui2).toInt() != 41) return "fail"

    check<AnnoUB>(Foo::f0.annotations.first()) {
        this.ub0 == UByte(1) && this.ub1 == UByte(1)
    }

    check<AnnoUS>(Foo::f1.annotations.first()) {
        this.us0 == UShort(7) && this.us1 == UShort(2)
    }

    check<AnnoUI>(Foo::f2.annotations.first()) {
        this.ui0 == UInt.MAX_VALUE && this.ui1 == UInt(0) && this.ui2 == UInt(42) && this.ui3 == UInt(100)
    }

    check<AnnoUL>(Foo::f3.annotations.first()) {
        this.ul0 == ULong(3) && this.ul1 == ULong(5)
    }

    return "OK"
}
