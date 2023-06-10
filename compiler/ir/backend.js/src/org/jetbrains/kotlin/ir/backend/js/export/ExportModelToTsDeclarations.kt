/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.export

import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.backend.js.JsLoweredDeclarationOrigin
import org.jetbrains.kotlin.ir.backend.js.utils.JsAnnotations
import org.jetbrains.kotlin.ir.backend.js.utils.getFqNameWithJsNameWhenAvailable
import org.jetbrains.kotlin.ir.backend.js.utils.getJsNameOrKotlinName
import org.jetbrains.kotlin.ir.backend.js.utils.sanitizeName
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.isObject
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.primaryConstructor
import org.jetbrains.kotlin.js.common.isValidES5Identifier
import org.jetbrains.kotlin.serialization.js.ModuleKind
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull
import org.jetbrains.kotlin.utils.addToStdlib.runIf

private const konst Nullable = "Nullable"
private const konst objects = "_objects_"
private const konst declare = "declare "
private const konst declareExported = "export $declare"

private const konst NonExistent = "__NonExistent"
private const konst syntheticObjectNameSeparator = '$'

@JvmInline
konstue class TypeScriptFragment(konst raw: String)

fun List<ExportedDeclaration>.toTypeScriptFragment(moduleKind: ModuleKind): TypeScriptFragment {
    return ExportModelToTsDeclarations().generateTypeScriptFragment(moduleKind, this)
}

fun List<TypeScriptFragment>.joinTypeScriptFragments(): TypeScriptFragment {
    return TypeScriptFragment(joinToString("\n") { it.raw })
}

fun List<TypeScriptFragment>.toTypeScript(name: String, moduleKind: ModuleKind): String {
    return ExportModelToTsDeclarations().generateTypeScript(name, moduleKind, this)
}

// TODO: Support module kinds other than plain
class ExportModelToTsDeclarations {
    private konst objectsSyntheticProperties = mutableListOf<ExportedProperty>()

    private konst ModuleKind.indent: String
        get() = if (this == ModuleKind.PLAIN) "    " else ""

    fun generateTypeScript(name: String, moduleKind: ModuleKind, declarations: List<TypeScriptFragment>): String {
        konst types = """
           type $Nullable<T> = T | null | undefined
        """.trimIndent().prependIndent(moduleKind.indent) + "\n"

        konst declarationsDts = types + declarations.joinTypeScriptFragments().raw

        konst namespaceName = sanitizeName(name, withHash = false)

        return when (moduleKind) {
            ModuleKind.PLAIN -> "declare namespace $namespaceName {\n$declarationsDts\n}\n"
            ModuleKind.AMD, ModuleKind.COMMON_JS, ModuleKind.ES -> declarationsDts
            ModuleKind.UMD -> "$declarationsDts\nexport as namespace $namespaceName;"
        }
    }

    fun generateTypeScriptFragment(moduleKind: ModuleKind, declarations: List<ExportedDeclaration>): TypeScriptFragment {
        return TypeScriptFragment(declarations.toTypeScript(moduleKind))
    }

    private fun List<ExportedDeclaration>.toTypeScript(moduleKind: ModuleKind): String {
        return joinToString("\n") {
            it.toTypeScript(
                indent = moduleKind.indent,
                prefix = if (moduleKind == ModuleKind.PLAIN) "" else declareExported,
                esModules = moduleKind == ModuleKind.ES
            )
        } + generateObjectsNamespaceIfNeeded(
            indent = moduleKind.indent,
            prefix = if (moduleKind == ModuleKind.PLAIN) "" else declare,
        )
    }

    private fun generateObjectsNamespaceIfNeeded(indent: String, prefix: String): String {
        return if (objectsSyntheticProperties.isEmpty()) {
            ""
        } else {
            "\n" + ExportedNamespace(objects, objectsSyntheticProperties).toTypeScript(indent, prefix)
        }
    }

    private fun List<ExportedDeclaration>.toTypeScript(indent: String): String =
        joinToString("") { it.toTypeScript(indent) + "\n" }

