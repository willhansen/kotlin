/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import kotlin.collections.CollectionsKt;
import kotlin.text.StringsKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.backend.common.CodegenUtil;
import org.jetbrains.kotlin.builtins.StandardNames;
import org.jetbrains.kotlin.builtins.functions.BuiltInFunctionArity;
import org.jetbrains.kotlin.builtins.functions.FunctionInvokeDescriptor;
import org.jetbrains.kotlin.codegen.binding.CalculatedClosure;
import org.jetbrains.kotlin.codegen.context.CodegenContext;
import org.jetbrains.kotlin.codegen.context.FacadePartWithSourceFile;
import org.jetbrains.kotlin.codegen.context.MethodContext;
import org.jetbrains.kotlin.codegen.context.RootContext;
import org.jetbrains.kotlin.codegen.state.GenerationState;
import org.jetbrains.kotlin.codegen.state.KotlinTypeMapper;
import org.jetbrains.kotlin.config.ApiVersion;
import org.jetbrains.kotlin.config.JvmAnalysisFlags;
import org.jetbrains.kotlin.config.JvmDefaultMode;
import org.jetbrains.kotlin.config.LanguageVersionSettings;
import org.jetbrains.kotlin.descriptors.*;
import org.jetbrains.kotlin.descriptors.impl.AnonymousFunctionDescriptor;
import org.jetbrains.kotlin.descriptors.impl.LocalVariableDescriptor;
import org.jetbrains.kotlin.load.java.DescriptorsJvmAbiUtil;
import org.jetbrains.kotlin.load.java.descriptors.JavaCallableMemberDescriptor;
import org.jetbrains.kotlin.load.java.descriptors.JavaPropertyDescriptor;
import org.jetbrains.kotlin.load.kotlin.ModuleVisibilityUtilsKt;
import org.jetbrains.kotlin.metadata.jvm.deserialization.ModuleMapping;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.KtCodeFragment;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtFunction;
import org.jetbrains.kotlin.psi.KtSuperTypeListEntry;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils;
import org.jetbrains.kotlin.resolve.DescriptorUtils;
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall;
import org.jetbrains.kotlin.resolve.inline.InlineUtil;
import org.jetbrains.kotlin.resolve.jvm.annotations.JvmAnnotationUtilKt;
import org.jetbrains.kotlin.resolve.jvm.checkers.PolymorphicSignatureCallChecker;
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue;
import org.jetbrains.kotlin.resolve.scopes.receivers.TransientReceiver;
import org.jetbrains.kotlin.resolve.source.PsiSourceElement;
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedCallableMemberDescriptor;
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor;
import org.jetbrains.kotlin.types.KotlinType;
import org.jetbrains.kotlin.util.OperatorNameConventions;

import java.io.File;

import static org.jetbrains.kotlin.codegen.coroutines.CoroutineCodegenUtilKt.SUSPEND_FUNCTION_CREATE_METHOD_NAME;
import static org.jetbrains.kotlin.descriptors.ClassKind.ANNOTATION_CLASS;
import static org.jetbrains.kotlin.descriptors.ClassKind.INTERFACE;
import static org.jetbrains.kotlin.descriptors.Modality.FINAL;
import static org.jetbrains.kotlin.resolve.BindingContext.DELEGATED_PROPERTY_RESOLVED_CALL;
import static org.jetbrains.kotlin.resolve.DescriptorUtils.isCompanionObject;
import static org.jetbrains.kotlin.resolve.calls.tasks.ExplicitReceiverKind.NO_EXPLICIT_RECEIVER;
import static org.jetbrains.kotlin.resolve.jvm.annotations.JvmAnnotationUtilKt.hasJvmFieldAnnotation;

public class JvmCodegenUtil {

    private JvmCodegenUtil() {
    }

    public static boolean isNonDefaultInterfaceMember(@NotNull CallableMemberDescriptor descriptor, @NotNull JvmDefaultMode jvmDefaultMode) {
        if (!isJvmInterface(descriptor.getContainingDeclaration())) {
            return false;
        }
        if (descriptor instanceof JavaCallableMemberDescriptor) {
            return descriptor.getModality() == Modality.ABSTRACT;
        }

        return !JvmAnnotationUtilKt.isCompiledToJvmDefault(descriptor, jvmDefaultMode);
    }

