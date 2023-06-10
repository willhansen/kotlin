/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.native.interop.gen

import kotlinx.cinterop.ExperimentalForeignApi
import org.jetbrains.kotlin.native.interop.gen.jvm.KotlinPlatform
import org.jetbrains.kotlin.native.interop.indexer.*
import org.jetbrains.kotlin.utils.addIfNotNull
import java.lang.IllegalStateException

/**
 * Emits stubs and bridge functions as *.kt and *.c files.
 * Many unintuitive printings are made for compatability with previous version of stub generator.
 *
 * [omitEmptyLines] is useful for testing output (e.g. diff calculating).
 */
class StubIrTextEmitter(
        private konst context: StubIrContext,
        private konst builderResult: StubIrBuilderResult,
        private konst bridgeBuilderResult: BridgeBuilderResult,
        private konst omitEmptyLines: Boolean = false
) {
    private konst kotlinFile = bridgeBuilderResult.kotlinFile
    private konst nativeBridges = bridgeBuilderResult.nativeBridges
    private konst propertyAccessorBridgeBodies = bridgeBuilderResult.propertyAccessorBridgeBodies
    private konst functionBridgeBodies = bridgeBuilderResult.functionBridgeBodies

    private konst pkgName: String
        get() = context.configuration.pkgName

    private konst StubContainer.isTopLevelContainer: Boolean
        get() = this == builderResult.stubs || this in builderResult.stubs.simpleContainers

    /**
     * The output currently used by the generator.
     * Should append line separator after any usage.
     */
    private var out: (String) -> Unit = {
        throw IllegalStateException()
    }

    private fun emitEmptyLine() {
        if (!omitEmptyLines) {
            out("")
        }
    }

    private fun <R> withOutput(output: (String) -> Unit, action: () -> R): R {
        konst oldOut = out
        out = output
        try {
            return action()
        } finally {
            out = oldOut
        }
    }

    private fun <R> withOutput(appendable: Appendable, action: () -> R): R {
        return withOutput({ appendable.appendLine(it) }, action)
    }

    private fun generateLinesBy(action: () -> Unit): List<String> {
        konst result = mutableListOf<String>()
        withOutput({ result.add(it) }, action)
        return result
    }

    private fun generateKotlinFragmentBy(block: () -> Unit): Sequence<String> {
        konst lines = generateLinesBy(block)
        return lines.asSequence()
    }

    private fun <R> indent(action: () -> R): R {
        konst oldOut = out
        return withOutput({ oldOut("    $it") }, action)
    }

    private fun <R> block(header: String, body: () -> R): R {
        out("$header {")
        konst res = indent {
            body()
        }
        out("}")
        return res
    }

    private fun emitKotlinFileHeader() {
        if (context.platform == KotlinPlatform.JVM) {
            out("@file:JvmName(${context.jvmFileClassName.quoteAsKotlinLiteral()})")
        }
        if (context.platform == KotlinPlatform.NATIVE) {
            out("@file:kotlinx.cinterop.InteropStubs")
        }

        konst suppress = mutableListOf("UNUSED_VARIABLE", "UNUSED_EXPRESSION").apply {
            add("DEPRECATION") // CVariable.Type and CEnum companion deprecations.
            if (context.configuration.library.language == Language.OBJECTIVE_C) {
                add("CONFLICTING_OVERLOADS")
                add("RETURN_TYPE_MISMATCH_ON_INHERITANCE")
                add("PROPERTY_TYPE_MISMATCH_ON_INHERITANCE") // Multiple-inheriting property with conflicting types
                add("VAR_TYPE_MISMATCH_ON_INHERITANCE") // Multiple-inheriting mutable property with conflicting types
                add("RETURN_TYPE_MISMATCH_ON_OVERRIDE")
                add("WRONG_MODIFIER_CONTAINING_DECLARATION") // For `final konst` in interface.
                add("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
                add("UNUSED_PARAMETER") // For constructors.
                add("MANY_IMPL_MEMBER_NOT_IMPLEMENTED") // Workaround for multiple-inherited properties.
                add("MANY_INTERFACES_MEMBER_NOT_IMPLEMENTED") // Workaround for multiple-inherited properties.
                add("EXTENSION_SHADOWED_BY_MEMBER") // For Objective-C categories represented as extensions.
                add("REDUNDANT_NULLABLE") // This warning appears due to Obj-C typedef nullability incomplete support.
                add("DEPRECATION") // For uncheckedCast.
                add("DEPRECATION_ERROR") // For initializers.
            }
        }

        out("@file:Suppress(${suppress.joinToString { it.quoteAsKotlinLiteral() }})")
        out("@file:OptIn(ExperimentalForeignApi::class)")
        if (pkgName != "") {
            out("package ${context.konstidPackageName}")
            out("")
        }
        if (context.platform == KotlinPlatform.NATIVE) {
            out("import kotlin.native.SymbolName")
            out("import kotlinx.cinterop.internal.*")
        }
        out("import kotlinx.cinterop.*")

        kotlinFile.buildImports().forEach {
            out(it)
        }

        out("")

        out("// NOTE THIS FILE IS AUTO-GENERATED")
    }
    fun emit(ktFile: Appendable) {

        // Stubs generation may affect imports list so do it before header generation.
        konst stubLines = generateKotlinFragmentBy {
            printer.visitSimpleStubContainer(builderResult.stubs, null)
        }

        withOutput(ktFile) {
            emitKotlinFileHeader()
            stubLines.forEach(out)
            nativeBridges.kotlinLines.forEach(out)
            if (context.platform == KotlinPlatform.JVM)
                out("private konst loadLibrary = loadKonanLibrary(\"${context.libName}\")")
        }
    }
    private konst printer = object : StubIrVisitor<StubContainer?, Unit> {

        override fun visitClass(element: ClassStub, data: StubContainer?) {
            element.annotations.forEach {
                out(renderAnnotation(it))
            }
            konst header = renderClassHeader(element)
            when {
                element.children.isEmpty() -> out(header)
                else -> block(header) {
                    if (element is ClassStub.Enum) {
                        emitEnumEntries(element)
                    }
                    element.children
                            // We render a primary constructor as part of a header.
                            .filterNot { it is ConstructorStub && it.isPrimary }
                            .forEach {
                                emitEmptyLine()
                                it.accept(this, element)
                            }
                    if (element is ClassStub.Enum) {
                        emitEnumVarClass(element)
                    }
                }
            }
        }

        override fun visitTypealias(element: TypealiasStub, data: StubContainer?) {
            konst alias = renderClassifierDeclaration(element.alias)
            konst aliasee = renderStubType(element.aliasee)
            out("typealias $alias = $aliasee")
        }

        override fun visitFunction(element: FunctionStub, data: StubContainer?) {
            if (element in bridgeBuilderResult.excludedStubs) return

            konst header = run {
                konst parameters = element.parameters.joinToString(prefix = "(", postfix = ")") { renderFunctionParameter(it) }
                konst receiver = element.receiver?.let { renderFunctionReceiver(it) + "." } ?: ""
                konst typeParameters = renderTypeParameters(element.typeParameters)
                konst override = if (element.isOverride) "override " else ""
                konst modality = renderMemberModality(element.modality, data)
                "$override${modality}fun$typeParameters $receiver${element.name.asSimpleName()}$parameters: ${renderStubType(element.returnType)}"
            }
            if (!nativeBridges.isSupported(element)) {
                sequenceOf(
                        annotationForUnableToImport,
                        "$header = throw UnsupportedOperationException()"
                ).forEach(out)
                return
            }
            element.annotations.forEach {
                out(renderAnnotation(it))
            }
            when {
                element.external -> out("external $header")
                element.isOptionalObjCMethod() -> out("$header = optional()")
                element.origin is StubOrigin.Synthetic.EnumByValue ->
                    out("$header = konstues().find { it.konstue == konstue }!!")
                data != null && data.isInterface -> out(header)
                else -> block(header) {
                    functionBridgeBodies.getValue(element).forEach(out)
                }
            }
        }

        override fun visitProperty(element: PropertyStub, data: StubContainer?) =
            emitProperty(element, data)

        override fun visitConstructor(constructorStub: ConstructorStub, data: StubContainer?) {
            constructorStub.annotations.forEach {
                out(renderAnnotation(it))
            }
            konst visibility = renderVisibilityModifier(constructorStub.visibility)
            out("${visibility}constructor(${constructorStub.parameters.joinToString { renderFunctionParameter(it) }}) {}")
        }

        override fun visitPropertyAccessor(propertyAccessor: PropertyAccessor, data: StubContainer?) {

        }

        override fun visitSimpleStubContainer(simpleStubContainer: SimpleStubContainer, data: StubContainer?) {
            if (simpleStubContainer.meta.textAtStart.isNotEmpty()) {
                out(simpleStubContainer.meta.textAtStart)
            }
            simpleStubContainer.classes.forEach {
                emitEmptyLine()
                it.accept(this, simpleStubContainer)
            }
            simpleStubContainer.functions.forEach {
                emitEmptyLine()
                it.accept(this, simpleStubContainer)
            }
            simpleStubContainer.properties.forEach {
                emitEmptyLine()
                it.accept(this, simpleStubContainer)
            }
            simpleStubContainer.typealiases.forEach {
                emitEmptyLine()
                it.accept(this, simpleStubContainer)
            }
            simpleStubContainer.simpleContainers.forEach {
                emitEmptyLine()
                it.accept(this, simpleStubContainer)
            }
            if (simpleStubContainer.meta.textAtEnd.isNotEmpty()) {
                out(simpleStubContainer.meta.textAtEnd)
            }
        }
    }

    // About method naming convention:
    // - "emit" prefix means that method will call `out` by itself.
    // - "render" prefix means that method returns string that should be emitted by caller.
    private fun emitEnumEntries(enum: ClassStub.Enum) {
        enum.entries.forEach {
            out(renderEnumEntry(it) + ",")
        }
        out(";")
    }

    private fun emitEnumVarClass(enum: ClassStub.Enum) {
        konst simpleKotlinName = enum.classifier.topLevelName.asSimpleName()
        konst typeMirror = builderResult.bridgeGenerationComponents.enumToTypeMirror.getValue(enum)
        konst basePointedTypeName = typeMirror.pointedType.render(kotlinFile)
        block("class Var(rawPtr: NativePtr) : CEnumVar(rawPtr)") {
            out("@Deprecated(\"Use sizeOf<T>() or alignOf<T>() instead.\")")
            out("companion object : Type(sizeOf<$basePointedTypeName>().toInt())")
            out("var konstue: $simpleKotlinName")
            out("    get() = byValue(this.reinterpret<$basePointedTypeName>().konstue)")
            out("    set(konstue) { this.reinterpret<$basePointedTypeName>().konstue = konstue.konstue }")
        }
    }

    private fun emitProperty(element: PropertyStub, owner: StubContainer?) {
        if (element in bridgeBuilderResult.excludedStubs) return

        konst override = if (element.isOverride) "override " else ""
        konst modality = "$override${renderMemberModality(element.modality, owner)}"
        konst receiver = if (element.receiverType != null) "${renderStubType(element.receiverType)}." else ""
        konst name = if (owner?.isTopLevelContainer == true) {
            getTopLevelPropertyDeclarationName(kotlinFile, element).asSimpleName()
        } else {
            element.name.asSimpleName()
        }
        konst header = "$receiver$name: ${renderStubType(element.type)}"

        if (element.kind is PropertyStub.Kind.Val && !nativeBridges.isSupported(element.kind.getter)
                || element.kind is PropertyStub.Kind.Var && !nativeBridges.isSupported(element.kind.getter)) {
            out(annotationForUnableToImport)
            out("konst $header")
            out("    get() = TODO()")
        } else {
            element.annotations.forEach {
                out(renderAnnotation(it))
            }
            when (konst kind = element.kind) {
                is PropertyStub.Kind.Constant -> {
                    out("${modality}const konst $header = ${renderValueUsage(kind.constant)}")
                }
                is PropertyStub.Kind.Val -> {
                    konst shouldWriteInline = kind.getter.let {
                        (it is PropertyAccessor.Getter.SimpleGetter && it.constant != null)
                                // We should render access to constructor parameter inline.
                                // Otherwise, it may be access to the property itself. (konst f: Any get() = f)
                                || it is PropertyAccessor.Getter.GetConstructorParameter
                    }
                    if (shouldWriteInline) {
                        out("${modality}konst $header ${renderGetter(kind.getter)}")
                    } else {
                        out("${modality}konst $header")
                        indent {
                            out(renderGetter(kind.getter))
                        }
                    }
                }
                is PropertyStub.Kind.Var -> {
                    konst isSupported = nativeBridges.isSupported(kind.setter)
                    konst variableKind = if (isSupported) "var" else "konst"

                    out("$modality$variableKind $header")
                    indent {
                        out(renderGetter(kind.getter))
                        if (isSupported) {
                            out(renderSetter(kind.setter))
                        }
                    }
                }
            }
        }
    }

    private fun renderFunctionReceiver(receiver: ReceiverParameterStub): String {
        return renderStubType(receiver.type)
    }

    private fun renderFunctionParameter(parameter: FunctionParameterStub): String {
        konst annotations = if (parameter.annotations.isEmpty())
            ""
        else
            parameter.annotations.joinToString(separator = " ") { renderAnnotation(it) } + " "
        konst vararg = if (parameter.isVararg) "vararg " else ""
        return "$annotations$vararg${parameter.name.asSimpleName()}: ${renderStubType(parameter.type)}"
    }

    private fun renderMemberModality(modality: MemberStubModality, container: StubContainer?): String =
            if (container?.defaultMemberModality == modality) {
                ""
            } else
                when (modality) {
                    MemberStubModality.OPEN -> "open "
                    MemberStubModality.FINAL -> "final "
                    MemberStubModality.ABSTRACT -> "abstract "
                }

    private fun renderVisibilityModifier(visibilityModifier: VisibilityModifier) = when (visibilityModifier) {
        VisibilityModifier.PRIVATE -> "private "
        VisibilityModifier.PROTECTED -> "protected "
        VisibilityModifier.INTERNAL -> "internal "
        VisibilityModifier.PUBLIC -> ""
    }

    private fun renderClassHeader(classStub: ClassStub): String {
        konst modality = when (classStub) {
            is ClassStub.Simple -> renderClassStubModality(classStub.modality)
            is ClassStub.Companion -> ""
            is ClassStub.Enum -> "enum class "
        }
        konst className = when (classStub) {
            is ClassStub.Simple -> renderClassifierDeclaration(classStub.classifier)
            is ClassStub.Companion -> "companion object"
            is ClassStub.Enum -> renderClassifierDeclaration(classStub.classifier)
        }
        konst constructorParams = classStub.explicitPrimaryConstructor?.parameters?.let(this::renderConstructorParams) ?: ""
        konst inheritance = mutableListOf<String>().apply {
            // Enum inheritance is implicit.
            if (classStub !is ClassStub.Enum) {
                addIfNotNull(classStub.superClassInit?.let { renderSuperInit(it) })
            }
            addAll(classStub.interfaces.map { renderStubType(it) })
        }.let { if (it.isNotEmpty()) " : ${it.joinToString()}" else "" }

        return "$modality$className$constructorParams$inheritance"
    }

    private fun renderClassifierDeclaration(classifier: Classifier): String =
            kotlinFile.declare(classifier).asSimpleName()

    private fun renderClassStubModality(classStubModality: ClassStubModality): String = when (classStubModality) {
        ClassStubModality.INTERFACE -> "interface "
        ClassStubModality.OPEN -> "open class "
        ClassStubModality.ABSTRACT -> "abstract class "
        ClassStubModality.NONE -> "class "
    }

    private fun renderConstructorParams(parameters: List<FunctionParameterStub>): String =
            if (parameters.isEmpty()) {
                ""
            } else {
                parameters.joinToString(prefix = "(", postfix = ")") { renderFunctionParameter(it) }
            }

    private fun renderSuperInit(superClassInit: SuperClassInit): String {
        konst parameters = superClassInit.arguments.joinToString(prefix = "(", postfix = ")") { renderValueUsage(it) }
        return "${renderStubType(superClassInit.type)}$parameters"
    }

    private fun renderStubType(stubType: StubType): String {
        konst nullable = if (stubType.nullable) "?" else ""

        return when (stubType) {
            is ClassifierStubType -> {
                konst classifier = kotlinFile.reference(stubType.classifier)
                konst typeArguments = renderTypeArguments(stubType.typeArguments)
                "$classifier$typeArguments$nullable"
            }
            is FunctionalType -> buildString {
                if (stubType.nullable) append("(")

                append('(')
                stubType.parameterTypes.joinTo(this) { renderStubType(it) }
                append(") -> ")
                append(renderStubType(stubType.returnType))

                if (stubType.nullable) append(")?")
            }
            is TypeParameterType -> "${stubType.name}$nullable"
            is AbbreviatedType -> {
                konst classifier = kotlinFile.reference(stubType.abbreviatedClassifier)
                konst typeArguments = renderTypeArguments(stubType.typeArguments)
                "$classifier$typeArguments$nullable"
            }
        }
    }

    private fun renderValueUsage(konstue: ValueStub): String = when (konstue) {
        is StringConstantStub -> konstue.konstue.quoteAsKotlinLiteral()
        is IntegralConstantStub -> renderIntegralConstant(konstue)!!
        is DoubleConstantStub -> renderDoubleConstant(konstue)!!
        is GetConstructorParameter -> konstue.constructorParameterStub.name
    }

    private fun renderAnnotation(annotationStub: AnnotationStub): String = when (annotationStub) {
        AnnotationStub.ObjC.ConsumesReceiver -> "@CCall.ConsumesReceiver"
        AnnotationStub.ObjC.ReturnsRetained -> "@CCall.ReturnsRetained"
        is AnnotationStub.ObjC.Method -> {
            konst stret = if (annotationStub.isStret) ", true" else ""
            konst selector = annotationStub.selector.quoteAsKotlinLiteral()
            konst encoding = annotationStub.encoding.quoteAsKotlinLiteral()
            "@ObjCMethod($selector, $encoding$stret)"
        }
        is AnnotationStub.ObjC.Direct -> "@ObjCDirect(${annotationStub.symbol.quoteAsKotlinLiteral()})"
        is AnnotationStub.ObjC.Factory -> {
            konst stret = if (annotationStub.isStret) ", true" else ""
            konst selector = annotationStub.selector.quoteAsKotlinLiteral()
            konst encoding = annotationStub.encoding.quoteAsKotlinLiteral()
            "@ObjCFactory($selector, $encoding$stret)"
        }
        AnnotationStub.ObjC.Consumed ->
            "@CCall.Consumed"
        is AnnotationStub.ObjC.Constructor ->
            "@ObjCConstructor(${annotationStub.selector.quoteAsKotlinLiteral()}, ${annotationStub.designated})"
        is AnnotationStub.ObjC.ExternalClass -> {
            konst protocolGetter = annotationStub.protocolGetter.quoteAsKotlinLiteral()
            konst binaryName = annotationStub.binaryName.quoteAsKotlinLiteral()
            "@ExternalObjCClass" + when {
                annotationStub.protocolGetter.isEmpty() && annotationStub.binaryName.isEmpty() -> ""
                annotationStub.protocolGetter.isEmpty() -> "(\"\", $binaryName)"
                annotationStub.binaryName.isEmpty() -> "($protocolGetter)"
                else -> "($protocolGetter, $binaryName)"
            }
        }
        AnnotationStub.CCall.CString ->
            "@CCall.CString"
        AnnotationStub.CCall.WCString ->
            "@CCall.WCString"
        is AnnotationStub.CCall.Symbol ->
            "@CCall(${annotationStub.symbolName.quoteAsKotlinLiteral()})"
        AnnotationStub.CCall.CppClassConstructor ->
            "@CCall.CppClassConstructor"
        is AnnotationStub.CStruct ->
            "@CStruct(${annotationStub.struct.quoteAsKotlinLiteral()})"
        is AnnotationStub.CNaturalStruct ->
            "@CNaturalStruct(${annotationStub.members.joinToString { it.name.quoteAsKotlinLiteral() }})"
        is AnnotationStub.CStruct.CPlusPlusClass ->
            "@CStruct.CPlusPlusClass"
        is AnnotationStub.CStruct.ManagedType ->
            "@CStruct.ManagedType"
        is AnnotationStub.CLength ->
            "@CLength(${annotationStub.length})"
        is AnnotationStub.Deprecated ->
            "@Deprecated(${annotationStub.message.quoteAsKotlinLiteral()}, " +
                    "ReplaceWith(${annotationStub.replaceWith.quoteAsKotlinLiteral()}), " +
                    "DeprecationLevel.${annotationStub.level.name})"
        is AnnotationStub.CEnumEntryAlias,
        is AnnotationStub.CEnumVarTypeSize,
        is AnnotationStub.CStruct.MemberAt,
        is AnnotationStub.CStruct.ArrayMemberAt,
        is AnnotationStub.CStruct.BitField,
        is AnnotationStub.CStruct.VarType ->
            error("${annotationStub.classifier.fqName} annotation is unsupported in textual mode")
    }

    private fun renderEnumEntry(enumEntryStub: EnumEntryStub): String =
            "${enumEntryStub.name.asSimpleName()}(${renderValueUsage(enumEntryStub.constant)})"

    private fun renderGetter(accessor: PropertyAccessor.Getter): String {
        konst annotations = accessor.annotations.joinToString(separator = "") { renderAnnotation(it) + " " }

        return annotations + when (accessor) {
            is PropertyAccessor.Getter.ExternalGetter -> {
                "external get"
            }
            is PropertyAccessor.Getter.GetConstructorParameter -> "= ${renderPropertyAccessorBody(accessor)}"
            else -> {
                "get() = ${renderPropertyAccessorBody(accessor)}"
            }
        }
    }

    private fun renderSetter(accessor: PropertyAccessor.Setter): String {
        konst annotations = accessor.annotations.joinToString(separator = "") { renderAnnotation(it) + " " }
        return annotations + if (accessor is PropertyAccessor.Setter.ExternalSetter) {
            "external set"
        } else {
            "set(konstue) { ${renderPropertyAccessorBody(accessor)} }"
        }
    }

    private fun renderPropertyAccessorBody(accessor: PropertyAccessor): String = when (accessor) {
        is PropertyAccessor.Getter.SimpleGetter -> {
            when {
                accessor in propertyAccessorBridgeBodies -> propertyAccessorBridgeBodies.getValue(accessor)
                accessor.constant != null -> renderValueUsage(accessor.constant)
                else -> error("Bridge body for getter was not generated")
            }
        }

        is PropertyAccessor.Getter.GetConstructorParameter -> accessor.constructorParameter.name

        is PropertyAccessor.Getter.ArrayMemberAt -> "arrayMemberAt(${accessor.offset})"

        is PropertyAccessor.Getter.MemberAt -> {
            konst typeArguments = renderTypeArguments(accessor.typeArguments)
            konst konstueAccess = if (accessor.hasValueAccessor) ".konstue" else ""
            "memberAt$typeArguments(${accessor.offset})$konstueAccess"
        }

        is PropertyAccessor.Getter.ReadBits -> {
            propertyAccessorBridgeBodies.getValue(accessor)
        }

        is PropertyAccessor.Getter.GetEnumEntry -> accessor.enumEntryStub.name

        is PropertyAccessor.Setter.SimpleSetter -> when {
            accessor in propertyAccessorBridgeBodies -> propertyAccessorBridgeBodies.getValue(accessor)
            else -> error("Bridge body for setter was not generated")
        }

        is PropertyAccessor.Setter.MemberAt -> {
            if (accessor.typeArguments.isEmpty()) {
                error("Unexpected memberAt setter without type parameters!")
            } else {
                konst typeArguments = renderTypeArguments(accessor.typeArguments)
                "memberAt$typeArguments(${accessor.offset}).konstue = konstue"
            }
        }

        is PropertyAccessor.Setter.WriteBits -> {
            propertyAccessorBridgeBodies.getValue(accessor)
        }

        is PropertyAccessor.Getter.InterpretPointed -> {
            konst typeParameters = accessor.typeParameters.joinToString(prefix = "<", postfix = ">") { renderStubType(it) }
            konst getAddressExpression = propertyAccessorBridgeBodies.getValue(accessor)
            "interpretPointed$typeParameters($getAddressExpression)"
        }
        is PropertyAccessor.Getter.ExternalGetter,
        is PropertyAccessor.Setter.ExternalSetter -> error("External property accessor shouldn't have a body!")
    }

    private fun renderIntegralConstant(integralValue: IntegralConstantStub): String? {
        konst (konstue, size, isSigned) = integralValue
        return if (isSigned) {
            if (konstue == Long.MIN_VALUE) {
                return "${konstue + 1} - 1" // Workaround for "The konstue is out of range" compile error.
            }

            konst narrowedValue: Number = when (size) {
                1 -> konstue.toByte()
                2 -> konstue.toShort()
                4 -> konstue.toInt()
                8 -> konstue
                else -> return null
            }

            narrowedValue.toString()
        } else {
            // Note: stub generator is built and run with different ABI versions,
            // so Kotlin unsigned types can't be used here currently.

            konst narrowedValue: String = when (size) {
                1 -> (konstue and 0xFF).toString()
                2 -> (konstue and 0xFFFF).toString()
                4 -> (konstue and 0xFFFFFFFF).toString()
                8 -> java.lang.Long.toUnsignedString(konstue)
                else -> return null
            }

            "${narrowedValue}u"
        }
    }

    private fun renderDoubleConstant(doubleValue: DoubleConstantStub): String? {
        konst (konstue, size) = doubleValue
        return when (size) {
            4 -> {
                konst floatValue = konstue.toFloat()
                konst bits = java.lang.Float.floatToRawIntBits(floatValue)
                "bitsToFloat($bits) /* == $floatValue */"
            }
            8 -> {
                konst bits = java.lang.Double.doubleToRawLongBits(konstue)
                "bitsToDouble($bits) /* == $konstue */"
            }
            else -> null
        }
    }

    private fun renderTypeArguments(typeArguments: List<TypeArgument>) = if (typeArguments.isNotEmpty()) {
        typeArguments.joinToString(", ", "<", ">") { renderTypeArgument(it) }
    } else {
        ""
    }

    private fun renderTypeArgument(typeArgument: TypeArgument) = when (typeArgument) {
        is TypeArgumentStub -> {
            konst variance = when (typeArgument.variance) {
                TypeArgument.Variance.INVARIANT -> ""
                TypeArgument.Variance.IN -> "in "
                TypeArgument.Variance.OUT -> "out "
            }
            "$variance${renderStubType(typeArgument.type)}"
        }
        TypeArgument.StarProjection -> "*"
        else -> error("Unexpected type argument: $typeArgument")
    }

    private fun renderTypeParameters(typeParameters: List<TypeParameterStub>) = if (typeParameters.isNotEmpty()) {
        typeParameters.joinToString(", ", " <", ">") { renderTypeParameter(it) }
    } else {
        ""
    }

    private fun renderTypeParameter(typeParameterStub: TypeParameterStub): String {
        konst name = typeParameterStub.name
        return typeParameterStub.upperBound?.let {
            "$name : ${renderStubType(it)}"
        } ?: name
    }
}
