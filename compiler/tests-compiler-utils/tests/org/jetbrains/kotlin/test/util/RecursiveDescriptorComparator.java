/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.util;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import kotlin.Unit;
import kotlin.text.StringsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.builtins.StandardNames;
import org.jetbrains.kotlin.contracts.description.*;
import org.jetbrains.kotlin.descriptors.*;
import org.jetbrains.kotlin.descriptors.impl.SubpackagesScope;
import org.jetbrains.kotlin.jvm.compiler.ExpectedLoadErrorsUtil;
import org.jetbrains.kotlin.load.java.descriptors.JavaClassDescriptor;
import org.jetbrains.kotlin.load.java.descriptors.JavaMethodDescriptor;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.platform.TargetPlatformKt;
import org.jetbrains.kotlin.renderer.*;
import org.jetbrains.kotlin.resolve.DescriptorUtils;
import org.jetbrains.kotlin.resolve.MemberComparator;
import org.jetbrains.kotlin.resolve.scopes.ChainedMemberScope;
import org.jetbrains.kotlin.resolve.scopes.MemberScope;
import org.jetbrains.kotlin.test.Assertions;
import org.jetbrains.kotlin.utils.Printer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.jetbrains.kotlin.resolve.DescriptorUtils.isEnumEntry;
import static org.jetbrains.kotlin.test.util.DescriptorValidator.ValidationVisitor.errorTypesForbidden;

public class RecursiveDescriptorComparator {

    private static final DescriptorRenderer DEFAULT_RENDERER = DescriptorRenderer.Companion.withOptions(
            options -> {
                options.setWithDefinedIn(false);
                options.setExcludedAnnotationClasses(Collections.singleton(new FqName(ExpectedLoadErrorsUtil.ANNOTATION_CLASS_NAME)));
                options.setOverrideRenderingPolicy(OverrideRenderingPolicy.RENDER_OPEN_OVERRIDE);
                options.setIncludePropertyConstant(true);
                options.setClassifierNamePolicy(ClassifierNamePolicy.FULLY_QUALIFIED.INSTANCE);
                options.setVerbose(true);
                options.setAnnotationArgumentsRenderingPolicy(AnnotationArgumentsRenderingPolicy.UNLESS_EMPTY);
                options.setModifiers(DescriptorRendererModifier.ALL);
                return Unit.INSTANCE;
            }
    );

    public static final Configuration DONT_INCLUDE_METHODS_OF_OBJECT = new Configuration(false, false, false, false,
                                                                                         false, descriptor -> true, errorTypesForbidden(), DEFAULT_RENDERER);
    public static final Configuration RECURSIVE = new Configuration(false, false, true, false,
                                                                    false, descriptor -> true, errorTypesForbidden(), DEFAULT_RENDERER);

    public static final Configuration RECURSIVE_ALL = new Configuration(true, true, true, false,
                                                                        true, descriptor -> true, errorTypesForbidden(), DEFAULT_RENDERER);

    public static final Predicate<DeclarationDescriptor> SKIP_BUILT_INS_PACKAGES = descriptor -> {
        if (descriptor instanceof PackageViewDescriptor) {
            return !StandardNames.BUILT_INS_PACKAGE_FQ_NAME.equals(((PackageViewDescriptor) descriptor).getFqName());
        }
        return true;
    };

    private static final ImmutableSet<String> KOTLIN_ANY_METHOD_NAMES = ImmutableSet.of("equals", "hashCode", "toString");

    private final Configuration conf;

    public RecursiveDescriptorComparator(@NotNull Configuration conf) {
        this.conf = conf;
    }

    public String serializeRecursively(@NotNull DeclarationDescriptor declarationDescriptor) {
        StringBuilder result = new StringBuilder();
        appendDeclarationRecursively(declarationDescriptor, DescriptorUtils.getContainingModule(declarationDescriptor),
                                     new Printer(result, 1), true);
        return result.toString();
    }

