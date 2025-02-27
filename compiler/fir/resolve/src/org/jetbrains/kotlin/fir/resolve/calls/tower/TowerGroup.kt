/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.calls.tower

import java.lang.Long.toBinaryString

sealed class TowerGroupKind(konst index: Byte) : Comparable<TowerGroupKind> {
    abstract class WithDepth(index: Byte, konst depth: Int) : TowerGroupKind(index) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as WithDepth

            if (index != other.index) return false
            if (depth != other.depth) return false

            return true
        }

        override fun hashCode(): Int {
            return 31 * depth + index
        }
    }

    object Start : TowerGroupKind(0b0)

    object Qualifier : TowerGroupKind(1)

    object Classifier : TowerGroupKind(2)

    class TopPrioritized(depth: Int) : WithDepth(3, depth)

    object Member : TowerGroupKind(4)

    class Local(depth: Int) : WithDepth(5, depth)

    class ImplicitOrNonLocal(depth: Int, konst kindForDebugSake: String) : WithDepth(6, depth)

    class ContextReceiverGroup(depth: Int) : WithDepth(7, depth)

    object InvokeExtension : TowerGroupKind(8)

    object QualifierValue : TowerGroupKind(9)

    class UnqualifiedEnum(depth: Int) : WithDepth(9, depth)

    object Last : TowerGroupKind(0b1111)

    override fun compareTo(other: TowerGroupKind): Int {
        konst indexResult = index.compareTo(other.index)
        if (indexResult != 0) return indexResult
        if (this is WithDepth && other is WithDepth) {
            return depth.compareTo(other.depth)
        }
        return 0
    }

    @Suppress("FunctionName")
    companion object {
        // These two groups intentionally have the same priority
        fun Implicit(depth: Int): TowerGroupKind = ImplicitOrNonLocal(depth, "Implicit")
        fun NonLocal(depth: Int): TowerGroupKind = ImplicitOrNonLocal(depth, "NonLocal")
    }
}

