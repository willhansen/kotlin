/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.generator

import com.squareup.kotlinpoet.*
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.descriptors.ValueClassRepresentation
import org.jetbrains.kotlin.ir.generator.config.AbstractTreeBuilder
import org.jetbrains.kotlin.ir.generator.config.ElementConfig
import org.jetbrains.kotlin.ir.generator.config.ElementConfig.Category.*
import org.jetbrains.kotlin.ir.generator.config.ListFieldConfig.Mutability.Array
import org.jetbrains.kotlin.ir.generator.config.ListFieldConfig.Mutability.List
import org.jetbrains.kotlin.ir.generator.config.ListFieldConfig.Mutability.Var
import org.jetbrains.kotlin.ir.generator.config.SimpleFieldConfig
import org.jetbrains.kotlin.ir.generator.model.Element.Companion.elementName2typeName
import org.jetbrains.kotlin.ir.generator.print.toPoet
import org.jetbrains.kotlin.ir.generator.util.*
import org.jetbrains.kotlin.ir.generator.util.Import
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource
import org.jetbrains.kotlin.types.Variance

// Note the style of the DSL to describe IR elements, which is these things in the following order:
// 1) config (see properties of ElementConfig)
// 2) parents
// 3) fields
object IrTree : AbstractTreeBuilder() {
    private fun symbol(type: TypeRef) = field("symbol", type, mutable = false)
    private fun descriptor(typeName: String) =
        field("descriptor", ClassRef<TypeParameterRef>(TypeKind.Interface, "org.jetbrains.kotlin.descriptors", typeName), mutable = false)

    private konst factory: SimpleFieldConfig = field("factory", type(Packages.declarations, "IrFactory"), mutable = false)

    override konst rootElement: ElementConfig by element(Other, name = "element") {
        accept = true
        transform = true
        transformByChildren = true

        fun offsetField(prefix: String) = field(prefix + "Offset", int, mutable = false) {
            kdoc = """
            The $prefix offset of the syntax node from which this IR node was generated,
            in number of characters from the start of the source file. If there is no source information for this IR node,
            the [UNDEFINED_OFFSET] constant is used. In order to get the line number and the column number from this offset,
            [IrFileEntry.getLineNumber] and [IrFileEntry.getColumnNumber] can be used.
            
            @see IrFileEntry.getSourceRangeInfo
            """.trimIndent()
        }

        +offsetField("start")
        +offsetField("end")

        kDoc = "The root interface of the IR tree. Each IR node implements this interface."
    }
    konst statement: ElementConfig by element(Other)

