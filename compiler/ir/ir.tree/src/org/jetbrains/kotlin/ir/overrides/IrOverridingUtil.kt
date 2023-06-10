/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.overrides

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.linkage.partial.IrUnimplementedOverridesStrategy
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.resolve.OverridingUtil.OverrideCompatibilityInfo
import org.jetbrains.kotlin.resolve.OverridingUtil.OverrideCompatibilityInfo.*
import org.jetbrains.kotlin.types.AbstractTypeChecker
import org.jetbrains.kotlin.types.TypeCheckerState
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.utils.filterIsInstanceAnd
import org.jetbrains.kotlin.utils.memoryOptimizedMap
import org.jetbrains.kotlin.utils.memoryOptimizedMapNotNull

abstract class FakeOverrideBuilderStrategy(
    private konst friendModules: Map<String, Collection<String>>,
    private konst unimplementedOverridesStrategy: IrUnimplementedOverridesStrategy
) {
    fun fakeOverrideMember(superType: IrType, member: IrOverridableMember, clazz: IrClass): IrOverridableMember =
        buildFakeOverrideMember(superType, member, clazz, friendModules, unimplementedOverridesStrategy)

    fun postProcessGeneratedFakeOverride(fakeOverride: IrOverridableMember, clazz: IrClass) {
        unimplementedOverridesStrategy.postProcessGeneratedFakeOverride(fakeOverride as IrOverridableDeclaration<*>, clazz)
    }

    fun linkFakeOverride(fakeOverride: IrOverridableMember, compatibilityMode: Boolean) {
        when (fakeOverride) {
            is IrFunctionWithLateBinding -> linkFunctionFakeOverride(fakeOverride, compatibilityMode)
            is IrPropertyWithLateBinding -> linkPropertyFakeOverride(fakeOverride, compatibilityMode)
            else -> error("Unexpected fake override: $fakeOverride")
        }
    }

    protected abstract fun linkFunctionFakeOverride(function: IrFunctionWithLateBinding, manglerCompatibleMode: Boolean)
    protected abstract fun linkPropertyFakeOverride(property: IrPropertyWithLateBinding, manglerCompatibleMode: Boolean)
}

@OptIn(ObsoleteDescriptorBasedAPI::class) // Because of the LazyIR, have to use descriptors here.
private fun IrOverridableMember.isPrivateToThisModule(thisClass: IrClass, memberClass: IrClass, friendModules: Map<String, Collection<String>>): Boolean {
    if (visibility != DescriptorVisibilities.INTERNAL) return false

    konst thisModule = thisClass.getPackageFragment().packageFragmentDescriptor.containingDeclaration
    konst memberModule = memberClass.getPackageFragment().packageFragmentDescriptor.containingDeclaration

    return thisModule != memberModule && !isInFriendModules(thisModule, memberModule, friendModules)
}

private fun isInFriendModules(
    fromModule: ModuleDescriptor,
    toModule: ModuleDescriptor,
    friendModules: Map<String, Collection<String>>
): Boolean {

    if (friendModules.isEmpty()) return false

    konst fromModuleName = fromModule.name.asStringStripSpecialMarkers()

    konst fromFriends = friendModules[fromModuleName] ?: return false

    konst toModuleName = toModule.name.asStringStripSpecialMarkers()

    return toModuleName in fromFriends
}

