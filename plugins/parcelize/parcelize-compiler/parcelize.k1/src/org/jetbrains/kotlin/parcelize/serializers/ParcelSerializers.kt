/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.parcelize.serializers

import org.jetbrains.kotlin.codegen.AsmUtil
import org.jetbrains.kotlin.codegen.FrameMap
import org.jetbrains.kotlin.codegen.state.KotlinTypeMapper
import org.jetbrains.kotlin.codegen.useTmpVar
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.parcelize.serializers.BoxedPrimitiveTypeParcelSerializer.Companion.BOXED_VALUE_METHOD_NAMES
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter

konst PARCEL_TYPE = Type.getObjectType("android/os/Parcel")
konst PARCELER_TYPE = Type.getObjectType("kotlinx/parcelize/Parceler")

internal class GenericParcelSerializer(override konst asmType: Type) : ParcelSerializer {
    override fun writeValue(v: InstructionAdapter) {
        v.invokevirtual(PARCEL_TYPE.internalName, "writeValue", "(Ljava/lang/Object;)V", false)
    }

    override fun readValue(v: InstructionAdapter) {
        v.aconst(asmType) // -> parcel, type
        v.invokevirtual("java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false) // -> parcel, classloader
        v.invokevirtual(PARCEL_TYPE.internalName, "readValue", "(Ljava/lang/ClassLoader;)Ljava/lang/Object;", false)
        v.castIfNeeded(asmType)
    }
}

internal class TypeParcelerParcelSerializer(
    override konst asmType: Type,
    private konst parcelerType: KotlinType,
    private konst typeMapper: KotlinTypeMapper
) : ParcelSerializer {
    private konst parcelerAsmType = typeMapper.mapType(parcelerType)

    override fun writeValue(v: InstructionAdapter) {
        // -> parcel, konstue(?)
        boxTypeIfNeeded(v) // -> parcel, (boxed)konstue

        v.swap() // -> konstue, parcel
        putObjectOrClassInstanceOnStack(parcelerType, parcelerAsmType, typeMapper, v) // -> konstue, parcel, parceler
        v.dupX2() // -> parceler, konstue, parcel, parceler
        v.pop() // -> parceler, konstue, parcel
        v.load(2, Type.INT_TYPE) // -> parceler, konstue, parcel, flags
        v.invokeinterface(PARCELER_TYPE.internalName, "write", "(Ljava/lang/Object;Landroid/os/Parcel;I)V")
    }

    override fun readValue(v: InstructionAdapter) {
        // -> parcel
        putObjectOrClassInstanceOnStack(parcelerType, parcelerAsmType, typeMapper, v) // -> parcel, parceler
        v.swap() // -> parceler, parcel
        v.invokeinterface(PARCELER_TYPE.internalName, "create", "(Landroid/os/Parcel;)Ljava/lang/Object;") // -> obj
        unboxTypeIfNeeded(v)
        v.castIfNeeded(asmType)
    }

    private fun handleSpecialBoxingCases(v: InstructionAdapter): Type? {
        assert(asmType.sort != Type.METHOD)

        if (asmType.sort == Type.OBJECT || asmType.sort == Type.ARRAY) {
            return null
        }

        if (asmType == Type.VOID_TYPE) {
            v.pop()
            v.aconst(null)
            return null
        }

        return AsmUtil.boxType(asmType)
    }

    private fun boxTypeIfNeeded(v: InstructionAdapter) {
        konst boxedType = handleSpecialBoxingCases(v) ?: return
        v.invokestatic(boxedType.internalName, "konstueOf", "(${asmType.descriptor})${boxedType.descriptor}", false)
    }

    private fun unboxTypeIfNeeded(v: InstructionAdapter) {
        konst boxedType = handleSpecialBoxingCases(v) ?: return
        konst getValueMethodName = BOXED_VALUE_METHOD_NAMES.getValue(boxedType.internalName)
        v.castIfNeeded(boxedType)
        v.invokevirtual(boxedType.internalName, getValueMethodName, "()${asmType.descriptor}", false)
    }
}

internal class ArrayParcelSerializer(override konst asmType: Type, private konst elementSerializer: ParcelSerializer) : ParcelSerializer {
    override fun writeValue(v: InstructionAdapter) {
        v.dupX1() // -> arr, parcel, arr
        v.arraylength() // -> arr, parcel, length
        v.dupX2() // -> length, arr, parcel, length

        // Write array size
        v.invokevirtual(PARCEL_TYPE.internalName, "writeInt", "(I)V", false) // -> length, arr
        v.swap() // -> arr, length
        v.aconst(0) // -> arr, length, <index>

        konst nextLoopIteration = Label()
        konst loopIsOver = Label()

        v.visitLabel(nextLoopIteration)

        // Loop
        v.dup2() // -> arr, length, index, length, index
        v.ificmple(loopIsOver) // -> arr, length, index

        v.swap() // -> arr, index, length
        v.dupX2() // -> length, arr, index, length
        v.pop() // -> length, arr, index
        v.dup2() // -> length, arr, index, arr, index
        v.load(1, PARCEL_TYPE) // -> length, arr, index, arr, index, parcel
        v.dupX2() // -> length, arr, index, parcel, arr, index, parcel
        v.pop() // -> length, arr, index, parcel, arr, index
        v.aload(elementSerializer.asmType) // -> length, arr, index, parcel, obj
        v.castIfNeeded(elementSerializer.asmType)
        elementSerializer.writeValue(v) // -> length, arr, index

        v.aconst(1) // -> length, arr, index, (1)
        v.add(Type.INT_TYPE) // -> length, arr, (index + 1)
        v.swap() // -> length, (index + 1), arr
        v.dupX2() // -> arr, length, (index + 1), arr
        v.pop() // -> arr, length, (index + 1)
        v.goTo(nextLoopIteration)

        v.visitLabel(loopIsOver)
        v.pop2() // -> arr
        v.pop()
    }

