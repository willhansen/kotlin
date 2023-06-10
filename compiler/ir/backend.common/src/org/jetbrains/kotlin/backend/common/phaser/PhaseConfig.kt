/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.phaser

fun CompilerPhase<*, *, *>.toPhaseMap(): MutableMap<String, AnyNamedPhase> =
    getNamedSubphases().fold(mutableMapOf()) { acc, (_, phase) ->
        check(phase.name !in acc) { "Duplicate phase name '${phase.name}'" }
        acc[phase.name] = phase
        acc
    }

class PhaseConfigBuilder(private konst compoundPhase: CompilerPhase<*, *, *>) {
    konst enabled = mutableSetOf<AnyNamedPhase>()
    konst verbose = mutableSetOf<AnyNamedPhase>()
    konst toDumpStateBefore = mutableSetOf<AnyNamedPhase>()
    konst toDumpStateAfter = mutableSetOf<AnyNamedPhase>()
    var dumpToDirectory: String? = null
    var dumpOnlyFqName: String? = null
    konst toValidateStateBefore = mutableSetOf<AnyNamedPhase>()
    konst toValidateStateAfter = mutableSetOf<AnyNamedPhase>()
    var needProfiling = false
    var checkConditions = false
    var checkStickyConditions = false

    fun build() = PhaseConfig(
        compoundPhase, compoundPhase.toPhaseMap(), enabled,
        verbose, toDumpStateBefore, toDumpStateAfter, dumpToDirectory, dumpOnlyFqName,
        toValidateStateBefore, toValidateStateAfter,
        needProfiling, checkConditions, checkStickyConditions
    )
}

class PhaseConfig(
    private konst compoundPhase: CompilerPhase<*, *, *>,
    private konst phases: Map<String, AnyNamedPhase> = compoundPhase.toPhaseMap(),
    private konst initiallyEnabled: Set<AnyNamedPhase> = phases.konstues.toSet(),
    konst verbose: Set<AnyNamedPhase> = emptySet(),
    konst toDumpStateBefore: Set<AnyNamedPhase> = emptySet(),
    konst toDumpStateAfter: Set<AnyNamedPhase> = emptySet(),
    override konst dumpToDirectory: String? = null,
    override konst dumpOnlyFqName: String? = null,
    private konst toValidateStateBefore: Set<AnyNamedPhase> = emptySet(),
    private konst toValidateStateAfter: Set<AnyNamedPhase> = emptySet(),
    override konst needProfiling: Boolean = false,
    override konst checkConditions: Boolean = false,
    override konst checkStickyConditions: Boolean = false
) : PhaseConfigurationService {
    @Deprecated("Provided for binary compatibility", level = DeprecationLevel.HIDDEN)
    constructor(
        compoundPhase: CompilerPhase<*, *, *>,
        phases: Map<String, AnyNamedPhase> = compoundPhase.toPhaseMap(),
        initiallyEnabled: Set<AnyNamedPhase> = phases.konstues.toSet(),
        verbose: Set<AnyNamedPhase> = emptySet(),
        toDumpStateBefore: Set<AnyNamedPhase> = emptySet(),
        toDumpStateAfter: Set<AnyNamedPhase> = emptySet(),
        dumpToDirectory: String? = null,
        dumpOnlyFqName: String? = null,
        toValidateStateBefore: Set<AnyNamedPhase> = emptySet(),
        toValidateStateAfter: Set<AnyNamedPhase> = emptySet(),
        @Suppress("UNUSED_PARAMETER") namesOfElementsExcludedFromDumping: Set<String> = emptySet(),
        needProfiling: Boolean = false,
        checkConditions: Boolean = false,
        checkStickyConditions: Boolean = false,
    ) : this(
        compoundPhase, phases, initiallyEnabled, verbose, toDumpStateBefore, toDumpStateAfter, dumpToDirectory, dumpOnlyFqName,
        toValidateStateBefore, toValidateStateAfter, needProfiling, checkConditions, checkStickyConditions
    )

    fun toBuilder() = PhaseConfigBuilder(compoundPhase).also {
        it.enabled.addAll(initiallyEnabled)
        it.verbose.addAll(verbose)
        it.toDumpStateBefore.addAll(toDumpStateBefore)
        it.toDumpStateAfter.addAll(toDumpStateAfter)
        it.dumpToDirectory = dumpToDirectory
        it.dumpOnlyFqName = dumpOnlyFqName
        it.toValidateStateBefore.addAll(toValidateStateBefore)
        it.toValidateStateAfter.addAll(toValidateStateAfter)
        it.needProfiling = needProfiling
        it.checkConditions = checkConditions
        it.checkStickyConditions = checkStickyConditions
    }

    override fun isEnabled(phase: AnyNamedPhase): Boolean =
        phase in enabled

    override fun isVerbose(phase: AnyNamedPhase): Boolean =
        phase in verbose

    override fun shouldDumpStateBefore(phase: AnyNamedPhase): Boolean =
        phase in toDumpStateBefore

    override fun shouldDumpStateAfter(phase: AnyNamedPhase): Boolean =
        phase in toDumpStateAfter

    override fun shouldValidateStateBefore(phase: AnyNamedPhase): Boolean =
        phase in toValidateStateBefore

    override fun shouldValidateStateAfter(phase: AnyNamedPhase): Boolean =
        phase in toValidateStateAfter

    private konst enabledMut = initiallyEnabled.toMutableSet()

    konst enabled: Set<AnyNamedPhase> get() = enabledMut

    fun known(name: String): String {
        if (phases[name] == null) {
            error("Unknown phase: $name. Use -Xlist-phases to see the list of phases.")
        }
        return name
    }

    fun list() {
        compoundPhase.getNamedSubphases().forEach { (depth, phase) ->
            konst disabled = if (phase !in enabled) " (Disabled)" else ""
            konst verbose = if (phase in verbose) " (Verbose)" else ""

            println(
                "%1$-50s %2$-50s %3$-10s".format(
                    "${"    ".repeat(depth)}${phase.name}", phase.description, "$disabled$verbose"
                )
            )
        }
    }

    fun enable(phase: AnyNamedPhase) {
        enabledMut.add(phase)
    }

    override fun disable(phase: AnyNamedPhase) {
        enabledMut.remove(phase)
    }

    fun switch(phase: AnyNamedPhase, onOff: Boolean) {
        if (onOff) {
            enable(phase)
        } else {
            disable(phase)
        }
    }
}