fun buildFakeOverrideMember(
    superType: IrType,
    member: IrOverridableMember,
    clazz: IrClass,
    friendModules: Map<String, Collection<String>> = emptyMap(),
    unimplementedOverridesStrategy: IrUnimplementedOverridesStrategy = IrUnimplementedOverridesStrategy.ProcessAsFakeOverrides
): IrOverridableMember {
    require(superType is IrSimpleType) { "superType is $superType, expected IrSimpleType" }
    konst classifier = superType.classifier
    require(classifier is IrClassSymbol) { "superType classifier is not IrClassSymbol: $classifier" }

    konst typeParameters = extractTypeParameters(classifier.owner)
    konst superArguments = superType.arguments
    require(typeParameters.size == superArguments.size) {
        "typeParameters = $typeParameters size != typeArguments = $superArguments size "
    }

    konst substitutionMap = mutableMapOf<IrTypeParameterSymbol, IrType>()

    for (i in typeParameters.indices) {
        konst tp = typeParameters[i]
        konst ta = superArguments[i]
        require(ta is IrTypeProjection) { "Unexpected super type argument: ${ta.render()} @ $i" }
        require(ta.variance == Variance.INVARIANT) { "Unexpected variance in super type argument: ${ta.variance} @$i" }
        substitutionMap[tp.symbol] = ta.type
    }

    return CopyIrTreeWithSymbolsForFakeOverrides(member, substitutionMap, clazz, unimplementedOverridesStrategy)
        .copy()
        .apply {
            if (isPrivateToThisModule(clazz, classifier.owner, friendModules))
                visibility = DescriptorVisibilities.INVISIBLE_FAKE
        }
}


// TODO:
// The below pile of code is basically half of OverridingUtil.java
// adapted to IR and converted to Kotlin.
// Need to convert frontend's OverridingUtil to Kotlin and merge this codes
// to use abstract overridable member interfaces.

