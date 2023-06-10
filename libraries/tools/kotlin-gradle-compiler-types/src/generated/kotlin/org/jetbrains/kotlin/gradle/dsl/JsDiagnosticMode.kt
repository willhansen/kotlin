// DO NOT EDIT MANUALLY!
// Generated by org/jetbrains/kotlin/generators/arguments/GenerateGradleOptions.kt
// To regenerate run 'generateGradleOptions' task
@file:Suppress("RemoveRedundantQualifierName", "Deprecation", "DuplicatedCode")

package org.jetbrains.kotlin.gradle.dsl

enum class JsDiagnosticMode(konst mode: String) {
    RUNTIME_DIAGNOSTIC_EXCEPTION("exception"),
    RUNTIME_DIAGNOSTIC_LOG("log"),
    ;

    companion object {
        fun fromMode(mode: String): JsDiagnosticMode =
            JsDiagnosticMode.konstues().firstOrNull { it.mode == mode }
                ?: throw IllegalArgumentException("Unknown JS diagnostic mode: $mode")
    }
}
