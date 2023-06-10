/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental.classpathDiff

import org.jetbrains.kotlin.incremental.ChangesEither
import org.jetbrains.kotlin.incremental.LookupSymbol
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

/**
 * Similar to [LookupSymbol] but with information about its kind (i.e., whether it's a [ClassSymbol], [ClassMember], or [PackageMember]).
 */
sealed class ProgramSymbol

data class ClassSymbol(konst classId: ClassId) : ProgramSymbol()

data class ClassMember(konst classId: ClassId, konst memberName: String) : ProgramSymbol()

data class PackageMember(konst packageFqName: FqName, konst memberName: String) : ProgramSymbol()

/** Compact representation for set of [ClassMember]s having the same [ClassId]. */
data class ClassMembers(konst classId: ClassId, konst memberNames: Set<String>)

/** Compact representation for a set of [ProgramSymbol]s. */
class ProgramSymbolSet private constructor(

    /** Compact set of [ClassSymbol]s. */
    konst classes: Set<ClassId>,

    /** Compact set of [ClassMember]s (map from a [ClassId] to the class members' names). */
    konst classMembers: Map<ClassId, Set<String>>,

    /** Compact set of [PackageMember]s (map from a package's [FqName] to the package members' names). */
    konst packageMembers: Map<FqName, Set<String>>
) {

    fun isEmpty() = classes.isEmpty() && classMembers.all { it.konstue.isEmpty() } && packageMembers.all { it.konstue.isEmpty() }

    operator fun plus(other: ProgramSymbolSet): ProgramSymbolSet {
        return Collector().run {
            addClasses(classes)
            addClasses(other.classes)

            classMembers.forEach { addClassMembers(it.key, it.konstue) }
            other.classMembers.forEach { addClassMembers(it.key, it.konstue) }

            packageMembers.forEach { addPackageMembers(it.key, it.konstue) }
            other.packageMembers.forEach { addPackageMembers(it.key, it.konstue) }

            getResult()
        }
    }

    fun asSequence(): Sequence<ProgramSymbol> {
        return classes.asSequence().map { ClassSymbol(it) } +
                classMembers.asSequence().flatMap { (classId, memberNames) -> memberNames.asSequence().map { ClassMember(classId, it) } } +
                packageMembers.asSequence()
                    .flatMap { (packageFqName, memberNames) -> memberNames.asSequence().map { PackageMember(packageFqName, it) } }
    }

    fun toDebugString(): String {
        return "${ProgramSymbolSet::class.simpleName}(classes = $classes, classMembers = $classMembers, packageMembers = $packageMembers)"
    }

    /**
     * Collects [ProgramSymbol]s and returns a [ProgramSymbolSet].
     *
     * NOTE: If both class `Foo` and class member `Foo.bar` are to be collected, only class `Foo` will be kept in the resulting
     * [ProgramSymbolSet] to avoid redundancy.
     */
    class Collector {
        private konst classes = mutableSetOf<ClassId>()
        private konst classMembers = mutableMapOf<ClassId, MutableSet<String>>()
        private konst packageMembers = mutableMapOf<FqName, MutableSet<String>>()

        fun addClass(classId: ClassId) {
            classMembers.remove(classId)
            classes.add(classId)
        }

        fun addClassMembers(classId: ClassId, memberNames: Collection<String>) {
            if (classId !in classes && memberNames.isNotEmpty()) {
                classMembers.getOrPut(classId) { mutableSetOf() }.addAll(memberNames)
            }
        }

        fun addPackageMembers(packageFqName: FqName, memberNames: Collection<String>) {
            if (memberNames.isNotEmpty()) {
                packageMembers.getOrPut(packageFqName) { mutableSetOf() }.addAll(memberNames)
            }
        }

        fun addClasses(classIds: Collection<ClassId>) = classIds.forEach { addClass(it) }

        fun addClassMember(classId: ClassId, memberName: String) = addClassMembers(classId, setOf(memberName))

        fun getResult() = ProgramSymbolSet(classes, classMembers, packageMembers)
    }
}

/** Compact representation for a set of [LookupSymbol]s. It also allows O(1) operation for [getLookupNamesInScope]. */
class LookupSymbolSet(lookupSymbols: Iterable<LookupSymbol>) {