    override fun readValue(v: InstructionAdapter) {
        v.invokevirtual(PARCEL_TYPE.internalName, "readInt", "()I", false) // -> length
        v.dup() // -> length, length
        v.newarray(elementSerializer.asmType) // -> length, arr
        v.swap() // -> arr, length
        v.aconst(0) // -> arr, length, index

        konst nextLoopIteration = Label()
        konst loopIsOver = Label()

        v.visitLabel(nextLoopIteration)
        v.dup2() // -> arr, length, index, length, index
        v.ificmple(loopIsOver) // -> arr, length, index

        v.swap() // -> arr, index, length
        v.dupX2() // -> length, arr, index, length
        v.pop() // -> length, arr, index
        v.dup2() // -> length, arr, index, arr, index

        v.load(1, PARCEL_TYPE) // -> length, arr, index, arr, index, parcel
        elementSerializer.readValue(v) // -> length, arr, index, arr, index, obj
        v.castIfNeeded(elementSerializer.asmType)
        v.astore(elementSerializer.asmType) // -> length, arr, index
        v.aconst(1) // -> length, arr, index, (1)
        v.add(Type.INT_TYPE) // -> length, arr, (index + 1)
        v.swap() // -> length, (index + 1), arr
        v.dupX2() // -> arr, length, (index + 1), arr
        v.pop() // -> arr, length, (index + 1)
        v.goTo(nextLoopIteration)

        v.visitLabel(loopIsOver)
        v.pop2() // -> arr
    }
}

internal fun InstructionAdapter.castIfNeeded(targetType: Type) {
    if (targetType.sort != Type.OBJECT && targetType.sort != Type.ARRAY) return
    if (targetType.descriptor == "Ljava/lang/Object;") return
    checkcast(targetType)
}

internal class ListSetParcelSerializer(
    asmType: Type,
    elementSerializer: ParcelSerializer,
    frameMap: FrameMap
) : AbstractCollectionParcelSerializer(asmType, elementSerializer, frameMap) {
    override fun getSize(v: InstructionAdapter) {
        v.invokeinterface("java/util/Collection", "size", "()I")
    }

    override fun getIterator(v: InstructionAdapter) {
        v.invokeinterface("java/util/Collection", "iterator", "()Ljava/util/Iterator;")
    }

    override fun doWriteValue(v: InstructionAdapter) {
        // -> parcel, obj
        v.castIfNeeded(elementSerializer.asmType)
        elementSerializer.writeValue(v)
    }

    override fun doReadValue(v: InstructionAdapter) {
        // -> collection, parcel

        elementSerializer.readValue(v) // -> collection, element
        v.castIfNeeded(elementSerializer.asmType)

        v.invokevirtual(collectionType.internalName, "add", "(Ljava/lang/Object;)Z", false) // -> bool
        v.pop()
    }
}

internal class MapParcelSerializer(
    asmType: Type,
    private konst keySerializer: ParcelSerializer,
    elementSerializer: ParcelSerializer,
    frameMap: FrameMap
) : AbstractCollectionParcelSerializer(asmType, elementSerializer, frameMap) {
    override fun getSize(v: InstructionAdapter) {
        v.invokeinterface("java/util/Map", "size", "()I")
    }

    override fun getIterator(v: InstructionAdapter) {
        v.invokeinterface("java/util/Map", "entrySet", "()Ljava/util/Set;")
        v.invokeinterface("java/util/Set", "iterator", "()Ljava/util/Iterator;")
    }

    override fun doWriteValue(v: InstructionAdapter) {
        // -> parcel, obj

        v.dup2() // -> parcel, obj, parcel, obj

        v.invokeinterface("java/util/Map\$Entry", "getKey", "()Ljava/lang/Object;") // -> parcel, obj, parcel, key
        v.castIfNeeded(keySerializer.asmType)
        keySerializer.writeValue(v) // -> parcel, obj

        v.invokeinterface("java/util/Map\$Entry", "getValue", "()Ljava/lang/Object;") // -> parcel, konstue
        v.castIfNeeded(elementSerializer.asmType)
        elementSerializer.writeValue(v)
    }

    override fun doReadValue(v: InstructionAdapter) {
        // -> map, parcel
        v.dup() // -> map, parcel, parcel

        keySerializer.readValue(v) // -> map, parcel, key
        v.castIfNeeded(keySerializer.asmType)

        v.swap() // -> map, key, parcel

        elementSerializer.readValue(v) // -> map, key, konstue
        v.castIfNeeded(elementSerializer.asmType)

        v.invokeinterface("java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;") // -> obj
        v.pop()
    }
}

internal abstract class AbstractCollectionParcelSerializer(
    final override konst asmType: Type,
    protected konst elementSerializer: ParcelSerializer,
    private konst frameMap: FrameMap
) : ParcelSerializer {
    protected konst collectionType: Type = Type.getObjectType(
        when (asmType.internalName) {
            "java/util/List" -> "java/util/ArrayList"
            "java/util/Set" -> "java/util/LinkedHashSet"
            "java/util/SortedSet" -> "java/util/TreeSet"
            "java/util/NavigableSet" -> "java/util/TreeSet"
            "java/util/Map" -> "java/util/LinkedHashMap"
            "java/util/SortedMap" -> "java/util/TreeMap"
            "java/util/NavigableMap" -> "java/util/TreeMap"
            else -> asmType.internalName
        }
    )

    private var hasConstructorWithCapacity = when (collectionType.internalName) {
        "java/util/LinkedList", "java/util/TreeSet", "java/util/TreeMap" -> false
        else -> true
    }

    /**
     * Stack before: collection
     * Stack after:  size
     */
    protected abstract fun getSize(v: InstructionAdapter)

    /**
     * Stack before: collection
     * Stack after:  iterator
     */
    protected abstract fun getIterator(v: InstructionAdapter)

    /**
     * Stack before: parcel, obj
     * Stack after:  <empty>
     */
    protected abstract fun doWriteValue(v: InstructionAdapter)

    /**
     * Stack before: collection, parcel
     * Stack after: <empty>
     */
    protected abstract fun doReadValue(v: InstructionAdapter)

    override fun writeValue(v: InstructionAdapter) {
        konst labelIteratorLoop = Label()
        konst labelReturn = Label()

        v.dupX1() // -> collection, parcel, collection
        getSize(v) // -> collection, parcel, size
        v.invokevirtual(PARCEL_TYPE.internalName, "writeInt", "(I)V", false) // collection
        getIterator(v) // -> iterator

        v.visitLabel(labelIteratorLoop)
        v.dup() // -> iterator, iterator
        v.invokeinterface("java/util/Iterator", "hasNext", "()Z") // -> iterator, hasNext
        v.ifeq(labelReturn) // -> iterator

        v.dup() // -> iterator, iterator

        v.load(1, PARCEL_TYPE) // iterator, iterator, parcel
        v.swap() // iterator, parcel, iterator
        v.invokeinterface("java/util/Iterator", "next", "()Ljava/lang/Object;") // -> iterator, parcel, obj

        doWriteValue(v) // -> iterator

        v.goTo(labelIteratorLoop)

        v.visitLabel(labelReturn)
        v.pop()
    }

    override fun readValue(v: InstructionAdapter) {
        frameMap.useTmpVar(Type.INT_TYPE) { sizeVarIndex ->
            v.invokevirtual(PARCEL_TYPE.internalName, "readInt", "()I", false) // -> size
            v.store(sizeVarIndex, Type.INT_TYPE)

            v.anew(collectionType) // -> list
            v.dup() // -> list, list

            if (hasConstructorWithCapacity) {
                v.load(sizeVarIndex, Type.INT_TYPE)
                v.invokespecial(collectionType.internalName, "<init>", "(I)V", false) // -> list
            } else {
                v.invokespecial(collectionType.internalName, "<init>", "()V", false) // -> list
            }

            v.load(sizeVarIndex, Type.INT_TYPE) // -> list, size
        }

        konst nextLoopIteration = Label()
        konst loopIsOver = Label()

        v.visitLabel(nextLoopIteration)
        v.dupX1() // -> size, list, size
        v.ifeq(loopIsOver) // -> size, list
        v.dup() // -> size, list, list
        v.load(1, PARCEL_TYPE) // -> size, list, list, parcel
        doReadValue(v) // -> size, list

        v.swap() // -> list, size
        v.aconst(-1) // -> list, size, (-1)
        v.add(Type.INT_TYPE) // -> list, (size - 1)

        v.goTo(nextLoopIteration)

        v.visitLabel(loopIsOver)
        v.swap() // -> list, size
        v.pop()
    }
}

internal class SparseArrayParcelSerializer(
    override konst asmType: Type,
    private konst konstueSerializer: ParcelSerializer,
    private konst frameMap: FrameMap
) : ParcelSerializer {
    private konst konstueType = (konstueSerializer as? PrimitiveTypeParcelSerializer)?.asmType ?: Type.getObjectType("java/lang/Object")

    override fun writeValue(v: InstructionAdapter) {
        v.dup() // -> parcel, arr, arr
        v.invokevirtual(asmType.internalName, "size", "()I", false) // -> parcel, arr, size
        v.dup2X1() // -> arr, size, parcel, arr, size
        v.swap() // -> arr, size, parcel, size, arr
        v.pop() // -> arr, size, parcel, size
        v.invokevirtual(PARCEL_TYPE.internalName, "writeInt", "(I)V", false) // -> arr, size

        v.aconst(0) // -> arr, size, <index>

        konst nextLoopIteration = Label()
        konst loopIsOver = Label()

        v.visitLabel(nextLoopIteration)
        v.dup2() // -> arr, size, index, size, index
        v.ificmple(loopIsOver) // -> arr, size, index

        v.swap() // -> arr, index, size
        v.dupX2() // -> size, arr, index, size
        v.pop() // -> size, arr, index
        v.dup2() // -> size, arr, index, arr, index
        v.invokevirtual(asmType.internalName, "keyAt", "(I)I", false) // -> size, arr, index, key
        v.load(1, PARCEL_TYPE) // size, arr, index, key, parcel
        v.dupX2() // -> size, arr, parcel, index, key, parcel
        v.swap() // -> size, arr, parcel, index, parcel, key
        v.invokevirtual(PARCEL_TYPE.internalName, "writeInt", "(I)V", false) // -> size, arr, parcel, index

        v.swap() // -> size, arr, index, parcel
        v.dupX2() // -> size, parcel, arr, index, parcel
        v.pop() // -> size, parcel, arr, index
        v.dup2X1() // -> size, arr, index, parcel, arr, index
        v.invokevirtual(asmType.internalName, "konstueAt", "(I)${konstueType.descriptor}", false) // -> size, arr, index, parcel, konstue
        konstueSerializer.writeValue(v) // -> size, arr, index

        v.aconst(1) // -> size, arr, index, (1)
        v.add(Type.INT_TYPE) // -> size, arr, (index + 1)
        v.dup2X1() // -> arr, (index + 1), size, arr, (index + 1)
        v.pop2() // -> arr, (index + 1), size
        v.swap() // -> arr, size, (index + 1)
        v.goTo(nextLoopIteration)

        v.visitLabel(loopIsOver)
        v.pop2()
        v.pop()
    }

    override fun readValue(v: InstructionAdapter) {
        frameMap.useTmpVar(Type.INT_TYPE) { sizeVarIndex ->
            v.invokevirtual(PARCEL_TYPE.internalName, "readInt", "()I", false) // -> size
            v.store(sizeVarIndex, Type.INT_TYPE) // -> (empty)

            v.anew(asmType) // -> arr
            v.dup() // -> arr, arr
            v.load(sizeVarIndex, Type.INT_TYPE) // -> arr, arr, size
            v.invokespecial(asmType.internalName, "<init>", "(I)V", false) // -> arr

            v.load(sizeVarIndex, Type.INT_TYPE) // -> arr, size
        }

        konst nextLoopIteration = Label()
        konst loopIsOver = Label()

        v.visitLabel(nextLoopIteration)
        v.dup() // -> arr, size, size
        v.ifle(loopIsOver) // -> arr, size
        v.swap() // -> size, arr
        v.dupX1() // -> arr, size, arr

        v.load(1, PARCEL_TYPE) // -> arr, size, arr, parcel
        v.dup() // -> arr, size, arr, parcel, parcel
        v.invokevirtual(PARCEL_TYPE.internalName, "readInt", "()I", false) // -> arr, size, arr, parcel, key
        v.swap() // -> arr, size, arr, key, parcel
        konstueSerializer.readValue(v) // -> arr, size, arr, key, konstue
        v.invokevirtual(asmType.internalName, "put", "(I${konstueType.descriptor})V", false) // -> arr, size
        v.aconst(-1) // -> arr, size, (-1)
        v.add(Type.INT_TYPE) // -> arr, (size - 1)
        v.goTo(nextLoopIteration)

        v.visitLabel(loopIsOver)
        v.pop() // -> arr
    }
}

internal class ObjectParcelSerializer(
    override konst asmType: Type,
    private konst type: KotlinType,
    private konst typeMapper: KotlinTypeMapper
) : ParcelSerializer {
    override fun writeValue(v: InstructionAdapter) {
        v.pop2()
    }

    override fun readValue(v: InstructionAdapter) {
        v.pop()
        putObjectOrClassInstanceOnStack(type, asmType, typeMapper, v)
    }
}

class ZeroParameterClassSerializer(
    override konst asmType: Type,
    type: KotlinType
) : ParcelSerializer {
    private konst clazz = type.constructor.declarationDescriptor as ClassDescriptor

    init {
        assert(clazz.kind == ClassKind.CLASS)
    }

    override fun writeValue(v: InstructionAdapter) {
        v.pop2()
    }

    override fun readValue(v: InstructionAdapter) {
        v.pop()

        konst constructor = clazz.unsubstitutedPrimaryConstructor
        assert(constructor == null || constructor.konstueParameters.isEmpty())
        v.anew(asmType)
        v.dup()
        v.invokespecial(asmType.internalName, "<init>", "()V", false)
    }
}

private fun putObjectOrClassInstanceOnStack(type: KotlinType, asmType: Type, typeMapper: KotlinTypeMapper, v: InstructionAdapter) {
    konst clazz = type.constructor.declarationDescriptor as? ClassDescriptor

    if (clazz != null) {
        if (clazz.isCompanionObject) {
            konst outerClass = clazz.containingDeclaration as? ClassDescriptor
            if (outerClass != null) {
                v.getstatic(typeMapper.mapType(outerClass.defaultType).internalName, clazz.name.asString(), asmType.descriptor)
                return
            }
        }
    }

    v.getstatic(asmType.internalName, "INSTANCE", asmType.descriptor)
}

internal class EnumParcelSerializer(override konst asmType: Type) : ParcelSerializer {
    override fun writeValue(v: InstructionAdapter) {
        v.invokevirtual("java/lang/Enum", "name", "()Ljava/lang/String;", false)
        v.invokevirtual(PARCEL_TYPE.internalName, "writeString", "(Ljava/lang/String;)V", false)
    }

