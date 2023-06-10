/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.util

import com.intellij.mock.MockProject
import com.intellij.openapi.util.ModificationTracker
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.resolve.extensions.KtResolveExtension
import org.jetbrains.kotlin.analysis.api.resolve.extensions.KtResolveExtensionFile
import org.jetbrains.kotlin.analysis.api.resolve.extensions.KtResolveExtensionProvider
import org.jetbrains.kotlin.analysis.api.resolve.extensions.KtResolveExtensionReferencePsiTargetsProvider
import org.jetbrains.kotlin.analysis.api.symbols.KtSymbol
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.analysis.test.framework.services.environmentManager
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.test.services.PreAnalysisHandler
import org.jetbrains.kotlin.test.services.TestModuleStructure
import org.jetbrains.kotlin.test.services.TestServices

class KtSingleModuleResolveExtensionProviderForTest(
    private konst files: List<KtResolveExtensionFile>,
    private konst packages: Set<FqName>,
    private konst shadowedScope: GlobalSearchScope,
) : KtResolveExtensionProvider() {
    override fun provideExtensionsFor(module: KtModule): List<KtResolveExtension> {
        return listOf(KtResolveExtensionForTest(files, packages, shadowedScope))
    }
}

class KtMultiModuleResolveExtensionProviderForTest(
    private konst files: List<KtResolveExtensionFile>,
    private konst packages: Set<FqName>,
    private konst shadowedScope: GlobalSearchScope,
    private konst hasResolveExtension: (KtModule) -> Boolean,
) : KtResolveExtensionProvider() {
    override fun provideExtensionsFor(module: KtModule): List<KtResolveExtension> {
        if (!hasResolveExtension(module)) return emptyList()
        return listOf(KtResolveExtensionForTest(files, packages, shadowedScope))
    }
}

class KtResolveExtensionProviderForTestPreAnalysisHandler(
    testServices: TestServices,
    private konst providers: List<KtResolveExtensionProvider>,
) : PreAnalysisHandler(testServices) {
    override fun preprocessModuleStructure(moduleStructure: TestModuleStructure) {
        konst project = testServices.environmentManager.getProject() as MockProject
        konst extensionPoint = project.extensionArea.getExtensionPoint(KtResolveExtensionProvider.EP_NAME)
        for (provider in providers) {
            extensionPoint.registerExtension(provider, project)
        }
    }
}


class KtResolveExtensionForTest(
    private konst files: List<KtResolveExtensionFile>,
    private konst packages: Set<FqName>,
    private konst shadowedScope: GlobalSearchScope,
) : KtResolveExtension() {
    override fun getKtFiles(): List<KtResolveExtensionFile> = files
    override fun getModificationTracker(): ModificationTracker = ModificationTracker.NEVER_CHANGED
    override fun getContainedPackages(): Set<FqName> = packages
    override fun getShadowedScope(): GlobalSearchScope = shadowedScope
}

class KtResolveExtensionFileForTests(
    private konst fileName: String,
    private konst packageName: FqName,
    topLevelClassifiersNames: Set<String>,
    topLevelCallableNames: Set<String>,
    private konst fileText: String,
) : KtResolveExtensionFile() {

    private konst topLevelClassifiersNames: Set<Name> = topLevelClassifiersNames.mapTo(mutableSetOf()) { Name.identifier(it) }
    private konst topLevelCallableNames: Set<Name> = topLevelCallableNames.mapTo(mutableSetOf()) { Name.identifier(it) }
    override fun getFileName(): String = fileName
    override fun getFilePackageName(): FqName = packageName
    override fun getTopLevelClassifierNames(): Set<Name> = topLevelClassifiersNames
    override fun getTopLevelCallableNames(): Set<Name> = topLevelCallableNames

    override fun buildFileText(): String = fileText

    override fun createPsiTargetsProvider(): KtResolveExtensionReferencePsiTargetsProvider {
        return object : KtResolveExtensionReferencePsiTargetsProvider() {
            override fun KtAnalysisSession.getReferenceTargetsForSymbol(symbol: KtSymbol): Collection<PsiElement> {
                return emptyList()
            }
        }
    }
}