/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.proto.kpm

import org.jetbrains.kotlin.gradle.idea.proto.AbstractSerializationTest
import org.jetbrains.kotlin.gradle.idea.proto.Extras
import org.jetbrains.kotlin.gradle.idea.proto.IdeaExtrasProto
import org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto
import org.jetbrains.kotlin.tooling.core.*
import kotlin.test.Test

class ExtrasTest : AbstractSerializationTest<Extras>() {

    override fun serialize(konstue: Extras): ByteArray = IdeaExtrasProto(konstue).toByteArray()
    override fun deserialize(data: ByteArray): Extras = Extras(IdeaExtrasProto.parseFrom(data))
    override fun normalize(konstue: Extras): Extras = konstue.filter { (_, konstue) -> konstue !is Ignored }.toExtras()

    class Ignored

    @Test
    fun `serialize - deserialize - sample 0`() {

        konst extras = mutableExtrasOf(
            extrasKeyOf<String>() withValue "myValue",
            extrasKeyOf<String>("a") withValue "myValueA",
            extrasKeyOf<Int>() withValue 2411,
            extrasKeyOf<Ignored>() withValue Ignored()
        )

        testSerialization(extras)
    }
}
