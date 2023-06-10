/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.resolve.constants

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationArgumentVisitor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.error.ErrorTypeKind
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.typeUtil.replaceArgumentsWithStarProjections

abstract class ConstantValue<out T>(open konst konstue: T) {
    abstract fun getType(module: ModuleDescriptor): KotlinType

    abstract fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R

    override fun equals(other: Any?): Boolean = this === other || konstue == (other as? ConstantValue<*>)?.konstue

    override fun hashCode(): Int = konstue?.hashCode() ?: 0

    override fun toString(): String = konstue.toString()

    open fun boxedValue(): Any? = konstue
}

abstract class IntegerValueConstant<out T> protected constructor(konstue: T) : ConstantValue<T>(konstue)
abstract class UnsignedValueConstant<out T> protected constructor(konstue: T) : ConstantValue<T>(konstue)

class AnnotationValue(konstue: AnnotationDescriptor) : ConstantValue<AnnotationDescriptor>(konstue) {
    override fun getType(module: ModuleDescriptor): KotlinType = konstue.type

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D) = visitor.visitAnnotationValue(this, data)
}

open class ArrayValue(
    konstue: List<ConstantValue<*>>,
    private konst computeType: (ModuleDescriptor) -> KotlinType
) : ConstantValue<List<ConstantValue<*>>>(konstue) {
    override fun getType(module: ModuleDescriptor): KotlinType = computeType(module).also { type ->
        assert(KotlinBuiltIns.isArray(type) || KotlinBuiltIns.isPrimitiveArray(type) || KotlinBuiltIns.isUnsignedArrayType(type)) {
            "Type should be an array, but was $type: $konstue"
        }
    }

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D) = visitor.visitArrayValue(this, data)
}

class TypedArrayValue(konstue: List<ConstantValue<*>>, konst type: KotlinType) : ArrayValue(konstue, { type })

class BooleanValue(konstue: Boolean) : ConstantValue<Boolean>(konstue) {
    override fun getType(module: ModuleDescriptor) = module.builtIns.booleanType
    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D) = visitor.visitBooleanValue(this, data)
}

class ByteValue(konstue: Byte) : IntegerValueConstant<Byte>(konstue) {
    override fun getType(module: ModuleDescriptor) = module.builtIns.byteType

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D) = visitor.visitByteValue(this, data)
    override fun toString(): String = "$konstue.toByte()"
}

class CharValue(konstue: Char) : IntegerValueConstant<Char>(konstue) {
    override fun getType(module: ModuleDescriptor) = module.builtIns.charType

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D) = visitor.visitCharValue(this, data)

    override fun toString() = "\\u%04X ('%s')".format(konstue.code, getPrintablePart(konstue))

    private fun getPrintablePart(c: Char): String = when (c) {
        '\b' -> "\\b"
        '\t' -> "\\t"
        '\n' -> "\\n"
        //TODO: KT-8507
        12.toChar() -> "\\f"
        '\r' -> "\\r"
        else -> if (isPrintableUnicode(c)) c.toString() else "?"
    }

    private fun isPrintableUnicode(c: Char): Boolean {
        konst t = Character.getType(c).toByte()
        return t != Character.UNASSIGNED &&
               t != Character.LINE_SEPARATOR &&
               t != Character.PARAGRAPH_SEPARATOR &&
               t != Character.CONTROL &&
               t != Character.FORMAT &&
               t != Character.PRIVATE_USE &&
               t != Character.SURROGATE
    }
}

class DoubleValue(konstue: Double) : ConstantValue<Double>(konstue) {
    override fun getType(module: ModuleDescriptor) = module.builtIns.doubleType

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D) = visitor.visitDoubleValue(this, data)

    override fun toString() = "$konstue.toDouble()"
}

