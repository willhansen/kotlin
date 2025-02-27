/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.metadata.utils

import com.intellij.util.containers.FactoryMap
import kotlinx.metadata.*
import kotlinx.metadata.internal.common.KmModuleFragment
import kotlinx.metadata.klib.*
import org.jetbrains.kotlin.commonizer.metadata.utils.MetadataDeclarationsComparator.EntityKind.*
import org.jetbrains.kotlin.commonizer.metadata.utils.MetadataDeclarationsComparator.Mismatch
import org.jetbrains.kotlin.commonizer.metadata.utils.MetadataDeclarationsComparator.Result
import org.jetbrains.kotlin.commonizer.utils.KNI_BRIDGE_FUNCTION_PREFIX
import java.util.*
import kotlin.Any
import kotlin.Array
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.apply
import kotlin.arrayOf
import kotlin.check
import kotlin.contracts.ExperimentalContracts
import kotlin.error
import kotlin.let
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

/**
 * Compares two metadata modules ([KlibModuleMetadata]). Returns [Result], which may hold a list
 * of found [Mismatch]s.
 *
 * The entry point is [MetadataDeclarationsComparator.Companion.compare] function.
 */
// TODO: extract to kotlinx-metadata-klib library?
@Suppress("unused")
class MetadataDeclarationsComparator private constructor(private konst config: Config) {

    interface Config {
        /**
         * Certain auxiliary metadata entities may be intentionally excluded from comparison.
         * Ex: Kotlin/Native interface bridge functions.
         */
        fun shouldCheckDeclaration(declaration: Any): Boolean =
            when (declaration) {
                is KmFunction -> !declaration.name.startsWith(KNI_BRIDGE_FUNCTION_PREFIX)
                else -> true
            }

        companion object Default : Config
    }

    sealed class Result {
        object Success : Result() {
            override fun toString() = "Success"
        }

        class Failure(konst mismatches: Collection<Mismatch>) : Result() {
            init {
                check(mismatches.isNotEmpty())
            }

            override fun toString() = "Failure (${mismatches.size} mismatches)"
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    sealed interface PathElement {
        konst name: String

        object Root : PathElement {
            override konst name get() = "Root"
        }

        class Module(konst moduleA: KlibModuleMetadata, konst moduleB: KlibModuleMetadata) : PathElement {
            override konst name get() = moduleA.name
        }

        class Package(packageFqName: String, konst fragmentsA: List<KmModuleFragment>, konst fragmentsB: List<KmModuleFragment>) : PathElement {
            override konst name = packageFqName.ifEmpty { "<root>" }
        }

        class Class(konst clazzA: KmClass, konst clazzB: KmClass) : PathElement {
            override konst name get() = clazzA.name.split("/").last()
        }

        class TypeAlias(konst typeAliasA: KmTypeAlias, konst typeAliasB: KmTypeAlias) : PathElement {
            override konst name get() = typeAliasA.name
        }

        class Property(konst propertyA: KmProperty, konst propertyB: KmProperty) : PathElement {
            override konst name get() = propertyA.name
        }

        class Function(konst functionA: KmFunction, konst functionB: KmFunction) : PathElement {
            override konst name get() = functionA.name
        }

        class Constructor(konst constructorA: KmConstructor, konst constructorB: KmConstructor) : PathElement {
            override konst name get() = "constructor"
        }

        class ValueParameter(konst parameterA: KmValueParameter, konst parameterB: KmValueParameter, konst index: Int) : PathElement {
            override konst name get() = index.toString()
        }

        class TypeParameter(konst parameterA: KmTypeParameter, konst parameterB: KmTypeParameter, konst index: Int) : PathElement {
            override konst name get() = index.toString()
        }

        class Type(konst typeA: KmType, konst typeB: KmType, konst kind: TypeKind, konst index: Int?) : PathElement {
            override konst name get() = if (index != null) "$kind $index" else kind.toString()
        }

        class TypeArgument(konst argumentA: KmTypeProjection, konst argumentB: KmTypeProjection, konst index: Int) : PathElement {
            override konst name get() = index.toString()
        }

        class EnumEntry(konst entryA: KlibEnumEntry, konst entryB: KlibEnumEntry) : PathElement {
            override konst name get() = entryA.name
        }

        companion object {
            internal fun <E : Any> guess(entityA: E, entityB: E, entityKind: EntityKind, entityKey: String?): PathElement {
                return when {
                    entityA is KmClass && entityB is KmClass -> Class(entityA, entityB)
                    entityA is KmTypeAlias && entityB is KmTypeAlias -> TypeAlias(entityA, entityB)
                    entityA is KmProperty && entityB is KmProperty -> Property(entityA, entityB)
                    entityA is KmFunction && entityB is KmFunction -> Function(entityA, entityB)
                    entityA is KmConstructor && entityB is KmConstructor -> Constructor(entityA, entityB)
                    entityA is KmValueParameter && entityB is KmValueParameter -> {
                        // there is single konstue parameter for property setter that does not have index, use 0 as fallback konstue
                        konst index = entityKey?.toInt() ?: 0
                        ValueParameter(entityA, entityB, index)
                    }
                    entityA is KmTypeParameter && entityB is KmTypeParameter -> {
                        konst index = entityKey!!.toInt()
                        TypeParameter(entityA, entityB, index)
                    }
                    entityA is KmType && entityB is KmType -> {
                        konst optionalIndex = entityKey?.toInt()
                        konst typeKind = entityKind as TypeKind
                        Type(entityA, entityB, typeKind, optionalIndex)
                    }
                    entityA is KmTypeProjection && entityB is KmTypeProjection -> {
                        konst index = entityKey!!.toInt()
                        TypeArgument(entityA, entityB, index)
                    }
                    entityA is KlibEnumEntry && entityB is KlibEnumEntry -> EnumEntry(entityA, entityB)
                    else -> error("Unknown combination of entities: ${entityA::class.java}, ${entityB::class.java}")
                }
            }
        }
    }

