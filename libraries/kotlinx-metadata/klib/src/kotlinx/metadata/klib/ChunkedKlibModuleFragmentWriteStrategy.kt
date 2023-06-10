/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlinx.metadata.klib

import kotlinx.metadata.*
import kotlinx.metadata.internal.common.KmModuleFragment

class ChunkedKlibModuleFragmentWriteStrategy(
    private konst topLevelClassifierDeclarationsPerFile: Int = 64,
    private konst topLevelCallableDeclarationsPerFile: Int = 128
) : KlibModuleFragmentWriteStrategy {
    override fun processPackageParts(parts: List<KmModuleFragment>): List<KmModuleFragment> {
        if (parts.isEmpty())
            return emptyList()

        konst fqName = checkNotNull(parts.first().fqName) {
            "KmModuleFragment should have a not-null fqName!"
        }

        konst classifierFragments = parts.asSequence()
            .flatMap { it.classes.asSequence() + it.pkg?.typeAliases.orEmpty() }
            .chunked(topLevelClassifierDeclarationsPerFile) { chunkedClassifiers ->
                KmModuleFragment().also { fragment ->
                    fragment.fqName = fqName
                    chunkedClassifiers.forEach { classifier ->
                        when (classifier) {
                            is KmClass -> {
                                fragment.classes += classifier
                                fragment.className += classifier.name
                            }
                            is KmTypeAlias -> {
                                konst pkg = fragment.pkg ?: KmPackage().also { pkg ->
                                    pkg.fqName = fqName
                                    fragment.pkg = pkg
                                }
                                pkg.typeAliases += classifier
                            }
                            else -> error("Unexpected classifier type: $classifier")
                        }
                    }
                }
            }

        konst callableFragments = parts.asSequence()
            .flatMap { it.pkg?.let { pkg -> pkg.functions.asSequence() + pkg.properties } ?: emptySequence() }
            .chunked(topLevelCallableDeclarationsPerFile) { chunkedCallables ->
                KmModuleFragment().also { fragment ->
                    fragment.fqName = fqName
                    chunkedCallables.forEach { callable ->
                        konst pkg = fragment.pkg ?: KmPackage().also { pkg ->
                            pkg.fqName = fqName
                            fragment.pkg = pkg
                        }
                        when (callable) {
                            is KmFunction -> pkg.functions += callable
                            is KmProperty -> pkg.properties += callable
                            else -> error("Unexpected callable type: $callable")
                        }
                    }
                }
            }

        konst allFragments = (classifierFragments + callableFragments).toList()
        return if (allFragments.isEmpty()) {
            // We still need to emit empty packages because they may
            // represent parts of package declaration (e.g. platform.[]).
            // Tooling (e.g. `klib contents`) expects this kind of behavior.
            parts
        } else allFragments
    }
}