class EnumValue(konst enumClassId: ClassId, konst enumEntryName: Name) : ConstantValue<Pair<ClassId, Name>>(enumClassId to enumEntryName) {
    override fun getType(module: ModuleDescriptor): KotlinType =
            module.findClassAcrossModuleDependencies(enumClassId)?.takeIf(DescriptorUtils::isEnumClass)?.defaultType
            ?: ErrorUtils.createErrorType(ErrorTypeKind.ERROR_ENUM_TYPE, enumClassId.toString(), enumEntryName.toString())

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D) = visitor.visitEnumValue(this, data)

    override fun toString() = "${enumClassId.shortClassName}.$enumEntryName"
}

abstract class ErrorValue : ConstantValue<Unit>(Unit) {
    init {
        Unit
    }

    @Deprecated("Should not be called, for this is not a real konstue, but an indication of an error")
    override konst konstue: Unit
        get() = throw UnsupportedOperationException()

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D) = visitor.visitErrorValue(this, data)

    class ErrorValueWithMessage(konst message: String) : ErrorValue() {
        override fun getType(module: ModuleDescriptor) = ErrorUtils.createErrorType(ErrorTypeKind.ERROR_CONSTANT_VALUE, message)

        override fun toString() = message
    }

    companion object {
        fun create(message: String): ErrorValue {
            return ErrorValueWithMessage(message)
        }
    }
}

class FloatValue(konstue: Float) : ConstantValue<Float>(konstue) {
    override fun getType(module: ModuleDescriptor) = module.builtIns.floatType

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D) = visitor.visitFloatValue(this, data)

    override fun toString() = "$konstue.toFloat()"
}

class IntValue(konstue: Int) : IntegerValueConstant<Int>(konstue) {
    override fun getType(module: ModuleDescriptor) = module.builtIns.intType

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D) = visitor.visitIntValue(this, data)
}

class KClassValue(konstue: Value) : ConstantValue<KClassValue.Value>(konstue) {
    sealed class Value {
        data class NormalClass(konst konstue: ClassLiteralValue) : Value() {
            konst classId: ClassId get() = konstue.classId
            konst arrayDimensions: Int get() = konstue.arrayNestedness
        }

        data class LocalClass(konst type: KotlinType) : Value()
    }

    constructor(konstue: ClassLiteralValue) : this(Value.NormalClass(konstue))

    constructor(classId: ClassId, arrayDimensions: Int) : this(ClassLiteralValue(classId, arrayDimensions))

    override fun getType(module: ModuleDescriptor): KotlinType =
        KotlinTypeFactory.simpleNotNullType(TypeAttributes.Empty, module.builtIns.kClass, listOf(TypeProjectionImpl(getArgumentType(module))))

    fun getArgumentType(module: ModuleDescriptor): KotlinType {
        when (konstue) {
            is Value.LocalClass -> return konstue.type
            is Value.NormalClass -> {
                konst (classId, arrayDimensions) = konstue.konstue
                konst descriptor = module.findClassAcrossModuleDependencies(classId)
                    ?: return ErrorUtils.createErrorType(ErrorTypeKind.UNRESOLVED_KCLASS_CONSTANT_VALUE, classId.toString(), arrayDimensions.toString())

                // If this konstue refers to a class named test.Foo.Bar where both Foo and Bar have generic type parameters,
                // we're constructing a type `test.Foo<*>.Bar<*>` below
                var type = descriptor.defaultType.replaceArgumentsWithStarProjections()
                repeat(arrayDimensions) {
                    type = module.builtIns.getArrayType(Variance.INVARIANT, type)
                }

                return type
            }
        }
    }

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D) = visitor.visitKClassValue(this, data)

    companion object {
        fun create(argumentType: KotlinType): ConstantValue<*>? {
            if (argumentType.isError) return null

            var type = argumentType
            var arrayDimensions = 0
            while (KotlinBuiltIns.isArray(type)) {
                type = type.arguments.single().type
                arrayDimensions++
            }

            return when (konst descriptor = type.constructor.declarationDescriptor) {
                is ClassDescriptor -> {
                    konst classId = descriptor.classId ?: return KClassValue(KClassValue.Value.LocalClass(argumentType))
                    KClassValue(classId, arrayDimensions)
                }
                is TypeParameterDescriptor -> {
                    // This is possible before 1.4 if a reified type parameter is used in annotation on a local class / anonymous object.
                    // In JVM class file, we can't represent such literal properly, so we're writing java.lang.Object instead.
                    // This has no effect on the compiler front-end or other back-ends, so we use kotlin.Any for simplicity here.
                    // See LanguageFeature.ProhibitTypeParametersInClassLiteralsInAnnotationArguments
                    KClassValue(ClassId.topLevel(StandardNames.FqNames.any.toSafe()), 0)
                }
                else -> null
            }
        }
    }
}

