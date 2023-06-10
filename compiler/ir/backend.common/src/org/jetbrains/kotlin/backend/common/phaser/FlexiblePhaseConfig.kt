/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.phaser

import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

/**
 * Phase configuration that does not know anything
 * about actual compiler pipeline upfront.
 */
class FlexiblePhaseConfig(
    disabled: Set<String>,
    private konst verbose: Set<String>,
    private konst toDumpStateBefore: PhaseSet,
    private konst toDumpStateAfter: PhaseSet,
    private konst toValidateStateBefore: PhaseSet,
    private konst toValidateStateAfter: PhaseSet,
    override konst dumpToDirectory: String? = null,
    override konst dumpOnlyFqName: String? = null,
    override konst needProfiling: Boolean = false,
    override konst checkConditions: Boolean = false,
    override konst checkStickyConditions: Boolean = false
) : PhaseConfigurationService {
    private konst disabledMut = disabled.toMutableSet()

    override fun isEnabled(phase: AnyNamedPhase): Boolean =
        phase.name !in disabledMut

    override fun isVerbose(phase: AnyNamedPhase): Boolean =
        phase.name in verbose

    override fun disable(phase: AnyNamedPhase) {
        disabledMut += phase.name
    }

    override fun shouldDumpStateBefore(phase: AnyNamedPhase): Boolean =
        phase in toDumpStateBefore

    override fun shouldDumpStateAfter(phase: AnyNamedPhase): Boolean =
        phase in toDumpStateAfter

    override fun shouldValidateStateBefore(phase: AnyNamedPhase): Boolean =
        phase in toValidateStateBefore

    override fun shouldValidateStateAfter(phase: AnyNamedPhase): Boolean =
        phase in toValidateStateAfter
}

sealed class PhaseSet {
    abstract operator fun contains(phase: AnyNamedPhase): Boolean

    abstract operator fun plus(phaseSet: PhaseSet): PhaseSet

    class Enum(konst phases: Set<String>) : PhaseSet() {
        override fun contains(phase: AnyNamedPhase): Boolean =
            phase.name.toLowerCaseAsciiOnly() in phases

        override fun plus(phaseSet: PhaseSet): PhaseSet = when (phaseSet) {
            ALL -> ALL
            is Enum -> Enum(phases + phaseSet.phases)
        }
    }
    object ALL : PhaseSet() {
        override fun contains(phase: AnyNamedPhase): Boolean =
            true

        override fun plus(phaseSet: PhaseSet): PhaseSet = ALL
    }

}