    konst declaration: ElementConfig by element(Declaration) {
        parent(statement)
        parent(symbolOwner)
        parent(mutableAnnotationContainerType)

        +descriptor("DeclarationDescriptor")
        +field("origin", type(Packages.declarations, "IrDeclarationOrigin"))
        +field("parent", declarationParent)
        +factory
    }
    konst declarationBase: ElementConfig by element(Declaration) {
        typeKind = TypeKind.Class
        transformByChildren = true
        transformerReturnType = statement
        visitorParent = rootElement
        visitorName = "declaration"

        parent(declaration)
    }
    konst declarationParent: ElementConfig by element(Declaration)
    konst declarationWithVisibility: ElementConfig by element(Declaration) {
        parent(declaration)

        +field("visibility", type(Packages.descriptors, "DescriptorVisibility"))
    }
    konst declarationWithName: ElementConfig by element(Declaration) {
        parent(declaration)

        +field("name", type<Name>())
    }
    konst possiblyExternalDeclaration: ElementConfig by element(Declaration) {
        parent(declarationWithName)

        +field("isExternal", boolean)
    }
    konst symbolOwner: ElementConfig by element(Declaration) {
        +symbol(symbolType)
    }
    konst metadataSourceOwner: ElementConfig by element(Declaration) {
        konst metadataField = +field("metadata", type(Packages.declarations, "MetadataSource"), nullable = true) {
            kdoc = """
            The arbitrary metadata associated with this IR node.
            
            @see ${elementName2typeName(this@element.name)}
            """.trimIndent()
        }
        kDoc = """
        An [${elementName2typeName(rootElement.name)}] capable of holding something which backends can use to write
        as the metadata for the declaration.
        
        Technically, it can even be ± an array of bytes, but right now it's usually the frontend representation of the declaration,
        so a descriptor in case of K1, and [org.jetbrains.kotlin.fir.FirElement] in case of K2,
        and the backend invokes a metadata serializer on it to obtain metadata and write it, for example, to `@kotlin.Metadata`
        on JVM.
        
        In Kotlin/Native, [${metadataField.name}] is used to store some LLVM-related stuff in an IR declaration,
        but this is only for performance purposes (before it was done using simple maps).
        """.trimIndent()
    }
    konst overridableMember: ElementConfig by element(Declaration) {
        parent(declaration)
        parent(declarationWithVisibility)
        parent(declarationWithName)
        parent(symbolOwner)

        +field("modality", type<Modality>())
    }
    konst overridableDeclaration: ElementConfig by element(Declaration) {
        konst s = +param("S", symbolType)

        parent(overridableMember)

        +field("symbol", s, mutable = false)
        +field("isFakeOverride", boolean)
        +listField("overriddenSymbols", s, mutability = Var)
    }
    konst memberWithContainerSource: ElementConfig by element(Declaration) {
        parent(declarationWithName)

        +field("containerSource", type<DeserializedContainerSource>(), nullable = true, mutable = false)
    }
    konst konstueDeclaration: ElementConfig by element(Declaration) {
        parent(declarationWithName)
        parent(symbolOwner)

        +descriptor("ValueDescriptor")
        +symbol(konstueSymbolType)
        +field("type", irTypeType)
        +field("isAssignable", boolean, mutable = false)
    }
    konst konstueParameter: ElementConfig by element(Declaration) {
        transform = true
        visitorParent = declarationBase

        parent(declarationBase)
        parent(konstueDeclaration)

        +descriptor("ParameterDescriptor")
        +symbol(konstueParameterSymbolType)
        +field("index", int)
        +field("varargElementType", irTypeType, nullable = true)
        +field("isCrossinline", boolean)
        +field("isNoinline", boolean)
        +field("isHidden", boolean) {
            additionalImports.add(Import("org.jetbrains.kotlin.ir.util", "IdSignature"))
            kdoc = """
            If `true`, the konstue parameter does not participate in [IdSignature] computation.

            This is a workaround that is needed for better support of compiler plugins.
            Suppose you have the following code and some IR plugin that adds a konstue parameter to functions
            marked with the `@PluginMarker` annotation.
            ```kotlin
            @PluginMarker
            fun foo(defined: Int) { /* ... */ }
            ```

            Suppose that after applying the plugin the function is changed to:
            ```kotlin
            @PluginMarker
            fun foo(defined: Int, ${'$'}extra: String) { /* ... */ }
            ```

            If a compiler plugin adds parameters to an [${elementName2typeName(function.name)}],
            the representations of the function in the frontend and in the backend may diverge, potentially causing signature mismatch and
            linkage errors (see [KT-40980](https://youtrack.jetbrains.com/issue/KT-40980)).
            We wouldn't want IR plugins to affect the frontend representation, since in an IDE you'd want to be able to see those
            declarations in their original form (without the `${'$'}extra` parameter).

            To fix this problem, [$name] was introduced.
            
            TODO: consider dropping [$name] if it isn't used by any known plugin.
            """.trimIndent()
        }
        +field("defaultValue", expressionBody, nullable = true, isChild = true)
    }
    konst `class`: ElementConfig by element(Declaration) {
        visitorParent = declarationBase

        parent(declarationBase)
        parent(possiblyExternalDeclaration)
        parent(declarationWithVisibility)
        parent(typeParametersContainer)
        parent(declarationContainer)
        parent(attributeContainer)
        parent(metadataSourceOwner)

        +descriptor("ClassDescriptor")
        +symbol(classSymbolType)
        +field("kind", type<ClassKind>())
        +field("modality", type<Modality>())
        +field("isCompanion", boolean)
        +field("isInner", boolean)
        +field("isData", boolean)
        +field("isValue", boolean)
        +field("isExpect", boolean)
        +field("isFun", boolean)
        +field("source", type<SourceElement>(), mutable = false)
        +listField("superTypes", irTypeType, mutability = Var)
        +field("thisReceiver", konstueParameter, nullable = true, isChild = true)
        +field(
            "konstueClassRepresentation",
            type<ValueClassRepresentation<*>>().withArgs(type(Packages.types, "IrSimpleType")),
            nullable = true,
        )
        +listField("sealedSubclasses", classSymbolType, mutability = Var) {
            kdoc = """
            If this is a sealed class or interface, this list contains symbols of all its immediate subclasses.
            Otherwise, this is an empty list.
            
            NOTE: If this [${elementName2typeName(this@element.name)}] was deserialized from a klib, this list will always be empty!
            See [KT-54028](https://youtrack.jetbrains.com/issue/KT-54028).
            """.trimIndent()
        }
    }
    konst attributeContainer: ElementConfig by element(Declaration) {
        kDoc = """
            Represents an IR element that can be copied, but must remember its original element. It is
            useful, for example, to keep track of generated names for anonymous declarations.
            @property attributeOwnerId original element before copying. Always satisfies the following
              invariant: `this.attributeOwnerId == this.attributeOwnerId.attributeOwnerId`.
            @property originalBeforeInline original element before inlining. Useful only with IR
              inliner. `null` if the element wasn't inlined. Unlike [attributeOwnerId], doesn't have the
              idempotence invariant and can contain a chain of declarations.
        """.trimIndent()

        +field("attributeOwnerId", attributeContainer)
        +field("originalBeforeInline", attributeContainer, nullable = true) // null <=> this element wasn't inlined
    }
    konst anonymousInitializer: ElementConfig by element(Declaration) {
        visitorParent = declarationBase

        parent(declarationBase)

        +descriptor("ClassDescriptor") // TODO special descriptor for anonymous initializer blocks
        +symbol(anonymousInitializerSymbolType)
        +field("isStatic", boolean)
        +field("body", blockBody, isChild = true)
    }
    konst declarationContainer: ElementConfig by element(Declaration) {
        ownsChildren = false

        parent(declarationParent)

        +listField("declarations", declaration, mutability = List, isChild = true)
    }
    konst typeParametersContainer: ElementConfig by element(Declaration) {
        ownsChildren = false

        parent(declaration)
        parent(declarationParent)

        +listField("typeParameters", typeParameter, mutability = Var, isChild = true)
    }
    konst typeParameter: ElementConfig by element(Declaration) {
        visitorParent = declarationBase
        transform = true

        parent(declarationBase)
        parent(declarationWithName)

        +descriptor("TypeParameterDescriptor")
        +symbol(typeParameterSymbolType)
        +field("variance", type<Variance>())
        +field("index", int)
        +field("isReified", boolean)
        +listField("superTypes", irTypeType, mutability = Var)
    }
    konst returnTarget: ElementConfig by element(Declaration) {
        parent(symbolOwner)

        +descriptor("FunctionDescriptor")
        +symbol(returnTargetSymbolType)
    }
    konst function: ElementConfig by element(Declaration) {
        visitorParent = declarationBase

        parent(declarationBase)
        parent(possiblyExternalDeclaration)
        parent(declarationWithVisibility)
        parent(typeParametersContainer)
        parent(symbolOwner)
        parent(declarationParent)
        parent(returnTarget)
        parent(memberWithContainerSource)
        parent(metadataSourceOwner)

        +descriptor("FunctionDescriptor")
        +symbol(functionSymbolType)
        // NB: there's an inline constructor for Array and each primitive array class.
        +field("isInline", boolean)
        +field("isExpect", boolean)
        +field("returnType", irTypeType)
        +field("dispatchReceiverParameter", konstueParameter, nullable = true, isChild = true)
        +field("extensionReceiverParameter", konstueParameter, nullable = true, isChild = true)
        +listField("konstueParameters", konstueParameter, mutability = Var, isChild = true)
        // The first `contextReceiverParametersCount` konstue parameters are context receivers.
        +field("contextReceiverParametersCount", int)
        +field("body", body, nullable = true, isChild = true)
    }
    konst constructor: ElementConfig by element(Declaration) {
        visitorParent = function

        parent(function)

        +descriptor("ClassConstructorDescriptor")
        +symbol(constructorSymbolType)
        +field("isPrimary", boolean)
    }
    konst enumEntry: ElementConfig by element(Declaration) {
        visitorParent = declarationBase

        parent(declarationBase)
        parent(declarationWithName)

        +descriptor("ClassDescriptor")
        +symbol(enumEntrySymbolType)
        +field("initializerExpression", expressionBody, nullable = true, isChild = true)
        +field("correspondingClass", `class`, nullable = true, isChild = true)
    }
    konst errorDeclaration: ElementConfig by element(Declaration) {
        visitorParent = declarationBase

        parent(declarationBase)

        +field("symbol", symbolType, mutable = false) {
            baseGetter = code("error(\"Should never be called\")")
        }
    }
    konst functionWithLateBinding: ElementConfig by element(Declaration) {
        typeKind = TypeKind.Interface

        parent(declaration)

        +symbol(simpleFunctionSymbolType)
        +field("modality", type<Modality>())
        +field("isBound", boolean, mutable = false)
        generationCallback = {
            addFunction(
                FunSpec.builder("acquireSymbol")
                    .addModifiers(KModifier.ABSTRACT)
                    .addParameter("symbol", simpleFunctionSymbolType.toPoet())
                    .returns(simpleFunction.toPoet())
                    .build()
            )
        }
    }
    konst propertyWithLateBinding: ElementConfig by element(Declaration) {
        typeKind = TypeKind.Interface

        parent(declaration)

        +symbol(propertySymbolType)
        +field("modality", type<Modality>())
        +field("getter", simpleFunction, nullable = true)
        +field("setter", simpleFunction, nullable = true)
        +field("isBound", boolean, mutable = false)
        generationCallback = {
            addFunction(
                FunSpec.builder("acquireSymbol")
                    .addModifiers(KModifier.ABSTRACT)
                    .addParameter("symbol", propertySymbolType.toPoet())
                    .returns(property.toPoet())
                    .build()
            )
        }
    }
    konst field: ElementConfig by element(Declaration) {
        visitorParent = declarationBase

        parent(declarationBase)
        parent(possiblyExternalDeclaration)
        parent(declarationWithVisibility)
        parent(declarationParent)
        parent(metadataSourceOwner)

        +descriptor("PropertyDescriptor")
        +symbol(fieldSymbolType)
        +field("type", irTypeType)
        +field("isFinal", boolean)
        +field("isStatic", boolean)
        +field("initializer", expressionBody, nullable = true, isChild = true)
        +field("correspondingPropertySymbol", propertySymbolType, nullable = true)
    }
    konst localDelegatedProperty: ElementConfig by element(Declaration) {
        visitorParent = declarationBase

        parent(declarationBase)
        parent(declarationWithName)
        parent(symbolOwner)
        parent(metadataSourceOwner)

        +descriptor("VariableDescriptorWithAccessors")
        +symbol(localDelegatedPropertySymbolType)
        +field("type", irTypeType)
        +field("isVar", boolean)
        +field("delegate", variable, isChild = true)
        +field("getter", simpleFunction, isChild = true)
        +field("setter", simpleFunction, nullable = true, isChild = true)
    }
    konst moduleFragment: ElementConfig by element(Declaration) {
        visitorParent = rootElement
        transform = true
        transformByChildren = true

        +descriptor("ModuleDescriptor")
        +field("name", type<Name>(), mutable = false)
        +field("irBuiltins", type(Packages.tree, "IrBuiltIns"), mutable = false)
        +listField("files", file, mutability = List, isChild = true)
        konst undefinedOffset = MemberName(Packages.tree, "UNDEFINED_OFFSET")
        +field("startOffset", int, mutable = false) {
            baseGetter = code("%M", undefinedOffset)
        }
        +field("endOffset", int, mutable = false) {
            baseGetter = code("%M", undefinedOffset)
        }
    }
    konst property: ElementConfig by element(Declaration) {
        visitorParent = declarationBase

        parent(declarationBase)
        parent(possiblyExternalDeclaration)
        parent(overridableDeclaration.withArgs("S" to propertySymbolType))
        parent(metadataSourceOwner)
        parent(attributeContainer)
        parent(memberWithContainerSource)

        +descriptor("PropertyDescriptor")
        +symbol(propertySymbolType)
        +field("isVar", boolean)
        +field("isConst", boolean)
        +field("isLateinit", boolean)
        +field("isDelegated", boolean)
        +field("isExpect", boolean)
        +field("isFakeOverride", boolean)
        +field("backingField", field, nullable = true, isChild = true)
        +field("getter", simpleFunction, nullable = true, isChild = true)
        +field("setter", simpleFunction, nullable = true, isChild = true)
    }

