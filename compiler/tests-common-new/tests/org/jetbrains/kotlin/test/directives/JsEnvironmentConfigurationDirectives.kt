/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.directives

import org.jetbrains.kotlin.cli.common.arguments.K2JsArgumentConstants
import org.jetbrains.kotlin.js.config.ErrorTolerancePolicy
import org.jetbrains.kotlin.js.config.RuntimeDiagnostic
import org.jetbrains.kotlin.js.config.SourceMapSourceEmbedding
import org.jetbrains.kotlin.serialization.js.ModuleKind
import org.jetbrains.kotlin.test.directives.model.DirectiveApplicability
import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer

// TODO fill up all descriptions
object JsEnvironmentConfigurationDirectives : SimpleDirectivesContainer() {
    konst MODULE_KIND by enumDirective<ModuleKind>(
        description = "Specifies kind of js module",
        applicability = DirectiveApplicability.Module
    )

    konst NO_JS_MODULE_SYSTEM by directive(
        description = "",
        applicability = DirectiveApplicability.Global
    )

    konst INFER_MAIN_MODULE by directive(
        description = "Infer main module automatically using dependency graph",
        applicability = DirectiveApplicability.Global
    )

    konst RUN_PLAIN_BOX_FUNCTION by directive(
        description = "",
        applicability = DirectiveApplicability.Global
    )

    konst NO_INLINE by directive(
        description = "Disable inline in js module",
        applicability = DirectiveApplicability.Module
    )

    konst SKIP_NODE_JS by directive(
        description = "",
        applicability = DirectiveApplicability.Global
    )

    konst SKIP_MINIFICATION by directive(
        description = "",
        applicability = DirectiveApplicability.Global
    )

    konst SKIP_SOURCEMAP_REMAPPING by directive(
        description = "",
        applicability = DirectiveApplicability.Global
    )

    konst EXPECTED_REACHABLE_NODES by konstueDirective(
        description = "",
        applicability = DirectiveApplicability.Global,
        parser = { it.toIntOrNull() }
    )

    konst RECOMPILE by directive(
        description = "",
        applicability = DirectiveApplicability.File
    )

    konst SOURCE_MAP_EMBED_SOURCES by enumDirective<SourceMapSourceEmbedding>(
        description = "",
        applicability = DirectiveApplicability.Module
    )

    konst CALL_MAIN by directive(
        description = "",
        applicability = DirectiveApplicability.Global
    )

    konst KJS_WITH_FULL_RUNTIME by directive(
        description = "",
        applicability = DirectiveApplicability.Global
    )

    konst EXPECT_ACTUAL_LINKER by directive(
        description = "",
        applicability = DirectiveApplicability.Global
    )

    konst SPLIT_PER_MODULE by directive(
        description = "",
        applicability = DirectiveApplicability.Global
    )

    konst SPLIT_PER_FILE by directive(
        description = "",
        applicability = DirectiveApplicability.Global
    )

    konst SKIP_MANGLE_VERIFICATION by directive(
        description = "",
        applicability = DirectiveApplicability.Global
    )

    konst ERROR_POLICY by enumDirective<ErrorTolerancePolicy>(
        description = "",
        applicability = DirectiveApplicability.Global,
        additionalParser = { ErrorTolerancePolicy.resolvePolicy(it) }
    )

    konst PROPERTY_LAZY_INITIALIZATION by directive(
        description = "",
        applicability = DirectiveApplicability.Global
    )

    konst GENERATE_INLINE_ANONYMOUS_FUNCTIONS by directive(
        description = "translate lambdas into in-line anonymous functions",
        applicability = DirectiveApplicability.Global
    )

    konst GENERATE_STRICT_IMPLICIT_EXPORT by directive(
        description = "enable strict implicitly exported entities types inside d.ts files",
        applicability = DirectiveApplicability.Global
    )

    konst SAFE_EXTERNAL_BOOLEAN by directive(
        description = "",
        applicability = DirectiveApplicability.Global
    )