    public static boolean isJvmInterface(@Nullable DeclarationDescriptor descriptor) {
        if (descriptor instanceof ClassDescriptor) {
            ClassKind kind = ((ClassDescriptor) descriptor).getKind();
            return kind == INTERFACE || kind == ANNOTATION_CLASS;
        }
        return false;
    }

    public static boolean isJvmInterface(KotlinType type) {
        return isJvmInterface(type.getConstructor().getDeclarationDescriptor());
    }

    public static boolean isConst(@NotNull CalculatedClosure closure) {
        return closure.getCapturedOuterClassDescriptor() == null &&
               closure.getCapturedReceiverFromOuterContext() == null &&
               closure.getCaptureVariables().isEmpty() &&
               !closure.isSuspend();
    }

    private static boolean isCallInsideSameClassAsFieldRepresentingProperty(
            @NotNull PropertyDescriptor descriptor,
            @NotNull CodegenContext context
    ) {
        boolean isFakeOverride = descriptor.getKind() == CallableMemberDescriptor.Kind.FAKE_OVERRIDE;
        boolean isDelegate = descriptor.getKind() == CallableMemberDescriptor.Kind.DELEGATION;

        DeclarationDescriptor containingDeclaration = descriptor.getContainingDeclaration().getOriginal();
        if (DescriptorsJvmAbiUtil.isPropertyWithBackingFieldInOuterClass(descriptor)) {
            // For property with backed field, check if the access is done in the same class containing the backed field and
            // not the class that declared the field.
            containingDeclaration = containingDeclaration.getContainingDeclaration();
        }

        return !isFakeOverride && !isDelegate &&
               (((context.hasThisDescriptor() && containingDeclaration == context.getThisDescriptor()) ||
                 ((context.getParentContext() instanceof FacadePartWithSourceFile)
                  && isWithinSameFile(((FacadePartWithSourceFile) context.getParentContext()).getSourceFile(), descriptor)))
                && context.getContextKind() != OwnerKind.DEFAULT_IMPLS);
    }

    private static boolean isWithinSameFile(
            @Nullable KtFile callerFile,
            @NotNull CallableMemberDescriptor descriptor
    ) {
        DeclarationDescriptor containingDeclaration = descriptor.getContainingDeclaration().getOriginal();
        if (containingDeclaration instanceof PackageFragmentDescriptor) {
            PsiElement calleeElement = DescriptorToSourceUtils.descriptorToDeclaration(descriptor);
            PsiFile calleeFile = calleeElement != null ? calleeElement.getContainingFile() : null;
            return callerFile != null && callerFile != SourceFile.NO_SOURCE_FILE && calleeFile == callerFile;

        }
        return false;
    }

    public static boolean isCallInsideSameModuleAsDeclared(
            @NotNull CallableMemberDescriptor declarationDescriptor,
            @NotNull CodegenContext context,
            @Nullable File outDirectory
    ) {
        if (context instanceof RootContext) {
            return true;
        }
        DeclarationDescriptor contextDescriptor = context.getContextDescriptor();

        CallableMemberDescriptor directMember = getDirectMember(declarationDescriptor);
        if (directMember instanceof DeserializedCallableMemberDescriptor) {
            return ModuleVisibilityUtilsKt.isContainedByCompiledPartOfOurModule(directMember, outDirectory);
        }
        else {
            return DescriptorUtils.areInSameModule(directMember, contextDescriptor);
        }
    }

    public static boolean isConstOrHasJvmFieldAnnotation(@NotNull PropertyDescriptor propertyDescriptor) {
        return propertyDescriptor.isConst() || hasJvmFieldAnnotation(propertyDescriptor);
    }

    public static String getCompanionObjectAccessorName(@NotNull ClassDescriptor companionObjectDescriptor) {
        return "access$" + companionObjectDescriptor.getName();
    }

