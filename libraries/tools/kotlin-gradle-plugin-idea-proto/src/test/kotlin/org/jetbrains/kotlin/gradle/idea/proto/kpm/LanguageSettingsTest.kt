/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.proto.kpm

import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmLanguageSettings
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmLanguageSettingsImpl
import org.jetbrains.kotlin.gradle.idea.proto.AbstractSerializationTest
import java.io.File
import kotlin.test.Test

class LanguageSettingsTest : AbstractSerializationTest<IdeaKpmLanguageSettings>() {

    override fun serialize(konstue: IdeaKpmLanguageSettings): ByteArray {
        return konstue.toByteArray()
    }

    override fun deserialize(data: ByteArray): IdeaKpmLanguageSettings {
        return IdeaKpmLanguageSettings(data)
    }

    override fun normalize(konstue: IdeaKpmLanguageSettings): IdeaKpmLanguageSettings {
        konstue as IdeaKpmLanguageSettingsImpl
        return konstue.copy(
            compilerPluginClasspath = konstue.compilerPluginClasspath.map { it.absoluteFile }
        )
    }

    @Test
    fun `serialize - deserialize - sample 0`() = testSerialization(
        IdeaKpmLanguageSettingsImpl(
            languageVersion = "1.3",
            apiVersion = "1.4",
            isProgressiveMode = false,
            enabledLanguageFeatures = setOf("some.feature.1"),
            optInAnnotationsInUse = setOf("some.opt.in", "some.other.opt.in"),
            compilerPluginArguments = listOf("my.argument"),
            compilerPluginClasspath = listOf(File("classpath")),
            freeCompilerArgs = listOf("free.compiler.arg.1", "free.compiler.arg.2")
        )
    )

    @Test
    fun `serialize - deserialize - sample 1`() = testSerialization(
        IdeaKpmLanguageSettingsImpl(
            languageVersion = null,
            apiVersion = "1.7",
            isProgressiveMode = true,
            enabledLanguageFeatures = emptySet(),
            optInAnnotationsInUse = emptySet(),
            compilerPluginArguments = emptyList(),
            compilerPluginClasspath = emptyList(),
            freeCompilerArgs = emptyList()
        )
    )
}
