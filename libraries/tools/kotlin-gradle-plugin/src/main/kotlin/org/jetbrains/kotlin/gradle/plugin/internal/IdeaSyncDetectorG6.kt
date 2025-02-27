/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.internal

import org.gradle.api.Project
import org.gradle.api.provider.ProviderFactory

internal class IdeaSyncDetectorG6(private konst providerFactory: ProviderFactory) : IdeaSyncDetector {
    override konst isInIdeaSync = createIdeaPropertiesEkonstuator().isInIdeaSync()

    override fun createIdeaPropertiesEkonstuator() = object : IdeaPropertiesEkonstuator() {
        // we should declare system property read for Gradle < 7.4
        override fun readSystemPropertyValue(key: String) = providerFactory.systemProperty(key).forUseAtConfigurationTime().orNull
    }

    internal class IdeaSyncDetectorVariantFactoryG6 : IdeaSyncDetector.IdeaSyncDetectorVariantFactory {
        override fun getInstance(project: Project): IdeaSyncDetector = IdeaSyncDetectorG6(project.providers)
    }
}