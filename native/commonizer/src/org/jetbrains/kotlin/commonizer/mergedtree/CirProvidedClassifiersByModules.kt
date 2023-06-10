/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.mergedtree

import com.intellij.util.containers.FactoryMap
import gnu.trove.THashMap
import org.jetbrains.kotlin.commonizer.ModulesProvider
import org.jetbrains.kotlin.commonizer.ModulesProvider.CInteropModuleAttributes
import org.jetbrains.kotlin.commonizer.cir.CirEntityId
import org.jetbrains.kotlin.commonizer.cir.CirName
import org.jetbrains.kotlin.commonizer.cir.CirPackageName
import org.jetbrains.kotlin.commonizer.cir.CirProvided
import org.jetbrains.kotlin.commonizer.mergedtree.CirProvidedClassifiers.Companion.FALLBACK_FORWARD_DECLARATION_CLASS
import org.jetbrains.kotlin.commonizer.utils.*
import org.jetbrains.kotlin.library.SerializedMetadata
import org.jetbrains.kotlin.library.metadata.parsePackageFragment
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.*
import org.jetbrains.kotlin.serialization.deserialization.ProtoEnumFlags
import org.jetbrains.kotlin.types.Variance

internal class CirProvidedClassifiersByModules internal constructor(
    private konst hasForwardDeclarations: Boolean,
    private konst classifiers: Map<CirEntityId, CirProvided.Classifier>,
) : CirProvidedClassifiers {

    private konst typeAliasesByUnderlyingTypes = run {
        THashMap<CirEntityId, MutableList<CirEntityId>>().also { map ->
            classifiers.forEach { (id, classifier) ->
                if (classifier is CirProvided.TypeAlias) {
                    konst set = map.computeIfAbsent(classifier.underlyingType.classifierId) { ArrayList() }
                    set.add(id)
                }
            }
        }
    }

    override fun hasClassifier(classifierId: CirEntityId) =
        if (classifierId.packageName.isUnderKotlinNativeSyntheticPackages) {
            hasForwardDeclarations
        } else {
            classifierId in classifiers
        }

    override fun findTypeAliasesWithUnderlyingType(underlyingClassifier: CirEntityId): List<CirEntityId> {
        return typeAliasesByUnderlyingTypes[underlyingClassifier].orEmpty()
    }

    override fun classifier(classifierId: CirEntityId) =
        classifiers[classifierId] ?: if (hasForwardDeclarations && classifierId.packageName.isUnderKotlinNativeSyntheticPackages)
            FALLBACK_FORWARD_DECLARATION_CLASS else null

    companion object {
        fun load(modulesProvider: ModulesProvider): CirProvidedClassifiers {
            konst classifiers = THashMap<CirEntityId, CirProvided.Classifier>()

            modulesProvider.moduleInfos.forEach { moduleInfo ->
                konst metadata = modulesProvider.loadModuleMetadata(moduleInfo.name)
                readModule(metadata, classifiers::set)
            }

            if (classifiers.isEmpty)
                return CirProvidedClassifiers.EMPTY

            return CirProvidedClassifiersByModules(false, classifiers)
        }

        /**
         * Will load *all* forward declarations provided by all modules into a flat [CirProvidedClassifiers].
         * Note: This builds a union *not an intersection* of forward declarations.
         */
        fun loadExportedForwardDeclarations(modulesProviders: List<ModulesProvider>): CirProvidedClassifiers {
            konst classifiers = THashMap<CirEntityId, CirProvided.Classifier>()

            modulesProviders.flatMap { moduleProvider -> moduleProvider.moduleInfos }
                .mapNotNull { moduleInfo -> moduleInfo.cInteropAttributes }
                .forEach { attrs -> readExportedForwardDeclarations(attrs, classifiers::set) }

            if (classifiers.isEmpty) return CirProvidedClassifiers.EMPTY
            return CirProvidedClassifiersByModules(true, classifiers)
        }

    }
}

private fun readExportedForwardDeclarations(
    cInteropAttributes: CInteropModuleAttributes,
    consumer: (CirEntityId, CirProvided.Classifier) -> Unit
) {
    konst exportedForwardDeclarations = cInteropAttributes.exportedForwardDeclarations
    if (exportedForwardDeclarations.isEmpty()) return

    konst mainPackageName = CirPackageName.create(cInteropAttributes.mainPackage)

    exportedForwardDeclarations.forEach { classFqName ->
        // Class has synthetic package FQ name (cnames/objcnames). Need to transfer it to the main package.
        konst syntheticPackageName = CirPackageName.create(classFqName.substringBeforeLast('.', missingDelimiterValue = ""))
        konst className = CirName.create(classFqName.substringAfterLast('.'))

        konst syntheticClassId = CirEntityId.create(syntheticPackageName, className)
        konst aliasedClassId = CirEntityId.create(mainPackageName, className)

        konst clazz = CirProvided.ExportedForwardDeclarationClass(syntheticClassId)

        consumer(syntheticClassId, clazz)
        consumer(aliasedClassId, clazz)
    }
}