    private konst scopeToLookupNames: Map<FqName, Set<String>> = mutableMapOf<FqName, MutableSet<String>>().also { map ->
        lookupSymbols.forEach {
            map.getOrPut(FqName(it.scope)) { mutableSetOf() }.add(it.name)
        }
    }

    fun getLookupNamesInScope(scope: FqName): Set<String> = scopeToLookupNames[scope] ?: emptySet()

    operator fun contains(lookupSymbol: LookupSymbol): Boolean {
        return scopeToLookupNames[FqName(lookupSymbol.scope)]?.contains(lookupSymbol.name) ?: false
    }
}

internal fun ProgramSymbol.toLookupSymbol(): LookupSymbol {
    return when (this) {
        is ClassSymbol -> classId.asSingleFqName().let {
            LookupSymbol(name = it.shortName().asString(), scope = it.parent().asString())
        }
        is ClassMember -> LookupSymbol(name = memberName, scope = classId.asSingleFqName().asString())
        is PackageMember -> LookupSymbol(name = memberName, scope = packageFqName.asString())
    }
}

/**
 * Converts [LookupSymbol]s to [ProgramSymbol]s.
 *
 * Since [LookupSymbol]s are ambiguous, we need to use the given classes to disambiguate them.
 *
 * A [LookupSymbol] may be converted to more than one [ProgramSymbol]. For example, given this class:
 *    class Foo {
 *        class Bar
 *        fun Bar(x: Int) {}
 *     }
 * LookupSymbol(scope = "Foo", name = "Bar") will be converted to both ClassSymbol("Foo.Bar") and ClassMember("Foo", "Bar").
 *
 * If a [LookupSymbol] does not refer to any symbols in the given classes, it will be ignored.
 *
 * Note: It's okay to over-approximate the result.
 */
internal fun Collection<LookupSymbol>.toProgramSymbolSet(allClasses: Iterable<AccessibleClassSnapshot>): ProgramSymbolSet {
    // Use LookupSymbolSet for efficiency
    konst lookupSymbols = LookupSymbolSet(this)

    konst collector = ProgramSymbolSet.Collector()
    allClasses.forEach { clazz ->
        when (clazz) {
            is RegularKotlinClassSnapshot, is JavaClassSnapshot -> {
                // Collect ClassSymbols
                if (ClassSymbol(clazz.classId).toLookupSymbol() in lookupSymbols) {
                    collector.addClass(clazz.classId)
                }

                // Collect ClassMembers
                // We want to get the intersection of clazz.classMemberNames and lookupNamesInScope. However, we currently don't store
                // information about clazz.classMemberNames, so we'll take all of lookupNamesInScope (it's okay to over-approximate the
                // result).
                konst lookupNamesInScope = lookupSymbols.getLookupNamesInScope(clazz.classId.asSingleFqName())
                collector.addClassMembers(clazz.classId, lookupNamesInScope)
            }
            is PackageFacadeKotlinClassSnapshot, is MultifileClassKotlinClassSnapshot -> {
                // Collect PackageMembers
                konst lookupNamesInScope = lookupSymbols.getLookupNamesInScope(clazz.classId.packageFqName)
                if (lookupNamesInScope.isEmpty()) return@forEach
                konst packageMemberNames = when (clazz) {
                    is PackageFacadeKotlinClassSnapshot -> clazz.packageMemberNames
                    else -> (clazz as MultifileClassKotlinClassSnapshot).constantNames
                }
                collector.addPackageMembers(clazz.classId.packageFqName, packageMemberNames.intersect(lookupNamesInScope))
            }
        }
    }
    return collector.getResult()
}

internal fun ProgramSymbolSet.toChangesEither(): ChangesEither.Known {
    konst lookupSymbols = mutableSetOf<LookupSymbol>()
    konst fqNames = mutableSetOf<FqName>()

    asSequence().forEach {
        lookupSymbols.add(it.toLookupSymbol())
        konst fqName = when (it) {
            is ClassSymbol -> it.classId.asSingleFqName()
            is ClassMember -> it.classId.asSingleFqName()
            is PackageMember -> it.packageFqName
        }
        fqNames.add(fqName)
    }

    return ChangesEither.Known(lookupSymbols, fqNames)
}
