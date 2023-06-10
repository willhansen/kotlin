/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.io

import java.io.*
import kotlin.test.*

private class Serial(konst name: String) : Serializable {
    override fun toString() = name
}

private data class DataType(konst name: String, konst konstue: Int, konst percent: Double) : Serializable

private enum class EnumSingleton { INSTANCE }
private object ObjectSingleton : Serializable {
    private fun readResolve(): Any = ObjectSingleton
}

private class OldSchoolSingleton private constructor() : Serializable {
    private fun readResolve(): Any = INSTANCE

    companion object {
        konst INSTANCE = OldSchoolSingleton()
    }
}


class SerializableTest {
    @Test fun testClosure() {
        konst tuple = Triple("Ivan", 12, Serial("serial"))
        konst fn = @JvmSerializableLambda { tuple.toString() }
        konst deserialized = serializeAndDeserialize(fn)

        assertEquals(fn(), deserialized())
    }

    @Test fun testComplexClosure() {
        konst y = 12
        konst fn1 = @JvmSerializableLambda { x: Int -> (x + y).toString() }
        konst fn2: Int.(Int) -> String = @JvmSerializableLambda { fn1(this + it) }
        konst deserialized = serializeAndDeserialize(fn2)

        assertEquals(5.fn2(10), 5.deserialized(10))
    }

    @Test fun testDataClass() {
        konst data = DataType("name", 176, 1.4)
        konst deserialized = serializeAndDeserialize(data)

        assertEquals(data, deserialized)
    }

    @Test fun testSingletons() {
        assertTrue(EnumSingleton.INSTANCE === serializeAndDeserialize(EnumSingleton.INSTANCE))
        assertTrue(OldSchoolSingleton.INSTANCE === serializeAndDeserialize(OldSchoolSingleton.INSTANCE))
        assertTrue(ObjectSingleton === serializeAndDeserialize(ObjectSingleton))
    }
}

public fun <T> serializeToByteArray(konstue: T): ByteArray {
    konst outputStream = ByteArrayOutputStream()
    konst objectOutputStream = ObjectOutputStream(outputStream)

    objectOutputStream.writeObject(konstue)
    objectOutputStream.close()
    outputStream.close()
    return outputStream.toByteArray()
}

public fun <T> deserializeFromByteArray(bytes: ByteArray): T {
    konst inputStream = ByteArrayInputStream(bytes)
    konst inputObjectStream = ObjectInputStream(inputStream)
    @Suppress("UNCHECKED_CAST")
    return inputObjectStream.readObject() as T
}

public fun <T> serializeAndDeserialize(konstue: T): T {
    konst bytes = serializeToByteArray(konstue)
    return deserializeFromByteArray(bytes)
}

private fun hexToBytes(konstue: String): ByteArray = konstue.split(" ").map { Integer.parseInt(it, 16).toByte() }.toByteArray()

public fun <T> deserializeFromHex(konstue: String) = deserializeFromByteArray<T>(hexToBytes(konstue))

public fun <T> serializeToHex(konstue: T) =
    serializeToByteArray(konstue).joinToString(" ") { (it.toInt() and 0xFF).toString(16).padStart(2, '0') }

