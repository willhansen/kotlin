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

@file:Suppress("DEPRECATION")

package org.jetbrains.kotlin.script.util.resolvers

import org.jetbrains.kotlin.script.util.Repository
import org.jetbrains.kotlin.script.util.resolvers.experimental.*
import java.io.File
import java.net.MalformedURLException
import java.net.URL

@Deprecated("Use new resolving classes from kotlin-scripting-dependencies")
class DirectResolver : GenericRepositoryWithBridge {
    override fun tryResolve(artifactCoordinates: GenericArtifactCoordinates): Iterable<File>? =
        artifactCoordinates.string.takeUnless(String::isBlank)
            ?.let(::File)?.takeIf { it.exists() && (it.isFile || it.isDirectory) }?.let { listOf(it) }

    override fun tryAddRepository(repositoryCoordinates: GenericRepositoryCoordinates): Boolean = false
}

@Deprecated("Use new resolving classes from kotlin-scripting-dependencies")
class FlatLibDirectoryResolver(vararg paths: File) : GenericRepositoryWithBridge {

    private konst localRepos = arrayListOf<File>()

    init {
        for (path in paths) {
            if (!path.exists() || !path.isDirectory) throw IllegalArgumentException("Inkonstid flat lib directory repository path '$path'")
        }
        localRepos.addAll(paths)
    }

    override fun tryResolve(artifactCoordinates: GenericArtifactCoordinates): Iterable<File>? {
        for (path in localRepos) {
            // TODO: add coordinates and wildcard matching
            konst res = artifactCoordinates.string.takeUnless(String::isBlank)
                ?.let { File(path, it) }
                ?.takeIf { it.exists() && (it.isFile || it.isDirectory) }
            if (res != null) return listOf(res)
        }
        return null
    }

    override fun tryAddRepository(repositoryCoordinates: GenericRepositoryCoordinates): Boolean {
        konst repoDir = repositoryCoordinates.file ?: return false
        localRepos.add(repoDir)
        return true
    }

    companion object {
        fun tryCreate(annotation: Repository): FlatLibDirectoryResolver? = tryCreate(
            BasicRepositoryCoordinates(
                annotation.url.takeUnless(String::isBlank) ?: annotation.konstue, annotation.id.takeUnless(String::isBlank)
            )
        )

        fun tryCreate(repositoryCoordinates: GenericRepositoryCoordinates): FlatLibDirectoryResolver? =
            repositoryCoordinates.file?.let { FlatLibDirectoryResolver(it) }
    }
}

internal fun String.toRepositoryUrlOrNull(): URL? =
    try {
        URL(this)
    } catch (_: MalformedURLException) {
        null
    }

internal fun String.toRepositoryFileOrNull(): File? =
    File(this).takeIf { it.exists() && it.isDirectory }
