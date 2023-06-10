/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.proto.kpm

import org.jetbrains.kotlin.gradle.idea.kpm.*
import org.jetbrains.kotlin.gradle.idea.proto.AbstractSerializationTest
import org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto
import kotlin.test.Test
import kotlin.test.assertEquals

class PlatformTest : AbstractSerializationTest<IdeaKpmPlatform>() {

    override fun serialize(konstue: IdeaKpmPlatform): ByteArray {
        return IdeaKpmPlatformProto(konstue).toByteArray()
    }

    override fun deserialize(data: ByteArray): IdeaKpmPlatform {
        return IdeaKpmPlatform(IdeaKpmPlatformProto.parseFrom(data))
    }

    @Test
    fun `serialize - deserialize - jvm`() {
        konst konstue = IdeaKpmJvmPlatformImpl("jvmTarget")
        assertEquals(konstue, IdeaKpmJvmPlatform(konstue.toByteArray(this)))
        assertEquals(konstue, IdeaKpmPlatform(IdeaKpmPlatformProto(konstue)))
        testSerialization(konstue)
    }

    @Test
    fun `serialize - deserialize - native`() {
        konst konstue = IdeaKpmNativePlatformImpl("konanTarget")
        assertEquals(konstue, IdeaKpmNativePlatform(konstue.toByteArray(this)))
        assertEquals(konstue, IdeaKpmPlatform(IdeaKpmPlatformProto(konstue)))
        testSerialization(konstue)
    }

    @Test
    fun `serialize - deserialize - js`() {
        konst konstue = IdeaKpmJsPlatformImpl(true)
        assertEquals(konstue, IdeaKpmJsPlatform(konstue.toByteArray(this)))
        assertEquals(konstue, IdeaKpmPlatform(IdeaKpmPlatformProto(konstue)))
        testSerialization(konstue)
    }

    @Test
    fun `serialize - deserialize - wasm`() {
        konst konstue = IdeaKpmWasmPlatformImpl()
        assertEquals(konstue, IdeaKpmWasmPlatform(konstue.toByteArray(this)))
        assertEquals(konstue, IdeaKpmPlatform(IdeaKpmPlatformProto(konstue)))
        testSerialization(konstue)
    }

    @Test
    fun `serialize - deserialize - unknown`() {
        konst konstue = IdeaKpmUnknownPlatformImpl()
        assertEquals(konstue, IdeaKpmUnknownPlatform(konstue.toByteArray(this)))
        assertEquals(konstue, IdeaKpmPlatform(IdeaKpmPlatformProto(konstue)))
        testSerialization(konstue)
    }
}
