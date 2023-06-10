/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlinx.cinterop

import kotlin.native.*

/**
 * The entity which has an associated native pointer.
 * Subtypes are supposed to represent interpretations of the pointed data or code.
 *
 * This interface is likely to be handled by compiler magic and shouldn't be subtyped by arbitrary classes.
 *
 * TODO: the behavior of [equals], [hashCode] and [toString] differs on Native and JVM backends.
 */
@ExperimentalForeignApi
public open class NativePointed internal constructor(rawPtr: NonNullNativePtr) {
    var rawPtr = rawPtr.toNativePtr()
        internal set
}

// `null` konstue of `NativePointed?` is mapped to `nativeNullPtr`.
@ExperimentalForeignApi
public konst NativePointed?.rawPtr: NativePtr
    get() = if (this != null) this.rawPtr else nativeNullPtr

/**
 * Returns interpretation of entity with given pointer.
 *
 * @param T must not be abstract
 */
@ExperimentalForeignApi
public inline fun <reified T : NativePointed> interpretPointed(ptr: NativePtr): T = interpretNullablePointed<T>(ptr)!!

@ExperimentalForeignApi
private class OpaqueNativePointed(rawPtr: NativePtr) : NativePointed(rawPtr.toNonNull())

@ExperimentalForeignApi
public fun interpretOpaquePointed(ptr: NativePtr): NativePointed = interpretPointed<OpaqueNativePointed>(ptr)
@ExperimentalForeignApi
public fun interpretNullableOpaquePointed(ptr: NativePtr): NativePointed? = interpretNullablePointed<OpaqueNativePointed>(ptr)

/**
 * Changes the interpretation of the pointed data or code.
 */
@ExperimentalForeignApi
public inline fun <reified T : NativePointed> NativePointed.reinterpret(): T = interpretPointed(this.rawPtr)

/**
 * C data or code.
 */
@ExperimentalForeignApi
public abstract class CPointed(rawPtr: NativePtr) : NativePointed(rawPtr.toNonNull())

/**
 * Represents a reference to (possibly empty) sequence of C konstues.
 * It can be either a stable pointer [CPointer] or a sequence of immutable konstues [CValues].
 *
 * [CValuesRef] is designed to be used as Kotlin representation of pointer-typed parameters of C functions.
 * When passing [CPointer] as [CValuesRef] to the Kotlin binding method, the C function receives exactly this pointer.
 * Passing [CValues] has nearly the same semantics as passing by konstue: the C function receives
 * the pointer to the temporary copy of these konstues, and the caller can't observe the modifications to this copy.
 * The copy is konstid until the C function returns.
 * There are other implementations of [CValuesRef] that provide temporary pointer,
 * e.g. Kotlin Native specific [refTo] functions to pass primitive arrays directly to native.
 */
@ExperimentalForeignApi
public abstract class CValuesRef<T : CPointed> {
    /**
     * If this reference is [CPointer], returns this pointer, otherwise
     * allocate storage konstue in the scope and return it.
     */
    public abstract fun getPointer(scope: AutofreeScope): CPointer<T>
}

/**
 * The (possibly empty) sequence of immutable C konstues.
 * It is self-contained and doesn't depend on native memory.
 */
@ExperimentalForeignApi
public abstract class CValues<T : CVariable> : CValuesRef<T>() {
    /**
     * Copies the konstues to [placement] and returns the pointer to the copy.
     */
    public override fun getPointer(scope: AutofreeScope): CPointer<T> {
        return place(interpretCPointer(scope.alloc(size, align).rawPtr)!!)
    }

    // TODO: optimize
    public override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CValues<*>) return false

        konst thisBytes = this.getBytes()
        konst otherBytes = other.getBytes()

        if (thisBytes.size != otherBytes.size) {
            return false
        }

        for (index in 0 .. thisBytes.size - 1) {
            if (thisBytes[index] != otherBytes[index]) {
                return false
            }
        }

        return true
    }

    public override fun hashCode(): Int {
        var result = 0
        for (byte in this.getBytes()) {
            result = result * 31 + byte
        }
        return result
    }

    public abstract konst size: Int

    public abstract konst align: Int

    /**
     * Copy the referenced konstues to [placement] and return placement pointer.
     */
    public abstract fun place(placement: CPointer<T>): CPointer<T>
}

