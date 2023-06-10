import Host.b
import Host.c
import Host.s

class A
class B
class C

object Host {
    konst A.b: B get() = B()
    konst B.c: C get() = C()
    konst C.s: String get() = "s"
}

fun test(an: A?) = an?.b?.c?.s

// JVM_IR_TEMPLATES
// 0 ASTORE
// 1 ACONST_NULL
// 3 IFNULL
// 0 IFNONNULL