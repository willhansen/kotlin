/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.proto.kpm

import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmProject
import org.jetbrains.kotlin.gradle.idea.proto.AbstractSerializationTest
import org.jetbrains.kotlin.gradle.idea.testFixtures.kpm.TestIdeaKpmInstances
import kotlin.test.Test

class ProjectTest : AbstractSerializationTest<IdeaKpmProject>() {

    override fun serialize(konstue: IdeaKpmProject): ByteArray {
        return konstue.toByteArray(this)
    }

    override fun deserialize(data: ByteArray): IdeaKpmProject {
        return IdeaKpmProject(data) ?: error("Failed to deserialize kpm project: ${logger.reports}")
    }

    @Test
    fun `serialize - deserialize - sample 0`() = testSerialization(TestIdeaKpmInstances.simpleProject)
}
