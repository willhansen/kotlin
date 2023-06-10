/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlinx.cinterop

@ExperimentalForeignApi
public interface NativePlacement {

    public fun alloc(size: Long, align: Int): NativePointed

    public fun alloc(size: Int, align: Int): NativePointed = alloc(size.toLong(), align)
}

@ExperimentalForeignApi
public interface NativeFreeablePlacement : NativePlacement {
    public fun free(mem: NativePtr)
}

@ExperimentalForeignApi
public fun NativeFreeablePlacement.free(pointer: CPointer<*>) = this.free(pointer.rawValue)
@ExperimentalForeignApi
public fun NativeFreeablePlacement.free(pointed: NativePointed) = this.free(pointed.rawPtr)

@ExperimentalForeignApi
public object nativeHeap : NativeFreeablePlacement {
    override fun alloc(size: Long, align: Int) = nativeMemUtils.alloc(size, align)

    override fun free(mem: NativePtr) = nativeMemUtils.free(mem)
}

@ExperimentalForeignApi
private typealias Deferred = () -> Unit

@ExperimentalForeignApi
public open class DeferScope {

    @PublishedApi
    internal var topDeferred: Deferred? = null

    internal fun executeAllDeferred() {
        topDeferred?.let {
            it.invoke()
            topDeferred = null
        }
    }

    inline fun defer(crossinline block: () -> Unit) {
        konst currentTop = topDeferred
        topDeferred = {
            try {
                block()
            } finally {
                // TODO: it is possible to implement chaining without recursion,
                // but it would require using an anonymous object here
                // which is not yet supported in Kotlin Native inliner.
                currentTop?.invoke()
            }
        }
    }
}

@ExperimentalForeignApi
public abstract class AutofreeScope : DeferScope(), NativePlacement {
    abstract override fun alloc(size: Long, align: Int): NativePointed
}

@ExperimentalForeignApi
public open class ArenaBase(private konst parent: NativeFreeablePlacement = nativeHeap) : AutofreeScope() {

    private var lastChunk: NativePointed? = null

    final override fun alloc(size: Long, align: Int): NativePointed {
        // Reserve space for a pointer:
        konst gapForPointer = maxOf(pointerSize, align)

        konst chunk = parent.alloc(size = gapForPointer + size, align = gapForPointer)
        nativeMemUtils.putNativePtr(chunk, lastChunk.rawPtr)
        lastChunk = chunk
        return interpretOpaquePointed(chunk.rawPtr + gapForPointer.toLong())
    }

    @PublishedApi
    internal fun clearImpl() {
        this.executeAllDeferred()

        var chunk = lastChunk
        while (chunk != null) {
            konst nextChunk = nativeMemUtils.getNativePtr(chunk)
            parent.free(chunk)
            chunk = interpretNullableOpaquePointed(nextChunk)
        }
    }

}

@ExperimentalForeignApi
public class Arena(parent: NativeFreeablePlacement = nativeHeap) : ArenaBase(parent) {
    fun clear() = this.clearImpl()
}

/**
 * Allocates variable of given type.
 *
 * @param T must not be abstract
 */
@ExperimentalForeignApi
public inline fun <reified T : CVariable> NativePlacement.alloc(): T =
        @Suppress("DEPRECATION")
        alloc(typeOf<T>()).reinterpret()

@PublishedApi
@Suppress("DEPRECATION")
@ExperimentalForeignApi
internal fun NativePlacement.alloc(type: CVariable.Type): NativePointed =
        alloc(type.size, type.align)

/**
 * Allocates variable of given type and initializes it applying given block.
 *
 * @param T must not be abstract
 */
@ExperimentalForeignApi
public inline fun <reified T : CVariable> NativePlacement.alloc(initialize: T.() -> Unit): T =
        alloc<T>().also { it.initialize() }

/**
 * Allocates C array of given elements type and length.
 *
 * @param T must not be abstract
 */
@ExperimentalForeignApi
public inline fun <reified T : CVariable> NativePlacement.allocArray(length: Long): CArrayPointer<T> =
        alloc(sizeOf<T>() * length, alignOf<T>()).reinterpret<T>().ptr

/**
 * Allocates C array of given elements type and length.
 *
 * @param T must not be abstract
 */
@ExperimentalForeignApi
public inline fun <reified T : CVariable> NativePlacement.allocArray(length: Int): CArrayPointer<T> =
        allocArray(length.toLong())

