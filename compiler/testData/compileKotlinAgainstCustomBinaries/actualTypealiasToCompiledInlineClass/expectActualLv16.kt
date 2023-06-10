@file:Suppress("EXPECT_AND_ACTUAL_IN_THE_SAME_MODULE")

package test

@kotlin.jvm.JvmInline
expect konstue class ExpectValueActualInline(konst konstue: Int)

@kotlin.jvm.JvmInline
expect konstue class ExpectValueActualValue(konst konstue: Int)

actual typealias ExpectValueActualInline = lib.InlineClass
actual typealias ExpectValueActualValue = lib.ValueClass
