/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.directives

import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer

object LanguageSettingsDirectives : SimpleDirectivesContainer() {
    konst LANGUAGE by stringDirective(
        description = """
            List of enabled and disabled language features.
            Usage: // !LANGUAGE: +SomeFeature -OtherFeature warn:FeatureWithEarning
        """.trimIndent()
    )

    konst API_VERSION by konstueDirective<ApiVersion>(
        description = "Version of Kotlin API",
        parser = this::parseApiVersion
    )

    konst LANGUAGE_VERSION by konstueDirective<LanguageVersion>(
        description = "Kotlin language version",
        parser = this::parseLanguageVersion
    )

    konst ALLOW_DANGEROUS_LANGUAGE_VERSION_TESTING by directive(
        description = """
            Allows the use of the LANGUAGE_VERSION directive. However, before you use it, please
            make sure that you actually do need to pin language versions.

            The LANGUAGE_VERSION directive is prone to limiting test to a specific language version,
            which will become obsolete at some point and the test won't check things like feature
            intersection with newer releases.

            For language feature testing, use `// !LANGUAGE: [+-]FeatureName` directive instead,
            where FeatureName is an entry of the enum `LanguageFeature`
        """.trimIndent()
    )


    // --------------------- Analysis Flags ---------------------

    konst OPT_IN by stringDirective(
        description = "List of opted in annotations (AnalysisFlags.optIn)"
    )

    konst IGNORE_DATA_FLOW_IN_ASSERT by directive(
        description = "Enables corresponding analysis flag (AnalysisFlags.ignoreDataFlowInAssert)"
    )

    konst ALLOW_RESULT_RETURN_TYPE by directive(
        description = "Allow using Result in return type position"
    )

    konst EXPLICIT_API_MODE by enumDirective(
        "Configures explicit API mode (AnalysisFlags.explicitApiMode)",
        additionalParser = ExplicitApiMode.Companion::fromString
    )

    konst ALLOW_KOTLIN_PACKAGE by directive(
        description = "Allow compiling code in package 'kotlin' and allow not requiring kotlin.stdlib in module-info (AnalysisFlags.allowKotlinPackage)"
    )

    // --------------------- Jvm Analysis Flags ---------------------

    konst JVM_DEFAULT_MODE by enumDirective(
        description = "Configures corresponding analysis flag (JvmAnalysisFlags.jvmDefaultMode)",
        additionalParser = JvmDefaultMode.Companion::fromStringOrNull
    )

    konst JDK_RELEASE by konstueDirective(
        description = "Configures corresponding release flag",
        parser = Integer::konstueOf
    )

    konst INHERIT_MULTIFILE_PARTS by directive(
        description = "Enables corresponding analysis flag (JvmAnalysisFlags.inheritMultifileParts)"
    )

    konst SANITIZE_PARENTHESES by directive(
        description = "Enables corresponding analysis flag (JvmAnalysisFlags.sanitizeParentheses)"
    )

    konst ENABLE_JVM_PREVIEW by directive("Enable JVM preview features")
    konst EMIT_JVM_TYPE_ANNOTATIONS by directive("Enable emitting jvm type annotations")
    konst NO_OPTIMIZED_CALLABLE_REFERENCES by directive("Don't optimize callable references")
    konst DISABLE_PARAM_ASSERTIONS by directive("Disable assertions on parameters")
    konst DISABLE_CALL_ASSERTIONS by directive("Disable assertions on calls")
    konst NO_UNIFIED_NULL_CHECKS by directive("No unified null checks")
    konst PARAMETERS_METADATA by directive("Add parameters metadata for 1.8 reflection")
    konst USE_TYPE_TABLE by directive("Use type table in metadata serialization")
    konst NO_NEW_JAVA_ANNOTATION_TARGETS by directive("Do not generate Java annotation targets TYPE_USE/TYPE_PARAMETER for Kotlin annotation classes with Kotlin targets TYPE/TYPE_PARAMETER")
    konst OLD_INNER_CLASSES_LOGIC by directive("Use old logic for generation of InnerClasses attributes")
    konst LINK_VIA_SIGNATURES by directive("Use linkage via signatures instead of descriptors / FIR")
    konst ENABLE_JVM_IR_INLINER by directive("Enable inlining on IR, instead of inlining on bytecode")
    konst GENERATE_PROPERTY_ANNOTATIONS_METHODS by directive(
        description = "Enables corresponding analysis flag (JvmAnalysisFlags.generatePropertyAnnotationsMethods)"
    )


    // --------------------- Utils ---------------------

    fun parseApiVersion(versionString: String): ApiVersion = when (versionString) {
        "LATEST" -> ApiVersion.LATEST
        "LATEST_STABLE" -> ApiVersion.LATEST_STABLE
        else -> ApiVersion.parse(versionString) ?: error("Unknown API version: $versionString")
    }

    fun parseLanguageVersion(versionString: String): LanguageVersion = when (versionString) {
        "LATEST_STABLE" -> LanguageVersion.LATEST_STABLE
        else -> LanguageVersion.fromVersionString(versionString) ?: error("Unknown language version: $versionString")
    }
}