    public static boolean couldUseDirectAccessToProperty(
            @NotNull PropertyDescriptor property,
            boolean forGetter,
            boolean isDelegated,
            @NotNull MethodContext contextBeforeInline,
            boolean shouldInlineConstVals
    ) {
        if (shouldInlineConstVals && property.isConst()) return true;

        if (KotlinTypeMapper.isAccessor(property)) return false;

        CodegenContext context = contextBeforeInline.getFirstCrossInlineOrNonInlineContext();
        // Inline functions can't use direct access because a field may not be visible at the call site
        if (context.isInlineMethodContext()) {
            return false;
        }

        if (!isCallInsideSameClassAsFieldRepresentingProperty(property, context)) {
            DeclarationDescriptor propertyOwner = property.getContainingDeclaration();
            boolean isAnnotationValue;
            if (propertyOwner instanceof ClassDescriptor) {
                isAnnotationValue = ((ClassDescriptor) propertyOwner).getKind() == ANNOTATION_CLASS;
            } else {
                isAnnotationValue = false;
            }

            if (isAnnotationValue || !isDebuggerContext(context)) {
                // Unless we are ekonstuating expression in debugger context, only properties of the same class can be directly accessed
                return false;
            }
            else {
                // In debugger we want to access through accessors if they are generated

                // Non default accessors must always be generated
                for (PropertyAccessorDescriptor accessorDescriptor : property.getAccessors()) {
                    if (!accessorDescriptor.isDefault()) {
                        if (forGetter == accessorDescriptor instanceof PropertyGetterDescriptor) {
                            return false;
                        }
                    }
                }

                // If property overrides something, accessors must be generated too
                if (!property.getOverriddenDescriptors().isEmpty()) return false;
            }
        }

        // Delegated and extension properties have no backing fields
        if (isDelegated || property.getExtensionReceiverParameter() != null) return false;

        PropertyAccessorDescriptor accessor = forGetter ? property.getGetter() : property.getSetter();

        // If there's no accessor declared we can use direct access
        if (accessor == null) return true;

        // If the accessor is non-default (i.e. it has some code) we should call that accessor and not use direct access
        if (DescriptorPsiUtilsKt.hasBody(accessor)) return false;

        // If the accessor is private or final, it can't be overridden in the subclass and thus we can use direct access
        return DescriptorVisibilities.isPrivate(accessor.getVisibility()) || accessor.getModality() == FINAL;
    }

    public static boolean isDebuggerContext(@NotNull CodegenContext context) {
        PsiFile file = null;

        DeclarationDescriptor contextDescriptor = context.getContextDescriptor();
        if (contextDescriptor instanceof DeclarationDescriptorWithSource) {
            SourceElement sourceElement = ((DeclarationDescriptorWithSource) contextDescriptor).getSource();
            if (sourceElement instanceof PsiSourceElement) {
                PsiElement psi = ((PsiSourceElement) sourceElement).getPsi();
                if (psi != null) {
                    file = psi.getContainingFile();
                }
            }
        }

        return file instanceof KtCodeFragment;
    }

    @Nullable
    public static ClassDescriptor getDispatchReceiverParameterForConstructorCall(
            @NotNull ConstructorDescriptor descriptor,
            @Nullable CalculatedClosure closure
    ) {
        //for compilation against sources
        if (closure != null) {
            return closure.getCapturedOuterClassDescriptor();
        }

        //for compilation against binaries
        //TODO: It's best to use this code also for compilation against sources
        // but sometimes structures that have dispatchReceiver (bug?) mapped to static classes
        ReceiverParameterDescriptor dispatchReceiver = descriptor.getDispatchReceiverParameter();
        if (dispatchReceiver != null) {
            ClassDescriptor expectedThisClass = (ClassDescriptor) dispatchReceiver.getContainingDeclaration();
            if (!expectedThisClass.getKind().isSingleton()) {
                return expectedThisClass;
            }
        }

        return null;
    }

    @NotNull
    public static CallableMemberDescriptor getDirectMember(@NotNull CallableMemberDescriptor descriptor) {
        return DescriptorUtils.getDirectMember(descriptor);
    }

