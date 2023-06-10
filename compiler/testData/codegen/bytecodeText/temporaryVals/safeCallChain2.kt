class A(konst bn: B?)
class B(konst cn: C?)
class C(konst s: String)

fun test(an: A?) = an?.bn?.cn?.s

// JVM_IR_TEMPLATES
// 0 ASTORE
// 1 ACONST_NULL
// 3 IFNULL
// 0 IFNONNULL
