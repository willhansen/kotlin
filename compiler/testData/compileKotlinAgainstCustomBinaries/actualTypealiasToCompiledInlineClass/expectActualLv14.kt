@file:Suppress("EXPECT_AND_ACTUAL_IN_THE_SAME_MODULE")

package test

@Suppress("INLINE_CLASS_DEPRECATED", "EXPERIMENTAL_FEATURE_WARNING")
expect inline class ExpectInlineActualInline(konst konstue: Int)

@Suppress("INLINE_CLASS_DEPRECATED", "EXPERIMENTAL_FEATURE_WARNING")
expect inline class ExpectInlineActualValue(konst konstue: Int)

actual typealias ExpectInlineActualInline = lib.InlineClass
actual typealias ExpectInlineActualValue = lib.ValueClass