    public static boolean isArgumentWhichWillBeInlined(@NotNull BindingContext bindingContext, @NotNull DeclarationDescriptor descriptor) {
        PsiElement declaration = DescriptorToSourceUtils.descriptorToDeclaration(descriptor);
        return InlineUtil.canBeInlineArgument(declaration) &&
               InlineUtil.isInlinedArgument((KtFunction) declaration, bindingContext, false);
    }

    @NotNull
    public static String getModuleName(ModuleDescriptor module) {
        Name name = module.getStableName();
        if (name == null) {
            // Defensive fallback to possibly unstable name, to not fail with exception
            return StringsKt.removeSurrounding(module.getName().asString(), "<", ">");
        } else {
            return StringsKt.removeSurrounding(name.asString(), "<", ">");
        }
    }

    @NotNull
    public static String getMappingFileName(@NotNull String moduleName) {
        return "META-INF/" + moduleName + "." + ModuleMapping.MAPPING_FILE_EXT;
    }

    public static boolean isInlinedJavaConstProperty(VariableDescriptor descriptor) {
        return descriptor instanceof JavaPropertyDescriptor && descriptor.isConst();
    }

    @Nullable
    public static KotlinType getPropertyDelegateType(
            @NotNull VariableDescriptorWithAccessors descriptor,
            @NotNull BindingContext bindingContext
    ) {
        VariableAccessorDescriptor getter = descriptor.getGetter();
        if (getter != null) {
            ResolvedCall<FunctionDescriptor> call = bindingContext.get(DELEGATED_PROPERTY_RESOLVED_CALL, getter);
            if (call != null) {
                assert call.getExplicitReceiverKind() != NO_EXPLICIT_RECEIVER : "No explicit receiver for call:" + call;
                ReceiverValue extensionReceiver = call.getExtensionReceiver();
                if (extensionReceiver != null) return extensionReceiver.getType();

                ReceiverValue dispatchReceiver = call.getDispatchReceiver();
                if (dispatchReceiver != null) return dispatchReceiver.getType();

                return null;
            }
        }
        return null;
    }

    public static boolean isDelegatedLocalVariable(@NotNull DeclarationDescriptor descriptor) {
        return descriptor instanceof LocalVariableDescriptor && ((LocalVariableDescriptor) descriptor).isDelegated();
    }

    @Nullable
    public static ReceiverValue getBoundCallableReferenceReceiver(@NotNull ResolvedCall<?> resolvedCall) {
        CallableDescriptor descriptor = resolvedCall.getResultingDescriptor();
        if (descriptor.getExtensionReceiverParameter() == null && descriptor.getDispatchReceiverParameter() == null) return null;

        ReceiverValue dispatchReceiver = resolvedCall.getDispatchReceiver();
        ReceiverValue extensionReceiver = resolvedCall.getExtensionReceiver();
        assert dispatchReceiver == null || extensionReceiver == null : "Cannot generate reference with both receivers: " + descriptor;
        ReceiverValue receiver = dispatchReceiver != null ? dispatchReceiver : extensionReceiver;

        if (receiver instanceof TransientReceiver) return null;

        return receiver;
    }

    public static boolean isCompanionObjectInInterfaceNotIntrinsic(@NotNull DeclarationDescriptor companionObject) {
        return isCompanionObject(companionObject) &&
               isJvmInterface(companionObject.getContainingDeclaration()) &&
               !DescriptorsJvmAbiUtil.isMappedIntrinsicCompanionObject((ClassDescriptor) companionObject);
    }

    public static boolean isNonIntrinsicPrivateCompanionObjectInInterface(@NotNull DeclarationDescriptorWithVisibility companionObject) {
        return isCompanionObjectInInterfaceNotIntrinsic(companionObject) &&
               DescriptorVisibilities.isPrivate(companionObject.getVisibility());
    }

    public static boolean isDeclarationOfBigArityFunctionInvoke(@Nullable DeclarationDescriptor descriptor) {
        return descriptor instanceof FunctionInvokeDescriptor && ((FunctionInvokeDescriptor) descriptor).hasBigArity();
    }