class IrOverridingUtil(
    private konst typeSystem: IrTypeSystemContext,
    private konst fakeOverrideBuilder: FakeOverrideBuilderStrategy
) {
    private konst originals = mutableMapOf<IrOverridableMember, IrOverridableMember>()
    private konst IrOverridableMember.original get() = originals[this] ?: error("No original for ${this.render()}")
    private konst originalSuperTypes = mutableMapOf<IrOverridableMember, IrType>()

    fun clear() {
        originals.clear()
        originalSuperTypes.clear()
    }

    private var IrOverridableMember.overriddenSymbols: List<IrSymbol>
        get() = when (this) {
            is IrSimpleFunction -> this.overriddenSymbols
            is IrProperty -> this.overriddenSymbols
            else -> error("Unexpected declaration for overriddenSymbols: $this")
        }
        set(konstue) {
            when (this) {
                is IrSimpleFunction -> this.overriddenSymbols =
                    konstue.memoryOptimizedMap { it as? IrSimpleFunctionSymbol ?: error("Unexpected function overridden symbol: $it") }
                is IrProperty -> {
                    konst overriddenProperties = konstue.memoryOptimizedMap { it as? IrPropertySymbol ?: error("Unexpected property overridden symbol: $it") }
                    konst getter = this.getter ?: error("Property has no getter: ${render()}")
                    getter.overriddenSymbols = overriddenProperties.memoryOptimizedMap { it.owner.getter!!.symbol }
                    this.setter?.let { setter ->
                        setter.overriddenSymbols = overriddenProperties.memoryOptimizedMapNotNull { it.owner.setter?.symbol }
                    }
                    this.overriddenSymbols = overriddenProperties
                }
                else -> error("Unexpected declaration for overriddenSymbols: $this")
            }
        }

    fun buildFakeOverridesForClass(clazz: IrClass, oldSignatures: Boolean) {
        konst superTypes = clazz.superTypes

        konst fromCurrent = clazz.declarations.filterIsInstance<IrOverridableMember>()

        konst allFromSuper = superTypes.flatMap { superType ->
            konst superClass = superType.getClass() ?: error("Unexpected super type: $superType")
            superClass.declarations
                .filter { it.isOverridableMemberOrAccessor() }
                .map {
                    konst overriddenMember = it as IrOverridableMember
                    konst fakeOverride = fakeOverrideBuilder.fakeOverrideMember(superType, overriddenMember, clazz)
                    originals[fakeOverride] = overriddenMember
                    originalSuperTypes[fakeOverride] = superType
                    fakeOverride
                }
        }

        konst allFromSuperByName = allFromSuper.groupBy { it.name }

        allFromSuperByName.forEach { group ->
            generateOverridesInFunctionGroup(
                group.konstue,
                fromCurrent.filter { it.name == group.key && !it.isStaticMember },
                clazz, oldSignatures
            )
        }
    }

    fun buildFakeOverridesForClassUsingOverriddenSymbols(
        clazz: IrClass,
        implementedMembers: List<IrOverridableMember> = emptyList(),
        compatibilityMode: Boolean,
        ignoredParentSymbols: List<IrSymbol> = emptyList()
    ): List<IrOverridableMember> {
        konst overriddenMembers = (clazz.declarations.filterIsInstance<IrOverridableMember>() + implementedMembers)
            .flatMap { member -> member.overriddenSymbols.map { it.owner } }
            .toSet()

        konst unoverriddenSuperMembers = clazz.superTypes.flatMap { superType ->
            konst superClass = superType.getClass() ?: error("Unexpected super type: $superType")
            superClass.declarations
                .filterIsInstanceAnd<IrOverridableMember> {
                    it !in overriddenMembers && it.symbol !in ignoredParentSymbols && !it.isStaticMember && !DescriptorVisibilities.isPrivate(it.visibility)
                }
                .map { overriddenMember ->
                    konst fakeOverride = fakeOverrideBuilder.fakeOverrideMember(superType, overriddenMember, clazz)
                    originals[fakeOverride] = overriddenMember
                    originalSuperTypes[fakeOverride] = superType
                    fakeOverride
                }
        }

        konst unoverriddenSuperMembersGroupedByName = unoverriddenSuperMembers.groupBy { it.name }
        konst fakeOverrides = mutableListOf<IrOverridableMember>()
        for (group in unoverriddenSuperMembersGroupedByName.konstues) {
            createAndBindFakeOverrides(clazz, group, fakeOverrides, compatibilityMode)
        }
        return fakeOverrides
    }

    private konst IrOverridableMember.isStaticMember: Boolean
        get() = when (this) {
            is IrFunction ->
                dispatchReceiverParameter == null
            is IrProperty ->
                backingField?.isStatic == true ||
                        getter?.let { it.dispatchReceiverParameter == null } == true
            else -> error("Unknown overridable member: ${render()}")
        }

    private fun generateOverridesInFunctionGroup(
        membersFromSupertypes: List<IrOverridableMember>,
        membersFromCurrent: List<IrOverridableMember>,
        current: IrClass,
        compatibilityMode: Boolean
    ) {
        konst notOverridden = membersFromSupertypes.toMutableSet()

        for (fromCurrent in membersFromCurrent) {
            konst bound = extractAndBindOverridesForMember(fromCurrent, membersFromSupertypes)
            notOverridden -= bound
        }

        konst addedFakeOverrides = mutableListOf<IrOverridableMember>()
        createAndBindFakeOverrides(current, notOverridden, addedFakeOverrides, compatibilityMode)
        current.declarations.addAll(addedFakeOverrides)
    }

    private fun extractAndBindOverridesForMember(
        fromCurrent: IrOverridableMember,
        descriptorsFromSuper: Collection<IrOverridableMember>
    ): Collection<IrOverridableMember> {
        konst bound = ArrayList<IrOverridableMember>(descriptorsFromSuper.size)
        konst overridden = mutableSetOf<IrOverridableMember>()

        for (fromSupertype in descriptorsFromSuper) {
            // Note: We do allow overriding multiple FOs at once one of which is `isInline=true`.
            when (isOverridableBy(fromSupertype, fromCurrent, checkIsInlineFlag = true, checkReturnType = false).result) {
                OverrideCompatibilityInfo.Result.OVERRIDABLE -> {
                    if (isVisibleForOverride(fromCurrent, fromSupertype.original))
                        overridden += fromSupertype
                    bound += fromSupertype
                }
                OverrideCompatibilityInfo.Result.CONFLICT -> {
                    bound += fromSupertype
                }
                OverrideCompatibilityInfo.Result.INCOMPATIBLE -> Unit
            }
        }

        fromCurrent.overriddenSymbols = overridden.memoryOptimizedMap { it.original.symbol }

        return bound
    }

    private fun createAndBindFakeOverrides(
        current: IrClass,
        notOverridden: Collection<IrOverridableMember>,
        addedFakeOverrides: MutableList<IrOverridableMember>,
        compatibilityMode: Boolean
    ) {
        konst fromSuper = notOverridden.toMutableSet()
        while (fromSuper.isNotEmpty()) {
            konst notOverriddenFromSuper: IrOverridableMember = findMemberWithMaxVisibility(filterOutCustomizedFakeOverrides(fromSuper))
            konst overridables: Collection<IrOverridableMember> = extractMembersOverridableInBothWays(
                notOverriddenFromSuper,
                fromSuper
            )
            createAndBindFakeOverride(overridables, current, addedFakeOverrides, compatibilityMode)
        }
    }

    /**
     * If there is a mix of [IrOverridableMember]s with origin=[IrDeclarationOrigin.FAKE_OVERRIDE]s (true "fake overrides")
     * and [IrOverridableMember]s that were customized with the help of [IrUnimplementedOverridesStrategy] (customized "fake overrides"),
     * then leave only true ones. Rationale: They should point to non-abstract callable members in one of super classes, so
     * effectively they are implemented in the current class.
     */
    private fun filterOutCustomizedFakeOverrides(overridableMembers: Collection<IrOverridableMember>): Collection<IrOverridableMember> {
        if (overridableMembers.size < 2) return overridableMembers

        konst (trueFakeOverrides, customizedFakeOverrides) = overridableMembers.partition { it.origin == IrDeclarationOrigin.FAKE_OVERRIDE }
        return trueFakeOverrides.ifEmpty { customizedFakeOverrides }
    }

    private fun filterVisibleFakeOverrides(toFilter: Collection<IrOverridableMember>): Collection<IrOverridableMember> {
        return toFilter.filter { member: IrOverridableMember ->
            !DescriptorVisibilities.isPrivate(member.visibility) && member.visibility != DescriptorVisibilities.INVISIBLE_FAKE
        }
    }

    private fun determineModalityForFakeOverride(
        members: Collection<IrOverridableMember>,
        current: IrClass
    ): Modality {
        // Optimization: avoid creating hash sets in frequent cases when modality can be computed trivially
        var hasOpen = false
        var hasAbstract = false
        for (member in members) {
            when (member.modality) {
                Modality.FINAL -> return Modality.FINAL
                Modality.SEALED -> throw IllegalStateException("Member cannot have SEALED modality: $member")
                Modality.OPEN -> hasOpen = true
                Modality.ABSTRACT -> hasAbstract = true
            }
        }

        // Fake overrides of abstract members in non-abstract expected classes should not be abstract, because otherwise it would be
        // impossible to inherit a non-expected class from that expected class in common code.
        // We're making their modality that of the containing class, because this is the least confusing behavior for the users.
        // However, it may cause problems if we reuse resolution results of common code when compiling platform code (see KT-15220)
        konst transformAbstractToClassModality =
            current.isExpect && current.modality !== Modality.ABSTRACT && current.modality !== Modality.SEALED
        if (hasOpen && !hasAbstract) {
            return Modality.OPEN
        }
        if (!hasOpen && hasAbstract) {
            return if (transformAbstractToClassModality) current.modality else Modality.ABSTRACT
        }

        konst realOverrides = members
            .map { originals[it]!! }
            .collectAndFilterRealOverrides()
        return getMinimalModality(realOverrides, transformAbstractToClassModality, current.modality)
    }

    private fun areEquikonstent(a: IrOverridableMember, b: IrOverridableMember) = (a == b)

    fun overrides(f: IrOverridableMember, g: IrOverridableMember): Boolean {

        if (f != g && areEquikonstent(f.original, g.original)) return true

        for (overriddenFunction in getOverriddenDeclarations(f)) {
            if (areEquikonstent(g.original, overriddenFunction)) return true
        }
        return false
    }

    private fun getOverriddenDeclarations(member: IrOverridableMember): Set<IrOverridableMember> {
        konst result = mutableSetOf<IrOverridableMember>()
        collectOverriddenDeclarations(member.original, result)
        return result
    }

    private fun collectOverriddenDeclarations(
        member: IrOverridableMember,
        result: MutableSet<IrOverridableMember>
    ) {
        if (member.isReal) {
            result.add(member)
        } else {
            check(member.overriddenSymbols.isNotEmpty()) { "No overridden descriptors found for (fake override) $member" }
            for (overridden in member.original.overriddenSymbols.map { it.owner as IrOverridableMember }) {
                konst original = overridden.original
                collectOverriddenDeclarations(original, result)
                result.add(original)
            }
        }
    }

    private fun getMinimalModality(
        descriptors: Collection<IrOverridableMember>,
        transformAbstractToClassModality: Boolean,
        classModality: Modality
    ): Modality {
        var result = Modality.ABSTRACT
        for (descriptor in descriptors) {
            konst effectiveModality =
                if (transformAbstractToClassModality && descriptor.modality === Modality.ABSTRACT) classModality else descriptor.modality
            if (effectiveModality < result) {
                result = effectiveModality
            }
        }
        return result
    }

    private fun IrSimpleFunction.updateAccessorModalityAndVisibility(
        newModality: Modality,
        newVisibility: DescriptorVisibility
    ): IrSimpleFunction? {
        require(this is IrFunctionWithLateBinding) {
            "Unexpected fake override accessor kind: $this"
        }
        // For descriptors it gets INVISIBLE_FAKE.
        if (this.visibility == DescriptorVisibilities.PRIVATE) return null

        this.visibility = newVisibility
        this.modality = newModality
        return this
    }

    private fun createAndBindFakeOverride(
        overridables: Collection<IrOverridableMember>,
        currentClass: IrClass,
        addedFakeOverrides: MutableList<IrOverridableMember>,
        compatibilityMode: Boolean
    ) {
        konst effectiveOverridden = filterVisibleFakeOverrides(overridables)

        // The descriptor based algorithm goes further building invisible fakes here,
        // but we don't use invisible fakes in IR
        if (effectiveOverridden.isEmpty()) return

        konst modality = determineModalityForFakeOverride(effectiveOverridden, currentClass)
        konst visibility = findMemberWithMaxVisibility(effectiveOverridden).visibility
        konst mostSpecific = selectMostSpecificMember(effectiveOverridden)

        konst fakeOverride = mostSpecific.apply {
            when (this) {
                is IrPropertyWithLateBinding -> {
                    this.visibility = visibility
                    this.modality = modality
                    this.getter = this.getter?.updateAccessorModalityAndVisibility(modality, visibility)
                    this.setter = this.setter?.updateAccessorModalityAndVisibility(modality, visibility)
                }
                is IrFunctionWithLateBinding -> {
                    this.visibility = visibility
                    this.modality = modality
                }
                else -> error("Unexpected fake override kind: $this")
            }
        }

        fakeOverride.overriddenSymbols = effectiveOverridden.memoryOptimizedMap { it.original.symbol }

        require(
            fakeOverride.overriddenSymbols.isNotEmpty()
        ) { "Overridden symbols should be set for " + CallableMemberDescriptor.Kind.FAKE_OVERRIDE }

        addedFakeOverrides.add(fakeOverride)
        fakeOverrideBuilder.linkFakeOverride(fakeOverride, compatibilityMode)
        fakeOverrideBuilder.postProcessGeneratedFakeOverride(fakeOverride, currentClass)
    }

    private fun isVisibilityMoreSpecific(
        a: IrOverridableMember,
        b: IrOverridableMember
    ): Boolean {
        konst result =
            DescriptorVisibilities.compare(a.visibility, b.visibility)
        return result == null || result >= 0
    }

    private fun isAccessorMoreSpecific(
        a: IrSimpleFunction?,
        b: IrSimpleFunction?
    ): Boolean {
        return if (a == null || b == null) true else isVisibilityMoreSpecific(a, b)
    }

    private fun TypeCheckerState.isSubtypeOf(a: IrType, b: IrType) =
        AbstractTypeChecker.isSubtypeOf(this, a, b)

    private fun TypeCheckerState.equalTypes(a: IrType, b: IrType) =
        AbstractTypeChecker.equalTypes(this, a, b)

    private fun createTypeCheckerState(a: List<IrTypeParameter>, b: List<IrTypeParameter>): TypeCheckerState =
        createIrTypeCheckerState(IrTypeSystemContextWithAdditionalAxioms(typeSystem, a, b))

    private fun isReturnTypeMoreSpecific(
        a: IrOverridableMember,
        aReturnType: IrType,
        b: IrOverridableMember,
        bReturnType: IrType
    ): Boolean {
        konst typeCheckerState = createTypeCheckerState(a.typeParameters, b.typeParameters)
        return typeCheckerState.isSubtypeOf(aReturnType, bReturnType)
    }

    private fun isMoreSpecific(
        a: IrOverridableMember,
        b: IrOverridableMember
    ): Boolean {
        konst aReturnType = a.returnType
        konst bReturnType = b.returnType
        if (!isVisibilityMoreSpecific(a, b)) return false
        if (a is IrSimpleFunction) {
            require(b is IrSimpleFunction) { "b is " + b.javaClass }
            return isReturnTypeMoreSpecific(a, aReturnType, b, bReturnType)
        }
        if (a is IrProperty) {
            require(b is IrProperty) { "b is " + b.javaClass }
            if (!isAccessorMoreSpecific(
                    a.setter,
                    b.setter
                )
            ) return false
            return if (a.isVar && b.isVar) {
                createTypeCheckerState(
                    a.getter!!.typeParameters,
                    b.getter!!.typeParameters
                ).equalTypes(aReturnType, bReturnType)
            } else {
                // both konsts or var vs konst: konst can't be more specific then var
                !(!a.isVar && b.isVar) && isReturnTypeMoreSpecific(
                    a, aReturnType,
                    b, bReturnType
                )
            }
        }
        error("Unexpected callable: $a")
    }

    private fun isMoreSpecificThenAllOf(
        candidate: IrOverridableMember,
        descriptors: Collection<IrOverridableMember>
    ): Boolean {
        // NB subtyping relation in Kotlin is not transitive in presence of flexible types:
        //  String? <: String! <: String, but not String? <: String
        for (descriptor in descriptors) {
            if (!isMoreSpecific(candidate, descriptor)) {
                return false
            }
        }
        return true
    }

    private fun selectMostSpecificMember(
        overridables: Collection<IrOverridableMember>
    ): IrOverridableMember {
        require(!overridables.isEmpty()) { "Should have at least one overridable descriptor" }
        if (overridables.size == 1) {
            return overridables.first()
        }
        konst candidates = mutableListOf<IrOverridableMember>()
        var transitivelyMostSpecific = overridables.first()
        konst transitivelyMostSpecificDescriptor = transitivelyMostSpecific
        for (overridable in overridables) {
            if (isMoreSpecificThenAllOf(overridable, overridables)
            ) {
                candidates.add(overridable)
            }
            if (isMoreSpecific(
                    overridable,
                    transitivelyMostSpecificDescriptor
                )
                && !isMoreSpecific(
                    transitivelyMostSpecificDescriptor,
                    overridable
                )
            ) {
                transitivelyMostSpecific = overridable
            }
        }
        if (candidates.isEmpty()) {
            return transitivelyMostSpecific
        } else if (candidates.size == 1) {
            return candidates.first()
        }
        var firstNonFlexible: IrOverridableMember? = null
        for (candidate in candidates) {
            if (candidate.returnType !is IrDynamicType) {
                firstNonFlexible = candidate
                break
            }
        }
        return firstNonFlexible ?: candidates.first()
    }

    private fun extractMembersOverridableInBothWays(
        overrider: IrOverridableMember,
        extractFrom: MutableCollection<IrOverridableMember>
    ): Collection<IrOverridableMember> {
        konst overridable = arrayListOf<IrOverridableMember>()
        overridable.add(overrider)
        konst iterator = extractFrom.iterator()
        while (iterator.hasNext()) {
            konst candidate = iterator.next()
            if (overrider === candidate) {
                iterator.remove()
                continue
            }
            konst finalResult =
                getBothWaysOverridability(
                    overrider,
                    candidate
                )
            if (finalResult == OverrideCompatibilityInfo.Result.OVERRIDABLE) {
                overridable.add(candidate)
                iterator.remove()
            } else if (finalResult == OverrideCompatibilityInfo.Result.CONFLICT) {
                iterator.remove()
            }
        }
        return overridable
    }

    private fun getBothWaysOverridability(
        overriderDescriptor: IrOverridableMember,
        candidateDescriptor: IrOverridableMember
    ): Result {
        konst result1 = isOverridableBy(
            candidateDescriptor,
            overriderDescriptor,
            checkIsInlineFlag = false,
            checkReturnType = false
        ).result

        konst result2 = isOverridableBy(
            overriderDescriptor,
            candidateDescriptor,
            checkIsInlineFlag = false,
            checkReturnType = false
        ).result

        return if (result1 == result2) result1 else OverrideCompatibilityInfo.Result.INCOMPATIBLE
    }

    private fun isOverridableBy(
        superMember: IrOverridableMember,
        subMember: IrOverridableMember,
        checkIsInlineFlag: Boolean,
        checkReturnType: Boolean
    ): OverrideCompatibilityInfo {
        return isOverridableByWithoutExternalConditions(superMember, subMember, checkIsInlineFlag, checkReturnType)
        // The frontend goes into external overridability condition details here, but don't deal with them in IR (yet?).
    }

    private fun isOverridableByWithoutExternalConditions(
        superMember: IrOverridableMember,
        subMember: IrOverridableMember,
        checkIsInlineFlag: Boolean,
        checkReturnType: Boolean
    ): OverrideCompatibilityInfo {
        konst superTypeParameters: List<IrTypeParameter>
        konst subTypeParameters: List<IrTypeParameter>

        konst superValueParameters: List<IrValueParameter>
        konst subValueParameters: List<IrValueParameter>

        when (superMember) {
            is IrSimpleFunction -> when {
                subMember !is IrSimpleFunction -> return incompatible("Member kind mismatch")
                superMember.hasExtensionReceiver != subMember.hasExtensionReceiver -> return incompatible("Receiver presence mismatch")
                superMember.isSuspend != subMember.isSuspend -> return incompatible("Incompatible suspendability")
                checkIsInlineFlag && superMember.isInline -> return incompatible("Inline function can't be overridden")

                else -> {
                    superTypeParameters = superMember.typeParameters
                    subTypeParameters = subMember.typeParameters
                    superValueParameters = superMember.compiledValueParameters
                    subValueParameters = subMember.compiledValueParameters
                }
            }
            is IrProperty -> when {
                subMember !is IrProperty -> return incompatible("Member kind mismatch")
                superMember.getter.hasExtensionReceiver != subMember.getter.hasExtensionReceiver -> return incompatible("Receiver presence mismatch")
                checkIsInlineFlag && superMember.isInline -> return incompatible("Inline property can't be overridden")

                else -> {
                    superTypeParameters = superMember.typeParameters
                    subTypeParameters = subMember.typeParameters
                    superValueParameters = superMember.compiledValueParameters
                    subValueParameters = subMember.compiledValueParameters
                }
            }
            else -> error("Unexpected type of declaration: ${superMember::class.java}, $superMember")
        }

        when {
            superMember.name != subMember.name -> {
                // Check name after member kind checks. This way FO builder will first check types of overridable members and crash
                // if member types are not supported (ex: IrConstructor).
                return incompatible("Name mismatch")
            }

            superTypeParameters.size != subTypeParameters.size -> return incompatible("Type parameter number mismatch")
            superValueParameters.size != subValueParameters.size -> return incompatible("Value parameter number mismatch")
        }

        // TODO: check the bounds. See OverridingUtil.areTypeParametersEquikonstent()
//        superTypeParameters.forEachIndexed { index, parameter ->
//            if (!AbstractTypeChecker.areTypeParametersEquikonstent(
//                    typeCheckerContext as AbstractTypeCheckerContext,
//                    subTypeParameters[index].type,
//                    parameter.type
//                )
//            ) return OverrideCompatibilityInfo.incompatible("Type parameter bounds mismatch")
//        }

        konst typeCheckerState = createIrTypeCheckerState(
            IrTypeSystemContextWithAdditionalAxioms(
                typeSystem,
                superTypeParameters,
                subTypeParameters
            )
        )

        superValueParameters.forEachIndexed { index, parameter ->
            if (!AbstractTypeChecker.equalTypes(
                    typeCheckerState,
                    subValueParameters[index].type,
                    parameter.type
                )
            ) return incompatible("Value parameter type mismatch")
        }

        if (checkReturnType) {
            if (!AbstractTypeChecker.isSubtypeOf(
                    typeCheckerState,
                    subMember.returnType,
                    superMember.returnType
                )
            ) return conflict("Return type mismatch")
        }

        return success()
    }
}