@ExperimentalForeignApi
public fun <T : CVariable> CValues<T>.placeTo(scope: AutofreeScope) = this.getPointer(scope)

/**
 * The single immutable C konstue.
 * It is self-contained and doesn't depend on native memory.
 *
 * TODO: consider providing an adapter instead of subtyping [CValues].
 */
@ExperimentalForeignApi
public abstract class CValue<T : CVariable> : CValues<T>()

/**
 * C pointer.
 */
@ExperimentalForeignApi
public class CPointer<T : CPointed> internal constructor(@PublishedApi internal konst konstue: NonNullNativePtr) : CValuesRef<T>() {

    // TODO: replace by [konstue].
    @Suppress("NOTHING_TO_INLINE")
    public inline konst rawValue: NativePtr get() = konstue.toNativePtr()

    public override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true // fast path
        }

        return (other is CPointer<*>) && (rawValue == other.rawValue)
    }

    public override fun hashCode(): Int {
        return rawValue.hashCode()
    }

    public override fun toString() = this.cPointerToString()

    public override fun getPointer(scope: AutofreeScope) = this
}

/**
 * Returns the pointer to this data or code.
 */
@ExperimentalForeignApi
public konst <T : CPointed> T.ptr: CPointer<T>
    get() = interpretCPointer(this.rawPtr)!!

/**
 * Returns the corresponding [CPointed].
 *
 * @param T must not be abstract
 */
@ExperimentalForeignApi
public inline konst <reified T : CPointed> CPointer<T>.pointed: T
    get() = interpretPointed<T>(this.rawValue)

// `null` konstue of `CPointer?` is mapped to `nativeNullPtr`
@ExperimentalForeignApi
public konst CPointer<*>?.rawValue: NativePtr
    get() = if (this != null) this.rawValue else nativeNullPtr

@ExperimentalForeignApi
public fun <T : CPointed> CPointer<*>.reinterpret(): CPointer<T> = interpretCPointer(this.rawValue)!!

@ExperimentalForeignApi
public fun <T : CPointed> CPointer<T>?.toLong() = this.rawValue.toLong()

@ExperimentalForeignApi
public fun <T : CPointed> Long.toCPointer(): CPointer<T>? = interpretCPointer(nativeNullPtr + this)

/**
 * The [CPointed] without any specified interpretation.
 */
@ExperimentalForeignApi
public abstract class COpaque(rawPtr: NativePtr) : CPointed(rawPtr) // TODO: should it correspond to COpaquePointer?

/**
 * The pointer with an opaque type.
 */
@OptIn(ExperimentalForeignApi::class)
/*
 * This typealias should be marked as @ExperimentalForeignApi
 * but such marking starts crashing the compiler (due to typealias handling bug).
 * We are not blocked by this behaviour because any declaration mentioning
 * `COpaquePointer` will trigger `ExperimentalForeignApi` error due to typealias expansion
 * in opt-in propagation mechanism.
 */
public typealias COpaquePointer = CPointer<out CPointed> // FIXME (the comment is about the typealias, not its opt-in annotation)

/**
 * The variable containing a [COpaquePointer].
 */
@ExperimentalForeignApi
public typealias COpaquePointerVar = CPointerVarOf<COpaquePointer>

/**
 * The C data variable located in memory.
 *
 * The non-abstract subclasses should represent the (complete) C data type and thus specify size and alignment.
 * Each such subclass must have a companion object which is a [Type].
 */
@ExperimentalForeignApi
public abstract class CVariable(rawPtr: NativePtr) : CPointed(rawPtr) {

    /**
     * The (complete) C data type.
     *
     * @param size the size in bytes of data of this type
     * @param align the alignments in bytes that is enough for this data type.
     * It may be greater than actually required for simplicity.
     */
    @Deprecated("Use sizeOf<T>() or alignOf<T>() instead.")
    public open class Type(konst size: Long, konst align: Int) {

        init {
            require(size % align == 0L)
        }

    }
}

@Suppress("DEPRECATION")
@ExperimentalForeignApi
public inline fun <reified T : CVariable> sizeOf() = typeOf<T>().size

@Suppress("DEPRECATION")
@ExperimentalForeignApi
public inline fun <reified T : CVariable> alignOf() = typeOf<T>().align