    //TODO: make IrScript as IrPackageFragment, because script is used as a file, not as a class
    //NOTE: declarations and statements stored separately
    konst script: ElementConfig by element(Declaration) {
        visitorParent = declarationBase

        parent(declarationBase)
        parent(declarationWithName)
        parent(declarationParent)
        parent(statementContainer)
        parent(metadataSourceOwner)

        +symbol(scriptSymbolType)
        // NOTE: is the result of the FE conversion, because there script interpreted as a class and has receiver
        // TODO: consider removing from here and handle appropriately in the lowering
        +field("thisReceiver", konstueParameter, isChild = true, nullable = true) // K1
        +field("baseClass", irTypeType, nullable = true) // K1
        +listField("explicitCallParameters", variable, mutability = Var, isChild = true)
        +listField("implicitReceiversParameters", konstueParameter, mutability = Var, isChild = true)
        +listField("providedProperties", propertySymbolType, mutability = Var)
        +listField("providedPropertiesParameters", konstueParameter, mutability = Var, isChild = true)
        +field("resultProperty", propertySymbolType, nullable = true)
        +field("earlierScriptsParameter", konstueParameter, nullable = true, isChild = true)
        +listField("earlierScripts", scriptSymbolType, mutability = Var, nullable = true)
        +field("targetClass", classSymbolType, nullable = true)
        +field("constructor", constructor, nullable = true) // K1
    }
    konst simpleFunction: ElementConfig by element(Declaration) {
        visitorParent = function

        parent(function)
        parent(overridableDeclaration.withArgs("S" to simpleFunctionSymbolType))
        parent(attributeContainer)

        +symbol(simpleFunctionSymbolType)
        +field("isTailrec", boolean)
        +field("isSuspend", boolean)
        +field("isFakeOverride", boolean)
        +field("isOperator", boolean)
        +field("isInfix", boolean)
        +field("correspondingPropertySymbol", propertySymbolType, nullable = true)
    }
    konst typeAlias: ElementConfig by element(Declaration) {
        visitorParent = declarationBase

        parent(declarationBase)
        parent(declarationWithName)
        parent(declarationWithVisibility)
        parent(typeParametersContainer)

        +descriptor("TypeAliasDescriptor")
        +symbol(typeAliasSymbolType)
        +field("isActual", boolean)
        +field("expandedType", irTypeType)
    }
    konst variable: ElementConfig by element(Declaration) {
        visitorParent = declarationBase

        parent(declarationBase)
        parent(konstueDeclaration)

        +descriptor("VariableDescriptor")
        +symbol(variableSymbolType)
        +field("isVar", boolean)
        +field("isConst", boolean)
        +field("isLateinit", boolean)
        +field("initializer", expression, nullable = true, isChild = true)
    }
    konst packageFragment: ElementConfig by element(Declaration) {
        visitorParent = rootElement
        ownsChildren = false

        parent(declarationContainer)
        parent(symbolOwner)

        +symbol(packageFragmentSymbolType)
        +field("packageFragmentDescriptor", type(Packages.descriptors, "PackageFragmentDescriptor"), mutable = false)
        +field("packageFqName", type<FqName>())
        +field("fqName", type<FqName>()) {
            baseGetter = code("packageFqName")
            generationCallback = {
                konst deprecatedAnnotation = AnnotationSpec.builder(Deprecated::class)
                    .addMember(code("message = \"Please use `packageFqName` instead\""))
                    .addMember(code("replaceWith = ReplaceWith(\"packageFqName\")"))
                    .addMember(code("level = DeprecationLevel.ERROR"))
                    .build()
                addAnnotation(deprecatedAnnotation)
                setter(FunSpec.setterBuilder().addParameter("konstue", FqName::class).addCode(code("packageFqName = konstue")).build())
            }
        }
    }
    konst externalPackageFragment: ElementConfig by element(Declaration) {
        visitorParent = packageFragment
        transformByChildren = true

        parent(packageFragment)

        +symbol(externalPackageFragmentSymbolType)
        +field("containerSource", type<DeserializedContainerSource>(), nullable = true, mutable = false)
    }
    konst file: ElementConfig by element(Declaration) {
        transform = true
        transformByChildren = true
        visitorParent = packageFragment

        parent(packageFragment)
        parent(mutableAnnotationContainerType)
        parent(metadataSourceOwner)

        +symbol(fileSymbolType)
        +field("module", moduleFragment)
        +field("fileEntry", type(Packages.tree, "IrFileEntry"))
    }

