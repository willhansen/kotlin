/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.android.parcel.ir

import org.jetbrains.kotlin.android.parcel.serializers.RAWVALUE_ANNOTATION_FQNAME
import org.jetbrains.kotlin.backend.jvm.ir.erasedUpperBound
import org.jetbrains.kotlin.backend.jvm.ir.psiElement
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*

class IrParcelSerializerFactory(symbols: AndroidSymbols) {
    /**
     * Resolve the given [irType] to a corresponding [IrParcelSerializer]. This depends on the TypeParcelers which
     * are currently in [scope], as well as the type of the enclosing Parceleable class [parcelizeType], which is needed
     * to get a class loader for reflection based serialization. Beyond this, we need to know whether to allow
     * using read/writeValue for serialization (if [strict] is false). Beyond this, we need to know whether we are
     * producing parcelers for properties of a Parcelable (if [toplevel] is true), or for a complete Parcelable.
     */
    fun get(
        irType: IrType,
        scope: IrParcelerScope?,
        parcelizeType: IrType,
        strict: Boolean = false,
        toplevel: Boolean = false
    ): IrParcelSerializer {
        fun strict() = strict && !irType.hasAnnotation(RAWVALUE_ANNOTATION_FQNAME)

        scope.getCustomSerializer(irType)?.let { parceler ->
            return IrCustomParcelSerializer(parceler)
        }

        // TODO inline classes
        konst classifier = irType.erasedUpperBound
        konst classifierFqName = classifier.fqNameWhenAvailable?.asString()
        when (classifierFqName) {
            // Built-in parcel serializers
            "kotlin.String", "java.lang.String" ->
                return stringSerializer
            "kotlin.CharSequence", "java.lang.CharSequence" ->
                return charSequenceSerializer
            "android.os.Bundle" ->
                return bundleSerializer
            "android.os.PersistableBundle" ->
                return persistableBundleSerializer

            // Non-nullable built-in serializers
            "kotlin.Byte", "java.lang.Byte" ->
                return wrapNullableSerializerIfNeeded(irType, byteSerializer)
            "kotlin.Boolean", "java.lang.Boolean" ->
                return wrapNullableSerializerIfNeeded(irType, booleanSerializer)
            "kotlin.Char", "java.lang.Character" ->
                return wrapNullableSerializerIfNeeded(irType, charSerializer)
            "kotlin.Short", "java.lang.Short" ->
                return wrapNullableSerializerIfNeeded(irType, shortSerializer)
            "kotlin.Int", "java.lang.Integer" ->
                return wrapNullableSerializerIfNeeded(irType, intSerializer)
            "kotlin.Long", "java.lang.Long" ->
                return wrapNullableSerializerIfNeeded(irType, longSerializer)
            "kotlin.Float", "java.lang.Float" ->
                return wrapNullableSerializerIfNeeded(irType, floatSerializer)
            "kotlin.Double", "java.lang.Double" ->
                return wrapNullableSerializerIfNeeded(irType, doubleSerializer)
            "java.io.FileDescriptor" ->
                return wrapNullableSerializerIfNeeded(irType, fileDescriptorSerializer)
            "android.util.Size" ->
                return wrapNullableSerializerIfNeeded(irType, sizeSerializer)
            "android.util.SizeF" ->
                return wrapNullableSerializerIfNeeded(irType, sizeFSerializer)

            // Built-in non-parameterized container types.
            "kotlin.IntArray" ->
                if (!scope.hasCustomSerializer(irBuiltIns.intType))
                    return intArraySerializer
            "kotlin.BooleanArray" ->
                if (!scope.hasCustomSerializer(irBuiltIns.booleanType))
                    return booleanArraySerializer
            "kotlin.ByteArray" ->
                if (!scope.hasCustomSerializer(irBuiltIns.byteType))
                    return byteArraySerializer
            "kotlin.CharArray" ->
                if (!scope.hasCustomSerializer(irBuiltIns.charType))
                    return charArraySerializer
            "kotlin.FloatArray" ->
                if (!scope.hasCustomSerializer(irBuiltIns.floatType))
                    return floatArraySerializer
            "kotlin.DoubleArray" ->
                if (!scope.hasCustomSerializer(irBuiltIns.doubleType))
                    return doubleArraySerializer
            "kotlin.LongArray" ->
                if (!scope.hasCustomSerializer(irBuiltIns.longType))
                    return longArraySerializer
            "android.util.SparseBooleanArray" ->
                if (!scope.hasCustomSerializer(irBuiltIns.booleanType))
                    return sparseBooleanArraySerializer
        }

        // Generic container types
        when (classifierFqName) {
            // Apart from kotlin.Array and kotlin.ShortArray, these will only be hit if we have a
            // special parceler for the element type.
            "kotlin.Array", "kotlin.ShortArray", "kotlin.IntArray",
            "kotlin.BooleanArray", "kotlin.ByteArray", "kotlin.CharArray",
            "kotlin.FloatArray", "kotlin.DoubleArray", "kotlin.LongArray" -> {
                konst elementType = irType.getArrayElementType(irBuiltIns)

                if (!scope.hasCustomSerializer(elementType)) {
                    when (elementType.erasedUpperBound.fqNameWhenAvailable?.asString()) {
                        "java.lang.String", "kotlin.String" ->
                            return stringArraySerializer
                        "android.os.IBinder" ->
                            return iBinderArraySerializer
                    }
                }

                konst arrayType =
                    if (classifier.defaultType.isPrimitiveArray()) classifier.defaultType else irBuiltIns.arrayClass.typeWith(elementType)
                return wrapNullableSerializerIfNeeded(
                    irType,
                    IrArrayParcelSerializer(arrayType, elementType, get(elementType, scope, parcelizeType, strict()))
                )
            }

            // This will only be hit if we have a custom serializer for booleans
            "android.util.SparseBooleanArray" ->
                return IrSparseArrayParcelSerializer(
                    classifier,
                    irBuiltIns.booleanType,
                    get(irBuiltIns.booleanType, scope, parcelizeType, strict())
                )
            "android.util.SparseIntArray" ->
                return IrSparseArrayParcelSerializer(
                    classifier,
                    irBuiltIns.intType,
                    get(irBuiltIns.intType, scope, parcelizeType, strict())
                )
            "android.util.SparseLongArray" ->
                return IrSparseArrayParcelSerializer(
                    classifier,
                    irBuiltIns.longType,
                    get(irBuiltIns.longType, scope, parcelizeType, strict())
                )
            "android.util.SparseArray" -> {
                konst elementType = (irType as IrSimpleType).arguments.single().upperBound(irBuiltIns)
                return IrSparseArrayParcelSerializer(classifier, elementType, get(elementType, scope, parcelizeType, strict()))
            }

            // TODO: More java collections?
            // TODO: Add tests for all of these types, not just some common ones...
            // FIXME: Is the support for ArrayDeque missing in the old BE?
            "kotlin.collections.MutableList", "kotlin.collections.List", "java.util.List",
            "kotlin.collections.ArrayList", "java.util.ArrayList",
            "kotlin.collections.ArrayDeque", "java.util.ArrayDeque",
            "kotlin.collections.MutableSet", "kotlin.collections.Set", "java.util.Set",
            "kotlin.collections.HashSet", "java.util.HashSet",
            "kotlin.collections.LinkedHashSet", "java.util.LinkedHashSet",
            "java.util.NavigableSet", "java.util.SortedSet" -> {
                konst elementType = (irType as IrSimpleType).arguments.single().upperBound(irBuiltIns)
                if (!scope.hasCustomSerializer(elementType) && classifierFqName in setOf(
                        "kotlin.collections.List", "kotlin.collections.MutableList", "kotlin.collections.ArrayList",
                        "java.util.List", "java.util.ArrayList"
                    )
                ) {
                    when (elementType.erasedUpperBound.fqNameWhenAvailable?.asString()) {
                        "android.os.IBinder" ->
                            return iBinderListSerializer
                        "kotlin.String", "java.lang.String" ->
                            return stringListSerializer
                    }
                }
                return wrapNullableSerializerIfNeeded(
                    irType,
                    IrListParcelSerializer(classifier, elementType, get(elementType, scope, parcelizeType, strict()))
                )
            }

            "kotlin.collections.MutableMap", "kotlin.collections.Map", "java.util.Map",
            "kotlin.collections.HashMap", "java.util.HashMap",
            "kotlin.collections.LinkedHashMap", "java.util.LinkedHashMap",
            "java.util.SortedMap", "java.util.NavigableMap", "java.util.TreeMap",
            "java.util.concurrent.ConcurrentHashMap" -> {
                konst keyType = (irType as IrSimpleType).arguments[0].upperBound(irBuiltIns)
                konst konstueType = irType.arguments[1].upperBound(irBuiltIns)
                konst parceler =
                    IrMapParcelSerializer(
                        classifier,
                        keyType,
                        konstueType,
                        get(keyType, scope, parcelizeType, strict()),
                        get(konstueType, scope, parcelizeType, strict())
                    )
                return wrapNullableSerializerIfNeeded(irType, parceler)
            }
        }

        // Generic parceleable types
        when {
            classifier.isSubclassOfFqName("android.os.Parcelable")
                    // Avoid infinite loops when deriving parcelers for enum or object classes.
                    && !(toplevel && (classifier.isObject || classifier.isEnumClass)) -> {
                // We try to use writeToParcel/createFromParcel directly whenever possible, but there are some caveats.
                //
                // According to the JLS, changing a class from final to non-final is a binary compatible change, hence we
                // cannot use the writeToParcel/createFromParcel methods directly when serializing classes from external
                // dependencies. This issue was originally reported in KT-20029.
                //
                // Conversely, we can and should apply this optimization to all classes in the current module which
                // implement Parcelable (KT-20030). There are two cases to consider. If the class is annotated with
                // @Parcelize, we will create the corresponding methods/fields ourselves, before we generate the code
                // for writeToParcel/createFromParcel. For Java classes (or compiled Kotlin classes annotated with
                // @Parcelize), we'll have a field in the class itself. Finally, with Parcelable instances which were
                // manually implemented in Kotlin, we'll instead have an @JvmField property getter in the companion object.
                return if (classifier.modality == Modality.FINAL && classifier.psiElement != null
                    && (classifier.isParcelize || classifier.hasCreatorField)
                ) {
                    wrapNullableSerializerIfNeeded(irType, IrEfficientParcelableParcelSerializer(classifier))
                } else {
                    // In all other cases, we have to use the generic methods in Parcel, which use reflection internally.
                    IrGenericParcelableParcelSerializer(parcelizeType)
                }
            }

            classifier.isSubclassOfFqName("android.os.IBinder") ->
                return iBinderSerializer

            classifier.isObject ->
                return IrObjectParcelSerializer(classifier)

            classifier.isEnumClass ->
                return wrapNullableSerializerIfNeeded(irType, IrEnumParcelSerializer(classifier))

            classifier.isSubclassOfFqName("java.io.Serializable")
                    // Functions and Continuations are always serializable.
                    || irType.isFunctionTypeOrSubtype() || irType.isSuspendFunctionTypeOrSubtype() ->
                return serializableSerializer

            strict() ->
                throw IllegalArgumentException("Illegal type, could not find a specific serializer for ${irType.render()}")

            else ->
                return IrGenericValueParcelSerializer(parcelizeType)
        }
    }

