/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.runners.codegen

import org.jetbrains.kotlin.test.Constructor
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.backend.BlackBoxCodegenSuppressor
import org.jetbrains.kotlin.test.backend.classic.ClassicBackendInput
import org.jetbrains.kotlin.test.backend.classic.ClassicJvmBackendFacade
import org.jetbrains.kotlin.test.backend.handlers.BytecodeTextHandler
import org.jetbrains.kotlin.test.backend.ir.IrBackendInput
import org.jetbrains.kotlin.test.backend.ir.JvmIrBackendFacade
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.builders.configureJvmArtifactsHandlersStep
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.USE_PSI_CLASS_FILES_READING
import org.jetbrains.kotlin.test.directives.JvmEnvironmentConfigurationDirectives.WITH_REFLECT
import org.jetbrains.kotlin.test.directives.ConfigurationDirectives.WITH_STDLIB
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.directives.configureFirParser
import org.jetbrains.kotlin.test.frontend.classic.ClassicFrontend2ClassicBackendConverter
import org.jetbrains.kotlin.test.frontend.classic.ClassicFrontend2IrConverter
import org.jetbrains.kotlin.test.frontend.classic.ClassicFrontendFacade
import org.jetbrains.kotlin.test.frontend.classic.ClassicFrontendOutputArtifact
import org.jetbrains.kotlin.test.frontend.fir.Fir2IrJvmResultsConverter
import org.jetbrains.kotlin.test.frontend.fir.FirFrontendFacade
import org.jetbrains.kotlin.test.frontend.fir.FirOutputArtifact
import org.jetbrains.kotlin.test.model.*
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerWithTargetBackendTest

abstract class AbstractBytecodeTextTestBase<R : ResultingArtifact.FrontendOutput<R>, I : ResultingArtifact.BackendInput<I>>(
    targetBackend: TargetBackend,
    konst targetFrontend: FrontendKind<R>
) : AbstractKotlinCompilerWithTargetBackendTest(targetBackend) {
    abstract konst frontendFacade: Constructor<FrontendFacade<R>>
    abstract konst frontendToBackendConverter: Constructor<Frontend2BackendConverter<R, I>>
    abstract konst backendFacade: Constructor<BackendFacade<I, BinaryArtifacts.Jvm>>

    override fun TestConfigurationBuilder.configuration() {
        defaultDirectives {
            +WITH_STDLIB
            +WITH_REFLECT
        }

        commonConfigurationForTest(targetFrontend, frontendFacade, frontendToBackendConverter, backendFacade)

        commonHandlersForCodegenTest()

        configureJvmArtifactsHandlersStep {
            useHandlers(::BytecodeTextHandler)
        }

        useAfterAnalysisCheckers(::BlackBoxCodegenSuppressor)
    }
}

open class AbstractBytecodeTextTest : AbstractBytecodeTextTestBase<ClassicFrontendOutputArtifact, ClassicBackendInput>(
    targetBackend = TargetBackend.JVM,
    targetFrontend = FrontendKinds.ClassicFrontend
) {
    override konst frontendFacade: Constructor<FrontendFacade<ClassicFrontendOutputArtifact>>
        get() = ::ClassicFrontendFacade

    override konst frontendToBackendConverter: Constructor<Frontend2BackendConverter<ClassicFrontendOutputArtifact, ClassicBackendInput>>
        get() = ::ClassicFrontend2ClassicBackendConverter

    override konst backendFacade: Constructor<BackendFacade<ClassicBackendInput, BinaryArtifacts.Jvm>>
        get() = ::ClassicJvmBackendFacade
}

open class AbstractIrBytecodeTextTest : AbstractBytecodeTextTestBase<ClassicFrontendOutputArtifact, IrBackendInput>(
    targetBackend = TargetBackend.JVM_IR,
    targetFrontend = FrontendKinds.ClassicFrontend
) {
    override konst frontendFacade: Constructor<FrontendFacade<ClassicFrontendOutputArtifact>>
        get() = ::ClassicFrontendFacade

    override konst frontendToBackendConverter: Constructor<Frontend2BackendConverter<ClassicFrontendOutputArtifact, IrBackendInput>>
        get() = ::ClassicFrontend2IrConverter

    override konst backendFacade: Constructor<BackendFacade<IrBackendInput, BinaryArtifacts.Jvm>>
        get() = ::JvmIrBackendFacade
}

open class AbstractFirBytecodeTextTestBase(konst parser: FirParser) : AbstractBytecodeTextTestBase<FirOutputArtifact, IrBackendInput>(
    targetBackend = TargetBackend.JVM_IR,
    targetFrontend = FrontendKinds.FIR
) {
    override konst frontendFacade: Constructor<FrontendFacade<FirOutputArtifact>>
        get() = ::FirFrontendFacade

    override konst frontendToBackendConverter: Constructor<Frontend2BackendConverter<FirOutputArtifact, IrBackendInput>>
        get() = ::Fir2IrJvmResultsConverter

    override konst backendFacade: Constructor<BackendFacade<IrBackendInput, BinaryArtifacts.Jvm>>
        get() = ::JvmIrBackendFacade

    override fun configure(builder: TestConfigurationBuilder) {
        super.configure(builder)
        with(builder) {
            defaultDirectives {
                // See KT-44152
                -USE_PSI_CLASS_FILES_READING
                builder.configureFirParser(parser)
            }
        }
    }
}

open class AbstractFirLightTreeBytecodeTextTest : AbstractFirBytecodeTextTestBase(FirParser.LightTree)

@FirPsiCodegenTest
open class AbstractFirPsiBytecodeTextTest : AbstractFirBytecodeTextTestBase(FirParser.Psi)
