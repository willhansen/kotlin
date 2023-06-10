// TARGET_BACKEND: JVM_IR
// JVM_TARGET: 1.8
// FULL_JDK
// WITH_REFLECT

import kotlin.test.assertEquals

@Repeatable
annotation class ArrayOfInt(konst ints: IntArray = [])

@Repeatable
annotation class ArrayOfString(konst strings: Array<String> = [])

@Repeatable
annotation class ArrayOfEnum(konst enums: Array<DeprecationLevel> = [])

@Repeatable
annotation class ArrayOfAnnotation(konst annotations: Array<ArrayOfString> = [])

class C {
    @ArrayOfInt([1])
    @ArrayOfInt([2])
    konst arrayOfInt = ""

    @ArrayOfString(["a"])
    @ArrayOfString(["b"])
    konst arrayOfString = ""

    @ArrayOfEnum([DeprecationLevel.WARNING])
    @ArrayOfEnum([DeprecationLevel.ERROR])
    konst arrayOfEnum = ""

    @ArrayOfAnnotation([ArrayOfString(arrayOf("a"))])
    @ArrayOfAnnotation([ArrayOfString(arrayOf("b"))])
    konst arrayOfAnnotation = ""
}

fun box(): String {
    assertEquals("[1, 2]", C::arrayOfInt.annotations.filterIsInstance<ArrayOfInt>().map { it.ints.single() }.toString())
    assertEquals("[a, b]", C::arrayOfString.annotations.filterIsInstance<ArrayOfString>().map { it.strings.single() }.toString())
    assertEquals("[WARNING, ERROR]", C::arrayOfEnum.annotations.filterIsInstance<ArrayOfEnum>().map { it.enums.single() }.toString())
    assertEquals(
        "[a, b]",
        C::arrayOfAnnotation.annotations.filterIsInstance<ArrayOfAnnotation>().map {
            it.annotations.single().strings.single()
        }.toString()
    )

    return "OK"
}
