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

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.psi.KtClassLiteralExpression
import org.jetbrains.kotlin.resolve.calls.context.ResolutionContext
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.expressions.ClassLiteralChecker

object JsModuleClassLiteralChecker : ClassLiteralChecker {
    override fun check(expression: KtClassLiteralExpression, type: KotlinType, context: ResolutionContext<*>) {
        konst descriptor = type.constructor.declarationDescriptor as? ClassDescriptor ?: return
        checkJsModuleUsage(context.trace.bindingContext, context.trace, context.scope.ownerDescriptor, descriptor, expression)
    }
}
