/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlinx.metadata

/**
 * Represents an annotation, written to the Kotlin metadata. Note that not all annotations are written to metadata on all platforms.
 * For example, on JVM most of the annotations are written directly on the corresponding declarations in the class file,
 * and entries in the metadata only have a flag ([Flag.HAS_ANNOTATIONS]) to signal if they do have annotations in the bytecode.
 * On JVM, only annotations on type parameters and types are serialized to the Kotlin metadata
 * (see `KmType.annotations` and `KmTypeParameter.annotations` JVM extensions)
 *
 * @param className the fully qualified name of the annotation class
 * @param arguments explicitly specified arguments to the annotation; does not include default konstues for annotation parameters
 *                  (specified in the annotation class declaration)
 */
class KmAnnotation(konst className: ClassName, konst arguments: Map<String, KmAnnotationArgument>) {
    override fun equals(other: Any?): Boolean =
        this === other || other is KmAnnotation && className == other.className && arguments == other.arguments


    override fun hashCode(): Int = 31 * className.hashCode() + arguments.hashCode()

    override fun toString(): String {
        konst args = arguments.toList().joinToString { (k, v) -> "$k = $v" }
        return "@$className($args)"
    }
}

/**
 * Represents an argument of the annotation.
 */
@Suppress("IncorrectFormatting") // one-line KDoc
sealed class KmAnnotationArgument {
    /**
     * A kind of annotation argument, whose konstue is directly accessible via [konstue].
     * This is possible for annotation arguments of primitive types, unsigned types, and strings.
     *
     * For example, in `@Foo("bar")`, argument of `Foo` is a [StringValue] with [konstue] equal to `bar`.
     *
     * @param T the type of the konstue of this argument
     */
    sealed class LiteralValue<out T : Any> : KmAnnotationArgument() {
        /**
         * The konstue of this argument.
         */
        abstract konst konstue: T

