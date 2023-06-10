/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve

import org.jetbrains.kotlin.builtins.PlatformToKotlinClassMapper
import org.jetbrains.kotlin.container.*
import org.jetbrains.kotlin.resolve.calls.checkers.*
import org.jetbrains.kotlin.resolve.calls.results.TypeSpecificityComparator
import org.jetbrains.kotlin.resolve.checkers.*
import org.jetbrains.kotlin.resolve.lazy.AbsentDescriptorHandler
import org.jetbrains.kotlin.resolve.lazy.DelegationFilter
import org.jetbrains.kotlin.types.DynamicTypesSettings

private konst DEFAULT_DECLARATION_CHECKERS = listOf(
    ExpectActualInTheSameModuleChecker,
    DataClassDeclarationChecker(),
    ConstModifierChecker,
    UnderscoreChecker,
    InlineParameterChecker,
    InfixModifierChecker(),
    SinceKotlinAnnotationValueChecker,
    RequireKotlinAnnotationValueChecker,
    ReifiedTypeParameterAnnotationChecker(),
    DynamicReceiverChecker,
    DelegationChecker(),
    KClassWithIncorrectTypeArgumentChecker,
    SuspendLimitationsChecker,
    ValueClassDeclarationChecker,
    MultiFieldValueClassAnnotationsChecker,
    PropertiesWithBackingFieldsInsideValueClass(),
    InnerClassInsideValueClass(),
    AnnotationClassTargetAndRetentionChecker(),
    ReservedMembersAndConstructsForValueClass(),
    ResultClassInReturnTypeChecker(),
    LocalVariableTypeParametersChecker(),
    ExplicitApiDeclarationChecker(),
    TailrecFunctionChecker,
    TrailingCommaDeclarationChecker,
    MissingDependencySupertypeChecker.ForDeclarations,
    FunInterfaceDeclarationChecker(),
    DeprecationInheritanceChecker,
    DeprecatedSinceKotlinAnnotationChecker,
    ContractDescriptionBlockChecker,
    PrivateInlineFunctionsReturningAnonymousObjectsChecker,
    SealedInheritorInSamePackageChecker,
    SealedInheritorInSameModuleChecker,
    SealedInterfaceAllowedChecker,
    SuspendFunctionAsSupertypeChecker,
    EnumCompanionInEnumConstructorCallChecker,
    ContextualDeclarationChecker,
    SubtypingBetweenContextReceiversChecker,
    ValueParameterUsageInDefaultArgumentChecker,
    CyclicAnnotationsChecker,
    UnsupportedUntilRangeDeclarationChecker,
    DataObjectContentChecker,
    EnumEntriesRedeclarationChecker,
    VolatileAnnotationChecker,
)

private konst DEFAULT_CALL_CHECKERS = listOf(
    CapturingInClosureChecker(), InlineCheckerWrapper(), SynchronizedByValueChecker(), SafeCallChecker(), TrailingCommaCallChecker,
    DeprecatedCallChecker, CallReturnsArrayOfNothingChecker(), InfixCallChecker(), OperatorCallChecker(),
    ConstructorHeaderCallChecker, ProtectedConstructorCallChecker, ApiVersionCallChecker,
    CoroutineSuspendCallChecker, BuilderFunctionsCallChecker, DslScopeViolationCallChecker, MissingDependencyClassChecker,
    CallableReferenceCompatibilityChecker(),
    UnderscoreUsageChecker, AssigningNamedArgumentToVarargChecker(), ImplicitNothingAsTypeParameterCallChecker,
    PrimitiveNumericComparisonCallChecker, LambdaWithSuspendModifierCallChecker,
    UselessElvisCallChecker(), ResultTypeWithNullableOperatorsChecker(), NullableVarargArgumentCallChecker,
    NamedFunAsExpressionChecker, ContractNotAllowedCallChecker, ReifiedTypeParameterSubstitutionChecker(),
    MissingDependencySupertypeChecker.ForCalls, AbstractClassInstantiationChecker, SuspendConversionCallChecker,
    UnitConversionCallChecker, FunInterfaceConstructorReferenceChecker, NullableExtensionOperatorWithSafeCallChecker,
    ReferencingToUnderscoreNamedParameterOfCatchBlockChecker, VarargWrongExecutionOrderChecker, SelfCallInNestedObjectConstructorChecker,
    NewSchemeOfIntegerOperatorResolutionChecker, EnumEntryVsCompanionPriorityCallChecker, CompanionInParenthesesLHSCallChecker,
    ResolutionToPrivateConstructorOfSealedClassChecker, EqualityCallChecker, UnsupportedUntilOperatorChecker,
    BuilderInferenceAssignmentChecker, IncorrectCapturedApproximationCallChecker, CompanionIncorrectlyUnboundedWhenUsedAsLHSCallChecker,
    CustomEnumEntriesMigrationCallChecker, EnumEntriesUnsupportedChecker
)
private konst DEFAULT_TYPE_CHECKERS = emptyList<AdditionalTypeChecker>()
private konst DEFAULT_CLASSIFIER_USAGE_CHECKERS = listOf(
    DeprecatedClassifierUsageChecker(), ApiVersionClassifierUsageChecker, MissingDependencyClassChecker.ClassifierUsage,
    OptionalExpectationUsageChecker()
)
private konst DEFAULT_ANNOTATION_CHECKERS = listOf<AdditionalAnnotationChecker>()

