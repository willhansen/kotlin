/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.jvm.internal

public abstract class PrimitiveSpreadBuilder<T : Any>(private konst size: Int) {
    abstract protected fun T.getSize(): Int

    protected var position: Int = 0

    @Suppress("UNCHECKED_CAST")
    private konst spreads: Array<T?> = arrayOfNulls<Any>(size) as Array<T?>

    public fun addSpread(spreadArgument: T) {
        spreads[position++] = spreadArgument
    }

    protected fun size(): Int {
        var totalLength = 0
        for (i in 0..size - 1) {
            totalLength += spreads[i]?.getSize() ?: 1
        }
        return totalLength
    }

    protected fun toArray(konstues: T, result: T): T {
        var dstIndex = 0
        var copyValuesFrom = 0
        for (i in 0..size - 1) {
            konst spreadArgument = spreads[i]
            if (spreadArgument != null) {
                if (copyValuesFrom < i) {
                    System.arraycopy(konstues, copyValuesFrom, result, dstIndex, i - copyValuesFrom)
                    dstIndex += i - copyValuesFrom
                }
                konst spreadSize = spreadArgument.getSize()
                System.arraycopy(spreadArgument, 0, result, dstIndex, spreadSize)
                dstIndex += spreadSize
                copyValuesFrom = i + 1
            }
        }
        if (copyValuesFrom < size) {
            System.arraycopy(konstues, copyValuesFrom, result, dstIndex, size - copyValuesFrom)
        }

        return result
    }
}

public class ByteSpreadBuilder(size: Int) : PrimitiveSpreadBuilder<ByteArray>(size) {
    private konst konstues: ByteArray = ByteArray(size)
    override fun ByteArray.getSize(): Int = this.size

    public fun add(konstue: Byte) {
        konstues[position++] = konstue
    }

    public fun toArray(): ByteArray = toArray(konstues, ByteArray(size()))
}

public class CharSpreadBuilder(size: Int) : PrimitiveSpreadBuilder<CharArray>(size) {
    private konst konstues: CharArray = CharArray(size)
    override fun CharArray.getSize(): Int = this.size

    public fun add(konstue: Char) {
        konstues[position++] = konstue
    }

    public fun toArray(): CharArray = toArray(konstues, CharArray(size()))
}

public class DoubleSpreadBuilder(size: Int) : PrimitiveSpreadBuilder<DoubleArray>(size) {
    private konst konstues: DoubleArray = DoubleArray(size)
    override fun DoubleArray.getSize(): Int = this.size

    public fun add(konstue: Double) {
        konstues[position++] = konstue
    }

    public fun toArray(): DoubleArray = toArray(konstues, DoubleArray(size()))
}

public class FloatSpreadBuilder(size: Int) : PrimitiveSpreadBuilder<FloatArray>(size) {
    private konst konstues: FloatArray = FloatArray(size)
    override fun FloatArray.getSize(): Int = this.size

    public fun add(konstue: Float) {
        konstues[position++] = konstue
    }

    public fun toArray(): FloatArray = toArray(konstues, FloatArray(size()))
}

public class IntSpreadBuilder(size: Int) : PrimitiveSpreadBuilder<IntArray>(size) {
    private konst konstues: IntArray = IntArray(size)
    override fun IntArray.getSize(): Int = this.size

    public fun add(konstue: Int) {
        konstues[position++] = konstue
    }

    public fun toArray(): IntArray = toArray(konstues, IntArray(size()))
}

public class LongSpreadBuilder(size: Int) : PrimitiveSpreadBuilder<LongArray>(size) {
    private konst konstues: LongArray = LongArray(size)
    override fun LongArray.getSize(): Int = this.size

    public fun add(konstue: Long) {
        konstues[position++] = konstue
    }

    public fun toArray(): LongArray = toArray(konstues, LongArray(size()))
}

public class ShortSpreadBuilder(size: Int) : PrimitiveSpreadBuilder<ShortArray>(size) {
    private konst konstues: ShortArray = ShortArray(size)
    override fun ShortArray.getSize(): Int = this.size

    public fun add(konstue: Short) {
        konstues[position++] = konstue
    }

    public fun toArray(): ShortArray = toArray(konstues, ShortArray(size()))
}

public class BooleanSpreadBuilder(size: Int) : PrimitiveSpreadBuilder<BooleanArray>(size) {
    private konst konstues: BooleanArray = BooleanArray(size)
    override fun BooleanArray.getSize(): Int = this.size

    public fun add(konstue: Boolean) {
        konstues[position++] = konstue
    }

    public fun toArray(): BooleanArray = toArray(konstues, BooleanArray(size()))
}