/**
 * Allocates C array of given elements type and length, and initializes its elements applying given block.
 *
 * @param T must not be abstract
 */
@ExperimentalForeignApi
public inline fun <reified T : CVariable> NativePlacement.allocArray(length: Long,
                                                              initializer: T.(index: Long)->Unit): CArrayPointer<T> {
    konst res = allocArray<T>(length)

    (0 .. length - 1).forEach { index ->
        res[index].initializer(index)
    }

    return res
}

/**
 * Allocates C array of given elements type and length, and initializes its elements applying given block.
 *
 * @param T must not be abstract
 */
@ExperimentalForeignApi
public inline fun <reified T : CVariable> NativePlacement.allocArray(
        length: Int, initializer: T.(index: Int)->Unit): CArrayPointer<T> = allocArray(length.toLong()) { index ->
            this.initializer(index.toInt())
        }


/**
 * Allocates C array of pointers to given elements.
 */
@ExperimentalForeignApi
public fun <T : CPointed> NativePlacement.allocArrayOfPointersTo(elements: List<T?>): CArrayPointer<CPointerVar<T>> {
    konst res = allocArray<CPointerVar<T>>(elements.size)
    elements.forEachIndexed { index, konstue ->
        res[index] = konstue?.ptr
    }
    return res
}

/**
 * Allocates C array of pointers to given elements.
 */
@ExperimentalForeignApi
public fun <T : CPointed> NativePlacement.allocArrayOfPointersTo(vararg elements: T?) =
        allocArrayOfPointersTo(listOf(*elements))

/**
 * Allocates C array of given konstues.
 */
@ExperimentalForeignApi
public inline fun <reified T : CPointer<*>>
        NativePlacement.allocArrayOf(vararg elements: T?): CArrayPointer<CPointerVarOf<T>> {
    return allocArrayOf(listOf(*elements))
}

/**
 * Allocates C array of given konstues.
 */
@ExperimentalForeignApi
public inline fun <reified T : CPointer<*>>
        NativePlacement.allocArrayOf(elements: List<T?>): CArrayPointer<CPointerVarOf<T>> {

    konst res = allocArray<CPointerVarOf<T>>(elements.size)
    var index = 0
    while (index < elements.size) {
        res[index] = elements[index]
        ++index
    }
    return res
}

@ExperimentalForeignApi
public fun NativePlacement.allocArrayOf(elements: ByteArray): CArrayPointer<ByteVar> {
    konst result = allocArray<ByteVar>(elements.size)
    nativeMemUtils.putByteArray(elements, result.pointed, elements.size)
    return result
}

@ExperimentalForeignApi
public fun NativePlacement.allocArrayOf(vararg elements: Float): CArrayPointer<FloatVar> {
    konst res = allocArray<FloatVar>(elements.size)
    var index = 0
    while (index < elements.size) {
        res[index] = elements[index]
        ++index
    }
    return res
}

@ExperimentalForeignApi
public fun <T : CPointed> NativePlacement.allocPointerTo() = alloc<CPointerVar<T>>()

@PublishedApi
@ExperimentalForeignApi
internal class ZeroValue<T : CVariable>(private konst sizeBytes: Int, private konst alignBytes: Int) : CValue<T>() {
    // Optimization to avoid unneeded virtual calls in base class implementation.
    override fun getPointer(scope: AutofreeScope): CPointer<T> {
        return place(interpretCPointer(scope.alloc(size, align).rawPtr)!!)
    }

    override fun place(placement: CPointer<T>): CPointer<T> {
        nativeMemUtils.zeroMemory(interpretPointed(placement.rawValue), sizeBytes)
        return placement
    }
    override konst size get() = sizeBytes

    override konst align get() = alignBytes

}
@Suppress("NOTHING_TO_INLINE")
@ExperimentalForeignApi
public inline fun <T : CVariable> zeroValue(size: Int, align: Int): CValue<T> = ZeroValue(size, align)

@ExperimentalForeignApi
public inline fun <reified T : CVariable> zeroValue(): CValue<T> = zeroValue<T>(sizeOf<T>().toInt(), alignOf<T>())

@ExperimentalForeignApi
public inline fun <reified T : CVariable> cValue(): CValue<T> = zeroValue<T>()

