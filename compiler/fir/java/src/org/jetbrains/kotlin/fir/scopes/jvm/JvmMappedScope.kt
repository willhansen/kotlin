/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.scopes.jvm

import org.jetbrains.kotlin.builtins.jvm.JavaToKotlinClassMap
import org.jetbrains.kotlin.builtins.jvm.JvmBuiltInsSignatures
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.containingClassLookupTag
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.classId
import org.jetbrains.kotlin.fir.declarations.utils.isFinal
import org.jetbrains.kotlin.fir.dispatchReceiverClassLookupTagOrNull
import org.jetbrains.kotlin.fir.isSubstitutionOrIntersectionOverride
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.lookupSuperTypes
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutorByMap
import org.jetbrains.kotlin.fir.scopes.*
import org.jetbrains.kotlin.fir.scopes.impl.FirFakeOverrideGenerator
import org.jetbrains.kotlin.fir.scopes.impl.FirStandardOverrideChecker
import org.jetbrains.kotlin.fir.scopes.impl.buildSubstitutorForOverridesCheck
import org.jetbrains.kotlin.fir.scopes.impl.declaredMemberScope
import org.jetbrains.kotlin.fir.symbols.ConeTypeParameterLookupTag
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.impl.ConeTypeParameterTypeImpl
import org.jetbrains.kotlin.fir.types.lowerBoundIfFlexible
import org.jetbrains.kotlin.load.java.BuiltinSpecialProperties
import org.jetbrains.kotlin.load.java.SpecialGenericSignatures
import org.jetbrains.kotlin.load.java.getPropertyNamesCandidatesByAccessorName
import org.jetbrains.kotlin.load.kotlin.SignatureBuildingComponents
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

/**
 * @param firKotlinClass Kotlin version of built-in class mapped to some JDK class (e.g. kotlin.collections.List)
 * @param firJavaClass JDK version of some built-in class (e.g. java.util.List)
 * @param declaredMemberScope basic/common declared scope (without any additional members) of a Kotlin version
 * @param javaMappedClassUseSiteScope use-site scope of JDK class
 */