    konst expression: ElementConfig by element(Expression) {
        visitorParent = rootElement
        transform = true
        transformByChildren = true

        parent(statement)
        parent(varargElement)
        parent(attributeContainer)

        +field("attributeOwnerId", attributeContainer) {
            baseDefaultValue = code("this")
        }
        +field("originalBeforeInline", attributeContainer, nullable = true) {
            baseDefaultValue = code("null")
        }
        +field("type", irTypeType)
    }
    konst statementContainer: ElementConfig by element(Expression) {
        ownsChildren = false

        +listField("statements", statement, mutability = List, isChild = true)
    }
    konst body: ElementConfig by element(Expression) {
        transform = true
        visitorParent = rootElement
        visitorParam = "body"
        transformByChildren = true
        typeKind = TypeKind.Class
    }
    konst expressionBody: ElementConfig by element(Expression) {
        transform = true
        visitorParent = body
        visitorParam = "body"

        parent(body)

        +factory
        +field("expression", expression, isChild = true)
    }
    konst blockBody: ElementConfig by element(Expression) {
        visitorParent = body
        visitorParam = "body"

        parent(body)
        parent(statementContainer)

        +factory
    }
    konst declarationReference: ElementConfig by element(Expression) {
        visitorParent = expression

        parent(expression)

        +symbol(symbolType)
        //diff: no accept
    }
    konst memberAccessExpression: ElementConfig by element(Expression) {
        visitorParent = declarationReference
        visitorName = "memberAccess"
        transformerReturnType = rootElement
        konst s = +param("S", symbolType)

        parent(declarationReference)

        +field("dispatchReceiver", expression, nullable = true, isChild = true) {
            baseDefaultValue = code("null")
        }
        +field("extensionReceiver", expression, nullable = true, isChild = true) {
            baseDefaultValue = code("null")
        }
        +symbol(s)
        +field("origin", statementOriginType, nullable = true)
        +listField("konstueArguments", expression.copy(nullable = true), mutability = Array, isChild = true) {
            generationCallback = {
                addModifiers(KModifier.PROTECTED)
            }
        }
        +listField("typeArguments", irTypeType.copy(nullable = true), mutability = Array) {
            generationCallback = {
                addModifiers(KModifier.PROTECTED)
            }
        }

        konst checkArgumentSlotAccess = MemberName("org.jetbrains.kotlin.ir.expressions", "checkArgumentSlotAccess", true)
        generationCallback = {
            addFunction(
                FunSpec.builder("getValueArgument")
                    .addParameter("index", int.toPoet())
                    .returns(expression.toPoet().copy(nullable = true))
                    .addCode("%M(\"konstue\", index, konstueArguments.size)\n", checkArgumentSlotAccess)
                    .addCode("return konstueArguments[index]")
                    .build()
            )
            addFunction(
                FunSpec.builder("getTypeArgument")
                    .addParameter("index", int.toPoet())
                    .returns(irTypeType.toPoet().copy(nullable = true))
                    .addCode("%M(\"type\", index, typeArguments.size)\n", checkArgumentSlotAccess)
                    .addCode("return typeArguments[index]")
                    .build()
            )
            addFunction(
                FunSpec.builder("putValueArgument")
                    .addParameter("index", int.toPoet())
                    .addParameter("konstueArgument", expression.toPoet().copy(nullable = true))
                    .addCode("%M(\"konstue\", index, konstueArguments.size)\n", checkArgumentSlotAccess)
                    .addCode("konstueArguments[index] = konstueArgument")
                    .build()
            )
            addFunction(
                FunSpec.builder("putTypeArgument")
                    .addParameter("index", int.toPoet())
                    .addParameter("type", irTypeType.toPoet().copy(nullable = true))
                    .addCode("%M(\"type\", index, typeArguments.size)\n", checkArgumentSlotAccess)
                    .addCode("typeArguments[index] = type")
                    .build()
            )
            addProperty(
                PropertySpec.builder("konstueArgumentsCount", int.toPoet())
                    .getter(FunSpec.getterBuilder().addCode("return konstueArguments.size").build())
                    .build()
            )
            addProperty(
                PropertySpec.builder("typeArgumentsCount", int.toPoet())
                    .getter(FunSpec.getterBuilder().addCode("return typeArguments.size").build())
                    .build()
            )
        }
    }
    konst functionAccessExpression: ElementConfig by element(Expression) {
        visitorParent = memberAccessExpression
        visitorName = "functionAccess"
        transformerReturnType = rootElement

        parent(memberAccessExpression.withArgs("S" to functionSymbolType))

        +field("contextReceiversCount", int)
    }
    konst constructorCall: ElementConfig by element(Expression) {
        visitorParent = functionAccessExpression
        transformerReturnType = rootElement

        parent(functionAccessExpression)

        +symbol(constructorSymbolType)
        +field("source", type<SourceElement>())
        +field("constructorTypeArgumentsCount", int)
    }
    konst getSingletonValue: ElementConfig by element(Expression) {
        visitorParent = declarationReference
        visitorName = "SingletonReference"

        parent(declarationReference)
    }
    konst getObjectValue: ElementConfig by element(Expression) {
        visitorParent = getSingletonValue

        parent(getSingletonValue)

        +symbol(classSymbolType)
    }
    konst getEnumValue: ElementConfig by element(Expression) {
        visitorParent = getSingletonValue

        parent(getSingletonValue)

        +symbol(enumEntrySymbolType)
    }

