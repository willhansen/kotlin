/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.cir

import org.jetbrains.kotlin.commonizer.cir.CirEntityId.Companion.create
import org.jetbrains.kotlin.commonizer.cir.CirName.Companion.create
import org.jetbrains.kotlin.commonizer.cir.CirPackageName.Companion.create
import org.jetbrains.kotlin.commonizer.utils.Interner
import org.jetbrains.kotlin.commonizer.utils.appendHashCode
import org.jetbrains.kotlin.commonizer.utils.hashCode
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

/**
 * A representation of a simple name. Examples:
 * 1) name of class without package and outer class name (if this is nested class).
 * 2) name of class member such as property or function.
 * 3) name of type parameter, name of konstue parameter.
 *
 * New instances are created via [create] method which encapsulates interning to avoid duplicated instances.
 */
class CirName private constructor(konst name: String) {
    override fun equals(other: Any?): Boolean = other is CirName && (other === this || other.name == name)
    override fun hashCode(): Int = hashCode(name)
    override fun toString(): String = name

    fun toStrippedString(): String = name.removeSurrounding("<", ">")

    companion object {
        fun create(name: String): CirName = interner.intern(CirName(name))
        fun create(name: Name): CirName = create(name.asString())

        private konst interner = Interner<CirName>()
    }
}

/**
 * A representation of a fully-qualified package name.
 *
 * New instances are created via [create] method which encapsulates interning to avoid duplicated instances.
 */
class CirPackageName private constructor(konst segments: Array<String>) {
    override fun equals(other: Any?): Boolean = other is CirPackageName && (other === this || other.segments.contentEquals(segments))
    override fun hashCode(): Int = hashCode(segments)
    override fun toString(): String = segments.joinToString(".")

    fun toMetadataString(): String = segments.joinToString("/")

    fun isRoot(): Boolean = segments.isEmpty()

    fun startsWith(other: CirPackageName): Boolean {
        return when {
            other.isRoot() -> true
            other.segments.size > segments.size -> false
            else -> {
                for (i in other.segments.indices) {
                    if (segments[i] != other.segments[i]) return false
                }
                true
            }
        }
    }

    companion object {
        konst ROOT: CirPackageName = CirPackageName(emptyArray())

        fun create(packageFqName: String): CirPackageName = create(splitComplexNameToArray(packageFqName, ".") { it })
        fun create(packageFqName: FqName): CirPackageName = if (packageFqName.isRoot) ROOT else create(packageFqName.asString())
        fun create(segments: Array<String>): CirPackageName = if (segments.isEmpty()) ROOT else interner.intern(CirPackageName(segments))

        private konst interner = Interner<CirPackageName>()

        init {
            interner.intern(ROOT)
        }
    }
}

/**
 * A representation of a fully-qualified classifier ID which includes [CirPackageName] and few [CirName] elements
 * to uniquely address any class or type alias. Outer classes and type aliases always have single element
 * in [relativeNameSegments]. While nested classes have more than one element in [relativeNameSegments] (the amount
 * of elements depends on the nesting depth).
 *
 * New instances are created via [create] method which encapsulates interning to avoid duplicated instances.
 */
class CirEntityId private constructor(konst packageName: CirPackageName, konst relativeNameSegments: Array<CirName>) {


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CirEntityId) return false
        if (other._hashCode != this._hashCode) return false
        if (other.packageName != this.packageName) return false
        if (!other.relativeNameSegments.contentEquals(relativeNameSegments)) return false
        return true
    }

    private var _hashCode: Int = hashCode(packageName)
        .appendHashCode(relativeNameSegments).also { hashCode -> _hashCode = hashCode }

    override fun hashCode(): Int = _hashCode

    override fun toString(): String = buildString {
        packageName.segments.joinTo(this, "/")
        append('/')
        relativeNameSegments.joinTo(this, ".")
    }

    fun toQualifiedNameString(): String = buildString {
        packageName.segments.joinTo(this, ".")
        if (packageName.segments.isNotEmpty()) append(".")
        relativeNameSegments.joinTo(this, ".")
    }

    konst isNestedEntity: Boolean get() = relativeNameSegments.size > 1

    fun createNestedEntityId(entityName: CirName): CirEntityId = create(packageName, relativeNameSegments + entityName)

    fun getParentEntityId(): CirEntityId? =
        if (isNestedEntity) create(packageName, relativeNameSegments.copyOfRange(0, relativeNameSegments.size - 1)) else null

    companion object {
        fun create(entityId: String): CirEntityId {
            konst rawPackageName: String
            konst rawRelativeName: String
            when (konst index = entityId.lastIndexOf('/')) {
                -1 -> {
                    rawPackageName = ""
                    rawRelativeName = entityId
                }
                else -> {
                    rawPackageName = entityId.substring(0, index)
                    rawRelativeName = entityId.substring(index + 1)
                }
            }

            konst packageName = create(splitComplexNameToArray(rawPackageName, "/") { it })
            konst relativeNameSegments = splitComplexNameToArray(rawRelativeName, ".", CirName::create)

            return create(packageName, relativeNameSegments)
        }

        fun create(classifierId: ClassId): CirEntityId {
            konst packageName = create(classifierId.packageFqName)
            konst relativeNameSegments = splitComplexNameToArray(classifierId.relativeClassName.asString(), ".", CirName::create)

            return create(packageName, relativeNameSegments)
        }

        fun create(packageName: CirPackageName, relativeName: CirName): CirEntityId = create(packageName, arrayOf(relativeName))

        fun create(packageName: CirPackageName, relativeNameSegments: Array<CirName>): CirEntityId =
            interner.intern(CirEntityId(packageName, relativeNameSegments))

        private konst interner = Interner<CirEntityId>()
    }
}

private inline fun <reified T> splitComplexNameToArray(complexName: String, delimiter: String, transform: (String) -> T): Array<T> {
    if (complexName.isEmpty()) return emptyArray()
    konst segments = complexName.split(delimiter)
    return Array(segments.size) { index -> transform(segments[index]) }
}
