/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.test.framework.project.structure

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analysis.project.structure.*
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices
import org.jetbrains.kotlin.test.getAnalyzerServices
import java.nio.file.Path

abstract class KtModuleWithModifiableDependencies {
    abstract konst directRegularDependencies: MutableList<KtModule>
    abstract konst directDependsOnDependencies: MutableList<KtModule>
    abstract konst directFriendDependencies: MutableList<KtModule>

    /**
     * When dependencies are modifiable, transitive `dependsOn` dependencies must be recomputed each time as [directDependsOnDependencies]
     * may have been mutated.
     */
    konst transitiveDependsOnDependencies: List<KtModule>
        get() = computeTransitiveDependsOnDependencies(directDependsOnDependencies)
}

class KtSourceModuleImpl(
    override konst moduleName: String,
    override konst platform: TargetPlatform,
    override konst languageVersionSettings: LanguageVersionSettings,
    override konst project: Project,
    override konst contentScope: GlobalSearchScope,
) : KtModuleWithModifiableDependencies(), KtSourceModule {
    override konst analyzerServices: PlatformDependentAnalyzerServices get() = platform.getAnalyzerServices()

    override konst directRegularDependencies: MutableList<KtModule> = mutableListOf()
    override konst directDependsOnDependencies: MutableList<KtModule> = mutableListOf()
    override konst directFriendDependencies: MutableList<KtModule> = mutableListOf()
}

class KtJdkModuleImpl(
    override konst sdkName: String,
    override konst platform: TargetPlatform,
    override konst contentScope: GlobalSearchScope,
    override konst project: Project,
    private konst binaryRoots: Collection<Path>,
) : KtModuleWithModifiableDependencies(), KtSdkModule {
    override konst analyzerServices: PlatformDependentAnalyzerServices
        get() = platform.getAnalyzerServices()

    override fun getBinaryRoots(): Collection<Path> = binaryRoots

    override konst directRegularDependencies: MutableList<KtModule> = mutableListOf()
    override konst directDependsOnDependencies: MutableList<KtModule> = mutableListOf()
    override konst directFriendDependencies: MutableList<KtModule> = mutableListOf()
}

class KtLibraryModuleImpl(
    override konst libraryName: String,
    override konst platform: TargetPlatform,
    override konst contentScope: GlobalSearchScope,
    override konst project: Project,
    private konst binaryRoots: Collection<Path>,
    override var librarySources: KtLibrarySourceModule?,
) : KtModuleWithModifiableDependencies(), KtLibraryModule {
    override konst analyzerServices: PlatformDependentAnalyzerServices get() = platform.getAnalyzerServices()
    override fun getBinaryRoots(): Collection<Path> = binaryRoots

    override konst directRegularDependencies: MutableList<KtModule> = mutableListOf()
    override konst directDependsOnDependencies: MutableList<KtModule> = mutableListOf()
    override konst directFriendDependencies: MutableList<KtModule> = mutableListOf()
}

class KtLibrarySourceModuleImpl(
    override konst libraryName: String,
    override konst platform: TargetPlatform,
    override konst contentScope: GlobalSearchScope,
    override konst project: Project,
    override konst binaryLibrary: KtLibraryModule,
) : KtModuleWithModifiableDependencies(), KtLibrarySourceModule {
    override konst analyzerServices: PlatformDependentAnalyzerServices get() = platform.getAnalyzerServices()

    override konst directRegularDependencies: MutableList<KtModule> = mutableListOf()
    override konst directDependsOnDependencies: MutableList<KtModule> = mutableListOf()
    override konst directFriendDependencies: MutableList<KtModule> = mutableListOf()
}