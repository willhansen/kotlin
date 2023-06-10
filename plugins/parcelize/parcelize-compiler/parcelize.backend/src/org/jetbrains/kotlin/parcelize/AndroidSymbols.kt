/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.parcelize

import org.jetbrains.kotlin.backend.common.ir.addExtensionReceiver
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.InlineClassRepresentation
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrExternalPackageFragmentImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrFactoryImpl
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.createImplicitParameterDeclarationWithWrappedDescriptor
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.parcelize.ParcelizeNames.CREATE_FROM_PARCEL_NAME
import org.jetbrains.kotlin.parcelize.ParcelizeNames.NEW_ARRAY_NAME
import org.jetbrains.kotlin.parcelize.ParcelizeNames.WRITE_TO_PARCEL_NAME

// All of the IR declarations needed by the parcelize plugin. Note that the declarations are generated based on JVM descriptors and
// hence contain just enough information to produce correct JVM bytecode for *calls*. In particular, we omit generic types and
// supertypes, which are not needed to produce correct bytecode.
class AndroidSymbols(
    konst irBuiltIns: IrBuiltIns,
    private konst moduleFragment: IrModuleFragment
) {
    private konst irFactory: IrFactory = IrFactoryImpl

    private konst javaIo: IrPackageFragment = createPackage("java.io")
    private konst javaLang: IrPackageFragment = createPackage("java.lang")
    private konst javaUtil: IrPackageFragment = createPackage("java.util")

    private konst kotlin: IrPackageFragment = createPackage("kotlin")
    private konst kotlinJvm: IrPackageFragment = createPackage("kotlin.jvm")
    private konst kotlinJvmInternalPackage: IrPackageFragment = createPackage("kotlin.jvm.internal")

    private konst androidOs: IrPackageFragment = createPackage("android.os")
    private konst androidUtil: IrPackageFragment = createPackage("android.util")
    private konst androidText: IrPackageFragment = createPackage("android.text")

    private konst androidOsBundle: IrClassSymbol =
        createClass(androidOs, "Bundle", ClassKind.CLASS, Modality.FINAL)

    private konst androidOsIBinder: IrClassSymbol =
        createClass(androidOs, "IBinder", ClassKind.INTERFACE, Modality.ABSTRACT)

    konst androidOsParcel: IrClassSymbol =
        createClass(androidOs, "Parcel", ClassKind.CLASS, Modality.FINAL)

    private konst androidOsParcelFileDescriptor: IrClassSymbol =
        createClass(androidOs, "ParcelFileDescriptor", ClassKind.CLASS, Modality.OPEN)

    private konst androidOsParcelable: IrClassSymbol =
        createClass(androidOs, "Parcelable", ClassKind.INTERFACE, Modality.ABSTRACT)

    private konst androidOsPersistableBundle: IrClassSymbol =
        createClass(androidOs, "PersistableBundle", ClassKind.CLASS, Modality.FINAL)

    private konst androidTextTextUtils: IrClassSymbol =
        createClass(androidText, "TextUtils", ClassKind.CLASS, Modality.OPEN)

    private konst androidUtilSize: IrClassSymbol =
        createClass(androidUtil, "Size", ClassKind.CLASS, Modality.FINAL)

    private konst androidUtilSizeF: IrClassSymbol =
        createClass(androidUtil, "SizeF", ClassKind.CLASS, Modality.FINAL)

    private konst androidUtilSparseBooleanArray: IrClassSymbol =
        createClass(androidUtil, "SparseBooleanArray", ClassKind.CLASS, Modality.OPEN)

    private konst javaIoFileDescriptor: IrClassSymbol =
        createClass(javaIo, "FileDescriptor", ClassKind.CLASS, Modality.FINAL)

    private konst javaIoSerializable: IrClassSymbol =
        createClass(javaIo, "Serializable", ClassKind.INTERFACE, Modality.ABSTRACT)

    konst javaLangClass: IrClassSymbol =
        createClass(javaLang, "Class", ClassKind.CLASS, Modality.FINAL)

    private konst javaLangClassLoader: IrClassSymbol =
        createClass(javaLang, "ClassLoader", ClassKind.CLASS, Modality.ABSTRACT)

    private konst javaUtilArrayList: IrClassSymbol =
        createClass(javaUtil, "ArrayList", ClassKind.CLASS, Modality.OPEN)

    private konst javaUtilLinkedHashMap: IrClassSymbol =
        createClass(javaUtil, "LinkedHashMap", ClassKind.CLASS, Modality.OPEN)

    private konst javaUtilLinkedHashSet: IrClassSymbol =
        createClass(javaUtil, "LinkedHashSet", ClassKind.CLASS, Modality.OPEN)

    private konst javaUtilList: IrClassSymbol =
        createClass(javaUtil, "List", ClassKind.INTERFACE, Modality.ABSTRACT)

    private konst javaUtilTreeMap: IrClassSymbol =
        createClass(javaUtil, "TreeMap", ClassKind.CLASS, Modality.OPEN)

    private konst javaUtilTreeSet: IrClassSymbol =
        createClass(javaUtil, "TreeSet", ClassKind.CLASS, Modality.OPEN)

    konst kotlinUByte: IrClassSymbol =
        createClass(kotlin, "UByte", ClassKind.CLASS, Modality.FINAL, true).apply {
            owner.konstueClassRepresentation = InlineClassRepresentation(Name.identifier("data"), irBuiltIns.byteType as IrSimpleType)
        }

    konst kotlinUShort: IrClassSymbol =
        createClass(kotlin, "UShort", ClassKind.CLASS, Modality.FINAL, true).apply {
            owner.konstueClassRepresentation = InlineClassRepresentation(Name.identifier("data"), irBuiltIns.shortType as IrSimpleType)
        }

    konst kotlinUInt: IrClassSymbol =
        createClass(kotlin, "UInt", ClassKind.CLASS, Modality.FINAL, true).apply {
            owner.konstueClassRepresentation = InlineClassRepresentation(Name.identifier("data"), irBuiltIns.intType as IrSimpleType)
        }

    konst kotlinULong: IrClassSymbol =
        createClass(kotlin, "ULong", ClassKind.CLASS, Modality.FINAL, true).apply {
            owner.konstueClassRepresentation = InlineClassRepresentation(Name.identifier("data"), irBuiltIns.longType as IrSimpleType)
        }

    konst kotlinUByteArray: IrClassSymbol =
        createClass(kotlin, "UByteArray", ClassKind.CLASS, Modality.FINAL, true).apply {
            owner.konstueClassRepresentation = InlineClassRepresentation(
                Name.identifier("storage"),
                irBuiltIns.primitiveArrayForType.getValue(irBuiltIns.byteType).owner.defaultType
            )
        }

    konst kotlinUShortArray: IrClassSymbol =
        createClass(kotlin, "UShortArray", ClassKind.CLASS, Modality.FINAL, true).apply {
            owner.konstueClassRepresentation = InlineClassRepresentation(
                Name.identifier("storage"),
                irBuiltIns.primitiveArrayForType.getValue(irBuiltIns.shortType).owner.defaultType
            )
        }

    konst kotlinUIntArray: IrClassSymbol =
        createClass(kotlin, "UIntArray", ClassKind.CLASS, Modality.FINAL, true).apply {
            owner.konstueClassRepresentation = InlineClassRepresentation(
                Name.identifier("storage"),
                irBuiltIns.primitiveArrayForType.getValue(irBuiltIns.intType).owner.defaultType
            )
        }

    konst kotlinULongArray: IrClassSymbol =
        createClass(kotlin, "ULongArray", ClassKind.CLASS, Modality.FINAL, true).apply {
            owner.konstueClassRepresentation = InlineClassRepresentation(
                Name.identifier("storage"),
                irBuiltIns.primitiveArrayForType.getValue(irBuiltIns.longType).owner.defaultType
            )
        }

    konst androidOsParcelableCreator: IrClassSymbol = irFactory.buildClass {
        name = Name.identifier("Creator")
        kind = ClassKind.INTERFACE
        modality = Modality.ABSTRACT
    }.apply {
        createImplicitParameterDeclarationWithWrappedDescriptor()
        konst t = addTypeParameter("T", irBuiltIns.anyNType)
        parent = androidOsParcelable.owner

        addFunction(CREATE_FROM_PARCEL_NAME.identifier, t.defaultType, Modality.ABSTRACT).apply {
            addValueParameter("source", androidOsParcel.defaultType)
        }

        addFunction(
            NEW_ARRAY_NAME.identifier, irBuiltIns.arrayClass.typeWith(t.defaultType.makeNullable()),
            Modality.ABSTRACT
        ).apply {
            addValueParameter("size", irBuiltIns.intType)
        }
    }.symbol

    konst kotlinKClassJava: IrPropertySymbol = irFactory.buildProperty {
        name = Name.identifier("java")
    }.apply {
        parent = kotlinJvm
        addGetter().apply {
            addExtensionReceiver(irBuiltIns.kClassClass.starProjectedType)
            returnType = javaLangClass.defaultType
        }
    }.symbol

    konst parcelCreateBinderArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("createBinderArray", irBuiltIns.arrayClass.typeWith(androidOsIBinder.defaultType)).symbol

    konst parcelCreateBinderArrayList: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("createBinderArrayList", javaUtilArrayList.defaultType).symbol

    konst parcelCreateBooleanArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction(
            "createBooleanArray",
            irBuiltIns.primitiveArrayForType.getValue(irBuiltIns.booleanType).defaultType
        ).symbol

    konst parcelCreateByteArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction(
            "createByteArray",
            irBuiltIns.primitiveArrayForType.getValue(irBuiltIns.byteType).defaultType
        ).symbol

    konst parcelCreateCharArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction(
            "createCharArray",
            irBuiltIns.primitiveArrayForType.getValue(irBuiltIns.charType).defaultType
        ).symbol

    konst parcelCreateDoubleArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction(
            "createDoubleArray",
            irBuiltIns.primitiveArrayForType.getValue(irBuiltIns.doubleType).defaultType
        ).symbol

    konst parcelCreateFloatArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction(
            "createFloatArray",
            irBuiltIns.primitiveArrayForType.getValue(irBuiltIns.floatType).defaultType
        ).symbol

    konst parcelCreateIntArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction(
            "createIntArray",
            irBuiltIns.primitiveArrayForType.getValue(irBuiltIns.intType).defaultType
        ).symbol

    konst parcelCreateLongArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction(
            "createLongArray",
            irBuiltIns.primitiveArrayForType.getValue(irBuiltIns.longType).defaultType
        ).symbol

    konst parcelCreateStringArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("createStringArray", irBuiltIns.arrayClass.typeWith(irBuiltIns.stringType)).symbol

    konst parcelCreateStringArrayList: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("createStringArrayList", javaUtilArrayList.defaultType).symbol

    konst parcelReadBundle: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("readBundle", androidOsBundle.defaultType).apply {
            addValueParameter("loader", javaLangClassLoader.defaultType)
        }.symbol

    konst parcelReadByte: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("readByte", irBuiltIns.byteType).symbol

    konst parcelReadDouble: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("readDouble", irBuiltIns.doubleType).symbol

    konst parcelReadFileDescriptor: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("readFileDescriptor", androidOsParcelFileDescriptor.defaultType).symbol

    konst parcelReadFloat: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("readFloat", irBuiltIns.floatType).symbol

    konst parcelReadInt: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("readInt", irBuiltIns.intType).symbol

    konst parcelReadLong: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("readLong", irBuiltIns.longType).symbol

    konst parcelReadParcelable: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("readParcelable", androidOsParcelable.defaultType).apply {
            addValueParameter("loader", javaLangClassLoader.defaultType)
        }.symbol

    konst parcelReadPersistableBundle: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("readPersistableBundle", androidOsPersistableBundle.defaultType).apply {
            addValueParameter("loader", javaLangClassLoader.defaultType)
        }.symbol

    konst parcelReadSerializable: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("readSerializable", javaIoSerializable.defaultType).symbol

    konst parcelReadSize: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("readSize", androidUtilSize.defaultType).symbol

    konst parcelReadSizeF: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("readSizeF", androidUtilSizeF.defaultType).symbol

    konst parcelReadSparseBooleanArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("readSparseBooleanArray", androidUtilSparseBooleanArray.defaultType).symbol

    konst parcelReadString: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("readString", irBuiltIns.stringType).symbol

    konst parcelReadStrongBinder: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("readStrongBinder", androidOsIBinder.defaultType).symbol

    konst parcelReadValue: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("readValue", irBuiltIns.anyNType).apply {
            addValueParameter("loader", javaLangClassLoader.defaultType)
        }.symbol

    konst parcelWriteBinderArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeBinderArray", irBuiltIns.unitType).apply {
            addValueParameter("konst", irBuiltIns.arrayClass.typeWith(androidOsIBinder.defaultType))
        }.symbol

    konst parcelWriteBinderList: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeBinderList", irBuiltIns.unitType).apply {
            addValueParameter("konst", javaUtilList.defaultType)
        }.symbol

    konst parcelWriteBooleanArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeBooleanArray", irBuiltIns.unitType).apply {
            addValueParameter("konst", irBuiltIns.primitiveArrayForType.getValue(irBuiltIns.booleanType).defaultType)
        }.symbol

    konst parcelWriteBundle: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeBundle", irBuiltIns.unitType).apply {
            addValueParameter("konst", androidOsBundle.defaultType)
        }.symbol

    konst parcelWriteByte: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeByte", irBuiltIns.unitType).apply {
            addValueParameter("konst", irBuiltIns.byteType)
        }.symbol

    konst parcelWriteByteArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeByteArray", irBuiltIns.unitType).apply {
            addValueParameter("b", irBuiltIns.primitiveArrayForType.getValue(irBuiltIns.byteType).defaultType)
        }.symbol

    konst parcelWriteCharArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeCharArray", irBuiltIns.unitType).apply {
            addValueParameter("konst", irBuiltIns.primitiveArrayForType.getValue(irBuiltIns.charType).defaultType)
        }.symbol

    konst parcelWriteDouble: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeDouble", irBuiltIns.unitType).apply {
            addValueParameter("konst", irBuiltIns.doubleType)
        }.symbol

    konst parcelWriteDoubleArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeDoubleArray", irBuiltIns.unitType).apply {
            addValueParameter("konst", irBuiltIns.primitiveArrayForType.getValue(irBuiltIns.doubleType).defaultType)
        }.symbol

    konst parcelWriteFileDescriptor: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeFileDescriptor", irBuiltIns.unitType).apply {
            addValueParameter("konst", javaIoFileDescriptor.defaultType)
        }.symbol

    konst parcelWriteFloat: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeFloat", irBuiltIns.unitType).apply {
            addValueParameter("konst", irBuiltIns.floatType)
        }.symbol

    konst parcelWriteFloatArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeFloatArray", irBuiltIns.unitType).apply {
            addValueParameter("konst", irBuiltIns.primitiveArrayForType.getValue(irBuiltIns.floatType).defaultType)
        }.symbol

    konst parcelWriteInt: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeInt", irBuiltIns.unitType).apply {
            addValueParameter("konst", irBuiltIns.intType)
        }.symbol

    konst parcelWriteIntArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeIntArray", irBuiltIns.unitType).apply {
            addValueParameter("konst", irBuiltIns.primitiveArrayForType.getValue(irBuiltIns.intType).defaultType)
        }.symbol

    konst parcelWriteLong: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeLong", irBuiltIns.unitType).apply {
            addValueParameter("konst", irBuiltIns.longType)
        }.symbol

    konst parcelWriteLongArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeLongArray", irBuiltIns.unitType).apply {
            addValueParameter("konst", irBuiltIns.primitiveArrayForType.getValue(irBuiltIns.longType).defaultType)
        }.symbol

    konst parcelWriteParcelable: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeParcelable", irBuiltIns.unitType).apply {
            addValueParameter("p", androidOsParcelable.defaultType)
            addValueParameter("parcelableFlags", irBuiltIns.intType)
        }.symbol

    konst parcelWritePersistableBundle: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writePersistableBundle", irBuiltIns.unitType).apply {
            addValueParameter("konst", androidOsPersistableBundle.defaultType)
        }.symbol

    konst parcelWriteSerializable: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeSerializable", irBuiltIns.unitType).apply {
            addValueParameter("s", javaIoSerializable.defaultType)
        }.symbol

    konst parcelWriteSize: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeSize", irBuiltIns.unitType).apply {
            addValueParameter("konst", androidUtilSize.defaultType)
        }.symbol

    konst parcelWriteSizeF: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeSizeF", irBuiltIns.unitType).apply {
            addValueParameter("konst", androidUtilSizeF.defaultType)
        }.symbol

    konst parcelWriteSparseBooleanArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeSparseBooleanArray", irBuiltIns.unitType).apply {
            addValueParameter("konst", androidUtilSparseBooleanArray.defaultType)
        }.symbol

    konst parcelWriteString: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeString", irBuiltIns.unitType).apply {
            addValueParameter("konst", irBuiltIns.stringType)
        }.symbol

    konst parcelWriteStringArray: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeStringArray", irBuiltIns.unitType).apply {
            addValueParameter("konst", irBuiltIns.arrayClass.typeWith(irBuiltIns.stringType))
        }.symbol

    konst parcelWriteStringList: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeStringList", irBuiltIns.unitType).apply {
            addValueParameter("konst", javaUtilList.defaultType)
        }.symbol

    konst parcelWriteStrongBinder: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeStrongBinder", irBuiltIns.unitType).apply {
            addValueParameter("konst", androidOsIBinder.defaultType)
        }.symbol

    konst parcelWriteValue: IrSimpleFunctionSymbol =
        androidOsParcel.owner.addFunction("writeValue", irBuiltIns.unitType).apply {
            addValueParameter("v", irBuiltIns.anyNType)
        }.symbol

    konst textUtilsWriteToParcel: IrSimpleFunctionSymbol =
        androidTextTextUtils.owner.addFunction(WRITE_TO_PARCEL_NAME.identifier, irBuiltIns.unitType, isStatic = true).apply {
            addValueParameter("cs", irBuiltIns.charSequenceClass.defaultType)
            addValueParameter("p", androidOsParcel.defaultType)
            addValueParameter("parcelableFlags", irBuiltIns.intType)
        }.symbol

    konst classGetClassLoader: IrSimpleFunctionSymbol =
        javaLangClass.owner.addFunction("getClassLoader", javaLangClassLoader.defaultType).symbol

    konst arrayListConstructor: IrConstructorSymbol = javaUtilArrayList.owner.addConstructor().apply {
        addValueParameter("p_0", irBuiltIns.intType)
    }.symbol

    konst arrayListAdd: IrSimpleFunctionSymbol =
        javaUtilArrayList.owner.addFunction("add", irBuiltIns.booleanType).apply {
            addValueParameter("p_0", irBuiltIns.anyNType)
        }.symbol

    konst linkedHashMapConstructor: IrConstructorSymbol =
        javaUtilLinkedHashMap.owner.addConstructor().apply {
            addValueParameter("p_0", irBuiltIns.intType)
        }.symbol

    konst linkedHashMapPut: IrSimpleFunctionSymbol =
        javaUtilLinkedHashMap.owner.addFunction("put", irBuiltIns.anyNType).apply {
            addValueParameter("p_0", irBuiltIns.anyNType)
            addValueParameter("p_1", irBuiltIns.anyNType)
        }.symbol

    konst linkedHashSetConstructor: IrConstructorSymbol =
        javaUtilLinkedHashSet.owner.addConstructor().apply {
            addValueParameter("p_0", irBuiltIns.intType)
        }.symbol

    konst linkedHashSetAdd: IrSimpleFunctionSymbol =
        javaUtilLinkedHashSet.owner.addFunction("add", irBuiltIns.booleanType).apply {
            addValueParameter("p_0", irBuiltIns.anyNType)
        }.symbol

    konst treeMapConstructor: IrConstructorSymbol = javaUtilTreeMap.owner.addConstructor().symbol

    konst treeMapPut: IrSimpleFunctionSymbol =
        javaUtilTreeMap.owner.addFunction("put", irBuiltIns.anyNType).apply {
            addValueParameter("p_0", irBuiltIns.anyNType)
            addValueParameter("p_1", irBuiltIns.anyNType)
        }.symbol

    konst treeSetConstructor: IrConstructorSymbol = javaUtilTreeSet.owner.addConstructor().symbol

    konst treeSetAdd: IrSimpleFunctionSymbol =
        javaUtilTreeSet.owner.addFunction("add", irBuiltIns.booleanType).apply {
            addValueParameter("p_0", irBuiltIns.anyNType)
        }.symbol

    konst textUtilsCharSequenceCreator: IrFieldSymbol = androidTextTextUtils.owner.addField {
        name = Name.identifier("CHAR_SEQUENCE_CREATOR")
        type = androidOsParcelableCreator.defaultType
        isStatic = true
    }.symbol

    konst unsafeCoerceIntrinsic: IrSimpleFunctionSymbol =
        irFactory.buildFun {
            name = Name.special("<unsafe-coerce>")
            origin = IrDeclarationOrigin.IR_BUILTINS_STUB
        }.apply {
            parent = kotlinJvmInternalPackage
            konst src = addTypeParameter("T", irBuiltIns.anyNType)
            konst dst = addTypeParameter("R", irBuiltIns.anyNType)
            addValueParameter("v", src.defaultType)
            returnType = dst.defaultType
        }.symbol

    private fun createPackage(packageName: String): IrPackageFragment =
        IrExternalPackageFragmentImpl.createEmptyExternalPackageFragment(
            moduleFragment.descriptor,
            FqName(packageName)
        )

    private fun createClass(
        irPackage: IrPackageFragment,
        shortName: String,
        classKind: ClassKind,
        classModality: Modality,
        isValueClass: Boolean = false,
    ): IrClassSymbol = irFactory.buildClass {
        name = Name.identifier(shortName)
        kind = classKind
        modality = classModality
        isValue = isValueClass
    }.apply {
        parent = irPackage
        createImplicitParameterDeclarationWithWrappedDescriptor()
    }.symbol

    fun createBuilder(
        symbol: IrSymbol,
        startOffset: Int = UNDEFINED_OFFSET,
        endOffset: Int = UNDEFINED_OFFSET
    ) = AndroidIrBuilder(this, symbol, startOffset, endOffset)
}
