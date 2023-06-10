// TARGET_BACKEND: JVM_IR
// JVM_TARGET: 1.8
// FULL_JDK
// WITH_REFLECT

// Android doesn't have @Repeatable before API level 24, so findAnnotations can't unpack repeatable annotations.
// IGNORE_BACKEND: ANDROID

import kotlin.test.assertEquals
import kotlin.reflect.full.findAnnotations

@JvmRepeatable(ArrayOfInt.ContainerOfInt::class)
annotation class ArrayOfInt(konst ints: IntArray = []) {
    annotation class ContainerOfInt(konst konstue: Array<ArrayOfInt>)
}

@JvmRepeatable(ArrayOfString.ContainerOfString::class)
annotation class ArrayOfString(konst strings: Array<String> = []) {
    annotation class ContainerOfString(konst konstue: Array<ArrayOfString>)
}

@JvmRepeatable(ArrayOfEnum.ContainerOfEnum::class)
annotation class ArrayOfEnum(konst enums: Array<DeprecationLevel> = []) {
    annotation class ContainerOfEnum(konst konstue: Array<ArrayOfEnum>)
}

@JvmRepeatable(ArrayOfAnnotation.ContainerOfAnnotation::class)
annotation class ArrayOfAnnotation(konst annotations: Array<ArrayOfString> = []) {
    annotation class ContainerOfAnnotation(konst konstue: Array<ArrayOfAnnotation>)
}

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
    assertEquals("[1, 2]", C::arrayOfInt.findAnnotations<ArrayOfInt>().map { it.ints.single() }.toString())
    assertEquals("[a, b]", C::arrayOfString.findAnnotations<ArrayOfString>().map { it.strings.single() }.toString())
    assertEquals("[WARNING, ERROR]", C::arrayOfEnum.findAnnotations<ArrayOfEnum>().map { it.enums.single() }.toString())
    assertEquals(
        "[a, b]",
        C::arrayOfAnnotation.findAnnotations<ArrayOfAnnotation>().map {
            it.annotations.single().strings.single()
        }.toString()
    )

    return "OK"
}