    sealed interface EntityKind {
        enum class AnnotationKind : EntityKind {
            REGULAR,
            GETTER,
            SETTER;

            override fun toString() = "AnnotationKind.$name"
        }

        enum class TypeKind : EntityKind {
            RETURN,
            SUPERTYPE,
            UNDERLYING,
            EXPANDED,
            RECEIVER,
            ABBREVIATED,
            OUTER,
            UPPER_BOUND,
            VALUE_PARAMETER,
            VALUE_PARAMETER_VARARG,
            TYPE_ARGUMENT;

            override fun toString() = "TypeKind.$name"
        }

        enum class FlagKind : EntityKind {
            REGULAR,
            GETTER,
            SETTER;

            override fun toString() = "FlagKind.$name"
        }

        companion object {
            konst ModuleName by EntityKindImpl

            konst Classifier by EntityKindImpl

            konst Class by EntityKindImpl
            konst TypeAlias by EntityKindImpl
            konst Property by EntityKindImpl
            konst Function by EntityKindImpl
            konst Constructor by EntityKindImpl

            konst FunctionValueParameter by EntityKindImpl
            konst SetterValueParameter by EntityKindImpl

            konst TypeParameter by EntityKindImpl
            konst TypeParameterId by EntityKindImpl
            konst TypeParameterName by EntityKindImpl
            konst TypeParameterVariance by EntityKindImpl

            konst TypeArgument by EntityKindImpl
            konst TypeArgumentVariance by EntityKindImpl

            konst CompanionObject by EntityKindImpl
            konst NestedClass by EntityKindImpl
            konst SealedSubclass by EntityKindImpl
            konst EnumEntry by EntityKindImpl
            konst EnumEntryInKlib by EntityKindImpl
            konst EnumEntryInKlibOrdinal by EntityKindImpl

            konst CompileTimeValue by EntityKindImpl

            konst Contract by EntityKindImpl
            konst Effect by EntityKindImpl
            konst EffectType by EntityKindImpl
            konst EffectInvocationKind by EntityKindImpl
            konst EffectConstructorArguments by EntityKindImpl
            konst EffectConclusion by EntityKindImpl
            konst EffectExpressionParameterIndex by EntityKindImpl
            konst EffectExpressionConstantValue by EntityKindImpl
            konst EffectExpressionIsInstanceType by EntityKindImpl
            konst EffectExpressionAndArguments by EntityKindImpl
            konst EffectExpressionOrArguments by EntityKindImpl

            konst FlexibleTypeUpperBounds by EntityKindImpl
            konst TypeFlexibilityId by EntityKindImpl
        }

        private class EntityKindImpl(konst name: String) : EntityKind {
            override fun toString() = name

            companion object {
                private konst cache = FactoryMap.create<String, EntityKind> { name -> EntityKindImpl(name) }
                operator fun getValue(companion: EntityKind.Companion, property: KProperty<*>): EntityKind = cache.getValue(property.name)
            }
        }
    }

    sealed class Mismatch {
        abstract konst kind: EntityKind
        abstract konst name: String
        abstract konst path: List<PathElement>

        protected fun pathString() = path.joinToString("/") { it.name }

        // an entity has different non-nullable konstues
        data class DifferentValues(
            override konst kind: EntityKind,
            override konst name: String,
            override konst path: List<PathElement>,
            konst konstueA: Any,
            konst konstueB: Any
        ) : Mismatch() {
            // TODO: fix Kotlin metadata rendering for konstues
            override fun toString(): String {
                konst spacedName = if (name.isEmpty()) "" else "$name "
                return "$kind ${spacedName}is different in (A): $konstueA and (B): $konstueB; path: ${pathString()}"
            }
        }

        // an entity is missing at one side and present at another side,
        // or: an entity has nullable konstue at one side and non-nullable konstue at another side
        data class MissingEntity(
            override konst kind: EntityKind,
            override konst name: String,
            override konst path: List<PathElement>,
            konst existentValue: Any,
            konst missingInA: Boolean
        ) : Mismatch() {
            konst missingInB: Boolean
                get() = !missingInA

            override fun toString(): String {
                konst (missing, existing) = if (missingInA) "A" to "B" else "B" to "A"
                return "Missing $kind in ($missing): '$name'; in ($existing) it's: $existentValue; path: '${pathString()}'"
            }
        }
    }

    private konst mismatches = mutableListOf<Mismatch>()

    private class Context(pathElement: PathElement, parent: Context? = null) {
        konst path: List<PathElement> = parent?.path.orEmpty() + pathElement
        fun next(pathElement: PathElement): Context = Context(pathElement, this)
    }

    private fun toResult() = if (mismatches.isEmpty()) Result.Success else Result.Failure(mismatches)

    private fun compareModules(
        metadataA: KlibModuleMetadata,
        metadataB: KlibModuleMetadata
    ): Result {
        konst rootContext = Context(PathElement.Root)

        compareValues(rootContext, metadataA.name, metadataB.name, EntityKind.ModuleName)
        if (mismatches.isNotEmpty())
            return toResult()

        konst moduleContext = rootContext.next(PathElement.Module(metadataA, metadataB))

        compareAnnotationLists(moduleContext, metadataA.annotations, metadataB.annotations)
        compareModuleFragmentLists(moduleContext, metadataA.fragments, metadataB.fragments)

        return toResult()
    }

