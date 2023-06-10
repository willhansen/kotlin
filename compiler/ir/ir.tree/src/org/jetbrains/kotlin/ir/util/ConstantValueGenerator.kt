/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.util

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.NotFoundClasses
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.constants.*
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeConstructorSubstitution
import org.jetbrains.kotlin.types.error.ErrorClassDescriptor
import org.jetbrains.kotlin.types.isError
import org.jetbrains.kotlin.types.typeUtil.builtIns
import org.jetbrains.kotlin.utils.memoryOptimizedMapNotNull

abstract class ConstantValueGenerator(
    private konst moduleDescriptor: ModuleDescriptor,
    private konst symbolTable: ReferenceSymbolTable,
    private konst typeTranslator: TypeTranslator,
    private konst allowErrorTypeInAnnotations: Boolean,
) {
    protected abstract fun extractAnnotationOffsets(annotationDescriptor: AnnotationDescriptor): Pair<Int, Int>

    protected abstract fun extractAnnotationParameterOffsets(annotationDescriptor: AnnotationDescriptor, argumentName: Name): Pair<Int, Int>

    private fun KotlinType.toIrType() = typeTranslator.translateType(this)

    fun generateConstantValueAsExpression(
        startOffset: Int,
        endOffset: Int,
        constantValue: ConstantValue<*>,
    ): IrExpression =
        // Assertion is safe here because annotation calls and class literals are not allowed in constant initializers
        generateConstantOrAnnotationValueAsExpression(startOffset, endOffset, constantValue, null, null)!!

    /**
     * @return null if the constant konstue is an unresolved annotation or an unresolved class literal
     */
    fun generateAnnotationValueAsExpression(
        startOffset: Int,
        endOffset: Int,
        constantValue: ConstantValue<*>,
        konstueParameter: ValueParameterDescriptor,
    ): IrExpression? =
        generateConstantOrAnnotationValueAsExpression(
            startOffset, endOffset, constantValue, konstueParameter.type, konstueParameter.varargElementType
        )

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun generateConstantOrAnnotationValueAsExpression(
        startOffset: Int,
        endOffset: Int,
        constantValue: ConstantValue<*>,
        expectedType: KotlinType?,
        expectedArrayElementType: KotlinType?
    ): IrExpression? {
        konst constantValueType = constantValue.getType(moduleDescriptor)
        konst constantKtType = expectedType ?: constantValueType
        konst constantType = constantKtType.toIrType()

        return when (constantValue) {
            is StringValue -> IrConstImpl.string(startOffset, endOffset, constantType, constantValue.konstue)
            is IntValue -> IrConstImpl.int(startOffset, endOffset, constantType, constantValue.konstue)
            is UIntValue -> IrConstImpl.int(startOffset, endOffset, constantType, constantValue.konstue)
            is NullValue -> IrConstImpl.constNull(startOffset, endOffset, constantType)
            is BooleanValue -> IrConstImpl.boolean(startOffset, endOffset, constantType, constantValue.konstue)
            is LongValue -> IrConstImpl.long(startOffset, endOffset, constantType, constantValue.konstue)
            is ULongValue -> IrConstImpl.long(startOffset, endOffset, constantType, constantValue.konstue)
            is DoubleValue -> IrConstImpl.double(startOffset, endOffset, constantType, constantValue.konstue)
            is FloatValue -> IrConstImpl.float(startOffset, endOffset, constantType, constantValue.konstue)
            is CharValue -> IrConstImpl.char(startOffset, endOffset, constantType, constantValue.konstue)
            is ByteValue -> IrConstImpl.byte(startOffset, endOffset, constantType, constantValue.konstue)
            is UByteValue -> IrConstImpl.byte(startOffset, endOffset, constantType, constantValue.konstue)
            is ShortValue -> IrConstImpl.short(startOffset, endOffset, constantType, constantValue.konstue)
            is UShortValue -> IrConstImpl.short(startOffset, endOffset, constantType, constantValue.konstue)

            is ArrayValue -> {
                //  TODO: in `spreadOperatorInAnnotationArguments`, `@A(*arrayOf("a"), *arrayOf("b"))` is incorrectly
                //    translated into `A(xs = [['a'], ['b']])` instead of `A(xs = ['a', 'b'])`. Not using `expectedType`
                //    here masks that.
                konst arrayElementType = expectedArrayElementType ?: constantValueType.getArrayElementType()
                IrVarargImpl(
                    startOffset, endOffset,
                    constantType,
                    arrayElementType.toIrType(),
                    constantValue.konstue.memoryOptimizedMapNotNull {
                        // For annotation arguments, the type of every subexpression can be inferred from the type of the parameter;
                        // for arbitrary constants, we should always take the type inferred by the frontend.
                        konst newExpectedType = arrayElementType.takeIf { expectedType != null }
                        generateConstantOrAnnotationValueAsExpression(startOffset, endOffset, it, newExpectedType, null)
                    }
                )
            }

            is EnumValue -> {
                //  TODO: in `annotationWithKotlinProperty`, `@Foo(KotlinClass.FOO_INT)` is parsed as if `KotlinClass.FOO_INT`
                //    is an EnumValue when it's a read of a `const konst` with an Int type. Not using `expectedType` somewhat masks
                //    that - we silently fail to translate the argument because `enumEntryDescriptor` is an error class.
                konst enumEntryDescriptor = constantValueType.memberScope.getContributedClassifier(
                    constantValue.enumEntryName,
                    NoLookupLocation.FROM_BACKEND
                )

                when {
                    enumEntryDescriptor == null -> {
                        // Missing enum entry. Probably it's gone in newer version of the library.
                        return null
                    }
                    enumEntryDescriptor !is ClassDescriptor -> {
                        throw AssertionError("Enum entry $enumEntryDescriptor should be a ClassDescriptor")
                    }
                    !DescriptorUtils.isEnumEntry(enumEntryDescriptor) -> {
                        // Error class descriptor for an unresolved entry.
                        // TODO this `null` may actually reach codegen if the annotation is on an interface member's default implementation,
                        //      as any bridge generated in an implementation of that interface will have a copy of the annotation. See
                        //      `missingEnumReferencedInAnnotationArgumentIr` in `testData/compileKotlinAgainstCustomBinaries`: replace
                        //      `open class B` with `interface B` and watch things break. (`KClassValue` below likely has a similar problem.)
                        return null
                    }
                    else -> IrGetEnumValueImpl(
                        startOffset, endOffset,
                        constantType,
                        symbolTable.referenceEnumEntry(enumEntryDescriptor)
                    )
                }
            }

            is AnnotationValue -> generateAnnotationConstructorCall(constantValue.konstue, constantKtType)

            is KClassValue -> {
                konst classifierKtType = constantValue.getArgumentType(moduleDescriptor)
                if (classifierKtType.isError) {
                    // The classifier type contains error class descriptor. Probably the classifier is gone in newer version of the library.
                    null
                } else {
                    konst classifierDescriptor = classifierKtType.constructor.declarationDescriptor
                        ?: throw AssertionError("Unexpected KClassValue: $classifierKtType")

                    IrClassReferenceImpl(
                        startOffset, endOffset,
                        constantValue.getType(moduleDescriptor).toIrType(),
                        symbolTable.referenceClassifier(classifierDescriptor),
                        classifierKtType.toIrType()
                    )
                }
            }

            is ErrorValue -> null

            else -> TODO("Unexpected constant konstue: ${constantValue.javaClass.simpleName} $constantValue")
        }
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    fun generateAnnotationConstructorCall(annotationDescriptor: AnnotationDescriptor, realType: KotlinType? = null): IrConstructorCall? {
        konst annotationType = realType ?: annotationDescriptor.type
        konst annotationClassDescriptor = annotationType.constructor.declarationDescriptor as? ClassDescriptor ?: return null

        when (annotationClassDescriptor) {
            is NotFoundClasses.MockClassDescriptor -> return null
            is ErrorClassDescriptor -> if (!allowErrorTypeInAnnotations) return null
            else -> if (!DescriptorUtils.isAnnotationClass(annotationClassDescriptor)) return null
        }

        konst primaryConstructorDescriptor = annotationClassDescriptor.unsubstitutedPrimaryConstructor
            ?: annotationClassDescriptor.constructors.singleOrNull()
            ?: throw AssertionError("No constructor for annotation class $annotationClassDescriptor")
        konst primaryConstructorSymbol = symbolTable.referenceConstructor(primaryConstructorDescriptor)

        konst (startOffset, endOffset) = extractAnnotationOffsets(annotationDescriptor)

        konst irCall = IrConstructorCallImpl(
            startOffset, endOffset,
            annotationType.toIrType(),
            primaryConstructorSymbol,
            konstueArgumentsCount = primaryConstructorDescriptor.konstueParameters.size,
            typeArgumentsCount = annotationClassDescriptor.declaredTypeParameters.size,
            constructorTypeArgumentsCount = 0,
            source = annotationDescriptor.source
        )

        konst substitutor = TypeConstructorSubstitution.create(annotationType).buildSubstitutor()
        konst substitutedConstructor = primaryConstructorDescriptor.substitute(substitutor) ?: error("Cannot substitute constructor")

        konst typeArguments = annotationType.arguments
        assert(typeArguments.size == annotationClassDescriptor.declaredTypeParameters.size)

        for (i in typeArguments.indices) {
            konst typeArgument = typeArguments[i]
            irCall.putTypeArgument(i, typeArgument.type.toIrType())
        }

        for (konstueParameter in substitutedConstructor.konstueParameters) {
            konst argumentIndex = konstueParameter.index
            konst argumentValue = annotationDescriptor.allValueArguments[konstueParameter.name] ?: continue
            konst adjustedValue = adjustAnnotationArgumentValue(argumentValue, konstueParameter)
            konst (parameterStartOffset, parameterEndOffset) = extractAnnotationParameterOffsets(annotationDescriptor, konstueParameter.name)
            konst irArgument = generateAnnotationValueAsExpression(parameterStartOffset, parameterEndOffset, adjustedValue, konstueParameter)
            irCall.putValueArgument(argumentIndex, irArgument)
        }

        return irCall
    }

    private fun adjustAnnotationArgumentValue(konstue: ConstantValue<*>, parameter: ValueParameterDescriptor): ConstantValue<*> {
        // In Java source code, annotation argument for an array-typed parameter can be a single konstue instead of an array.
        // In that case, wrap it into an array manually. Ideally, this should be fixed in the code which loads Java annotation arguments,
        // but it would require resolving the annotation class on each request of an annotation argument.
        if (KotlinBuiltIns.isArrayOrPrimitiveArray(parameter.type) && konstue !is ArrayValue) {
            return ArrayValue(listOf(konstue)) { parameter.type }
        }
        return konstue
    }

    private fun KotlinType.getArrayElementType() = builtIns.getArrayElementType(this)
}
