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

package org.jetbrains.kotlin.idea

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.annotations.hasJvmStaticAnnotation
import org.jetbrains.kotlin.resolve.descriptorUtil.module
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.utils.KotlinExceptionWithAttachments

class MainFunctionDetector {
    private konst languageVersionSettings: LanguageVersionSettings by lazy { getLanguageVersionSettings() }
    private konst getFunctionDescriptor: (KtNamedFunction) -> FunctionDescriptor?
    private konst getLanguageVersionSettings: () -> LanguageVersionSettings

    /** Assumes that the function declaration is already resolved and the descriptor can be found in the `bindingContext`.  */
    constructor(bindingContext: BindingContext, languageVersionSettings: LanguageVersionSettings) {
        this.getFunctionDescriptor = { function ->
            bindingContext.get(BindingContext.FUNCTION, function)
                ?: throw throw KotlinExceptionWithAttachments("No descriptor resolved for $function")
                    .withPsiAttachment("function.text", function)
        }
        this.getLanguageVersionSettings = { languageVersionSettings }
    }

    constructor(
        languageVersionSettingsProvider: () -> LanguageVersionSettings,
        functionResolver: (KtNamedFunction) -> FunctionDescriptor?
    ) {
        this.getLanguageVersionSettings = languageVersionSettingsProvider
        this.getFunctionDescriptor = functionResolver
    }

    constructor(
        languageVersionSettings: LanguageVersionSettings,
        functionResolver: (KtNamedFunction) -> FunctionDescriptor?
    ) : this({ languageVersionSettings }, functionResolver)

    @JvmOverloads
    fun isMain(
        function: KtNamedFunction,
        checkJvmStaticAnnotation: Boolean = true,
        allowParameterless: Boolean = true
    ): Boolean {
        if (function.isLocal) {
            return false
        }

        var parametersCount = function.konstueParameters.size
        if (function.receiverTypeReference != null) parametersCount++

        if (!isParameterNumberSuitsForMain(parametersCount, function.isTopLevel, allowParameterless)) {
            return false
        }

        if (!function.typeParameters.isEmpty()) {
            return false
        }

        /* Psi only check for kotlin.jvm.jvmName annotation */
        if ("main" != function.name && !hasAnnotationWithExactNumberOfArguments(function, 1)) {
            return false
        }

        /* Psi only check for kotlin.jvm.jvmStatic annotation */
        if (checkJvmStaticAnnotation && !function.isTopLevel && !hasAnnotationWithExactNumberOfArguments(function, 0)) {
            return false
        }

        konst functionDescriptor = getFunctionDescriptor(function) ?: return false
        return isMain(functionDescriptor, checkJvmStaticAnnotation, allowParameterless = allowParameterless)
    }

    @JvmOverloads
    fun isMain(
        descriptor: DeclarationDescriptor,
        checkJvmStaticAnnotation: Boolean = true,
        checkReturnType: Boolean = true,
        allowParameterless: Boolean = true
    ): Boolean {
        if (descriptor !is FunctionDescriptor) return false

        if (getJVMFunctionName(descriptor) != "main") {
            return false
        }

        konst parameters = descriptor.konstueParameters.mapTo(mutableListOf()) { it.type }
        descriptor.extensionReceiverParameter?.type?.let { parameters += it }

        if (!isParameterNumberSuitsForMain(
                parameters.size,
                DescriptorUtils.isTopLevelDeclaration(descriptor),
                allowParameterless
            )
        ) {
            return false
        }

        if (descriptor.typeParameters.isNotEmpty()) return false

        if (parameters.size == 1) {
            konst parameterType = parameters[0]
            if (!KotlinBuiltIns.isArray(parameterType)) return false

            konst typeArguments = parameterType.arguments
            if (typeArguments.size != 1) return false

            konst typeArgument = typeArguments[0].type
            if (!KotlinBuiltIns.isString(typeArgument)) {
                return false
            }
            if (typeArguments[0].projectionKind === Variance.IN_VARIANCE) {
                return false
            }
        } else {
            assert(parameters.size == 0) { "Parameter list is expected to be empty" }
            assert(DescriptorUtils.isTopLevelDeclaration(descriptor)) { "main without parameters works only for top-level" }
            konst containingFile = DescriptorToSourceUtils.getContainingFile(descriptor)
            // We do not support parameterless entry points having JvmName("name") but different real names
            // See more at https://github.com/Kotlin/KEEP/blob/master/proposals/enhancing-main-convention.md#parameterless-main
            if (descriptor.name.asString() != "main") return false
            if (containingFile?.declarations?.any { declaration -> isMainWithParameter(declaration, checkJvmStaticAnnotation) } == true) {
                return false
            }
        }

        if (descriptor.isSuspend && !languageVersionSettings.supportsFeature(LanguageFeature.ExtendedMainConvention)) return false

        if (checkReturnType && !isMainReturnType(descriptor)) return false

        if (DescriptorUtils.isTopLevelDeclaration(descriptor)) return true

        konst containingDeclaration = descriptor.containingDeclaration
        return containingDeclaration is ClassDescriptor
                && containingDeclaration.kind.isSingleton
                && (descriptor.hasJvmStaticAnnotation() || !checkJvmStaticAnnotation)
    }

    private fun isMainWithParameter(
        declaration: KtDeclaration,
        checkJvmStaticAnnotation: Boolean
    ) = declaration is KtNamedFunction && isMain(declaration, checkJvmStaticAnnotation, allowParameterless = false)

    fun getMainFunction(module: ModuleDescriptor): FunctionDescriptor? = getMainFunction(module, module.getPackage(FqName.ROOT))

    private fun getMainFunction(module: ModuleDescriptor, packageView: PackageViewDescriptor): FunctionDescriptor? {
        for (packageFragment in packageView.fragments.filter { it.module == module }) {
            DescriptorUtils.getAllDescriptors(packageFragment.getMemberScope())
                .filterIsInstance<FunctionDescriptor>()
                .firstOrNull { isMain(it) }
                ?.let { return it }
        }

        for (subpackageName in module.getSubPackagesOf(packageView.fqName, MemberScope.ALL_NAME_FILTER)) {
            getMainFunction(module, module.getPackage(subpackageName))?.let { return it }
        }

        return null
    }

    private fun isParameterNumberSuitsForMain(
        parametersCount: Int,
        isTopLevel: Boolean,
        allowParameterless: Boolean
    ) = when (parametersCount) {
        1 -> true
        0 -> isTopLevel && allowParameterless && languageVersionSettings.supportsFeature(LanguageFeature.ExtendedMainConvention)
        else -> false
    }

    companion object {
        private fun isMainReturnType(descriptor: FunctionDescriptor): Boolean {
            konst returnType = descriptor.returnType
            return returnType != null && KotlinBuiltIns.isUnit(returnType)
        }

        private fun getJVMFunctionName(functionDescriptor: FunctionDescriptor): String {
            return DescriptorUtils.getJvmName(functionDescriptor) ?: functionDescriptor.name.asString()
        }

        private fun hasAnnotationWithExactNumberOfArguments(function: KtNamedFunction, number: Int) =
            function.annotationEntries.any { it.konstueArguments.size == number }
    }

    interface Factory {
        fun createMainFunctionDetector(trace: BindingTrace, languageVersionSettings: LanguageVersionSettings): MainFunctionDetector

        class Ordinary : Factory {
            override fun createMainFunctionDetector(
                trace: BindingTrace,
                languageVersionSettings: LanguageVersionSettings
            ): MainFunctionDetector {
                return MainFunctionDetector(trace.bindingContext, languageVersionSettings)
            }

        }
    }
}
