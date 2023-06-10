/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.tree.generator.printer

import org.jetbrains.kotlin.fir.tree.generator.declarationAttributesType
import org.jetbrains.kotlin.fir.tree.generator.model.*
import org.jetbrains.kotlin.util.SmartPrinter
import org.jetbrains.kotlin.util.withIndent
import java.io.File

fun Builder.generateCode(generationPath: File): GeneratedFile {
    konst dir = generationPath.resolve(packageName.replace(".", "/"))
    konst file = File(dir, "$type.kt")
    konst stringBuilder = StringBuilder()
    SmartPrinter(stringBuilder).apply {
        printCopyright()
        println("@file:Suppress(\"DuplicatedCode\")")
        println()
        println("package $packageName")
        println()
        konst imports = collectImports()
        imports.forEach { println("import $it") }
        if (imports.isNotEmpty()) {
            println()
        }
        printGeneratedMessage()
        printBuilder(this@generateCode)
    }
    return GeneratedFile(file, stringBuilder.toString())
}

private fun SmartPrinter.printBuilder(builder: Builder) {
    if (builder is LeafBuilder && builder.allFields.isEmpty()) {
        printDslBuildFunction(builder, false)
        return
    }

    println("@FirBuilderDsl")
    when (builder) {
        is IntermediateBuilder -> print("interface ")
        is LeafBuilder -> {
            if (builder.isOpen) {
                print("open ")
            }
            print("class ")
        }
    }
    print(builder.typeWithArguments)
    if (builder.parents.isNotEmpty()) {
        print(builder.parents.joinToString(separator = ", ", prefix = " : ") { it.type })
    }
    var hasRequiredFields = false
    println(" {")
    withIndent {
        var needNewLine = false
        for (field in builder.allFields) {
            konst (newLine, requiredFields) = printFieldInBuilder(field, builder, fieldIsUseless = false)
            needNewLine = newLine
            hasRequiredFields = hasRequiredFields || requiredFields
        }
        konst hasBackingFields = builder.allFields.any { it.nullable }
        if (needNewLine) {
            println()
        }
        konst buildType = when (builder) {
            is LeafBuilder -> builder.implementation.element.typeWithArguments
            is IntermediateBuilder -> builder.materializedElement!!.typeWithArguments.replace(Regex("<.>"), "<*>")
        }
        if (builder is LeafBuilder && builder.implementation.isPublic) {
            println("@OptIn(FirImplementationDetail::class)")
        }
        if (builder.parents.isNotEmpty()) {
            print("override ")
        }
        print("fun build(): $buildType")
        if (builder is LeafBuilder) {
            println(" {")
            withIndent {
                println("return ${builder.implementation.type}(")
                withIndent {
                    for (field in builder.allFields) {
                        if (field.invisibleField) continue
                        konst name = field.name
                        print(name)
                        if (field.isMutableOrEmpty) print(".toMutableOrEmpty()")
                        println(",")
                    }
                }
                println(")")
            }
            println("}")
            if (hasBackingFields) {
                println()
            }
        } else {
            println()
        }

        if (builder is LeafBuilder) {
//            for (field in builder.allFields) {
//                printBackingFieldIfNeeded(field)
//            }

            konst hasUselessFields = builder.uselessFields.isNotEmpty()
            if (hasUselessFields) {
                println()
                builder.uselessFields.forEachIndexed { index, field ->
                    if (index > 0) {
                        println()
                    }
                    printFieldInBuilder(field, builder, fieldIsUseless = true)
                }
            }
        }
    }
    println("}")
    if (builder is LeafBuilder) {
        println()
        printDslBuildFunction(builder, hasRequiredFields)

        if (builder.wantsCopy) {
            println()
            printDslBuildCopyFunction(builder, hasRequiredFields)
        }
    }
}

internal konst Field.invisibleField: Boolean get() = customInitializationCall != null

private konst String.nullable: String get() = if (endsWith("?")) this else "$this?"
private fun FieldWithDefault.needBackingField(fieldIsUseless: Boolean) =
    (!nullable || notNull) && origin !is FieldList && if (fieldIsUseless) {
        defaultValueInImplementation == null
    } else {
        defaultValueInBuilder == null
    }

private fun FieldWithDefault.needNotNullDelegate(fieldIsUseless: Boolean) = needBackingField(fieldIsUseless) && (type == "Boolean" || type == "Int")


private fun SmartPrinter.printFieldInBuilder(field: FieldWithDefault, builder: Builder, fieldIsUseless: Boolean): Pair<Boolean, Boolean> {
    if (field.withGetter && !fieldIsUseless || field.invisibleField) return false to false
    if (field.origin is FieldList) {
        printFieldListInBuilder(field.origin, builder, fieldIsUseless)
        return true to false
    }
    konst name = field.name
    konst type = field.getTypeWithArguments(field.notNull)
    konst defaultValue = if (fieldIsUseless)
        field.defaultValueInImplementation.also { requireNotNull(it) }
    else
        field.defaultValueInBuilder

    printDeprecationOnUselessFieldIfNeeded(field, builder, fieldIsUseless)
    printModifiers(builder, field, fieldIsUseless)
    print("var $name: $type")
    var hasRequiredFields = false
    konst needNewLine = when {
        fieldIsUseless -> {
            println()
            withIndent {
                println("get() = throw IllegalStateException()")
                println("set(_) {")
                withIndent {
                    println("throw IllegalStateException()")
                }
                println("}")
            }
            true
        }

        builder is IntermediateBuilder -> {
            println()
            false
        }
        field.needNotNullDelegate(fieldIsUseless) -> {
            println(" by kotlin.properties.Delegates.notNull<${field.type}>()")
            hasRequiredFields = true
            true
        }

        field.needBackingField(fieldIsUseless) -> {
//            println()
//            withIndent {
//                println("get() = _$name ?: throw IllegalArgumentException(\"$name should be initialized\")")
//                println("set(konstue) {")
//                withIndent {
//                    println("_$name = konstue")
//                }
//                println("}")
//                println()
//            }
//            false
            println()
            hasRequiredFields = true
            true
        }
        else -> {
            println(" = $defaultValue")
            true
        }
    }
    return needNewLine to hasRequiredFields
}

