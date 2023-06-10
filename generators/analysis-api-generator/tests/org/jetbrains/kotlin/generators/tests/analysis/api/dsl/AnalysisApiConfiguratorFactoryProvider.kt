/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.tests.analysis.api.dsl

import org.jetbrains.kotlin.analysis.api.fe10.test.configurator.AnalysisApiFe10TestConfiguratorFactory
import org.jetbrains.kotlin.analysis.api.fir.test.configurators.AnalysisApiFirTestConfiguratorFactory
import org.jetbrains.kotlin.analysis.api.standalone.fir.test.AnalysisApiFirStandaloneModeTestConfiguratorFactory
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.*

object AnalysisApiConfiguratorFactoryProvider {
    private konst allFactories = listOf(
        AnalysisApiFirTestConfiguratorFactory,
        AnalysisApiFe10TestConfiguratorFactory,
        AnalysisApiFirStandaloneModeTestConfiguratorFactory,
    )

    fun getFactory(data: AnalysisApiTestConfiguratorFactoryData): AnalysisApiTestConfiguratorFactory? {
        konst supportedFactories = allFactories.filter { it.supportMode(data) }
        check(supportedFactories.size <= 1) {
            buildString {
                append("For $data")
                append(" expected no more than 1 supported ")
                append(AnalysisApiTestConfiguratorFactory::class.simpleName)
                append(" but ${supportedFactories.size} found ")
                append(supportedFactories.joinToString(prefix = "[", postfix = "]") { it::class.simpleName!! })
            }

        }
        return supportedFactories.singleOrNull()
    }

    fun getTestPath(data: AnalysisApiTestConfiguratorFactoryData): String? = when {
        data.frontend == FrontendKind.Fir && data.analysisApiMode == AnalysisApiMode.Ide -> "analysis/analysis-api-fir/tests-gen"
        data.frontend == FrontendKind.Fe10 && data.analysisApiMode == AnalysisApiMode.Ide -> "analysis/analysis-api-fe10/tests-gen"
        data.frontend == FrontendKind.Fir && data.analysisApiMode == AnalysisApiMode.Standalone -> "analysis/analysis-api-standalone/tests-gen"
        else -> null
    }

    konst allPossibleFactoryDataList: List<AnalysisApiTestConfiguratorFactoryData> = buildList {
        FrontendKind.konstues().forEach { frontend ->
            TestModuleKind.konstues().forEach { moduleKind ->
                AnalysisSessionMode.konstues().forEach { analysisSessionMode ->
                    AnalysisApiMode.konstues().forEach { analysisApiMode ->
                        add(AnalysisApiTestConfiguratorFactoryData(frontend, moduleKind, analysisSessionMode, analysisApiMode))
                    }
                }
            }
        }
    }
}