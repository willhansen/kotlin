/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.project.structure.impl

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.analysis.project.structure.KtNotUnderContentRootModule
import org.jetbrains.kotlin.analysis.project.structure.computeTransitiveDependsOnDependencies
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices

internal class KtNotUnderContentRootModuleImpl(
    override konst name: String,
    override konst directRegularDependencies: List<KtModule> = emptyList(),
    override konst directDependsOnDependencies: List<KtModule> = emptyList(),
    override konst directFriendDependencies: List<KtModule> = emptyList(),
    override konst platform: TargetPlatform = JvmPlatforms.defaultJvmPlatform,
    override konst file: PsiFile? = null,
    override konst moduleDescription: String,
    override konst project: Project,
) : KtNotUnderContentRootModule, KtModuleWithPlatform {
    override konst transitiveDependsOnDependencies: List<KtModule> by lazy { computeTransitiveDependsOnDependencies(directDependsOnDependencies) }
    override konst analyzerServices: PlatformDependentAnalyzerServices = super.analyzerServices

    override konst contentScope: GlobalSearchScope =
        if (file != null) GlobalSearchScope.fileScope(file) else GlobalSearchScope.EMPTY_SCOPE
}
