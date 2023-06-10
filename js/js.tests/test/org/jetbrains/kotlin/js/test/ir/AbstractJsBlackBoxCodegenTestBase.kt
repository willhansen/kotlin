/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.js.test.ir

import org.jetbrains.kotlin.js.test.JsAdditionalSourceProvider
import org.jetbrains.kotlin.js.test.JsFailingTestSuppressor
import org.jetbrains.kotlin.js.test.handlers.*
import org.jetbrains.kotlin.platform.js.JsPlatforms
import org.jetbrains.kotlin.test.Constructor
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.backend.BlackBoxCodegenSuppressor
import org.jetbrains.kotlin.test.backend.handlers.IrMangledNameAndSignatureDumpHandler
import org.jetbrains.kotlin.test.backend.handlers.JsIrInterpreterDumpHandler
import org.jetbrains.kotlin.test.backend.handlers.KlibInterpreterDumpHandler
import org.jetbrains.kotlin.test.builders.*
import org.jetbrains.kotlin.test.directives.DiagnosticsDirectives
import org.jetbrains.kotlin.test.directives.DiagnosticsDirectives.DIAGNOSTICS
import org.jetbrains.kotlin.test.directives.JsEnvironmentConfigurationDirectives
import org.jetbrains.kotlin.test.frontend.classic.handlers.ClassicDiagnosticsHandler
import org.jetbrains.kotlin.test.frontend.fir.handlers.FirDiagnosticsHandler
import org.jetbrains.kotlin.test.model.*
import org.jetbrains.kotlin.test.runners.AbstractKotlinCompilerWithTargetBackendTest
import org.jetbrains.kotlin.test.runners.codegen.commonClassicFrontendHandlersForCodegenTest
import org.jetbrains.kotlin.test.services.JsLibraryProvider
import org.jetbrains.kotlin.test.services.configuration.CommonEnvironmentConfigurator
import org.jetbrains.kotlin.test.services.configuration.JsEnvironmentConfigurator
import org.jetbrains.kotlin.test.services.sourceProviders.CoroutineHelpersSourceFilesProvider
import java.lang.Boolean.getBoolean

abstract class AbstractJsBlackBoxCodegenTestBase<R : ResultingArtifact.FrontendOutput<R>, I : ResultingArtifact.BackendInput<I>, A : ResultingArtifact.Binary<A>>(
    konst targetFrontend: FrontendKind<R>,
    targetBackend: TargetBackend,
    private konst pathToTestDir: String,
    private konst testGroupOutputDirPrefix: String,
    protected konst skipMinification: Boolean = getBoolean("kotlin.js.skipMinificationTest"),
) : AbstractKotlinCompilerWithTargetBackendTest(targetBackend) {
    abstract konst frontendFacade: Constructor<FrontendFacade<R>>
    abstract konst frontendToBackendConverter: Constructor<Frontend2BackendConverter<R, I>>
    abstract konst backendFacade: Constructor<BackendFacade<I, A>>
    abstract konst afterBackendFacade: Constructor<AbstractTestFacade<A, BinaryArtifacts.Js>>?
    abstract konst recompileFacade: Constructor<AbstractTestFacade<BinaryArtifacts.Js, BinaryArtifacts.Js>>

    override fun TestConfigurationBuilder.configuration() {
        commonConfigurationForJsBlackBoxCodegenTest()
        jsArtifactsHandlersStep {
            useHandlers(
                ::NodeJsGeneratorHandler,
                ::JsBoxRunner,
                ::JsMinifierRunner,
                ::JsAstHandler
            )
        }
    }

    protected fun TestConfigurationBuilder.commonConfigurationForJsBlackBoxCodegenTest() {
        commonConfigurationForJsCodegenTest(targetFrontend, frontendFacade, frontendToBackendConverter, backendFacade)

        konst pathToRootOutputDir = System.getProperty("kotlin.js.test.root.out.dir") ?: error("'kotlin.js.test.root.out.dir' is not set")
        defaultDirectives {
            JsEnvironmentConfigurationDirectives.PATH_TO_ROOT_OUTPUT_DIR with pathToRootOutputDir
            JsEnvironmentConfigurationDirectives.PATH_TO_TEST_DIR with pathToTestDir
            JsEnvironmentConfigurationDirectives.TEST_GROUP_OUTPUT_DIR_PREFIX with testGroupOutputDirPrefix
            +JsEnvironmentConfigurationDirectives.TYPED_ARRAYS
            +JsEnvironmentConfigurationDirectives.GENERATE_NODE_JS_RUNNER
            if (skipMinification) +JsEnvironmentConfigurationDirectives.SKIP_MINIFICATION
            if (getBoolean("kotlin.js.ir.skipRegularMode")) +JsEnvironmentConfigurationDirectives.SKIP_REGULAR_MODE
        }

        useAdditionalSourceProviders(
            ::JsAdditionalSourceProvider,
            ::CoroutineHelpersSourceFilesProvider,
        )

        forTestsNotMatching("compiler/testData/codegen/box/diagnostics/functions/tailRecursion/*") {
            defaultDirectives {
                DIAGNOSTICS with "-warnings"
            }
        }

        forTestsNotMatching("compiler/testData/codegen/boxError/*") {
            enableMetaInfoHandler()
        }

        configureIrHandlersStep {
            useHandlers(::IrMangledNameAndSignatureDumpHandler)
        }

        afterBackendFacade?.let { facadeStep(it) }
        facadeStep(recompileFacade)
        jsArtifactsHandlersStep {
            useHandlers(::JsSourceMapPathRewriter)
        }

        useAfterAnalysisCheckers(
            ::JsArtifactsDumpHandler
        )

        forTestsMatching("compiler/testData/codegen/box/involvesIrInterpreter/*") {
            enableMetaInfoHandler()
            configureKlibArtifactsHandlersStep {
                useHandlers(::KlibInterpreterDumpHandler)
            }
            configureJsArtifactsHandlersStep {
                useHandlers(::JsIrInterpreterDumpHandler)
            }
        }
    }
}

@Suppress("reformat")
fun <
    R : ResultingArtifact.FrontendOutput<R>,
    I : ResultingArtifact.BackendInput<I>,
    A : ResultingArtifact.Binary<A>
> TestConfigurationBuilder.commonConfigurationForJsCodegenTest(
    targetFrontend: FrontendKind<R>,
    frontendFacade: Constructor<FrontendFacade<R>>,
    frontendToBackendConverter: Constructor<Frontend2BackendConverter<R, I>>,
    backendFacade: Constructor<BackendFacade<I, A>>,
) {
    globalDefaults {
        frontend = targetFrontend
        targetPlatform = JsPlatforms.defaultJsPlatform
        dependencyKind = DependencyKind.Binary
    }

    defaultDirectives {
        +DiagnosticsDirectives.REPORT_ONLY_EXPLICITLY_DEFINED_DEBUG_INFO
    }

    useConfigurators(
        ::CommonEnvironmentConfigurator,
        ::JsEnvironmentConfigurator,
    )

    useAdditionalService(::JsLibraryProvider)

    useAfterAnalysisCheckers(
        ::JsFailingTestSuppressor,
        ::BlackBoxCodegenSuppressor,
    )

    facadeStep(frontendFacade)
    classicFrontendHandlersStep {
        commonClassicFrontendHandlersForCodegenTest()
        useHandlers(::ClassicDiagnosticsHandler)
    }

    firHandlersStep {
        useHandlers(::FirDiagnosticsHandler)
    }

    facadeStep(frontendToBackendConverter)
    irHandlersStep()

    facadeStep(backendFacade)
    klibArtifactsHandlersStep()
}
