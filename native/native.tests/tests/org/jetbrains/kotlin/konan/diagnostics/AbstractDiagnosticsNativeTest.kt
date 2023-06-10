/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.diagnostics

import org.jetbrains.kotlin.platform.konan.NativePlatforms
import org.jetbrains.kotlin.test.Constructor
import org.jetbrains.kotlin.test.FirParser
import org.jetbrains.kotlin.test.builders.TestConfigurationBuilder
import org.jetbrains.kotlin.test.builders.classicFrontendHandlersStep
import org.jetbrains.kotlin.test.builders.firHandlersStep
import org.jetbrains.kotlin.test.directives.*
import org.jetbrains.kotlin.test.frontend.classic.ClassicFrontendFacade
import org.jetbrains.kotlin.test.frontend.classic.ClassicFrontendOutputArtifact
import org.jetbrains.kotlin.test.frontend.classic.handlers.ClassicDiagnosticsHandler
import org.jetbrains.kotlin.test.frontend.classic.handlers.DeclarationsDumpHandler
import org.jetbrains.kotlin.test.frontend.classic.handlers.OldNewInferenceMetaInfoProcessor
import org.jetbrains.kotlin.test.frontend.fir.FirFrontendFacade
import org.jetbrains.kotlin.test.frontend.fir.FirOutputArtifact
import org.jetbrains.kotlin.test.frontend.fir.handlers.*
import org.jetbrains.kotlin.test.model.*
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerTest
import org.jetbrains.kotlin.test.runners.configurationForClassicAndFirTestsAlongside
import org.jetbrains.kotlin.test.runners.enableLazyResolvePhaseChecking
import org.jetbrains.kotlin.test.services.configuration.CommonEnvironmentConfigurator
import org.jetbrains.kotlin.test.services.configuration.NativeEnvironmentConfigurator
import org.jetbrains.kotlin.test.services.sourceProviders.AdditionalDiagnosticsSourceFilesProvider
import org.jetbrains.kotlin.test.services.sourceProviders.CoroutineHelpersSourceFilesProvider

abstract class AbstractDiagnosticsNativeTestBase<R : ResultingArtifact.FrontendOutput<R>> : AbstractKotlinCompilerTest() {
    abstract konst targetFrontend: FrontendKind<R>
    abstract konst frontend: Constructor<FrontendFacade<R>>
    abstract fun handlersSetup(builder: TestConfigurationBuilder)

    override fun TestConfigurationBuilder.configuration() {
        globalDefaults {
            frontend = targetFrontend
            targetPlatform = NativePlatforms.unspecifiedNativePlatform
            dependencyKind = DependencyKind.Source
        }

        defaultDirectives {
            +JvmEnvironmentConfigurationDirectives.USE_PSI_CLASS_FILES_READING
        }

        enableMetaInfoHandler()

        useConfigurators(
            ::CommonEnvironmentConfigurator,
            ::NativeEnvironmentConfigurator,
        )

        useMetaInfoProcessors(::OldNewInferenceMetaInfoProcessor)
        useAdditionalSourceProviders(
            ::AdditionalDiagnosticsSourceFilesProvider,
            ::CoroutineHelpersSourceFilesProvider,
        )

        facadeStep(frontend)

        handlersSetup(this)

        forTestsMatching("testData/diagnostics/nativeTests/*") {
            defaultDirectives {
                +LanguageSettingsDirectives.ALLOW_KOTLIN_PACKAGE
                +ConfigurationDirectives.WITH_STDLIB
            }
        }
        forTestsMatching("testData/diagnostics/nativeTests/testsWithStdLib/*") {
            defaultDirectives {
                +ConfigurationDirectives.WITH_STDLIB
            }
        }
    }
}

abstract class AbstractDiagnosticsNativeTest : AbstractDiagnosticsNativeTestBase<ClassicFrontendOutputArtifact>() {
    override konst targetFrontend: FrontendKind<ClassicFrontendOutputArtifact>
        get() = FrontendKinds.ClassicFrontend

    override konst frontend: Constructor<FrontendFacade<ClassicFrontendOutputArtifact>>
        get() = ::ClassicFrontendFacade

    override fun handlersSetup(builder: TestConfigurationBuilder) {
        builder.classicFrontendHandlersStep {
            useHandlers(
                ::DeclarationsDumpHandler,
                ::ClassicDiagnosticsHandler,
            )
        }
    }
}

abstract class AbstractFirNativeDiagnosticsTestBase(konst parser: FirParser) : AbstractDiagnosticsNativeTestBase<FirOutputArtifact>() {
    override konst targetFrontend: FrontendKind<FirOutputArtifact>
        get() = FrontendKinds.FIR

    override konst frontend: Constructor<FrontendFacade<FirOutputArtifact>>
        get() = ::FirFrontendFacade

    override fun handlersSetup(builder: TestConfigurationBuilder) {
        builder.firHandlersStep {
            useHandlers(
                ::FirDiagnosticsHandler,
                ::FirDumpHandler,
                ::FirCfgDumpHandler,
                ::FirCfgConsistencyHandler,
                ::FirResolvedTypesVerifier,
                ::FirScopeDumpHandler,
            )
        }
    }

    override fun configure(builder: TestConfigurationBuilder) {
        super.configure(builder)
        with(builder) {
            configureFirParser(parser)
            enableLazyResolvePhaseChecking()

            forTestsMatching("compiler/testData/diagnostics/*") {
                configurationForClassicAndFirTestsAlongside()
            }
        }
    }
}

abstract class AbstractFirPsiNativeDiagnosticsTest : AbstractFirNativeDiagnosticsTestBase(FirParser.Psi)
abstract class AbstractFirLightTreeNativeDiagnosticsTest : AbstractFirNativeDiagnosticsTestBase(FirParser.LightTree)
