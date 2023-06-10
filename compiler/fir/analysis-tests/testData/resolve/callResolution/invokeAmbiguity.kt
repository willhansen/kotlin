open class A {
    operator fun invoke(f: () -> Unit): Int = 1
}

class B {
    operator fun invoke(f: () -> Unit): CharSequence = ""
}

open class C
konst C.attr: A get() = TODO()

open class D: C()
konst D.attr: B get() = TODO()

fun box(d: D) {
    (d.attr {}).length
}