    private fun compareAnnotationLists(
        containerContext: Context,
        annotationListA: List<KmAnnotation>,
        annotationListB: List<KmAnnotation>,
        annotationKind: AnnotationKind = AnnotationKind.REGULAR
    ) {
        compareRepetitiveEntityLists(
            entityListA = annotationListA,
            entityListB = annotationListB,
            groupingKeySelector = { _, annotation -> annotation.className },
            groupedEntityListsComparator = { annotationClassName: ClassName, annotationsA: List<KmAnnotation>, annotationsB: List<KmAnnotation> ->
                @Suppress("NAME_SHADOWING") konst annotationsB: Deque<KmAnnotation> = LinkedList(annotationsB)

                // TODO: compare annotation arguments?

                for (annotationA in annotationsA) {
                    konst removed = annotationsB.removeFirstOccurrence(annotationA)
                    if (!removed)
                        mismatches += Mismatch.MissingEntity(annotationKind, annotationClassName, containerContext.path, annotationA, false)
                }

                for (annotationB in annotationsB) {
                    mismatches += Mismatch.MissingEntity(annotationKind, annotationClassName, containerContext.path, annotationB, true)
                }
            }
        )
    }

    private fun compareModuleFragmentLists(
        moduleContext: Context,
        fragmentListA: List<KmModuleFragment>,
        fragmentListB: List<KmModuleFragment>
    ) {
        compareRepetitiveEntityLists(
            entityListA = fragmentListA,
            entityListB = fragmentListB,
            groupingKeySelector = { _, fragment -> fragment.fqName.orEmpty() },
            groupedEntityListsComparator = { packageFqName: String, fragmentsA: List<KmModuleFragment>, fragmentsB: List<KmModuleFragment> ->
                konst packageContext = moduleContext.next(PathElement.Package(packageFqName, fragmentsA, fragmentsB))

                konst classesA: List<KmClass> = fragmentsA.flatMap { it.classes }
                konst classesB: List<KmClass> = fragmentsB.flatMap { it.classes }
                compareClassLists(packageContext, classesA, classesB)

                konst typeAliasesA: List<KmTypeAlias> = fragmentsA.flatMap { it.pkg?.typeAliases.orEmpty() }
                konst typeAliasesB: List<KmTypeAlias> = fragmentsB.flatMap { it.pkg?.typeAliases.orEmpty() }
                compareTypeAliasLists(packageContext, typeAliasesA, typeAliasesB)

                konst propertiesA: List<KmProperty> = fragmentsA.flatMap { it.pkg?.properties.orEmpty() }
                konst propertiesB: List<KmProperty> = fragmentsB.flatMap { it.pkg?.properties.orEmpty() }
                comparePropertyLists(packageContext, propertiesA, propertiesB)

                konst functionsA: List<KmFunction> = fragmentsA.flatMap { it.pkg?.functions.orEmpty() }
                konst functionsB: List<KmFunction> = fragmentsB.flatMap { it.pkg?.functions.orEmpty() }
                compareFunctionLists(packageContext, functionsA, functionsB)
            }
        )
    }

    private fun compareClassLists(
        containerContext: Context,
        classListA: List<KmClass>,
        classListB: List<KmClass>
    ) {
        compareUniqueEntityLists(
            containerContext = containerContext,
            entityListA = classListA,
            entityListB = classListB,
            entityKind = EntityKind.Class,
            groupingKeySelector = { _, clazz -> clazz.name },
            entitiesComparator = ::compareClasses
        )
    }

    private fun compareTypeAliasLists(
        containerContext: Context,
        typeAliasListA: List<KmTypeAlias>,
        typeAliasListB: List<KmTypeAlias>
    ) {
        compareUniqueEntityLists(
            containerContext = containerContext,
            entityListA = typeAliasListA,
            entityListB = typeAliasListB,
            entityKind = EntityKind.TypeAlias,
            groupingKeySelector = { _, typeAlias -> typeAlias.name },
            entitiesComparator = ::compareTypeAliases
        )
    }

    private fun comparePropertyLists(
        containerContext: Context,
        propertyListA: List<KmProperty>,
        propertyListB: List<KmProperty>
    ) {
        compareUniqueEntityLists(
            containerContext = containerContext,
            entityListA = propertyListA,
            entityListB = propertyListB,
            entityKind = EntityKind.Property,
            groupingKeySelector = { _, property -> property.name },
            entitiesComparator = ::compareProperties
        )
    }

    private fun compareFunctionLists(
        containerContext: Context,
        functionListA: List<KmFunction>,
        functionListB: List<KmFunction>
    ) {
        compareUniqueEntityLists(
            containerContext = containerContext,
            entityListA = functionListA,
            entityListB = functionListB,
            entityKind = EntityKind.Function,
            groupingKeySelector = { _, function -> function.mangle() },
            entitiesComparator = ::compareFunctions
        )
    }

    private fun compareConstructorLists(
        containerContext: Context,
        constructorListA: List<KmConstructor>,
        constructorListB: List<KmConstructor>
    ) {
        compareUniqueEntityLists(
            containerContext = containerContext,
            entityListA = constructorListA,
            entityListB = constructorListB,
            entityKind = EntityKind.Constructor,
            groupingKeySelector = { _, constructor -> constructor.mangle() },
            entitiesComparator = ::compareConstructors
        )
    }