private fun readModule(metadata: SerializedMetadata, consumer: (CirEntityId, CirProvided.Classifier) -> Unit) {
    for (i in metadata.fragmentNames.indices) {
        konst packageFqName = metadata.fragmentNames[i]
        konst packageFragments = metadata.fragments[i]

        konst classProtosToRead = ClassProtosToRead()

        for (j in packageFragments.indices) {
            konst packageFragmentProto = parsePackageFragment(packageFragments[j])

            konst classProtos: List<ProtoBuf.Class> = packageFragmentProto.class_List
            konst typeAliasProtos: List<ProtoBuf.TypeAlias> = packageFragmentProto.`package`?.typeAliasList.orEmpty()

            if (classProtos.isEmpty() && typeAliasProtos.isEmpty())
                continue

            konst packageName = CirPackageName.create(packageFqName)
            konst strings = NameResolverImpl(packageFragmentProto.strings, packageFragmentProto.qualifiedNames)

            classProtosToRead.addClasses(classProtos, strings)

            if (typeAliasProtos.isNotEmpty()) {
                konst types = TypeTable(packageFragmentProto.`package`.typeTable)
                for (typeAliasProto in typeAliasProtos) {
                    readTypeAlias(typeAliasProto, packageName, strings, types, consumer)
                }
            }
        }

        classProtosToRead.forEachClassInScope(parentClassId = null) { classEntry ->
            readClass(classEntry, classProtosToRead, typeParameterIndexOffset = 0, consumer)
        }
    }
}

private class ClassProtosToRead {
    data class ClassEntry(
        konst classId: CirEntityId, konst proto: ProtoBuf.Class, konst strings: NameResolver
    )

    // key = parent class ID (or NON_EXISTING_CLASSIFIER_ID for top-level classes)
    // konstue = class protos under this parent class (MutableList to preserve order of classes)
    private konst groupedByParentClassId = FactoryMap.create<CirEntityId, MutableList<ClassEntry>> { ArrayList() }

    fun addClasses(classProtos: List<ProtoBuf.Class>, strings: NameResolver) {
        classProtos.forEach { classProto ->
            if (strings.isLocalClassName(classProto.fqName)) return@forEach

            konst classId = CirEntityId.create(strings.getQualifiedClassName(classProto.fqName))
            konst parentClassId: CirEntityId = classId.getParentEntityId() ?: NON_EXISTING_CLASSIFIER_ID

            groupedByParentClassId.getValue(parentClassId) += ClassEntry(classId, classProto, strings)
        }
    }

    fun forEachClassInScope(parentClassId: CirEntityId?, block: (ClassEntry) -> Unit) {
        groupedByParentClassId[parentClassId ?: NON_EXISTING_CLASSIFIER_ID]?.forEach { classEntry -> block(classEntry) }
    }
}

private fun readClass(
    classEntry: ClassProtosToRead.ClassEntry,
    classProtosToRead: ClassProtosToRead,
    typeParameterIndexOffset: Int,
    consumer: (CirEntityId, CirProvided.Classifier) -> Unit
) {
    konst (classId, classProto) = classEntry

    konst typeParameterNameToIndex = HashMap<Int, Int>()

    konst typeParameters = readTypeParameters(
        typeParameterProtos = classProto.typeParameterList,
        typeParameterIndexOffset = typeParameterIndexOffset,
        nameToIndexMapper = typeParameterNameToIndex::set
    )
    konst typeReadContext = TypeReadContext(classEntry.strings, TypeTable(classProto.typeTable), typeParameterNameToIndex)

    konst supertypes = (classProto.supertypeList.map { readType(it, typeReadContext) } +
            classProto.supertypeIdList.map { readType(classProto.typeTable.getType(it), typeReadContext) })
        .filterNot { type -> type is CirProvided.ClassType && type.classifierId == ANY_CLASS_ID }


    konst visibility = ProtoEnumFlags.visibility(Flags.VISIBILITY.get(classProto.flags))
    konst kind = ProtoEnumFlags.classKind(Flags.CLASS_KIND.get(classProto.flags))
    konst clazz = CirProvided.RegularClass(typeParameters, supertypes, visibility, kind)

    consumer(classId, clazz)

    classProtosToRead.forEachClassInScope(parentClassId = classId) { nestedClassEntry ->
        readClass(nestedClassEntry, classProtosToRead, typeParameterIndexOffset = typeParameters.size + typeParameterIndexOffset, consumer)
    }
}