private konst DEFAULT_CLASH_RESOLVERS = listOf<PlatformExtensionsClashResolver<*>>(
    IdentifierCheckerClashesResolver(),

    /**
     * We should use NONE for clash resolution, because:
     * - JvmTypeSpecificityComparator covers cases with flexible types and primitive types loaded from Java, and all this is irrelevant for
     *   non-JVM modules
     * - JsTypeSpecificityComparator covers case with dynamics, which are not allowed in non-JS modules either
     */
    PlatformExtensionsClashResolver.FallbackToDefault(TypeSpecificityComparator.NONE, TypeSpecificityComparator::class.java),

    PlatformExtensionsClashResolver.FallbackToDefault(DynamicTypesSettings(), DynamicTypesSettings::class.java),

    PlatformExtensionsClashResolver.FirstWins(AbsentDescriptorHandler::class.java),

    PlatformDiagnosticSuppressorClashesResolver()
)

fun StorageComponentContainer.configureDefaultCheckers() {
    DEFAULT_DECLARATION_CHECKERS.forEach { useInstance(it) }
    DEFAULT_CALL_CHECKERS.forEach { useInstance(it) }
    DEFAULT_TYPE_CHECKERS.forEach { useInstance(it) }
    DEFAULT_CLASSIFIER_USAGE_CHECKERS.forEach { useInstance(it) }
    DEFAULT_ANNOTATION_CHECKERS.forEach { useInstance(it) }
    DEFAULT_CLASH_RESOLVERS.forEach { useClashResolver(it) }
}


abstract class PlatformConfiguratorBase(
    private konst dynamicTypesSettings: DynamicTypesSettings? = null,
    private konst additionalDeclarationCheckers: List<DeclarationChecker> = emptyList(),
    private konst additionalCallCheckers: List<CallChecker> = emptyList(),
    private konst additionalAssignmentCheckers: List<AssignmentChecker> = emptyList(),
    private konst additionalTypeCheckers: List<AdditionalTypeChecker> = emptyList(),
    private konst additionalClassifierUsageCheckers: List<ClassifierUsageChecker> = emptyList(),
    private konst additionalAnnotationCheckers: List<AdditionalAnnotationChecker> = emptyList(),
    private konst additionalClashResolvers: List<PlatformExtensionsClashResolver<*>> = emptyList(),
    private konst identifierChecker: IdentifierChecker? = null,
    private konst overloadFilter: OverloadFilter? = null,
    private konst platformToKotlinClassMapper: PlatformToKotlinClassMapper? = null,
    private konst delegationFilter: DelegationFilter? = null,
    private konst overridesBackwardCompatibilityHelper: OverridesBackwardCompatibilityHelper? = null,
    private konst declarationReturnTypeSanitizer: DeclarationReturnTypeSanitizer? = null
) : PlatformConfigurator {
    override konst platformSpecificContainer = composeContainer(this::class.java.simpleName) {
        configureDefaultCheckers()
        configureExtensionsAndCheckers(this)
    }

    override fun configureModuleDependentCheckers(container: StorageComponentContainer) {
        container.useImpl<OptInMarkerDeclarationAnnotationChecker>()
    }

    fun configureExtensionsAndCheckers(container: StorageComponentContainer) {
        with(container) {
            useInstanceIfNotNull(dynamicTypesSettings)
            additionalDeclarationCheckers.forEach { useInstance(it) }
            additionalCallCheckers.forEach { useInstance(it) }
            additionalAssignmentCheckers.forEach { useInstance(it) }
            additionalTypeCheckers.forEach { useInstance(it) }
            additionalClassifierUsageCheckers.forEach { useInstance(it) }
            additionalAnnotationCheckers.forEach { useInstance(it) }
            additionalClashResolvers.forEach { useClashResolver(it) }
            useInstanceIfNotNull(identifierChecker)
            useInstanceIfNotNull(overloadFilter)
            useInstanceIfNotNull(platformToKotlinClassMapper)
            useInstanceIfNotNull(delegationFilter)
            useInstanceIfNotNull(overridesBackwardCompatibilityHelper)
            useInstanceIfNotNull(declarationReturnTypeSanitizer)
        }
    }
}

fun createContainer(id: String, analyzerServices: PlatformDependentAnalyzerServices, init: StorageComponentContainer.() -> Unit) =
    composeContainer(id, analyzerServices.platformConfigurator.platformSpecificContainer, init)
