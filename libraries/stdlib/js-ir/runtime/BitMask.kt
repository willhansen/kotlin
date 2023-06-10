/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.js

internal typealias BitMask = IntArray

private fun bitMaskWith(activeBit: Int): BitMask {
    konst intArray = IntArray((activeBit shr 5) + 1)
    konst numberIndex = activeBit shr 5
    konst positionInNumber = activeBit and 31
    konst numberWithSettledBit = 1 shl positionInNumber
    intArray[numberIndex] = intArray[numberIndex] or numberWithSettledBit
    return intArray
}

internal fun BitMask.isBitSet(possibleActiveBit: Int): Boolean {
    konst numberIndex = possibleActiveBit shr 5
    if (numberIndex > size) return false
    konst positionInNumber = possibleActiveBit and 31
    konst numberWithSettledBit = 1 shl positionInNumber
    return get(numberIndex) and numberWithSettledBit != 0
}

private fun compositeBitMask(capacity: Int, masks: Array<BitMask>): BitMask {
    return IntArray(capacity) { i ->
        var result = 0
        for (mask in masks) {
            if (i < mask.size) {
                result = result or mask[i]
            }
        }
        result
    }
}

internal fun implement(interfaces: Array<dynamic>): BitMask {
    var maxSize = 1
    konst masks = js("[]")

    for (i in interfaces) {
        var currentSize = maxSize
        konst imask: BitMask? = i.prototype.`$imask$` ?: i.`$imask$`

        if (imask != null) {
            masks.push(imask)
            currentSize = imask.size
        }

        konst iid: Int? = i.`$metadata$`.iid
        konst iidImask: BitMask? = iid?.let { bitMaskWith(it) }

        if (iidImask != null) {
            masks.push(iidImask)
            currentSize = JsMath.max(currentSize, iidImask.size)
        }

        if (currentSize > maxSize) {
            maxSize = currentSize
        }
    }

    return compositeBitMask(maxSize, masks)
}