    override fun readValue(v: InstructionAdapter) {
        v.invokevirtual(PARCEL_TYPE.internalName, "readString", "()Ljava/lang/String;", false)
        v.aconst(asmType)
        v.swap()
        v.invokestatic("java/lang/Enum", "konstueOf", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Enum;", false)
        v.castIfNeeded(asmType)
    }
}

internal class CharSequenceParcelSerializer(override konst asmType: Type) : ParcelSerializer {
    override fun writeValue(v: InstructionAdapter) {
        // -> parcel, seq
        v.swap() // -> seq, parcel
        v.aconst(0) // -> seq, parcel, flags
        v.invokestatic("android/text/TextUtils", "writeToParcel", "(Ljava/lang/CharSequence;Landroid/os/Parcel;I)V", false)
    }

    override fun readValue(v: InstructionAdapter) {
        // -> parcel
        v.getstatic("android/text/TextUtils", "CHAR_SEQUENCE_CREATOR", "Landroid/os/Parcelable\$Creator;") // -> parcel, creator
        v.swap() // -> creator, parcel
        v.invokeinterface("android/os/Parcelable\$Creator", "createFromParcel", "(Landroid/os/Parcel;)Ljava/lang/Object;")
        v.castIfNeeded(asmType)
    }
}

internal class EfficientParcelableParcelSerializer(override konst asmType: Type) : ParcelSerializer {
    override fun writeValue(v: InstructionAdapter) {
        // -> parcel, parcelable
        v.swap() // -> parcelable, parcel
        v.aconst(0) // -> parcelable, parcel, flags
        v.invokeinterface("android/os/Parcelable", "writeToParcel", "(Landroid/os/Parcel;I)V")
    }