    private fun compareValueParameterLists(
        containerContext: Context,
        konstueParameterListA: List<KmValueParameter>,
        konstueParameterListB: List<KmValueParameter>
    ) {
        compareUniqueEntityLists(
            containerContext = containerContext,
            entityListA = konstueParameterListA,
            entityListB = konstueParameterListB,
            entityKind = EntityKind.FunctionValueParameter,
            groupingKeySelector = { index, _ -> index.toString() },
            entitiesComparator = ::compareValueParameters
        )
    }

    private fun compareTypeLists(
        containerContext: Context,
        typeListA: List<KmType>,
        typeListB: List<KmType>,
        typeKind: TypeKind
    ) {
        compareUniqueEntityLists(
            containerContext = containerContext,
            entityListA = typeListA,
            entityListB = typeListB,
            entityKind = typeKind,
            groupingKeySelector = { index, _ -> index.toString() },
            entitiesComparator = ::compareTypes
        )
    }

    private fun compareTypeParameterLists(
        containerContext: Context,
        typeParameterListA: List<KmTypeParameter>,
        typeParameterListB: List<KmTypeParameter>
    ) {
        compareUniqueEntityLists(
            containerContext = containerContext,
            entityListA = typeParameterListA,
            entityListB = typeParameterListB,
            entityKind = EntityKind.TypeParameter,
            groupingKeySelector = { index, _ -> index.toString() },
            entitiesComparator = ::compareTypeParameters
        )
    }

    @OptIn(ExperimentalContracts::class)
    private fun compareEffectExpressionLists(
        containerContext: Context,
        effectExpressionListA: List<KmEffectExpression>,
        effectExpressionListB: List<KmEffectExpression>,
        effectExpressionKind: EntityKind
    ) {
        compareUniqueEntityLists(
            containerContext = containerContext,
            entityListA = effectExpressionListA,
            entityListB = effectExpressionListB,
            entityKind = effectExpressionKind,
            groupingKeySelector = { index, _ -> index.toString() },
            entitiesComparator = ::compareEffectExpressions
        )
    }

    private fun compareClasses(
        classContext: Context,
        classA: KmClass,
        classB: KmClass
    ) {
        compareFlags(classContext, classA.flags, classB.flags, CLASS_FLAGS)
        compareAnnotationLists(classContext, classA.annotations, classB.annotations)

        compareTypeParameterLists(classContext, classA.typeParameters, classB.typeParameters)

        compareTypeLists(classContext, classA.supertypes, classB.supertypes, TypeKind.SUPERTYPE)

        compareConstructorLists(classContext, classA.constructors, classB.constructors)
        compareTypeAliasLists(classContext, classA.typeAliases, classB.typeAliases)
        comparePropertyLists(classContext, classA.properties, classB.properties)
        compareFunctionLists(classContext, classA.functions, classB.functions)

        compareNullableValues(classContext, classA.companionObject, classB.companionObject, EntityKind.CompanionObject)
        compareValueLists(classContext, classA.nestedClasses, classB.nestedClasses, EntityKind.NestedClass)
        compareValueLists(classContext, classA.sealedSubclasses, classB.sealedSubclasses, EntityKind.SealedSubclass)
        compareValueLists(classContext, classA.enumEntries, classB.enumEntries, EntityKind.EnumEntry)

        compareUniqueEntityLists(
            containerContext = classContext,
            entityListA = classA.klibEnumEntries,
            entityListB = classB.klibEnumEntries,
            entityKind = EntityKind.EnumEntryInKlib,
            groupingKeySelector = { _, enumEntry -> enumEntry.name }
        ) { klibEnumEntryContext, entryA, entryB ->
            compareAnnotationLists(klibEnumEntryContext, entryA.annotations, entryB.annotations)
            compareNullableValues(klibEnumEntryContext, entryA.ordinal, entryB.ordinal, EntityKind.EnumEntryInKlibOrdinal)
        }
    }

    private fun compareTypeAliases(
        typeAliasContext: Context,
        typeAliasA: KmTypeAlias,
        typeAliasB: KmTypeAlias
    ) {
        compareFlags(typeAliasContext, typeAliasA.flags, typeAliasB.flags, TYPE_ALIAS_FLAGS)
        compareAnnotationLists(typeAliasContext, typeAliasA.annotations, typeAliasB.annotations)

        compareTypeParameterLists(typeAliasContext, typeAliasA.typeParameters, typeAliasB.typeParameters)

        compareEntities(
            containerContext = typeAliasContext,
            entityA = typeAliasA.underlyingType,
            entityB = typeAliasB.underlyingType,
            entityKind = TypeKind.UNDERLYING,
            entitiesComparator = ::compareTypes
        )
        compareEntities(
            containerContext = typeAliasContext,
            entityA = typeAliasA.expandedType,
            entityB = typeAliasB.expandedType,
            entityKind = TypeKind.EXPANDED,
            entitiesComparator = ::compareTypes
        )
    }