    private void appendDeclarationRecursively(
            @NotNull DeclarationDescriptor descriptor,
            @NotNull ModuleDescriptor module,
            @NotNull Printer printer,
            boolean topLevel
    ) {
        if (!isFromModule(descriptor, module)) return;

        boolean isEnumEntry = isEnumEntry(descriptor);
        boolean isClassOrPackage =
                (descriptor instanceof ClassOrPackageFragmentDescriptor || descriptor instanceof PackageViewDescriptor) && !isEnumEntry;

        StringBuilder content = new StringBuilder();
        if (isClassOrPackage) {
            Printer child = new Printer(content, printer);

            if (!topLevel) {
                child.pushIndent();
            }

            if (descriptor instanceof ClassDescriptor) {
                ClassDescriptor klass = (ClassDescriptor) descriptor;
                appendSubDescriptors(descriptor, module, klass.getDefaultType().getMemberScope(), klass.getConstructors(), child);
                MemberScope staticScope = klass.getStaticScope();
                if (!DescriptorUtils.getAllDescriptors(staticScope).isEmpty()) {
                    child.println();
                    child.println("// Static members");
                    appendSubDescriptors(descriptor, module, staticScope, Collections.emptyList(), child);
                }
            }
            else if (descriptor instanceof PackageFragmentDescriptor) {
                appendSubDescriptors(descriptor, module,
                                     ((PackageFragmentDescriptor) descriptor).getMemberScope(),
                                     Collections.emptyList(), child);
            }
            else if (descriptor instanceof PackageViewDescriptor) {
                appendSubDescriptors(descriptor, module,
                                     getPackageScopeInModule((PackageViewDescriptor) descriptor, module),
                                     Collections.emptyList(), child);
            }

            if (!topLevel) {
                if (child.isEmpty() && (
                        descriptor instanceof PackageFragmentDescriptor || descriptor instanceof PackageViewDescriptor
                )) {
                    return;
                }

                printer.println();
            }
        }

        printDescriptor(descriptor, printer);

        if (isClassOrPackage) {
            if (!topLevel) {
                printer.printlnWithNoIndent(" {");
                printer.printWithNoIndent(content);
                printer.println("}");
            }
            else {
                printer.println();
                printer.println();
                printer.printWithNoIndent(StringsKt.trimStart(content, Printer.LINE_SEPARATOR.toCharArray()));
            }
        }
        else if (conf.checkPropertyAccessors && descriptor instanceof PropertyDescriptor) {
            printer.printlnWithNoIndent();
            printer.pushIndent();
            PropertyDescriptor propertyDescriptor = (PropertyDescriptor) descriptor;
            PropertyGetterDescriptor getter = propertyDescriptor.getGetter();
            if (getter != null) {
                printer.println(conf.renderer.render(getter));
            }

            PropertySetterDescriptor setter = propertyDescriptor.getSetter();
            if (setter != null) {
                printer.println(conf.renderer.render(setter));
            }

            printer.popIndent();
        }
        else {
            printer.printlnWithNoIndent();
        }

        if (isEnumEntry) {
            printer.println();
        }
    }

    private void printDescriptor(
            @NotNull DeclarationDescriptor descriptor,
            @NotNull Printer printer
    ) {
        boolean isPrimaryConstructor = conf.checkPrimaryConstructors &&
                                       descriptor instanceof ConstructorDescriptor && ((ConstructorDescriptor) descriptor).isPrimary();

        boolean isRecord = descriptor instanceof JavaClassDescriptor && ((JavaClassDescriptor) descriptor).isRecord();
        boolean isRecordComponent = descriptor instanceof JavaMethodDescriptor && ((JavaMethodDescriptor) descriptor).isForRecordComponent();

        StringBuilder prefix = new StringBuilder();

        if (isPrimaryConstructor) {
            prefix.append("/*primary*/ ");
        }

        if (isRecord) {
            prefix.append("/*record*/ ");
        }

        if (isRecordComponent) {
            prefix.append("/*record component*/ ");
        }

        printer.print(prefix.toString(), conf.renderer.render(descriptor));

        if (descriptor instanceof FunctionDescriptor && conf.checkFunctionContracts) {
            printEffectsIfAny((FunctionDescriptor) descriptor, printer);
        }
    }

