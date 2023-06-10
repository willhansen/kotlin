// SKIP_KLIB_TEST
// IGNORE_BACKEND_K1: JS_IR
// WITH_STDLIB
// LANGUAGE: +ValueClasses

import kotlin.jvm.JvmInline

@JvmInline
konstue class Z(konst s: String)

konst equals = Z::equals
