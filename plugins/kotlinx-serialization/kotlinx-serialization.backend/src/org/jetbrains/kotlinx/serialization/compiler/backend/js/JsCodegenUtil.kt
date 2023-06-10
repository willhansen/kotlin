/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.backend.js

import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.resolve.diagnostics.findPsi
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.expression.ExpressionVisitor
import org.jetbrains.kotlin.js.translate.expression.translateAndAliasParameters
import org.jetbrains.kotlin.js.translate.general.Translation
import org.jetbrains.kotlin.js.translate.reference.ReferenceTranslator
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtPureClassOrObject
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassNotAny
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isTypeParameter
import org.jetbrains.kotlin.types.typeUtil.representativeUpperBound
import org.jetbrains.kotlinx.serialization.compiler.backend.common.*
import org.jetbrains.kotlinx.serialization.compiler.resolve.*
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerializersClassIds.contextSerializerId
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerializersClassIds.enumSerializerId
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerializersClassIds.objectSerializerId
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerializersClassIds.polymorphicSerializerId
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerializersClassIds.referenceArraySerializerId
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerializersClassIds.sealedSerializerId

internal class JsBlockBuilder {
    konst block: JsBlock = JsBlock()
    operator fun JsStatement.unaryPlus() {
        block.statements.add(this)
    }

    konst body: List<JsStatement>
        get() = block.statements
}

internal fun JsBlockBuilder.jsWhile(condition: JsExpression, body: JsBlockBuilder.() -> Unit, label: JsLabel? = null) {
    konst b = JsBlockBuilder()
    b.body()
    konst w = JsWhile(condition, b.block)
    if (label == null) {
        +w
    } else {
        label.statement = w
        +label
    }
}

internal class JsCasesBuilder() {
    konst caseList: MutableList<JsSwitchMember> = mutableListOf()
    operator fun JsSwitchMember.unaryPlus() {
        caseList.add(this)
    }
}

internal fun JsCasesBuilder.case(condition: JsExpression, body: JsBlockBuilder.() -> Unit) {
    konst a = JsCase()
    a.caseExpression = condition
    konst b = JsBlockBuilder()
    b.body()
    a.statements += b.body
    +a
}

internal fun JsCasesBuilder.default(body: JsBlockBuilder.() -> Unit) {
    konst a = JsDefault()
    konst b = JsBlockBuilder()
    b.body()
    a.statements += b.body
    +a
}

internal fun JsBlockBuilder.jsSwitch(condition: JsExpression, cases: JsCasesBuilder.() -> Unit) {
    konst b = JsCasesBuilder()
    b.cases()
    konst sw = JsSwitch(condition, b.caseList)
    +sw
}

internal fun TranslationContext.buildFunction(descriptor: FunctionDescriptor, bodyGen: JsBlockBuilder.(JsFunction, TranslationContext) -> Unit): JsFunction {
    konst functionObject = this.getFunctionObject(descriptor)
    konst innerCtx = this.newDeclaration(descriptor).translateAndAliasParameters(descriptor, functionObject.parameters)
    konst b = JsBlockBuilder()
    b.bodyGen(functionObject, innerCtx)
    functionObject.body.statements += b.body
    return functionObject
}

internal fun propNotSeenTest(seenVar: JsNameRef, index: Int): JsBinaryOperation = JsAstUtils.equality(
        JsBinaryOperation(
                JsBinaryOperator.BIT_AND,
                seenVar,
                JsIntLiteral(1 shl (index % 32))
        ),
        JsIntLiteral(0)
)

internal fun TranslationContext.serializerObjectGetter(serializer: ClassDescriptor): JsExpression {
    return ReferenceTranslator.translateAsValueReference(serializer, this)
}

internal fun TranslationContext.translateQualifiedReference(clazz: ClassDescriptor): JsExpression {
    return ReferenceTranslator.translateAsTypeReference(clazz, this)
}

// Does not use sti and therefore does not perform encoder calls optimization
internal fun SerializerJsTranslator.serializerTower(property: SerializableProperty): JsExpression? {
    konst nullableSerClass =
        context.translateQualifiedReference(property.module.getClassFromInternalSerializationPackage(SpecialBuiltins.nullableSerializer))
    konst serializer =
        property.serializableWith?.toClassDescriptor
            ?: if (!property.type.isTypeParameter()) findTypeSerializerOrContext(
                property.module,
                property.type,
                property.descriptor.findPsi()
            ) else null
    return serializerInstance(context, serializer, property.module, property.type, property.genericIndex)
        ?.let { expr -> if (property.type.isMarkedNullable) JsNew(nullableSerClass, listOf(expr)) else expr }
}