    private static void printEffectsIfAny(FunctionDescriptor functionDescriptor, Printer printer) {
        AbstractContractProvider contractProvider = functionDescriptor.getUserData(ContractProviderKey.INSTANCE);
        if (contractProvider == null) return;

        ContractDescription contractDescription = contractProvider.getContractDescription();
        if (contractDescription == null || contractDescription.getEffects().isEmpty()) return;

        printer.println();
        printer.pushIndent();
        for (EffectDeclaration effect : contractDescription.getEffects()) {
            StringBuilder sb = new StringBuilder();
            ContractDescriptionRenderer renderer = new ContractDescriptionRenderer(sb);
            effect.accept(renderer, Unit.INSTANCE);
            printer.println(sb.toString());
        }
        printer.popIndent();
    }

    @NotNull
    private MemberScope getPackageScopeInModule(@NotNull PackageViewDescriptor descriptor, @NotNull ModuleDescriptor module) {
        // See LazyPackageViewDescriptorImpl#memberScope
        List<MemberScope> scopes = new ArrayList<>();
        for (PackageFragmentDescriptor fragment : descriptor.getFragments()) {
            if (isFromModule(fragment, module)) {
                scopes.add(fragment.getMemberScope());
            }
        }
        scopes.add(new SubpackagesScope(module, descriptor.getFqName()));
        return ChainedMemberScope.Companion.create("test", scopes);
    }

    private boolean isFromModule(@NotNull DeclarationDescriptor descriptor, @NotNull ModuleDescriptor module) {
        if (conf.renderDeclarationsFromOtherModules) return true;

        if (descriptor instanceof PackageViewDescriptor) {
            // PackageViewDescriptor does not belong to any module, so we check if one of its containing fragments is in our module
            for (PackageFragmentDescriptor fragment : ((PackageViewDescriptor) descriptor).getFragments()) {
                if (module.equals(DescriptorUtils.getContainingModule(fragment))) return true;
            }
        }

        // 'expected' declarations do not belong to the platform-specific module, even though they participate in the analysis
        if (descriptor instanceof MemberDescriptor && ((MemberDescriptor) descriptor).isExpect() &&
            !TargetPlatformKt.isCommon(module.getPlatform())) return false;

        return module.equals(DescriptorUtils.getContainingModule(descriptor));
    }

    private boolean shouldSkip(@NotNull DeclarationDescriptor subDescriptor) {
        boolean isFunctionFromAny = subDescriptor.getContainingDeclaration() instanceof ClassDescriptor
                                    && subDescriptor instanceof FunctionDescriptor
                                    && KOTLIN_ANY_METHOD_NAMES.contains(subDescriptor.getName().asString());
        return (isFunctionFromAny && !conf.includeMethodsOfKotlinAny) || !conf.recursiveFilter.test(subDescriptor);
    }

    private void appendSubDescriptors(
            @NotNull DeclarationDescriptor descriptor,
            @NotNull ModuleDescriptor module,
            @NotNull MemberScope memberScope,
            @NotNull Collection<? extends DeclarationDescriptor> extraSubDescriptors,
            @NotNull Printer printer
    ) {
        if (!isFromModule(descriptor, module)) return;

        List<DeclarationDescriptor> subDescriptors = Lists.newArrayList();

        subDescriptors.addAll(DescriptorUtils.getAllDescriptors(memberScope));
        subDescriptors.addAll(extraSubDescriptors);

        subDescriptors.sort(MemberComparator.INSTANCE);

        for (DeclarationDescriptor subDescriptor : subDescriptors) {
            if (!shouldSkip(subDescriptor)) {
                appendDeclarationRecursively(subDescriptor, module, printer, false);
            }
        }
    }

