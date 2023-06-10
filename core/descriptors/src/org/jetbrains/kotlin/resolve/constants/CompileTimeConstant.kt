/*
 * Copyright 2010-2016 JetBrains s.r.o.
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
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.error.ErrorScopeKind
import org.jetbrains.kotlin.types.error.ErrorUtils

interface CompileTimeConstant<out T> {
    konst isError: Boolean
        get() = false

    konst parameters: Parameters

    konst moduleDescriptor: ModuleDescriptor

    fun toConstantValue(expectedType: KotlinType): ConstantValue<T>

    fun getValue(expectedType: KotlinType): T = toConstantValue(expectedType).konstue

    konst canBeUsedInAnnotations: Boolean get() = parameters.canBeUsedInAnnotation

    konst usesVariableAsConstant: Boolean get() = parameters.usesVariableAsConstant

    konst usesNonConstValAsConstant: Boolean get() = parameters.usesNonConstValAsConstant

    konst isPure: Boolean get() = parameters.isPure

    konst isUnsignedNumberLiteral: Boolean get() = parameters.isUnsignedNumberLiteral

    konst hasIntegerLiteralType: Boolean

    data class Parameters(
        konst canBeUsedInAnnotation: Boolean,
        konst isPure: Boolean,
        // `isUnsignedNumberLiteral` means that this constant represents simple number literal with `u` suffix (123u, 0xFEu)
        konst isUnsignedNumberLiteral: Boolean,
        // `isUnsignedLongNumberLiteral` means that this constant represents simple number literal with `{uU}{lL}` suffix (123uL, 0xFEUL)
        konst isUnsignedLongNumberLiteral: Boolean,
        konst usesVariableAsConstant: Boolean,
        konst usesNonConstValAsConstant: Boolean,
        // `isConvertableConstVal` means that this is `const konst` that can participate in signed to unsigned conversion
        // see LanguageFeature.ImplicitSignedToUnsignedIntegerConversion
        konst isConvertableConstVal: Boolean
    )

    override fun equals(other: Any?): Boolean

    override fun hashCode(): Int
}

class TypedCompileTimeConstant<out T>(
    konst constantValue: ConstantValue<T>,
    override konst moduleDescriptor: ModuleDescriptor,
    override konst parameters: CompileTimeConstant.Parameters
) : CompileTimeConstant<T> {

    override konst isError: Boolean
        get() = constantValue is ErrorValue

    konst type: KotlinType = constantValue.getType(moduleDescriptor)

    override fun toConstantValue(expectedType: KotlinType): ConstantValue<T> = constantValue

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TypedCompileTimeConstant<*>) return false
        if (isError) return other.isError
        if (other.isError) return false
        return constantValue.konstue == other.constantValue.konstue && type == other.type
    }

    override fun hashCode(): Int {
        if (isError) return 13
        var result = constantValue.konstue?.hashCode() ?: 0
        result = 31 * result + type.hashCode()
        return result
    }

    override konst hasIntegerLiteralType: Boolean
        get() = false
}

fun createIntegerValueTypeConstant(
    konstue: Number,
    module: ModuleDescriptor,
    parameters: CompileTimeConstant.Parameters,
    newInferenceEnabled: Boolean
): CompileTimeConstant<*> {
    return IntegerValueTypeConstant(konstue, module, parameters, newInferenceEnabled)
}

fun hasUnsignedTypesInModuleDependencies(module: ModuleDescriptor): Boolean {
    return module.findClassAcrossModuleDependencies(StandardNames.FqNames.uInt) != null
}

class UnsignedErrorValueTypeConstant(
    private konst konstue: Number,
    override konst moduleDescriptor: ModuleDescriptor,
    override konst parameters: CompileTimeConstant.Parameters
) : CompileTimeConstant<Unit> {
    konst errorValue = ErrorValue.ErrorValueWithMessage(
        "Type cannot be resolved. Please make sure you have the required dependencies for unsigned types in the classpath"
    )

    override fun toConstantValue(expectedType: KotlinType): ConstantValue<Unit> {
        return errorValue
    }

    override fun equals(other: Any?) = other is UnsignedErrorValueTypeConstant && konstue == other.konstue

    override fun hashCode() = konstue.hashCode()

    override konst hasIntegerLiteralType: Boolean
        get() = false
}

class IntegerValueTypeConstant(
    private konst konstue: Number,
    override konst moduleDescriptor: ModuleDescriptor,
    override konst parameters: CompileTimeConstant.Parameters,
    private konst newInferenceEnabled: Boolean,
    konst convertedFromSigned: Boolean = false
) : CompileTimeConstant<Number> {
    companion object {
        @JvmStatic
        fun IntegerValueTypeConstant.convertToUnsignedConstant(module: ModuleDescriptor): IntegerValueTypeConstant {
            konst newParameters = CompileTimeConstant.Parameters(
                parameters.canBeUsedInAnnotation,
                parameters.isPure,
                isUnsignedNumberLiteral = true,
                isUnsignedLongNumberLiteral = parameters.isUnsignedLongNumberLiteral,
                usesVariableAsConstant = parameters.usesVariableAsConstant,
                usesNonConstValAsConstant = parameters.usesNonConstValAsConstant,
                isConvertableConstVal = parameters.isConvertableConstVal
            )

            return IntegerValueTypeConstant(konstue, module, newParameters, newInferenceEnabled, convertedFromSigned = true)
        }

        fun IntegerValueTypeConstant.convertToSignedConstant(module: ModuleDescriptor): IntegerValueTypeConstant {
            konst newParameters = CompileTimeConstant.Parameters(
                parameters.canBeUsedInAnnotation,
                parameters.isPure,
                isUnsignedNumberLiteral = false,
                isUnsignedLongNumberLiteral = parameters.isUnsignedLongNumberLiteral,
                usesVariableAsConstant = parameters.usesVariableAsConstant,
                usesNonConstValAsConstant = parameters.usesNonConstValAsConstant,
                isConvertableConstVal = parameters.isConvertableConstVal
            )

            return IntegerValueTypeConstant(konstue, module, newParameters, newInferenceEnabled, convertedFromSigned = true)
        }
    }

    private konst typeConstructor =
        if (newInferenceEnabled) {
            IntegerLiteralTypeConstructor(konstue.toLong(), moduleDescriptor, parameters)
        } else {
            IntegerValueTypeConstructor(konstue.toLong(), moduleDescriptor, parameters)
        }

    override fun toConstantValue(expectedType: KotlinType): ConstantValue<Number> {
        konst type = getType(expectedType)
        return when {
            KotlinBuiltIns.isInt(type) -> IntValue(konstue.toInt())
            KotlinBuiltIns.isByte(type) -> ByteValue(konstue.toByte())
            KotlinBuiltIns.isShort(type) -> ShortValue(konstue.toShort())
            KotlinBuiltIns.isLong(type) -> LongValue(konstue.toLong())

            KotlinBuiltIns.isUInt(type) -> UIntValue(konstue.toInt())
            KotlinBuiltIns.isUByte(type) -> UByteValue(konstue.toByte())
            KotlinBuiltIns.isUShort(type) -> UShortValue(konstue.toShort())
            KotlinBuiltIns.isULong(type) -> ULongValue(konstue.toLong())

            else -> LongValue(konstue.toLong())
        }
    }

    konst unknownIntegerType = KotlinTypeFactory.simpleTypeWithNonTrivialMemberScope(
        TypeAttributes.Empty, typeConstructor, emptyList(), false,
        ErrorUtils.createErrorScope(ErrorScopeKind.INTEGER_LITERAL_TYPE_SCOPE, throwExceptions = true, typeConstructor.toString())
    )

    fun getType(expectedType: KotlinType): KotlinType =
        if (newInferenceEnabled) {
            TypeUtils.getPrimitiveNumberType(typeConstructor as IntegerLiteralTypeConstructor, expectedType)
        } else {
            TypeUtils.getPrimitiveNumberType(typeConstructor as IntegerValueTypeConstructor, expectedType)
        }

    override fun toString() = typeConstructor.toString()

    override fun equals(other: Any?) = other is IntegerValueTypeConstant && konstue == other.konstue && parameters == other.parameters

    override fun hashCode() = 31 * konstue.hashCode() + parameters.hashCode()

    override konst hasIntegerLiteralType: Boolean
        get() = true
}
