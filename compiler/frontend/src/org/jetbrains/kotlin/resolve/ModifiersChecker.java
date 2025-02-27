/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.config.LanguageFeature;
import org.jetbrains.kotlin.config.LanguageVersionSettings;
import org.jetbrains.kotlin.descriptors.*;
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory1;
import org.jetbrains.kotlin.extensions.DeclarationAttributeAltererExtension;
import org.jetbrains.kotlin.incremental.components.ExpectActualTracker;
import org.jetbrains.kotlin.lexer.KtKeywordToken;
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken;
import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;
import org.jetbrains.kotlin.resolve.checkers.*;
import org.jetbrains.kotlin.resolve.deprecation.DeprecationResolver;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jetbrains.kotlin.diagnostics.Errors.*;
import static org.jetbrains.kotlin.lexer.KtTokens.*;

public class ModifiersChecker {
    private enum DetailedClassKind {
        ENUM_CLASS("Enum class"),
        ENUM_ENTRY("Enum entry"),
        ANNOTATION_CLASS("Annotation class"),
        INTERFACE("Interface"),
        COMPANION_OBJECT("Companion object"),
        ANONYMOUS_OBJECT("Anonymous object"),
        OBJECT("Object"),
        CLASS("Class");

        public final String withCapitalFirstLetter;

        DetailedClassKind(String withCapitalFirstLetter) {
            this.withCapitalFirstLetter = withCapitalFirstLetter;
        }

        @NotNull
        public static DetailedClassKind getClassKind(@NotNull ClassDescriptor descriptor) {
            if (DescriptorUtils.isEnumEntry(descriptor)) return ENUM_ENTRY;
            if (DescriptorUtils.isEnumClass(descriptor)) return ENUM_CLASS;
            if (DescriptorUtils.isAnnotationClass(descriptor)) return ANNOTATION_CLASS;
            if (DescriptorUtils.isInterface(descriptor)) return INTERFACE;
            if (DescriptorUtils.isCompanionObject(descriptor)) return COMPANION_OBJECT;
            if (DescriptorUtils.isAnonymousObject(descriptor)) return ANONYMOUS_OBJECT;
            if (DescriptorUtils.isObject(descriptor)) return OBJECT;
            return CLASS;
        }
    }

    @NotNull
    public static Modality resolveMemberModalityFromModifiers(
            @Nullable KtModifierListOwner modifierListOwner,
            @NotNull Modality defaultModality,
            @NotNull BindingContext bindingContext,
            @Nullable DeclarationDescriptor containingDescriptor
    ) {
        return resolveModalityFromModifiers(modifierListOwner, defaultModality,
                                            bindingContext, containingDescriptor, /* allowSealed = */ false);
    }

    @NotNull
    public static Modality resolveModalityFromModifiers(
            @Nullable KtModifierListOwner modifierListOwner,
            @NotNull Modality defaultModality,
            @NotNull BindingContext bindingContext,
            @Nullable DeclarationDescriptor containingDescriptor,
            boolean allowSealed
    ) {
        KtModifierList modifierList = (modifierListOwner != null) ? modifierListOwner.getModifierList() : null;
        Modality modality = resolveModalityFromModifiers(containingDescriptor, modifierList, defaultModality, allowSealed);

        if (modifierListOwner != null) {
            Collection<DeclarationAttributeAltererExtension> extensions =
                    DeclarationAttributeAltererExtension.Companion.getInstances(modifierListOwner.getProject());

            DeclarationDescriptor descriptor = bindingContext.get(BindingContext.DECLARATION_TO_DESCRIPTOR, modifierListOwner);
            for (DeclarationAttributeAltererExtension extension : extensions) {
                Modality newModality = extension.refineDeclarationModality(
                        modifierListOwner, descriptor, containingDescriptor, modality, false);

                if (newModality != null) {
                    modality = newModality;
                    break;
                }
            }
        }

        return modality;
    }

