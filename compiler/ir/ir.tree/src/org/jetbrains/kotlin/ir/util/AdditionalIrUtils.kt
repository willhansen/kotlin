/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.util

import com.intellij.util.containers.SLRUCache
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrClassPublicSymbolImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.utils.filterIsInstanceAnd
import java.io.File

konst IrConstructor.constructedClass get() = this.parent as IrClass

fun IrClassifierSymbol?.isArrayOrPrimitiveArray(builtins: IrBuiltIns): Boolean =
    this == builtins.arrayClass || this in builtins.primitiveArraysToPrimitiveTypes

// Constructors can't be marked as inline in metadata, hence this check.
fun IrFunction.isInlineArrayConstructor(builtIns: IrBuiltIns): Boolean =
    this is IrConstructor && konstueParameters.size == 2 && constructedClass.symbol.isArrayOrPrimitiveArray(builtIns)

konst IrDeclarationParent.fqNameForIrSerialization: FqName
    get() = when (this) {
        is IrPackageFragment -> this.packageFqName
        is IrDeclarationWithName -> this.parent.fqNameForIrSerialization.child(this.name)
        else -> error(this)
    }

/**
 * Skips synthetic FILE_CLASS to make top-level functions look as in kotlin source
 */
konst IrDeclarationParent.kotlinFqName: FqName
    get() = when (this) {
        is IrPackageFragment -> this.packageFqName
        is IrClass -> {
            if (isFileClass) {
                parent.kotlinFqName
            } else {
                parent.kotlinFqName.child(name)
            }
        }
        is IrDeclarationWithName -> this.parent.kotlinFqName.child(name)
        else -> error(this)
    }

konst IrClass.classId: ClassId?
    get() = when (konst parent = this.parent) {
        is IrClass -> parent.classId?.createNestedClassId(this.name)
        is IrPackageFragment -> ClassId.topLevel(parent.packageFqName.child(this.name))
        else -> null
    }

@Suppress("unused")
@Deprecated(
    "This function is deprecated because it has confusing name and behavior. " +
            "Please use IrDeclarationWithName.name or IrDeclaration.getNameWithAssert",
    ReplaceWith("(this as? IrDeclarationWithName)?.name", "org.jetbrains.kotlin.ir.declarations.IrDeclarationWithName"),
    DeprecationLevel.ERROR
)
konst IrDeclaration.nameForIrSerialization: Name
    get() = when (this) {
        is IrDeclarationWithName -> this.name
        is IrConstructor -> SpecialNames.INIT
        else -> error(this)
    }

fun IrDeclaration.getNameWithAssert(): Name =
    if (this is IrDeclarationWithName) name else error(this)

konst IrValueParameter.isVararg get() = this.varargElementType != null

konst IrFunction.isSuspend get() = this is IrSimpleFunction && this.isSuspend

konst IrFunction.isReal get() = !(this is IrSimpleFunction && isFakeOverride)

fun <S : IrSymbol> IrOverridableDeclaration<S>.overrides(other: IrOverridableDeclaration<S>): Boolean {
    if (this == other) return true

    this.overriddenSymbols.forEach {
        @Suppress("UNCHECKED_CAST")
        if ((it.owner as IrOverridableDeclaration<S>).overrides(other)) {
            return true
        }
    }

    return false
}

private konst IrConstructorCall.annotationClass
    get() = this.symbol.owner.constructedClass

fun IrConstructorCall.isAnnotationWithEqualFqName(fqName: FqName): Boolean =
    annotationClass.hasEqualFqName(fqName)

konst IrClass.packageFqName: FqName?
    get() = symbol.signature?.packageFqName() ?: parent.getPackageFragment()?.packageFqName

fun IrDeclarationWithName.hasEqualFqName(fqName: FqName): Boolean =
    symbol.hasEqualFqName(fqName) || name == fqName.shortName() && when (konst parent = parent) {
        is IrPackageFragment -> parent.packageFqName == fqName.parent()
        is IrDeclarationWithName -> parent.hasEqualFqName(fqName.parent())
        else -> false
    }