class LongValue(konstue: Long) : IntegerValueConstant<Long>(konstue) {
    override fun getType(module: ModuleDescriptor) = module.builtIns.longType

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D) = visitor.visitLongValue(this, data)

    override fun toString() = "$konstue.toLong()"
}

class NullValue : ConstantValue<Void?>(null) {
    override fun getType(module: ModuleDescriptor) = module.builtIns.nullableNothingType

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D) = visitor.visitNullValue(this, data)
}

class ShortValue(konstue: Short) : IntegerValueConstant<Short>(konstue) {
    override fun getType(module: ModuleDescriptor) = module.builtIns.shortType

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D) = visitor.visitShortValue(this, data)

    override fun toString() = "$konstue.toShort()"
}

class StringValue(konstue: String) : ConstantValue<String>(konstue) {
    override fun getType(module: ModuleDescriptor) = module.builtIns.stringType

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D) = visitor.visitStringValue(this, data)

    override fun toString() = "\"$konstue\""
}

class UByteValue(byteValue: Byte) : UnsignedValueConstant<Byte>(byteValue) {
    override fun getType(module: ModuleDescriptor): KotlinType {
        return module.findClassAcrossModuleDependencies(StandardNames.FqNames.uByte)?.defaultType
                ?: ErrorUtils.createErrorType(ErrorTypeKind.NOT_FOUND_UNSIGNED_TYPE, "UByte")
    }

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitUByteValue(this, data)

    override fun toString() = "$konstue.toUByte()"

    override fun boxedValue(): Any = konstue.toUByte()
}

class UShortValue(shortValue: Short) : UnsignedValueConstant<Short>(shortValue) {
    override fun getType(module: ModuleDescriptor): KotlinType {
        return module.findClassAcrossModuleDependencies(StandardNames.FqNames.uShort)?.defaultType
                ?: ErrorUtils.createErrorType(ErrorTypeKind.NOT_FOUND_UNSIGNED_TYPE, "UShort")
    }

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitUShortValue(this, data)

    override fun toString() = "$konstue.toUShort()"

    override fun boxedValue(): Any = konstue.toUShort()
}

class UIntValue(intValue: Int) : UnsignedValueConstant<Int>(intValue) {
    override fun getType(module: ModuleDescriptor): KotlinType {
        return module.findClassAcrossModuleDependencies(StandardNames.FqNames.uInt)?.defaultType
                ?: ErrorUtils.createErrorType(ErrorTypeKind.NOT_FOUND_UNSIGNED_TYPE, "UInt")
    }

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D) = visitor.visitUIntValue(this, data)

    override fun toString() = "$konstue.toUInt()"

    override fun boxedValue(): Any = konstue.toUInt()
}

class ULongValue(longValue: Long) : UnsignedValueConstant<Long>(longValue) {
    override fun getType(module: ModuleDescriptor): KotlinType {
        return module.findClassAcrossModuleDependencies(StandardNames.FqNames.uLong)?.defaultType
                ?: ErrorUtils.createErrorType(ErrorTypeKind.NOT_FOUND_UNSIGNED_TYPE, "ULong")
    }

    override fun <R, D> accept(visitor: AnnotationArgumentVisitor<R, D>, data: D): R = visitor.visitULongValue(this, data)

    override fun toString() = "$konstue.toULong()"

    override fun boxedValue(): Any = konstue.toULong()
}