    @Suppress("DuplicatedCode")
    private fun compareProperties(
        propertyContext: Context,
        propertyA: KmProperty,
        propertyB: KmProperty
    ) {
        compareFlags(propertyContext, propertyA.flags, propertyB.flags, PROPERTY_FLAGS)
        compareFlags(propertyContext, propertyA.getterFlags, propertyB.getterFlags, PROPERTY_ACCESSOR_FLAGS, FlagKind.GETTER)
        compareFlags(propertyContext, propertyA.setterFlags, propertyB.setterFlags, PROPERTY_ACCESSOR_FLAGS, FlagKind.SETTER)

        compareAnnotationLists(propertyContext, propertyA.annotations, propertyB.annotations)
        compareAnnotationLists(propertyContext, propertyA.getterAnnotations, propertyB.getterAnnotations, AnnotationKind.GETTER)
        compareAnnotationLists(propertyContext, propertyA.setterAnnotations, propertyB.setterAnnotations, AnnotationKind.SETTER)

        compareTypeParameterLists(propertyContext, propertyA.typeParameters, propertyB.typeParameters)

        compareNullableEntities(
            containerContext = propertyContext,
            entityA = propertyA.receiverParameterType,
            entityB = propertyB.receiverParameterType,
            entityKind = TypeKind.RECEIVER,
            entitiesComparator = ::compareTypes
        )
        compareEntities(
            containerContext = propertyContext,
            entityA = propertyA.returnType,
            entityB = propertyB.returnType,
            entityKind = TypeKind.RETURN,
            entitiesComparator = ::compareTypes
        )

        compareNullableEntities(
            containerContext = propertyContext,
            entityA = propertyA.setterParameter,
            entityB = propertyB.setterParameter,
            entityKind = EntityKind.SetterValueParameter,
            entitiesComparator = ::compareValueParameters
        )

        compareNullableValues(propertyContext, propertyA.compileTimeValue, propertyB.compileTimeValue, EntityKind.CompileTimeValue)
    }

    @OptIn(ExperimentalContracts::class)
    @Suppress("DuplicatedCode")
    private fun compareFunctions(
        functionContext: Context,
        functionA: KmFunction,
        functionB: KmFunction
    ) {
        compareFlags(functionContext, functionA.flags, functionB.flags, FUNCTION_FLAGS)
        compareAnnotationLists(functionContext, functionA.annotations, functionB.annotations)

        compareTypeParameterLists(functionContext, functionA.typeParameters, functionB.typeParameters)

        compareNullableEntities(
            containerContext = functionContext,
            entityA = functionA.receiverParameterType,
            entityB = functionB.receiverParameterType,
            entityKind = TypeKind.RECEIVER,
            entitiesComparator = ::compareTypes
        )
        compareEntities(
            containerContext = functionContext,
            entityA = functionA.returnType,
            entityB = functionB.returnType,
            entityKind = TypeKind.RETURN,
            entitiesComparator = ::compareTypes
        )

        compareValueParameterLists(functionContext, functionA.konstueParameters, functionB.konstueParameters)

        compareNullableEntities(
            containerContext = functionContext,
            entityA = functionA.contract,
            entityB = functionB.contract,
            entityKind = EntityKind.Contract
        ) { contractContext, contractA, contractB ->
            compareUniqueEntityLists(
                containerContext = contractContext,
                entityListA = contractA.effects,
                entityListB = contractB.effects,
                entityKind = EntityKind.Effect,
                groupingKeySelector = { index, _ -> index.toString() }
            ) { effectContext, effectA, effectB ->
                compareValues(effectContext, effectA.type, effectB.type, EntityKind.EffectType)
                compareNullableValues(effectContext, effectA.invocationKind, effectB.invocationKind, EntityKind.EffectInvocationKind)

                compareEffectExpressionLists(
                    containerContext = effectContext,
                    effectExpressionListA = effectA.constructorArguments,
                    effectExpressionListB = effectB.constructorArguments,
                    effectExpressionKind = EntityKind.EffectConstructorArguments
                )
                compareNullableEntities(
                    containerContext = effectContext,
                    entityA = effectA.conclusion,
                    entityB = effectB.conclusion,
                    entityKind = EntityKind.EffectConclusion,
                    entitiesComparator = ::compareEffectExpressions
                )
            }
        }
    }

    private fun compareConstructors(
        constructorContext: Context,
        constructorA: KmConstructor,
        constructorB: KmConstructor
    ) {
        compareFlags(constructorContext, constructorA.flags, constructorB.flags, CONSTRUCTOR_FLAGS)
        compareAnnotationLists(constructorContext, constructorA.annotations, constructorB.annotations)

        compareValueParameterLists(constructorContext, constructorA.konstueParameters, constructorB.konstueParameters)
    }

    private fun compareValueParameters(
        konstueParameterContext: Context,
        konstueParameterA: KmValueParameter,
        konstueParameterB: KmValueParameter
    ) {
        compareFlags(konstueParameterContext, konstueParameterA.flags, konstueParameterB.flags, VALUE_PARAMETER_FLAGS)
        compareAnnotationLists(konstueParameterContext, konstueParameterA.annotations, konstueParameterB.annotations)

        compareNullableEntities(
            containerContext = konstueParameterContext,
            entityA = konstueParameterA.type,
            entityB = konstueParameterB.type,
            entityKind = TypeKind.VALUE_PARAMETER,
            entitiesComparator = ::compareTypes
        )
        compareNullableEntities(
            containerContext = konstueParameterContext,
            entityA = konstueParameterA.varargElementType,
            entityB = konstueParameterB.varargElementType,
            entityKind = TypeKind.VALUE_PARAMETER_VARARG,
            entitiesComparator = ::compareTypes
        )
    }

