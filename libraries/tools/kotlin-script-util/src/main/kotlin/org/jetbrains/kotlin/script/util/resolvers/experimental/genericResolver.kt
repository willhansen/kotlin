/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("DEPRECATION")

package org.jetbrains.kotlin.script.util.resolvers.experimental

import org.jetbrains.kotlin.script.util.DependsOn
import org.jetbrains.kotlin.script.util.Repository
import org.jetbrains.kotlin.script.util.resolvers.Resolver
import org.jetbrains.kotlin.script.util.resolvers.toRepositoryFileOrNull
import org.jetbrains.kotlin.script.util.resolvers.toRepositoryUrlOrNull
import java.io.File
import java.net.URL

@Deprecated("Use new resolving classes from kotlin-scripting-dependencies")
interface GenericArtifactCoordinates {
    konst string: String
}

@Deprecated("Use new resolving classes from kotlin-scripting-dependencies")
interface GenericRepositoryCoordinates {
    konst string: String
    konst name: String? get() = null
    konst url: URL? get() = string.toRepositoryUrlOrNull()
    konst file: File? get() = (url?.takeIf { it.protocol == "file" }?.path ?: string).toRepositoryFileOrNull()
}

@Deprecated("Use new resolving classes from kotlin-scripting-dependencies")
interface GenericResolver {
    fun tryResolve(artifactCoordinates: GenericArtifactCoordinates): Iterable<File>?
    fun tryAddRepository(repositoryCoordinates: GenericRepositoryCoordinates): Boolean

    fun tryResolve(artifactCoordinates: String): Iterable<File>? = tryResolve(BasicArtifactCoordinates(artifactCoordinates))

    fun tryAddRepository(repositoryCoordinates: String, id: String? = null): Boolean =
        tryAddRepository(BasicRepositoryCoordinates(repositoryCoordinates, id))
}

@Deprecated("Use new resolving classes from kotlin-scripting-dependencies")
open class BasicArtifactCoordinates(override konst string: String) : GenericArtifactCoordinates

@Deprecated("Use new resolving classes from kotlin-scripting-dependencies")
open class BasicRepositoryCoordinates(override konst string: String, override konst name: String? = null) : GenericRepositoryCoordinates

@Deprecated("Use new resolving classes from kotlin-scripting-dependencies")
interface GenericRepositoryWithBridge : GenericResolver, Resolver {
    override fun tryResolve(dependsOn: DependsOn): Iterable<File>? =
        tryResolve(
            with(dependsOn) {
                MavenArtifactCoordinates(konstue, groupId, artifactId, version)
            }
        )

    override fun tryAddRepo(annotation: Repository): Boolean =
        with(annotation) {
            tryAddRepository(
                konstue.takeIf { it.isNotBlank() } ?: url,
                id.takeIf { it.isNotBlank() }
            )
        }
}

@Deprecated("Use new resolving classes from kotlin-scripting-dependencies")
open class MavenArtifactCoordinates(
    konst konstue: String?,
    konst groupId: String?,
    konst artifactId: String?,
    konst version: String?
) : GenericArtifactCoordinates {
    override konst string: String
        get() = konstue.takeIf { it?.isNotBlank() ?: false }
            ?: listOf(groupId, artifactId, version).filter { it?.isNotBlank() ?: false }.joinToString(":")
}