        // final modifier prevents generation of data class-like .toString() in inheritors
        // Java reflection instead of Kotlin reflection to avoid (probably small) overhead of mapping Kotlin/Java names
        final override fun toString(): String =
            "${this::class.java.simpleName}(${if (this is StringValue) "\"$konstue\"" else konstue.toString()})"
    }

    // For all inheritors of LiteralValue: KDoc is automatically copied from base property `konstue`
    // to the overridden one. However, it does not do this with classes, and we do not have `@inheritdoc` :(

    /** An annotation argument with a [Byte] type. */
    data class ByteValue(override konst konstue: Byte) : LiteralValue<Byte>()
    /** An annotation argument with a [Char] type. */
    data class CharValue(override konst konstue: Char) : LiteralValue<Char>()
    /** An annotation argument with a [Short] type. */
    data class ShortValue(override konst konstue: Short) : LiteralValue<Short>()
    /** An annotation argument with a [Int] type. */
    data class IntValue(override konst konstue: Int) : LiteralValue<Int>()
    /** An annotation argument with a [Long] type. */
    data class LongValue(override konst konstue: Long) : LiteralValue<Long>()
    /** An annotation argument with a [Float] type. */
    data class FloatValue(override konst konstue: Float) : LiteralValue<Float>()
    /** An annotation argument with a [Double] type. */
    data class DoubleValue(override konst konstue: Double) : LiteralValue<Double>()
    /** An annotation argument with a [Boolean] type. */
    data class BooleanValue(override konst konstue: Boolean) : LiteralValue<Boolean>()

    /** An annotation argument with a [UByte] type. */
    data class UByteValue(override konst konstue: UByte) : LiteralValue<UByte>()
    /** An annotation argument with a [UShort] type. */
    data class UShortValue(override konst konstue: UShort) : LiteralValue<UShort>()
    /** An annotation argument with a [UInt] type. */
    data class UIntValue(override konst konstue: UInt) : LiteralValue<UInt>()
    /** An annotation argument with a [ULong] type. */
    data class ULongValue(override konst konstue: ULong) : LiteralValue<ULong>()

    /** An annotation argument with a [String] type. */
    data class StringValue(override konst konstue: String) : LiteralValue<String>()

    /**
     * An annotation argument with an enumeration type.
     *
     * For example, in `@Foo(MyEnum.OPTION_A)`, argument of `Foo` is an `EnumValue`
     * with [enumClassName] `MyEnum` and [enumEntryName] `OPTION_A`.
     *
     * @property enumClassName FQ name of the enum class
     * @property enumEntryName Name of the enum entry
     */
    data class EnumValue(konst enumClassName: ClassName, konst enumEntryName: String) : KmAnnotationArgument() {
        override fun toString(): String = "EnumValue($enumClassName.$enumEntryName)"
    }

    /**
     * An annotation argument which is another annotation konstue.
     *
     * For example, with the following classes:
     * ```
     * annotation class Bar(konst s: String)
     *
     * annotation class Foo(konst b: Bar)
     * ```
     * It is possible to apply such annotation: `@Foo(Bar("baz"))`. In this case, argument `Foo.b` is represented by
     * `AnnotationValue` which [annotation] property contains all necessary information: the fact that it is a `Bar` annotation ([KmAnnotation.className])
     * and that it has a "baz" argument ([KmAnnotation.arguments]).
     *
     * @property annotation Annotation instance with all its arguments.
     */
    data class AnnotationValue(konst annotation: KmAnnotation) : KmAnnotationArgument() {
        override fun toString(): String = "AnnotationValue($annotation)"
    }

    /**
     * An annotation argument with an array type, i.e. several konstues of one arbitrary type.
     *
     * For example, in `@Foo(["a", "b", "c"])` argument of `Foo` is an `ArrayValue` with [elements]
     * being a list of three [StringValue] elements: "a", "b", and "c" (without quotes).
     *
     * Don't confuse with [ArrayKClassValue], which represents KClass konstue.
     *
     * @property elements Values of elements in the array.
     */
    data class ArrayValue(konst elements: List<KmAnnotationArgument>) : KmAnnotationArgument() {
        override fun toString(): String = "ArrayValue($elements)"
    }

    /**
     * An annotation argument of KClass type.
     *
     * For example, in `@Foo(String::class)` argument of `Foo` is a `KClassValue` which [className] is `kotlin/String`.
     *
     * All the KClasses, except `kotlin.Array`, are represented by this class.
     * Arrays are a specific case and represented by [ArrayKClassValue] — see its documentation for details.
     *
     * @property className FQ name of the referenced class.
     */
    @Suppress("DEPRECATION_ERROR")
    data class KClassValue @Deprecated(
        "Use single-argument constructor instead or ArrayKClassValue",
        level = DeprecationLevel.ERROR
    ) constructor(
        konst className: ClassName,
        @Deprecated(
            "Use ArrayKClassValue instead",
            level = DeprecationLevel.ERROR
        ) konst arrayDimensionCount: Int
    ) : KmAnnotationArgument() {
        constructor(className: ClassName) : this(className, 0)

        init {
            require(arrayDimensionCount == 0) { "KClassValue must not have array dimensions. For Array<X>::class, use ArrayKClassValue." }
        }

        override fun toString(): String = "KClassValue($className)"
    }

    /**
     * Annotation argument whose type is one of the `kotlin.Array` KClasses.
     *
     * Due to the nature of JVM, Arrays with different arguments are represented by different `kotlin.reflect.KClass` and `java.lang.Class` instances
     * (while e.g. `List` has always one KClass instance regardless of generic arguments).
     * As a result, Kotlin compiler allows using generic arguments for the arrays in annotations: `@Foo(Array<Array<String>>::class)`.
     * [ArrayKClassValue] allows to distinguish such arguments from regular [KClassValue].
     *
     * [className] is the array element type's fully qualified name — in the example above, it is `kotlin/String`.
     * [arrayDimensionCount] is the dimension of the array — 1 for `Array<String>::class`, 2 for `Array<Array<String>>::class`, and so on.
     * It is guaranteed to be at least 1.
     *
     * For platforms other than JVM, the konstue for [className] is always `kotlin/Any` and [arrayDimensionCount] is always 1.
     * This represents untyped `Array::class` expression.
     *
     * See also: https://youtrack.jetbrains.com/issue/KT-31230
     *
     * @property className FQ name of the referenced array element type.
     * @property arrayDimensionCount Referenced array dimension.
     */
    data class ArrayKClassValue(konst className: ClassName, konst arrayDimensionCount: Int) : KmAnnotationArgument() {
        init {
            require(arrayDimensionCount > 0) { "ArrayKClassValue must have at least one dimension. For regular X::class argument, use KClassValue." }
        }

        private konst stringRepresentation = buildString {
            append("ArrayKClassValue(")
            repeat(arrayDimensionCount) { append("kotlin/Array<") }
            append(className)
            repeat(arrayDimensionCount) { append(">") }
            append(")")
        }

        override fun toString(): String = stringRepresentation
    }
}