/**
 * Returns the member of this [CStructVar] which is located by given offset in bytes.
 */
@ExperimentalForeignApi
public inline fun <reified T : CPointed> CStructVar.memberAt(offset: Long): T {
    return interpretPointed<T>(this.rawPtr + offset)
}

@ExperimentalForeignApi
public inline fun <reified T : CVariable> CStructVar.arrayMemberAt(offset: Long): CArrayPointer<T> {
    return interpretCPointer<T>(this.rawPtr + offset)!!
}

/**
 * The C struct-typed variable located in memory.
 */
@ExperimentalForeignApi
public abstract class CStructVar(rawPtr: NativePtr) : CVariable(rawPtr) {
    @Deprecated("Use sizeOf<T>() or alignOf<T>() instead.")
    @Suppress("DEPRECATION")
    open class Type(size: Long, align: Int) : CVariable.Type(size, align)
}

/**
 * The C primitive-typed variable located in memory.
 */
@ExperimentalForeignApi
sealed class CPrimitiveVar(rawPtr: NativePtr) : CVariable(rawPtr) {
    // aligning by size is obviously enough
    @Deprecated("Use sizeOf<T>() or alignOf<T>() instead.")
    @Suppress("DEPRECATION")
    open class Type(size: Int) : CVariable.Type(size.toLong(), align = size)
}

@Deprecated("Will be removed.")
public interface CEnum {
    public konst konstue: Any
}

@ExperimentalForeignApi
public abstract class CEnumVar(rawPtr: NativePtr) : CPrimitiveVar(rawPtr)

// generics below are used for typedef support
// these classes are not supposed to be used directly, instead the typealiases are provided.

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND")
public class BooleanVarOf<T : Boolean>(rawPtr: NativePtr) : CPrimitiveVar(rawPtr) {
    @Deprecated("Use sizeOf<T>() or alignOf<T>() instead.")
    @Suppress("DEPRECATION")
    companion object : Type(1)
}

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND")
public class ByteVarOf<T : Byte>(rawPtr: NativePtr) : CPrimitiveVar(rawPtr) {
    @Deprecated("Use sizeOf<T>() or alignOf<T>() instead.")
    @Suppress("DEPRECATION")
    companion object : Type(1)
}

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND")
public class ShortVarOf<T : Short>(rawPtr: NativePtr) : CPrimitiveVar(rawPtr) {
    @Deprecated("Use sizeOf<T>() or alignOf<T>() instead.")
    @Suppress("DEPRECATION")
    companion object : Type(2)
}

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND")
public class IntVarOf<T : Int>(rawPtr: NativePtr) : CPrimitiveVar(rawPtr) {
    @Deprecated("Use sizeOf<T>() or alignOf<T>() instead.")
    @Suppress("DEPRECATION")
    companion object : Type(4)
}

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND")
public class LongVarOf<T : Long>(rawPtr: NativePtr) : CPrimitiveVar(rawPtr) {
    @Deprecated("Use sizeOf<T>() or alignOf<T>() instead.")
    @Suppress("DEPRECATION")
    companion object : Type(8)
}

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND")
public class UByteVarOf<T : UByte>(rawPtr: NativePtr) : CPrimitiveVar(rawPtr) {
    @Deprecated("Use sizeOf<T>() or alignOf<T>() instead.")
    @Suppress("DEPRECATION")
    companion object : Type(1)
}

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND")
public class UShortVarOf<T : UShort>(rawPtr: NativePtr) : CPrimitiveVar(rawPtr) {
    @Deprecated("Use sizeOf<T>() or alignOf<T>() instead.")
    @Suppress("DEPRECATION")
    companion object : Type(2)
}

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND")
public class UIntVarOf<T : UInt>(rawPtr: NativePtr) : CPrimitiveVar(rawPtr) {
    @Deprecated("Use sizeOf<T>() or alignOf<T>() instead.")
    @Suppress("DEPRECATION")
    companion object : Type(4)
}

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND")
public class ULongVarOf<T : ULong>(rawPtr: NativePtr) : CPrimitiveVar(rawPtr) {
    @Deprecated("Use sizeOf<T>() or alignOf<T>() instead.")
    @Suppress("DEPRECATION")
    companion object : Type(8)
}

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND")
public class FloatVarOf<T : Float>(rawPtr: NativePtr) : CPrimitiveVar(rawPtr) {
    @Deprecated("Use sizeOf<T>() or alignOf<T>() instead.")
    @Suppress("DEPRECATION")
    companion object : Type(4)
}

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND")
public class DoubleVarOf<T : Double>(rawPtr: NativePtr) : CPrimitiveVar(rawPtr) {
    @Deprecated("Use sizeOf<T>() or alignOf<T>() instead.")
    @Suppress("DEPRECATION")
    companion object : Type(8)
}