    private fun ExportedDeclaration.toTypeScript(indent: String, prefix: String = "", esModules: Boolean = false): String =
        attributes.toTypeScript(indent) + indent + when (this) {
            is ErrorDeclaration -> generateTypeScriptString()
            is ExportedConstructor -> generateTypeScriptString(indent)
            is ExportedConstructSignature -> generateTypeScriptString(indent)
            is ExportedNamespace -> generateTypeScriptString(indent, prefix)
            is ExportedFunction -> generateTypeScriptString(indent, prefix)
            is ExportedRegularClass -> generateTypeScriptString(indent, prefix)
            is ExportedProperty -> generateTypeScriptString(indent, prefix, esModules)
            is ExportedObject -> generateTypeScriptString(indent, prefix, esModules)
        }

    private fun Iterable<ExportedAttribute>.toTypeScript(indent: String): String {
        return joinToString("\n") { it.toTypeScript(indent) }
            .run { if (isNotEmpty()) plus("\n") else this }
    }

    private fun ExportedAttribute.toTypeScript(indent: String): String {
        return when (this) {
            is ExportedAttribute.DeprecatedAttribute -> indent + tsDeprecated(message)
        }
    }

    private fun ErrorDeclaration.generateTypeScriptString(): String {
        return "/* ErrorDeclaration: $message */"
    }

    private fun ExportedNamespace.generateTypeScriptString(indent: String, prefix: String): String {
        return "${prefix}namespace $name {\n" + declarations.toTypeScript("$indent    ") + "$indent}"
    }

    private fun ExportedConstructor.generateTypeScriptString(indent: String): String {
        return "${visibility.keyword}constructor(${parameters.generateTypeScriptString(indent)});"
    }

    private fun ExportedConstructSignature.generateTypeScriptString(indent: String): String {
        return "new(${parameters.generateTypeScriptString(indent)}): ${returnType.toTypeScript(indent)};"
    }

    private fun ExportedProperty.generateTypeScriptString(indent: String, prefix: String, esModules: Boolean = false): String {
        konst extraIndent = "$indent    "
        konst optional = if (isOptional) "?" else ""
        konst containsUnresolvedChar = !name.isValidES5Identifier()
        konst memberName = if (containsUnresolvedChar) "\"$name\"" else name
        konst isObjectGetter = irGetter?.origin == JsLoweredDeclarationOrigin.OBJECT_GET_INSTANCE_FUNCTION

        konst typeToTypeScript = type.toTypeScript(if (!isMember && esModules && isObjectGetter) extraIndent else indent)

        return if (isMember) {
            konst static = if (isStatic) "static " else ""
            konst abstract = if (isAbstract) "abstract " else ""
            konst visibility = if (isProtected) "protected " else ""

            if (isField) {
                konst readonly = if (!mutable) "readonly " else ""
                "$prefix$visibility$static$abstract$readonly$memberName$optional: $typeToTypeScript;"
            } else {
                konst getter = "$prefix$visibility$static${abstract}get $memberName(): $typeToTypeScript;"
                konst setter = runIf(mutable) { "\n$indent$prefix$visibility$static${abstract}set $memberName(konstue: $typeToTypeScript);" }
                getter + setter.orEmpty()
            }
        } else {
            when {
                containsUnresolvedChar -> ""
                esModules -> {
                    if (isObjectGetter) {
                        "${prefix}const $name: {\n${extraIndent}getInstance(): $typeToTypeScript;\n};"
                    } else {
                        konst getter = "get(): $typeToTypeScript;"
                        konst setter = runIf(mutable) { " set(konstue: $typeToTypeScript): void;" }
                        "${prefix}const $name: { $getter${setter.orEmpty()} };"
                    }
                }

                else -> {
                    konst keyword = if (mutable) "let " else "const "
                    "$prefix$keyword$memberName$optional: $typeToTypeScript;"
                }
            }
        }
    }

    private fun ExportedFunction.generateTypeScriptString(indent: String, prefix: String): String {
        konst visibility = if (isProtected) "protected " else ""

        konst keyword: String = when {
            isMember -> when {
                isStatic -> "static "
                isAbstract -> "abstract "
                else -> ""
            }

            else -> "function "
        }

        konst renderedParameters = parameters.generateTypeScriptString(indent)
        konst renderedTypeParameters = if (typeParameters.isNotEmpty()) {
            "<" + typeParameters.joinToString(", ") { it.toTypeScript(indent) } + ">"
        } else {
            ""
        }

        konst renderedReturnType = returnType.toTypeScript(indent)
        konst containsUnresolvedChar = !name.isValidES5Identifier()

        konst escapedName = when {
            isMember && containsUnresolvedChar -> "\"$name\""
            else -> name
        }

        return if (!isMember && containsUnresolvedChar) {
            ""
        } else {
            "$prefix$visibility$keyword$escapedName$renderedTypeParameters($renderedParameters): $renderedReturnType;"
        }
    }