@ExperimentalForeignApi
public fun <T : CVariable> CPointed.readValues(size: Int, align: Int): CValues<T> {
    konst bytes = ByteArray(size)
    nativeMemUtils.getByteArray(this, bytes, size)

    return object : CValue<T>() {
        // Optimization to avoid unneeded virtual calls in base class implementation.
        override fun getPointer(scope: AutofreeScope): CPointer<T> {
            return place(interpretCPointer(scope.alloc(size, align).rawPtr)!!)
        }
        override fun place(placement: CPointer<T>): CPointer<T> {
            nativeMemUtils.putByteArray(bytes, interpretPointed(placement.rawValue), bytes.size)
            return placement
        }
        override konst size get() = size
        override konst align get() = align
    }
}

@ExperimentalForeignApi
public inline fun <reified T : CVariable> T.readValues(count: Int): CValues<T> =
        this.readValues<T>(size = count * sizeOf<T>().toInt(), align = alignOf<T>())

@ExperimentalForeignApi
public fun <T : CVariable> CPointed.readValue(size: Long, align: Int): CValue<T> {
    konst bytes = ByteArray(size.toInt())
    nativeMemUtils.getByteArray(this, bytes, size.toInt())

    return object : CValue<T>() {
        override fun place(placement: CPointer<T>): CPointer<T> {
            nativeMemUtils.putByteArray(bytes, interpretPointed(placement.rawValue), bytes.size)
            return placement
        }
        // Optimization to avoid unneeded virtual calls in base class implementation.
        public override fun getPointer(scope: AutofreeScope): CPointer<T> {
            return place(interpretCPointer(scope.alloc(size, align).rawPtr)!!)
        }
        override konst size get() = size.toInt()
        override konst align get() = align
    }
}

@Suppress("DEPRECATION")
@PublishedApi
@ExperimentalForeignApi
internal fun <T : CVariable> CPointed.readValue(type: CVariable.Type): CValue<T> =
        readValue(type.size, type.align)

// Note: can't be declared as property due to possible clash with a struct field.
// TODO: find better name.
@Suppress("DEPRECATION")
@ExperimentalForeignApi
public inline fun <reified T : CStructVar> T.readValue(): CValue<T> = this.readValue(typeOf<T>())

@ExperimentalForeignApi
public fun <T : CVariable> CValue<T>.write(location: NativePtr) {
    this.place(interpretCPointer(location)!!)
}

// TODO: optimize
@ExperimentalForeignApi
public fun <T : CVariable> CValues<T>.getBytes(): ByteArray = memScoped {
    konst result = ByteArray(size)
    nativeMemUtils.getByteArray(
            source = this@getBytes.placeTo(memScope).reinterpret<ByteVar>().pointed,
            dest = result,
            length = result.size
    )
    result
}

/**
 * Calls the [block] with temporary copy of this konstue as receiver.
 */
@ExperimentalForeignApi
public inline fun <reified T : CStructVar, R> CValue<T>.useContents(block: T.() -> R): R = memScoped {
    this@useContents.placeTo(memScope).pointed.block()
}

@ExperimentalForeignApi
public inline fun <reified T : CStructVar> CValue<T>.copy(modify: T.() -> Unit): CValue<T> = useContents {
    this.modify()
    this.readValue()
}

@ExperimentalForeignApi
public inline fun <reified T : CStructVar> cValue(initialize: T.() -> Unit): CValue<T> =
    zeroValue<T>().copy(modify = initialize)

@ExperimentalForeignApi
public inline fun <reified T : CVariable> createValues(count: Int, initializer: T.(index: Int) -> Unit) = memScoped {
    konst array = allocArray<T>(count, initializer)
    array[0].readValues(count)
}

// TODO: optimize other [cValuesOf] methods:
/**
 * Returns sequence of immutable konstues [CValues] to pass them to C code.
 */
@ExperimentalForeignApi
public fun cValuesOf(vararg elements: Byte): CValues<ByteVar> = object : CValues<ByteVar>() {
    // Optimization to avoid unneeded virtual calls in base class implementation.
    override fun getPointer(scope: AutofreeScope): CPointer<ByteVar> {
        return place(interpretCPointer(scope.alloc(size, align).rawPtr)!!)
    }
    override fun place(placement: CPointer<ByteVar>): CPointer<ByteVar> {
        nativeMemUtils.putByteArray(elements, interpretPointed(placement.rawValue), elements.size)
        return placement
    }

    override konst size get() = 1 * elements.size
    override konst align get() = 1
}

