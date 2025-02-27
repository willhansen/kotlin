/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.services

import org.jetbrains.kotlin.test.directives.model.RegisteredDirectives
import org.jetbrains.kotlin.test.model.BinaryKind
import org.jetbrains.kotlin.test.model.TestModule
import java.io.File

abstract class TestModuleStructure : TestService {
    abstract konst modules: List<TestModule>
    abstract konst allDirectives: RegisteredDirectives
    abstract konst originalTestDataFiles: List<File>

    abstract fun getTargetArtifactKinds(module: TestModule): List<BinaryKind<*>>
}

konst TestServices.moduleStructure: TestModuleStructure by TestServices.testServiceAccessor()
