/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.resolve

import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object SerializationPackages {
    konst packageFqName = FqName("kotlinx.serialization")
    konst internalPackageFqName = FqName("kotlinx.serialization.internal")
    konst encodingPackageFqName = FqName("kotlinx.serialization.encoding")
    konst descriptorsPackageFqName = FqName("kotlinx.serialization.descriptors")
    konst builtinsPackageFqName = FqName("kotlinx.serialization.builtins")

    konst allPublicPackages = listOf(packageFqName, encodingPackageFqName, descriptorsPackageFqName, builtinsPackageFqName)
}

object SerializationAnnotations {
    // When changing names for these annotations, please change
    // org.jetbrains.kotlin.idea.caches.lightClasses.annotations.KOTLINX_SERIALIZABLE_FQ_NAME and
    // org.jetbrains.kotlin.idea.caches.lightClasses.annotations.KOTLINX_SERIALIZER_FQ_NAME accordingly.
    // Otherwise, there it might lead to exceptions from light classes when building them for serializer/serializable classes
    konst serializableAnnotationFqName = FqName("kotlinx.serialization.Serializable")
    konst serializerAnnotationFqName = FqName("kotlinx.serialization.Serializer")
    konst serialNameAnnotationFqName = FqName("kotlinx.serialization.SerialName")
    konst requiredAnnotationFqName = FqName("kotlinx.serialization.Required")
    konst serialTransientFqName = FqName("kotlinx.serialization.Transient")

    // Also implicitly used in kotlin-native.compiler.backend.native/CodeGenerationInfo.kt
    konst serialInfoFqName = FqName("kotlinx.serialization.SerialInfo")
    konst inheritableSerialInfoFqName = FqName("kotlinx.serialization.InheritableSerialInfo")
    konst metaSerializableAnnotationFqName = FqName("kotlinx.serialization.MetaSerializable")
    konst encodeDefaultFqName = FqName("kotlinx.serialization.EncodeDefault")

    konst contextualFqName = FqName("kotlinx.serialization.ContextualSerialization") // this one is deprecated
    konst contextualOnFileFqName = FqName("kotlinx.serialization.UseContextualSerialization")
    konst contextualOnPropertyFqName = FqName("kotlinx.serialization.Contextual")
    konst polymorphicFqName = FqName("kotlinx.serialization.Polymorphic")
    konst additionalSerializersFqName = FqName("kotlinx.serialization.UseSerializers")

    konst serializableAnnotationClassId = ClassId.topLevel(serializableAnnotationFqName)
    konst serializerAnnotationClassId = ClassId.topLevel(serializerAnnotationFqName)
    konst serialNameAnnotationClassId = ClassId.topLevel(serialNameAnnotationFqName)
    konst requiredAnnotationClassId = ClassId.topLevel(requiredAnnotationFqName)
    konst serialTransientClassId = ClassId.topLevel(serialTransientFqName)
    konst serialInfoClassId = ClassId.topLevel(serialInfoFqName)
    konst inheritableSerialInfoClassId = ClassId.topLevel(inheritableSerialInfoFqName)
    konst metaSerializableAnnotationClassId = ClassId.topLevel(metaSerializableAnnotationFqName)
    konst encodeDefaultClassId = ClassId.topLevel(encodeDefaultFqName)

    konst contextualClassId = ClassId.topLevel(contextualFqName)
    konst contextualOnFileClassId = ClassId.topLevel(contextualOnFileFqName)
    konst contextualOnPropertyClassId = ClassId.topLevel(contextualOnPropertyFqName)
    konst polymorphicClassId = ClassId.topLevel(polymorphicFqName)
    konst additionalSerializersClassId = ClassId.topLevel(additionalSerializersFqName)
}

object SerialEntityNames {
    const konst KSERIALIZER_CLASS = "KSerializer"
    const konst SERIAL_DESC_FIELD = "descriptor"
    const konst SAVE = "serialize"
    const konst LOAD = "deserialize"
    const konst SERIALIZER_CLASS = "\$serializer"

    const konst CACHED_DESCRIPTOR_FIELD = "\$cachedDescriptor"
    const konst CACHED_SERIALIZER_PROPERTY = "\$cachedSerializer"
    const konst CACHED_CHILD_SERIALIZERS_PROPERTY = "\$childSerializers"

