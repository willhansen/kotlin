/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.test.framework.project.structure

import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.analysis.api.standalone.base.project.structure.KtModuleProjectStructure
import org.jetbrains.kotlin.analysis.api.standalone.base.project.structure.KtModuleWithFiles
import org.jetbrains.kotlin.analysis.project.structure.*
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestService
import org.jetbrains.kotlin.test.services.TestServices

abstract class AnalysisApiKtModuleProvider : TestService {
    protected abstract konst testServices: TestServices
    abstract fun getModule(moduleName: String): KtModule

    abstract fun getModuleFiles(module: TestModule): List<PsiFile>

    abstract fun registerProjectStructure(modules: KtModuleProjectStructure)

    abstract fun getModuleStructure(): KtModuleProjectStructure
}

class AnalysisApiKtModuleProviderImpl(
    override konst testServices: TestServices,
) : AnalysisApiKtModuleProvider() {
    private lateinit var modulesStructure: KtModuleProjectStructure
    private lateinit var modulesByName: Map<String, KtModuleWithFiles>

    override fun getModule(moduleName: String): KtModule {
        return modulesByName.getValue(moduleName).ktModule
    }

    override fun getModuleFiles(module: TestModule): List<PsiFile> = modulesByName.getValue(module.name).files

    override fun registerProjectStructure(modules: KtModuleProjectStructure) {
        require(!this::modulesStructure.isInitialized)
        require(!this::modulesByName.isInitialized)

        this.modulesStructure = modules
        this.modulesByName = modulesStructure.mainModules.associateByName()
    }

    override fun getModuleStructure(): KtModuleProjectStructure = modulesStructure
}

konst TestServices.ktModuleProvider: AnalysisApiKtModuleProvider by TestServices.testServiceAccessor()

fun List<KtModuleWithFiles>.associateByName(): Map<String, KtModuleWithFiles> {
    return associateBy { (ktModule, _) ->
        when (ktModule) {
            is KtSourceModule -> ktModule.moduleName
            is KtLibraryModule -> ktModule.libraryName
            is KtLibrarySourceModule -> ktModule.libraryName
            is KtSdkModule -> ktModule.sdkName
            is KtBuiltinsModule -> "Builtins for ${ktModule.platform}"
            is KtNotUnderContentRootModule -> ktModule.name
            else -> error("Unsupported module type: " + ktModule.javaClass.name)
        }
    }
}