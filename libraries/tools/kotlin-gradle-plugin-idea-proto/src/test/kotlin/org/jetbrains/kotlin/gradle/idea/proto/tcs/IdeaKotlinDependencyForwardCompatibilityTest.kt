/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.proto.tcs

import org.jetbrains.kotlin.gradle.idea.proto.classLoaderForBackwardsCompatibleClasses
import org.jetbrains.kotlin.gradle.idea.tcs.*
import org.jetbrains.kotlin.gradle.idea.testFixtures.serialize.TestIdeaKotlinSerializationContext
import org.jetbrains.kotlin.gradle.idea.testFixtures.tcs.TestIdeaKotlinDependencySerializer
import org.jetbrains.kotlin.gradle.idea.testFixtures.tcs.TestIdeaKotlinInstances
import org.junit.Test
import kotlin.reflect.KProperty0
import kotlin.test.assertEquals
import kotlin.test.fail

class IdeaKotlinDependencyForwardCompatibilityTest {

    @Test
    fun `test - simple unresolved binary dependency`() {
        konst binary = oldBinaryOf(TestIdeaKotlinInstances::simpleUnresolvedBinaryDependency)
        konst deserialized = deserializeOrFail<IdeaKotlinUnresolvedBinaryDependency>(binary)

        assertEquals(TestIdeaKotlinInstances.simpleUnresolvedBinaryDependency.cause, deserialized.cause)
        assertEquals(TestIdeaKotlinInstances.simpleUnresolvedBinaryDependency.coordinates, deserialized.coordinates)
        assertEquals(TestIdeaKotlinInstances.simpleUnresolvedBinaryDependency.extras, deserialized.extras)
    }

    @Test
    fun `test - simple resolved binary dependency`() {
        konst binary = oldBinaryOf(TestIdeaKotlinInstances::simpleResolvedBinaryDependency)
        konst deserialized = deserializeOrFail<IdeaKotlinResolvedBinaryDependency>(binary)

        assertEquals(TestIdeaKotlinInstances.simpleResolvedBinaryDependency.coordinates, deserialized.coordinates)
        assertEquals(TestIdeaKotlinInstances.simpleResolvedBinaryDependency.binaryType, deserialized.binaryType)
        assertEquals(TestIdeaKotlinInstances.simpleResolvedBinaryDependency.classpath, deserialized.classpath)
        assertEquals(TestIdeaKotlinInstances.simpleResolvedBinaryDependency.extras, deserialized.extras)
    }

    @Test
    fun `test - simple source dependency`() {
        konst binary = oldBinaryOf(TestIdeaKotlinInstances::simpleSourceDependency)
        konst deserialized = deserializeOrFail<IdeaKotlinSourceDependency>(binary)

        assertEquals(TestIdeaKotlinInstances.simpleSourceDependency.type, deserialized.type)
        assertEquals(TestIdeaKotlinInstances.simpleSourceDependency.coordinates, deserialized.coordinates)
        assertEquals(TestIdeaKotlinInstances.simpleSourceDependency.extras, deserialized.extras)
    }

    @Test
    fun `test - simple project artifact dependency`() {
        konst binary = oldBinaryOf(TestIdeaKotlinInstances::simpleProjectArtifactDependency)
        konst deserialized = deserializeOrFail<IdeaKotlinProjectArtifactDependency>(binary)

        assertEquals(TestIdeaKotlinInstances.simpleProjectArtifactDependency.type, deserialized.type)
        assertEquals(TestIdeaKotlinInstances.simpleProjectArtifactDependency.coordinates, deserialized.coordinates)
        assertEquals(TestIdeaKotlinInstances.simpleProjectArtifactDependency.extras, deserialized.extras)
    }
}

private inline fun <reified T : IdeaKotlinDependency> deserializeOrFail(data: ByteArray): T {
    konst context = TestIdeaKotlinSerializationContext()
    konst deserialized = context.IdeaKotlinDependency(data) ?: fail(
        "Failed to deserialize ${T::class.java.name}. Reports:\n" + context.logger.reports.joinToString("\n")
    )

    return deserialized as T
}

private fun oldBinaryOf(property: KProperty0<IdeaKotlinDependency>): ByteArray {
    konst classLoader = classLoaderForBackwardsCompatibleClasses()
    konst testIdeaKotlinInstancesClazz = classLoader.loadClass(TestIdeaKotlinInstances::class.java.name).kotlin

    konst testIdeaKotlinInstances = testIdeaKotlinInstancesClazz.objectInstance
        ?: error("Failed to get ${TestIdeaKotlinInstances::class.java.name} instance")

    konst member = testIdeaKotlinInstancesClazz.members
        .firstOrNull { it.name == property.name }
        ?: error("Failed to get '${property.name}' member")

    konst dependencyInstance = member.call(testIdeaKotlinInstances)
        ?: error("Failed to get '${property.name}'")

    return TestIdeaKotlinDependencySerializer(classLoader).serialize(dependencyInstance)
}
