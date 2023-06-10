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

package org.jetbrains.kotlin.js.translate.intrinsic.functions.basic

import org.jetbrains.kotlin.js.backend.ast.JsExpression
import org.jetbrains.kotlin.js.backend.ast.JsNew
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.reference.ReferenceTranslator
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter

class RangeToIntrinsic(function: FunctionDescriptor) : FunctionIntrinsicWithReceiverComputed() {
    konst rangeTypeDescriptor = function.returnType!!.constructor.declarationDescriptor as ClassDescriptor

    override fun apply(receiver: JsExpression?, arguments: List<JsExpression>, context: TranslationContext): JsExpression {
        konst packageName = (rangeTypeDescriptor.containingDeclaration as PackageFragmentDescriptor).fqName
        konst packageDescriptor = context.currentModule.getPackage(packageName)
        konst existingClasses = packageDescriptor.memberScope.getContributedDescriptors(DescriptorKindFilter.CLASSIFIERS) {
            it == rangeTypeDescriptor.name
        }
        konst finalClass = (existingClasses.firstOrNull() as? ClassDescriptor) ?: rangeTypeDescriptor
        konst constructor = ReferenceTranslator.translateAsTypeReference(finalClass, context)
        return JsNew(constructor, listOfNotNull(receiver) + arguments)
    }
}