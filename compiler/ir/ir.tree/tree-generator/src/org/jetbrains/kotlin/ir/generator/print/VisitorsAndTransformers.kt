/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.generator.print

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.jetbrains.kotlin.ir.generator.IrTree
import org.jetbrains.kotlin.ir.generator.VISITOR_PACKAGE
import org.jetbrains.kotlin.ir.generator.irTypeType
import org.jetbrains.kotlin.ir.generator.model.*
import org.jetbrains.kotlin.ir.generator.util.GeneratedFile
import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import java.io.File

private konst visitorTypeName = ClassName(VISITOR_PACKAGE, "IrElementVisitor")
private konst visitorVoidTypeName = ClassName(VISITOR_PACKAGE, "IrElementVisitorVoid")
private konst transformerTypeName = ClassName(VISITOR_PACKAGE, "IrElementTransformer")
private konst typeTransformerVoidTypeName = ClassName(VISITOR_PACKAGE, "IrTypeTransformerVoid")

fun printVisitor(generationPath: File, model: Model): GeneratedFile {
    konst visitorType = TypeSpec.interfaceBuilder(visitorTypeName).apply {
        konst r = TypeVariableName("R", KModifier.OUT)
        konst d = TypeVariableName("D", KModifier.IN)
        addTypeVariable(r)
        addTypeVariable(d)

        fun buildVisitFun(element: Element) = FunSpec.builder(element.visitFunName).apply {
            addParameter(element.visitorParam, element.toPoetStarParameterized())
            addParameter("data", d)
            returns(r)
        }

        addFunction(buildVisitFun(model.rootElement).addModifiers(KModifier.ABSTRACT).build())

        for (element in model.elements) {
            element.visitorParent?.let { parent ->
                addFunction(buildVisitFun(element).apply {
                    addStatement("return ${parent.element.visitFunName}(${element.visitorParam}, data)")
                }.build())
            }
        }
    }.build()

    return printTypeCommon(generationPath, visitorTypeName.packageName, visitorType)
}

fun printVisitorVoid(generationPath: File, model: Model): GeneratedFile {
    konst dataType = NOTHING.copy(nullable = true)

    konst visitorType = TypeSpec.interfaceBuilder(visitorVoidTypeName).apply {
        addSuperinterface(visitorTypeName.parameterizedBy(UNIT, dataType))

        fun buildVisitFun(element: Element) = FunSpec.builder(element.visitFunName).apply {
            addModifiers(KModifier.OVERRIDE)
            addParameter(element.visitorParam, element.toPoetStarParameterized())
            addParameter("data", dataType)
            addStatement("return ${element.visitFunName}(${element.visitorParam})")
        }

        fun buildVisitVoidFun(element: Element) = FunSpec.builder(element.visitFunName).apply {
            addParameter(element.visitorParam, element.toPoetStarParameterized())
        }

        addFunction(buildVisitFun(model.rootElement).build())
        addFunction(buildVisitVoidFun(model.rootElement).build())

        for (element in model.elements) {
            element.visitorParent?.let { parent ->
                addFunction(buildVisitFun(element).build())
                addFunction(buildVisitVoidFun(element).apply {
                    addStatement("return ${parent.element.visitFunName}(${element.visitorParam})")
                }.build())
            }
        }
    }.build()

    return printTypeCommon(generationPath, visitorVoidTypeName.packageName, visitorType)
}

fun printTransformer(generationPath: File, model: Model): GeneratedFile {
    konst visitorType = TypeSpec.interfaceBuilder(transformerTypeName).apply {
        konst d = TypeVariableName("D", KModifier.IN)
        addTypeVariable(d)

        addSuperinterface(visitorTypeName.parameterizedBy(model.rootElement.toPoetStarParameterized(), d))

        fun buildVisitFun(element: Element) = FunSpec.builder(element.visitFunName).apply {
            addModifiers(KModifier.OVERRIDE)
            addParameter(element.visitorParam, element.toPoetStarParameterized())
            addParameter("data", d)
        }

        for (element in model.elements) {
            konst returnType = element.getTransformExplicitType()
            if (element.transformByChildren) {
                addFunction(buildVisitFun(element).apply {
                    addStatement("${element.visitorParam}.transformChildren(this, data)")
                    addStatement("return ${element.visitorParam}")
                    returns(returnType.toPoetStarParameterized())
                }.build())
            } else {
                element.visitorParent?.let { parent ->
                    addFunction(buildVisitFun(element).apply {
                        addStatement("return ${parent.element.visitFunName}(${element.visitorParam}, data)")
                        returns(returnType.toPoetStarParameterized())
                    }.build())
                }
            }
        }
    }.build()

    return printTypeCommon(generationPath, transformerTypeName.packageName, visitorType)
}

