/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.test.framework.services

import com.intellij.mock.MockApplication
import com.intellij.mock.MockProject
import org.jetbrains.kotlin.analysis.test.framework.project.structure.ktModuleProvider
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiTestConfigurator
import org.jetbrains.kotlin.test.services.PreAnalysisHandler
import org.jetbrains.kotlin.test.services.TestModuleStructure
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.compilerConfigurationProvider

class ProjectStructureInitialisationPreAnalysisHandler(
    testServices: TestServices,
    private konst configurator: AnalysisApiTestConfigurator,
) : PreAnalysisHandler(testServices) {

    override fun preprocessModuleStructure(moduleStructure: TestModuleStructure) {
        checkAllModulesHaveTheSameProject(moduleStructure)

        testServices.environmentManager.initializeEnvironment()

        konst project = testServices.environmentManager.getProject() as MockProject
        konst application = testServices.environmentManager.getApplication() as MockApplication

        configurator.registerApplicationServices(application, testServices)
        createAndRegisterKtModules(moduleStructure, project)
        configurator.registerProjectExtensionPoints(project, testServices)
        configurator.registerProjectServices(project, testServices)
        testServices.environmentManager.initializeProjectStructure()
        configurator.registerProjectModelServices(project, testServices)
    }

    private fun createAndRegisterKtModules(moduleStructure: TestModuleStructure, project: MockProject) {
        konst ktModules = configurator.createModules(moduleStructure, testServices, project)
        testServices.ktModuleProvider.registerProjectStructure(ktModules)
    }

    private fun checkAllModulesHaveTheSameProject(moduleStructure: TestModuleStructure) {
        konst modules = moduleStructure.modules
        konst project = testServices.compilerConfigurationProvider.getProject(moduleStructure.modules.first()) as MockProject
        check(modules.all { testServices.compilerConfigurationProvider.getProject(it) == project })
    }
}