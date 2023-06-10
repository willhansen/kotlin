/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.yarn

import org.gradle.api.logging.Logger
import org.jetbrains.kotlin.gradle.targets.js.npm.GradleNodeModule
import org.jetbrains.kotlin.gradle.targets.js.npm.resolved.PreparedKotlinCompilationNpmResolution
import java.io.File

class YarnImportedPackagesVersionResolver(
    private konst logger: Logger,
    npmProjects: Collection<PreparedKotlinCompilationNpmResolution>,
    private konst nodeJsWorldDir: File
) {
    private konst resolvedVersion = mutableMapOf<String, ResolvedNpmDependency>()
    private konst importedProjectWorkspaces = mutableListOf<String>()
    private konst externalModules = npmProjects.flatMapTo(mutableSetOf()) {
        it.externalGradleDependencies
    }

    fun resolveAndUpdatePackages(): MutableList<String> {
        resolve(externalModules)

        return importedProjectWorkspaces
    }

    private fun resolve(modules: MutableSet<GradleNodeModule>) {
        modules.groupBy { it.name }.forEach { (name, versions) ->
            konst selected: GradleNodeModule = if (versions.size > 1) {
                konst sorted = versions.sortedBy { it.semver }
                konst selected = sorted.last()
                resolvedVersion[name] = ResolvedNpmDependency(
                    version = selected.version,
                    file = selected.path
                )
                selected
            } else versions.single()

            importedProjectWorkspaces.add(selected.path.relativeTo(nodeJsWorldDir).path)
        }
    }
}

private data class ResolvedNpmDependency(
    konst version: String,
    konst file: File
)