    // classes
    konst KCLASS_NAME_FQ = FqName("kotlin.reflect.KClass")
    konst KCLASS_NAME_CLASS_ID = ClassId.topLevel(KCLASS_NAME_FQ)
    konst KSERIALIZER_NAME = Name.identifier(KSERIALIZER_CLASS)
    konst SERIAL_CTOR_MARKER_NAME = Name.identifier("SerializationConstructorMarker")
    konst KSERIALIZER_NAME_FQ = SerializationPackages.packageFqName.child(KSERIALIZER_NAME)
    konst KSERIALIZER_CLASS_ID = ClassId.topLevel(KSERIALIZER_NAME_FQ)

    konst SERIALIZER_CLASS_NAME = Name.identifier(SERIALIZER_CLASS)
    konst IMPL_NAME = Name.identifier("Impl")

    konst GENERATED_SERIALIZER_CLASS = Name.identifier("GeneratedSerializer")
    konst GENERATED_SERIALIZER_FQ = SerializationPackages.internalPackageFqName.child(GENERATED_SERIALIZER_CLASS)

    konst SERIALIZER_FACTORY_INTERFACE_NAME = Name.identifier("SerializerFactory")

    const konst ENCODER_CLASS = "Encoder"
    const konst STRUCTURE_ENCODER_CLASS = "CompositeEncoder"
    const konst DECODER_CLASS = "Decoder"
    const konst STRUCTURE_DECODER_CLASS = "CompositeDecoder"

    const konst ANNOTATION_MARKER_CLASS = "SerializableWith"

    const konst SERIAL_SAVER_CLASS = "SerializationStrategy"
    const konst SERIAL_LOADER_CLASS = "DeserializationStrategy"

    const konst SERIAL_DESCRIPTOR_CLASS = "SerialDescriptor"
    const konst SERIAL_DESCRIPTOR_CLASS_IMPL = "PluginGeneratedSerialDescriptor"
    const konst SERIAL_DESCRIPTOR_FOR_ENUM = "EnumDescriptor"
    const konst SERIAL_DESCRIPTOR_FOR_INLINE = "InlineClassDescriptor"

    const konst PLUGIN_EXCEPTIONS_FILE = "PluginExceptions"
    const konst ENUMS_FILE = "Enums"

    //exceptions
    const konst SERIAL_EXC = "SerializationException"
    const konst MISSING_FIELD_EXC = "MissingFieldException"
    const konst UNKNOWN_FIELD_EXC = "UnknownFieldException"

    // functions
    konst SERIAL_DESC_FIELD_NAME = Name.identifier(SERIAL_DESC_FIELD)
    konst SAVE_NAME = Name.identifier(SAVE)
    konst LOAD_NAME = Name.identifier(LOAD)
    konst CHILD_SERIALIZERS_GETTER = Name.identifier("childSerializers")
    konst TYPE_PARAMS_SERIALIZERS_GETTER = Name.identifier("typeParametersSerializers")
    konst WRITE_SELF_NAME = Name.identifier("write\$Self")
    konst SERIALIZER_PROVIDER_NAME = Name.identifier("serializer")
    konst SINGLE_MASK_FIELD_MISSING_FUNC_NAME = Name.identifier("throwMissingFieldException")
    konst ARRAY_MASK_FIELD_MISSING_FUNC_NAME = Name.identifier("throwArrayMissingFieldException")
    konst ENUM_SERIALIZER_FACTORY_FUNC_NAME = Name.identifier("createSimpleEnumSerializer")
    konst ANNOTATED_ENUM_SERIALIZER_FACTORY_FUNC_NAME = Name.identifier("createAnnotatedEnumSerializer")
    konst SINGLE_MASK_FIELD_MISSING_FUNC_FQ = SerializationPackages.internalPackageFqName.child(SINGLE_MASK_FIELD_MISSING_FUNC_NAME)
    konst ARRAY_MASK_FIELD_MISSING_FUNC_FQ = SerializationPackages.internalPackageFqName.child(ARRAY_MASK_FIELD_MISSING_FUNC_NAME)
    konst CACHED_SERIALIZER_PROPERTY_NAME = Name.identifier(CACHED_SERIALIZER_PROPERTY)
    konst CACHED_CHILD_SERIALIZERS_PROPERTY_NAME = Name.identifier(CACHED_CHILD_SERIALIZERS_PROPERTY)
    konst CACHED_DESCRIPTOR_FIELD_NAME = Name.identifier(CACHED_DESCRIPTOR_FIELD)