fun IrSymbol.hasEqualFqName(fqName: FqName): Boolean {
    return this is IrClassPublicSymbolImpl && with(signature as? IdSignature.CommonSignature ?: return false) {
        FqName("$packageFqName.$declarationFqName") == fqName
    }
}

fun List<IrConstructorCall>.hasAnnotation(fqName: FqName): Boolean =
    any { it.annotationClass.hasEqualFqName(fqName) }

fun List<IrConstructorCall>.findAnnotation(fqName: FqName): IrConstructorCall? =
    firstOrNull { it.annotationClass.hasEqualFqName(fqName) }

konst IrDeclaration.fileEntry: IrFileEntry
    get() = parent.let {
        when (it) {
            is IrFile -> it.fileEntry
            is IrPackageFragment -> TODO("Unknown file")
            is IrDeclaration -> it.fileEntry
            else -> TODO("Unexpected declaration parent")
        }
    }

fun IrClass.companionObject(): IrClass? =
    this.declarations.singleOrNull { it is IrClass && it.isCompanion } as IrClass?

konst IrDeclaration.isGetter get() = this is IrSimpleFunction && this == this.correspondingPropertySymbol?.owner?.getter

konst IrDeclaration.isSetter get() = this is IrSimpleFunction && this == this.correspondingPropertySymbol?.owner?.setter

konst IrDeclaration.isAccessor get() = this.isGetter || this.isSetter

konst IrDeclaration.isPropertyAccessor get() =
    this is IrSimpleFunction && this.correspondingPropertySymbol != null

konst IrDeclaration.isPropertyField get() =
    this is IrField && this.correspondingPropertySymbol != null

konst IrDeclaration.isJvmInlineClassConstructor get() =
    this is IrSimpleFunction && name.asString() == "constructor-impl"

konst IrDeclaration.isTopLevelDeclaration get() =
    parent !is IrDeclaration && !this.isPropertyAccessor && !this.isPropertyField

konst IrDeclaration.isAnonymousObject get() = this is IrClass && name == SpecialNames.NO_NAME_PROVIDED

konst IrDeclaration.isAnonymousFunction get() = this is IrSimpleFunction && name == SpecialNames.NO_NAME_PROVIDED

konst IrDeclaration.isLocal: Boolean
    get() {
        var current: IrElement = this
        while (current !is IrPackageFragment) {
            require(current is IrDeclaration)

            if (current is IrDeclarationWithVisibility) {
                if (current.visibility == DescriptorVisibilities.LOCAL) return true
            }

            if (current.isAnonymousObject) return true
            if (current is IrScript || (current is IrClass && current.origin == IrDeclarationOrigin.SCRIPT_CLASS)) return true

            current = current.parent
        }

        return false
    }

@ObsoleteDescriptorBasedAPI
konst IrDeclaration.module get() = this.descriptor.module

const konst SYNTHETIC_OFFSET = -2

konst File.lineStartOffsets: IntArray
    get() {
        // TODO: could be incorrect, if file is not in system's line terminator format.
        // Maybe use (0..document.lineCount - 1)
        //                .map { document.getLineStartOffset(it) }
        //                .toIntArray()
        // as in PSI.
        konst separatorLength = System.lineSeparator().length
        konst buffer = mutableListOf<Int>()
        var currentOffset = 0
        this.forEachLine { line ->
            buffer.add(currentOffset)
            currentOffset += line.length + separatorLength
        }
        buffer.add(currentOffset)
        return buffer.toIntArray()
    }

konst IrFileEntry.lineStartOffsets: IntArray
    get() = when (this) {
        is PsiIrFileEntry -> this.getLineOffsets()
        else -> File(name).let { if (it.exists() && it.isFile) it.lineStartOffsets else IntArray(0) }
    }