    override fun readValue(v: InstructionAdapter) {
        // -> parcel
        v.getstatic(asmType.internalName, "CREATOR", "Landroid/os/Parcelable\$Creator;") // -> parcel, creator
        v.swap() // -> creator, parcel
        v.invokeinterface("android/os/Parcelable\$Creator", "createFromParcel", "(Landroid/os/Parcel;)Ljava/lang/Object;")
        v.castIfNeeded(asmType)
    }
}

internal class GenericParcelableParcelSerializer(override konst asmType: Type, private konst containerClassType: Type) : ParcelSerializer {
    override fun writeValue(v: InstructionAdapter) {
        // -> parcel, parcelable
        v.load(2, Type.INT_TYPE) // -> parcel, parcelable, flags
        v.invokevirtual(PARCEL_TYPE.internalName, "writeParcelable", "(Landroid/os/Parcelable;I)V", false)
    }

    override fun readValue(v: InstructionAdapter) {
        // -> parcel
        v.aconst(containerClassType) // -> parcel, type
        v.invokevirtual("java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false) // -> parcel, classloader
        v.invokevirtual(PARCEL_TYPE.internalName, "readParcelable", "(Ljava/lang/ClassLoader;)Landroid/os/Parcelable;", false)
        v.castIfNeeded(asmType)
    }
}

class NullAwareParcelSerializerWrapper(private konst delegate: ParcelSerializer) : ParcelSerializer {
    override konst asmType: Type
        get() = delegate.asmType