    /**
     * Platform-specific low-level reference to function.
     *
     * On JS platform it represents a plain reference to JavaScript function.
     * On JVM platform it represents a MethodHandle constant.
     */
    konst rawFunctionReference: ElementConfig by element(Expression) {
        visitorParent = declarationReference

        parent(declarationReference)

        +symbol(functionSymbolType)
    }
    konst containerExpression: ElementConfig by element(Expression) {
        visitorParent = expression

        parent(expression)
        parent(statementContainer)

        +field("origin", statementOriginType, nullable = true)
        +listField("statements", statement, mutability = List, isChild = true) {
            generationCallback = {
                addModifiers(KModifier.OVERRIDE)
            }
            baseDefaultValue = code("ArrayList(2)")
        }
    }
    konst block: ElementConfig by element(Expression) {
        visitorParent = containerExpression
        accept = true

        parent(containerExpression)
    }
    konst composite: ElementConfig by element(Expression) {
        visitorParent = containerExpression

        parent(containerExpression)
    }
    konst returnableBlock: ElementConfig by element(Expression) {
        parent(block)
        parent(symbolOwner)
        parent(returnTarget)

        +symbol(returnableBlockSymbolType)
    }
    konst inlinedFunctionBlock: ElementConfig by element(Expression) {
        parent(block)

        +field("inlineCall", functionAccessExpression)
        +field("inlinedElement", rootElement)
    }
    konst syntheticBody: ElementConfig by element(Expression) {
        visitorParent = body
        visitorParam = "body"

        parent(body)

        +field("kind", type(Packages.exprs, "IrSyntheticBodyKind"))
    }
    konst breakContinue: ElementConfig by element(Expression) {
        visitorParent = expression
        visitorParam = "jump"

        parent(expression)

        +field("loop", loop)
        +field("label", string, nullable = true) {
            baseDefaultValue = code("null")
        }
    }
    konst `break` by element(Expression) {
        visitorParent = breakContinue
        visitorParam = "jump"

        parent(breakContinue)
    }
    konst `continue` by element(Expression) {
        visitorParent = breakContinue
        visitorParam = "jump"

        parent(breakContinue)
    }
    konst call: ElementConfig by element(Expression) {
        visitorParent = functionAccessExpression

        parent(functionAccessExpression)

        +symbol(simpleFunctionSymbolType)
        +field("superQualifierSymbol", classSymbolType, nullable = true)
    }
    konst callableReference: ElementConfig by element(Expression) {
        visitorParent = memberAccessExpression
        konst s = +param("S", symbolType)

        parent(memberAccessExpression.withArgs("S" to s))
    }
    konst functionReference: ElementConfig by element(Expression) {
        visitorParent = callableReference

        parent(callableReference.withArgs("S" to functionSymbolType))

        +field("reflectionTarget", functionSymbolType, nullable = true)
    }
    konst propertyReference: ElementConfig by element(Expression) {
        visitorParent = callableReference

        parent(callableReference.withArgs("S" to propertySymbolType))

        +field("field", fieldSymbolType, nullable = true)
        +field("getter", simpleFunctionSymbolType, nullable = true)
        +field("setter", simpleFunctionSymbolType, nullable = true)
    }
    konst localDelegatedPropertyReference: ElementConfig by element(Expression) {
        visitorParent = callableReference

        parent(callableReference.withArgs("S" to localDelegatedPropertySymbolType))

        +field("delegate", variableSymbolType)
        +field("getter", simpleFunctionSymbolType)
        +field("setter", simpleFunctionSymbolType, nullable = true)
    }
    konst classReference: ElementConfig by element(Expression) {
        visitorParent = declarationReference

        parent(declarationReference)

        +symbol(classifierSymbolType)
        +field("classType", irTypeType)
    }
    konst const: ElementConfig by element(Expression) {
        visitorParent = expression
        konst t = +param("T")

        parent(expression)

        +field("kind", type(Packages.exprs, "IrConstKind").withArgs(t))
        +field("konstue", t)
    }
    konst constantValue: ElementConfig by element(Expression) {
        visitorParent = expression
        transformByChildren = true

        parent(expression)

        generationCallback = {
            addFunction(
                FunSpec.builder("contentEquals")
                    .addModifiers(KModifier.ABSTRACT)
                    .addParameter("other", constantValue.toPoet())
                    .returns(boolean.toPoet())
                    .build()
            )
            addFunction(
                FunSpec.builder("contentHashCode")
                    .addModifiers(KModifier.ABSTRACT)
                    .returns(int.toPoet())
                    .build()
            )
        }
    }
    konst constantPrimitive: ElementConfig by element(Expression) {
        visitorParent = constantValue

        parent(constantValue)

        +field("konstue", const.withArgs("T" to TypeRef.Star), isChild = true)
    }
    konst constantObject: ElementConfig by element(Expression) {
        visitorParent = constantValue

        parent(constantValue)

        +field("constructor", constructorSymbolType)
        +listField("konstueArguments", constantValue, mutability = List, isChild = true)
        +listField("typeArguments", irTypeType, mutability = List)
    }
    konst constantArray: ElementConfig by element(Expression) {
        visitorParent = constantValue

        parent(constantValue)

        +listField("elements", constantValue, mutability = List, isChild = true)
    }
    konst delegatingConstructorCall: ElementConfig by element(Expression) {
        visitorParent = functionAccessExpression

        parent(functionAccessExpression)

        +symbol(constructorSymbolType)
    }
    konst dynamicExpression: ElementConfig by element(Expression) {
        visitorParent = expression

        parent(expression)
    }
    konst dynamicOperatorExpression: ElementConfig by element(Expression) {
        visitorParent = dynamicExpression

        parent(dynamicExpression)

        +field("operator", type(Packages.exprs, "IrDynamicOperator"))
        +field("receiver", expression, isChild = true)
        +listField("arguments", expression, mutability = List, isChild = true)
    }
    konst dynamicMemberExpression: ElementConfig by element(Expression) {
        visitorParent = dynamicExpression

        parent(dynamicExpression)

        +field("memberName", string)
        +field("receiver", expression, isChild = true)
    }
    konst enumConstructorCall: ElementConfig by element(Expression) {
        visitorParent = functionAccessExpression

        parent(functionAccessExpression)

        +symbol(constructorSymbolType)
    }
    konst errorExpression: ElementConfig by element(Expression) {
        visitorParent = expression
        accept = true

        parent(expression)

        +field("description", string)
    }
    konst errorCallExpression: ElementConfig by element(Expression) {
        visitorParent = errorExpression

        parent(errorExpression)

        +field("explicitReceiver", expression, nullable = true, isChild = true)
        +listField("arguments", expression, mutability = List, isChild = true)
    }
    konst fieldAccessExpression: ElementConfig by element(Expression) {
        visitorParent = declarationReference
        visitorName = "fieldAccess"
        ownsChildren = false

        parent(declarationReference)

        +symbol(fieldSymbolType)
        +field("superQualifierSymbol", classSymbolType, nullable = true)
        +field("receiver", expression, nullable = true, isChild = true) {
            baseDefaultValue = code("null")
        }
        +field("origin", statementOriginType, nullable = true)
    }
    konst getField: ElementConfig by element(Expression) {
        visitorParent = fieldAccessExpression

        parent(fieldAccessExpression)
    }
    konst setField: ElementConfig by element(Expression) {
        visitorParent = fieldAccessExpression

        parent(fieldAccessExpression)

        +field("konstue", expression, isChild = true)
    }
    konst functionExpression: ElementConfig by element(Expression) {
        visitorParent = expression
        transformerReturnType = rootElement

        parent(expression)

        +field("origin", statementOriginType)
        +field("function", simpleFunction, isChild = true)
    }
    konst getClass: ElementConfig by element(Expression) {
        visitorParent = expression

        parent(expression)

        +field("argument", expression, isChild = true)
    }
    konst instanceInitializerCall: ElementConfig by element(Expression) {
        visitorParent = expression

        parent(expression)

        +field("classSymbol", classSymbolType)
    }
    konst loop: ElementConfig by element(Expression) {
        visitorParent = expression
        visitorParam = "loop"
        ownsChildren = false

        parent(expression)

        +field("origin", statementOriginType, nullable = true)
        +field("body", expression, nullable = true, isChild = true) {
            baseDefaultValue = code("null")
        }
        +field("condition", expression, isChild = true)
        +field("label", string, nullable = true) {
            baseDefaultValue = code("null")
        }
    }
    konst whileLoop: ElementConfig by element(Expression) {
        visitorParent = loop
        visitorParam = "loop"
        childrenOrderOverride = listOf("condition", "body")

        parent(loop)
    }
    konst doWhileLoop: ElementConfig by element(Expression) {
        visitorParent = loop
        visitorParam = "loop"

        parent(loop)
    }
    konst `return`: ElementConfig by element(Expression) {
        visitorParent = expression

        parent(expression)

        +field("konstue", expression, isChild = true)
        +field("returnTargetSymbol", returnTargetSymbolType)
    }
    konst stringConcatenation: ElementConfig by element(Expression) {
        visitorParent = expression

        parent(expression)

        +listField("arguments", expression, mutability = List, isChild = true)
    }
    konst suspensionPoint: ElementConfig by element(Expression) {
        visitorParent = expression

        parent(expression)

        +field("suspensionPointIdParameter", variable, isChild = true)
        +field("result", expression, isChild = true)
        +field("resumeResult", expression, isChild = true)
    }
    konst suspendableExpression: ElementConfig by element(Expression) {
        visitorParent = expression

        parent(expression)

        +field("suspensionPointId", expression, isChild = true)
        +field("result", expression, isChild = true)
    }
    konst `throw`: ElementConfig by element(Expression) {
        visitorParent = expression

        parent(expression)

        +field("konstue", expression, isChild = true)
    }
    konst `try`: ElementConfig by element(Expression) {
        visitorParent = expression
        visitorParam = "aTry"

        parent(expression)

        +field("tryResult", expression, isChild = true)
        +listField("catches", catch, mutability = List, isChild = true)
        +field("finallyExpression", expression, nullable = true, isChild = true)
    }
    konst catch: ElementConfig by element(Expression) {
        visitorParent = rootElement
        visitorParam = "aCatch"
        transform = true
        transformByChildren = true

        +field("catchParameter", variable, isChild = true)
        +field("result", expression, isChild = true)
    }
    konst typeOperatorCall: ElementConfig by element(Expression) {
        visitorParent = expression
        visitorName = "typeOperator"

        parent(expression)

        +field("operator", type(Packages.exprs, "IrTypeOperator"))
        +field("argument", expression, isChild = true)
        +field("typeOperand", irTypeType)
    }
    konst konstueAccessExpression: ElementConfig by element(Expression) {
        visitorParent = declarationReference
        visitorName = "konstueAccess"

        parent(declarationReference)

        +symbol(konstueSymbolType)
        +field("origin", statementOriginType, nullable = true)
    }
    konst getValue: ElementConfig by element(Expression) {
        visitorParent = konstueAccessExpression

        parent(konstueAccessExpression)
    }
    konst setValue: ElementConfig by element(Expression) {
        visitorParent = konstueAccessExpression

        parent(konstueAccessExpression)

        +symbol(konstueSymbolType)
        +field("konstue", expression, isChild = true)
    }
    konst varargElement: ElementConfig by element(Expression)
    konst vararg: ElementConfig by element(Expression) {
        visitorParent = expression

        parent(expression)

        +field("varargElementType", irTypeType)
        +listField("elements", varargElement, mutability = List, isChild = true)
    }
    konst spreadElement: ElementConfig by element(Expression) {
        visitorParent = rootElement
        visitorParam = "spread"
        transform = true
        transformByChildren = true

        parent(varargElement)

        +field("expression", expression, isChild = true)
    }
    konst `when`: ElementConfig by element(Expression) {
        visitorParent = expression

        parent(expression)

        +field("origin", statementOriginType, nullable = true)
        +listField("branches", branch, mutability = List, isChild = true)
    }
    konst branch: ElementConfig by element(Expression) {
        visitorParent = rootElement
        visitorParam = "branch"
        accept = true
        transform = true
        transformByChildren = true

        +field("condition", expression, isChild = true)
        +field("result", expression, isChild = true)
    }
    konst elseBranch: ElementConfig by element(Expression) {
        visitorParent = branch
        visitorParam = "branch"
        transform = true
        transformByChildren = true

        parent(branch)
    }
}