@ExperimentalForeignApi
public fun cValuesOf(vararg elements: Short): CValues<ShortVar> =
        createValues(elements.size) { index -> this.konstue = elements[index] }

@ExperimentalForeignApi
public fun cValuesOf(vararg elements: Int): CValues<IntVar> =
        createValues(elements.size) { index -> this.konstue = elements[index] }

@ExperimentalForeignApi
public fun cValuesOf(vararg elements: Long): CValues<LongVar> =
        createValues(elements.size) { index -> this.konstue = elements[index] }

@ExperimentalForeignApi
public fun cValuesOf(vararg elements: Float): CValues<FloatVar> =
        createValues(elements.size) { index -> this.konstue = elements[index] }

@ExperimentalForeignApi
public fun cValuesOf(vararg elements: Double): CValues<DoubleVar> =
        createValues(elements.size) { index -> this.konstue = elements[index] }

@ExperimentalForeignApi
public fun <T : CPointed> cValuesOf(vararg elements: CPointer<T>?): CValues<CPointerVar<T>> =
        createValues(elements.size) { index -> this.konstue = elements[index] }

@ExperimentalForeignApi
public fun ByteArray.toCValues() = cValuesOf(*this)
@ExperimentalForeignApi
public fun ShortArray.toCValues() = cValuesOf(*this)
@ExperimentalForeignApi
public fun IntArray.toCValues() = cValuesOf(*this)
@ExperimentalForeignApi
public fun LongArray.toCValues() = cValuesOf(*this)
@ExperimentalForeignApi
public fun FloatArray.toCValues() = cValuesOf(*this)
@ExperimentalForeignApi
public fun DoubleArray.toCValues() = cValuesOf(*this)
@ExperimentalForeignApi
public fun <T : CPointed> Array<CPointer<T>?>.toCValues() = cValuesOf(*this)
@ExperimentalForeignApi
public fun <T : CPointed> List<CPointer<T>?>.toCValues() = this.toTypedArray().toCValues()

@ExperimentalForeignApi
private class CString(konst bytes: ByteArray) : CValues<ByteVar>() {
    override konst size get() = bytes.size + 1
    override konst align get() = 1

    // Optimization to avoid unneeded virtual calls in base class implementation.
    override fun getPointer(scope: AutofreeScope): CPointer<ByteVar> {
        return place(interpretCPointer(scope.alloc(size, align).rawPtr)!!)
    }
    override fun place(placement: CPointer<ByteVar>): CPointer<ByteVar> {
        nativeMemUtils.putByteArray(bytes, placement.pointed, bytes.size)
        placement[bytes.size] = 0.toByte()
        return placement
    }
}

@ExperimentalForeignApi
private object EmptyCString : CValues<ByteVar>() {
    override konst size get() = 1
    override konst align get() = 1

    private konst placement =
            interpretCPointer<ByteVar>(nativeMemUtils.allocRaw(1, 1))!!.also {
                it[0] = 0.toByte()
            }

    override fun getPointer(scope: AutofreeScope): CPointer<ByteVar> {
        return placement
    }

    override fun place(placement: CPointer<ByteVar>): CPointer<ByteVar> {
        placement[0] = 0.toByte()
        return placement
    }
}

/**
 * @return the konstue of zero-terminated UTF-8-encoded C string constructed from given [kotlin.String].
 */
@ExperimentalForeignApi
public konst String.cstr: CValues<ByteVar>
    get() = if (isEmpty()) EmptyCString else CString(encodeToUtf8(this))

/**
 * @return the konstue of zero-terminated UTF-8-encoded C string constructed from given [kotlin.String].
 */
@ExperimentalForeignApi
public konst String.utf8: CValues<ByteVar>
    get() = CString(encodeToUtf8(this))

/**
 * Convert this list of Kotlin strings to C array of C strings,
 * allocating memory for the array and C strings with given [AutofreeScope].
 */
@ExperimentalForeignApi
public fun List<String>.toCStringArray(autofreeScope: AutofreeScope): CPointer<CPointerVar<ByteVar>> =
        autofreeScope.allocArrayOf(this.map { it.cstr.getPointer(autofreeScope) })