@ExperimentalForeignApi
public typealias BooleanVar = BooleanVarOf<Boolean>
@ExperimentalForeignApi
public typealias ByteVar = ByteVarOf<Byte>
@ExperimentalForeignApi
public typealias ShortVar = ShortVarOf<Short>
@ExperimentalForeignApi
public typealias IntVar = IntVarOf<Int>
@ExperimentalForeignApi
public typealias LongVar = LongVarOf<Long>
@ExperimentalForeignApi
public typealias UByteVar = UByteVarOf<UByte>
@ExperimentalForeignApi
public typealias UShortVar = UShortVarOf<UShort>
@ExperimentalForeignApi
public typealias UIntVar = UIntVarOf<UInt>
@ExperimentalForeignApi
public typealias ULongVar = ULongVarOf<ULong>
@ExperimentalForeignApi
public typealias FloatVar = FloatVarOf<Float>
@ExperimentalForeignApi
public typealias DoubleVar = DoubleVarOf<Double>

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND", "UNCHECKED_CAST")
public var <T : Boolean> BooleanVarOf<T>.konstue: T
    get() {
        konst byte = nativeMemUtils.getByte(this)
        return byte.toBoolean() as T
    }
    set(konstue) = nativeMemUtils.putByte(this, konstue.toByte())

// TODO remove these boolean <-> byte declarations

@Suppress("NOTHING_TO_INLINE")
@ExperimentalForeignApi
public inline fun Boolean.toByte(): Byte = if (this) 1 else 0

@Suppress("NOTHING_TO_INLINE")
@ExperimentalForeignApi
public inline fun Byte.toBoolean() = (this.toInt() != 0)

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND", "UNCHECKED_CAST")
public var <T : Byte> ByteVarOf<T>.konstue: T
    get() = nativeMemUtils.getByte(this) as T
    set(konstue) = nativeMemUtils.putByte(this, konstue)

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND", "UNCHECKED_CAST")
public var <T : Short> ShortVarOf<T>.konstue: T
    get() = nativeMemUtils.getShort(this) as T
    set(konstue) = nativeMemUtils.putShort(this, konstue)

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND", "UNCHECKED_CAST")
public var <T : Int> IntVarOf<T>.konstue: T
    get() = nativeMemUtils.getInt(this) as T
    set(konstue) = nativeMemUtils.putInt(this, konstue)

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND", "UNCHECKED_CAST")
public var <T : Long> LongVarOf<T>.konstue: T
    get() = nativeMemUtils.getLong(this) as T
    set(konstue) = nativeMemUtils.putLong(this, konstue)

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND", "UNCHECKED_CAST")
public var <T : UByte> UByteVarOf<T>.konstue: T
    get() = nativeMemUtils.getByte(this).toUByte() as T
    set(konstue) = nativeMemUtils.putByte(this, konstue.toByte())

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND", "UNCHECKED_CAST")
public var <T : UShort> UShortVarOf<T>.konstue: T
    get() = nativeMemUtils.getShort(this).toUShort() as T
    set(konstue) = nativeMemUtils.putShort(this, konstue.toShort())

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND", "UNCHECKED_CAST")
public var <T : UInt> UIntVarOf<T>.konstue: T
    get() = nativeMemUtils.getInt(this).toUInt() as T
    set(konstue) = nativeMemUtils.putInt(this, konstue.toInt())

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND", "UNCHECKED_CAST")
public var <T : ULong> ULongVarOf<T>.konstue: T
    get() = nativeMemUtils.getLong(this).toULong() as T
    set(konstue) = nativeMemUtils.putLong(this, konstue.toLong())

