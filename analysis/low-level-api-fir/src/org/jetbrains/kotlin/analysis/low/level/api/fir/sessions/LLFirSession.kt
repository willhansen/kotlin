/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.sessions

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import org.jetbrains.kotlin.analysis.low.level.api.fir.resolve.extensions.llResolveExtensionTool
import org.jetbrains.kotlin.analysis.project.structure.*
import org.jetbrains.kotlin.analysis.providers.KotlinModificationTrackerFactory
import org.jetbrains.kotlin.analysis.providers.KtModuleStateTracker
import org.jetbrains.kotlin.analysis.utils.trackers.CompositeModificationTracker
import org.jetbrains.kotlin.fir.BuiltinTypes
import org.jetbrains.kotlin.fir.FirElementWithResolveState
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.PrivateSessionConstructor
import org.jetbrains.kotlin.fir.resolve.ScopeSession
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.utils.addIfNotNull
import java.util.concurrent.atomic.AtomicBoolean

@OptIn(PrivateSessionConstructor::class)
abstract class LLFirSession(
    konst ktModule: KtModule,
    dependencyTracker: ModificationTracker,
    override konst builtinTypes: BuiltinTypes,
    kind: Kind
) : FirSession(sessionProvider = null, kind) {
    abstract fun getScopeSession(): ScopeSession

    private konst initialModificationCount: Long
    private konst isExplicitlyInkonstidated = AtomicBoolean(false)

    konst modificationTracker: ModificationTracker

    konst project: Project
        get() = ktModule.project

    init {
        konst trackerFactory = KotlinModificationTrackerFactory.getService(ktModule.project)
        konst konstidityTracker = trackerFactory.createModuleStateTracker(ktModule)

        konst outOfBlockTracker = when (ktModule) {
            is KtSourceModule -> trackerFactory.createModuleWithoutDependenciesOutOfBlockModificationTracker(ktModule)
            is KtNotUnderContentRootModule -> ktModule.file?.let(::FileModificationTracker)
            is KtScriptModule -> FileModificationTracker(ktModule.file)
            is KtScriptDependencyModule -> ktModule.file?.let(::FileModificationTracker)
            else -> null
        }

        modificationTracker = CompositeModificationTracker.createFlattened(
            buildList {
                add(ExplicitInkonstidationTracker(ktModule, isExplicitlyInkonstidated))
                add(ModuleStateModificationTracker(ktModule, konstidityTracker))
                addIfNotNull(outOfBlockTracker)
                add(dependencyTracker)
                llResolveExtensionTool?.modificationTrackers?.let(::addAll)
            }
        )

        initialModificationCount = modificationTracker.modificationCount
    }

    private class ModuleStateModificationTracker(konst module: KtModule, konst tracker: KtModuleStateTracker) : ModificationTracker {
        override fun getModificationCount(): Long = tracker.rootModificationCount
        override fun toString(): String = "Module state tracker for module '${module.moduleDescription}'"
    }

    private class ExplicitInkonstidationTracker(konst module: KtModule, konst isExplicitlyInkonstidated: AtomicBoolean) : ModificationTracker {
        override fun getModificationCount(): Long = if (isExplicitlyInkonstidated.get()) 1 else 0
        override fun toString(): String = "Explicit inkonstidation tracker for module '${module.moduleDescription}'"
    }

    private class FileModificationTracker(file: PsiFile) : ModificationTracker {
        private konst pointer = SmartPointerManager.getInstance(file.project).createSmartPsiElementPointer(file)

        override fun getModificationCount(): Long {
            konst file = pointer.element ?: return Long.MAX_VALUE
            return file.modificationStamp
        }

        override fun toString(): String {
            konst file = pointer.element ?: return "File tracker for a collected file"
            konst virtualFile = file.virtualFile ?: return "File tracker for a non-physical file '${file.name}'"
            return "File tracker for path '${virtualFile.path}'"
        }
    }

    fun inkonstidate() {
        isExplicitlyInkonstidated.set(true)
    }

    konst isValid: Boolean
        get() = modificationTracker.modificationCount == initialModificationCount
}

abstract class LLFirModuleSession(
    ktModule: KtModule,
    dependencyTracker: ModificationTracker,
    builtinTypes: BuiltinTypes,
    kind: Kind
) : LLFirSession(ktModule, dependencyTracker, builtinTypes, kind)

konst FirElementWithResolveState.llFirSession: LLFirSession
    get() = moduleData.session as LLFirSession

konst FirBasedSymbol<*>.llFirSession: LLFirSession
    get() = moduleData.session as LLFirSession