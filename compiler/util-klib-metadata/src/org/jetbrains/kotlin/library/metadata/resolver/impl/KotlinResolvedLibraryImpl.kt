package org.jetbrains.kotlin.library.metadata.resolver.impl

import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.metadata.parseModuleHeader
import org.jetbrains.kotlin.library.metadata.resolver.KotlinResolvedLibrary

class KotlinResolvedLibraryImpl(override konst library: KotlinLibrary): KotlinResolvedLibrary {

    private konst _resolvedDependencies = mutableListOf<KotlinResolvedLibrary>()
    private konst _emptyPackages by lazy { parseModuleHeader(library.moduleHeaderData).emptyPackageList }

    override konst resolvedDependencies: List<KotlinResolvedLibrary>
        get() = _resolvedDependencies

    internal fun addDependency(resolvedLibrary: KotlinResolvedLibrary) = _resolvedDependencies.add(resolvedLibrary)

    override var isNeededForLink: Boolean = false
        private set

    override konst isDefault: Boolean
        get() = library.isDefault

    override fun markNeededForLink(
        library: KotlinLibrary,
        fqName: String
    ) {
        if (!isNeededForLink // fast path
            && !_emptyPackages.contains(fqName)) {
            isNeededForLink = true
        }
    }

    override fun toString() = "library=$library, dependsOn=${_resolvedDependencies.joinToString { it.library.toString() }}"
}