    @NotNull
    private static Modality resolveModalityFromModifiers(
            @Nullable DeclarationDescriptor containingDescriptor,
            @Nullable KtModifierList modifierList,
            @NotNull Modality defaultModality,
            boolean allowSealed
    ) {
        if (modifierList == null) return defaultModality;
        boolean hasAbstractModifier = modifierList.hasModifier(ABSTRACT_KEYWORD);
        boolean hasOverrideModifier = modifierList.hasModifier(OVERRIDE_KEYWORD);

        if (allowSealed && modifierList.hasModifier(SEALED_KEYWORD)) {
            return Modality.SEALED;
        }
        if (modifierList.hasModifier(OPEN_KEYWORD)) {
            if (containingDescriptor instanceof ClassDescriptor) {
                ClassDescriptor classOrInterface = (ClassDescriptor) containingDescriptor;
                if (classOrInterface.getKind() == ClassKind.INTERFACE && classOrInterface.isExpect()) {
                    return Modality.OPEN;
                }
            }
            if (hasAbstractModifier || defaultModality == Modality.ABSTRACT) {
                return Modality.ABSTRACT;
            }
            return Modality.OPEN;
        }
        if (hasAbstractModifier) {
            return Modality.ABSTRACT;
        }
        boolean hasFinalModifier = modifierList.hasModifier(FINAL_KEYWORD);
        if (hasOverrideModifier && !hasFinalModifier && !(defaultModality == Modality.ABSTRACT)) {
            return Modality.OPEN;
        }
        if (hasFinalModifier) {
            return Modality.FINAL;
        }
        return defaultModality;
    }

    @NotNull
    public static DescriptorVisibility resolveVisibilityFromModifiers(
            @NotNull KtModifierListOwner modifierListOwner,
            @NotNull DescriptorVisibility defaultVisibility
    ) {
        return resolveVisibilityFromModifiers(modifierListOwner.getModifierList(), defaultVisibility);
    }

    public static DescriptorVisibility resolveVisibilityFromModifiers(
            @Nullable KtModifierList modifierList,
            @NotNull DescriptorVisibility defaultVisibility
    ) {
        if (modifierList == null) return defaultVisibility;
        if (modifierList.hasModifier(PRIVATE_KEYWORD)) return DescriptorVisibilities.PRIVATE;
        if (modifierList.hasModifier(PUBLIC_KEYWORD)) return DescriptorVisibilities.PUBLIC;
        if (modifierList.hasModifier(PROTECTED_KEYWORD)) return DescriptorVisibilities.PROTECTED;
        if (modifierList.hasModifier(INTERNAL_KEYWORD)) return DescriptorVisibilities.INTERNAL;
        return defaultVisibility;
    }

    public class ModifiersCheckingProcedure {
        private final BindingTrace trace;

        private ModifiersCheckingProcedure(@NotNull BindingTrace trace) {
            this.trace = trace;
        }

        public void checkParameterHasNoValOrVar(
                @NotNull KtValVarKeywordOwner parameter,
                @NotNull DiagnosticFactory1<PsiElement, KtKeywordToken> diagnosticFactory
        ) {
            PsiElement konstOrVar = parameter.getValOrVarKeyword();
            if (konstOrVar != null) {
                trace.report(diagnosticFactory.on(konstOrVar, ((KtKeywordToken) konstOrVar.getNode().getElementType())));
            }
        }

        public void checkModifiersForDeclaration(@NotNull KtDeclaration modifierListOwner, @NotNull MemberDescriptor descriptor) {
            checkNestedClassAllowed(modifierListOwner, descriptor);
            checkTypeParametersModifiers(modifierListOwner);
            checkModifierListCommon(modifierListOwner, descriptor);
            checkIllegalHeader(modifierListOwner, descriptor);
        }