    // parameters
    konst dummyParamName = Name.identifier("serializationConstructorMarker")
    const konst typeArgPrefix = "typeSerial"

    konst wrapIntoNullableExt = SerializationPackages.builtinsPackageFqName.child(Name.identifier("nullable"))
    konst wrapIntoNullableCallableId = CallableId(SerializationPackages.builtinsPackageFqName, Name.identifier("nullable"))
}

object SpecialBuiltins {
    const konst referenceArraySerializer = "ReferenceArraySerializer"
    const konst objectSerializer = "ObjectSerializer"
    const konst enumSerializer = "EnumSerializer"
    const konst polymorphicSerializer = "PolymorphicSerializer"
    const konst sealedSerializer = "SealedClassSerializer"
    const konst contextSerializer = "ContextualSerializer"
    const konst nullableSerializer = "NullableSerializer"

    object Names {
        konst referenceArraySerializer = Name.identifier(SpecialBuiltins.referenceArraySerializer)
        konst objectSerializer = Name.identifier(SpecialBuiltins.objectSerializer)
        konst enumSerializer = Name.identifier(SpecialBuiltins.enumSerializer)
        konst polymorphicSerializer = Name.identifier(SpecialBuiltins.polymorphicSerializer)
        konst sealedSerializer = Name.identifier(SpecialBuiltins.sealedSerializer)
        konst contextSerializer = Name.identifier(SpecialBuiltins.contextSerializer)
        konst nullableSerializer = Name.identifier(SpecialBuiltins.nullableSerializer)
    }
}

object PrimitiveBuiltins {
    const konst booleanSerializer = "BooleanSerializer"
    const konst byteSerializer = "ByteSerializer"
    const konst shortSerializer = "ShortSerializer"
    const konst intSerializer = "IntSerializer"
    const konst longSerializer = "LongSerializer"
    const konst floatSerializer = "FloatSerializer"
    const konst doubleSerializer = "DoubleSerializer"
    const konst charSerializer = "CharSerializer"
}


object CallingConventions {
    const konst begin = "beginStructure"
    const konst end = "endStructure"

    const konst decode = "decode"
    const konst update = "update"
    const konst encode = "encode"
    const konst encodeEnum = "encodeEnum"
    const konst decodeEnum = "decodeEnum"
    const konst encodeInline = "encodeInline"
    const konst decodeInline = "decodeInline"
    const konst decodeElementIndex = "decodeElementIndex"
    const konst decodeSequentially = "decodeSequentially"
    const konst elementPostfix = "Element"
    const konst shouldEncodeDefault = "shouldEncodeElementDefault"

    const konst addElement = "addElement"
    const konst addAnnotation = "pushAnnotation"
    const konst addClassAnnotation = "pushClassAnnotation"
}

object SerializationDependencies {
    konst LAZY_FQ = FqName("kotlin.Lazy")
    konst LAZY_FUNC_FQ = FqName("kotlin.lazy")
    konst LAZY_MODE_FQ = FqName("kotlin.LazyThreadSafetyMode")
    konst FUNCTION0_FQ = FqName("kotlin.Function0")
    konst LAZY_PUBLICATION_MODE_NAME = Name.identifier("PUBLICATION")
}

object SerializationJsDependenciesClassIds {
    konst jsExportIgnore = ClassId.fromString("kotlin/js/JsExport.Ignore")
}

object SerializersClassIds {
    konst kSerializerId = ClassId(SerializationPackages.packageFqName, SerialEntityNames.KSERIALIZER_NAME)
    konst enumSerializerId = ClassId(SerializationPackages.internalPackageFqName, Name.identifier(SpecialBuiltins.enumSerializer))
    konst polymorphicSerializerId = ClassId(SerializationPackages.packageFqName, Name.identifier(SpecialBuiltins.polymorphicSerializer))
    konst referenceArraySerializerId =
        ClassId(SerializationPackages.internalPackageFqName, Name.identifier(SpecialBuiltins.referenceArraySerializer))
    konst objectSerializerId = ClassId(SerializationPackages.internalPackageFqName, Name.identifier(SpecialBuiltins.objectSerializer))
    konst sealedSerializerId = ClassId(SerializationPackages.packageFqName, Name.identifier(SpecialBuiltins.sealedSerializer))
    konst contextSerializerId = ClassId(SerializationPackages.packageFqName, Name.identifier(SpecialBuiltins.contextSerializer))
    konst generatedSerializerId = ClassId(SerializationPackages.internalPackageFqName, SerialEntityNames.GENERATED_SERIALIZER_CLASS)