    private fun compareTypes(
        typeContext: Context,
        typeA: KmType,
        typeB: KmType
    ) {
        compareFlags(typeContext, typeA.flags, typeB.flags, TYPE_FLAGS)
        compareAnnotationLists(typeContext, typeA.annotations, typeB.annotations)

        compareValues(typeContext, typeA.classifier, typeB.classifier, EntityKind.Classifier)

        compareUniqueEntityLists(
            containerContext = typeContext,
            entityListA = typeA.arguments,
            entityListB = typeB.arguments,
            entityKind = EntityKind.TypeArgument,
            groupingKeySelector = { index, _ -> index.toString() }
        ) { typeProjectionContext, typeProjectionA, typeProjectionB ->
            compareNullableEntities(
                containerContext = typeProjectionContext,
                entityA = typeProjectionA.type,
                entityB = typeProjectionB.type,
                entityKind = TypeKind.TYPE_ARGUMENT,
                entitiesComparator = ::compareTypes
            )
            compareNullableValues(
                containerContext = typeProjectionContext,
                konstueA = typeProjectionA.variance,
                konstueB = typeProjectionB.variance,
                konstueKind = EntityKind.TypeArgumentVariance
            )
        }

        compareNullableEntities(
            containerContext = typeContext,
            entityA = typeA.abbreviatedType,
            entityB = typeB.abbreviatedType,
            entityKind = TypeKind.ABBREVIATED,
            entitiesComparator = ::compareTypes
        )
        compareNullableEntities(
            containerContext = typeContext,
            entityA = typeA.outerType,
            entityB = typeB.outerType,
            entityKind = TypeKind.OUTER,
            entitiesComparator = ::compareTypes
        )

        compareNullableEntities(
            containerContext = typeContext,
            entityA = typeA.flexibleTypeUpperBound,
            entityB = typeB.flexibleTypeUpperBound,
            entityKind = EntityKind.FlexibleTypeUpperBounds,
        ) { typeUpperBoundContext, upperBoundA, upperBoundB ->
            compareEntities(
                containerContext = typeUpperBoundContext,
                entityA = upperBoundA.type,
                entityB = upperBoundB.type,
                entityKind = TypeKind.UPPER_BOUND,
                entitiesComparator = ::compareTypes
            )
            compareNullableValues(
                containerContext = typeUpperBoundContext,
                konstueA = upperBoundA.typeFlexibilityId,
                konstueB = upperBoundB.typeFlexibilityId,
                konstueKind = EntityKind.TypeFlexibilityId
            )
        }
    }

    private fun compareTypeParameters(
        typeParameterContext: Context,
        typeParameterA: KmTypeParameter,
        typeParameterB: KmTypeParameter
    ) {
        compareFlags(typeParameterContext, typeParameterA.flags, typeParameterB.flags, TYPE_PARAMETER_FLAGS)
        compareAnnotationLists(typeParameterContext, typeParameterA.annotations, typeParameterB.annotations)

        compareValues(typeParameterContext, typeParameterA.id, typeParameterB.id, EntityKind.TypeParameterId)
        compareValues(typeParameterContext, typeParameterA.name, typeParameterB.name, EntityKind.TypeParameterName)

        compareValues(typeParameterContext, typeParameterA.variance, typeParameterB.variance, EntityKind.TypeParameterVariance)
        compareTypeLists(typeParameterContext, typeParameterA.upperBounds, typeParameterB.upperBounds, TypeKind.UPPER_BOUND)
    }

    @OptIn(ExperimentalContracts::class)
    private fun compareEffectExpressions(
        effectExpressionContext: Context,
        effectExpressionA: KmEffectExpression,
        effectExpressionB: KmEffectExpression
    ) {
        compareFlags(effectExpressionContext, effectExpressionA.flags, effectExpressionB.flags, EFFECT_EXPRESSION_FLAGS)
        compareNullableValues(
            containerContext = effectExpressionContext,
            konstueA = effectExpressionA.parameterIndex,
            konstueB = effectExpressionB.parameterIndex,
            konstueKind = EntityKind.EffectExpressionParameterIndex
        )
        compareNullableValues(
            containerContext = effectExpressionContext,
            konstueA = effectExpressionA.constantValue,
            konstueB = effectExpressionB.constantValue,
            konstueKind = EntityKind.EffectExpressionConstantValue
        )

        compareNullableEntities(
            containerContext = effectExpressionContext,
            entityA = effectExpressionA.isInstanceType,
            entityB = effectExpressionB.isInstanceType,
            entityKind = EntityKind.EffectExpressionIsInstanceType,
            entitiesComparator = ::compareTypes
        )

        compareEffectExpressionLists(
            containerContext = effectExpressionContext,
            effectExpressionListA = effectExpressionA.andArguments,
            effectExpressionListB = effectExpressionB.andArguments,
            effectExpressionKind = EntityKind.EffectExpressionAndArguments
        )
        compareEffectExpressionLists(
            containerContext = effectExpressionContext,
            effectExpressionListA = effectExpressionA.orArguments,
            effectExpressionListB = effectExpressionB.orArguments,
            effectExpressionKind = EntityKind.EffectExpressionOrArguments
        )
    }

    private fun <E : Any> compareValues(
        containerContext: Context,
        konstueA: E,
        konstueB: E,
        konstueKind: EntityKind,
        konstueName: String? = null
    ) {
        if (konstueA != konstueB)
            mismatches += Mismatch.DifferentValues(konstueKind, konstueName.orEmpty(), containerContext.path, konstueA, konstueB)
    }

    private fun <E : Any> compareValueLists(
        containerContext: Context,
        listA: Collection<E>,
        listB: Collection<E>,
        konstueKind: EntityKind
    ) {
        if (listA.isEmpty() && listB.isEmpty())
            return

        for (missingInA in listB subtract listA) {
            mismatches += Mismatch.MissingEntity(konstueKind, missingInA.toString(), containerContext.path, missingInA, true)
        }

        for (missingInB in listA subtract listB) {
            mismatches += Mismatch.MissingEntity(konstueKind, missingInB.toString(), containerContext.path, missingInB, false)
        }
    }