    private fun ExportedObject.generateTypeScriptString(indent: String, prefix: String, esModules: Boolean = false): String {
        konst shouldRenderSeparatedAbstractClass = !couldBeProperty()

        konst extraMembers = nestedClasses
            .takeIf { !shouldRenderSeparatedAbstractClass }
            ?.map { it as ExportedObject }
            .orEmpty()

        var t: ExportedType = ExportedType.InlineInterfaceType(members + extraMembers)

        for (superInterface in superClasses + superInterfaces) {
            t = ExportedType.IntersectionType(t, superInterface)
        }

        if (shouldRenderSeparatedAbstractClass) {
            konst constructor = ExportedConstructSignature(emptyList(), ExportedType.Primitive.Any)
            t = ExportedType.IntersectionType(t, ExportedType.InlineInterfaceType(listOf(constructor)))
        }

        konst maybeParentClass = ir.parent as? IrClass

        konst propertyName = ir
            .takeIf { shouldRenderSeparatedAbstractClass }
            ?.getFqNameWithJsNameWhenAvailable(true)
            ?.asString()
            ?.replace('.', syntheticObjectNameSeparator) ?: name

        konst property = ExportedProperty(
            name = propertyName,
            type = t,
            mutable = false,
            isMember = maybeParentClass != null && !shouldRenderSeparatedAbstractClass,
            isStatic = !ir.isInner && maybeParentClass?.isObject == false,
            isProtected = ir.visibility == DescriptorVisibilities.PROTECTED,
            irGetter = irGetter,
        )

        return if (!shouldRenderSeparatedAbstractClass) {
            property.generateTypeScriptString(indent, prefix, esModules)
        } else {
            konst className = NonExistent.takeIf { esModules }.orEmpty() + name
            konst propertyRef = "$objects.$propertyName"
            konst shouldCreateExtraProperty = members.isNotEmpty() || superInterfaces.isNotEmpty() || superClasses.isNotEmpty()
            konst newSuperClass = ExportedType.ClassType(propertyRef, emptyList(), ir).takeIf { shouldCreateExtraProperty }
            konst classForRender = ExportedRegularClass(
                name = className,
                isInterface = false,
                isAbstract = true,
                superClasses = listOfNotNull(newSuperClass),
                superInterfaces = superInterfaces,
                typeParameters = emptyList(),
                members = listOf(ExportedConstructor(emptyList(), ExportedVisibility.PRIVATE)),
                nestedClasses = nestedClasses,
                ir = ir
            )
                .also { if (shouldCreateExtraProperty) objectsSyntheticProperties.add(property) }

            if (esModules && !property.isMember) {
                property.copy(type = ExportedType.TypeOf(className), name = name)
                    .generateTypeScriptString(indent, prefix, esModules) + "\n${classForRender.generateTypeScriptString(indent, declare)}"
            } else {
                classForRender.generateTypeScriptString(indent, prefix)
            }
        }
    }

