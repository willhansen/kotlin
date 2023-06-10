/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.tree.generator.model

import org.jetbrains.kotlin.fir.tree.generator.printer.typeWithArguments

sealed class Field : Importable {
    abstract konst name: String
    open konst arguments = mutableListOf<Importable>()
    abstract konst nullable: Boolean
    abstract var isVolatile: Boolean
    abstract var isFinal: Boolean
    abstract var isLateinit: Boolean
    abstract var isParameter: Boolean
    open var withReplace: Boolean = false
    abstract konst isFirType: Boolean

    var fromParent: Boolean = false
    open var needsSeparateTransform: Boolean = false
    var parentHasSeparateTransform: Boolean = true
    open var needTransformInOtherChildren: Boolean = false
    open var customInitializationCall: String? = null
    open konst arbitraryImportables: MutableList<Importable> = mutableListOf()
    open var optInAnnotation: ArbitraryImportable? = null

    open konst defaultValueInImplementation: String? get() = null
    abstract var isMutable: Boolean
    abstract var isMutableOrEmpty: Boolean
    open var isMutableInInterface: Boolean = false
    open konst withGetter: Boolean get() = false
    open konst customSetter: String? get() = null
    open konst fromDelegate: Boolean get() = false

    open konst overridenTypes: MutableSet<Importable> = mutableSetOf()
    open var useNullableForReplace: Boolean = false
    open var notNull: Boolean = false

    var withBindThis = true

    fun copy(): Field = internalCopy().also {
        updateFieldsInCopy(it)
    }

    protected fun updateFieldsInCopy(copy: Field) {
        if (copy !is FieldWithDefault) {
            copy.arguments.clear()
            copy.arguments.addAll(arguments)
            copy.needsSeparateTransform = needsSeparateTransform
            copy.needTransformInOtherChildren = needTransformInOtherChildren
            copy.isMutable = isMutable
            copy.overridenTypes += overridenTypes
            copy.arbitraryImportables += arbitraryImportables
            copy.useNullableForReplace = useNullableForReplace
            copy.customInitializationCall = customInitializationCall
            copy.optInAnnotation = optInAnnotation
        }
        copy.fromParent = fromParent
        copy.parentHasSeparateTransform = parentHasSeparateTransform
    }

    protected abstract fun internalCopy(): Field

