// FIR_IDENTICAL
// SKIP_TXT
// ALLOW_KOTLIN_PACKAGE

// MODULE: m1-common
// FILE: common.kt

package kotlin.jvm

annotation class JvmInline

expect konstue class VC(konst a: Any)

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt

package kotlin.jvm

@JvmInline
actual konstue class VC(konst a: Any)