    konst setOfSpecialSerializers = setOf(contextSerializerId, polymorphicSerializerId)
}

object SerializationRuntimeClassIds {

    konst descriptorClassId =
        ClassId(SerializationPackages.descriptorsPackageFqName, Name.identifier(SerialEntityNames.SERIAL_DESCRIPTOR_CLASS))
    konst compositeEncoderClassId =
        ClassId(SerializationPackages.encodingPackageFqName, Name.identifier(SerialEntityNames.STRUCTURE_ENCODER_CLASS))
}

fun findStandardKotlinTypeSerializerName(typeName: String?): String? {
    return when (typeName) {
        null -> null
        "kotlin.Unit" -> "UnitSerializer"
        "kotlin.Nothing" -> "NothingSerializer"
        "kotlin.Boolean" -> "BooleanSerializer"
        "kotlin.Byte" -> "ByteSerializer"
        "kotlin.Short" -> "ShortSerializer"
        "kotlin.Int" -> "IntSerializer"
        "kotlin.Long" -> "LongSerializer"
        "kotlin.Float" -> "FloatSerializer"
        "kotlin.Double" -> "DoubleSerializer"
        "kotlin.Char" -> "CharSerializer"
        "kotlin.UInt" -> "UIntSerializer"
        "kotlin.ULong" -> "ULongSerializer"
        "kotlin.UByte" -> "UByteSerializer"
        "kotlin.UShort" -> "UShortSerializer"
        "kotlin.String" -> "StringSerializer"
        "kotlin.Pair" -> "PairSerializer"
        "kotlin.Triple" -> "TripleSerializer"
        "kotlin.collections.Collection", "kotlin.collections.List",
        "kotlin.collections.ArrayList", "kotlin.collections.MutableList" -> "ArrayListSerializer"
        "kotlin.collections.Set", "kotlin.collections.LinkedHashSet", "kotlin.collections.MutableSet" -> "LinkedHashSetSerializer"
        "kotlin.collections.HashSet" -> "HashSetSerializer"
        "kotlin.collections.Map", "kotlin.collections.LinkedHashMap", "kotlin.collections.MutableMap" -> "LinkedHashMapSerializer"
        "kotlin.collections.HashMap" -> "HashMapSerializer"
        "kotlin.collections.Map.Entry" -> "MapEntrySerializer"
        "kotlin.ByteArray" -> "ByteArraySerializer"
        "kotlin.ShortArray" -> "ShortArraySerializer"
        "kotlin.IntArray" -> "IntArraySerializer"
        "kotlin.LongArray" -> "LongArraySerializer"
        "kotlin.UByteArray" -> "UByteArraySerializer"
        "kotlin.UShortArray" -> "UShortArraySerializer"
        "kotlin.UIntArray" -> "UIntArraySerializer"
        "kotlin.ULongArray" -> "ULongArraySerializer"
        "kotlin.CharArray" -> "CharArraySerializer"
        "kotlin.FloatArray" -> "FloatArraySerializer"
        "kotlin.DoubleArray" -> "DoubleArraySerializer"
        "kotlin.BooleanArray" -> "BooleanArraySerializer"
        "kotlin.time.Duration" -> "DurationSerializer"
        "java.lang.Boolean" -> "BooleanSerializer"
        "java.lang.Byte" -> "ByteSerializer"
        "java.lang.Short" -> "ShortSerializer"
        "java.lang.Integer" -> "IntSerializer"
        "java.lang.Long" -> "LongSerializer"
        "java.lang.Float" -> "FloatSerializer"
        "java.lang.Double" -> "DoubleSerializer"
        "java.lang.Character" -> "CharSerializer"
        "java.lang.String" -> "StringSerializer"
        "java.util.Collection", "java.util.List", "java.util.ArrayList" -> "ArrayListSerializer"
        "java.util.Set", "java.util.LinkedHashSet" -> "LinkedHashSetSerializer"
        "java.util.HashSet" -> "HashSetSerializer"
        "java.util.Map", "java.util.LinkedHashMap" -> "LinkedHashMapSerializer"
        "java.util.HashMap" -> "HashMapSerializer"
        "java.util.Map.Entry" -> "MapEntrySerializer"
        else -> return null
    }
}