        private void checkNestedClassAllowed(@NotNull KtDeclaration declaration, @NotNull DeclarationDescriptor descriptor) {
            if (!(declaration instanceof KtClassOrObject)) return;
            KtClassOrObject ktClassOrObject = (KtClassOrObject) declaration;
            if (!(descriptor instanceof ClassDescriptor)) return;
            ClassDescriptor classDescriptor = (ClassDescriptor) descriptor;
            DeclarationDescriptor containingDeclaration = descriptor.getContainingDeclaration();
            if (!(containingDeclaration instanceof ClassDescriptor)) return;
            ClassDescriptor containingClass = (ClassDescriptor) containingDeclaration;

            DetailedClassKind kind = DetailedClassKind.getClassKind(classDescriptor);

            if (kind == DetailedClassKind.ANONYMOUS_OBJECT || kind == DetailedClassKind.ENUM_ENTRY) return;

            // Local enums / objects / companion objects are handled in different checks
            if ((kind == DetailedClassKind.ENUM_CLASS || kind == DetailedClassKind.OBJECT || kind == DetailedClassKind.COMPANION_OBJECT) &&
                DescriptorUtils.isLocal(classDescriptor)) {
                return;
            }

            // Since 1.3, enum entries can contain inner classes only.
            // Companion objects are reported in ModifierCheckerCore.
            if (DescriptorUtils.isEnumEntry(containingClass) && !classDescriptor.isInner() && kind != DetailedClassKind.COMPANION_OBJECT) {
                DiagnosticFactory1<KtClassOrObject, String> diagnostic =
                        languageVersionSettings.supportsFeature(LanguageFeature.NestedClassesInEnumEntryShouldBeInner)
                        ? NESTED_CLASS_NOT_ALLOWED
                        : NESTED_CLASS_DEPRECATED;
                trace.report(diagnostic.on(ktClassOrObject, kind.withCapitalFirstLetter));
                return;
            }

            if (!classDescriptor.isInner() && (containingClass.isInner() || DescriptorUtils.isLocal(containingClass))) {
                trace.report(NESTED_CLASS_NOT_ALLOWED.on(ktClassOrObject, kind.withCapitalFirstLetter));
            }
        }

        private void checkModifierListCommon(@NotNull KtDeclaration modifierListOwner, @NotNull DeclarationDescriptor descriptor) {
            AnnotationUseSiteTargetChecker.INSTANCE.check(modifierListOwner, descriptor, trace, languageVersionSettings);
            runDeclarationCheckers(modifierListOwner, descriptor);
            annotationChecker.check(modifierListOwner, trace, descriptor);
            ModifierCheckerCore.INSTANCE.check(modifierListOwner, trace, descriptor, languageVersionSettings);
        }

        public void checkModifiersForLocalDeclaration(
                @NotNull KtDeclaration modifierListOwner,
                @NotNull DeclarationDescriptor descriptor
        ) {
            checkModifierListCommon(modifierListOwner, descriptor);
        }

        public void checkModifiersForDestructuringDeclaration(@NotNull KtDestructuringDeclaration multiDeclaration) {
            annotationChecker.check(multiDeclaration, trace, null);
            ModifierCheckerCore.INSTANCE.check(multiDeclaration, trace, null, languageVersionSettings);
            for (KtDestructuringDeclarationEntry multiEntry : multiDeclaration.getEntries()) {
                annotationChecker.check(multiEntry, trace, null);
                ModifierCheckerCore.INSTANCE.check(multiEntry, trace, null, languageVersionSettings);
                UnderscoreChecker.INSTANCE.checkNamed(multiEntry, trace, languageVersionSettings, /* allowSingleUnderscore = */ true);
            }
        }

