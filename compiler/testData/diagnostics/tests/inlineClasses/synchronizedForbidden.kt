// TARGET_BACKEND: JVM
// WITH_STDLIB
// SKIP_TXT
// KT-49339

@JvmInline
konstue class A(konst a: Int) {
    <!SYNCHRONIZED_ON_VALUE_CLASS!>@get:Synchronized<!>
    konst f0
        get() = Unit

    <!SYNCHRONIZED_ON_VALUE_CLASS!>@Synchronized<!>
    fun f1() = Unit

    <!SYNCHRONIZED_ON_VALUE_CLASS!>@Synchronized<!>
    fun String.f2() = Unit

    <!SYNCHRONIZED_ON_VALUE_CLASS!>@get:Synchronized<!>
    konst String.f3
        get() = Unit

    <!SYNCHRONIZED_ON_VALUE_CLASS!>@get:Synchronized<!>
    konst A.f4
        get() = Unit

    <!SYNCHRONIZED_ON_VALUE_CLASS!>@Synchronized<!>
    fun A.f5() = Unit

    konst f6
        <!SYNCHRONIZED_ON_VALUE_CLASS!>@Synchronized<!>
        get() = Unit

    konst A.f7
        <!SYNCHRONIZED_ON_VALUE_CLASS!>@Synchronized<!>
        get() = Unit

    konst String.f8
        <!SYNCHRONIZED_ON_VALUE_CLASS!>@Synchronized<!>
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
    <!FORBIDDEN_SYNCHRONIZED_BY_VALUE_CLASSES_OR_PRIMITIVES!>synchronized<!>(a) {}
    <!FORBIDDEN_SYNCHRONIZED_BY_VALUE_CLASSES_OR_PRIMITIVES!>synchronized<!>(2) {}
    <!FORBIDDEN_SYNCHRONIZED_BY_VALUE_CLASSES_OR_PRIMITIVES!>synchronized<!>(0x2) {}
    <!FORBIDDEN_SYNCHRONIZED_BY_VALUE_CLASSES_OR_PRIMITIVES!>synchronized<!>(2U) {}
    <!FORBIDDEN_SYNCHRONIZED_BY_VALUE_CLASSES_OR_PRIMITIVES!>synchronized<!>(true) {}
    <!FORBIDDEN_SYNCHRONIZED_BY_VALUE_CLASSES_OR_PRIMITIVES!>synchronized<!>(2L) {}
    <!FORBIDDEN_SYNCHRONIZED_BY_VALUE_CLASSES_OR_PRIMITIVES!>synchronized<!>(2.to(1).first) {}
    <!FORBIDDEN_SYNCHRONIZED_BY_VALUE_CLASSES_OR_PRIMITIVES!>synchronized<!>(2.toByte()) {}
    <!FORBIDDEN_SYNCHRONIZED_BY_VALUE_CLASSES_OR_PRIMITIVES!>synchronized<!>(2UL) {}
    <!FORBIDDEN_SYNCHRONIZED_BY_VALUE_CLASSES_OR_PRIMITIVES!>synchronized<!>(2F) {}
    <!FORBIDDEN_SYNCHRONIZED_BY_VALUE_CLASSES_OR_PRIMITIVES!>synchronized<!>(2.0) {}
    <!FORBIDDEN_SYNCHRONIZED_BY_VALUE_CLASSES_OR_PRIMITIVES!>synchronized<!>('2') {}
    <!FORBIDDEN_SYNCHRONIZED_BY_VALUE_CLASSES_OR_PRIMITIVES!>synchronized<!>(block={}, lock='2')
    <!FORBIDDEN_SYNCHRONIZED_BY_VALUE_CLASSES_OR_PRIMITIVES!>synchronized<!>(block={}, lock=a)
    for (b in listOf(a)) {
        <!FORBIDDEN_SYNCHRONIZED_BY_VALUE_CLASSES_OR_PRIMITIVES!>synchronized<!>(b) {}
        <!FORBIDDEN_SYNCHRONIZED_BY_VALUE_CLASSES_OR_PRIMITIVES!>synchronized<!>(b.to(1).first) {}
        <!FORBIDDEN_SYNCHRONIZED_BY_VALUE_CLASSES_OR_PRIMITIVES!>synchronized<!>(block={}, lock=a)
    }
}