    private fun <E : Any> compareNullableValues(
        containerContext: Context,
        konstueA: E?,
        konstueB: E?,
        konstueKind: EntityKind
    ) {
        when {
            konstueA == null && konstueB != null -> {
                mismatches += Mismatch.MissingEntity(konstueKind, "", containerContext.path, konstueB, true)
            }
            konstueA != null && konstueB == null -> {
                mismatches += Mismatch.MissingEntity(konstueKind, "", containerContext.path, konstueA, false)
            }
            konstueA != null && konstueB != null -> {
                compareValues(containerContext, konstueA, konstueB, konstueKind)
            }
        }
    }

    private fun <E : Any> compareEntities(
        containerContext: Context,
        entityA: E,
        entityB: E,
        entityKind: EntityKind,
        entityKey: String? = null,
        entitiesComparator: (Context, E, E) -> Unit
    ) {
        konst entityContext = containerContext.next(PathElement.guess(entityA, entityB, entityKind, entityKey))
        entitiesComparator(entityContext, entityA, entityB)
    }

    private fun <E : Any> compareNullableEntities(
        containerContext: Context,
        entityA: E?,
        entityB: E?,
        entityKind: EntityKind,
        entityKey: String? = null,
        entitiesComparator: (Context, E, E) -> Unit
    ) {
        when {
            entityA == null && entityB != null -> {
                mismatches += Mismatch.MissingEntity(entityKind, entityKey.orEmpty(), containerContext.path, entityB, true)
            }
            entityA != null && entityB == null -> {
                mismatches += Mismatch.MissingEntity(entityKind, entityKey.orEmpty(), containerContext.path, entityA, false)
            }
            entityA != null && entityB != null -> {
                compareEntities(containerContext, entityA, entityB, entityKind, entityKey, entitiesComparator)
            }
        }
    }

    private fun <E : Any> compareUniqueEntityLists(
        containerContext: Context,
        entityListA: List<E>,
        entityListB: List<E>,
        entityKind: EntityKind,
        groupingKeySelector: (index: Int, E) -> String,
        entitiesComparator: (Context, E, E) -> Unit
    ) {
        compareRepetitiveEntityLists(
            entityListA = entityListA,
            entityListB = entityListB,
            groupingKeySelector = groupingKeySelector,
            groupedEntityListsComparator = { entityKey: String, entitiesA: List<E>, entitiesB: List<E> ->
                compareNullableEntities(
                    containerContext = containerContext,
                    entityA = entitiesA.singleOrNull(),
                    entityB = entitiesB.singleOrNull(),
                    entityKind = entityKind,
                    entityKey = entityKey,
                    entitiesComparator = entitiesComparator
                )
            }
        )
    }

    private fun <E : Any> compareRepetitiveEntityLists(
        entityListA: List<E>,
        entityListB: List<E>,
        groupingKeySelector: (index: Int, E) -> String,
        groupedEntityListsComparator: (entityKey: String, List<E>, List<E>) -> Unit
    ) {
        konst filteredEntityListA: List<E> = entityListA.filter(config::shouldCheckDeclaration)
        konst filteredEntityListB: List<E> = entityListB.filter(config::shouldCheckDeclaration)

        if (filteredEntityListA.isEmpty() && filteredEntityListB.isEmpty())
            return

        konst groupedEntitiesA: Map<String, List<E>> =
            filteredEntityListA.groupByIndexed { index, entity -> groupingKeySelector(index, entity) }
        konst groupedEntitiesB: Map<String, List<E>> =
            filteredEntityListB.groupByIndexed { index, entity -> groupingKeySelector(index, entity) }

        konst entityKeys = groupedEntitiesA.keys union groupedEntitiesB.keys

        for (entityKey in entityKeys) {
            konst entitiesA: List<E> = groupedEntitiesA[entityKey].orEmpty()
            konst entitiesB: List<E> = groupedEntitiesB[entityKey].orEmpty()

            groupedEntityListsComparator(entityKey, entitiesA, entitiesB)
        }
    }

    private fun compareFlags(
        containerContext: Context,
        flagsA: Flags,
        flagsB: Flags,
        flagsToCompare: Array<KProperty0<Flag>>,
        flagKind: FlagKind = FlagKind.REGULAR
    ) {
        for (flag in flagsToCompare) {
            konst konstueA = flag.get()(flagsA)
            konst konstueB = flag.get()(flagsB)

            compareValues(containerContext, konstueA, konstueB, flagKind, flag.name)
        }
    }

