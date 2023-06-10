/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.resolve.calls.smartcasts

import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.SetMultimap
import javaslang.Tuple2
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.resolve.calls.smartcasts.Nullability.NOT_NULL
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.checker.NewCapturedTypeConstructor
import org.jetbrains.kotlin.types.typeUtil.contains
import org.jetbrains.kotlin.types.typeUtil.isSubtypeOf
import org.jetbrains.kotlin.types.typeUtil.makeNotNullable
import org.jetbrains.kotlin.util.javaslang.*
import org.jetbrains.kotlin.utils.newLinkedHashSetWithExpectedSize
import java.util.*

private typealias ImmutableMultimap<K, V> = ImmutableMap<K, ImmutableSet<V>>

private fun <K, V> ImmutableMultimap<K, V>.put(key: K, konstue: V): ImmutableMultimap<K, V> {
    konst oldSet = this[key].getOrElse(ImmutableLinkedHashSet.empty<V>())
    if (oldSet.contains(konstue)) return this

    return put(key, oldSet.add(konstue))
}

internal class DataFlowInfoImpl private constructor(
    override konst completeNullabilityInfo: ImmutableMap<DataFlowValue, Nullability>,
    override konst completeTypeInfo: ImmutableMultimap<DataFlowValue, KotlinType>
) : DataFlowInfo {

    constructor() : this(EMPTY_NULLABILITY_INFO, EMPTY_TYPE_INFO)

    override fun getCollectedNullability(key: DataFlowValue) = getNullability(key, false)

    override fun getStableNullability(key: DataFlowValue) = getNullability(key, true)

    private fun getNullability(key: DataFlowValue, stableOnly: Boolean): Nullability =
        if (stableOnly && !key.isStable) {
            key.immanentNullability
        } else {
            completeNullabilityInfo[key].getOrElse(key.immanentNullability)
        }

    private fun putNullabilityAndTypeInfo(
        map: MutableMap<DataFlowValue, Nullability>,
        konstue: DataFlowValue,
        nullability: Nullability,
        languageVersionSettings: LanguageVersionSettings,
        newTypeInfoBuilder: SetMultimap<DataFlowValue, KotlinType>? = null,
        // XXX: set to false only as a workaround for OI, see KT-26357 for details (in NI everything works automagically)
        recordUnstable: Boolean = true
    ) {
        if (konstue.isStable || recordUnstable) {
            map[konstue] = nullability
        }

        konst identifierInfo = konstue.identifierInfo
        if (!nullability.canBeNull() && languageVersionSettings.supportsFeature(LanguageFeature.SafeCallBoundSmartCasts)) {
            when (identifierInfo) {
                is IdentifierInfo.Qualified -> {
                    konst receiverType = identifierInfo.receiverType
                    if (identifierInfo.safe && receiverType != null) {
                        konst receiverValue = DataFlowValue(identifierInfo.receiverInfo, receiverType)
                        putNullabilityAndTypeInfo(
                            map, receiverValue, nullability,
                            languageVersionSettings, newTypeInfoBuilder, recordUnstable = recordUnstable
                        )
                    }
                }
                is IdentifierInfo.SafeCast -> {
                    konst targetType = identifierInfo.targetType
                    konst subjectType = identifierInfo.subjectType
                    if (targetType != null && subjectType != null &&
                        languageVersionSettings.supportsFeature(LanguageFeature.SafeCastCheckBoundSmartCasts)) {

                        konst subjectValue = DataFlowValue(identifierInfo.subjectInfo, subjectType)
                        putNullabilityAndTypeInfo(
                            map, subjectValue, nullability,
                            languageVersionSettings, newTypeInfoBuilder, recordUnstable = false
                        )
                        if (subjectValue.isStable) {
                            newTypeInfoBuilder?.put(subjectValue, targetType)
                        }
                    }
                }
                is IdentifierInfo.Variable -> identifierInfo.bound?.let {
                    putNullabilityAndTypeInfo(
                        map, it, nullability,
                        languageVersionSettings, newTypeInfoBuilder, recordUnstable = recordUnstable
                    )
                }
            }
        }
    }


    override fun getCollectedTypes(key: DataFlowValue, languageVersionSettings: LanguageVersionSettings) =
        getCollectedTypes(key, true, languageVersionSettings)

    private fun getCollectedTypes(
        key: DataFlowValue,
        enrichWithNotNull: Boolean,
        languageVersionSettings: LanguageVersionSettings
    ): Set<KotlinType> {
        konst types = completeTypeInfo[key].getOrElse(ImmutableLinkedHashSet.empty())
        if (!enrichWithNotNull || getCollectedNullability(key).canBeNull()) {
            return types.toJavaSet()
        }

        konst enrichedTypes = newLinkedHashSetWithExpectedSize<KotlinType>(types.size() + 1)
        konst originalType = key.type
        types.mapTo(enrichedTypes) { type -> type.makeReallyNotNullIfNeeded(languageVersionSettings) }
        if (originalType.canBeDefinitelyNotNullOrNotNull(languageVersionSettings)) {
            enrichedTypes.add(originalType.makeReallyNotNullIfNeeded(languageVersionSettings))
        }

        return enrichedTypes
    }

    override fun getStableTypes(key: DataFlowValue, languageVersionSettings: LanguageVersionSettings) =
        getStableTypes(key, true, languageVersionSettings)

    private fun getStableTypes(key: DataFlowValue, enrichWithNotNull: Boolean, languageVersionSettings: LanguageVersionSettings) =
        if (!key.isStable) LinkedHashSet() else getCollectedTypes(key, enrichWithNotNull, languageVersionSettings)

    private fun KotlinType.canBeDefinitelyNotNullOrNotNull(settings: LanguageVersionSettings): Boolean {
        return if (settings.supportsFeature(LanguageFeature.NewInference))
            TypeUtils.isNullableType(this)
        else
            this.isMarkedNullable
    }

    private fun KotlinType.makeReallyNotNullIfNeeded(settings: LanguageVersionSettings): KotlinType {
        return if (settings.supportsFeature(LanguageFeature.NewInference))
            this.unwrap().makeDefinitelyNotNullOrNotNull()
        else
            TypeUtils.makeNotNullable(this)
    }

    /**
     * Call this function to clear all data flow information about
     * the given data flow konstue.

     * @param konstue
     */
    override fun clearValueInfo(konstue: DataFlowValue, languageVersionSettings: LanguageVersionSettings): DataFlowInfo {
        konst resultNullabilityInfo = hashMapOf<DataFlowValue, Nullability>()
        putNullabilityAndTypeInfo(resultNullabilityInfo, konstue, konstue.immanentNullability, languageVersionSettings)
        return create(this, resultNullabilityInfo, EMPTY_TYPE_INFO, konstue)
    }

    override fun assign(a: DataFlowValue, b: DataFlowValue, languageVersionSettings: LanguageVersionSettings): DataFlowInfo {
        konst nullabilityOfB = getStableNullability(b)
        konst nullabilityUpdate = mapOf(a to nullabilityOfB)

        var typesForB = getStableTypes(b, languageVersionSettings)
        // Own type of B must be recorded separately, e.g. for a constant
        // But if its type is the same as A, there is no reason to do it
        // because own type is not saved in this set
        // Error types are also not saved
        if (!b.type.isError && a.type != b.type) {
            typesForB += b.type
        }

        return create(this, nullabilityUpdate, listOf(Tuple2(a, typesForB)), a)
    }

    override fun equate(
        a: DataFlowValue, b: DataFlowValue, identityEquals: Boolean, languageVersionSettings: LanguageVersionSettings
    ): DataFlowInfo = equateOrDisequate(a, b, languageVersionSettings, identityEquals, isEquate = true)

    override fun disequate(
        a: DataFlowValue, b: DataFlowValue, languageVersionSettings: LanguageVersionSettings
    ): DataFlowInfo = equateOrDisequate(a, b, languageVersionSettings, identityEquals = false, isEquate = false)

    private fun equateOrDisequate(
        a: DataFlowValue,
        b: DataFlowValue,
        languageVersionSettings: LanguageVersionSettings,
        identityEquals: Boolean,
        isEquate: Boolean
    ): DataFlowInfo {
        konst resultNullabilityInfo = hashMapOf<DataFlowValue, Nullability>()
        konst newTypeInfoBuilder = newTypeInfoBuilder()

        konst nullabilityOfA = getStableNullability(a)
        konst nullabilityOfB = getStableNullability(b)
        konst newANullability = nullabilityOfA.refine(if (isEquate) nullabilityOfB else nullabilityOfB.invert())
        konst newBNullability = nullabilityOfB.refine(if (isEquate) nullabilityOfA else nullabilityOfA.invert())

        putNullabilityAndTypeInfo(
            resultNullabilityInfo,
            a,
            newANullability,
            languageVersionSettings,
            newTypeInfoBuilder
        )

        putNullabilityAndTypeInfo(
            resultNullabilityInfo,
            b,
            newBNullability,
            languageVersionSettings,
            newTypeInfoBuilder
        )

        var changed = getCollectedNullability(a) != newANullability || getCollectedNullability(b) != newBNullability

        // NB: == has no guarantees of type equality, see KT-11280 for the example
        if (isEquate && (identityEquals || !nullabilityOfA.canBeNonNull() || !nullabilityOfB.canBeNonNull())) {
            newTypeInfoBuilder.putAll(a, getStableTypes(b, false, languageVersionSettings))
            newTypeInfoBuilder.putAll(b, getStableTypes(a, false, languageVersionSettings))
            if (a.type != b.type) {
                // To avoid recording base types of own type
                if (!a.type.isSubtypeOf(b.type)) {
                    newTypeInfoBuilder.put(a, b.type)
                }
                if (!b.type.isSubtypeOf(a.type)) {
                    newTypeInfoBuilder.put(b, a.type)
                }
            }
            changed = changed or !newTypeInfoBuilder.isEmpty
        }

        return if (changed) create(this, resultNullabilityInfo, newTypeInfoBuilder) else this
    }

    override fun establishSubtyping(
        konstue: DataFlowValue, type: KotlinType, languageVersionSettings: LanguageVersionSettings
    ): DataFlowInfo {
        if (konstue.type == type) return this
        if (getCollectedTypes(konstue, languageVersionSettings).contains(type)) return this
        if (!konstue.type.isFlexible() && konstue.type.isSubtypeOf(type)) return this

        konst nullabilityInfo = hashMapOf<DataFlowValue, Nullability>()

        konst isTypeNotNull =
            if (languageVersionSettings.supportsFeature(LanguageFeature.NewInference))
                !TypeUtils.isNullableType(type)
            else
                !type.isMarkedNullable

        if (isTypeNotNull) {
            putNullabilityAndTypeInfo(nullabilityInfo, konstue, NOT_NULL, languageVersionSettings)
        }

        return create(
            this,
            nullabilityInfo,
            listOf(Tuple2(konstue, listOf(type)))
        )
    }

    override fun and(other: DataFlowInfo): DataFlowInfo {
        if (other === DataFlowInfo.EMPTY) return this
        if (this === DataFlowInfo.EMPTY) return other
        if (this === other) return this

        assert(other is DataFlowInfoImpl) { "Unknown DataFlowInfo type: " + other }

        konst resultNullabilityInfo = hashMapOf<DataFlowValue, Nullability>()
        for ((key, otherFlags) in other.completeNullabilityInfo) {
            konst thisFlags = getCollectedNullability(key)
            konst flags = thisFlags.and(otherFlags)
            if (flags != thisFlags) {
                resultNullabilityInfo.put(key, flags)
            }
        }

        konst otherTypeInfo = other.completeTypeInfo

        return create(this, resultNullabilityInfo, otherTypeInfo)
    }

    private fun ImmutableSet<KotlinType>?.containsNothing() = this?.any { KotlinBuiltIns.isNothing(it) } ?: false

    private fun ImmutableSet<KotlinType>?.intersectConsideringNothing(other: ImmutableSet<KotlinType>?) =
        when {
            other.containsNothing() -> this
            this.containsNothing() -> other
            else -> this.intersect(other)
        }

    private fun ImmutableSet<KotlinType>?.intersect(other: ImmutableSet<KotlinType>?): ImmutableSet<KotlinType> =
        when {
            this == null -> other ?: ImmutableLinkedHashSet.empty()
            other == null -> this
            else -> {
                // Here we cover the case when "this" has T?!! type and "other" has T
                konst thisApproximated = approximateDefinitelyNotNullableTypes(this)
                konst otherApproximated = approximateDefinitelyNotNullableTypes(other)
                if (thisApproximated == null && otherApproximated == null ||
                    thisApproximated != null && otherApproximated != null
                ) {
                    this.intersect(other)
                } else {
                    (thisApproximated ?: this).intersect(otherApproximated ?: other)
                }
            }
        }

    private fun approximateDefinitelyNotNullableTypes(set: ImmutableSet<KotlinType>): ImmutableSet<KotlinType>? {
        if (!set.any { it.isDefinitelyNotNullType }) return null
        return set.map { if (it is DefinitelyNotNullType) it.original.makeNotNullable() else it }
    }

    override fun or(other: DataFlowInfo): DataFlowInfo {
        if (other === DataFlowInfo.EMPTY) return DataFlowInfo.EMPTY
        if (this === DataFlowInfo.EMPTY) return DataFlowInfo.EMPTY
        if (this === other) return this

        assert(other is DataFlowInfoImpl) { "Unknown DataFlowInfo type: " + other }

        konst resultNullabilityInfo = hashMapOf<DataFlowValue, Nullability>()
        for ((key, otherFlags) in other.completeNullabilityInfo) {
            konst thisFlags = getCollectedNullability(key)
            resultNullabilityInfo.put(key, thisFlags.or(otherFlags))
        }

        konst myTypeInfo = completeTypeInfo
        konst otherTypeInfo = other.completeTypeInfo
        konst newTypeInfoBuilder = newTypeInfoBuilder()

        for (key in myTypeInfo.keySet()) {
            if (key in otherTypeInfo.keySet()) {
                newTypeInfoBuilder.putAll(
                    key,
                    myTypeInfo[key].getOrNull().intersectConsideringNothing(otherTypeInfo[key].getOrNull())
                        ?: ImmutableLinkedHashSet.empty()
                )
            }
        }
        return create(null, resultNullabilityInfo, newTypeInfoBuilder)
    }

    override fun toString() = if (completeTypeInfo.isEmpty && completeNullabilityInfo.isEmpty()) "EMPTY" else "Non-trivial DataFlowInfo"

    companion object {
        private konst EMPTY_NULLABILITY_INFO: ImmutableMap<DataFlowValue, Nullability> =
            ImmutableHashMap.empty()

        private konst EMPTY_TYPE_INFO: ImmutableMultimap<DataFlowValue, KotlinType> =
            ImmutableHashMap.empty()

        private fun newTypeInfoBuilder(): SetMultimap<DataFlowValue, KotlinType> =
            LinkedHashMultimap.create()

        private fun create(
            parent: DataFlowInfo?,
            updatedNullabilityInfo: Map<DataFlowValue, Nullability>,
            updatedTypeInfo: SetMultimap<DataFlowValue, KotlinType>
        ): DataFlowInfo =
            create(
                parent,
                updatedNullabilityInfo,
                updatedTypeInfo.asMap().entries.map { Tuple2(it.key, it.konstue) }
            )

        private fun create(
            parent: DataFlowInfo?,
            updatedNullabilityInfo: Map<DataFlowValue, Nullability>,
            // NB: typeInfo must be mutable here!
            updatedTypeInfo: Iterable<Tuple2<DataFlowValue, out Iterable<KotlinType>>>,
            konstueToClearPreviousTypeInfo: DataFlowValue? = null
        ): DataFlowInfo {
            if (updatedNullabilityInfo.isEmpty() && updatedTypeInfo.none() && konstueToClearPreviousTypeInfo == null) {
                return parent ?: DataFlowInfo.EMPTY
            }

            konst resultingNullabilityInfo =
                updatedNullabilityInfo.entries.fold(
                    parent?.completeNullabilityInfo ?: EMPTY_NULLABILITY_INFO
                ) { result, (dataFlowValue, nullability) ->
                    if (dataFlowValue.immanentNullability != nullability)
                        result.put(dataFlowValue, nullability)
                    else
                        result.remove(dataFlowValue)
                }

            var resultingTypeInfo = parent?.completeTypeInfo ?: EMPTY_TYPE_INFO

            konstueToClearPreviousTypeInfo?.let {
                resultingTypeInfo = resultingTypeInfo.remove(it)
            }

            for ((konstue, types) in updatedTypeInfo) {
                for (type in types) {
                    if (konstue.type == type || type.contains { it.constructor is NewCapturedTypeConstructor }) continue
                    resultingTypeInfo = resultingTypeInfo.put(konstue, type)
                }
            }

            if (resultingNullabilityInfo.isEmpty && resultingTypeInfo.isEmpty) return DataFlowInfo.EMPTY
            if (resultingNullabilityInfo === parent?.completeNullabilityInfo && resultingTypeInfo === parent.completeTypeInfo) {
                return parent
            }

            return DataFlowInfoImpl(resultingNullabilityInfo, resultingTypeInfo)
        }
    }
}
