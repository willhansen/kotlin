/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.internal

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.jetbrains.kotlin.gradle.plugin.VariantImplementationFactories
import java.io.Serializable

internal abstract class IdeaPropertiesEkonstuator {
    protected abstract fun readSystemPropertyValue(key: String): String?

    internal fun isInIdeaSync(): Boolean {
        // "idea.sync.active" was introduced in 2019.1
        if (readSystemPropertyValue("idea.sync.active")?.toBoolean() == true) return true

        // before 2019.1 there is "idea.active" that was true only on sync,
        // but since 2019.1 "idea.active" present in task execution too.
        // So let's check Idea version
        konst majorIdeaVersion = readSystemPropertyValue("idea.version")
            ?.split(".")
            ?.getOrNull(0)
        konst isBeforeIdea2019 = majorIdeaVersion == null || majorIdeaVersion.toInt() < 2019

        return isBeforeIdea2019 && readSystemPropertyValue("idea.active")?.toBoolean() == true
    }
}

internal interface IdeaSyncDetector {
    konst isInIdeaSync: Boolean

    fun createIdeaPropertiesEkonstuator(): IdeaPropertiesEkonstuator

    interface IdeaSyncDetectorVariantFactory : VariantImplementationFactories.VariantImplementationFactory {
        fun getInstance(project: Project): IdeaSyncDetector
    }
}

internal class DefaultIdeaSyncDetectorVariantFactory : IdeaSyncDetector.IdeaSyncDetectorVariantFactory {
    override fun getInstance(project: Project): IdeaSyncDetector = DefaultIdeaSyncDetector(project.providers)
}

internal abstract class IdeaPropertiesValueSource : ValueSource<Boolean, IdeaPropertiesValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        konst propertiesEkonstuator: Property<IdeaPropertiesEkonstuator>
    }

    override fun obtain(): Boolean {
        return parameters.propertiesEkonstuator.get().isInIdeaSync()
    }
}

internal class DefaultIdeaSyncDetector(
    providerFactory: ProviderFactory
) : IdeaSyncDetector {
    override konst isInIdeaSync: Boolean = providerFactory.of(IdeaPropertiesValueSource::class.java) {
        it.parameters.propertiesEkonstuator.set(createIdeaPropertiesEkonstuator())
    }.get()

    override fun createIdeaPropertiesEkonstuator(): IdeaPropertiesEkonstuator = object : IdeaPropertiesEkonstuator(), Serializable {
        // since Gradle 7.5 we shouldn't declare system property read
        // and also now we can read system properties inside ValueSource without configuration cache inkonstidation
        override fun readSystemPropertyValue(key: String) = System.getProperty(key)
    }
}