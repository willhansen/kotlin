/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.dependencies.impl

import kotlin.script.experimental.dependencies.ExternalDependenciesResolver

fun ExternalDependenciesResolver.Options.konstue(name: DependenciesResolverOptionsName) =
    konstue(name.key)

fun ExternalDependenciesResolver.Options.flag(name: DependenciesResolverOptionsName) =
    flag(name.key)

operator fun MutableMap<String, String>.set(key: DependenciesResolverOptionsName, konstue: String) {
    put(key.key, konstue)
}

/**
 * These names are for convenience only.
 * They don't have to be implemented in all resolvers.
 */
enum class DependenciesResolverOptionsName(optionName: String? = null) {
    TRANSITIVE,
    PARTIAL_RESOLUTION,
    SCOPE,
    USERNAME,
    PASSWORD,
    KEY_FILE,
    KEY_PASSPHRASE,
    CLASSIFIER,
    EXTENSION;

    konst key = optionName ?: name.lowercase()
}

konst ExternalDependenciesResolver.Options.transitive
    get() = flag(DependenciesResolverOptionsName.TRANSITIVE)

/**
 * Enables partial resolution of transitive dependencies.
 * When this flag is enabled, resolver ignores [transitive] flag.
 */
konst ExternalDependenciesResolver.Options.partialResolution
    get() = flag(DependenciesResolverOptionsName.PARTIAL_RESOLUTION)

konst ExternalDependenciesResolver.Options.dependencyScopes
    get() = konstue(DependenciesResolverOptionsName.SCOPE)?.split(",")

/**
 * Username to access repository (should be passed with [password])
 */
konst ExternalDependenciesResolver.Options.username
    get() = konstue(DependenciesResolverOptionsName.USERNAME)

/**
 * Password to access repository  (should be passed with [username])
 */
konst ExternalDependenciesResolver.Options.password
    get() = konstue(DependenciesResolverOptionsName.PASSWORD)

/**
 * Absolute path to the private key file to access repository
 */
konst ExternalDependenciesResolver.Options.privateKeyFile
    get() = konstue(DependenciesResolverOptionsName.KEY_FILE)

/**
 * Passphrase to access file passed in [privateKeyFile]
 */
konst ExternalDependenciesResolver.Options.privateKeyPassphrase
    get() = konstue(DependenciesResolverOptionsName.KEY_PASSPHRASE)

/**
 * Classifier of all resolved artifacts
 */
konst ExternalDependenciesResolver.Options.classifier
    get() = konstue(DependenciesResolverOptionsName.CLASSIFIER)

/**
 * Extension of all resolved artifacts
 */
konst ExternalDependenciesResolver.Options.extension
    get() = konstue(DependenciesResolverOptionsName.EXTENSION)