    private static void compareDescriptorWithFile(
            @NotNull DeclarationDescriptor actual,
            @NotNull Configuration configuration,
            @NotNull File txtFile,
            @NotNull Assertions assertions
    ) {
        doCompareDescriptors(null, actual, configuration, txtFile, assertions);
    }

    public static void compareDescriptors(
            @NotNull DeclarationDescriptor expected,
            @NotNull DeclarationDescriptor actual,
            @NotNull Configuration configuration,
            @Nullable File txtFile,
            @NotNull Assertions assertions
    ) {
        if (expected == actual) {
            throw new IllegalArgumentException("Don't invoke this method with expected == actual." +
                                               "Invoke compareDescriptorWithFile() instead.");
        }
        doCompareDescriptors(expected, actual, configuration, txtFile, assertions);
    }

    public static void konstidateAndCompareDescriptorWithFile(
            @NotNull DeclarationDescriptor actual,
            @NotNull Configuration configuration,
            @NotNull File txtFile,
            @NotNull Assertions assertions
    ) {
        DescriptorValidator.konstidate(configuration.konstidationStrategy, actual);
        compareDescriptorWithFile(actual, configuration, txtFile, assertions);
    }

    public static void konstidateAndCompareDescriptors(
            @NotNull DeclarationDescriptor expected,
            @NotNull DeclarationDescriptor actual,
            @NotNull Configuration configuration,
            @Nullable File txtFile,
            @NotNull Assertions assertions
    ) {
        DescriptorValidator.konstidate(configuration.konstidationStrategy, expected);
        DescriptorValidator.konstidate(configuration.konstidationStrategy, actual);
        compareDescriptors(expected, actual, configuration, txtFile, assertions);
    }

    private static void doCompareDescriptors(
            @Nullable DeclarationDescriptor expected,
            @NotNull DeclarationDescriptor actual,
            @NotNull Configuration configuration,
            @Nullable File txtFile,
            @NotNull Assertions assertions
    ) {
        RecursiveDescriptorComparator comparator = new RecursiveDescriptorComparator(configuration);

        String actualSerialized = comparator.serializeRecursively(actual);

        if (expected != null) {
            String expectedSerialized = comparator.serializeRecursively(expected);

            assertions.assertEquals(expectedSerialized, actualSerialized, () -> "Expected and actual descriptors differ");
        }

        if (txtFile != null) {
            assertions.assertEqualsToFile(txtFile, actualSerialized, (s) -> s);
        }
    }

    public static class Configuration {
        private final boolean checkPrimaryConstructors;
        private final boolean checkPropertyAccessors;
        private final boolean includeMethodsOfKotlinAny;
        private final boolean renderDeclarationsFromOtherModules;
        private final boolean checkFunctionContracts;
        private final Predicate<DeclarationDescriptor> recursiveFilter;
        private final DescriptorRenderer renderer;
        private final DescriptorValidator.ValidationVisitor konstidationStrategy;

        public Configuration(
                boolean checkPrimaryConstructors,
                boolean checkPropertyAccessors,
                boolean includeMethodsOfKotlinAny,
                boolean renderDeclarationsFromOtherModules,
                boolean checkFunctionContracts,
                Predicate<DeclarationDescriptor> recursiveFilter,
                DescriptorValidator.ValidationVisitor konstidationStrategy,
                DescriptorRenderer renderer
        ) {
            this.checkPrimaryConstructors = checkPrimaryConstructors;
            this.checkPropertyAccessors = checkPropertyAccessors;
            this.includeMethodsOfKotlinAny = includeMethodsOfKotlinAny;
            this.renderDeclarationsFromOtherModules = renderDeclarationsFromOtherModules;
            this.checkFunctionContracts = checkFunctionContracts;
            this.recursiveFilter = recursiveFilter;
            this.konstidationStrategy = konstidationStrategy;
            this.renderer = rendererWithPropertyAccessors(renderer, checkPropertyAccessors);
        }

