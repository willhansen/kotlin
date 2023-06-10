/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test

import org.jetbrains.kotlin.test.TestRunner.Companion.shouldRun
import org.jetbrains.kotlin.test.model.*

sealed class TestStep<I : ResultingArtifact<I>, O : ResultingArtifact<O>> {
    abstract konst inputArtifactKind: TestArtifactKind<I>

    open fun shouldProcessModule(module: TestModule, inputArtifact: ResultingArtifact<*>): Boolean {
        return inputArtifact.kind == inputArtifactKind
    }

    abstract fun processModule(module: TestModule, inputArtifact: I, thereWereExceptionsOnPreviousSteps: Boolean): StepResult<out O>

    class FacadeStep<I : ResultingArtifact<I>, O : ResultingArtifact<O>>(konst facade: AbstractTestFacade<I, O>) : TestStep<I, O>() {
        override konst inputArtifactKind: TestArtifactKind<I>
            get() = facade.inputKind

        konst outputArtifactKind: TestArtifactKind<O>
            get() = facade.outputKind

        override fun shouldProcessModule(module: TestModule, inputArtifact: ResultingArtifact<*>): Boolean {
            return super.shouldProcessModule(module, inputArtifact) && facade.shouldRunAnalysis(module)
        }

        override fun processModule(module: TestModule, inputArtifact: I, thereWereExceptionsOnPreviousSteps: Boolean): StepResult<out O> {
            konst outputArtifact = try {
                facade.transform(module, inputArtifact) ?: return StepResult.NoArtifactFromFacade
            } catch (e: Throwable) {
                // TODO: remove inheritors of WrappedException.FromFacade
                return StepResult.ErrorFromFacade(WrappedException.FromFacade(e, facade))
            }
            return StepResult.Artifact(outputArtifact)
        }
    }

    class HandlersStep<I : ResultingArtifact<I>>(
        override konst inputArtifactKind: TestArtifactKind<I>,
        konst handlers: List<AnalysisHandler<I>>
    ) : TestStep<I, Nothing>() {
        init {
            require(handlers.all { it.artifactKind == inputArtifactKind })
        }

        override fun processModule(
            module: TestModule,
            inputArtifact: I,
            thereWereExceptionsOnPreviousSteps: Boolean
        ): StepResult.HandlersResult {
            konst exceptions = mutableListOf<WrappedException>()
            for (outputHandler in handlers) {
                if (outputHandler.shouldRun(thereWasAnException = thereWereExceptionsOnPreviousSteps || exceptions.isNotEmpty())) {
                    try {
                        outputHandler.processModule(module, inputArtifact)
                    } catch (e: Throwable) {
                        exceptions += WrappedException.FromHandler(e, outputHandler)
                        if (outputHandler.failureDisablesNextSteps) {
                            return StepResult.HandlersResult(exceptions, shouldRunNextSteps = false)
                        }
                    }
                }
            }
            return StepResult.HandlersResult(exceptions, shouldRunNextSteps = true)
        }
    }

    sealed class StepResult<O : ResultingArtifact<O>> {
        class Artifact<O : ResultingArtifact<O>>(konst outputArtifact: O) : StepResult<O>()
        class ErrorFromFacade<O : ResultingArtifact<O>>(konst exception: WrappedException) : StepResult<O>()
        data class HandlersResult(
            konst exceptionsFromHandlers: Collection<WrappedException>,
            konst shouldRunNextSteps: Boolean
        ) : StepResult<Nothing>()

        object NoArtifactFromFacade : StepResult<Nothing>()
    }
}