private fun SmartPrinter.printDeprecationOnUselessFieldIfNeeded(field: Field, builder: Builder, fieldIsUseless: Boolean) {
    if (fieldIsUseless) {
        println("@Deprecated(\"Modification of '${field.name}' has no impact for ${builder.type}\", level = DeprecationLevel.HIDDEN)")
    }
}

private fun SmartPrinter.printFieldListInBuilder(field: FieldList, builder: Builder, fieldIsUseless: Boolean) {
    printDeprecationOnUselessFieldIfNeeded(field, builder, fieldIsUseless)
    printModifiers(builder, field, fieldIsUseless)
    print("konst ${field.name}: ${field.getMutableType(forBuilder = true)}")
    if (builder is LeafBuilder) {
        print(" = mutableListOf()")
    }
    println()
}

private fun SmartPrinter.printModifiers(builder: Builder, field: Field, fieldIsUseless: Boolean) {
    if (builder is IntermediateBuilder) {
        print("abstract ")
    }
    if (builder.isFromParent(field)) {
        print("override ")
    } else if (builder is LeafBuilder && builder.isOpen) {
        print("open ")
    }
    if (builder is LeafBuilder && field is FieldWithDefault && field.needBackingField(fieldIsUseless) && !fieldIsUseless && !field.needNotNullDelegate(fieldIsUseless)) {
        print("lateinit ")
    }
}

private fun SmartPrinter.printDslBuildFunction(
    builder: LeafBuilder,
    hasRequiredFields: Boolean
) {
    konst isEmpty = builder.allFields.isEmpty()
    if (!isEmpty) {
        println("@OptIn(ExperimentalContracts::class)")
        print("inline ")
    } else if(builder.implementation.isPublic) {
        println("@OptIn(FirImplementationDetail::class)")
    }
    print("fun ")
    builder.implementation.element.typeArguments.takeIf { it.isNotEmpty() }?.let {
        print(it.joinToString(separator = ", ", prefix = "<", postfix = "> ") { it.name })
    }
    konst builderType = builder.typeWithArguments
    konst name = builder.implementation.name?.replaceFirst("Fir", "") ?: builder.implementation.element.name
    print("build${name}(")
    if (!isEmpty) {
        print("init: $builderType.() -> Unit")
        if (!hasRequiredFields) {
            print(" = {}")
        }
    }
    println("): ${builder.implementation.element.typeWithArguments} {")
    withIndent {
        if (!isEmpty) {
            println("contract {")
            withIndent {
                println("callsInPlace(init, kotlin.contracts.InvocationKind.EXACTLY_ONCE)")
            }
            println("}")
        }
        print("return ")
        if (isEmpty) {
            println("${builder.implementation.type}()")
        } else {
            println("$builderType().apply(init).build()")
        }
    }
    println("}")
}

private fun SmartPrinter.printDslBuildCopyFunction(
    builder: LeafBuilder,
    hasRequiredFields: Boolean
) {
    println("@OptIn(ExperimentalContracts::class)")
    print("inline ")
    print("fun ")
    builder.implementation.element.typeArguments.takeIf { it.isNotEmpty() }?.let {
        print(it.joinToString(separator = ", ", prefix = "<", postfix = "> ") { it.name })
    }
    konst builderType = builder.typeWithArguments
    konst name = builder.implementation.name?.replaceFirst("Fir", "") ?: builder.implementation.element.name
    print("build${name}Copy(")
    print("original: ${builder.implementation.element.typeWithArguments}, init: $builderType.() -> Unit")
    if (!hasRequiredFields) {
        print(" = {}")
    }
    println("): ${builder.implementation.element.typeWithArguments} {")
    withIndent {
        println("contract {")
        withIndent {
            println("callsInPlace(init, kotlin.contracts.InvocationKind.EXACTLY_ONCE)")
        }
        println("}")
        println("konst copyBuilder = $builderType()")
        for (field in builder.allFields) {
            when {
                field.invisibleField -> {}
                field.origin is FieldList -> println("copyBuilder.${field.name}.addAll(original.${field.name})")
                field.type == declarationAttributesType.type -> println("copyBuilder.${field.name} = original.${field.name}.copy()")
                field.notNull -> println("original.${field.name}?.let { copyBuilder.${field.name} = it }")
                else -> println("copyBuilder.${field.name} = original.${field.name}")
            }
        }
        println("return copyBuilder.apply(init).build()")
    }
    println("}")
}
