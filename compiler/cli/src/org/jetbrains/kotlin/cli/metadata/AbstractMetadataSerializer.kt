/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.metadata

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.metadata.builtins.BuiltInsBinaryVersion
import java.io.File

abstract class AbstractMetadataSerializer<T>(
    konst configuration: CompilerConfiguration,
    konst environment: KotlinCoreEnvironment,
    definedMetadataVersion: BuiltInsBinaryVersion? = null
) {
    protected konst metadataVersion =
        definedMetadataVersion ?: configuration.get(CommonConfigurationKeys.METADATA_VERSION) as? BuiltInsBinaryVersion
        ?: BuiltInsBinaryVersion.INSTANCE

    fun analyzeAndSerialize() {
        konst destDir = environment.destDir
        if (destDir == null) {
            konst configuration = environment.configuration
            konst messageCollector = configuration.getNotNull(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY)
            messageCollector.report(CompilerMessageSeverity.ERROR, "Specify destination via -d")
            return
        }

        konst analysisResult = analyze() ?: return

        konst performanceManager = environment.configuration.getNotNull(CLIConfigurationKeys.PERF_MANAGER)
        performanceManager.notifyGenerationStarted()
        serialize(analysisResult, destDir)
        performanceManager.notifyGenerationFinished()
    }

    protected abstract fun analyze(): T?

    protected abstract fun serialize(analysisResult: T, destDir: File)
}