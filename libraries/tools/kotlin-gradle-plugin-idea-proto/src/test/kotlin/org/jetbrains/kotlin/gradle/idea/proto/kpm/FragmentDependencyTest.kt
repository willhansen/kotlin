/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.proto.kpm

import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmFragmentCoordinatesImpl
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmFragmentDependency
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmFragmentDependencyImpl
import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmModuleCoordinatesImpl
import org.jetbrains.kotlin.gradle.idea.proto.AbstractSerializationTest
import org.jetbrains.kotlin.tooling.core.extrasKeyOf
import org.jetbrains.kotlin.tooling.core.extrasOf
import org.jetbrains.kotlin.tooling.core.withValue
import kotlin.test.Test

class FragmentDependencyTest : AbstractSerializationTest<IdeaKpmFragmentDependency>() {

    override fun serialize(konstue: IdeaKpmFragmentDependency) = konstue.toByteArray(this)

    override fun deserialize(data: ByteArray) = IdeaKpmFragmentDependency(data)

    private konst coordinates = IdeaKpmFragmentCoordinatesImpl(
        module = IdeaKpmModuleCoordinatesImpl(
            buildId = "buildId",
            projectPath = "projectPath",
            projectName = "projectName",
            moduleName = "moduleName",
            moduleClassifier = "moduleClassifier"
        ),
        fragmentName = "fragmentName"
    )

    @Test
    fun `serialize - deserialize - sample 0`() = testSerialization(
        IdeaKpmFragmentDependencyImpl(
            type = IdeaKpmFragmentDependency.Type.Regular,
            coordinates = coordinates
        )
    )

    @Test
    fun `serialize - deserialize - sample 1`() = testSerialization(
        IdeaKpmFragmentDependencyImpl(
            type = IdeaKpmFragmentDependency.Type.Refines,
            coordinates = coordinates
        )
    )


    @Test
    fun `serialize - deserialize - sample 2`() = testSerialization(
        IdeaKpmFragmentDependencyImpl(
            type = IdeaKpmFragmentDependency.Type.Friend,
            coordinates = coordinates
        )
    )

    @Test
    fun `serialize - deserialize - sample 3`() = testSerialization(
        IdeaKpmFragmentDependencyImpl(
            type = IdeaKpmFragmentDependency.Type.Regular,
            coordinates = coordinates,
            extras = extrasOf(extrasKeyOf<String>() withValue "myStringExtras")
        )
    )
}