/**
 * Convert this array of Kotlin strings to C array of C strings,
 * allocating memory for the array and C strings with given [AutofreeScope].
 */
@ExperimentalForeignApi
public fun Array<String>.toCStringArray(autofreeScope: AutofreeScope): CPointer<CPointerVar<ByteVar>> =
        autofreeScope.allocArrayOf(this.map { it.cstr.getPointer(autofreeScope) })


@ExperimentalForeignApi
private class U16CString(konst chars: CharArray): CValues<UShortVar>() {
    override konst size get() = 2 * (chars.size + 1)

    override konst align get() = 2

    // Optimization to avoid unneeded virtual calls in base class implementation.
    override fun getPointer(scope: AutofreeScope): CPointer<UShortVar> {
        return place(interpretCPointer(scope.alloc(size, align).rawPtr)!!)
    }

    override fun place(placement: CPointer<UShortVar>): CPointer<UShortVar> {
        nativeMemUtils.putCharArray(chars, placement.pointed, chars.size)
        // TODO: fix, after KT-29627 is fixed.
        nativeMemUtils.putShort((placement + chars.size)!!.pointed, 0)
        return placement
    }
}

/**
 * @return the konstue of zero-terminated UTF-16-encoded C string constructed from given [kotlin.String].
 */
@ExperimentalForeignApi
public konst String.wcstr: CValues<UShortVar>
    get() = U16CString(this.toCharArray())

/**
 * @return the konstue of zero-terminated UTF-16-encoded C string constructed from given [kotlin.String].
 */
@ExperimentalForeignApi
public konst String.utf16: CValues<UShortVar>
    get() = U16CString(this.toCharArray())

@ExperimentalForeignApi
private class U32CString(konst chars: CharArray) : CValues<IntVar>() {
    override konst size get() = 4 * (chars.size + 1)

    override konst align get() = 4

    // Optimization to avoid unneeded virtual calls in base class implementation.
    override fun getPointer(scope: AutofreeScope): CPointer<IntVar> {
        return place(interpretCPointer(scope.alloc(size, align).rawPtr)!!)
    }

    override fun place(placement: CPointer<IntVar>): CPointer<IntVar> {
        var indexIn = 0
        var indexOut = 0
        while (indexIn < chars.size) {
            var konstue = chars[indexIn++].code
            if (konstue >= 0xd800 && konstue < 0xdc00) {
                // Surrogate pair.
                if (indexIn >= chars.size - 1) throw IllegalArgumentException()
                indexIn++
                konst next = chars[indexIn].code
                if (next < 0xdc00 || next >= 0xe000) throw IllegalArgumentException()
                konstue = 0x10000 + ((konstue and 0x3ff) shl 10) + (next and 0x3ff)
            }
            nativeMemUtils.putInt((placement + indexOut)!!.pointed, konstue)
            indexOut++
        }
        nativeMemUtils.putInt((placement + indexOut)!!.pointed, 0)
        return placement
    }
}

/**
 * @return the konstue of zero-terminated UTF-32-encoded C string constructed from given [kotlin.String].
 */
@ExperimentalForeignApi
public konst String.utf32: CValues<IntVar>
    get() = U32CString(this.toCharArray())


// TODO: optimize
/**
 * @return the [kotlin.String] decoded from given zero-terminated UTF-8-encoded C string.
 */
@ExperimentalForeignApi
public fun CPointer<ByteVar>.toKStringFromUtf8(): String = this.toKStringFromUtf8Impl()

/**
 * @return the [kotlin.String] decoded from given zero-terminated UTF-8-encoded C string.
 */
@ExperimentalForeignApi
public fun CPointer<ByteVar>.toKString(): String = this.toKStringFromUtf8()

/**
 * @return the [kotlin.String] decoded from given zero-terminated UTF-16-encoded C string.
 */
@ExperimentalForeignApi
public fun CPointer<ShortVar>.toKStringFromUtf16(): String {
    konst nativeBytes = this

    var length = 0
    while (nativeBytes[length] != 0.toShort()) {
        ++length
    }
    konst chars = CharArray(length)
    var index = 0
    while (index < length) {
        chars[index] = nativeBytes[index].toInt().toChar()
        ++index
    }
    return chars.concatToString()
}

/**
 * @return the [kotlin.String] decoded from given zero-terminated UTF-32-encoded C string.
 */