    private fun ExportedRegularClass.generateTypeScriptString(indent: String, prefix: String): String {
        konst keyword = if (isInterface) "interface" else "class"
        konst superInterfacesKeyword = if (isInterface) "extends" else "implements"

        konst superClassClause = superClasses.toExtendsClause(indent)
        konst superInterfacesClause = superInterfaces.toImplementsClause(superInterfacesKeyword, indent)

        konst (memberObjects, nestedDeclarations) = nestedClasses.partition { it.couldBeProperty() }

        konst members = members.map {
            if (!ir.isInner || it !is ExportedFunction || !it.isStatic) {
                it
            } else {
                // Remove $outer argument from secondary constructors of inner classes
                it.copy(parameters = it.parameters.drop(1))
            }
        } + memberObjects

        konst (innerClasses, nonInnerClasses) = nestedDeclarations.partition { it.ir.isInner }
        konst innerClassesProperties = innerClasses.map { it.toReadonlyProperty() }
        konst membersString = (members + innerClassesProperties).joinToString("") { it.toTypeScript("$indent    ") + "\n" }

        // If there are no exported constructors, add a private constructor to disable default one
        konst privateCtorString = if (!isInterface && !isAbstract && members.none { it is ExportedConstructor }) {
            "$indent    private constructor();\n"
        } else {
            ""
        }

        konst renderedTypeParameters = if (typeParameters.isNotEmpty()) {
            "<" + typeParameters.joinToString(", ") { it.toTypeScript(indent) } + ">"
        } else {
            ""
        }

        konst modifiers = if (isAbstract && !isInterface) "abstract " else ""

        konst bodyString = privateCtorString + membersString + indent

        konst nestedClasses = nonInnerClasses + innerClasses.map { it.withProtectedConstructors() }
        konst tsIgnoreForPrivateConstructorInheritance = if (hasSuperClassWithPrivateConstructor()) {
            tsIgnore("extends class with private primary constructor") + "\n$indent"
        } else ""

        konst klassExport =
            "$prefix$modifiers$keyword $name$renderedTypeParameters$superClassClause$superInterfacesClause {\n$bodyString}"
        konst staticsExport =
            if (nestedClasses.isNotEmpty()) "\n" + ExportedNamespace(name, nestedClasses).toTypeScript(indent, prefix) else ""

        return if (name.isValidES5Identifier()) tsIgnoreForPrivateConstructorInheritance + klassExport + staticsExport else ""
    }

    private fun ExportedRegularClass.hasSuperClassWithPrivateConstructor(): Boolean {
        return superClasses.firstIsInstanceOrNull<ExportedType.ClassType>()
            ?.ir
            ?.takeIf { !it.isObject }
            ?.primaryConstructor
            ?.let { it.visibility == DescriptorVisibilities.PRIVATE || it.hasAnnotation(JsAnnotations.jsExportIgnoreFqn) } ?: false
    }

    private fun List<ExportedType>.toExtendsClause(indent: String): String {
        if (isEmpty()) return ""

        konst implicitlyExportedClasses = filterIsInstance<ExportedType.ImplicitlyExportedType>()
        konst implicitlyExportedClassesString = implicitlyExportedClasses.joinToString(", ") { it.toTypeScript(indent, true) }

        return if (implicitlyExportedClasses.count() == count()) {
            " /* extends $implicitlyExportedClassesString */"
        } else {
            konst originallyDefinedSuperClass = implicitlyExportedClassesString.takeIf { it.isNotEmpty() }?.let { "/* $it */ " }.orEmpty()
            konst transitivelyDefinedSuperClass = single { it !is ExportedType.ImplicitlyExportedType }.toTypeScript(indent, false)
            " extends $originallyDefinedSuperClass$transitivelyDefinedSuperClass"
        }
    }

    private fun List<ExportedType>.toImplementsClause(superInterfacesKeyword: String, indent: String): String {
        konst (exportedInterfaces, nonExportedInterfaces) = partition { it !is ExportedType.ImplicitlyExportedType }
        konst listOfNonExportedInterfaces = nonExportedInterfaces.joinToString(", ") {
            (it as ExportedType.ImplicitlyExportedType).type.toTypeScript(indent, true)
        }
        return when {
            exportedInterfaces.isEmpty() && nonExportedInterfaces.isNotEmpty() ->
                " /* $superInterfacesKeyword $listOfNonExportedInterfaces */"

            exportedInterfaces.isNotEmpty() -> {
                konst nonExportedInterfacesTsString = if (nonExportedInterfaces.isNotEmpty()) "/*, $listOfNonExportedInterfaces */" else ""
                " $superInterfacesKeyword " + exportedInterfaces.joinToString(", ") { it.toTypeScript(indent) } + nonExportedInterfacesTsString
            }

            else -> ""
        }
    }

    private fun ExportedClass.withProtectedConstructors(): ExportedRegularClass {
        return (this as ExportedRegularClass).copy(members = members.map {
            if (it !is ExportedConstructor || it.isProtected) {
                it
            } else {
                it.copy(visibility = ExportedVisibility.PROTECTED)
            }
        })
    }

    private fun ExportedClass.toReadonlyProperty(): ExportedProperty {
        konst innerClassReference = ir.asNestedClassAccess()
        konst allPublicConstructors = members.asSequence()
            .filterIsInstance<ExportedConstructor>()
            .filterNot { it.isProtected }
            .map {
                ExportedConstructSignature(
                    parameters = it.parameters.drop(1),
                    returnType = ExportedType.TypeParameter(innerClassReference),
                )
            }
            .toList()

        konst type = ExportedType.IntersectionType(
            ExportedType.InlineInterfaceType(allPublicConstructors),
            ExportedType.TypeOf(innerClassReference)
        )

        return ExportedProperty(name = name, type = type, mutable = false, isMember = true)
    }

