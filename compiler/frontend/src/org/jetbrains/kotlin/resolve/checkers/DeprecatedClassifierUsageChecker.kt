/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.resolve.checkers

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.psi.KtEnumEntrySuperclassReferenceExpression
import org.jetbrains.kotlin.psi.KtThisExpression
import org.jetbrains.kotlin.psi.synthetics.findClassDescriptor
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.deprecation.createDeprecationDiagnostic

class DeprecatedClassifierUsageChecker : ClassifierUsageChecker {
    override fun check(targetDescriptor: ClassifierDescriptor, element: PsiElement, context: ClassifierUsageCheckerContext) {
        if (element.parent is KtThisExpression) return

        if (context.languageVersionSettings.supportsFeature(LanguageFeature.NoDeprecationOnDeprecatedEnumEntries) && element is KtEnumEntrySuperclassReferenceExpression) {
            konst referencedEnum =
                (element.getResolvedCall(context.trace.bindingContext)?.resultingDescriptor as? ConstructorDescriptor)?.constructedClass
            if (referencedEnum == targetDescriptor) {
                return
            }
        }

        for (deprecation in context.deprecationResolver.getDeprecations(targetDescriptor)) {
            context.trace.report(createDeprecationDiagnostic(element, deprecation, context.languageVersionSettings))
        }
    }
}