    public static boolean isDeclarationOfBigArityCreateCoroutineMethod(@Nullable DeclarationDescriptor descriptor) {
        return descriptor instanceof SimpleFunctionDescriptor && descriptor.getName().asString().equals(SUSPEND_FUNCTION_CREATE_METHOD_NAME) &&
               ((SimpleFunctionDescriptor) descriptor).getValueParameters().size() >= BuiltInFunctionArity.BIG_ARITY - 1 &&
               descriptor.getContainingDeclaration() instanceof AnonymousFunctionDescriptor && ((AnonymousFunctionDescriptor) descriptor.getContainingDeclaration()).isSuspend();
    }

    public static boolean isOverrideOfBigArityFunctionInvoke(@Nullable DeclarationDescriptor descriptor) {
        return descriptor instanceof FunctionDescriptor &&
               descriptor.getName().equals(OperatorNameConventions.INVOKE) &&
               CollectionsKt.any(
                       DescriptorUtils.getAllOverriddenDeclarations((FunctionDescriptor) descriptor),
                       JvmCodegenUtil::isDeclarationOfBigArityFunctionInvoke
               );
    }

    @Nullable
    public static ClassDescriptor getSuperClass(
            @NotNull KtSuperTypeListEntry specifier,
            @NotNull GenerationState state,
            @NotNull BindingContext bindingContext
    ) {
        ClassDescriptor superClass = CodegenUtil.getSuperClassBySuperTypeListEntry(specifier, bindingContext);

        assert superClass != null || state.getClassBuilderMode() == ClassBuilderMode.LIGHT_CLASSES
                : "ClassDescriptor should not be null:" + specifier.getText();
        return superClass;
    }

    public static boolean isPolymorphicSignature(@NotNull FunctionDescriptor descriptor) {
        return descriptor.getAnnotations().hasAnnotation(PolymorphicSignatureCallChecker.polymorphicSignatureFqName);
    }

    @NotNull
    public static String sanitizeNameIfNeeded(@NotNull String name, @NotNull LanguageVersionSettings languageVersionSettings) {
        if (languageVersionSettings.getFlag(JvmAnalysisFlags.getSanitizeParentheses())) {
            return name.replace("(", "$_").replace(")", "$_");
        }

        return name;
    }

    // Before metadata version 1.1.16 we did not generate equals-impl0 methods correctly.
    // The method is still present on all inline classes, but the implementation always throws
    // a NullPointerException.
    public static boolean typeHasSpecializedInlineClassEquality(@NotNull KotlinType type, @NotNull GenerationState state) {
        ClassifierDescriptor descriptor = type.getConstructor().getDeclarationDescriptor();
        if (!(descriptor instanceof DeserializedClassDescriptor))
            return true;

        DeserializedClassDescriptor classDescriptor = (DeserializedClassDescriptor) descriptor;

        // The Result class is the only inline class in the standard library without special rules for equality.
        // We only call Result.equals-impl0 if we are compiling for Kotlin 1.4 or later. Otherwise, the code
        // might well be running against an older version of the standard library.
        if (DescriptorUtils.getFqNameSafe(classDescriptor).equals(StandardNames.RESULT_FQ_NAME)) {
            return state.getLanguageVersionSettings().getApiVersion().compareTo(ApiVersion.KOTLIN_1_4) >= 0;
        } else {
            return ((DeserializedClassDescriptor) descriptor).getMetadataVersion().isAtLeast(1, 1, 16);
        }
    }

    public static boolean isInSamePackage(DeclarationDescriptor descriptor1, DeclarationDescriptor descriptor2) {
        PackageFragmentDescriptor package1 = DescriptorUtils.getParentOfType(descriptor1, PackageFragmentDescriptor.class, false);
        PackageFragmentDescriptor package2 = DescriptorUtils.getParentOfType(descriptor2, PackageFragmentDescriptor.class, false);

        return package1 != null && package2 != null &&
               package1.getFqName().equals(package2.getFqName());
    }

    // Used mainly for debugging purposes.
    @SuppressWarnings("unused")
    public static String dumpContextHierarchy(CodegenContext context) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        for (CodegenContext current = context; current != null; current = current.getParentContext(), ++i) {
            result.append(i).append(": ").append(current).append('\n');
        }
        return result.toString();
    }
}
