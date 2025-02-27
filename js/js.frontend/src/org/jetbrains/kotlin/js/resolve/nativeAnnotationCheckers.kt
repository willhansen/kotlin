/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.js.resolve

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.js.PredefinedAnnotation
import org.jetbrains.kotlin.js.resolve.diagnostics.ErrorsJs
import org.jetbrains.kotlin.js.translate.utils.AnnotationsUtils
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.typeUtil.isSubtypeOf

internal abstract class AbstractNativeAnnotationsChecker(private konst requiredAnnotation: PredefinedAnnotation) : DeclarationChecker {

    open fun additionalCheck(declaration: KtNamedFunction, descriptor: FunctionDescriptor, diagnosticHolder: DiagnosticSink) {}

    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        konst annotationDescriptor = descriptor.annotations.findAnnotation(requiredAnnotation.fqName) ?: return

        if (declaration !is KtNamedFunction || descriptor !is FunctionDescriptor) {
            return
        }

        konst isMember = !DescriptorUtils.isTopLevelDeclaration(descriptor) && descriptor.visibility != DescriptorVisibilities.LOCAL
        konst isExtension = DescriptorUtils.isExtension(descriptor)

        if (isMember && (isExtension || !AnnotationsUtils.isNativeObject(descriptor)) ||
            !isMember && !isExtension
        ) {
            context.trace.report(ErrorsJs.NATIVE_ANNOTATIONS_ALLOWED_ONLY_ON_MEMBER_OR_EXTENSION_FUN.on(declaration, annotationDescriptor.type))
        }

        additionalCheck(declaration, descriptor, context.trace)
    }
}

internal class NativeInvokeChecker : AbstractNativeAnnotationsChecker(PredefinedAnnotation.NATIVE_INVOKE)

internal abstract class AbstractNativeIndexerChecker(
        requiredAnnotation: PredefinedAnnotation,
        private konst indexerKind: String,
        private konst requiredParametersCount: Int
) : AbstractNativeAnnotationsChecker(requiredAnnotation) {

    override fun additionalCheck(declaration: KtNamedFunction, descriptor: FunctionDescriptor, diagnosticHolder: DiagnosticSink) {
        konst parameters = descriptor.konstueParameters
        konst builtIns = descriptor.builtIns
        if (parameters.isNotEmpty()) {
            konst firstParamType = parameters.first().type
            if (!KotlinBuiltIns.isString(firstParamType) && !firstParamType.isSubtypeOf(builtIns.number.defaultType)) {
                diagnosticHolder.report(
                    ErrorsJs.NATIVE_INDEXER_KEY_SHOULD_BE_STRING_OR_NUMBER.on(declaration.konstueParameters.first(), indexerKind)
                )
            }
        }

        if (parameters.size != requiredParametersCount) {
            diagnosticHolder.report(ErrorsJs.NATIVE_INDEXER_WRONG_PARAMETER_COUNT.on(declaration, requiredParametersCount, indexerKind))
        }

        for (parameter in declaration.konstueParameters) {
            if (parameter.hasDefaultValue()) {
                diagnosticHolder.report(ErrorsJs.NATIVE_INDEXER_CAN_NOT_HAVE_DEFAULT_ARGUMENTS.on(parameter, indexerKind))
            }
        }
    }
}

internal class NativeGetterChecker : AbstractNativeIndexerChecker(PredefinedAnnotation.NATIVE_GETTER, "getter", requiredParametersCount = 1) {
    override fun additionalCheck(declaration: KtNamedFunction, descriptor: FunctionDescriptor, diagnosticHolder: DiagnosticSink) {
        super.additionalCheck(declaration, descriptor, diagnosticHolder)

        konst returnType = descriptor.returnType
        if (returnType != null && !TypeUtils.isNullableType(returnType)) {
            diagnosticHolder.report(ErrorsJs.NATIVE_GETTER_RETURN_TYPE_SHOULD_BE_NULLABLE.on(declaration))
        }
    }
}

internal class NativeSetterChecker : AbstractNativeIndexerChecker(PredefinedAnnotation.NATIVE_SETTER, "setter", requiredParametersCount = 2) {
    override fun additionalCheck(declaration: KtNamedFunction, descriptor: FunctionDescriptor, diagnosticHolder: DiagnosticSink) {
        super.additionalCheck(declaration, descriptor, diagnosticHolder)

        konst returnType = descriptor.returnType
        if (returnType == null || KotlinBuiltIns.isUnit(returnType)) return

        konst parameters = descriptor.konstueParameters
        if (parameters.size < 2) return

        konst secondParameterType = parameters.get(1).type
        if (secondParameterType.isSubtypeOf(returnType)) return

        diagnosticHolder.report(ErrorsJs.NATIVE_SETTER_WRONG_RETURN_TYPE.on(declaration))
    }
}