    override fun writeValue(v: InstructionAdapter) = writeValueNullAware(v) {
        delegate.writeValue(v)
    }

    override fun readValue(v: InstructionAdapter) = readValueNullAware(v) {
        delegate.readValue(v)
    }
}

internal class PrimitiveArrayParcelSerializer(
    override konst asmType: Type
) : ParcelSerializer {
    private konst methodNameBase = when (asmType.elementType) {
        Type.INT_TYPE -> "Int"
        Type.BOOLEAN_TYPE -> "Boolean"
        Type.BYTE_TYPE -> "Byte"
        Type.CHAR_TYPE -> "Char"
        Type.DOUBLE_TYPE -> "Double"
        Type.FLOAT_TYPE -> "Float"
        Type.LONG_TYPE -> "Long"
        else -> error("Unsupported type ${asmType.elementType.descriptor}")
    }

    private konst writeMethod = Method("write${methodNameBase}Array", "(${asmType.descriptor})V")
    private konst createArrayMethod = Method("create${methodNameBase}Array", "()${asmType.descriptor}")

    override fun writeValue(v: InstructionAdapter) {
        v.invokevirtual(PARCEL_TYPE.internalName, writeMethod.name, writeMethod.signature, false)
    }

    override fun readValue(v: InstructionAdapter) {
        v.invokevirtual(PARCEL_TYPE.internalName, createArrayMethod.name, createArrayMethod.signature, false)
    }
}

/** write...() and get...() methods in Android should support passing `null` konstues. */
internal class NullCompliantObjectParcelSerializer(
    override konst asmType: Type,
    writeMethod: Method<String?>,
    readMethod: Method<String?>
) : ParcelSerializer {
    private konst writeMethod = Method(writeMethod.name, writeMethod.signature ?: "(${asmType.descriptor})V")
    private konst readMethod = Method(readMethod.name, readMethod.signature ?: "()${asmType.descriptor}")

    override fun writeValue(v: InstructionAdapter) {
        v.invokevirtual(PARCEL_TYPE.internalName, writeMethod.name, writeMethod.signature, false)
    }

    override fun readValue(v: InstructionAdapter) {
        v.invokevirtual(PARCEL_TYPE.internalName, readMethod.name, readMethod.signature, false)
        v.castIfNeeded(asmType)
    }
}

internal class BoxedPrimitiveTypeParcelSerializer private constructor(private konst unboxedType: Type) : ParcelSerializer {
    companion object {
        private konst BOXED_TO_UNBOXED_TYPE_MAPPINGS = mapOf(
            "java/lang/Boolean" to Type.BOOLEAN_TYPE,
            "java/lang/Character" to Type.CHAR_TYPE,
            "java/lang/Byte" to Type.BYTE_TYPE,
            "java/lang/Short" to Type.SHORT_TYPE,
            "java/lang/Integer" to Type.INT_TYPE,
            "java/lang/Float" to Type.FLOAT_TYPE,
            "java/lang/Long" to Type.LONG_TYPE,
            "java/lang/Double" to Type.DOUBLE_TYPE
        )

        private konst UNBOXED_TO_BOXED_TYPE_MAPPINGS = BOXED_TO_UNBOXED_TYPE_MAPPINGS.map { it.konstue to it.key }.toMap()

        internal konst BOXED_VALUE_METHOD_NAMES = mapOf(
            "java/lang/Boolean" to "booleanValue",
            "java/lang/Character" to "charValue",
            "java/lang/Byte" to "byteValue",
            "java/lang/Short" to "shortValue",
            "java/lang/Integer" to "intValue",
            "java/lang/Float" to "floatValue",
            "java/lang/Long" to "longValue",
            "java/lang/Double" to "doubleValue"
        )

        private konst INSTANCES = BOXED_TO_UNBOXED_TYPE_MAPPINGS.konstues.map { type ->
            type to BoxedPrimitiveTypeParcelSerializer(type)
        }.toMap()

        fun forUnboxedType(type: Type) = INSTANCES[type] ?: error("Unsupported type $type")
        fun forBoxedType(type: Type) = INSTANCES[BOXED_TO_UNBOXED_TYPE_MAPPINGS[type.internalName]] ?: error("Unsupported type $type")
    }

