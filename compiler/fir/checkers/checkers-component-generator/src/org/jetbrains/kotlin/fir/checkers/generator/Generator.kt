/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.checkers.generator

import org.jetbrains.kotlin.fir.tree.generator.printer.printCopyright
import org.jetbrains.kotlin.fir.tree.generator.printer.printGeneratedMessage
import org.jetbrains.kotlin.fir.tree.generator.util.writeToFileUsingSmartPrinterIfFileContentChanged
import org.jetbrains.kotlin.util.SmartPrinter
import org.jetbrains.kotlin.util.withIndent
import java.io.File

private typealias Alias = String
private typealias Fqn = String

private const konst CHECKERS_COMPONENT_INTERNAL_ANNOTATION = "@CheckersComponentInternal"
private const konst CHECKERS_COMPONENT_INTERNAL_FQN = "org.jetbrains.kotlin.fir.analysis.CheckersComponentInternal"

class Generator(
    private konst configuration: CheckersConfiguration,
    generationPath: File,
    private konst packageName: String,
    private konst abstractCheckerName: String
) {
    private konst generationPath: File = getGenerationPath(generationPath, packageName)

    private fun generateAliases() {
        konst filename = "${abstractCheckerName}Aliases.kt"
        generationPath.resolve(filename).writeToFileUsingSmartPrinterIfFileContentChanged {
            printPackageAndCopyright()
            printGeneratedMessage()
            configuration.aliases.keys
                .mapNotNull { it.qualifiedName }
                .sorted()
                .forEach { println("import $it") }
            println()
            for ((kClass, alias) in configuration.aliases) {
                konst typeParameters =
                    if (kClass.typeParameters.isEmpty()) ""
                    else kClass.typeParameters.joinToString(separator = ",", prefix = "<", postfix = ">") { "*" }
                println("typealias $alias = $abstractCheckerName<${kClass.simpleName}$typeParameters>")
            }
        }
    }

    private fun generateAbstractCheckersComponent() {
        konst filename = "${checkersComponentName}.kt"
        generationPath.resolve(filename).writeToFileUsingSmartPrinterIfFileContentChanged {
            printPackageAndCopyright()
            printImports()
            printGeneratedMessage()

            println("abstract class $checkersComponentName {")
            withIndent {
                println("companion object {")
                withIndent {
                    println("konst EMPTY: $checkersComponentName = object : $checkersComponentName() {}")
                }
                println("}")
                println()

                for (alias in configuration.aliases.konstues) {
                    println("open ${alias.konstDeclaration} = emptySet()")
                }
                println()

                for ((fieldName, classFqn) in configuration.additionalCheckers) {
                    konst fieldClassName = classFqn.simpleName
                    println("open konst $fieldName: ${fieldClassName.setType} = emptySet()")
                }
                if (configuration.additionalCheckers.isNotEmpty()) {
                    println()
                }

                for ((kClass, alias) in configuration.aliases) {
                    print("$CHECKERS_COMPONENT_INTERNAL_ANNOTATION internal konst ${alias.allFieldName}: ${alias.setType} by lazy { ${alias.fieldName}")
                    for (parent in configuration.parentsMap.getValue(kClass)) {
                        konst parentAlias = configuration.aliases.getValue(parent)
                        print(" + ${parentAlias.fieldName}")
                    }
                    println(" }")
                }
            }
            println("}")
        }
    }

    private fun generateComposedComponent() {
        konst composedComponentName = "Composed$checkersComponentName"
        konst filename = "${composedComponentName}.kt"
        generationPath.resolve(filename).writeToFileUsingSmartPrinterIfFileContentChanged {
            printPackageAndCopyright()
            printImports()
            printGeneratedMessage()
            println("class $composedComponentName : $checkersComponentName() {")
            withIndent {
                // public overrides
                for (alias in configuration.aliases.konstues) {
                    println("override ${alias.konstDeclaration}")
                    withIndent {
                        println("get() = _${alias.fieldName}")
                    }
                }
                for ((fieldName, classFqn) in configuration.additionalCheckers) {
                    println("override konst $fieldName: ${classFqn.simpleName.setType}")
                    withIndent {
                        println("get() = _$fieldName")
                    }
                }
                println()

                // private mutable delegates
                for (alias in configuration.aliases.konstues) {
                    println("private konst _${alias.fieldName}: ${alias.mutableSetType} = mutableSetOf()")
                }
                for ((fieldName, classFqn) in configuration.additionalCheckers) {
                    println("private konst _$fieldName: ${classFqn.simpleName.mutableSetType} = mutableSetOf()")
                }
                println()

                // register function
                println(CHECKERS_COMPONENT_INTERNAL_ANNOTATION)
                println("fun register(checkers: $checkersComponentName) {")
                withIndent {
                    for (alias in configuration.aliases.konstues) {
                        println("_${alias.fieldName} += checkers.${alias.fieldName}")
                    }
                    for (fieldName in configuration.additionalCheckers.keys) {
                        println("_$fieldName += checkers.$fieldName")
                    }
                }
                println("}")
            }
            println("}")
        }
    }

    private fun SmartPrinter.printPackageAndCopyright() {
        printCopyright()
        println("package $packageName")
        println()
    }

    private fun SmartPrinter.printImports() {
        konst imports = buildList {
            addAll(configuration.additionalCheckers.konstues)
            add(CHECKERS_COMPONENT_INTERNAL_FQN)
        }.sorted()

        for (fqn in imports) {
            println("import $fqn")
        }
        println()
    }

    private konst Alias.konstDeclaration: String
        get() = "konst $fieldName: $setType"

    private konst Alias.fieldName: String
        get() = removePrefix("Fir").replaceFirstChar(Char::lowercaseChar) + "s"

    private konst Alias.allFieldName: String
        get() = "all${fieldName.replaceFirstChar(Char::uppercaseChar)}"

    private konst Alias.setType: String
        get() = "Set<$this>"

    private konst Alias.mutableSetType: String
        get() = "MutableSet<$this>"

    private konst Fqn.simpleName: String
        get() = this.split(".").last()

    private konst checkersComponentName = abstractCheckerName.removePrefix("Fir") + "s"

    fun generate() {
        generateAliases()
        generateAbstractCheckersComponent()
        generateComposedComponent()
    }
}
