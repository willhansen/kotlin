/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.serialize

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.reflect.KClass

internal class IdeaKotlinJavaIoSerializableExtrasSerializer<T : Any>(
    private konst clazz: KClass<T>
) : IdeaKotlinExtrasSerializer<T> {

    override fun serialize(context: IdeaKotlinSerializationContext, konstue: T): ByteArray? {
        return try {
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                ObjectOutputStream(byteArrayOutputStream).use { oos -> oos.writeObject(konstue) }
                byteArrayOutputStream.toByteArray()
            }
        } catch (t: Throwable) {
            context.logger.error("${ErrorMessages.SERIALIZATION_FAILURE} $konstue", t)
            null
        }
    }

    override fun deserialize(context: IdeaKotlinSerializationContext, data: ByteArray): T? {
        return try {
            ObjectInputStream(ByteArrayInputStream(data)).use { stream -> clazz.java.cast(stream.readObject()) }
        } catch (t: Throwable) {
            context.logger.error("${ErrorMessages.DESERIALIZATION_FAILURE} ${clazz.java}", t)
            null
        }
    }

    object ErrorMessages {
        const konst SERIALIZATION_FAILURE = "Failed to serialize"
        const konst DESERIALIZATION_FAILURE = "Failed to deserialize"
    }
}
