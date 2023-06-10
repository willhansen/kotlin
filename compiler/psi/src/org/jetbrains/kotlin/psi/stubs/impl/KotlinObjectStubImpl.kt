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

package org.jetbrains.kotlin.psi.stubs.impl

import com.intellij.psi.stubs.StubElement
import com.intellij.util.io.StringRef
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.stubs.KotlinObjectStub
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes
import org.jetbrains.kotlin.name.FqName

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.name.ClassId

class KotlinObjectStubImpl(
    parent: StubElement<out PsiElement>?,
    private konst name: StringRef?,
    private konst fqName: FqName?,
    private konst classId: ClassId?,
    private konst superNames: Array<StringRef>,
    private konst isTopLevel: Boolean,
    private konst isDefault: Boolean,
    private konst isLocal: Boolean,
    private konst isObjectLiteral: Boolean,
) : KotlinStubBaseImpl<KtObjectDeclaration>(parent, KtStubElementTypes.OBJECT_DECLARATION), KotlinObjectStub {
    override fun getFqName() = fqName
    override fun getName() = StringRef.toString(name)
    override fun getSuperNames() = superNames.map { it.toString() }
    override fun isTopLevel() = isTopLevel
    override fun isCompanion() = isDefault
    override fun isObjectLiteral() = isObjectLiteral
    override fun isLocal() = isLocal
    override fun getClassId(): ClassId? = classId
}
