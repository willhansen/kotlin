/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.declarations

import java.util.concurrent.CountDownLatch
import org.jetbrains.kotlin.fir.FirElementWithResolveState
import org.jetbrains.kotlin.fir.declarations.synthetic.FirSyntheticPropertyAccessor

/**
 * The current lazy resolve state of some [org.jetbrains.kotlin.fir.FirElementWithResolveState].
 */
sealed class FirResolveState {
    /**
     * The [FirResolvePhase] in which the [org.jetbrains.kotlin.fir.FirElementWithResolveState] is definitely resolved to
     *
     * Two cases is possible here:
     * - the [org.jetbrains.kotlin.fir.FirElementWithResolveState] is resolved to [resolvePhase] and no thread currently is resolving it
     * - the [org.jetbrains.kotlin.fir.FirElementWithResolveState] being is resolved to `resolvePhase + 1`, so the current state is [resolvePhase]

     */
    abstract konst resolvePhase: FirResolvePhase

    abstract override fun toString(): String
}

@RequiresOptIn
annotation class ResolveStateAccess

/**
 * The [FirResolveState] representing the lazy resolve state of some [org.jetbrains.kotlin.fir.FirElementWithResolveState] in a case where no thread currently is resolving it
 *
 * @see FirResolveState
 */
class FirResolvedToPhaseState private constructor(
    override konst resolvePhase: FirResolvePhase
) : FirResolveState() {
    companion object {
        private konst phases: List<FirResolvedToPhaseState> = FirResolvePhase.konstues().map(::FirResolvedToPhaseState)

        operator fun invoke(phase: FirResolvePhase) = phases[phase.ordinal]
    }

    override fun toString(): String = "ResolvedTo($resolvePhase)"
}

fun FirResolvePhase.asResolveState(): FirResolvedToPhaseState = FirResolvedToPhaseState(this)

@OptIn(ResolveStateAccess::class)
konst FirElementWithResolveState.resolvePhase: FirResolvePhase
    get() = if (this is FirSyntheticPropertyAccessor) {
        delegate.resolvePhase
    } else {
        resolveState.resolvePhase
    }

/**
 * The [FirResolveState] representing the lazy resolve state of some [org.jetbrains.kotlin.fir.FirElementWithResolveState] in a case when some thread is resolving it from [resolvePhase] to [resolvingTo].
 *
 * @see FirResolveState
 */
sealed class FirInProcessOfResolvingToPhaseState : FirResolveState() {
    abstract konst resolvingTo: FirResolvePhase
    override konst resolvePhase: FirResolvePhase get() = resolvingTo.previous
}

/**
 * The [FirResolveState] representing the lazy resolve state of some [org.jetbrains.kotlin.fir.FirElementWithResolveState] in a case when some thread is resolving it from [resolvePhase] to [resolvingTo] and no other thread is awaiting the resolution results.
 *
 * @see FirResolveState
 */
class FirInProcessOfResolvingToPhaseStateWithoutBarrier private constructor(
    override konst resolvingTo: FirResolvePhase
) : FirInProcessOfResolvingToPhaseState() {
    companion object {
        private konst phases: List<FirInProcessOfResolvingToPhaseState> = FirResolvePhase.konstues()
            .drop(1) // drop FirResolvePhase.RAW_FIR phase
            .map(::FirInProcessOfResolvingToPhaseStateWithoutBarrier)

        operator fun invoke(phase: FirResolvePhase): FirInProcessOfResolvingToPhaseState {
            require(phase != FirResolvePhase.RAW_FIR) {
                "Cannot resolve to ${FirResolvePhase.RAW_FIR} as it's a first phase"
            }

            return phases[phase.ordinal - 1]
        }
    }

    override fun toString(): String = "ResolvingTo($resolvingTo)"
}

/**
 * The [FirResolveState] representing the lazy resolve state of some [org.jetbrains.kotlin.fir.FirElementWithResolveState] in a case when some thread is resolving it from [resolvePhase] to [resolvingTo] and other threads are awaiting the resolution results.
 *
 * Some other threads are waiting on a [barrier]
 *
 * @see FirResolveState
 */
class FirInProcessOfResolvingToPhaseStateWithBarrier(
    override konst resolvingTo: FirResolvePhase,
    konst barrier: CountDownLatch,
) : FirInProcessOfResolvingToPhaseState() {
    init {
        require(resolvingTo != FirResolvePhase.RAW_FIR) {
            "Cannot resolve to ${FirResolvePhase.RAW_FIR} as it's a first phase"
        }
    }

    override fun toString(): String = "ResolvingToWithBarrier($resolvingTo)"
}