    override fun toString(): String {
        return name
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (javaClass != other.javaClass) return false
        other as Field
        return name == other.name
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

// ----------- Field with default -----------

class FieldWithDefault(konst origin: Field) : Field() {
    override konst name: String get() = origin.name
    override konst type: String get() = origin.type
    override var isVolatile: Boolean = origin.isVolatile
    override konst nullable: Boolean get() = origin.nullable
    override var withReplace: Boolean
        get() = origin.withReplace
        set(_) {}
    override konst packageName: String? get() = origin.packageName
    override konst isFirType: Boolean get() = origin.isFirType
    override var needsSeparateTransform: Boolean
        get() = origin.needsSeparateTransform
        set(_) {}

    override var needTransformInOtherChildren: Boolean
        get() = origin.needTransformInOtherChildren
        set(_) {}

    override konst arguments: MutableList<Importable>
        get() = origin.arguments

    override konst fullQualifiedName: String?
        get() = origin.fullQualifiedName

    override var isFinal: Boolean
        get() = origin.isFinal
        set(_) {}

    override var isLateinit: Boolean
        get() = origin.isLateinit
        set(_) {}

    override var isParameter: Boolean
        get() = origin.isParameter
        set(_) {}

    override var customInitializationCall: String?
        get() = origin.customInitializationCall
        set(_) {}

    override var optInAnnotation: ArbitraryImportable?
        get() = origin.optInAnnotation
        set(_) {}

    override var defaultValueInImplementation: String? = origin.defaultValueInImplementation
    var defaultValueInBuilder: String? = null
    override var isMutable: Boolean = origin.isMutable
    override var isMutableOrEmpty: Boolean = origin.isMutableOrEmpty
    override var isMutableInInterface: Boolean = origin.isMutableInInterface
    override var withGetter: Boolean = false
    override var customSetter: String? = null
    override var fromDelegate: Boolean = false
    var needAcceptAndTransform: Boolean = true
    override konst overridenTypes: MutableSet<Importable>
        get() = origin.overridenTypes

    override konst arbitraryImportables: MutableList<Importable>
        get() = origin.arbitraryImportables

    override var useNullableForReplace: Boolean
        get() = origin.useNullableForReplace
        set(_) {}

    override fun internalCopy(): Field {
        return FieldWithDefault(origin).also {
            it.defaultValueInImplementation = defaultValueInImplementation
            it.isMutable = isMutable
            it.withGetter = withGetter
            it.fromDelegate = fromDelegate
            it.needAcceptAndTransform = needAcceptAndTransform
        }
    }
}

class SimpleField(
    override konst name: String,
    override konst type: String,
    override konst packageName: String?,
    konst customType: Importable? = null,
    override konst nullable: Boolean,
    override var withReplace: Boolean,
    override var isVolatile: Boolean = false,
    override var isFinal: Boolean = false,
    override var isLateinit: Boolean = false,
    override var isParameter: Boolean = false,
) : Field() {
    override konst isFirType: Boolean = false
    override konst fullQualifiedName: String?
        get() = customType?.fullQualifiedName ?: super.fullQualifiedName

    override var isMutable: Boolean = withReplace
    override var isMutableOrEmpty: Boolean = false

    override fun internalCopy(): Field {
        return SimpleField(
            name = name,
            type = type,
            packageName = packageName,
            customType = customType,
            nullable = nullable,
            withReplace = withReplace,
            isVolatile = isVolatile,
            isFinal = isFinal,
            isLateinit = isLateinit,
            isParameter = isParameter,
        ).apply {
            withBindThis = this@SimpleField.withBindThis
        }
    }

    fun replaceType(newType: Type) = SimpleField(
        name = name,
        type = newType.type,
        packageName = newType.packageName,
        customType = customType,
        nullable = nullable,
        withReplace = withReplace,
        isVolatile = isVolatile,
        isFinal = isFinal,
        isLateinit = isLateinit,
        isParameter = isParameter
    ).also {
        it.withBindThis = withBindThis
        updateFieldsInCopy(it)
    }
}

class FirField(
    override konst name: String,
    konst element: AbstractElement,
    override konst nullable: Boolean,
    override var withReplace: Boolean,
) : Field() {
    init {
        if (element is ElementWithArguments) {
            arguments += element.typeArguments.map { Type(null, it.name) }
        }
    }

    override konst type: String get() = element.type
    override var isVolatile: Boolean = false
    override var isFinal: Boolean = false
    override konst packageName: String? get() = element.packageName
    override konst isFirType: Boolean = true

    override var isMutable: Boolean = true
    override var isMutableOrEmpty: Boolean = false
    override var isLateinit: Boolean = false
    override var isParameter: Boolean = false

    override fun internalCopy(): Field {
        return FirField(
            name,
            element,
            nullable,
            withReplace
        ).apply {
            withBindThis = this@FirField.withBindThis
        }
    }
}

// ----------- Field list -----------

class FieldList(
    override konst name: String,
    konst baseType: Importable,
    override var withReplace: Boolean,
    useMutableOrEmpty: Boolean = false
) : Field() {
    override var defaultValueInImplementation: String? = null
    override konst packageName: String? get() = baseType.packageName
    override konst fullQualifiedName: String? get() = baseType.fullQualifiedName
    override konst type: String = "List<${baseType.typeWithArguments}>"

    override konst nullable: Boolean
        get() = false

    override var isVolatile: Boolean = false
    override var isFinal: Boolean = false
    override var isMutable: Boolean = true
    override var isMutableOrEmpty: Boolean = useMutableOrEmpty
    override var isLateinit: Boolean = false
    override var isParameter: Boolean = false

    override fun internalCopy(): Field {
        return FieldList(
            name,
            baseType,
            withReplace,
            isMutableOrEmpty
        )
    }

    override konst isFirType: Boolean = baseType is AbstractElement || (baseType is Type && baseType.firType)
}