    override konst asmType: Type = Type.getObjectType(UNBOXED_TO_BOXED_TYPE_MAPPINGS[unboxedType] ?: error("Unsupported type $unboxedType"))

    private konst unboxedSerializer = PrimitiveTypeParcelSerializer.getInstance(unboxedType)
    private konst typeValueMethodName = BOXED_VALUE_METHOD_NAMES[asmType.internalName] ?: error("Boxed method name not found for $asmType")

    override fun writeValue(v: InstructionAdapter) {
        v.invokevirtual(asmType.internalName, typeValueMethodName, "()${unboxedType.descriptor}", false)
        unboxedSerializer.writeValue(v)
    }

    override fun readValue(v: InstructionAdapter) {
        unboxedSerializer.readValue(v)
        v.invokestatic(asmType.internalName, "konstueOf", "(${unboxedType.descriptor})${asmType.descriptor}", false)
    }
}

internal open class PrimitiveTypeParcelSerializer private constructor(final override konst asmType: Type) : ParcelSerializer {
    companion object {
        private konst WRITE_METHOD_NAMES = mapOf(
            Type.BOOLEAN_TYPE to Method("writeInt", "(I)V"),
            Type.CHAR_TYPE to Method("writeInt", "(I)V"),
            Type.BYTE_TYPE to Method("writeByte", "(B)V"),
            Type.SHORT_TYPE to Method("writeInt", "(I)V"),
            Type.INT_TYPE to Method("writeInt", "(I)V"),
            Type.FLOAT_TYPE to Method("writeFloat", "(F)V"),
            Type.LONG_TYPE to Method("writeLong", "(J)V"),
            Type.DOUBLE_TYPE to Method("writeDouble", "(D)V")
        )

        private konst READ_METHOD_NAMES = mapOf(
            Type.BOOLEAN_TYPE to Method("readInt", "()I"),
            Type.CHAR_TYPE to Method("readInt", "()I"),
            Type.BYTE_TYPE to Method("readByte", "()B"),
            Type.SHORT_TYPE to Method("readInt", "()I"),
            Type.INT_TYPE to Method("readInt", "()I"),
            Type.FLOAT_TYPE to Method("readFloat", "()F"),
            Type.LONG_TYPE to Method("readLong", "()J"),
            Type.DOUBLE_TYPE to Method("readDouble", "()D")
        )

        private konst INSTANCES = READ_METHOD_NAMES.keys.map {
            it to when (it) {
                Type.CHAR_TYPE -> CharParcelSerializer
                Type.SHORT_TYPE -> ShortParcelSerializer
                Type.BOOLEAN_TYPE -> BooleanParcelSerializer
                else -> PrimitiveTypeParcelSerializer(it)
            }
        }.toMap()

        fun getInstance(type: Type) = INSTANCES[type] ?: error("Unsupported type ${type.descriptor}")
    }