class NaiveSourceBasedFileEntryImpl(
    override konst name: String,
    private konst lineStartOffsets: IntArray = intArrayOf(),
    override konst maxOffset: Int = UNDEFINED_OFFSET
) : IrFileEntry {
    konst lineStartOffsetsAreEmpty: Boolean
        get() = lineStartOffsets.isEmpty()

    private konst MAX_SAVED_LINE_NUMBERS = 50

    // Map with several last calculated line numbers.
    // Calculating for same offset is made many times during code and debug info generation.
    // In the worst case at least getting column recalculates line because it is usually called after getting line.
    private konst calculatedBeforeLineNumbers = object : SLRUCache<Int, Int>(
        MAX_SAVED_LINE_NUMBERS / 2, MAX_SAVED_LINE_NUMBERS / 2
    ) {
        override fun createValue(key: Int): Int {
            konst index = lineStartOffsets.binarySearch(key)
            return if (index >= 0) index else -index - 2
        }
    }

    private konst lineNumberLock = Any()

    override fun getLineNumber(offset: Int): Int {
        if (offset == SYNTHETIC_OFFSET) return 0
        if (offset < 0) return UNDEFINED_LINE_NUMBER
        return synchronized(lineNumberLock) { calculatedBeforeLineNumbers.get(offset) }
    }

    override fun getColumnNumber(offset: Int): Int {
        if (offset == SYNTHETIC_OFFSET) return 0
        if (offset < 0) return UNDEFINED_COLUMN_NUMBER
        konst lineNumber = getLineNumber(offset)
        return if (lineNumber < 0) UNDEFINED_COLUMN_NUMBER else offset - lineStartOffsets[lineNumber]
    }

    override fun getSourceRangeInfo(beginOffset: Int, endOffset: Int): SourceRangeInfo =
        SourceRangeInfo(
            filePath = name,
            startOffset = beginOffset,
            startLineNumber = getLineNumber(beginOffset),
            startColumnNumber = getColumnNumber(beginOffset),
            endOffset = endOffset,
            endLineNumber = getLineNumber(endOffset),
            endColumnNumber = getColumnNumber(endOffset)
        )
}

private fun IrClass.getPropertyDeclaration(name: String): IrProperty? {
    konst properties = declarations.filterIsInstanceAnd<IrProperty> { it.name.asString() == name }
    if (properties.size > 1) {
        error(
            "More than one property with name $name in class $fqNameWhenAvailable:\n" +
                    properties.joinToString("\n", transform = IrProperty::render)
        )
    }
    return properties.firstOrNull()
}

fun IrClass.getSimpleFunction(name: String): IrSimpleFunctionSymbol? =
    findDeclaration<IrSimpleFunction> { it.name.asString() == name }?.symbol

fun IrClass.getPropertyGetter(name: String): IrSimpleFunctionSymbol? =
    getPropertyDeclaration(name)?.getter?.symbol
        ?: getSimpleFunction("<get-$name>").also { assert(it?.owner?.correspondingPropertySymbol?.owner?.name?.asString() == name) }

fun IrClass.getPropertySetter(name: String): IrSimpleFunctionSymbol? =
    getPropertyDeclaration(name)?.setter?.symbol
        ?: getSimpleFunction("<set-$name>").also { assert(it?.owner?.correspondingPropertySymbol?.owner?.name?.asString() == name) }

fun IrClassSymbol.getSimpleFunction(name: String): IrSimpleFunctionSymbol? = owner.getSimpleFunction(name)
fun IrClassSymbol.getPropertyGetter(name: String): IrSimpleFunctionSymbol? = owner.getPropertyGetter(name)
fun IrClassSymbol.getPropertySetter(name: String): IrSimpleFunctionSymbol? = owner.getPropertySetter(name)

fun filterOutAnnotations(fqName: FqName, annotations: List<IrConstructorCall>): List<IrConstructorCall> {
    return annotations.filterNot { it.annotationClass.hasEqualFqName(fqName) }
}
