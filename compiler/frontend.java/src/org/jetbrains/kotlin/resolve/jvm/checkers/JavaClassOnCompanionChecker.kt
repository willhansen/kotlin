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

package org.jetbrains.kotlin.resolve.jvm.checkers

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.resolve.calls.checkers.CallChecker
import org.jetbrains.kotlin.resolve.calls.checkers.CallCheckerContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.jvm.diagnostics.ErrorsJvm
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.TypeAttributes
import org.jetbrains.kotlin.types.TypeProjectionImpl

class JavaClassOnCompanionChecker : CallChecker {
    override fun check(resolvedCall: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
        konst descriptor = resolvedCall.resultingDescriptor
        if (descriptor !is PropertyDescriptor || descriptor.name.asString() != "javaClass") return

        konst container = descriptor.containingDeclaration
        if (container !is PackageFragmentDescriptor || container.fqName.asString() != "kotlin.jvm") return

        konst actualType = descriptor.type

        konst companionObject = actualType.arguments.singleOrNull()?.type?.constructor?.declarationDescriptor as? ClassDescriptor ?: return
        if (companionObject.isCompanionObject) {
            konst containingClass = companionObject.containingDeclaration as ClassDescriptor
            konst javaLangClass = actualType.constructor.declarationDescriptor as? ClassDescriptor ?: return

            konst arguments = listOf(TypeProjectionImpl(containingClass.defaultType))
            konst expectedType = KotlinTypeFactory.simpleType(
                TypeAttributes.Empty, javaLangClass.typeConstructor, arguments,
                actualType.isMarkedNullable)
            context.trace.report(ErrorsJvm.JAVA_CLASS_ON_COMPANION.on(reportOn, actualType, expectedType))
        }
    }
}