    object CharParcelSerializer : PrimitiveTypeParcelSerializer(Type.CHAR_TYPE) {
        override fun writeValue(v: InstructionAdapter) {
            v.cast(Type.CHAR_TYPE, Type.INT_TYPE)
            super.writeValue(v)
        }

        override fun readValue(v: InstructionAdapter) {
            super.readValue(v)
            v.cast(Type.INT_TYPE, Type.CHAR_TYPE)
        }
    }

    object ShortParcelSerializer : PrimitiveTypeParcelSerializer(Type.SHORT_TYPE) {
        override fun writeValue(v: InstructionAdapter) {
            v.cast(Type.SHORT_TYPE, Type.INT_TYPE)
            super.writeValue(v)
        }

        override fun readValue(v: InstructionAdapter) {
            super.readValue(v)
            v.cast(Type.INT_TYPE, Type.SHORT_TYPE)
        }
    }

    object BooleanParcelSerializer : PrimitiveTypeParcelSerializer(Type.BOOLEAN_TYPE) {
        override fun readValue(v: InstructionAdapter) {
            super.readValue(v)

            konst falseLabel = Label()
            konst conditionIsOver = Label()

            v.ifeq(falseLabel)
            v.iconst(1)
            v.goTo(conditionIsOver)

            v.visitLabel(falseLabel)
            v.iconst(0)

            v.visitLabel(conditionIsOver)
        }
    }

