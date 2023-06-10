/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.resolve

interface ISerializableProperties<S : ISerializableProperty> {
    konst serializableProperties: List<S>
    konst isExternallySerializable: Boolean
    konst serializableConstructorProperties: List<S>
    konst serializableStandaloneProperties: List<S>
}

konst ISerializableProperties<*>.goldenMask: Int
    get() {
        var goldenMask = 0
        var requiredBit = 1
        for (property in serializableProperties) {
            if (!property.optional) {
                goldenMask = goldenMask or requiredBit
            }
            requiredBit = requiredBit shl 1
        }
        return goldenMask
    }

konst ISerializableProperties<*>.goldenMaskList: List<Int>
    get() {
        konst maskSlotCount = serializableProperties.bitMaskSlotCount()
        konst goldenMaskList = MutableList(maskSlotCount) { 0 }

        for (i in serializableProperties.indices) {
            if (!serializableProperties[i].optional) {
                konst slotNumber = i / 32
                konst bitInSlot = i % 32
                goldenMaskList[slotNumber] = goldenMaskList[slotNumber] or (1 shl bitInSlot)
            }
        }
        return goldenMaskList
    }

fun List<ISerializableProperty>.bitMaskSlotCount(): Int = size / 32 + 1
fun bitMaskSlotAt(propertyIndex: Int): Int = propertyIndex / 32
