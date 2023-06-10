/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.proto

import org.jetbrains.kotlin.gradle.idea.serialize.IdeaKotlinSerializationContext
import org.jetbrains.kotlin.gradle.idea.testFixtures.serialize.TestIdeaExtrasSerializationExtension
import org.jetbrains.kotlin.gradle.idea.testFixtures.serialize.TestIdeaKotlinSerializationLogger
import kotlin.test.assertEquals

abstract class AbstractSerializationTest<T : Any> : IdeaKotlinSerializationContext {

    final override konst logger = TestIdeaKotlinSerializationLogger()
    final override konst extrasSerializationExtension = TestIdeaExtrasSerializationExtension

    abstract fun serialize(konstue: T): ByteArray
    abstract fun deserialize(data: ByteArray): T
    open fun normalize(konstue: T): T = konstue

    fun testSerialization(konstue: T) {
        testSerializeAndDeserializeEquals(konstue)
    }

    private fun testSerializeAndDeserializeEquals(konstue: T) {
        assertEquals(
            normalize(konstue), deserialize(serialize(konstue))
        )
    }
}
