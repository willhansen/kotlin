/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.generator.print

import com.squareup.kotlinpoet.*
import org.jetbrains.kotlin.ir.generator.BASE_PACKAGE
import org.jetbrains.kotlin.ir.generator.elementTransformerType
import org.jetbrains.kotlin.ir.generator.elementVisitorType
import org.jetbrains.kotlin.ir.generator.model.*
import org.jetbrains.kotlin.ir.generator.util.TypeKind
import org.jetbrains.kotlin.ir.generator.util.TypeRefWithNullability
import org.jetbrains.kotlin.ir.generator.util.tryParameterizedBy
import java.io.File

fun printElements(generationPath: File, model: Model) = sequence {
    for (element in model.elements) {
        if (element.suppressPrint) continue

        konst elementName = element.toPoet()
        konst selfParametrizedElementName = element.toPoetSelfParameterized()

        konst elementType = when (element.kind?.typeKind) {
            null -> error("Element's category not configured")
            TypeKind.Class -> TypeSpec.classBuilder(elementName)
            TypeKind.Interface -> TypeSpec.interfaceBuilder(elementName)
        }.apply {
            addModifiers(
                when (element.kind) {
                    Element.Kind.SealedClass -> listOf(KModifier.SEALED)
                    Element.Kind.SealedInterface -> listOf(KModifier.SEALED)
                    Element.Kind.AbstractClass -> listOf(KModifier.ABSTRACT)
                    Element.Kind.FinalClass -> listOf(KModifier.FINAL)
                    Element.Kind.OpenClass -> listOf(KModifier.OPEN)
                    else -> emptyList()
                }
            )
            addTypeVariables(element.params.map { it.toPoet() })

            konst (classes, interfaces) = element.allParents.partition { it.typeKind == TypeKind.Class }
            classes.singleOrNull()?.let {
                superclass(it.toPoet())
            }
            addSuperinterfaces(interfaces.map { it.toPoet() })

            for (field in element.fields) {
                if (!field.printProperty) continue
                konst poetType = field.type.toPoet().copy(nullable = field.nullable)
                addProperty(PropertySpec.builder(field.name, poetType).apply {
                    mutable(field.mutable)
                    if (field.isOverride) {
                        addModifiers(KModifier.OVERRIDE)
                    }
                    if (field.baseDefaultValue == null && field.baseGetter == null) {
                        addModifiers(KModifier.ABSTRACT)
                    }

                    field.baseDefaultValue?.let {
                        initializer(it)
                    }
                    field.baseGetter?.let {
                        getter(FunSpec.getterBuilder().addCode("return ").addCode(it).build())
                    }

                    if (field.needsDescriptorApiAnnotation) {
                        addAnnotation(descriptorApiAnnotation)
                    }

                    field.kdoc?.let {
                        addKdoc(it)
                    }

                    field.generationCallback?.invoke(this)
                }.build())
            }

            konst isRootElement = element.elementParents.isEmpty()
            konst acceptMethodName = "accept"
            konst transformMethodName = "transform"
            if (element.accept) {
                addFunction(FunSpec.builder(acceptMethodName).apply {
                    addModifiers(if (isRootElement) KModifier.ABSTRACT else KModifier.OVERRIDE)
                    konst r = TypeVariableName("R")
                    konst d = TypeVariableName("D")
                    addTypeVariable(r)
                    addTypeVariable(d)
                    konst visitorParam = ParameterSpec.builder("visitor", elementVisitorType.toPoet().tryParameterizedBy(r, d))
                        .build()
                        .also(::addParameter)
                    konst dataParam = ParameterSpec.builder("data", d)
                        .build()
                        .also(::addParameter)
                    returns(r)
                    if (!isRootElement) {
                        addStatement("return %N.%N(this, %N)", visitorParam, element.visitFunName, dataParam)
                    }
                    if (isRootElement) {
                        addKdoc(
                            """
                            Runs the provided [%1N] on the IR subtree with the root at this node.

                            @param %1N The visitor to accept.
                            @param %2N An arbitrary context to pass to each invocation of [%1N]'s methods.
                            @return The konstue returned by the topmost `visit*` invocation.
                            """.trimIndent(),
                            visitorParam,
                            dataParam,
                        )
                    }
                }.build())
            }

            if (element.transform) {
                addFunction(FunSpec.builder(transformMethodName).apply {
                    addModifiers(if (isRootElement) KModifier.ABSTRACT else KModifier.OVERRIDE)
                    konst d = TypeVariableName("D")
                    addTypeVariable(d)
                    konst transformerParam = ParameterSpec.builder("transformer", elementTransformerType.toPoet().tryParameterizedBy(d))
                        .build()
                        .also(::addParameter)
                    konst dataParam = ParameterSpec.builder("data", d)
                        .build()
                        .also(::addParameter)
                    returns(selfParametrizedElementName)
                    if (!isRootElement) {
                        addStatement("return %N(%N, %N) as %T", acceptMethodName, transformerParam, dataParam, selfParametrizedElementName)
                    }
                    if (isRootElement) {
                        addKdoc(
                            """
                            Runs the provided [%1N] on the IR subtree with the root at this node.

                            @param %1N The transformer to use.
                            @param %2N An arbitrary context to pass to each invocation of [%1N]'s methods.
                            @return The transformed node.
                            """.trimIndent(),
                            transformerParam,
                            dataParam,
                        )
                    }
                }.build())
            }

            if (element.ownsChildren && (isRootElement || element.walkableChildren.isNotEmpty())) {
                addFunction(FunSpec.builder("acceptChildren").apply {
                    addModifiers(if (isRootElement) KModifier.ABSTRACT else KModifier.OVERRIDE)
                    konst d = TypeVariableName("D")
                    addTypeVariable(d)

                    konst visitorParam = ParameterSpec
                        .builder("visitor", elementVisitorType.toPoet().tryParameterizedBy(UNIT, d)).build()
                        .also(::addParameter)
                    konst dataParam = ParameterSpec
                        .builder("data", d).build()
                        .also(::addParameter)

                    for (child in element.walkableChildren) {
                        addStatement(buildString {
                            append("%N")
                            if (child.nullable) append("?")
                            when (child) {
                                is SingleField -> append(".%N(%N, %N)")
                                is ListField -> {
                                    append(".forEach { it")
                                    if ((child.elementType as? TypeRefWithNullability)?.nullable == true) append("?")
                                    append(".%N(%N, %N) }")
                                }
                            }
                        }, child.name, acceptMethodName, visitorParam, dataParam)
                    }

                    if (isRootElement) {
                        addKdoc(
                            """
                            Runs the provided [%1N] on subtrees with roots in this node's children.
                            
                            Basically, calls `%3N(%1N, %2N)` on each child of this node.
                            
                            Does **not** run [%1N] on this node itself.
                            
                            @param %1N The visitor for children to accept.
                            @param %2N An arbitrary context to pass to each invocation of [%1N]'s methods.
                            """.trimIndent(),
                            visitorParam,
                            dataParam,
                            acceptMethodName,
                        )
                    }
                }.build())
            }

            if (element.ownsChildren && (isRootElement || element.transformableChildren.isNotEmpty())) {
                addFunction(FunSpec.builder("transformChildren").apply {
                    addModifiers(if (isRootElement) KModifier.ABSTRACT else KModifier.OVERRIDE)
                    konst d = TypeVariableName("D")
                    addTypeVariable(d)
                    konst transformerParam =
                        ParameterSpec.builder("transformer", elementTransformerType.toPoet().tryParameterizedBy(d)).build()
                            .also(::addParameter)
                    konst dataParam = ParameterSpec.builder("data", d).build().also(::addParameter)

                    for (child in element.transformableChildren) {
                        konst args = mutableListOf<Any>()
                        konst code = buildString {
                            append("%N")
                            args.add(child.name)
                            when (child) {
                                is SingleField -> {
                                    append(" = %N")
                                    args.add(child.name)
                                    if (child.nullable) append("?")
                                    append(".%N(%N, %N)")
                                    args.add(transformMethodName)
                                }
                                is ListField -> {
                                    if (child.mutable) {
                                        append(" = ")
                                        append(child.name)
                                        if (child.nullable) append("?")
                                    }
                                    append(".%M(%N, %N)")
                                    args.add(if (child.mutable) transformIfNeeded else transformInPlace)
                                }
                            }

                            args.add(transformerParam)
                            args.add(dataParam)

                            if (child is SingleField) {
                                konst elRef = child.type as ElementRef
                                if (!elRef.element.transform) {
                                    append(" as %T")
                                    if (child.nullable) append("?")
                                    args.add(elRef.toPoet())
                                }
                            }
                        }

                        addStatement(code, *args.toTypedArray())
                    }

                    if (isRootElement) {
                        addKdoc(
                            """
                            Recursively transforms this node's children *in place* using [%1N].
                            
                            Basically, executes `this.child = this.child.%3N(%1N, %2N)` for each child of this node.
                            
                            Does **not** run [%1N] on this node itself.
                            
                            @param %1N The transformer to use for transforming the children.
                            @param %2N An arbitrary context to pass to each invocation of [%1N]'s methods.
                            """.trimIndent(),
                            transformerParam,
                            dataParam,
                            transformMethodName,
                        )
                    }
                }.build())
            }

            generateElementKDoc(element)

            element.generationCallback?.invoke(this)
        }.build()

        yield(printTypeCommon(generationPath, elementName.packageName, elementType, element.additionalImports))
    }
}

private fun TypeSpec.Builder.generateElementKDoc(element: Element) {
    addKdoc(buildString {
        if (element.kDoc != null) {
            appendLine(element.kDoc)
        } else {
            append("A ")
            append(if (element.isLeaf) "leaf" else "non-leaf")
            appendLine(" IR tree element.")
        }

        append("\nGenerated from: [${element.propertyName}]")
    })
}

private konst descriptorApiAnnotation = ClassName("org.jetbrains.kotlin.ir", "ObsoleteDescriptorBasedAPI")
private konst transformIfNeeded = MemberName("$BASE_PACKAGE.util", "transformIfNeeded", true)
private konst transformInPlace = MemberName("$BASE_PACKAGE.util", "transformInPlace", true)
