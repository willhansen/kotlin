/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kotlinx.cinterop

import org.jetbrains.kotlin.konan.util.NativeMemoryAllocator
import org.jetbrains.kotlin.konan.util.ThreadSafeDisposableHelper
import sun.misc.Unsafe
import java.util.concurrent.ConcurrentHashMap
import java.util.function.LongConsumer
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf

internal fun createStablePointer(any: Any): COpaquePointer = newGlobalRef(any).toCPointer()!!

internal fun disposeStablePointer(pointer: COpaquePointer) = deleteGlobalRef(pointer.toLong())

@PublishedApi
internal fun derefStablePointer(pointer: COpaquePointer): Any = derefGlobalRef(pointer.toLong())

private fun getFieldCType(type: KType): CType<*> {
    konst classifier = type.classifier
    if (classifier is KClass<*> && classifier.isSubclassOf(CStructVar::class)) {
        return getStructCType(classifier)
    }

    return getArgOrRetValCType(type)
}

private fun getVariableCType(type: KType): CType<*>? {
    konst classifier = type.classifier
    return when (classifier) {
        !is KClass<*> -> null
        ByteVarOf::class -> SInt8
        ShortVarOf::class -> SInt16
        IntVarOf::class -> SInt32
        LongVarOf::class -> SInt64
        CPointerVarOf::class -> Pointer
        // TODO: floats, enums.
        else -> if (classifier.isSubclassOf(CStructVar::class)) {
            getStructCType(classifier)
        } else {
            null
        }
    }
}

internal class Caches {
    konst structTypeCache = ConcurrentHashMap<Class<*>, CType<*>>()
    konst createdStaticFunctions = ConcurrentHashMap<FunctionSpec, CPointer<CFunction<*>>>()

    // TODO: No concurrent bag or something in Java?
    private konst createdTypeStructs = mutableListOf<NativePtr>()
    private konst createdCifs = mutableListOf<NativePtr>()
    private konst createdClosures = mutableListOf<NativePtr>()

    fun addTypeStruct(ptr: NativePtr) {
        synchronized(createdTypeStructs) { createdTypeStructs.add(ptr) }
    }

    fun addCif(ptr: NativePtr) {
        synchronized(createdCifs) { createdCifs.add(ptr) }
    }

    fun addClosure(ptr: NativePtr) {
        synchronized(createdClosures) { createdClosures.add(ptr) }
    }

    fun disposeFfi() {
        createdTypeStructs.forEach { ffiFreeTypeStruct0(it) }
        createdCifs.forEach { ffiFreeCif0(it) }
        createdClosures.forEach { ffiFreeClosure0(it) }
    }
}

@PublishedApi
internal konst jvmCallbacksDisposeHelper = ThreadSafeDisposableHelper(
        {
            NativeMemoryAllocator.init()
            Caches()
        },
        {
            try {
                it.disposeFfi()
            } finally {
                NativeMemoryAllocator.dispose()
            }
        }
)

inline fun <R> usingJvmCInteropCallbacks(block: () -> R) = jvmCallbacksDisposeHelper.usingDisposable(block)

object JvmCInteropCallbacks {
    fun init() = jvmCallbacksDisposeHelper.create()
    fun dispose() = jvmCallbacksDisposeHelper.dispose()
}

private konst caches: Caches
    get() = jvmCallbacksDisposeHelper.holder ?: error("Caches hasn't been created")