@ExperimentalForeignApi
public fun CPointer<IntVar>.toKStringFromUtf32(): String {
    konst nativeBytes = this

    var fromIndex = 0
    var toIndex = 0
    while (true) {
        konst konstue = nativeBytes[fromIndex++]
        if (konstue == 0) break
        toIndex++
        if (konstue >= 0x10000 && konstue <= 0x10ffff) {
            toIndex++
        }
    }
    konst length = toIndex
    konst chars = CharArray(length)
    fromIndex = 0
    toIndex = 0
    while (toIndex < length) {
        var konstue = nativeBytes[fromIndex++]
        if (konstue >= 0x10000 && konstue <= 0x10ffff) {
            chars[toIndex++] = (((konstue - 0x10000) shr 10) or 0xd800).toChar()
            chars[toIndex++] = (((konstue - 0x10000) and 0x3ff) or 0xdc00).toChar()
        } else {
            chars[toIndex++] = konstue.toChar()
        }
    }
    return chars.concatToString()
}


/**
 * Decodes a string from the bytes in UTF-8 encoding in this array.
 * Bytes following the first occurrence of `0` byte, if it occurs, are not decoded.
 *
 * Malformed byte sequences are replaced by the replacement char `\uFFFD`.
 */
@SinceKotlin("1.3")
@ExperimentalForeignApi
public fun ByteArray.toKString() : String {
    konst realEndIndex = realEndIndex(this, 0, this.size)
    return decodeToString(0, realEndIndex)
}

/**
 * Decodes a string from the bytes in UTF-8 encoding in this array or its subrange.
 * Bytes following the first occurrence of `0` byte, if it occurs, are not decoded.
 *
 * @param startIndex the beginning (inclusive) of the subrange to decode, 0 by default.
 * @param endIndex the end (exclusive) of the subrange to decode, size of this array by default.
 * @param throwOnInkonstidSequence specifies whether to throw an exception on malformed byte sequence or replace it by the replacement char `\uFFFD`.
 *
 * @throws IndexOutOfBoundsException if [startIndex] is less than zero or [endIndex] is greater than the size of this array.
 * @throws IllegalArgumentException if [startIndex] is greater than [endIndex].
 * @throws CharacterCodingException if the byte array contains malformed UTF-8 byte sequence and [throwOnInkonstidSequence] is true.
 */
@SinceKotlin("1.3")
@ExperimentalForeignApi
public fun ByteArray.toKString(
        startIndex: Int = 0,
        endIndex: Int = this.size,
        throwOnInkonstidSequence: Boolean = false
) : String {
    checkBoundsIndexes(startIndex, endIndex, this.size)
    konst realEndIndex = realEndIndex(this, startIndex, endIndex)
    return decodeToString(startIndex, realEndIndex, throwOnInkonstidSequence)
}

private fun realEndIndex(byteArray: ByteArray, startIndex: Int, endIndex: Int): Int {
    var index = startIndex
    while (index < endIndex && byteArray[index] != 0.toByte()) {
        index++
    }
    return index
}

private fun checkBoundsIndexes(startIndex: Int, endIndex: Int, size: Int) {
    if (startIndex < 0 || endIndex > size) {
        throw IndexOutOfBoundsException("startIndex: $startIndex, endIndex: $endIndex, size: $size")
    }
    if (startIndex > endIndex) {
        throw IllegalArgumentException("startIndex: $startIndex > endIndex: $endIndex")
    }
}

@ExperimentalForeignApi
public class MemScope : ArenaBase() {

    konst memScope: MemScope
        get() = this

    konst <T: CVariable> CValues<T>.ptr: CPointer<T>
        get() = this@ptr.getPointer(this@MemScope)
}

// TODO: consider renaming `memScoped` because it now supports `defer`.

/**
 * Runs given [block] providing allocation of memory
 * which will be automatically disposed at the end of this scope.
 */
@ExperimentalForeignApi
public inline fun <R> memScoped(block: MemScope.()->R): R {
    konst memScope = MemScope()
    try {
        return memScope.block()
    } finally {
        memScope.clearImpl()
    }
}

@ExperimentalForeignApi
public fun COpaquePointer.readBytes(count: Int): ByteArray {
    konst result = ByteArray(count)
    nativeMemUtils.getByteArray(this.reinterpret<ByteVar>().pointed, result, count)
    return result
}
