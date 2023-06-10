class A(konst b: B)
class B(konst c: C)
class C(konst s: String)

fun test(na: A?) =
    na?.b?.c?.s

// JVM_IR_TEMPLATES
// 3 DUP
// 3 IFNULL
// 0 IFNONNULL
// 1 ACONST_NULL

// JVM_TEMPLATES
// 3 DUP
// 3 IFNULL
// 0 IFNONNULL
// 1 ACONST_NULL