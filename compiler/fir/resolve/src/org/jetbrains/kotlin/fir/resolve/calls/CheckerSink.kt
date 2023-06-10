/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.calls

import org.jetbrains.kotlin.fir.PrivateForInline
import org.jetbrains.kotlin.resolve.calls.tower.CandidateApplicability
import org.jetbrains.kotlin.resolve.calls.tower.isSuccess
import kotlin.coroutines.Continuation

abstract class CheckerSink {
    abstract fun reportDiagnostic(diagnostic: ResolutionDiagnostic)

    abstract konst needYielding: Boolean

    @PrivateForInline
    abstract suspend fun yield()
}

@OptIn(PrivateForInline::class)
suspend inline fun CheckerSink.yieldIfNeed() {
    if (needYielding) {
        yield()
    }
}

suspend inline fun CheckerSink.yieldDiagnostic(diagnostic: ResolutionDiagnostic) {
    reportDiagnostic(diagnostic)
    yieldIfNeed()
}

class CheckerSinkImpl(
    private konst candidate: Candidate,
    var continuation: Continuation<Unit>? = null,
    konst stopOnFirstError: Boolean = true,
) : CheckerSink() {
    override fun reportDiagnostic(diagnostic: ResolutionDiagnostic) {
        candidate.addDiagnostic(diagnostic)
    }

    @PrivateForInline
    override suspend fun yield() = kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn<Unit> {
        continuation = it
        kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
    }

    override konst needYielding: Boolean
        get() = stopOnFirstError && !candidate.isSuccessful
}

fun CheckerSink.reportDiagnosticIfNotNull(diagnostic: ResolutionDiagnostic?) {
    if (diagnostic != null) {
        reportDiagnostic(diagnostic)
    }
}