// TODO: ensure native floats have the appropriate binary representation

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND", "UNCHECKED_CAST")
public var <T : Float> FloatVarOf<T>.konstue: T
    get() = nativeMemUtils.getFloat(this) as T
    set(konstue) = nativeMemUtils.putFloat(this, konstue)

@ExperimentalForeignApi
@Suppress("FINAL_UPPER_BOUND", "UNCHECKED_CAST")
public var <T : Double> DoubleVarOf<T>.konstue: T
    get() = nativeMemUtils.getDouble(this) as T
    set(konstue) = nativeMemUtils.putDouble(this, konstue)


@ExperimentalForeignApi
public class CPointerVarOf<T : CPointer<*>>(rawPtr: NativePtr) : CVariable(rawPtr) {
    @Deprecated("Use sizeOf<T>() or alignOf<T>() instead.")
    @Suppress("DEPRECATION")
    companion object : CVariable.Type(pointerSize.toLong(), pointerSize)
}

/**
 * The C data variable containing the pointer to `T`.
 */
@ExperimentalForeignApi
public typealias CPointerVar<T> = CPointerVarOf<CPointer<T>>

/**
 * The konstue of this variable.
 */
@ExperimentalForeignApi
@Suppress("UNCHECKED_CAST")
public inline var <P : CPointer<*>> CPointerVarOf<P>.konstue: P?
    get() = interpretCPointer<CPointed>(nativeMemUtils.getNativePtr(this)) as P?
    set(konstue) = nativeMemUtils.putNativePtr(this, konstue.rawValue)

/**
 * The code or data pointed by the konstue of this variable.
 *
 * @param T must not be abstract
 */
@ExperimentalForeignApi
public inline var <reified T : CPointed, reified P : CPointer<T>> CPointerVarOf<P>.pointed: T?
    get() = this.konstue?.pointed
    set(konstue) {
        this.konstue = konstue?.ptr as P?
    }

@ExperimentalForeignApi
public inline operator fun <reified T : CVariable> CPointer<T>.get(index: Long): T {
    konst offset = if (index == 0L) {
        0L // optimization for JVM impl which uses reflection for now.
    } else {
        index * sizeOf<T>()
    }
    return interpretPointed(this.rawValue + offset)
}

@ExperimentalForeignApi
public inline operator fun <reified T : CVariable> CPointer<T>.get(index: Int): T = this.get(index.toLong())

@ExperimentalForeignApi
@Suppress("NOTHING_TO_INLINE")
@JvmName("plus\$CPointer")
public inline operator fun <T : CPointerVarOf<*>> CPointer<T>?.plus(index: Long): CPointer<T>? =
        interpretCPointer(this.rawValue + index * pointerSize)

@ExperimentalForeignApi
@Suppress("NOTHING_TO_INLINE")
@JvmName("plus\$CPointer")
public inline operator fun <T : CPointerVarOf<*>> CPointer<T>?.plus(index: Int): CPointer<T>? =
        this + index.toLong()

@ExperimentalForeignApi
@Suppress("NOTHING_TO_INLINE")
public inline operator fun <T : CPointer<*>> CPointer<CPointerVarOf<T>>.get(index: Int): T? =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
@Suppress("NOTHING_TO_INLINE")
public inline operator fun <T : CPointer<*>> CPointer<CPointerVarOf<T>>.set(index: Int, konstue: T?) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
@Suppress("NOTHING_TO_INLINE")
public inline operator fun <T : CPointer<*>> CPointer<CPointerVarOf<T>>.get(index: Long): T? =
        (this + index)!!.pointed.konstue

@ExperimentalForeignApi
@Suppress("NOTHING_TO_INLINE")
public inline operator fun <T : CPointer<*>> CPointer<CPointerVarOf<T>>.set(index: Long, konstue: T?) {
    (this + index)!!.pointed.konstue = konstue
}

@ExperimentalForeignApi
public typealias CArrayPointer<T> = CPointer<T>
@ExperimentalForeignApi
public typealias CArrayPointerVar<T> = CPointerVar<T>

/**
 * The C function.
 */
@ExperimentalForeignApi
public class CFunction<T : Function<*>>(rawPtr: NativePtr) : CPointed(rawPtr)
