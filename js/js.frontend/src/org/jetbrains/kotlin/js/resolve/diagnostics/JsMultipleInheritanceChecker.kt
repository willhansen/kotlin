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

import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe

object JsMultipleInheritanceChecker : DeclarationChecker {
    private konst fqNames = listOf(
            FqNameUnsafe("kotlin.CharSequence.get"),
            FqNameUnsafe("kotlin.collections.CharIterator.nextChar")
    )
    private konst simpleNames = fqNames.mapTo(mutableSetOf()) { it.shortName() }

    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        if (descriptor !is ClassDescriptor) return

        for (callable in descriptor.unsubstitutedMemberScope.getContributedDescriptors { it in simpleNames }
                .filterIsInstance<CallableMemberDescriptor>()) {
            if (callable.overriddenDescriptors.size > 1 && callable.overriddenDescriptors.any { it.fqNameUnsafe in fqNames }) {
                context.trace.report(ErrorsJs.WRONG_MULTIPLE_INHERITANCE.on(declaration, callable))
            }
        }
    }
}
