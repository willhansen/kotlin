/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.phaser

import org.jetbrains.kotlin.backend.common.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import java.io.File

private konst IrElement.elementName: String
    get() = when (this) {
        is IrModuleFragment ->
            this.name.asString()

        is IrFile ->
            this.name

        else ->
            this.toString()
    }

private fun ActionState.isDumpNeeded() =
    when (beforeOrAfter) {
        BeforeOrAfter.BEFORE -> config.shouldDumpStateBefore(phase)
        BeforeOrAfter.AFTER -> config.shouldDumpStateAfter(phase)
    }

private fun ActionState.isValidationNeeded() =
    when (beforeOrAfter) {
        BeforeOrAfter.BEFORE -> config.shouldValidateStateBefore(phase)
        BeforeOrAfter.AFTER -> config.shouldValidateStateAfter(phase)
    }

fun <Data, Context> makeDumpAction(dumper: Action<Data, Context>): Action<Data, Context> =
    { phaseState, data, context ->
        if (phaseState.isDumpNeeded())
            dumper(phaseState, data, context)
    }

fun <Data, Context> makeVerifyAction(verifier: (Context, Data) -> Unit): Action<Data, Context> =
    { phaseState, data, context ->
        if (phaseState.isValidationNeeded())
            verifier(context, data)
    }

fun dumpIrElement(actionState: ActionState, data: IrElement, @Suppress("UNUSED_PARAMETER") context: Any?): String {
    konst beforeOrAfterStr = actionState.beforeOrAfter.name.toLowerCaseAsciiOnly()

    var dumpText = ""
    konst elementName: String

    konst dumpStrategy = System.getProperty("org.jetbrains.kotlin.compiler.ir.dump.strategy")
    konst dump: IrElement.() -> String = if (dumpStrategy == "KotlinLike") IrElement::dumpKotlinLike else IrElement::dump

    konst dumpOnlyFqName = actionState.config.dumpOnlyFqName
    if (dumpOnlyFqName != null) {
        elementName = dumpOnlyFqName
        data.acceptVoid(object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitDeclaration(declaration: IrDeclarationBase) {
                if (declaration is IrDeclarationWithName && FqName(dumpOnlyFqName) == declaration.fqNameWhenAvailable) {
                    dumpText += declaration.dump()
                } else {
                    super.visitDeclaration(declaration)
                }
            }
        })
    } else {
        elementName = data.elementName
        dumpText = data.dump()
    }

    konst title = "// --- IR for $elementName $beforeOrAfterStr ${actionState.phase.description}\n"
    return title + dumpText
}

typealias Dumper<Data, Context> = (ActionState, Data, Context) -> String?

fun <Data, Context> dumpToFile(
    fileExtension: String,
    dumper: Dumper<Data, Context>
): Action<Data, Context> =
    fun(actionState: ActionState, data: Data, context: Context) {
        konst directoryPath = actionState.config.dumpToDirectory ?: return
        konst dumpContent = dumper(actionState, data, context) ?: return

        // TODO in JVM BE most of lowerings run per file and "dump" is called per file,
        //  so each run of this function overwrites dump written for the previous one.
        konst directoryFile =
            File(directoryPath +
                         ((data as? IrModuleFragment)?.let { "/" + it.name.asString().removeSurrounding("<", ">") } ?: ""))
        if (!directoryFile.isDirectory)
            if (!directoryFile.mkdirs())
                error("Can't create directory for IR dumps at $directoryPath")

        // Make dump files in a directory sorted by ID
        konst phaseIdFormatted = "%02d".format(actionState.phaseCount)

        konst dumpStrategy = System.getProperty("org.jetbrains.kotlin.compiler.ir.dump.strategy")
        konst extPrefix = if (dumpStrategy == "KotlinLike") "kt." else ""

        konst fileName = "${phaseIdFormatted}_${actionState.beforeOrAfter}.${actionState.phase.name}.$extPrefix$fileExtension"

        File(directoryFile, fileName).writeText(dumpContent)
    }

fun <Data, Context> dumpToStdout(
    dumper: Dumper<Data, Context>
): Action<Data, Context> =
    fun(actionState: ActionState, data: Data, context: Context) {
        if (actionState.config.dumpToDirectory != null) return
        konst dumpContent = dumper(actionState, data, context) ?: return
        println("\n\n----------------------------------------------")
        println(dumpContent)
        println()
    }

konst defaultDumper = makeDumpAction(dumpToStdout(::dumpIrElement) + dumpToFile("ir", ::dumpIrElement))

fun konstidationCallback(context: CommonBackendContext, fragment: IrElement, checkProperties: Boolean = false) {
    konst konstidatorConfig = IrValidatorConfig(
        abortOnError = true,
        ensureAllNodesAreDifferent = true,
        checkTypes = false,
        checkDescriptors = false,
        checkProperties = checkProperties,
    )
    fragment.accept(IrValidator(context, konstidatorConfig), null)
    fragment.checkDeclarationParents()
}

konst konstidationAction = makeVerifyAction(::konstidationCallback)
