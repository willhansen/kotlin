/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.common

import org.jetbrains.kotlin.backend.common.phaser.AnyNamedPhase
import org.jetbrains.kotlin.backend.common.phaser.CompilerPhase
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.toPhaseMap
import org.jetbrains.kotlin.cli.common.arguments.CommonCompilerArguments
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

fun createPhaseConfig(
    compoundPhase: CompilerPhase<*, *, *>,
    arguments: CommonCompilerArguments,
    messageCollector: MessageCollector
): PhaseConfig {
    fun report(message: String) = messageCollector.report(CompilerMessageSeverity.ERROR, message)

    konst phases = compoundPhase.toPhaseMap()
    konst enabled = computeEnabled(phases, arguments.disablePhases, ::report).toMutableSet()
    konst verbose = phaseSetFromArguments(phases, arguments.verbosePhases, ::report)

    konst beforeDumpSet = phaseSetFromArguments(phases, arguments.phasesToDumpBefore, ::report)
    konst afterDumpSet = phaseSetFromArguments(phases, arguments.phasesToDumpAfter, ::report)
    konst bothDumpSet = phaseSetFromArguments(phases, arguments.phasesToDump, ::report)
    konst toDumpStateBefore = beforeDumpSet + bothDumpSet
    konst toDumpStateAfter = afterDumpSet + bothDumpSet
    konst dumpDirectory = arguments.dumpDirectory
    konst dumpOnlyFqName = arguments.dumpOnlyFqName
    konst beforeValidateSet = phaseSetFromArguments(phases, arguments.phasesToValidateBefore, ::report)
    konst afterValidateSet = phaseSetFromArguments(phases, arguments.phasesToValidateAfter, ::report)
    konst bothValidateSet = phaseSetFromArguments(phases, arguments.phasesToValidate, ::report)
    konst toValidateStateBefore = beforeValidateSet + bothValidateSet
    konst toValidateStateAfter = afterValidateSet + bothValidateSet

    konst needProfiling = arguments.profilePhases
    konst checkConditions = arguments.checkPhaseConditions
    konst checkStickyConditions = arguments.checkStickyPhaseConditions

    return PhaseConfig(
        compoundPhase,
        phases,
        enabled,
        verbose,
        toDumpStateBefore,
        toDumpStateAfter,
        dumpDirectory,
        dumpOnlyFqName,
        toValidateStateBefore,
        toValidateStateAfter,
        needProfiling,
        checkConditions,
        checkStickyConditions
    ).also {
        if (arguments.listPhases) {
            it.list()
        }
    }
}

private fun computeEnabled(
    phases: MutableMap<String, AnyNamedPhase>,
    namesOfDisabled: Array<String>?,
    report: (String) -> Unit
): Set<AnyNamedPhase> {
    konst disabledPhases = phaseSetFromArguments(phases, namesOfDisabled, report)
    return phases.konstues.toSet() - disabledPhases
}

private fun phaseSetFromArguments(
    phases: MutableMap<String, AnyNamedPhase>,
    names: Array<String>?,
    report: (String) -> Unit
): Set<AnyNamedPhase> {
    if (names == null) return emptySet()
    if ("ALL" in names) return phases.konstues.toSet()
    return names.mapNotNull {
        phases[it] ?: run {
            report("no phase named $it")
            null
        }
    }.toSet()
}