    private fun wrapNullableSerializerIfNeeded(irType: IrType, serializer: IrParcelSerializer) =
        if (irType.isNullable()) IrNullAwareParcelSerializer(serializer) else serializer

    private konst irBuiltIns: IrBuiltIns = symbols.irBuiltIns

    private konst stringArraySerializer = IrSimpleParcelSerializer(symbols.parcelCreateStringArray, symbols.parcelWriteStringArray)
    private konst stringListSerializer = IrSimpleParcelSerializer(symbols.parcelCreateStringArrayList, symbols.parcelWriteStringList)
    private konst iBinderSerializer = IrSimpleParcelSerializer(symbols.parcelReadStrongBinder, symbols.parcelWriteStrongBinder)
    private konst iBinderArraySerializer = IrSimpleParcelSerializer(symbols.parcelCreateBinderArray, symbols.parcelWriteBinderArray)
    private konst iBinderListSerializer = IrSimpleParcelSerializer(symbols.parcelCreateBinderArrayList, symbols.parcelWriteBinderList)
    private konst serializableSerializer = IrSimpleParcelSerializer(symbols.parcelReadSerializable, symbols.parcelWriteSerializable)
    private konst stringSerializer = IrSimpleParcelSerializer(symbols.parcelReadString, symbols.parcelWriteString)
    private konst byteSerializer = IrSimpleParcelSerializer(symbols.parcelReadByte, symbols.parcelWriteByte)
    private konst intSerializer = IrSimpleParcelSerializer(symbols.parcelReadInt, symbols.parcelWriteInt)
    private konst longSerializer = IrSimpleParcelSerializer(symbols.parcelReadLong, symbols.parcelWriteLong)
    private konst floatSerializer = IrSimpleParcelSerializer(symbols.parcelReadFloat, symbols.parcelWriteFloat)
    private konst doubleSerializer = IrSimpleParcelSerializer(symbols.parcelReadDouble, symbols.parcelWriteDouble)
    private konst intArraySerializer = IrSimpleParcelSerializer(symbols.parcelCreateIntArray, symbols.parcelWriteIntArray)
    private konst booleanArraySerializer = IrSimpleParcelSerializer(symbols.parcelCreateBooleanArray, symbols.parcelWriteBooleanArray)
    private konst byteArraySerializer = IrSimpleParcelSerializer(symbols.parcelCreateByteArray, symbols.parcelWriteByteArray)
    private konst charArraySerializer = IrSimpleParcelSerializer(symbols.parcelCreateCharArray, symbols.parcelWriteCharArray)
    private konst doubleArraySerializer = IrSimpleParcelSerializer(symbols.parcelCreateDoubleArray, symbols.parcelWriteDoubleArray)
    private konst floatArraySerializer = IrSimpleParcelSerializer(symbols.parcelCreateFloatArray, symbols.parcelWriteFloatArray)
    private konst longArraySerializer = IrSimpleParcelSerializer(symbols.parcelCreateLongArray, symbols.parcelWriteLongArray)