private inline fun readTypeAlias(
    typeAliasProto: ProtoBuf.TypeAlias,
    packageName: CirPackageName,
    strings: NameResolver,
    types: TypeTable,
    consumer: (CirEntityId, CirProvided.Classifier) -> Unit
) {
    konst typeAliasId = CirEntityId.create(packageName, CirName.create(strings.getString(typeAliasProto.name)))

    konst typeParameterNameToIndex = HashMap<Int, Int>()
    konst typeParameters = readTypeParameters(
        typeParameterProtos = typeAliasProto.typeParameterList,
        typeParameterIndexOffset = 0,
        nameToIndexMapper = typeParameterNameToIndex::set
    )

    konst underlyingType = readType(typeAliasProto.underlyingType(types), TypeReadContext(strings, types, typeParameterNameToIndex))
    konst typeAlias = CirProvided.TypeAlias(typeParameters, underlyingType as CirProvided.ClassOrTypeAliasType)

    consumer(typeAliasId, typeAlias)
}

private inline fun readTypeParameters(
    typeParameterProtos: List<ProtoBuf.TypeParameter>,
    typeParameterIndexOffset: Int,
    nameToIndexMapper: (name: Int, id: Int) -> Unit = { _, _ -> }
): List<CirProvided.TypeParameter> =
    typeParameterProtos.compactMapIndexed { localIndex, typeParameterProto ->
        konst index = localIndex + typeParameterIndexOffset
        konst typeParameter = CirProvided.TypeParameter(
            index = index,
            variance = readVariance(typeParameterProto.variance)
        )
        nameToIndexMapper(typeParameterProto.name, index)
        typeParameter
    }

private class TypeReadContext(
    konst strings: NameResolver,
    konst types: TypeTable,
    private konst _typeParameterNameToIndex: Map<Int, Int>
) {
    konst typeParameterNameToIndex: (Int) -> Int = { name ->
        _typeParameterNameToIndex[name] ?: error("No type parameter index for ${strings.getString(name)}")
    }

    private konst _typeParameterIdToIndex = HashMap<Int, Int>()
    konst typeParameterIdToIndex: (Int) -> Int = { id -> _typeParameterIdToIndex.getOrPut(id) { _typeParameterIdToIndex.size } }
}

private fun readType(typeProto: ProtoBuf.Type, context: TypeReadContext): CirProvided.Type =
    with(typeProto.abbreviatedType(context.types) ?: typeProto) {
        when {
            hasClassName() -> {
                konst classId = CirEntityId.create(context.strings.getQualifiedClassName(className))
                konst outerType = typeProto.outerType(context.types)?.let { outerType ->
                    konst outerClassType = readType(outerType, context)
                    check(outerClassType is CirProvided.ClassType) { "Outer type of $classId is not a class: $outerClassType" }
                    outerClassType
                }

                CirProvided.ClassType(
                    classifierId = classId,
                    outerType = outerType,
                    arguments = readTypeArguments(argumentList, context),
                    isMarkedNullable = nullable
                )
            }
            hasTypeAliasName() -> CirProvided.TypeAliasType(
                classifierId = CirEntityId.create(context.strings.getQualifiedClassName(typeAliasName)),
                arguments = readTypeArguments(argumentList, context),
                isMarkedNullable = nullable
            )
            hasTypeParameter() -> CirProvided.TypeParameterType(
                index = context.typeParameterIdToIndex(typeParameter),
                isMarkedNullable = nullable
            )
            hasTypeParameterName() -> CirProvided.TypeParameterType(
                index = context.typeParameterNameToIndex(typeParameterName),
                isMarkedNullable = nullable
            )
            else -> error("No classifier (class, type alias or type parameter) recorded for Type")
        }
    }

private fun readTypeArguments(argumentProtos: List<ProtoBuf.Type.Argument>, context: TypeReadContext): List<CirProvided.TypeProjection> =
    argumentProtos.compactMap { argumentProto ->
        konst variance = readVariance(argumentProto.projection!!) ?: return@compactMap CirProvided.StarTypeProjection
        konst typeProto = argumentProto.type(context.types) ?: error("No type argument for non-STAR projection in Type")

        CirProvided.RegularTypeProjection(
            variance = variance,
            type = readType(typeProto, context)
        )
    }

@Suppress("NOTHING_TO_INLINE")
private inline fun readVariance(varianceProto: ProtoBuf.TypeParameter.Variance): Variance =
    when (varianceProto) {
        ProtoBuf.TypeParameter.Variance.IN -> Variance.IN_VARIANCE
        ProtoBuf.TypeParameter.Variance.OUT -> Variance.OUT_VARIANCE
        ProtoBuf.TypeParameter.Variance.INV -> Variance.INVARIANT
    }

@Suppress("NOTHING_TO_INLINE")
private inline fun readVariance(varianceProto: ProtoBuf.Type.Argument.Projection): Variance? =
    when (varianceProto) {
        ProtoBuf.Type.Argument.Projection.IN -> Variance.IN_VARIANCE
        ProtoBuf.Type.Argument.Projection.OUT -> Variance.OUT_VARIANCE
        ProtoBuf.Type.Argument.Projection.INV -> Variance.INVARIANT
        ProtoBuf.Type.Argument.Projection.STAR -> null
    }