fun printTypeVisitor(generationPath: File, model: Model): GeneratedFile {
    konst transformTypeFunName = "transformType"

    fun FunSpec.Builder.addVisitTypeStatement(element: Element, field: Field) {
        konst visitorParam = element.visitorParam
        konst access = "$visitorParam.${field.name}"
        when (field) {
            is SingleField -> addStatement("$access = $transformTypeFunName($visitorParam, $access, data)")
            is ListField -> {
                if (field.mutable) {
                    addStatement("$access = $access.map { $transformTypeFunName($visitorParam, it, data) }")
                } else {
                    beginControlFlow("for (i in 0 until $access.size)")
                    addStatement("$access[i] = $transformTypeFunName($visitorParam, $access[i], data)")
                    endControlFlow()
                }
            }
        }
    }

    fun Element.getFieldsWithIrTypeType(insideParent: Boolean = false): List<Field> {
        konst parentsFields = elementParents.flatMap { it.element.getFieldsWithIrTypeType(insideParent = true) }
        if (insideParent && this.visitorParent != null) {
            return parentsFields
        }

        konst irTypeFields = this.fields
            .filter {
                konst type = when (it) {
                    is SingleField -> it.type
                    is ListField -> it.elementType
                }
                type.toString() == irTypeType.toString()
            }

        return irTypeFields + parentsFields
    }

    konst visitorType = TypeSpec.interfaceBuilder(typeTransformerVoidTypeName).apply {
        konst d = TypeVariableName("D", KModifier.IN)
        addTypeVariable(d)
        addSuperinterface(transformerTypeName.parameterizedBy(d))

        konst abstractVisitFun = FunSpec.builder(transformTypeFunName).apply {
            konst poetNullableIrType = irTypeType.toPoet().copy(nullable = true)
            konst typeVariable = TypeVariableName("Type", poetNullableIrType)
            addTypeVariable(typeVariable)
            addParameter("container", model.rootElement.toPoet())
            addParameter("type", typeVariable)
            addParameter("data", d)
            returns(typeVariable)
        }
        addFunction(abstractVisitFun.addModifiers(KModifier.ABSTRACT).build())

        fun buildVisitFun(element: Element) = FunSpec.builder(element.visitFunName).apply {
            addModifiers(KModifier.OVERRIDE)
            addParameter(element.visitorParam, element.toPoetStarParameterized())
            addParameter("data", d)
        }

        for (element in model.elements) {
            konst irTypeFields = element.getFieldsWithIrTypeType()
            if (irTypeFields.isEmpty()) continue

            konst returnType = element.getTransformExplicitType()
            element.visitorParent?.let { _ ->
                addFunction(buildVisitFun(element).apply {
                    returns(returnType.toPoetStarParameterized())

                    konst visitorParam = element.visitorParam
                    when (element.name) {
                        IrTree.memberAccessExpression.name -> {
                            if (irTypeFields.singleOrNull()?.name != "typeArguments") {
                                error(
                                    """`Ir${IrTree.memberAccessExpression.name.capitalizeAsciiOnly()}` has unexpected fields with `IrType` type. 
                                        |Please adjust logic of `${typeTransformerVoidTypeName.simpleName}`'s generation.""".trimMargin()
                                )
                            }
                            beginControlFlow("(0 until $visitorParam.typeArgumentsCount).forEach {")
                            beginControlFlow("$visitorParam.getTypeArgument(it)?.let { type ->")
                            addStatement("expression.putTypeArgument(it, $transformTypeFunName($visitorParam, type, data))")
                            endControlFlow()
                            endControlFlow()
                        }
                        IrTree.`class`.name -> {
                            beginControlFlow("$visitorParam.konstueClassRepresentation?.mapUnderlyingType {")
                            addStatement("$transformTypeFunName($visitorParam, it, data)")
                            endControlFlow()
                            irTypeFields.forEach { addVisitTypeStatement(element, it) }
                        }
                        else -> irTypeFields.forEach { addVisitTypeStatement(element, it) }
                    }
                    addStatement("return super.${element.visitFunName}($visitorParam, data)")
                }.build())
            }
        }
    }.build()

    return printTypeCommon(generationPath, typeTransformerVoidTypeName.packageName, visitorType)
}

private fun Element.getTransformExplicitType(): Element {
    return generateSequence(this) { it.visitorParent?.element }
        .firstNotNullOfOrNull {
            when {
                it.transformByChildren -> it.transformerReturnType ?: it
                else -> it.transformerReturnType
            }
        } ?: this
}
