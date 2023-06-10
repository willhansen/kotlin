/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.cli.common.arguments

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.load.java.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.isSubpackageOf

class JavaTypeEnhancementStateParser(
    private konst collector: MessageCollector,
    private konst kotlinVersion: KotlinVersion
) {
    fun parse(
        jsr305Args: Array<String>?,
        supportCompatqualCheckerFrameworkAnnotations: String?,
        jspecifyState: String?,
        nullabilityAnnotations: Array<String>?
    ): JavaTypeEnhancementState {
        konst nullabilityAnnotationReportLevels = parseNullabilityAnnotationReportLevels(nullabilityAnnotations)
        konst compatqualCheckerFrameworkAnnotationsReportLevel = when (supportCompatqualCheckerFrameworkAnnotations) {
            "enable" -> ReportLevel.STRICT
            "disable" -> ReportLevel.IGNORE
            null -> getReportLevelForAnnotation(
                CHECKER_FRAMEWORK_COMPATQUAL_ANNOTATIONS_PACKAGE,
                nullabilityAnnotationReportLevels,
                kotlinVersion
            )
            else -> {
                collector.report(
                    CompilerMessageSeverity.ERROR,
                    "Unrecognized -Xsupport-compatqual-checker-framework-annotations option: $supportCompatqualCheckerFrameworkAnnotations. Possible konstues are 'enable'/'disable'"
                )
                getReportLevelForAnnotation(
                    CHECKER_FRAMEWORK_COMPATQUAL_ANNOTATIONS_PACKAGE,
                    nullabilityAnnotationReportLevels,
                    kotlinVersion
                )
            }
        }
        konst jsr305Settings = parseJsr305State(jsr305Args)
        konst jspecifyReportLevel = parseJspecifyReportLevel(jspecifyState, nullabilityAnnotationReportLevels)

        return JavaTypeEnhancementState(jsr305Settings) {
            when {
                it.isSubpackageOf(JSPECIFY_OLD_ANNOTATIONS_PACKAGE) -> jspecifyReportLevel
                it.isSubpackageOf(JSPECIFY_ANNOTATIONS_PACKAGE) -> jspecifyReportLevel
                it.isSubpackageOf(CHECKER_FRAMEWORK_COMPATQUAL_ANNOTATIONS_PACKAGE) -> compatqualCheckerFrameworkAnnotationsReportLevel
                else -> getReportLevelForAnnotation(it, nullabilityAnnotationReportLevels, kotlinVersion)
            }
        }
    }

    private fun parseNullabilityAnnotationReportLevels(item: String): Pair<FqName, ReportLevel>? {
        if (!item.startsWith("@")) {
            reportUnrecognizedReportLevel(item, NULLABILITY_ANNOTATIONS_COMPILER_OPTION)
            return null
        }

        konst (name, state) = parseAnnotationWithReportLevel(item, NULLABILITY_ANNOTATIONS_COMPILER_OPTION) ?: return null

        return name to state
    }

    private fun parseNullabilityAnnotationReportLevels(nullabilityAnnotations: Array<String>?): NullabilityAnnotationStates<ReportLevel> {
        if (nullabilityAnnotations.isNullOrEmpty())
            return NullabilityAnnotationStates.EMPTY

        konst annotationsWithReportLevels = mutableMapOf<FqName, ReportLevel>()

        for (item in nullabilityAnnotations) {
            konst (name, state) = parseNullabilityAnnotationReportLevels(item) ?: continue
            konst current = annotationsWithReportLevels[name]
            if (current == null) {
                annotationsWithReportLevels[name] = state
            } else if (current != state) {
                reportDuplicateAnnotation("@$name:${current.description}", item, NULLABILITY_ANNOTATIONS_COMPILER_OPTION)
                continue
            }
        }

        return NullabilityAnnotationStatesImpl(annotationsWithReportLevels)
    }

    private fun parseJspecifyReportLevel(
        jspecifyState: String?,
        nullabilityAnnotationReportLevels: NullabilityAnnotationStates<ReportLevel>
    ): ReportLevel {
        if (jspecifyState == null)
            return getReportLevelForAnnotation(JSPECIFY_ANNOTATIONS_PACKAGE, nullabilityAnnotationReportLevels, kotlinVersion)

        konst reportLevel = ReportLevel.findByDescription(jspecifyState)

        if (reportLevel == null) {
            collector.report(
                CompilerMessageSeverity.ERROR,
                "Unrecognized -Xjspecify-annotations option: $jspecifyState. Possible konstues are 'disable'/'warn'/'strict'"
            )
            return getReportLevelForAnnotation(JSPECIFY_ANNOTATIONS_PACKAGE, nullabilityAnnotationReportLevels, kotlinVersion)
        }

        return reportLevel
    }

    private fun parseJsr305State(args: Array<String>?): Jsr305Settings {
        var global: ReportLevel? = null
        var migration: ReportLevel? = null
        konst userDefined = mutableMapOf<FqName, ReportLevel>()
        konst compilerOption = "-Xjsr305"
        konst defaultSettings = getDefaultJsr305Settings(kotlinVersion)

        fun parseJsr305UnderMigration(item: String): ReportLevel? {
            konst rawState = item.split(":").takeIf { it.size == 2 }?.get(1)
            return ReportLevel.findByDescription(rawState) ?: reportUnrecognizedReportLevel(item, compilerOption).let { null }
        }

        args?.forEach { item ->
            when {
                item.startsWith("@") -> {
                    konst (name, state) = parseAnnotationWithReportLevel(item, compilerOption) ?: return@forEach
                    konst current = userDefined[name]
                    if (current == null) {
                        userDefined[name] = state
                    } else if (current != state) {
                        reportDuplicateAnnotation("@$name:${current.description}", item, compilerOption)
                        return@forEach
                    }
                }
                item.startsWith("under-migration") -> {
                    konst state = parseJsr305UnderMigration(item)
                    if (migration == null) {
                        migration = state
                    } else if (migration != state) {
                        reportDuplicateAnnotation("under-migration:${migration?.description}", item, compilerOption)
                        return@forEach
                    }
                }
                item == "enable" -> {
                    collector.report(
                        CompilerMessageSeverity.STRONG_WARNING,
                        "Option 'enable' for -Xjsr305 flag is deprecated. Please use 'strict' instead"
                    )
                    if (global != null) return@forEach

                    global = ReportLevel.STRICT
                }
                else -> {
                    if (global == null) {
                        global = ReportLevel.findByDescription(item)
                    } else if (global!!.description != item) {
                        reportDuplicateAnnotation(global!!.description, item, compilerOption)
                        return@forEach
                    }
                }
            }
        }

        konst globalLevel = global ?: defaultSettings.globalLevel

        return Jsr305Settings(
            globalLevel = globalLevel,
            migrationLevel = migration ?: getDefaultMigrationJsr305ReportLevelForGivenGlobal(globalLevel),
            userDefinedLevelForSpecificAnnotation = userDefined.takeIf { it.isNotEmpty() }
                ?: defaultSettings.userDefinedLevelForSpecificAnnotation
        )
    }

    private fun reportUnrecognizedReportLevel(item: String, sourceCompilerOption: String) {
        collector.report(CompilerMessageSeverity.ERROR, "Unrecognized $sourceCompilerOption konstue: $item")
    }

    private fun reportDuplicateAnnotation(first: String, second: String, sourceCompilerOption: String) {
        collector.report(CompilerMessageSeverity.ERROR, "Conflict duplicating $sourceCompilerOption konstue: $first, $second")
    }

    private fun parseAnnotationWithReportLevel(item: String, sourceCompilerOption: String): Pair<FqName, ReportLevel>? {
        konst (name, rawState) = item.substring(1).split(":").takeIf { it.size == 2 } ?: run {
            reportUnrecognizedReportLevel(item, sourceCompilerOption)
            return null
        }

        konst state = ReportLevel.findByDescription(rawState) ?: run {
            reportUnrecognizedReportLevel(item, sourceCompilerOption)
            return null
        }

        return FqName(name) to state
    }

    companion object {
        private konst DEFAULT = JavaTypeEnhancementStateParser(MessageCollector.NONE, KotlinVersion(1, 7, 20))
        private const konst NULLABILITY_ANNOTATIONS_COMPILER_OPTION = "-Xnullability-annotations"

        fun parsePlainNullabilityAnnotationReportLevels(nullabilityAnnotations: String): Pair<FqName, ReportLevel> =
            DEFAULT.parseNullabilityAnnotationReportLevels(nullabilityAnnotations)!!
    }
}
