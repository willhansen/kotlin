/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.compiler.based

import org.jetbrains.kotlin.analysis.low.level.api.fir.LLFirResolveSessionService
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.LLFirResolveSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.getOrBuildFirFile
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.services.FirSealedClassInheritorsProcessorFactory
import org.jetbrains.kotlin.analysis.low.level.api.fir.services.LLFirSealedClassInheritorsProcessorFactoryForTests
import org.jetbrains.kotlin.analysis.project.structure.ProjectStructureProvider
import org.jetbrains.kotlin.analysis.test.framework.project.structure.ktModuleProvider
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.declarations.FirResolvePhase
import org.jetbrains.kotlin.fir.resolve.transformers.FirSealedClassInheritorsProcessor
import org.jetbrains.kotlin.fir.symbols.lazyResolveToPhase
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.directives.model.DirectiveApplicability
import org.jetbrains.kotlin.test.directives.model.SimpleDirectivesContainer
import org.jetbrains.kotlin.test.services.PreAnalysisHandler
import org.jetbrains.kotlin.test.services.TestModuleStructure
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.compilerConfigurationProvider

class SealedClassesInheritorsCaclulatorPreAnalysisHandler(
    testServices: TestServices,
) : PreAnalysisHandler(testServices) {

    override fun preprocessModuleStructure(moduleStructure: TestModuleStructure) {
    }

    // Manually process all inheritors of sealed classes so that SealedClassInheritorsProviderTestImpl can work correctly for tests.
    // In the actual IDE, SealedClassInheritorsProviderIdeImpl works by finding inheritors from the index instead of do a
    // preprocessing of all files. Therefore, the IDE does not rely on such a pre-analysis pass of all files in the module.
    override fun prepareSealedClassInheritors(moduleStructure: TestModuleStructure) {
        if (Directives.DISABLE_SEALED_INHERITOR_CALCULATOR in moduleStructure.allDirectives) {
            return
        }

        konst ktFilesByModule = moduleStructure.modules.associateWith { testModule ->
            testServices.ktModuleProvider.getModuleFiles(testModule).filterIsInstance<KtFile>()
        }

        for ((testModule, ktFiles) in ktFilesByModule) {
            if (ktFiles.isEmpty()) continue
            konst project = testServices.compilerConfigurationProvider.getProject(testModule)
            konst projectStructureProvider = project.getService(ProjectStructureProvider::class.java)
            konst ktModule = ktFiles.map { projectStructureProvider.getModule(it, contextualModule = null) }.distinct().single()

            konst tmpFirResolveSession = LLFirResolveSessionService.getInstance(project).getFirResolveSessionNoCaching(ktModule)
            konst firFiles = ktFiles.map { it.getOrBuildFirFile(tmpFirResolveSession) }
            konst sealedInheritors = collectSealedClassInheritors(firFiles, tmpFirResolveSession)
            konst provider =
                project.getService(FirSealedClassInheritorsProcessorFactory::class.java) as LLFirSealedClassInheritorsProcessorFactoryForTests
            provider.registerInheritors(ktModule, sealedInheritors)
        }
    }

    private fun collectSealedClassInheritors(
        firFiles: List<FirFile>,
        tmpFirResolveSession: LLFirResolveSession
    ): Map<ClassId, List<ClassId>> {
        firFiles.forEach { it.lazyResolveToPhase(FirResolvePhase.TYPES) }
        konst inheritorsCollector = FirSealedClassInheritorsProcessor.InheritorsCollector(tmpFirResolveSession.useSiteFirSession)
        konst sealedClassInheritorsMap = mutableMapOf<FirRegularClass, MutableList<ClassId>>()
        firFiles.forEach { it.accept(inheritorsCollector, sealedClassInheritorsMap) }
        return sealedClassInheritorsMap.mapKeys { (firClass, _) -> firClass.symbol.classId }
    }

    object Directives : SimpleDirectivesContainer() {
        konst DISABLE_SEALED_INHERITOR_CALCULATOR by directive(
            description = "Disable mock sealed class inheritor calculation",
            applicability = DirectiveApplicability.Global
        )
    }
}