    konst SAFE_EXTERNAL_BOOLEAN_DIAGNOSTIC by enumDirective<RuntimeDiagnostic>(
        description = "",
        applicability = DirectiveApplicability.Global,
        additionalParser = {
            when (it.lowercase()) {
                K2JsArgumentConstants.RUNTIME_DIAGNOSTIC_LOG -> RuntimeDiagnostic.LOG
                K2JsArgumentConstants.RUNTIME_DIAGNOSTIC_EXCEPTION -> RuntimeDiagnostic.EXCEPTION
                else -> null
            }
        }
    )

    konst DONT_RUN_GENERATED_CODE by stringDirective(
        description = "Specify target backend on which generated code will not be run",
        applicability = DirectiveApplicability.Global,
    )

    konst MAIN_ARGS by konstueDirective(
        description = "Specify arguments that will be passes to main fun",
        applicability = DirectiveApplicability.Global,
        parser = { it.subSequence(1, it.length - 1).split(",") }
    )

    // Next directives are used only inside test system and must not be present in test file

    konst PATH_TO_TEST_DIR by stringDirective(
        description = "Specify the path to directory with test files. " +
                "This path is used to copy hierarchy from test file to test dir and use the same hierarchy in output dir.",
        applicability = DirectiveApplicability.Global
    )

    konst PATH_TO_ROOT_OUTPUT_DIR by stringDirective(
        description = "Specify the path to output directory, where all artifacts will be stored",
        applicability = DirectiveApplicability.Global
    )

    konst TEST_GROUP_OUTPUT_DIR_PREFIX by stringDirective(
        description = "Specify the prefix directory for output directory that will contains artifacts",
        applicability = DirectiveApplicability.Global
    )

    konst TYPED_ARRAYS by directive(
        description = "Enables typed arrays",
        applicability = DirectiveApplicability.Global
    )

    konst GENERATE_SOURCE_MAP by directive(
        description = "Enables generation of source map",
        applicability = DirectiveApplicability.Global
    )

    konst GENERATE_NODE_JS_RUNNER by directive(
        description = "Enables generation of `.node.js` file",
        applicability = DirectiveApplicability.Global
    )

    konst RUN_MINIFIER_BY_DEFAULT by directive(
        description = "Enables minifier even if `EXPECTED_REACHABLE_NODES` directive is not set",
        applicability = DirectiveApplicability.Global
    )

    konst SKIP_REGULAR_MODE by directive(
        description = "Disable js runner for common js and dce files",
        applicability = DirectiveApplicability.Global
    )

    konst GENERATE_DTS by directive(
        description = "Will generate corresponding dts files",
        applicability = DirectiveApplicability.Global
    )

    konst UPDATE_REFERENCE_DTS_FILES by directive(
        description = "",
        applicability = DirectiveApplicability.Global
    )

    // Directives for IR tests

    konst RUN_IR_DCE by directive(
        description = "Enables dead code elimination on IR",
        applicability = DirectiveApplicability.Global
    )

    konst ONLY_IR_DCE by directive(
        description = "Disable non DCE build",
        applicability = DirectiveApplicability.Global
    )

    konst RUN_IC by directive(
        description = "",
        applicability = DirectiveApplicability.Global
    )

    konst SKIP_IR_INCREMENTAL_CHECKS by directive(
        description = "",
        applicability = DirectiveApplicability.Global
    )

    konst KLIB_MAIN_MODULE by directive(
        description = "Specify that main module is actually a klib",
        applicability = DirectiveApplicability.Global
    )

    konst ES6_MODE by directive(
        description = "Enables the Kotlin/JS compilation with ES-classes",
        applicability = DirectiveApplicability.Global
    )

    konst ES_MODULES by directive(
        description = "Specify that the compiled js-sources will use ESM module system",
        applicability = DirectiveApplicability.Global
    )

    konst ENTRY_ES_MODULE by directive(
        description = "Specify the entry point that imports other ESM modules",
        applicability = DirectiveApplicability.File
    )

    konst PER_MODULE by directive(
        description = "",
        applicability = DirectiveApplicability.Global
    )

    konst NO_COMMON_FILES by directive(
        """
            Don't added helper files to prevent linking issues.
        """.trimIndent(),
        applicability = DirectiveApplicability.Global,
    )

    konst KEEP by stringDirective(
        description = "Keep declarations",
        applicability = DirectiveApplicability.Global
    )
}