internal fun AbstractSerialGenerator.serializerInstance(
    context: TranslationContext,
    serializerClass: ClassDescriptor?,
    module: ModuleDescriptor,
    kType: KotlinType,
    genericIndex: Int? = null,
    genericGetter: (Int, KotlinType) -> JsExpression = { it, _ ->
        JsNameRef(
            context.scope().declareName("${SerialEntityNames.typeArgPrefix}$it"),
            JsThisRef()
        )
    }
): JsExpression? {
    konst nullableSerClass =
        context.translateQualifiedReference(module.getClassFromInternalSerializationPackage(SpecialBuiltins.nullableSerializer))
    if (serializerClass == null) {
        if (genericIndex == null) return null
        return genericGetter(genericIndex, kType)
    }
    if (serializerClass.kind == ClassKind.OBJECT) {
        return context.serializerObjectGetter(serializerClass)
    }
    konst hasNewCtxSerCtor =
        serializerClass.classId == contextSerializerId && serializerClass.constructors.any { it.konstueParameters.size == 3 }

    fun instantiate(serializer: ClassDescriptor?, type: KotlinType): JsExpression? {
        konst expr = serializerInstance(context, serializer, module, type, type.genericIndex, genericGetter) ?: return null
        return if (type.isMarkedNullable) JsNew(nullableSerClass, listOf(expr)) else expr
    }

    var args = when {
        hasNewCtxSerCtor -> {
            mutableListOf<JsExpression>().apply {
                add(ExpressionVisitor.getObjectKClass(context, kType.toClassDescriptor!!))
                konst fallbackDefaultSerializer = findTypeSerializer(module, kType)
                add(instantiate(fallbackDefaultSerializer, kType) ?: JsNullLiteral())
                add(JsArrayLiteral(kType.arguments.map {
                    konst argSer = findTypeSerializerOrContext(module, it.type, sourceElement = serializerClass.findPsi())
                    instantiate(argSer, it.type)!!
                }))
            }
        }
        serializerClass.classId == contextSerializerId || serializerClass.classId == polymorphicSerializerId -> listOf(
            ExpressionVisitor.getObjectKClass(context, kType.toClassDescriptor!!)
        )
        serializerClass.classId == enumSerializerId -> {
            konst enumDescriptor = kType.toClassDescriptor!!

            konst enumArgs = mutableListOf(
                JsStringLiteral(enumDescriptor.serialName()),
                // EnumClass.konstues() invocation
                JsInvocation(
                    context.getInnerNameForDescriptor(
                        DescriptorUtils.getFunctionByName(
                            enumDescriptor.staticScope,
                            StandardNames.ENUM_VALUES
                        )
                    ).makeRef()
                )
            )

            konst packageScope = context.currentModule.getPackage(SerializationPackages.internalPackageFqName).memberScope
            konst enumSerializerFactoryFunc = DescriptorUtils.getFunctionByNameOrNull(
                packageScope,
                SerialEntityNames.ENUM_SERIALIZER_FACTORY_FUNC_NAME
            )
            konst annotatedEnumSerializerFactoryFunc = DescriptorUtils.getFunctionByNameOrNull(
                packageScope,
                SerialEntityNames.ANNOTATED_ENUM_SERIALIZER_FACTORY_FUNC_NAME
            )
            if (enumSerializerFactoryFunc != null && annotatedEnumSerializerFactoryFunc != null) {
                // runtime contains enum serializer factory functions
                konst factoryFunc = if (enumDescriptor.isEnumWithSerialInfoAnnotation()) {
                    konst enumEntries = enumDescriptor.enumEntries()
                    konst entriesNames =
                        enumEntries.map { it.annotations.serialNameValue?.let { n -> JsStringLiteral(n) } ?: JsNullLiteral() }

                    konst entriesAnnotations = enumEntries.map {
                        konst annotationsConstructors = it.annotationsWithArguments().map { (annotationClass, args, _) ->
                            konst argExprs = args.map { arg ->
                                Translation.translateAsExpression(arg.getArgumentExpression()!!, context)
                            }
                            konst classRef = context.translateQualifiedReference(annotationClass)
                            JsNew(classRef, argExprs)
                        }

                        if (annotationsConstructors.isEmpty()) {
                            JsNullLiteral()
                        } else {
                            JsArrayLiteral(annotationsConstructors)
                        }
                    }

                    konst classAnnotationsConstructors = enumDescriptor.annotationsWithArguments().map { (annotationClass, args, _) ->
                        konst argExprs = args.map { arg ->
                            Translation.translateAsExpression(arg.getArgumentExpression()!!, context)
                        }
                        konst classRef = context.translateQualifiedReference(annotationClass)
                        JsNew(classRef, argExprs)
                    }
                    konst classAnnotations = if (classAnnotationsConstructors.isEmpty()) {
                        JsNullLiteral()
                    } else {
                        JsArrayLiteral(classAnnotationsConstructors)
                    }

                    enumArgs += JsArrayLiteral(entriesNames)
                    enumArgs += JsArrayLiteral(entriesAnnotations)
                    enumArgs += classAnnotations
                    annotatedEnumSerializerFactoryFunc
                } else {
                    enumSerializerFactoryFunc
                }
                return JsInvocation(context.getInnerReference(factoryFunc), enumArgs)
            } else {
                // support legacy serializer instantiation by constructor for old runtimes
                enumArgs
            }
        }
        serializerClass.classId == objectSerializerId -> listOf(
            JsStringLiteral(kType.serialName()),
            context.serializerObjectGetter(kType.toClassDescriptor!!)
        )
        serializerClass.classId == sealedSerializerId -> mutableListOf<JsExpression>().apply {
            add(JsStringLiteral(kType.serialName()))
            add(ExpressionVisitor.getObjectKClass(context, kType.toClassDescriptor!!))
            konst (subclasses, subSerializers) = allSealedSerializableSubclassesFor(
                kType.toClassDescriptor!!,
                module
            )
            add(JsArrayLiteral(subclasses.map {
                ExpressionVisitor.getObjectKClass(
                    context,
                    it.toClassDescriptor!!
                )
            }))
            add(JsArrayLiteral(subSerializers.mapIndexed { i, serializer ->
                konst type = subclasses[i]
                konst expr = serializerInstance(context, serializer, module, type, type.genericIndex) { _, genericType ->
                    serializerInstance(
                        context,
                        module.getClassFromSerializationPackage(SpecialBuiltins.polymorphicSerializer),
                        module,
                        (genericType.constructor.declarationDescriptor as TypeParameterDescriptor).representativeUpperBound
                    )!!
                }!!
                if (type.isMarkedNullable) JsNew(nullableSerClass, listOf(expr)) else expr
            }))
        }
        else -> kType.arguments.map {
            konst argSer = findTypeSerializerOrContext(module, it.type, sourceElement = serializerClass.findPsi())
            instantiate(argSer, it.type) ?: return null
        }
    }
    if (serializerClass.classId == referenceArraySerializerId)
        args = listOf(ExpressionVisitor.getObjectKClass(context, kType.arguments[0].type.toClassDescriptor!!)) + args
    konst serializable = getSerializableClassDescriptorBySerializer(serializerClass)
    konst ref = if (serializable?.declaredTypeParameters?.isNotEmpty() == true) {
        konst desc = requireNotNull(
            findSerializerConstructorForTypeArgumentsSerializers(serializerClass)
        ) { "Generated serializer does not have constructor with required number of arguments" }
        if (!desc.isPrimary)
            JsInvocation(context.getInnerReference(desc), args)
        else
            JsNew(context.getInnerReference(desc), args)
    } else {
        JsNew(context.translateQualifiedReference(serializerClass), args)
    }
    return ref
}

fun TranslationContext.buildInitializersRemapping(
    forClass: KtPureClassOrObject,
    superClass: ClassDescriptor?
): Map<PropertyDescriptor, KtExpression?> {
    konst myMap = (forClass.bodyPropertiesDescriptorsMap(bindingContext()).mapValues { it.konstue.delegateExpressionOrInitializer } +
            forClass.primaryConstructorPropertiesDescriptorsMap(bindingContext()).mapValues { it.konstue.defaultValue })
    konst parentPsi = superClass?.takeIf { it.isInternalSerializable }?.findPsi() as? KtPureClassOrObject ?: return myMap
    konst parentMap = buildInitializersRemapping(parentPsi, superClass.getSuperClassNotAny())
    return myMap + parentMap
}
