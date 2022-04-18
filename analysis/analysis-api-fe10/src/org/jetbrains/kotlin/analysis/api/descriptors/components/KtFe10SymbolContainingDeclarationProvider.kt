/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.components

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.ProjectScope
import org.jetbrains.kotlin.analysis.api.components.KtSymbolContainingDeclarationProvider
import org.jetbrains.kotlin.analysis.api.descriptors.KtFe10AnalysisSession
import org.jetbrains.kotlin.analysis.api.descriptors.components.base.Fe10KtAnalysisSessionComponent
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.KtFe10DescSymbol
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.getDescriptor
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.toKtSymbol
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.psiBased.base.KtFe10PsiSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtBackingFieldSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtPackageSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtValueParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtSymbolKind
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtSymbolWithKind
import org.jetbrains.kotlin.analysis.api.tokens.ValidityToken
import org.jetbrains.kotlin.analysis.project.structure.KtLibraryModule
import org.jetbrains.kotlin.analysis.project.structure.KtLibrarySourceModule
import org.jetbrains.kotlin.analysis.project.structure.KtModule
import org.jetbrains.kotlin.analysis.project.structure.getKtModule
import org.jetbrains.kotlin.cfg.getElementParentDeclaration
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.load.kotlin.JvmPackagePartSource
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices
import org.jetbrains.kotlin.resolve.descriptorUtil.platform
import org.jetbrains.kotlin.resolve.jvm.platform.JvmPlatformAnalyzerServices
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DescriptorWithContainerSource
import java.nio.file.Path
import java.nio.file.Paths

internal class KtFe10SymbolContainingDeclarationProvider(
    override val analysisSession: KtFe10AnalysisSession
) : KtSymbolContainingDeclarationProvider(), Fe10KtAnalysisSessionComponent {
    override val token: ValidityToken
        get() = analysisSession.token

    override fun getContainingDeclaration(symbol: KtSymbol): KtSymbolWithKind? {
        if (symbol is KtSymbolWithKind && symbol.symbolKind == KtSymbolKind.TOP_LEVEL) {
            return null
        }

        return when (symbol) {
            is KtPackageSymbol -> null
            is KtBackingFieldSymbol -> symbol.owningProperty
            is KtFe10DescSymbol<*> -> symbol.descriptor.containingDeclaration?.toKtSymbol(analysisContext) as? KtSymbolWithKind
            is KtFe10PsiSymbol<*, *> -> {
                val parentDeclaration = symbol.psi.getElementParentDeclaration()
                if (parentDeclaration != null) {
                    return with(analysisSession) {
                        parentDeclaration.getSymbol() as? KtSymbolWithKind
                    }
                }

                return null
            }

            else -> null
        }
    }

    // TODO this is a dummy and incorrect implementation just to satisfy some tests
    override fun getContainingModule(symbol: KtSymbol): KtModule {
        // Implicit lambda parameter doesn't have a source PSI.
        if ((symbol as? KtValueParameterSymbol)?.isImplicitLambdaParameter == true) {
            // Retrieve the module from its containing lambda instead.
            getContainingDeclaration(symbol)?.let { parentLambdaSymbol ->
                return getContainingModule(parentLambdaSymbol)
            }
        }
        return symbol.psi?.getKtModule(analysisSession.analysisContext.resolveSession.project)
            ?: symbol.getDescriptor()?.getFakeContainingKtModule()
            ?: TODO(symbol.toString())
    }

    private fun DeclarationDescriptor.getFakeContainingKtModule(): KtModule {
        return when (this) {
            is DescriptorWithContainerSource -> {
                val libraryPath = Paths.get((containerSource as JvmPackagePartSource).knownJvmBinaryClass?.containingLibrary!!)
                object : KtLibraryModule {
                    override val libraryName: String = libraryPath.fileName.toString().substringBeforeLast(".")
                    override val librarySources: KtLibrarySourceModule? = null
                    override fun getBinaryRoots(): Collection<Path> = listOf(libraryPath)
                    override val directRegularDependencies: List<KtModule> = emptyList()
                    override val directRefinementDependencies: List<KtModule> = emptyList()
                    override val directFriendDependencies: List<KtModule> = emptyList()
                    override val contentScope: GlobalSearchScope = ProjectScope.getLibrariesScope(project)
                    override val platform: TargetPlatform
                        get() = this@getFakeContainingKtModule.platform!!
                    override val analyzerServices: PlatformDependentAnalyzerServices
                        get() = JvmPlatformAnalyzerServices
                    override val project: Project
                        get() = analysisSession.analysisContext.resolveSession.project

                }
            }

            else -> TODO(this.toString())
        }
    }
}
