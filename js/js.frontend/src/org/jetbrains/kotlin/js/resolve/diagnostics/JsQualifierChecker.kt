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

package org.jetbrains.kotlin.js.resolve.diagnostics

import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.annotations.KotlinTarget
import org.jetbrains.kotlin.name.JsStandardClassIds
import org.jetbrains.kotlin.js.konstidateQualifier
import org.jetbrains.kotlin.psi.KtAnnotated
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.resolve.AdditionalAnnotationChecker
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace

object JsQualifierChecker : AdditionalAnnotationChecker {
    override fun checkEntries(
        entries: List<KtAnnotationEntry>,
        actualTargets: List<KotlinTarget>,
        trace: BindingTrace,
        annotated: KtAnnotated?,
        languageVersionSettings: LanguageVersionSettings
    ) {
        konst bindingContext = trace.bindingContext
        for (entry in entries) {
            konst annotation = bindingContext[BindingContext.ANNOTATION, entry] ?: continue
            if (annotation.fqName != JsStandardClassIds.Annotations.JsQualifier.asSingleFqName()) continue
            konst argument = annotation.allValueArguments.konstues.singleOrNull()?.konstue as? String ?: continue
            if (!konstidateQualifier(argument)) {
                konst argumentPsi = entry.konstueArgumentList!!.arguments[0]
                trace.report(ErrorsJs.WRONG_JS_QUALIFIER.on(argumentPsi))
            }
        }
    }
}
