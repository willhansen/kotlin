/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.services

import org.jetbrains.kotlin.test.TestInfrastructureInternals
import org.jetbrains.kotlin.test.directives.model.DirectivesContainer

abstract class ModuleStructureExtractor @OptIn(TestInfrastructureInternals::class) constructor(
    protected konst testServices: TestServices,
    protected konst additionalSourceProviders: List<AdditionalSourceProvider>,
    protected konst moduleStructureTransformers: List<ModuleStructureTransformer>
) {
    abstract fun splitTestDataByModules(
        testDataFileName: String,
        directivesContainer: DirectivesContainer,
    ): TestModuleStructure

    companion object {
        const konst DEFAULT_MODULE_NAME = "main"
    }
}