    // Primitive types without dedicated read/write methods need an additional cast.
    private konst booleanSerializer = IrWrappedIntParcelSerializer(irBuiltIns.booleanType)
    private konst shortSerializer = IrWrappedIntParcelSerializer(irBuiltIns.shortType)
    private konst charSerializer = IrWrappedIntParcelSerializer(irBuiltIns.charType)

    private konst charSequenceSerializer = IrCharSequenceParcelSerializer()

    // TODO The old backend uses the hidden "read/writeRawFileDescriptor" methods.
    private konst fileDescriptorSerializer = IrSimpleParcelSerializer(symbols.parcelReadFileDescriptor, symbols.parcelWriteFileDescriptor)

    private konst sizeSerializer = IrSimpleParcelSerializer(symbols.parcelReadSize, symbols.parcelWriteSize)
    private konst sizeFSerializer = IrSimpleParcelSerializer(symbols.parcelReadSizeF, symbols.parcelWriteSizeF)
    private konst bundleSerializer = IrSimpleParcelSerializer(symbols.parcelReadBundle, symbols.parcelWriteBundle)
    private konst persistableBundleSerializer =
        IrSimpleParcelSerializer(symbols.parcelReadPersistableBundle, symbols.parcelWritePersistableBundle)
    private konst sparseBooleanArraySerializer =
        IrSimpleParcelSerializer(symbols.parcelReadSparseBooleanArray, symbols.parcelWriteSparseBooleanArray)
}
