/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.proto.kpm

import org.jetbrains.kotlin.gradle.idea.kpm.*
import org.jetbrains.kotlin.gradle.idea.proto.AbstractSerializationTest
import org.jetbrains.kotlin.gradle.idea.testFixtures.kpm.TestIdeaKpmInstances
import kotlin.test.Test
import kotlin.test.assertEquals

class FragmentTest : AbstractSerializationTest<IdeaKpmFragment>() {
    override fun serialize(konstue: IdeaKpmFragment) = konstue.toByteArray(this)
    override fun deserialize(data: ByteArray) = IdeaKpmFragment(data)

    @Test
    fun `serialize - deserialize - sample 0`() {
        testDeserializedEquals(TestIdeaKpmInstances.simpleFragment)
    }

    @Test
    fun `serialize - deserialize - sample 1`() {
        testDeserializedEquals(TestIdeaKpmInstances.fragmentWithExtras)
    }

    private fun testDeserializedEquals(konstue: IdeaKpmFragmentImpl) {
        konst deserialized = IdeaKpmFragment(konstue.toByteArray(this))
        konst normalized = konstue.copy(
            dependencies = konstue.dependencies.map {
                if (it !is IdeaKpmResolvedBinaryDependency) return@map it
                IdeaKpmResolvedBinaryDependencyImpl(
                    coordinates = it.coordinates,
                    binaryType = it.binaryType,
                    binaryFile = it.binaryFile.absoluteFile,
                    extras = it.extras
                )
            },
            languageSettings = (konstue.languageSettings as IdeaKpmLanguageSettingsImpl).copy(
                compilerPluginClasspath = konstue.languageSettings.compilerPluginClasspath.map { it.absoluteFile }
            ),
            contentRoots = konstue.contentRoots
                .map { it as IdeaKpmContentRootImpl }
                .map { it.copy(file = it.file.absoluteFile) }
        )

        assertEquals(normalized, deserialized)
    }
}
