/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.project.structure

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analysis.api.resolve.extensions.KtResolveExtensionProvider
import org.jetbrains.kotlin.analysis.low.level.api.fir.IdeSessionComponents
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.services.createSealedInheritorsProvider
import org.jetbrains.kotlin.analysis.low.level.api.fir.fir.caches.FirThreadSafeCachesFactory
import org.jetbrains.kotlin.analysis.low.level.api.fir.providers.LLFirIdePredicateBasedProvider
import org.jetbrains.kotlin.analysis.low.level.api.fir.providers.LLFirIdeRegisteredPluginAnnotations
import org.jetbrains.kotlin.analysis.low.level.api.fir.resolve.extensions.LLFirNonEmptyResolveExtensionTool
import org.jetbrains.kotlin.analysis.low.level.api.fir.resolve.extensions.LLFirResolveExtensionTool
import org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.LLFirSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.LLFirSourcesSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.util.LLFirExceptionHandler
import org.jetbrains.kotlin.analysis.project.structure.KtCompilerPluginsProvider
import org.jetbrains.kotlin.analysis.project.structure.KtSourceModule
import org.jetbrains.kotlin.analysis.project.structure.moduleScopeProvider
import org.jetbrains.kotlin.analysis.providers.createAnnotationResolver
import org.jetbrains.kotlin.analysis.providers.createDeclarationProvider
import org.jetbrains.kotlin.fir.FirExceptionHandler
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.SessionConfiguration
import org.jetbrains.kotlin.fir.caches.FirCachesFactory
import org.jetbrains.kotlin.fir.declarations.SealedClassInheritorsProvider
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrar
import org.jetbrains.kotlin.fir.extensions.FirExtensionRegistrarAdapter
import org.jetbrains.kotlin.fir.extensions.FirPredicateBasedProvider
import org.jetbrains.kotlin.fir.extensions.FirRegisteredPluginAnnotations
import org.jetbrains.kotlin.fir.java.FirJavaFacadeForSource
import org.jetbrains.kotlin.fir.java.JavaSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.impl.FirCompositeSymbolProvider
import org.jetbrains.kotlin.fir.session.FirSessionConfigurator
import org.jetbrains.kotlin.load.java.createJavaClassFinder

@SessionConfiguration
internal fun LLFirSession.registerIdeComponents(project: Project) {
    register(IdeSessionComponents::class, IdeSessionComponents.create())
    register(FirCachesFactory::class, FirThreadSafeCachesFactory)
    register(SealedClassInheritorsProvider::class, project.createSealedInheritorsProvider())
    register(FirExceptionHandler::class, LLFirExceptionHandler)
    createResolveExtensionTool()?.let {
        register(LLFirResolveExtensionTool::class, it)
    }
}

private fun LLFirSession.createResolveExtensionTool(): LLFirResolveExtensionTool? {
    konst extensions = KtResolveExtensionProvider.provideExtensionsFor(ktModule)
    if (extensions.isEmpty()) return null
    return LLFirNonEmptyResolveExtensionTool(this, extensions)
}


internal inline fun createCompositeSymbolProvider(
    session: FirSession,
    createSubProviders: MutableList<FirSymbolProvider>.() -> Unit
): FirCompositeSymbolProvider =
    FirCompositeSymbolProvider(session, buildList(createSubProviders))

@SessionConfiguration
internal fun FirSession.registerCompilerPluginExtensions(project: Project, module: KtSourceModule) {
    konst extensionProvider = project.getService<KtCompilerPluginsProvider>(KtCompilerPluginsProvider::class.java) ?: return
    FirSessionConfigurator(this).apply {
        konst registrars = FirExtensionRegistrarAdapter.getInstances(project) +
                extensionProvider.getRegisteredExtensions(module, FirExtensionRegistrarAdapter)
        for (extensionRegistrar in registrars) {
            registerExtensions((extensionRegistrar as FirExtensionRegistrar).configure())
        }
    }.configure()
}

@SessionConfiguration
internal fun LLFirSourcesSession.registerCompilerPluginServices(
    contentScope: GlobalSearchScope,
    project: Project,
    module: KtSourceModule
) {
    konst projectWithDependenciesScope = contentScope.uniteWith(project.moduleScopeProvider.getModuleLibrariesScope(module))
    konst annotationsResolver = project.createAnnotationResolver(projectWithDependenciesScope)

    // We need FirRegisteredPluginAnnotations and FirPredicateBasedProvider during extensions' registration process
    register(FirRegisteredPluginAnnotations::class, LLFirIdeRegisteredPluginAnnotations(this, annotationsResolver))
    register(
        FirPredicateBasedProvider::class,
        LLFirIdePredicateBasedProvider(this, annotationsResolver, project.createDeclarationProvider(projectWithDependenciesScope, module))
    )

}
