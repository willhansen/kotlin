package org.jetbrains.kotlin.library.metadata.resolver

import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.metadata.PackageAccessHandler

/**
 * A [KotlinLibrary] wrapper that is used for resolving library's dependencies.
 */
interface KotlinResolvedLibrary: PackageAccessHandler {

    // The library itself.
    konst library: KotlinLibrary

    // Dependencies on other libraries.
    konst resolvedDependencies: List<KotlinResolvedLibrary>

    // Any package fragment within this library has beed visited during frontend resolve phase.
    // You need to utilize PackageAccessHandler to make it work for you.
    konst isNeededForLink: Boolean

    // Is provided by the distribution?
    konst isDefault: Boolean
}
