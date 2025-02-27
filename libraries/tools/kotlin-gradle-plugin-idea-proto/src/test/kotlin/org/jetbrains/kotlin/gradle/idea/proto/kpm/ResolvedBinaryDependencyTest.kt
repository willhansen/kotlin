/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.proto.kpm

import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmBinaryCoordinatesImpl
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmResolvedBinaryDependency
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmResolvedBinaryDependencyImpl
import org.jetbrains.kotlin.gradle.idea.proto.AbstractSerializationTest
import org.jetbrains.kotlin.tooling.core.emptyExtras
import org.jetbrains.kotlin.tooling.core.extrasKeyOf
import org.jetbrains.kotlin.tooling.core.extrasOf
import org.jetbrains.kotlin.tooling.core.withValue
import java.io.File
import kotlin.test.Test

class ResolvedBinaryDependencyTest : AbstractSerializationTest<IdeaKpmResolvedBinaryDependency>() {

    override fun serialize(konstue: IdeaKpmResolvedBinaryDependency) = konstue.toByteArray(this)
    override fun deserialize(data: ByteArray) = IdeaKpmResolvedBinaryDependency(data)
    override fun normalize(konstue: IdeaKpmResolvedBinaryDependency) =
        konstue.run { this as IdeaKpmResolvedBinaryDependencyImpl }.copy(binaryFile = konstue.binaryFile.absoluteFile)

    @Test
    fun `serialize - deserialize - sample 0`() = testSerialization(
        IdeaKpmResolvedBinaryDependencyImpl(
            null, binaryType = "binaryType", binaryFile = File("bin"), emptyExtras()
        )
    )

    @Test
    fun `serialize - deserialize - sample 1`() = testSerialization(
        IdeaKpmResolvedBinaryDependencyImpl(
            coordinates = IdeaKpmBinaryCoordinatesImpl(
                group = "group",
                module = "module",
                version = "version",
                kotlinModuleName = null,
                kotlinFragmentName = null
            ),
            binaryType = "binaryType",
            binaryFile = File("bin"),
            extras = extrasOf(extrasKeyOf<Int>() withValue 2411)
        )
    )
}