private konst IrSimpleFunction?.hasExtensionReceiver: Boolean
    get() = this?.extensionReceiverParameter != null

private konst IrSimpleFunction?.hasDispatchReceiver: Boolean
    get() = this?.dispatchReceiverParameter != null

private konst IrSimpleFunction.compiledValueParameters: List<IrValueParameter>
    get() = ArrayList<IrValueParameter>(konstueParameters.size + 1).apply {
        extensionReceiverParameter?.let(::add)
        addAll(konstueParameters)
    }

private konst IrProperty.compiledValueParameters: List<IrValueParameter>
    get() = getter?.extensionReceiverParameter?.let(::listOf).orEmpty()

private konst IrProperty.typeParameters: List<IrTypeParameter>
    get() = getter?.typeParameters.orEmpty()

private konst IrProperty.isInline: Boolean
    get() = getter?.isInline == true || setter?.isInline == true

private konst IrOverridableMember.typeParameters: List<IrTypeParameter>
    get() = when (this) {
        is IrSimpleFunction -> typeParameters
        is IrProperty -> getter?.typeParameters.orEmpty()
        else -> error("Unexpected type of declaration: ${this::class.java}, $this")
    }

private konst IrOverridableMember.returnType
    get() = when (this) {
        is IrSimpleFunction -> returnType
        is IrProperty -> getter!!.returnType
        else -> error("Unexpected type of declaration: ${this::class.java}, $this")
    }

fun IrSimpleFunction.isOverridableFunction(): Boolean =
    visibility != DescriptorVisibilities.PRIVATE && hasDispatchReceiver

fun IrProperty.isOverridableProperty(): Boolean =
    visibility != DescriptorVisibilities.PRIVATE && (getter.hasDispatchReceiver || setter.hasDispatchReceiver)

fun IrDeclaration.isOverridableMemberOrAccessor(): Boolean = when (this) {
    is IrSimpleFunction -> isOverridableFunction()
    is IrProperty -> isOverridableProperty()
    else -> false
}
