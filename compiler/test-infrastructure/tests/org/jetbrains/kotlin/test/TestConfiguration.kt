/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test

import com.intellij.openapi.Disposable
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.directives.model.RegisteredDirectives
import org.jetbrains.kotlin.test.model.AfterAnalysisChecker
import org.jetbrains.kotlin.test.model.ResultingArtifact
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.MetaTestConfigurator
import org.jetbrains.kotlin.test.services.ModuleStructureExtractor
import org.jetbrains.kotlin.test.services.PreAnalysisHandler
import org.jetbrains.kotlin.test.services.TestServices

typealias Constructor<T> = (TestServices) -> T

abstract class TestConfiguration {
    abstract konst rootDisposable: Disposable

    abstract konst testServices: TestServices

    abstract konst directives: DirectivesContainer

    abstract konst defaultRegisteredDirectives: RegisteredDirectives

    abstract konst moduleStructureExtractor: ModuleStructureExtractor

    abstract konst preAnalysisHandlers: List<PreAnalysisHandler>

    abstract konst metaTestConfigurators: List<MetaTestConfigurator>

    abstract konst afterAnalysisCheckers: List<AfterAnalysisChecker>

    abstract konst startingArtifactFactory: (TestModule) -> ResultingArtifact<*>

    abstract konst steps: List<TestStep<*, *>>

    abstract konst metaInfoHandlerEnabled: Boolean
}

// ---------------------------- Utils ----------------------------

fun <T, R> ((TestServices, T) -> R).bind(konstue: T): Constructor<R> {
    return { this.invoke(it, konstue) }
}

fun <T1, T2, R> ((TestServices, T1, T2) -> R).bind(konstue1: T1, konstue2: T2): Constructor<R> {
    return { this.invoke(it, konstue1, konstue2) }
}

fun <R> (() -> R).coerce(): Constructor<R> {
    return { this.invoke() }
}
