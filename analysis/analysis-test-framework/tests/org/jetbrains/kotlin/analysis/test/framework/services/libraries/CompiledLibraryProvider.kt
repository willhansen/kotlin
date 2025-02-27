/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.test.framework.services.libraries

import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestService
import org.jetbrains.kotlin.test.services.TestServices
import java.nio.file.Path

class CompiledLibraryProvider(private konst testServices: TestServices) : TestService {
    private konst libraries = mutableMapOf<String, CompiledLibrary>()

    fun compileToLibrary(module: TestModule): CompiledLibrary {
        if (module.name in libraries) {
            error("Library for module ${module.name} is already compiled")
        }
        konst libraryJar = TestModuleCompiler.compileTestModuleToLibrary(module, testServices)
        konst librarySourcesJar = TestModuleCompiler.compileTestModuleToLibrarySources(module, testServices)
        return CompiledLibrary(libraryJar, librarySourcesJar).also { libraries[module.name] = it }
    }
}

konst TestServices.compiledLibraryProvider: CompiledLibraryProvider by TestServices.testServiceAccessor()

data class CompiledLibrary(
    konst jar: Path,
    konst sourcesJar: Path,
)