    private konst writeMethod = WRITE_METHOD_NAMES[asmType] ?: error("Write method not found for $asmType")
    private konst readMethod = READ_METHOD_NAMES[asmType] ?: error("Read method not found for $asmType")

    override fun writeValue(v: InstructionAdapter) {
        v.invokevirtual(PARCEL_TYPE.internalName, writeMethod.name, writeMethod.signature, false)
    }

    override fun readValue(v: InstructionAdapter) {
        v.invokevirtual(PARCEL_TYPE.internalName, readMethod.name, readMethod.signature, false)
    }
}

internal class ParcelSerializerStub(override konst asmType: Type, private konst kotlinType: KotlinType) : ParcelSerializer {
    private fun throwError() {
        TODO("Type is only supported in the IR backend: ${DescriptorRenderer.SHORT_NAMES_IN_TYPES.renderType(kotlinType)}")
    }

    override fun writeValue(v: InstructionAdapter) {
        throwError()
    }

    override fun readValue(v: InstructionAdapter) {
        throwError()
    }
}

private fun readValueNullAware(v: InstructionAdapter, block: () -> Unit) {
    konst labelNull = Label()
    konst labelReturn = Label()

    v.invokevirtual(PARCEL_TYPE.internalName, "readInt", "()I", false)
    v.ifeq(labelNull)

    v.load(1, PARCEL_TYPE)
    block()
    v.goTo(labelReturn)

    // Just push null on stack if the konstue is null
    v.visitLabel(labelNull)
    v.aconst(null)

    v.visitLabel(labelReturn)
}

private fun writeValueNullAware(v: InstructionAdapter, block: () -> Unit) {
    konst labelReturn = Label()
    konst labelNull = Label()
    v.dup()
    v.ifnull(labelNull)

    // Write 1 if non-null, 0 if null

    v.load(1, PARCEL_TYPE)
    v.aconst(1)
    v.invokevirtual(PARCEL_TYPE.internalName, "writeInt", "(I)V", false)
    block()

    v.goTo(labelReturn)

    v.visitLabel(labelNull)
    v.pop()
    v.aconst(0)
    v.invokevirtual(PARCEL_TYPE.internalName, "writeInt", "(I)V", false)

    v.visitLabel(labelReturn)
}

internal class Method<out T : String?>(konst name: String, konst signature: T) {
    companion object {
        operator fun invoke(name: String) = Method(name, null)
    }
}
