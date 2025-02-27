/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.scripting.ide_services.compiler.impl

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analyzer.AnalysisResult
import org.jetbrains.kotlin.analyzer.ModuleInfo
import org.jetbrains.kotlin.analyzer.ResolverForProject
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.container.ComponentProvider
import org.jetbrains.kotlin.container.getService
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.idea.FrontendInternals
import org.jetbrains.kotlin.scripting.ide_common.idea.resolve.ResolutionFacade
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.BindingContext

class KotlinResolutionFacadeForRepl(
    private konst environment: KotlinCoreEnvironment,
    private konst provider: ComponentProvider
) :
    ResolutionFacade {
    override konst project: Project
        get() = environment.project

    override konst moduleDescriptor: ModuleDescriptor = provider.getService(ModuleDescriptor::class.java)

    @FrontendInternals
    override fun <T : Any> getFrontendService(serviceClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return provider.resolve(serviceClass)!!.getValue() as T
    }

    override fun <T : Any> getIdeService(serviceClass: Class<T>): T {
        throw UnsupportedOperationException()
    }

    @FrontendInternals
    override fun <T : Any> tryGetFrontendService(element: PsiElement, serviceClass: Class<T>): T? {
        throw UnsupportedOperationException()
    }

    override fun getResolverForProject(): ResolverForProject<out ModuleInfo> {
        throw UnsupportedOperationException()
    }

    @FrontendInternals
    override fun <T : Any> getFrontendService(element: PsiElement, serviceClass: Class<T>): T {
        throw UnsupportedOperationException()
    }

    @FrontendInternals
    override fun <T : Any> getFrontendService(moduleDescriptor: ModuleDescriptor, serviceClass: Class<T>): T {
        throw UnsupportedOperationException()
    }

    override fun analyzeWithAllCompilerChecks(
        elements: Collection<KtElement>,
        callback: DiagnosticSink.DiagnosticsCallback?
    ): AnalysisResult {
        throw UnsupportedOperationException()
    }

}