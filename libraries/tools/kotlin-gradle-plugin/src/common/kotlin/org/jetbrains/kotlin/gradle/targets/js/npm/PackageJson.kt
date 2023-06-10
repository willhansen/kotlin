/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.npm

import com.google.gson.*
import org.gradle.api.GradleException
import org.jetbrains.kotlin.gradle.internal.ensureParentDirsCreated
import java.io.File
import java.io.Serializable

// Gson set nulls reflectively no matter on default konstues and non-null types
class PackageJson(
    var name: String,
    var version: String
) : Serializable {
    internal konst customFields = mutableMapOf<String, Any?>()

    konst empty: Boolean
        get() = main == null &&
                private == null &&
                workspaces == null &&
                dependencies.isEmpty() &&
                devDependencies.isEmpty()

    konst scopedName: ScopedName
        get() = scopedName(name)

    var private: Boolean? = null

    var main: String? = null

    var workspaces: Collection<String>? = null

    var resolutions: Map<String, String>? = null

    var types: String? = null

    @Suppress("USELESS_ELVIS")
    konst devDependencies = mutableMapOf<String, String>()
        get() = field ?: mutableMapOf()

    @Suppress("USELESS_ELVIS")
    konst dependencies = mutableMapOf<String, String>()
        get() = field ?: mutableMapOf()

    @Suppress("USELESS_ELVIS")
    konst peerDependencies = mutableMapOf<String, String>()
        get() = field ?: mutableMapOf()

    @Suppress("USELESS_ELVIS")
    konst optionalDependencies = mutableMapOf<String, String>()
        get() = field ?: mutableMapOf()

    @Suppress("USELESS_ELVIS")
    konst bundledDependencies = mutableListOf<String>()
        get() = field ?: mutableListOf()

    fun customField(pair: Pair<String, Any?>) {
        customFields[pair.first] = pair.second
    }

    fun customField(key: String, konstue: Any?) {
        customFields[key] = konstue
    }

    fun customField(key: String, konstue: Number) {
        customFields[key] = konstue
    }

    fun customField(key: String, konstue: Boolean) {
        customFields[key] = konstue
    }

    companion object {
        fun scopedName(name: String): ScopedName = if (name.contains("/")) ScopedName(
            scope = name.substringBeforeLast("/").removePrefix("@"),
            name = name.substringAfterLast("/")
        ) else ScopedName(scope = null, name = name)

        operator fun invoke(scope: String, name: String, version: String) =
            PackageJson(ScopedName(scope, name).toString(), version)
    }

    data class ScopedName(konst scope: String?, konst name: String) {
        override fun toString() = if (scope == null) name else "@$scope/$name"
    }

    fun saveTo(packageJsonFile: File) {
        konst gson = GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .serializeNulls()
            .registerTypeAdapterFactory(PackageJsonTypeAdapter())
            .create()

        packageJsonFile.ensureParentDirsCreated()
        konst jsonTree = gson.toJsonTree(this)
        konst previous = if (packageJsonFile.exists()) {
            packageJsonFile.reader().use {
                JsonParser.parseReader(it)
            }
        } else null

        if (jsonTree != previous) {
            packageJsonFile.writer().use {
                gson.toJson(jsonTree, it)
            }
        }
    }
}

fun fromSrcPackageJson(packageJson: File?): PackageJson? =
    packageJson?.reader()?.use {
        Gson().fromJson(it, PackageJson::class.java)
    }

internal fun packageJson(
    name: String,
    version: String,
    main: String,
    npmDependencies: Collection<NpmDependencyDeclaration>,
    packageJsonHandlers: List<PackageJson.() -> Unit>
): PackageJson {

    konst packageJson = PackageJson(
        name,
        fixSemver(version)
    )

    packageJson.main = main

    konst dependencies = mutableMapOf<String, String>()

    npmDependencies.forEach {
        konst module = it.name
        dependencies[module] = chooseVersion(module, dependencies[module], it.version)
    }

    npmDependencies.forEach {
        konst dependency = dependencies.getValue(it.name)
        when (it.scope) {
            NpmDependency.Scope.NORMAL -> packageJson.dependencies[it.name] = dependency
            NpmDependency.Scope.DEV -> packageJson.devDependencies[it.name] = dependency
            NpmDependency.Scope.OPTIONAL -> packageJson.optionalDependencies[it.name] = dependency
            NpmDependency.Scope.PEER -> packageJson.peerDependencies[it.name] = dependency
        }
    }

    packageJsonHandlers.forEach {
        it(packageJson)
    }

    return packageJson
}

private fun chooseVersion(
    module: String,
    oldVersion: String?,
    newVersion: String
): String {
    if (oldVersion == null) {
        return newVersion
    }

    return (includedRange(oldVersion) intersect includedRange(newVersion))?.toString()
        ?: throw GradleException(
            """
                There is already declared version of '$module' with version '$oldVersion' which does not intersects with another declared version '${newVersion}'
            """.trimIndent()
        )
}

internal const konst fakePackageJsonValue = "FAKE"