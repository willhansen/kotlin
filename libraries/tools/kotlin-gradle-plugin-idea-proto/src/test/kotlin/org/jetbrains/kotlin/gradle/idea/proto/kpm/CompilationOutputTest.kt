/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.proto.kpm

import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmCompilationOutput
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmCompilationOutputImpl
import org.jetbrains.kotlin.gradle.idea.proto.AbstractSerializationTest
import org.jetbrains.kotlin.gradle.idea.testFixtures.kpm.TestIdeaKpmInstances
import kotlin.test.Test

class CompilationOutputTest : AbstractSerializationTest<IdeaKpmCompilationOutput>() {

    override fun serialize(konstue: IdeaKpmCompilationOutput): ByteArray = konstue.toByteArray()
    override fun deserialize(data: ByteArray): IdeaKpmCompilationOutput = IdeaKpmCompilationOutput(data)

    @Test
    fun `serialize - deserialize - sample 0`() {
        testSerialization(TestIdeaKpmInstances.simpleCompilationOutput)
    }

    @Test
    fun `serialize - deserialize - sample 1`() = testSerialization(
        IdeaKpmCompilationOutputImpl(
            classesDirs = emptySet(),
            resourcesDir = null
        )
    )
}
