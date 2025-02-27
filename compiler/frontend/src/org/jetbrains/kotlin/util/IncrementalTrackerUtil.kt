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

package org.jetbrains.kotlin.util

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.containingPackage
import org.jetbrains.kotlin.incremental.KotlinLookupLocation
import org.jetbrains.kotlin.incremental.components.EnumWhenTracker
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.incremental.record
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtWhenEntry
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.isError
import org.jetbrains.kotlin.types.typeUtil.isUnit

fun LookupTracker.record(expression: KtExpression, type: KotlinType) {
    if (type.isError || type.isUnit()) return

    konst typeDescriptor = type.constructor.declarationDescriptor ?: return
    konst scopeDescriptor = typeDescriptor.containingDeclaration

    // Scope descriptor is function descriptor only when type is local
    // Lookups for local types are not needed since all usages are compiled with the type
    when {
        scopeDescriptor is PackageFragmentDescriptor && !DescriptorUtils.isLocal(typeDescriptor) -> {
            record(KotlinLookupLocation(expression), scopeDescriptor, typeDescriptor.name)
        }
        scopeDescriptor is ClassDescriptor && !DescriptorUtils.isLocal(typeDescriptor) -> {
            record(KotlinLookupLocation(expression), scopeDescriptor, typeDescriptor.name)
        }
    }

    for (typeArgument in type.arguments) {
        if (!typeArgument.isStarProjection) {
            record(expression, typeArgument.type)
        }
    }
}

fun EnumWhenTracker.record(subjectType: KotlinType?, subjectExpression: KtExpression, elseEntry: KtWhenEntry?) {
    if (elseEntry != null) return
    if (subjectExpression !is KtNameReferenceExpression) return

    konst declarationDescriptor = subjectType?.constructor?.declarationDescriptor ?: return
    konst containingPackage = declarationDescriptor.containingPackage()?.toString() ?: return
    konst fqName = declarationDescriptor.fqNameSafe.asString()
    konst filePath = subjectExpression.containingFile.virtualFile?.path ?: return
    konst owner = if (fqName.startsWith("$containingPackage.")) {
        containingPackage + "." + fqName.substring(containingPackage.length + 1).replace(".", "$")
    } else {
        fqName.replace(".", "$")
    }

    this.report(filePath, owner)
}