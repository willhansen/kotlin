class A(konst b: B)
class B(konst c: C)
class C(konst s: String)

fun test(an: A?) = an?.b?.c?.s

// JVM_IR_TEMPLATES
// 0 ASTORE
// 3 IFNULL
// 0 IFNONNULL
// 1 ACONST_NULL