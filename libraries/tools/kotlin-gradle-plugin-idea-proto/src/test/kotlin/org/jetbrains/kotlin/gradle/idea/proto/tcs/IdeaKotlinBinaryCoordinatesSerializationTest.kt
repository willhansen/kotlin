/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.proto.tcs

import org.jetbrains.kotlin.gradle.idea.proto.AbstractSerializationTest
import org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinBinaryCoordinatesProto
import org.jetbrains.kotlin.gradle.idea.tcs.IdeaKotlinBinaryCoordinates
import org.junit.Test

class IdeaKotlinBinaryCoordinatesSerializationTest : AbstractSerializationTest<IdeaKotlinBinaryCoordinates>() {
    override fun serialize(konstue: IdeaKotlinBinaryCoordinates): ByteArray = IdeaKotlinBinaryCoordinatesProto(konstue).toByteArray()

    override fun deserialize(data: ByteArray): IdeaKotlinBinaryCoordinates =
        IdeaKotlinBinaryCoordinates(IdeaKotlinBinaryCoordinatesProto.parseFrom(data))

    @Test
    fun `sample 0`() = testSerialization(
        IdeaKotlinBinaryCoordinates(
            group = "myGroup",
            module = "myModule",
            version = "myVersion",
            sourceSetName = "mySourceSetName"
        )
    )

    @Test
    fun `sample 1`() = testSerialization(
        IdeaKotlinBinaryCoordinates(
            group = "myGroup",
            module = "myModule",
            version = null,
            sourceSetName = null
        )
    )

    @Test
    fun `sample 2`() = testSerialization(
        IdeaKotlinBinaryCoordinates(
            group = "myGroup",
            module = "myModule",
            version = "myVersion",
            sourceSetName = null
        )
    )
}