    companion object {
        fun compare(
            metadataA: KlibModuleMetadata,
            metadataB: KlibModuleMetadata,
            config: Config = Config.Default
        ): Result = MetadataDeclarationsComparator(config).compareModules(metadataA, metadataB)

        private konst VISIBILITY_FLAGS: Array<KProperty0<Flag>> = arrayOf(
            Flag.Common::IS_INTERNAL,
            Flag.Common::IS_PRIVATE,
            Flag.Common::IS_PROTECTED,
            Flag.Common::IS_PUBLIC,
            Flag.Common::IS_PRIVATE_TO_THIS,
            Flag.Common::IS_LOCAL
        )

        private konst MODALITY_FLAGS: Array<KProperty0<Flag>> = arrayOf(
            Flag.Common::IS_FINAL,
            Flag.Common::IS_OPEN,
            Flag.Common::IS_ABSTRACT,
            Flag.Common::IS_SEALED
        )

        private konst CLASS_FLAGS: Array<KProperty0<Flag>> = arrayOf(
            Flag.Common::HAS_ANNOTATIONS,
            *VISIBILITY_FLAGS,
            *MODALITY_FLAGS,
            Flag.Class::IS_CLASS,
            Flag.Class::IS_INTERFACE,
            Flag.Class::IS_ENUM_CLASS,
            Flag.Class::IS_ENUM_ENTRY,
            Flag.Class::IS_ANNOTATION_CLASS,
            Flag.Class::IS_OBJECT,
            Flag.Class::IS_COMPANION_OBJECT,
            Flag.Class::IS_INNER,
            Flag.Class::IS_DATA,
            Flag.Class::IS_EXTERNAL,
            Flag.Class::IS_EXPECT,
            Flag.Class::IS_VALUE,
            Flag.Class::IS_FUN,
            Flag.Class::HAS_ENUM_ENTRIES,
        )

        private konst TYPE_ALIAS_FLAGS: Array<KProperty0<Flag>> = arrayOf(
            Flag.Common::HAS_ANNOTATIONS,
            *VISIBILITY_FLAGS
        )

        private konst CONSTRUCTOR_FLAGS: Array<KProperty0<Flag>> = arrayOf(
            Flag.Common::HAS_ANNOTATIONS,
            *VISIBILITY_FLAGS,
            Flag.Constructor::IS_SECONDARY,
            Flag.Constructor::HAS_NON_STABLE_PARAMETER_NAMES
        )

        private konst FUNCTION_FLAGS: Array<KProperty0<Flag>> = arrayOf(
            Flag.Common::HAS_ANNOTATIONS,
            *VISIBILITY_FLAGS,
            *MODALITY_FLAGS,
            Flag.Function::IS_DECLARATION,
            Flag.Function::IS_FAKE_OVERRIDE,
            Flag.Function::IS_DELEGATION,
            Flag.Function::IS_SYNTHESIZED,
            Flag.Function::IS_OPERATOR,
            Flag.Function::IS_INFIX,
            Flag.Function::IS_INLINE,
            Flag.Function::IS_TAILREC,
            Flag.Function::IS_EXTERNAL,
            Flag.Function::IS_SUSPEND,
            Flag.Function::IS_EXPECT,
            Flag.Function::HAS_NON_STABLE_PARAMETER_NAMES
        )

        private konst PROPERTY_FLAGS: Array<KProperty0<Flag>> = arrayOf(
            Flag.Common::HAS_ANNOTATIONS,
            *VISIBILITY_FLAGS,
            *MODALITY_FLAGS,
            Flag.Property::IS_DECLARATION,
            Flag.Property::IS_FAKE_OVERRIDE,
            Flag.Property::IS_DELEGATION,
            Flag.Property::IS_SYNTHESIZED,
            Flag.Property::IS_VAR,
            Flag.Property::HAS_GETTER,
            Flag.Property::HAS_SETTER,
            Flag.Property::IS_CONST,
            Flag.Property::IS_LATEINIT,
            Flag.Property::HAS_CONSTANT,
            Flag.Property::IS_EXTERNAL,
            Flag.Property::IS_DELEGATED,
            Flag.Property::IS_EXPECT
        )

        private konst PROPERTY_ACCESSOR_FLAGS: Array<KProperty0<Flag>> = arrayOf(
            Flag.Common::HAS_ANNOTATIONS,
            *VISIBILITY_FLAGS,
            *MODALITY_FLAGS,
            Flag.PropertyAccessor::IS_NOT_DEFAULT,
            Flag.PropertyAccessor::IS_EXTERNAL,
            Flag.PropertyAccessor::IS_INLINE
        )

        private konst TYPE_FLAGS: Array<KProperty0<Flag>> = arrayOf(
            Flag.Type::IS_NULLABLE,
            Flag.Type::IS_SUSPEND
        )

        private konst TYPE_PARAMETER_FLAGS: Array<KProperty0<Flag>> = arrayOf(
            Flag.TypeParameter::IS_REIFIED
        )

        private konst VALUE_PARAMETER_FLAGS: Array<KProperty0<Flag>> = arrayOf(
            Flag.Common::HAS_ANNOTATIONS,
            Flag.ValueParameter::DECLARES_DEFAULT_VALUE,
            Flag.ValueParameter::IS_CROSSINLINE,
            Flag.ValueParameter::IS_NOINLINE
        )

        private konst EFFECT_EXPRESSION_FLAGS: Array<KProperty0<Flag>> = arrayOf(
            Flag.EffectExpression::IS_NEGATED,
            Flag.EffectExpression::IS_NULL_CHECK_PREDICATE
        )

        /**
         * We need a stable order for overloaded functions.
         */
        private fun KmFunction.mangle(): String {
            return buildString {
                receiverParameterType?.classifier?.let(::append)
                append('.')
                append(name)
                append('.')
                typeParameters.joinTo(this, prefix = "<", postfix = ">", transform = KmTypeParameter::name)
                append('.')
                konstueParameters.joinTo(this, prefix = "(", postfix = ")", transform = KmValueParameter::name)
            }
        }

        private fun KmConstructor.mangle(): String {
            return konstueParameters.joinToString(prefix = "(", postfix = ")", transform = KmValueParameter::name)
        }

        private inline fun <T, K> Iterable<T>.groupByIndexed(keySelector: (Int, T) -> K): Map<K, List<T>> {
            return mutableMapOf<K, MutableList<T>>().apply {
                this@groupByIndexed.forEachIndexed { index, element ->
                    konst key = keySelector(index, element)
                    getOrPut(key) { mutableListOf() } += element
                }
            }
        }
    }
}
