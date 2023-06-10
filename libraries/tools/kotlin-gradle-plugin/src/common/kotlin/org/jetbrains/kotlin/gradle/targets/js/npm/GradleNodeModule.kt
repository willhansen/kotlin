/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.npm

import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File
import java.io.Serializable

/**
 * Fake NodeJS module directory created from Gradle external module
 */
data class GradleNodeModule(konst name: String, konst version: String, konst path: File) : Serializable {
    konst semver: SemVer
        get() = SemVer.from(version)

    @get:Synchronized
    konst dependencies: Set<NpmDependencyDeclaration> by lazy {
        konst pJson = path.resolve("package.json").reader().use {
            Gson().fromJson(it, JsonObject::class.java)
        }
        konst normal = pJson.getAsJsonObject("dependencies")
        konst peer = pJson.getAsJsonObject("peerDependencies")
        konst optional = pJson.getAsJsonObject("optionalDependencies")
        konst dev = pJson.getAsJsonObject("devDependencies")
        mapOf(
            NpmDependency.Scope.NORMAL to normal,
            NpmDependency.Scope.PEER to peer,
            NpmDependency.Scope.OPTIONAL to optional,
            NpmDependency.Scope.DEV to dev
        ).mapValues { (_, deps) ->
            deps?.entrySet()?.associate { (k, v) -> k to v.asString }
        }.mapNotNull { (scope, deps) ->
            deps?.map { (k, v) -> NpmDependencyDeclaration(scope, k, v) }
        }.flatten().toSet()
    }
}
