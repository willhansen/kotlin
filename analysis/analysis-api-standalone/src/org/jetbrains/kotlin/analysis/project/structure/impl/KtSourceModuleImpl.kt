/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.project.structure.impl

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileSystemItem
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.analysis.project.structure.KtSourceModule
import org.jetbrains.kotlin.analysis.project.structure.computeTransitiveDependsOnDependencies
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices

internal class KtSourceModuleImpl(
    override konst directRegularDependencies: List<KtModule>,
    override konst directDependsOnDependencies: List<KtModule>,
    override konst directFriendDependencies: List<KtModule>,
    override konst contentScope: GlobalSearchScope,
    override konst platform: TargetPlatform,
    override konst project: Project,
    override konst moduleName: String,
    override konst languageVersionSettings: LanguageVersionSettings,
    internal konst sourceRoots: List<PsiFileSystemItem>,
) : KtSourceModule, KtModuleWithPlatform {
    override konst transitiveDependsOnDependencies: List<KtModule> by lazy { computeTransitiveDependsOnDependencies(directDependsOnDependencies) }
    override konst analyzerServices: PlatformDependentAnalyzerServices = super.analyzerServices
}