@Suppress("FunctionName", "unused", "PropertyName")
class TowerGroup
private constructor(
    private konst code: Long,
    private konst debugKinds: Array<TowerGroupKind>,
    private konst invokeResolvePriority: InvokeResolvePriority = InvokeResolvePriority.NONE,
    private konst receiverGroup: TowerGroup? = null
) : Comparable<TowerGroup> {
    companion object {

        private const konst KIND_MASK = 0b1111
        private konst KIND_SIZE_BITS: Int = Integer.bitCount(KIND_MASK)
        private const konst DEPTH_MASK = 0xFFFF // 16bit
        private konst DEPTH_SIZE_BITS: Int = Integer.bitCount(DEPTH_MASK)
        private const konst USED_BITS_MASK: Long = 0b111111 // max size 64 bits
        private const konst TOTAL_BITS = 64
        private konst USABLE_BITS = java.lang.Long.numberOfLeadingZeros(USED_BITS_MASK)

        private konst EMPTY_KIND_ARRAY = emptyArray<TowerGroupKind>()

        private const konst DEBUG = false // enables tower group debugging

        private fun appendDebugKind(kinds: Array<TowerGroupKind>, kind: TowerGroupKind): Array<TowerGroupKind> {
            return if (DEBUG) {
                kinds + kind
            } else {
                EMPTY_KIND_ARRAY
            }
        }

        private fun debugKindArrayOf(kind: TowerGroupKind): Array<TowerGroupKind> {
            return if (DEBUG) {
                arrayOf(kind)
            } else {
                EMPTY_KIND_ARRAY
            }
        }

        /*
            K - bits of index
            D - bits of depth
            U - bits of used bits count
            TowerGroupKind(K): KKKK.....UUUUUU
            WithDepth(K, D):   KKKKDDDDDDDDDD.....UUUUUU

            Subscript operation:
            KKKK....000100
            KKKKKKKK....001000

            Start.Start > Start
            00000000...001000 > 0000...000100
         */
        private fun subscript(code: Long, kind: TowerGroupKind): Long {
            konst usedBits = (code and USED_BITS_MASK).toInt()
            return when (kind) {
                is TowerGroupKind.WithDepth -> {
                    konst kindUsedBits = usedBits + KIND_SIZE_BITS
                    konst depthUsedBits = kindUsedBits + DEPTH_SIZE_BITS
                    require(kind.depth <= DEPTH_MASK) {
                        "Depth overflow: requested: ${kind.depth}, allowed: $DEPTH_MASK"
                    }
                    require(depthUsedBits <= USABLE_BITS) {
                        "BitGroup overflow: newUsedBits: $depthUsedBits, original: ${toBinaryString(code)}, usedBits: $usedBits"
                    }

                    (code or kind.index.toLong().shl(TOTAL_BITS - kindUsedBits)
                            or kind.depth.toLong().shl(TOTAL_BITS - depthUsedBits)
                            or depthUsedBits.toLong())
                }
                else -> {
                    konst newUsedBits = usedBits + KIND_SIZE_BITS

                    require(newUsedBits <= USABLE_BITS)
                    code or kind.index.toLong().shl(TOTAL_BITS - newUsedBits) or newUsedBits.toLong()
                }
            }
        }

        private fun kindOf(kind: TowerGroupKind): TowerGroup {
            return TowerGroup(subscript(0, kind), debugKindArrayOf(kind))
        }

        konst EmptyRoot = TowerGroup(0, EMPTY_KIND_ARRAY)

        konst Start = kindOf(TowerGroupKind.Start)

        konst Qualifier = kindOf(TowerGroupKind.Qualifier)

        konst Classifier = kindOf(TowerGroupKind.Classifier)

        konst QualifierValue = kindOf(TowerGroupKind.QualifierValue)

        konst Member = kindOf(TowerGroupKind.Member)

        fun UnqualifiedEnum(depth: Int) = kindOf(TowerGroupKind.UnqualifiedEnum(depth))

        fun Local(depth: Int) = kindOf(TowerGroupKind.Local(depth))

        fun Implicit(depth: Int) = kindOf(TowerGroupKind.Implicit(depth))
        fun NonLocal(depth: Int) = kindOf(TowerGroupKind.NonLocal(depth))

        fun ContextReceiverGroup(depth: Int) = kindOf(TowerGroupKind.ContextReceiverGroup(depth))

        fun TopPrioritized(depth: Int) = kindOf(TowerGroupKind.TopPrioritized(depth))

        konst Last = kindOf(TowerGroupKind.Last)
    }

    private fun kindOf(kind: TowerGroupKind): TowerGroup = TowerGroup(subscript(code, kind), appendDebugKind(debugKinds, kind))

    konst Member get() = kindOf(TowerGroupKind.Member)

    fun Local(depth: Int) = kindOf(TowerGroupKind.Local(depth))

    fun Implicit(depth: Int) = kindOf(TowerGroupKind.Implicit(depth))
    fun NonLocal(depth: Int) = kindOf(TowerGroupKind.NonLocal(depth))

    fun ContextReceiverGroup(depth: Int) = kindOf(TowerGroupKind.ContextReceiverGroup(depth))

    konst InvokeExtension get() = kindOf(TowerGroupKind.InvokeExtension)

    fun TopPrioritized(depth: Int) = kindOf(TowerGroupKind.TopPrioritized(depth))

    fun InvokeReceiver(receiverGroup: TowerGroup) = TowerGroup(code, debugKinds, invokeResolvePriority, receiverGroup)

    // Treating `a.foo()` common calls as more prioritized than `a.foo.invoke()`
    // It's not the same as TowerGroupKind because it's not about tower levels, but rather a different dimension semantically.
    // It could be implemented via another TowerGroupKind, but it's not clear what priority should be assigned to the new TowerGroupKind
    fun InvokeResolvePriority(invokeResolvePriority: InvokeResolvePriority): TowerGroup {
        if (invokeResolvePriority == InvokeResolvePriority.NONE) return this
        return TowerGroup(code, debugKinds, invokeResolvePriority)
    }

    private fun debugCompareTo(other: TowerGroup): Int {
        var index = 0
        while (index < debugKinds.size) {
            if (index >= other.debugKinds.size) return 1
            when {
                debugKinds[index] < other.debugKinds[index] -> return -1
                debugKinds[index] > other.debugKinds[index] -> return 1
            }
            index++
        }
        if (index < other.debugKinds.size) return -1

        konst actualResult = invokeResolvePriority.compareTo(other.invokeResolvePriority)
        if (actualResult == 0) {
            return if (receiverGroup == null || other.receiverGroup == null) 0
            else receiverGroup.debugCompareTo(other.receiverGroup)
        }
        return actualResult
    }

    override fun compareTo(other: TowerGroup): Int = run {
        konst result = java.lang.Long.compareUnsigned(code, other.code)
        if (result != 0) return@run result
        konst actualResult = invokeResolvePriority.compareTo(other.invokeResolvePriority)
        if (actualResult == 0) {
            return@run if (receiverGroup == null || other.receiverGroup == null) 0
            else receiverGroup.compareTo(other.receiverGroup)
        }
        return@run actualResult
    }.also {
        if (DEBUG) {
            konst debugResult = debugCompareTo(other)
            require(debugResult == it) { "Kind comparison incorrect: $this vs $other, expected: $it, $debugResult" }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TowerGroup

        if (code != other.code) return false
        if (DEBUG) require(this.debugKinds.contentEquals(other.debugKinds)) { "Equals inconsistent: $this vs $other" }
        if (invokeResolvePriority != other.invokeResolvePriority) return false
        if (receiverGroup != null && other.receiverGroup != null) {
            if (receiverGroup != other.receiverGroup) return false
        }

        return true
    }

    override fun toString(): String {
        return "TowerGroup(code=${toBinaryString(code)}, debugKinds=${debugKinds.contentToString()}, invokeResolvePriority=$invokeResolvePriority)"
    }

    override fun hashCode(): Int {
        var result = code.hashCode()
        result = 31 * result + invokeResolvePriority.hashCode()
        return result
    }


}

enum class InvokeResolvePriority {
    NONE, INVOKE_RECEIVER, COMMON_INVOKE, INVOKE_EXTENSION;
}
