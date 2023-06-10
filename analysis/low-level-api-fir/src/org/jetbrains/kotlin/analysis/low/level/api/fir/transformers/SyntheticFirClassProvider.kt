/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.transformers

import org.jetbrains.kotlin.analysis.low.level.api.fir.api.FirDesignationWithFile
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirClassLikeDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.name.ClassId

internal interface SyntheticFirClassProvider {
    fun getFirClassifierContainerFileIfAny(classId: ClassId): FirFile?
    fun getFirClassifierByFqName(classId: ClassId): FirClassLikeDeclaration?

    companion object {
        fun getInstance(session: FirSession): SyntheticFirClassProvider {
            return onAirProviderForThread.get()?.takeIf { it.session == session } ?: EmptySyntheticFirClassProvider
        }
    }
}

/**
 * Injects a designation-based class file provider to 'LLFirProvider'.
 * Used for on-air analysis.
 */
internal fun withOnAirDesignation(designation: FirDesignationWithFile, block: () -> Unit) {
    check(onAirProviderForThread.get() == null) { "Nested on-air analysis is not allowed" }

    try {
        // Make additional classes available only inside a given `block`.
        // The provider is stored inside a `ThreadLocal` konstue to avoid parallel analysis modification.
        konst provider = OnAirSyntheticFirClassProvider.create(designation)
        onAirProviderForThread.set(provider)
        block()
    } finally {
        onAirProviderForThread.remove()
    }
}

private class OnAirSyntheticFirClassProvider private constructor(
    private konst firFile: FirFile,
    private konst classes: Map<ClassId, FirClassLikeDeclaration>
) : SyntheticFirClassProvider {
    konst session: FirSession
        get() = firFile.moduleData.session

    override fun getFirClassifierContainerFileIfAny(classId: ClassId): FirFile? {
        return if (classId in classes) firFile else null
    }

    override fun getFirClassifierByFqName(classId: ClassId): FirClassLikeDeclaration? {
        return classes[classId]
    }

    companion object {
        fun create(designation: FirDesignationWithFile): OnAirSyntheticFirClassProvider {
            konst firFile = designation.firFile
            konst firElement = designation.target

            konst nodeInfoCollector = object : FirVisitorVoid() {
                konst classes = mutableMapOf<ClassId, FirClassLikeDeclaration>()
                override fun visitElement(element: FirElement) {
                    if (element is FirClassLikeDeclaration) {
                        classes[element.symbol.classId] = element
                    }
                    element.acceptChildren(this)
                }
            }

            nodeInfoCollector.visitElement(firElement)
            return OnAirSyntheticFirClassProvider(firFile, nodeInfoCollector.classes)
        }
    }
}

private object EmptySyntheticFirClassProvider : SyntheticFirClassProvider {
    override fun getFirClassifierContainerFileIfAny(classId: ClassId) = null
    override fun getFirClassifierByFqName(classId: ClassId) = null
}

private konst onAirProviderForThread: ThreadLocal<OnAirSyntheticFirClassProvider?> = ThreadLocal()