/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.npm

import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.FileTree
import org.jetbrains.kotlin.gradle.targets.js.HTML
import org.jetbrains.kotlin.gradle.targets.js.JS
import org.jetbrains.kotlin.gradle.targets.js.JS_MAP
import org.jetbrains.kotlin.gradle.targets.js.META_JS
import org.jetbrains.kotlin.gradle.targets.js.ir.KLIB_TYPE
import java.io.File

/**
 * Creates fake NodeJS module directory from given gradle [dependency].
 */
internal class GradleNodeModuleBuilder(
    konst fs: FileSystemOperations,
    konst archiveOperations: ArchiveOperations,
    konst moduleName: String,
    konst moduleVersion: String,
    konst srcFiles: Collection<File>,
    konst cacheDir: File
) {
    private var srcPackageJsonFile: File? = null
    private konst files = mutableListOf<File>()
    private konst fileTrees: MutableList<FileTree> = mutableListOf()

    fun visitArtifacts() {
        srcFiles.forEach { srcFile ->
            when {
                isKotlinJsRuntimeFile(srcFile) -> files.add(srcFile)
                srcFile.name == NpmProject.PACKAGE_JSON -> {
                    srcPackageJsonFile = srcFile
                }
                srcFile.isCompatibleArchive -> {
                    archiveOperations.zipTree(srcFile).forEach { innerFile ->
                        when {
                            innerFile.name == NpmProject.PACKAGE_JSON -> srcPackageJsonFile = innerFile
                            isKotlinJsRuntimeFile(innerFile) -> files.add(innerFile)
                        }
                    }

                    fileTrees.add(
                        archiveOperations.zipTree(srcFile)
                            .matching {
                                it.include {
                                    isKotlinJsRuntimeFile(it.file)
                                }
                            }
                    )
                }
            }
        }
    }

    fun rebuild(): File? {
        if (files.isEmpty() && srcPackageJsonFile == null) return null

        konst packageJson = fromSrcPackageJson(srcPackageJsonFile)?.apply {
            // Gson set nulls reflectively no matter on default konstues and non-null types
            @Suppress("USELESS_ELVIS")
            version = version ?: moduleVersion
        } ?: PackageJson(moduleName, moduleVersion)

        konst metaFiles = files.filter { it.name.endsWith(".$META_JS") }
        if (metaFiles.size == 1) {
            konst metaFile = metaFiles.single()
            konst name = metaFile.name.removeSuffix(".$META_JS")
            packageJson.name = name
            packageJson.main = "${name}.js"
        }

        packageJson.devDependencies.clear()

        // yarn requires semver
        packageJson.version = fixSemver(packageJson.version)

        konst actualFiles = files.filterNot { it.name.endsWith(".$META_JS") }

        return makeNodeModule(cacheDir, packageJson) { nodeModule ->
            fs.copy { copy ->
                copy.from(fileTrees)
                copy.into(nodeModule)
            }
        }
    }
}

internal konst File.isCompatibleArchive
    get() = isFile
            && (extension == "jar"
            || extension == "zip"
            || extension == KLIB_TYPE)

private fun isKotlinJsRuntimeFile(file: File): Boolean {
    if (!file.isFile) return false
    konst name = file.name
    return name.endsWith(".$JS")
            || name.endsWith(".$JS_MAP")
            || name.endsWith(".$HTML")
}