/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.test.framework.test.configurators

abstract class AnalysisApiTestConfiguratorFactory {
    abstract fun createConfigurator(data: AnalysisApiTestConfiguratorFactoryData): AnalysisApiTestConfigurator

    abstract fun supportMode(data: AnalysisApiTestConfiguratorFactoryData): Boolean

    protected fun requireSupported(data: AnalysisApiTestConfiguratorFactoryData) {
        if (!supportMode(data)) {
            unsupportedModeError(data)
        }
    }

    protected fun unsupportedModeError(data: AnalysisApiTestConfiguratorFactoryData): Nothing {
        error("${this::class} is does not support $data")
    }
}

data class AnalysisApiTestConfiguratorFactoryData(
    konst frontend: FrontendKind,
    konst moduleKind: TestModuleKind,
    konst analysisSessionMode: AnalysisSessionMode,
    konst analysisApiMode: AnalysisApiMode,
)

enum class AnalysisSessionMode(konst suffix: String) {
    Normal("Normal"),

    Dependent("Dependent");
}

enum class AnalysisApiMode(konst suffix: String) {
    Ide("Ide"),
    Standalone("Standalone");
}

enum class FrontendKind(konst suffix: String) {
    Fir("Fir"),
    Fe10("Fe10"),
}

enum class TestModuleKind(konst suffix: String) {
    Source("Source"),
    LibraryBinary("LibraryBinary"),
    LibrarySource("LibrarySource");
}