        private void checkIllegalHeader(@NotNull KtModifierListOwner modifierListOwner, @NotNull DeclarationDescriptor descriptor) {
            // Most cases are already handled by ModifierCheckerCore, only check nested classes here
            KtModifierList modifierList = modifierListOwner.getModifierList();
            PsiElement keyword = modifierList != null ? modifierList.getModifier(HEADER_KEYWORD) : null;
            if (keyword != null &&
                descriptor instanceof ClassDescriptor && descriptor.getContainingDeclaration() instanceof ClassDescriptor) {
                trace.report(WRONG_MODIFIER_TARGET.on(keyword, KtTokens.HEADER_KEYWORD, "nested class"));
            }
            else if (keyword == null && modifierList != null) {
                keyword = modifierList.getModifier(EXPECT_KEYWORD);
                if (keyword != null &&
                    descriptor instanceof ClassDescriptor && descriptor.getContainingDeclaration() instanceof ClassDescriptor) {
                    trace.report(WRONG_MODIFIER_TARGET.on(keyword, KtTokens.EXPECT_KEYWORD, "nested class"));
                }
            }
        }

        @NotNull
        public Map<KtModifierKeywordToken, PsiElement> getTokensCorrespondingToModifiers(
                @NotNull KtModifierList modifierList,
                @NotNull Collection<KtModifierKeywordToken> possibleModifiers
        ) {
            Map<KtModifierKeywordToken, PsiElement> tokens = new HashMap<>();
            for (KtModifierKeywordToken modifier : possibleModifiers) {
                if (modifierList.hasModifier(modifier)) {
                    tokens.put(modifier, modifierList.getModifier(modifier));
                }
            }
            return tokens;
        }


        public void runDeclarationCheckers(@NotNull KtDeclaration declaration, @NotNull DeclarationDescriptor descriptor) {
            DeclarationCheckerContext context = new DeclarationCheckerContext(
                    trace, languageVersionSettings, deprecationResolver, moduleDescriptor, expectActualTracker,
                    missingSupertypesResolver
            );
            for (DeclarationChecker checker : declarationCheckers) {
                ProgressManager.checkCanceled();
                checker.check(declaration, descriptor, context);
            }
            OperatorModifierChecker.INSTANCE.check(declaration, descriptor, trace, languageVersionSettings);
            PublishedApiUsageChecker.INSTANCE.check(declaration, descriptor, trace);
            OptionalExpectationChecker.INSTANCE.check(declaration, descriptor, trace);
        }

        public void checkTypeParametersModifiers(@NotNull KtModifierListOwner modifierListOwner) {
            if (!(modifierListOwner instanceof KtTypeParameterListOwner)) return;
            List<KtTypeParameter> typeParameters = ((KtTypeParameterListOwner) modifierListOwner).getTypeParameters();
            for (KtTypeParameter typeParameter : typeParameters) {
                ModifierCheckerCore.INSTANCE.check(typeParameter, trace, null, languageVersionSettings);
            }
        }
    }

    private final AnnotationChecker annotationChecker;
    private final Iterable<DeclarationChecker> declarationCheckers;
    private final LanguageVersionSettings languageVersionSettings;
    private final ExpectActualTracker expectActualTracker;
    private final DeprecationResolver deprecationResolver;
    private final ModuleDescriptor moduleDescriptor;
    private final MissingSupertypesResolver missingSupertypesResolver;

    public ModifiersChecker(
            @NotNull AnnotationChecker annotationChecker,
            @NotNull Iterable<DeclarationChecker> declarationCheckers,
            @NotNull LanguageVersionSettings languageVersionSettings,
            @NotNull ExpectActualTracker expectActualTracker,
            @NotNull DeprecationResolver deprecationResolver,
            @NotNull ModuleDescriptor moduleDescriptor,
            @NotNull MissingSupertypesResolver missingSupertypesResolver
    ) {
        this.annotationChecker = annotationChecker;
        this.declarationCheckers = declarationCheckers;
        this.languageVersionSettings = languageVersionSettings;
        this.expectActualTracker = expectActualTracker;
        this.deprecationResolver = deprecationResolver;
        this.moduleDescriptor = moduleDescriptor;
        this.missingSupertypesResolver = missingSupertypesResolver;
    }

    @NotNull
    public ModifiersCheckingProcedure withTrace(@NotNull BindingTrace trace) {
        return new ModifiersCheckingProcedure(trace);
    }
}
