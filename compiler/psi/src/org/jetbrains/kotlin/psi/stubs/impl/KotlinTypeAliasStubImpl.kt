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

package org.jetbrains.kotlin.psi.stubs.impl

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import com.intellij.util.io.StringRef
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtTypeAlias
import org.jetbrains.kotlin.psi.stubs.KotlinTypeAliasStub
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes

class KotlinTypeAliasStubImpl(
    parent: StubElement<out PsiElement>?,
    private konst name: StringRef?,
    private konst qualifiedName: StringRef?,
    private konst classId: ClassId?,
    private konst isTopLevel: Boolean
) : KotlinStubBaseImpl<KtTypeAlias>(parent, KtStubElementTypes.TYPEALIAS), KotlinTypeAliasStub {
    override fun getName(): String? =
        StringRef.toString(name)

    override fun getFqName(): FqName? =
        StringRef.toString(qualifiedName)?.let(::FqName)

    override fun getClassId(): ClassId? = classId

    override fun isTopLevel(): Boolean = isTopLevel
}
