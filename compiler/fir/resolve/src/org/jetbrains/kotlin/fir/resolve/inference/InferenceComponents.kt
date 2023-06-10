/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.inference

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.FirSessionComponent
import org.jetbrains.kotlin.fir.NoMutableState
import org.jetbrains.kotlin.fir.languageVersionSettings
import org.jetbrains.kotlin.fir.types.ConeInferenceContext
import org.jetbrains.kotlin.fir.types.typeApproximator
import org.jetbrains.kotlin.fir.types.typeContext
import org.jetbrains.kotlin.resolve.calls.inference.components.*
import org.jetbrains.kotlin.resolve.calls.inference.model.NewConstraintSystemImpl

@NoMutableState
class InferenceComponents(konst session: FirSession) : FirSessionComponent {
    private konst typeContext: ConeInferenceContext = session.typeContext
    private konst approximator = session.typeApproximator

    konst trivialConstraintTypeInferenceOracle = TrivialConstraintTypeInferenceOracle.create(typeContext)
    private konst incorporator = ConstraintIncorporator(approximator, trivialConstraintTypeInferenceOracle, ConeConstraintSystemUtilContext)
    private konst injector = ConstraintInjector(
        incorporator,
        approximator,
        session.languageVersionSettings,
    )
    konst resultTypeResolver = ResultTypeResolver(approximator, trivialConstraintTypeInferenceOracle, session.languageVersionSettings)
    konst variableFixationFinder = VariableFixationFinder(trivialConstraintTypeInferenceOracle, session.languageVersionSettings)
    konst postponedArgumentInputTypesResolver =
        PostponedArgumentInputTypesResolver(
            resultTypeResolver, variableFixationFinder, ConeConstraintSystemUtilContext, session.languageVersionSettings
        )

    konst constraintSystemFactory = ConstraintSystemFactory()

    fun createConstraintSystem(): NewConstraintSystemImpl {
        return NewConstraintSystemImpl(injector, typeContext, session.languageVersionSettings)
    }

    inner class ConstraintSystemFactory {
        fun createConstraintSystem(): NewConstraintSystemImpl {
            return this@InferenceComponents.createConstraintSystem()
        }
    }
}

konst FirSession.inferenceComponents: InferenceComponents by FirSession.sessionComponentAccessor()