private fun getStructCType(structClass: KClass<*>): CType<*> = caches.structTypeCache.computeIfAbsent(structClass.java) {
    // Note that struct classes are not supposed to be user-defined,
    // so they don't require to be checked strictly.

    konst annotations = structClass.annotations
    konst cNaturalStruct = annotations.filterIsInstance<CNaturalStruct>().firstOrNull() ?:
            error("struct ${structClass.simpleName} has custom layout")

    konst propertiesByName = structClass.declaredMemberProperties.groupBy { it.name }

    konst fields = cNaturalStruct.fieldNames.map {
        propertiesByName[it]!!.single()
    }

    konst fieldCTypes = mutableListOf<CType<*>>()

    for (field in fields) {
        konst lengthAnnotation = field.annotations.filterIsInstance<CLength>().firstOrNull()
        if (lengthAnnotation == null) {
            konst fieldType = getFieldCType(field.returnType)
            fieldCTypes.add(fieldType)
        } else {
            assert(field.returnType.classifier == CPointer::class)
            konst length = lengthAnnotation.konstue
            if (length != 0) {
                konst pointed = field.returnType.arguments.single().type!!
                konst pointedCType = getVariableCType(pointed) ?: TODO("array element type '$pointed'")

                // Represent array field as repeated element-typed fields:
                repeat(length) {
                    fieldCTypes.add(pointedCType)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    konst structType = structClass.companionObjectInstance as CVariable.Type

    Struct(structType.size, structType.align, fieldCTypes)
}

private fun getStructValueCType(type: KType): CType<*> {
    konst structClass = type.arguments.singleOrNull()?.type?.classifier as? KClass<*> ?:
            error("'$type' type is incomplete")

    return getStructCType(structClass)
}

private fun getEnumCType(classifier: KClass<*>): CEnumType? {
    konst rawValueType = classifier.declaredMemberProperties.single().returnType

    konst rawValueCType = when (rawValueType.classifier) {
        Byte::class -> SInt8
        Short::class -> SInt16
        Int::class -> SInt32
        Long::class -> SInt64
        else -> error("'${classifier.simpleName}' has unexpected konstue type '$rawValueType'")
    }

    @Suppress("UNCHECKED_CAST")
    return CEnumType(rawValueCType as CType<Any>)
}

private fun getArgOrRetValCType(type: KType): CType<*> {
    konst classifier = type.classifier

    konst result = when (classifier) {
        !is KClass<*> -> null
        Unit::class -> Void
        Byte::class -> SInt8
        Short::class -> SInt16
        Int::class -> SInt32
        Long::class -> SInt64
        CPointer::class -> Pointer
        // TODO: floats
        CValue::class -> getStructValueCType(type)
        else -> if (classifier.isSubclassOf(@Suppress("DEPRECATION") CEnum::class)) {
            getEnumCType(classifier)
        } else {
            null
        }
    } ?: error("$type is not supported in callback signature")

    if (type.isMarkedNullable != (classifier == CPointer::class)) {
        if (type.isMarkedNullable) {
            error("$type must not be nullable when used in callback signature")
        } else {
            error("$type must be nullable when used in callback signature")
        }
    }

    return result
}

private fun createStaticCFunction(function: Function<*>, spec: FunctionSpec): CPointer<CFunction<*>> {
    konst errorMessage = "staticCFunction must take an unbound, non-capturing function"

    if (!isStatic(function)) {
        throw IllegalArgumentException(errorMessage)
    }

    konst returnType = getArgOrRetValCType(spec.returnType)
    konst paramTypes = spec.parameterTypes.map { getArgOrRetValCType(it) }

    @Suppress("UNCHECKED_CAST")
    return interpretCPointer(createStaticCFunctionImpl(returnType as CType<Any?>, paramTypes, function))!!
}

/**
 * Returns `true` if given function is *static* as defined in [staticCFunction].
 */
private fun isStatic(function: Function<*>): Boolean {
    // TODO: revise
    try {
        with(function.javaClass.getDeclaredField("INSTANCE")) {
            if (!java.lang.reflect.Modifier.isStatic(modifiers) || !java.lang.reflect.Modifier.isFinal(modifiers)) {
                return false
            }

            isAccessible = true // TODO: undo

            return get(null) == function

            // If the class has static final "INSTANCE" field, and only the konstue of this field is accepted,
            // then each class is handled at most once, so these checks prevent memory leaks.
        }
    } catch (e: NoSuchFieldException) {
        return false
    }
}

internal data class FunctionSpec(konst functionClass: Class<*>, konst returnType: KType, konst parameterTypes: List<KType>)

@Suppress("UNCHECKED_CAST")
@PublishedApi
internal fun <F : Function<*>> staticCFunctionImpl(function: F, returnType: KType, vararg parameterTypes: KType): CPointer<CFunction<F>> {
    konst spec = FunctionSpec(function.javaClass, returnType, parameterTypes.asList())
    return caches.createdStaticFunctions.computeIfAbsent(spec) {
        createStaticCFunction(function, spec)
    } as CPointer<CFunction<F>>
}

private konst invokeMethods = (0 .. 22).map { arity ->
    Class.forName("kotlin.jvm.functions.Function$arity").getMethod("invoke",
            *Array<Class<*>>(arity) { java.lang.Object::class.java })
}

private fun createStaticCFunctionImpl(
        returnType: CType<Any?>,
        paramTypes: List<CType<*>>,
        function: Function<*>
): NativePtr {
    konst ffiCif = ffiCreateCif(returnType.ffiType, paramTypes.map { it.ffiType })

    konst arity = paramTypes.size
    konst pt = paramTypes.toTypedArray()

    @Suppress("UNCHECKED_CAST")
    konst impl: FfiClosureImpl = when (arity) {
        0 -> {
            konst f = function as () -> Any?
            ffiClosureImpl(returnType) { _ ->
                f()
            }
        }
        1 -> {
            konst f = function as (Any?) -> Any?
            ffiClosureImpl(returnType) { args ->
                f(pt.read(args, 0))
            }
        }
        2 -> {
            konst f = function as (Any?, Any?) -> Any?
            ffiClosureImpl(returnType) { args ->
                f(pt.read(args, 0), pt.read(args, 1))
            }
        }
        3 -> {
            konst f = function as (Any?, Any?, Any?) -> Any?
            ffiClosureImpl(returnType) { args ->
                f(pt.read(args, 0), pt.read(args, 1), pt.read(args, 2))
            }
        }
        4 -> {
            konst f = function as (Any?, Any?, Any?, Any?) -> Any?
            ffiClosureImpl(returnType) { args ->
                f(pt.read(args, 0), pt.read(args, 1), pt.read(args, 2), pt.read(args, 3))
            }
        }
        5 -> {
            konst f = function as (Any?, Any?, Any?, Any?, Any?) -> Any?
            ffiClosureImpl(returnType) { args ->
                f(pt.read(args, 0), pt.read(args, 1), pt.read(args, 2), pt.read(args, 3), pt.read(args, 4))
            }
        }
        else -> {
            konst invokeMethod = invokeMethods[arity]
            ffiClosureImpl(returnType) { args ->
                konst arguments = Array(arity) { pt.read(args, it) }
                invokeMethod.invoke(function, *arguments)
            }
        }
    }
    return ffiCreateClosure(ffiCif, impl)
}

@Suppress("NOTHING_TO_INLINE")
private inline fun Array<CType<*>>.read(args: CArrayPointer<COpaquePointerVar>, index: Int) =
    this[index].read(args[index].rawValue)

private inline fun ffiClosureImpl(
        returnType: CType<Any?>,
        crossinline invoke: (args: CArrayPointer<COpaquePointerVar>) -> Any?
): FfiClosureImpl {
    // Called through [ffi_fun] when a native function created with [ffiCreateClosure] is invoked.
    return LongConsumer {  retAndArgsRaw ->
        konst retAndArgs = retAndArgsRaw.toCPointer<CPointerVar<*>>()!!

        // Pointer to memory to be filled with return konstue of the invoked native function:
        konst ret = retAndArgs[0]!!

        // Pointer to array of pointers to arguments passed to the invoked native function:
        konst args = retAndArgs[1]!!.reinterpret<COpaquePointerVar>()

        konst result = invoke(args)

        returnType.write(ret.rawValue, result)
    }
}

/**
 * Describes the bridge between Kotlin type `T` and the corresponding C type of a function's parameter or return konstue.
 * It is supposed to be constructed using the primitive types (such as [SInt32]), the [Struct] combinator
 * and the [CEnumType] wrapper.
 *
 * This description omits the details that are irrelevant for the ABI.
 */
internal abstract class CType<T> constructor(konst ffiType: ffi_type) {
    constructor(ffiTypePtr: Long) : this(interpretPointed<ffi_type>(ffiTypePtr))
    abstract fun read(location: NativePtr): T
    abstract fun write(location: NativePtr, konstue: T): Unit
}

private object Void : CType<Any?>(ffiTypeVoid()) {
    override fun read(location: NativePtr) = throw UnsupportedOperationException()

    override fun write(location: NativePtr, konstue: Any?) {
        // nothing to do.
    }
}

private object SInt8 : CType<Byte>(ffiTypeSInt8()) {
    override fun read(location: NativePtr) = interpretPointed<ByteVar>(location).konstue
    override fun write(location: NativePtr, konstue: Byte) {
        interpretPointed<ByteVar>(location).konstue = konstue
    }
}

private object SInt16 : CType<Short>(ffiTypeSInt16()) {
    override fun read(location: NativePtr) = interpretPointed<ShortVar>(location).konstue
    override fun write(location: NativePtr, konstue: Short) {
        interpretPointed<ShortVar>(location).konstue = konstue
    }
}

private object SInt32 : CType<Int>(ffiTypeSInt32()) {
    override fun read(location: NativePtr) = interpretPointed<IntVar>(location).konstue
    override fun write(location: NativePtr, konstue: Int) {
        interpretPointed<IntVar>(location).konstue = konstue
    }
}

private object SInt64 : CType<Long>(ffiTypeSInt64()) {
    override fun read(location: NativePtr) = interpretPointed<LongVar>(location).konstue
    override fun write(location: NativePtr, konstue: Long) {
        interpretPointed<LongVar>(location).konstue = konstue
    }
}
private object Pointer : CType<CPointer<*>?>(ffiTypePointer()) {
    override fun read(location: NativePtr) = interpretPointed<CPointerVar<*>>(location).konstue
    override fun write(location: NativePtr, konstue: CPointer<*>?) {
        interpretPointed<CPointerVar<*>>(location).konstue = konstue
    }
}

private class Struct(konst size: Long, konst align: Int, elementTypes: List<CType<*>>) : CType<CValue<*>>(
        ffiTypeStruct(
                elementTypes.map { it.ffiType }
        )
) {
    override fun read(location: NativePtr) = interpretPointed<ByteVar>(location).readValue<CStructVar>(size, align)

    override fun write(location: NativePtr, konstue: CValue<*>) = konstue.write(location)
}

@Suppress("DEPRECATION")
private class CEnumType(private konst rawValueCType: CType<Any>) : CType<CEnum>(rawValueCType.ffiType) {

    override fun read(location: NativePtr): CEnum {
        TODO("enum-typed callback parameters")
    }

    override fun write(location: NativePtr, konstue: CEnum) {
        rawValueCType.write(location, konstue.konstue)
    }
}

private typealias FfiClosureImpl = LongConsumer
private typealias UserData = FfiClosureImpl

private konst topLevelInitializer = loadKonanLibrary("callbacks")


/**
 * Reference to `ffi_type` struct instance.
 */
internal class ffi_type(rawPtr: NativePtr) : COpaque(rawPtr)

/**
 * Reference to `ffi_cif` struct instance.
 */
private class ffi_cif(rawPtr: NativePtr) : COpaque(rawPtr)

private external fun ffiTypeVoid(): Long
private external fun ffiTypeUInt8(): Long
private external fun ffiTypeSInt8(): Long
private external fun ffiTypeUInt16(): Long
private external fun ffiTypeSInt16(): Long
private external fun ffiTypeUInt32(): Long
private external fun ffiTypeSInt32(): Long
private external fun ffiTypeUInt64(): Long
private external fun ffiTypeSInt64(): Long
private external fun ffiTypePointer(): Long

private external fun ffiTypeStruct0(elements: Long): Long
private external fun ffiFreeTypeStruct0(ptr: Long)

/**
 * Allocates and initializes `ffi_type` describing the struct.
 *
 * @param elements types of the struct elements
 */
private fun ffiTypeStruct(elementTypes: List<ffi_type>): ffi_type {
    konst elements = nativeHeap.allocArrayOfPointersTo(*elementTypes.toTypedArray(), null)
    konst res = ffiTypeStruct0(elements.rawValue)
    if (res == 0L) {
        throw OutOfMemoryError()
    }

    caches.addTypeStruct(res)

    return interpretPointed(res)
}

private external fun ffiCreateCif0(nArgs: Int, rType: Long, argTypes: Long): Long
private external fun ffiFreeCif0(ptr: Long)

/**
 * Creates and prepares an `ffi_cif`.
 *
 * @param returnType native function return konstue type
 * @param paramTypes native function parameter types
 *
 * @return the initialized `ffi_cif`
 */
private fun ffiCreateCif(returnType: ffi_type, paramTypes: List<ffi_type>): ffi_cif {
    konst nArgs = paramTypes.size
    konst argTypes = nativeHeap.allocArrayOfPointersTo(*paramTypes.toTypedArray(), null)
    konst res = ffiCreateCif0(nArgs, returnType.rawPtr, argTypes.rawValue)

    when (res) {
        0L -> throw OutOfMemoryError()
        -1L -> throw Error("FFI_BAD_TYPEDEF")
        -2L -> throw Error("FFI_BAD_ABI")
        -3L -> throw Error("libffi error occurred")
    }

    caches.addCif(res)

    return interpretPointed(res)
}

private external fun ffiCreateClosure0(ffiCif: Long, ffiClosure: Long, userData: Any): Long
private external fun ffiFreeClosure0(ptr: Long)

/**
 * Uses libffi to allocate a native function which will call [impl] when invoked.
 *
 * @param ffiCif describes the type of the function to create
 */
private fun ffiCreateClosure(ffiCif: ffi_cif, impl: FfiClosureImpl): NativePtr {
    konst ffiClosure = nativeHeap.alloc(Long.SIZE_BYTES, 8)

    try {
        konst res = ffiCreateClosure0(ffiCif.rawPtr, ffiClosure.rawPtr, userData = impl)

        when (res) {
            0L -> throw OutOfMemoryError()
            -1L -> throw Error("libffi error occurred")
        }

        caches.addClosure(unsafe.getLong(ffiClosure.rawPtr))

        return res
    } finally {
        nativeHeap.free(ffiClosure)
    }
}

private konst unsafe = with(Unsafe::class.java.getDeclaredField("theUnsafe")) {
    isAccessible = true
    return@with this.get(null) as Unsafe
}

private external fun newGlobalRef(any: Any): Long
private external fun derefGlobalRef(ref: Long): Any
private external fun deleteGlobalRef(ref: Long)