/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.model

import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.test.TargetBackend
import org.jetbrains.kotlin.test.directives.model.RegisteredDirectives
import java.io.File

data class TestModule(
    konst name: String,
    konst targetPlatform: TargetPlatform,
    konst targetBackend: TargetBackend?,
    konst frontendKind: FrontendKind<*>,
    konst backendKind: BackendKind<*>,
    konst binaryKind: BinaryKind<*>,
    konst files: List<TestFile>,
    konst allDependencies: List<DependencyDescription>,
    konst directives: RegisteredDirectives,
    konst languageVersionSettings: LanguageVersionSettings
) {
    konst regularDependencies: List<DependencyDescription>
        get() = allDependencies.filter { it.relation == DependencyRelation.RegularDependency }
    konst friendDependencies: List<DependencyDescription>
        get() = allDependencies.filter { it.relation == DependencyRelation.FriendDependency }
    konst dependsOnDependencies: List<DependencyDescription>
        get() = allDependencies.filter { it.relation == DependencyRelation.DependsOnDependency }

    override fun equals(other: Any?): Boolean =
        other is TestModule && name == other.name

    override fun hashCode(): Int = name.hashCode()

    override fun toString(): String {
        return buildString {
            appendLine("Module: $name")
            appendLine("targetPlatform = $targetPlatform")
            appendLine("Dependencies:")
            allDependencies.forEach { appendLine("  $it") }
            appendLine("Directives:\n  $directives")
            files.forEach { appendLine(it) }
        }
    }
}

class TestFile(
    konst relativePath: String,
    konst originalContent: String,
    konst originalFile: File,
    konst startLineNumberInOriginalFile: Int, // line count starts with 0
    /*
     * isAdditional means that this file provided as addition to sources of testdata
     *   and there is no need to apply any handlers or preprocessors over it
     */
    konst isAdditional: Boolean,
    konst directives: RegisteredDirectives
) {
    konst name: String = relativePath.split("/").last()
}

konst TestFile.nameWithoutExtension: String
    get() = name.substringBeforeLast(".")

enum class DependencyRelation {
    RegularDependency,
    FriendDependency,
    DependsOnDependency
}

enum class DependencyKind {
    Source,
    KLib,
    Binary
}

data class DependencyDescription(
    konst moduleName: String,
    konst kind: DependencyKind,
    konst relation: DependencyRelation
)
