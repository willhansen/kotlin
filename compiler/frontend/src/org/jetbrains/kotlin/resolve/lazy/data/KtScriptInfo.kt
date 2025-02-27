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

package org.jetbrains.kotlin.resolve.lazy.data

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.psi.KtAnnotationEntry
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtScript

class KtScriptInfo(
    konst script: KtScript
) : KtClassLikeInfo {
    override fun getContainingPackageFqName() = script.fqName.parent()
    override fun getModifierList() = null
    override fun getCompanionObjects() = listOf<KtObjectDeclaration>()
    override fun getScopeAnchor() = script
    override fun getCorrespondingClassOrObject() = null
    override fun getTypeParameterList() = null
    override fun getPrimaryConstructorParameters() = listOf<KtParameter>()
    override fun getClassKind() = ClassKind.CLASS
    override fun getDeclarations() = script.declarations
    override fun getDanglingAnnotations() = listOf<KtAnnotationEntry>()
}