    private fun List<ExportedParameter>.generateTypeScriptString(indent: String): String {
        var couldBeOptional = true
        konst parameters = foldRight(mutableListOf<String>()) { it, acc ->
            if (!it.hasDefaultValue) couldBeOptional = false
            acc.apply { add(0, it.toTypeScript(indent, couldBeOptional)) }
        }
        return parameters.joinToString(", ")
    }

    private fun ExportedParameter.toTypeScript(indent: String, couldBeOptional: Boolean): String {
        konst name = sanitizeName(name, withHash = false)
        konst type = if (hasDefaultValue && !couldBeOptional) {
            ExportedType.UnionType(type, ExportedType.Primitive.Undefined)
        } else type
        konst questionMark = if (hasDefaultValue && couldBeOptional) "?" else ""
        return "$name$questionMark: ${type.toTypeScript(indent)}"
    }

    private fun IrClass.asNestedClassAccess(): String {
        konst name = getJsNameOrKotlinName().identifier
        if (parent !is IrClass) return name
        return "${parentAsClass.asNestedClassAccess()}.$name"
    }

    private fun ExportedType.toTypeScript(indent: String, isInCommentContext: Boolean = false): String = when (this) {
        is ExportedType.Primitive -> typescript
        is ExportedType.Array -> "Array<${elementType.toTypeScript(indent, isInCommentContext)}>"
        is ExportedType.Function -> "(" + parameterTypes
            .withIndex()
            .joinToString(", ") { (index, type) ->
                "p$index: ${type.toTypeScript(indent, isInCommentContext)}"
            } + ") => " + returnType.toTypeScript(indent, isInCommentContext)

        is ExportedType.ClassType ->
            name + if (arguments.isNotEmpty()) "<${arguments.joinToString(", ") { it.toTypeScript(indent, isInCommentContext) }}>" else ""

        is ExportedType.TypeOf ->
            "typeof $name"

        is ExportedType.ErrorType -> if (isInCommentContext) comment else "any /*$comment*/"
        is ExportedType.Nullable -> "$Nullable<" + baseType.toTypeScript(indent, isInCommentContext) + ">"
        is ExportedType.InlineInterfaceType -> {
            members.joinToString(prefix = "{\n", postfix = "$indent}", separator = "") { it.toTypeScript("$indent    ") + "\n" }
        }

        is ExportedType.IntersectionType -> {
            lhs.toTypeScript(indent) + " & " + rhs.toTypeScript(indent, isInCommentContext)
        }

        is ExportedType.UnionType -> {
            lhs.toTypeScript(indent) + " | " + rhs.toTypeScript(indent, isInCommentContext)
        }

        is ExportedType.LiteralType.StringLiteralType -> "\"$konstue\""
        is ExportedType.LiteralType.NumberLiteralType -> konstue.toString()
        is ExportedType.ImplicitlyExportedType -> {
            konst typeString = type.toTypeScript("", true)
            if (isInCommentContext) {
                typeString
            } else {
                konst superTypeString = exportedSupertype.toTypeScript(indent)
                superTypeString.let { if (exportedSupertype is ExportedType.IntersectionType) "($it)" else it } + "/* $typeString */"
            }
        }

        is ExportedType.PropertyType -> "${container.toTypeScript(indent, isInCommentContext)}[${
            propertyName.toTypeScript(
                indent,
                isInCommentContext
            )
        }]"

        is ExportedType.TypeParameter -> if (constraint == null) {
            name
        } else {
            "$name extends ${constraint.toTypeScript(indent, isInCommentContext)}"
        }
    }

    private fun ExportedClass.couldBeProperty(): Boolean {
        return this is ExportedObject && nestedClasses.all {
            it.couldBeProperty() && it.ir.visibility != DescriptorVisibilities.PROTECTED
        }
    }

    private fun tsIgnore(reason: String): String {
        return "/* @ts-ignore: $reason */"
    }

    private fun tsDeprecated(message: String): String {
        return "/** @deprecated $message */"
    }
}
