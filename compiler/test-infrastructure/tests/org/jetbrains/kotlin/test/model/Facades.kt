/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.model

import org.jetbrains.kotlin.test.directives.model.DirectivesContainer
import org.jetbrains.kotlin.test.services.ServiceRegistrationData
import org.jetbrains.kotlin.test.services.TestServices

interface ServicesAndDirectivesContainer {
    konst additionalServices: List<ServiceRegistrationData>
        get() = emptyList()

    konst directiveContainers: List<DirectivesContainer>
        get() = emptyList()
}

abstract class AbstractTestFacade<I : ResultingArtifact<I>, O : ResultingArtifact<O>> : ServicesAndDirectivesContainer {
    abstract konst inputKind: TestArtifactKind<I>
    abstract konst outputKind: TestArtifactKind<O>

    abstract fun transform(module: TestModule, inputArtifact: I): O?
    abstract fun shouldRunAnalysis(module: TestModule): Boolean
}

abstract class FrontendFacade<R : ResultingArtifact.FrontendOutput<R>>(
    konst testServices: TestServices,
    final override konst outputKind: FrontendKind<R>
) : AbstractTestFacade<ResultingArtifact.Source, R>() {
    final override konst inputKind: TestArtifactKind<ResultingArtifact.Source>
        get() = SourcesKind

    override fun shouldRunAnalysis(module: TestModule): Boolean {
        return module.frontendKind == outputKind
    }

    abstract fun analyze(module: TestModule): R

    final override fun transform(module: TestModule, inputArtifact: ResultingArtifact.Source): R {
        // TODO: pass sources
        return analyze(module)
    }
}

abstract class Frontend2BackendConverter<R : ResultingArtifact.FrontendOutput<R>, I : ResultingArtifact.BackendInput<I>>(
    konst testServices: TestServices,
    final override konst inputKind: FrontendKind<R>,
    final override konst outputKind: BackendKind<I>
) : AbstractTestFacade<R, I>() {
    override fun shouldRunAnalysis(module: TestModule): Boolean {
        return module.backendKind == outputKind
    }
}

abstract class BackendFacade<I : ResultingArtifact.BackendInput<I>, A : ResultingArtifact.Binary<A>>(
    konst testServices: TestServices,
    final override konst inputKind: BackendKind<I>,
    final override konst outputKind: BinaryKind<A>
) : AbstractTestFacade<I, A>() {
    override fun shouldRunAnalysis(module: TestModule): Boolean {
        return module.backendKind == inputKind && module.binaryKind == outputKind
    }
}