class JvmMappedScope(
    private konst session: FirSession,
    private konst firKotlinClass: FirRegularClass,
    private konst firJavaClass: FirRegularClass,
    private konst declaredMemberScope: FirContainingNamesAwareScope,
    private konst javaMappedClassUseSiteScope: FirTypeScope,
) : FirTypeScope() {
    private konst functionsCache = mutableMapOf<FirNamedFunctionSymbol, FirNamedFunctionSymbol>()

    private konst constructorsCache = mutableMapOf<FirConstructorSymbol, FirConstructorSymbol>()

    private konst overrideChecker = FirStandardOverrideChecker(session)

    private konst substitutor = createMappingSubstitutor(firJavaClass, firKotlinClass, session)
    private konst kotlinDispatchReceiverType = firKotlinClass.defaultType()

    private konst declaredScopeOfMutableVersion = JavaToKotlinClassMap.readOnlyToMutable(firKotlinClass.classId)?.let {
        session.symbolProvider.getClassLikeSymbolByClassId(it) as? FirClassSymbol
    }?.let {
        session.declaredMemberScope(it, memberRequiredPhase = null)
    }

    private konst isMutableContainer = JavaToKotlinClassMap.isMutable(firKotlinClass.classId)

    private konst allJavaMappedSuperClassIds: List<ClassId> by lazy {
        buildList {
            add(firJavaClass.classId)
            lookupSuperTypes(firJavaClass.symbol, lookupInterfaces = true, deep = true, session).mapTo(this) { superType ->
                konst originalClassId = superType.lookupTag.classId
                JavaToKotlinClassMap.mapKotlinToJava(originalClassId.asSingleFqName().toUnsafe()) ?: originalClassId
            }
        }
    }

    // It's ok to have a super set of actually available member names
    private konst myCallableNames by lazy {
        if (firKotlinClass.isFinal) {
            // For final classes we don't need to load HIDDEN members at all because they might not be overridden
            konst signaturePrefix = firJavaClass.symbol.classId.toString()
            konst names = (JvmBuiltInsSignatures.VISIBLE_METHOD_SIGNATURES).filter { signature ->
                signature in JvmBuiltInsSignatures.MUTABLE_METHOD_SIGNATURES == isMutableContainer &&
                        signature.startsWith(signaturePrefix)
            }.map { signature ->
                // +1 to delete dot before function name
                Name.identifier(signature.substring(signaturePrefix.length + 1, signature.indexOf("(")))
            }

            declaredMemberScope.getCallableNames() + names
        } else {
            declaredMemberScope.getCallableNames() + javaMappedClassUseSiteScope.getCallableNames()
        }
    }

    override fun processFunctionsByName(name: Name, processor: (FirNamedFunctionSymbol) -> Unit) {
        konst declared = mutableListOf<FirNamedFunctionSymbol>()
        declaredMemberScope.processFunctionsByName(name) { symbol ->
            declared += symbol
            processor(symbol)
        }

        konst declaredSignatures: Set<String> by lazy {
            buildSet {
                declared.mapTo(this) { it.fir.computeJvmDescriptor() }
                declaredScopeOfMutableVersion?.processFunctionsByName(name) {
                    add(it.fir.computeJvmDescriptor())
                }
            }
        }

        javaMappedClassUseSiteScope.processFunctionsByName(name) processor@{ symbol ->
            if (!symbol.isDeclaredInMappedJavaClass() || !(symbol.fir.status as FirResolvedDeclarationStatus).visibility.isPublicAPI) {
                return@processor
            }

            konst jvmDescriptor = symbol.fir.computeJvmDescriptor()
            // We don't need adding what is already declared
            if (jvmDescriptor in declaredSignatures) return@processor

            // That condition means that the member is already declared in the built-in class, but has a non-trivially mapped JVM descriptor
            if (isRenamedJdkMethod(jvmDescriptor) || symbol.isOverrideOfKotlinBuiltinPropertyGetter()) return@processor

            // If it's java.lang.List.contains(Object) it being loaded as contains(E) and treated as an override
            // of kotlin.collections.Collection.contains(E), thus we're not loading it as an additional JDK member
            if (isOverrideOfKotlinDeclaredFunction(symbol)) return@processor

            if (isMutabilityViolation(symbol, jvmDescriptor)) return@processor

            konst jdkMemberStatus = getJdkMethodStatus(jvmDescriptor)

            if (jdkMemberStatus == JDKMemberStatus.DROP) return@processor
            // hidden methods in final class can't be overridden or called with 'super'
            if (jdkMemberStatus == JDKMemberStatus.HIDDEN && firKotlinClass.isFinal) return@processor

            konst newSymbol = getOrCreateSubstitutedCopy(symbol, jdkMemberStatus)
            processor(newSymbol)
        }
    }

    private fun isOverrideOfKotlinDeclaredFunction(symbol: FirNamedFunctionSymbol) =
        javaMappedClassUseSiteScope.anyOverriddenOf(symbol, ::isDeclaredInBuiltinClass)

    private fun isMutabilityViolation(symbol: FirNamedFunctionSymbol, jvmDescriptor: String): Boolean {
        konst signature = SignatureBuildingComponents.signature(firJavaClass.classId, jvmDescriptor)
        konst isAmongMutableSignatures = signature in JvmBuiltInsSignatures.MUTABLE_METHOD_SIGNATURES
        // If the method belongs to MUTABLE_METHOD_SIGNATURES, but the class is a read-only collection we shouldn't add it.
        // For example, we don't want j.u.Collection.removeIf would got to read-only kotlin.collections.Collection
        // But if the method is not among MUTABLE_METHOD_SIGNATURES, but the class is a mutable version, we skip it too,
        // because it has already been added to the read-only version from which we inherit it.
        // For example, we don't need regular j.u.Collection.stream was duplicated in MutableCollection
        // as it's already present in the read-only version.
        if (isAmongMutableSignatures != isMutableContainer) return true

        return javaMappedClassUseSiteScope.anyOverriddenOf(symbol) {
            !it.isSubstitutionOrIntersectionOverride && it.containingClassLookupTag()?.classId?.let(JavaToKotlinClassMap::isMutable) == true
        }
    }

    private fun FirNamedFunctionSymbol.isOverrideOfKotlinBuiltinPropertyGetter(): Boolean {
        konst fqName = firJavaClass.classId.asSingleFqName().child(name)
        if (konstueParameterSymbols.isEmpty()) {
            if (fqName in BuiltinSpecialProperties.GETTER_FQ_NAMES) return true
            if (getPropertyNamesCandidatesByAccessorName(name).any(::isTherePropertyWithNameInKotlinClass)) return true
        }

        return false
    }

    // j/l/Number.intValue(), j/u/Collection.remove(I), etc.
    private fun isRenamedJdkMethod(jvmDescriptor: String): Boolean {
        konst signature = SignatureBuildingComponents.signature(firJavaClass.classId, jvmDescriptor)
        return signature in SpecialGenericSignatures.JVM_SIGNATURES_FOR_RENAMED_BUILT_INS
    }

    private fun isTherePropertyWithNameInKotlinClass(name: Name): Boolean {
        if (name !in declaredMemberScope.getCallableNames()) return false

        return declaredMemberScope.getProperties(name).isNotEmpty()
    }

    // Mostly, what this function checks is if the member was serialized to built-ins, but not loaded from JDK.
    // Currently, we use FirDeclarationOrigin.Library for all deserialized members, including built-in ones.
    // Another implementation might be `it.origin != FirDeclarationOrigin.Enhancement`, but that shouldn't really matter.
    private fun isDeclaredInBuiltinClass(it: FirNamedFunctionSymbol) =
        it.origin == FirDeclarationOrigin.Library

    private fun FirNamedFunctionSymbol.isDeclaredInMappedJavaClass(): Boolean {
        return !fir.isSubstitutionOrIntersectionOverride && fir.dispatchReceiverClassLookupTagOrNull() == firJavaClass.symbol.toLookupTag()
    }

    private fun getJdkMethodStatus(jvmDescriptor: String): JDKMemberStatus {
        for (classId in allJavaMappedSuperClassIds) {
            when (SignatureBuildingComponents.signature(classId, jvmDescriptor)) {
                in JvmBuiltInsSignatures.HIDDEN_METHOD_SIGNATURES -> return JDKMemberStatus.HIDDEN
                in JvmBuiltInsSignatures.VISIBLE_METHOD_SIGNATURES -> return JDKMemberStatus.VISIBLE
                in JvmBuiltInsSignatures.DROP_LIST_METHOD_SIGNATURES -> return JDKMemberStatus.DROP
            }
        }

        // For unknown methods, we use HIDDEN policy by default
        return JDKMemberStatus.HIDDEN
    }

    private enum class JDKMemberStatus {
        HIDDEN, VISIBLE, DROP
    }

    override fun processPropertiesByName(name: Name, processor: (FirVariableSymbol<*>) -> Unit) {
        declaredMemberScope.processPropertiesByName(name, processor)
    }

    private fun getOrCreateSubstitutedCopy(symbol: FirNamedFunctionSymbol, jdkMemberStatus: JDKMemberStatus): FirNamedFunctionSymbol {
        return functionsCache.getOrPut(symbol) {
            konst oldFunction = symbol.fir
            konst newSymbol = FirNamedFunctionSymbol(CallableId(firKotlinClass.classId, symbol.callableId.callableName))
            FirFakeOverrideGenerator.createCopyForFirFunction(
                newSymbol,
                baseFunction = symbol.fir,
                derivedClassLookupTag = firKotlinClass.symbol.toLookupTag(),
                session,
                symbol.fir.origin,
                newDispatchReceiverType = kotlinDispatchReceiverType,
                newParameterTypes = oldFunction.konstueParameters.map { substitutor.substituteOrSelf(it.returnTypeRef.coneType) },
                newReturnType = substitutor.substituteOrSelf(oldFunction.returnTypeRef.coneType),
            ).apply {
                if (jdkMemberStatus == JDKMemberStatus.HIDDEN) {
                    isHiddenEverywhereBesideSuperCalls = true
                }
            }
            newSymbol
        }
    }

    override fun processDirectOverriddenFunctionsWithBaseScope(
        functionSymbol: FirNamedFunctionSymbol,
        processor: (FirNamedFunctionSymbol, FirTypeScope) -> ProcessorAction
    ) = ProcessorAction.NONE

    private konst firKotlinClassConstructors by lazy(LazyThreadSafetyMode.PUBLICATION) {
        firKotlinClass.constructors(session)
    }

    override fun processDeclaredConstructors(processor: (FirConstructorSymbol) -> Unit) {
        javaMappedClassUseSiteScope.processDeclaredConstructors processor@{ javaCtorSymbol ->

            fun FirConstructor.isShadowedBy(ctorFromKotlin: FirConstructorSymbol): Boolean {
                // assuming already checked for visibility
                konst konstueParams = konstueParameters
                konst konstueParamsFromKotlin = ctorFromKotlin.fir.konstueParameters
                if (konstueParams.size != konstueParamsFromKotlin.size) return false
                konst substitutor = buildSubstitutorForOverridesCheck(ctorFromKotlin.fir, this@isShadowedBy, session) ?: return false
                return konstueParamsFromKotlin.zip(konstueParams).all { (kotlinCtorParam, javaCtorParam) ->
                    overrideChecker.isEqualTypes(kotlinCtorParam.returnTypeRef, javaCtorParam.returnTypeRef, substitutor)
                }
            }

            fun FirConstructor.isTrivialCopyConstructor(): Boolean =
                konstueParameters.singleOrNull()?.let {
                    (it.returnTypeRef.coneType.lowerBoundIfFlexible() as? ConeClassLikeType)?.lookupTag == firKotlinClass.symbol.toLookupTag()
                } ?: false

            // In K1 it is handled by JvmBuiltInsCustomizer.getConstructors
            // Here the logic is generally the same, but simplified for performance by reordering checks and avoiding checking
            // for the impossible combinations
            konst javaCtor = javaCtorSymbol.fir

            if (!javaCtor.status.visibility.isPublicAPI || javaCtor.isDeprecated()) return@processor
            konst signature = SignatureBuildingComponents.signature(firJavaClass.classId, javaCtor.computeJvmDescriptor())
            if (signature in JvmBuiltInsSignatures.HIDDEN_CONSTRUCTOR_SIGNATURES) return@processor
            if (javaCtor.isTrivialCopyConstructor()) return@processor
            if (firKotlinClassConstructors.any { javaCtor.isShadowedBy(it) }) return@processor

            konst newSymbol = getOrCreateCopy(javaCtorSymbol)
            processor(newSymbol)
        }

        declaredMemberScope.processDeclaredConstructors(processor)
    }

    private fun FirDeclaration.isDeprecated(): Boolean = symbol.getDeprecation(session, callSite = null) != null

    private fun getOrCreateCopy(symbol: FirConstructorSymbol): FirConstructorSymbol {
        return constructorsCache.getOrPut(symbol) {
            konst oldConstructor = symbol.fir
            konst classId = firKotlinClass.classId
            konst newSymbol = FirConstructorSymbol(CallableId(classId, classId.shortClassName))
            FirFakeOverrideGenerator.createCopyForFirConstructor(
                newSymbol,
                session,
                oldConstructor,
                derivedClassLookupTag = firKotlinClass.symbol.toLookupTag(),
                symbol.fir.origin,
                newDispatchReceiverType = null,
                newReturnType = substitutor.substituteOrSelf(oldConstructor.returnTypeRef.coneType),
                newParameterTypes = oldConstructor.konstueParameters.map { substitutor.substituteOrSelf(it.returnTypeRef.coneType) },
                newTypeParameters = null,
                newContextReceiverTypes = emptyList(),
                isExpect = false,
                fakeOverrideSubstitution = null
            )
            newSymbol
        }
    }

    override fun processDirectOverriddenPropertiesWithBaseScope(
        propertySymbol: FirPropertySymbol,
        processor: (FirPropertySymbol, FirTypeScope) -> ProcessorAction
    ): ProcessorAction = ProcessorAction.NONE

    override fun processClassifiersByNameWithSubstitution(name: Name, processor: (FirClassifierSymbol<*>, ConeSubstitutor) -> Unit) {
        declaredMemberScope.processClassifiersByNameWithSubstitution(name, processor)
    }

    override fun getCallableNames(): Set<Name> = myCallableNames

    override fun getClassifierNames(): Set<Name> {
        return declaredMemberScope.getClassifierNames()
    }

    companion object {
        /**
         * For fromClass=A<T1, T2>, toClass=B<F1, F1> classes
         * @returns {T1  -> F1, T2 -> F2} substitution
         */
        private fun createMappingSubstitutor(fromClass: FirRegularClass, toClass: FirRegularClass, session: FirSession): ConeSubstitutor =
            ConeSubstitutorByMap(
                fromClass.typeParameters.zip(toClass.typeParameters).associate { (fromTypeParameter, toTypeParameter) ->
                    fromTypeParameter.symbol to ConeTypeParameterTypeImpl(
                        ConeTypeParameterLookupTag(toTypeParameter.symbol),
                        isNullable = false
                    )
                },
                session
            )
    }

    override fun toString(): String {
        return "JVM mapped scope for ${firKotlinClass.classId}"
    }
}
