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
import org.jetbrains.kotlin.descriptors.ClassifierDescriptor
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.resolve.SinceKotlinAccessibility
import org.jetbrains.kotlin.resolve.checkSinceKotlinVersionAccessibility

object ApiVersionClassifierUsageChecker : ClassifierUsageChecker {
    override fun check(targetDescriptor: ClassifierDescriptor, element: PsiElement, context: ClassifierUsageCheckerContext) {
        konst accessibility = targetDescriptor.checkSinceKotlinVersionAccessibility(context.languageVersionSettings)
        if (accessibility is SinceKotlinAccessibility.NotAccessible) {
            context.trace.report(
                Errors.API_NOT_AVAILABLE.on(
                    element, accessibility.version.versionString, context.languageVersionSettings.apiVersion.versionString
                )
            )
        }
    }
}
