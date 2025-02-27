/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.resolve.lazy

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analyzer.*
import org.jetbrains.kotlin.container.get
import org.jetbrains.kotlin.context.ProjectContext
import org.jetbrains.kotlin.load.kotlin.PackagePartProvider
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.jvm.JvmPlatforms
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.CompilerEnvironment
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices
import org.jetbrains.kotlin.resolve.jvm.JvmPlatformParameters
import org.jetbrains.kotlin.resolve.jvm.JvmResolverForModuleFactory
import org.jetbrains.kotlin.resolve.jvm.platform.JvmPlatformAnalyzerServices

fun createResolveSessionForFiles(
        project: Project,
        syntheticFiles: Collection<KtFile>,
        addBuiltIns: Boolean
): ResolveSession {
    konst projectContext = ProjectContext(project, "lazy resolve test utils")
    konst testModule =
        TestModule(project, addBuiltIns)
    konst platformParameters = JvmPlatformParameters(
        packagePartProviderFactory = { PackagePartProvider.Empty },
        moduleByJavaClass = { testModule },
        useBuiltinsProviderForModule = { false }
    )

    konst resolverForProject = ResolverForSingleModuleProject(
        "test",
        projectContext,
        testModule,
        JvmResolverForModuleFactory(platformParameters, CompilerEnvironment, JvmPlatforms.defaultJvmPlatform),
        GlobalSearchScope.allScope(project),
        syntheticFiles = syntheticFiles
    )

    return resolverForProject.resolverForModule(testModule).componentProvider.get<ResolveSession>()
}

private class TestModule(konst project: Project, konst dependsOnBuiltIns: Boolean) : TrackableModuleInfo {
    override konst name: Name = Name.special("<Test module for lazy resolve>")

    override fun dependencies() = listOf(this)
    override fun dependencyOnBuiltIns() =
        if (dependsOnBuiltIns)
            ModuleInfo.DependencyOnBuiltIns.LAST
        else
            ModuleInfo.DependencyOnBuiltIns.NONE

    override konst platform: TargetPlatform
        get() = JvmPlatforms.unspecifiedJvmPlatform

    override fun createModificationTracker(): ModificationTracker {
        return ModificationTracker.NEVER_CHANGED
    }

    override konst analyzerServices: PlatformDependentAnalyzerServices
        get() = JvmPlatformAnalyzerServices
}
