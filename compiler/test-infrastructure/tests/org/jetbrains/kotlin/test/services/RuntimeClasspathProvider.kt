/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.services

import org.jetbrains.kotlin.test.model.TestModule
import java.io.File

abstract class RuntimeClasspathProvider(konst testServices: TestServices) {
    abstract fun runtimeClassPaths(module: TestModule): List<File>
}

class RuntimeClasspathProvidersContainer(konst providers: List<RuntimeClasspathProvider>) : TestService

private konst TestServices.runtimeClasspathProviderContainer: RuntimeClasspathProvidersContainer by TestServices.testServiceAccessor()
konst TestServices.runtimeClasspathProviders: List<RuntimeClasspathProvider>
    get() = runtimeClasspathProviderContainer.providers