        public Configuration filterRecursion(@NotNull Predicate<DeclarationDescriptor> stepIntoFilter) {
            return new Configuration(checkPrimaryConstructors, checkPropertyAccessors, includeMethodsOfKotlinAny,
                                     renderDeclarationsFromOtherModules, checkFunctionContracts, stepIntoFilter,
                                     konstidationStrategy.withStepIntoFilter(stepIntoFilter), renderer);
        }

        public Configuration checkPrimaryConstructors(boolean checkPrimaryConstructors) {
            return new Configuration(checkPrimaryConstructors, checkPropertyAccessors, includeMethodsOfKotlinAny,
                                     renderDeclarationsFromOtherModules, checkFunctionContracts, recursiveFilter, konstidationStrategy, renderer);
        }

        public Configuration checkPropertyAccessors(boolean checkPropertyAccessors) {
            return new Configuration(
                    checkPrimaryConstructors, checkPropertyAccessors, includeMethodsOfKotlinAny, renderDeclarationsFromOtherModules,
                    checkFunctionContracts, recursiveFilter, konstidationStrategy,
                    rendererWithPropertyAccessors(renderer, checkPropertyAccessors)
            );
        }

        public Configuration checkFunctionContracts(boolean checkFunctionContracts) {
            return new Configuration(checkPrimaryConstructors, checkPropertyAccessors, includeMethodsOfKotlinAny,
                                     renderDeclarationsFromOtherModules, checkFunctionContracts, recursiveFilter, konstidationStrategy, renderer);
        }

        public Configuration includeMethodsOfKotlinAny(boolean includeMethodsOfKotlinAny) {
            return new Configuration(checkPrimaryConstructors, checkPropertyAccessors, includeMethodsOfKotlinAny,
                                     renderDeclarationsFromOtherModules, checkFunctionContracts, recursiveFilter, konstidationStrategy, renderer);
        }

        public Configuration renderDeclarationsFromOtherModules(boolean renderDeclarationsFromOtherModules) {
            return new Configuration(checkPrimaryConstructors, checkPropertyAccessors, includeMethodsOfKotlinAny,
                                     renderDeclarationsFromOtherModules, checkFunctionContracts, recursiveFilter, konstidationStrategy, renderer);
        }

        public Configuration withValidationStrategy(@NotNull DescriptorValidator.ValidationVisitor konstidationStrategy) {
            return new Configuration(checkPrimaryConstructors, checkPropertyAccessors, includeMethodsOfKotlinAny,
                                     renderDeclarationsFromOtherModules, checkFunctionContracts, recursiveFilter, konstidationStrategy, renderer);
        }

        public Configuration withRenderer(@NotNull DescriptorRenderer renderer) {
            return new Configuration(checkPrimaryConstructors, checkPropertyAccessors, includeMethodsOfKotlinAny,
                                     renderDeclarationsFromOtherModules, checkFunctionContracts, recursiveFilter, konstidationStrategy, renderer);
        }

        public Configuration withRendererOptions(@NotNull Consumer<DescriptorRendererOptions> configure) {
            return new Configuration(
                    checkPrimaryConstructors, checkPropertyAccessors, includeMethodsOfKotlinAny,
                    renderDeclarationsFromOtherModules, checkFunctionContracts, recursiveFilter, konstidationStrategy,
                    newRenderer(renderer, configure));
        }

        @NotNull
        private static DescriptorRenderer rendererWithPropertyAccessors(
                @NotNull DescriptorRenderer renderer, boolean checkPropertyAccessors
        ) {
            return newRenderer(renderer, options ->
                    options.setPropertyAccessorRenderingPolicy(
                            checkPropertyAccessors ? PropertyAccessorRenderingPolicy.DEBUG : PropertyAccessorRenderingPolicy.NONE
                    )
            );
        }

        @NotNull
        private static DescriptorRenderer newRenderer(
                @NotNull DescriptorRenderer renderer, @NotNull Consumer<DescriptorRendererOptions> configure
        ) {
            return renderer.withOptions(options -> {
                configure.accept(options);
                return Unit.INSTANCE;
            });
        }
    }
}
