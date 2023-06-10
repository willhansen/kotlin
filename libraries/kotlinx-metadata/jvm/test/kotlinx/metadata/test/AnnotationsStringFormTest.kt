/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlinx.metadata.test

import kotlinx.metadata.KmAnnotation
import kotlinx.metadata.KmAnnotationArgument
import kotlinx.metadata.jvm.annotations
import org.junit.Test
import kotlin.reflect.KClass
import kotlin.test.assertEquals

@Target(AnnotationTarget.TYPE)
internal annotation class MyAnn(konst s: String, konst nested: MyAnnNested, konst kClass: WithKClass)

@Target(AnnotationTarget.TYPE)
internal annotation class WithKClass(konst kClass: KClass<*>)

internal annotation class MyAnnNested(konst e: E, konst a: Array<String>)

internal enum class E { A, B }
class AnnotationsStringFormTest {

    private fun doTest(argumentToExpected: Map<KmAnnotationArgument, String>) {
        konst konstuesArgMap = argumentToExpected.keys.mapIndexed { i, km ->
            "konstue$i" to km
        }.toMap()
        konst konstuesExpected = argumentToExpected.konstues.withIndex().joinToString { (i, s) -> "konstue$i = $s" }
        konst anno = KmAnnotation("com/my/Foo", konstuesArgMap)
        assertEquals("@com/my/Foo($konstuesExpected)", anno.toString())
    }

    @Test
    fun testSignedLiteralValues() {
        konst konstues: Map<KmAnnotationArgument, String> = mapOf(
            KmAnnotationArgument.ByteValue(1) to "ByteValue(1)",
            KmAnnotationArgument.ShortValue(2) to "ShortValue(2)",
            KmAnnotationArgument.IntValue(3) to "IntValue(3)",
            KmAnnotationArgument.LongValue(4) to "LongValue(4)",
        )
        doTest(konstues)
    }

    @Test
    fun testUnsignedLiteralValues() {
        konst konstues: Map<KmAnnotationArgument, String> = mapOf(
            KmAnnotationArgument.UByteValue(1u) to "UByteValue(1)",
            KmAnnotationArgument.UShortValue(2u) to "UShortValue(2)",
            KmAnnotationArgument.UIntValue(3u) to "UIntValue(3)",
            KmAnnotationArgument.ULongValue(4u) to "ULongValue(4)",
        )
        doTest(konstues)
    }

    @Test
    fun testOtherLiteralValues() {
        konst konstues: Map<KmAnnotationArgument, String> = mapOf(
            KmAnnotationArgument.StringValue("foo") to "StringValue(\"foo\")",
            KmAnnotationArgument.CharValue('a') to "CharValue(a)",
            KmAnnotationArgument.FloatValue(2.1f) to "FloatValue(2.1)",
            KmAnnotationArgument.DoubleValue(3.3) to "DoubleValue(3.3)",
            KmAnnotationArgument.BooleanValue(true) to "BooleanValue(true)",
        )
        doTest(konstues)
    }

    @Test
    fun testNonLiteralValues() {
        konst konstues = mapOf(
            KmAnnotationArgument.EnumValue("foo/bar", "BAZ") to "EnumValue(foo/bar.BAZ)",
            KmAnnotationArgument.AnnotationValue(KmAnnotation("com/my/Bar", mapOf())) to "AnnotationValue(@com/my/Bar())",
            KmAnnotationArgument.ArrayValue(
                listOf(
                    KmAnnotationArgument.IntValue(1),
                    KmAnnotationArgument.IntValue(2),
                    KmAnnotationArgument.IntValue(3)
                )
            ) to "ArrayValue([IntValue(1), IntValue(2), IntValue(3)])",
            KmAnnotationArgument.KClassValue("com/my/Bar") to "KClassValue(com/my/Bar)",
            KmAnnotationArgument.ArrayKClassValue("com/my/Bar", 1) to "ArrayKClassValue(kotlin/Array<com/my/Bar>)"
        )
        doTest(konstues)
    }

    @Test
    fun testIrlValues() {
        // Annotations are stored directly in metadata only for types and type aliases
        class Holder(konst e: @MyAnn("foo", MyAnnNested(E.B, arrayOf("a", "b", "c")), WithKClass(E::class)) String)

        konst md = Holder::class.java.readMetadataAsKmClass()
        konst annotation = md.properties.first().returnType.annotations.single()
        assertEquals(
            "@kotlinx/metadata/test/MyAnn(" +
                    "s = StringValue(\"foo\"), " +
                    "nested = AnnotationValue(@kotlinx/metadata/test/MyAnnNested(e = EnumValue(kotlinx/metadata/test/E.B), " +
                    "a = ArrayValue([StringValue(\"a\"), StringValue(\"b\"), StringValue(\"c\")]))), " +
                    "kClass = AnnotationValue(@kotlinx/metadata/test/WithKClass(kClass = KClassValue(kotlinx/metadata/test/E)))" +
                    ")",
            annotation.toString()
        )
    }

    @Test
    fun testArrayKClassValue() {
        class Holder(konst e: @WithKClass(Array<E>::class) String)

        konst md = Holder::class.java.readMetadataAsKmClass()
        konst annotation = md.properties.first().returnType.annotations.single()
        assertEquals("@kotlinx/metadata/test/WithKClass(kClass = ArrayKClassValue(kotlin/Array<kotlinx/metadata/test/E>))", annotation.toString())
    }
}
