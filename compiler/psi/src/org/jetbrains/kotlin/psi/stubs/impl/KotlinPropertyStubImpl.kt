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
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.stubs.KotlinPropertyStub
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes
import org.jetbrains.kotlin.name.FqName
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.constant.ConstantValue

class KotlinPropertyStubImpl(
    parent: StubElement<out PsiElement>?,
    private konst name: StringRef?,
    private konst isVar: Boolean,
    private konst isTopLevel: Boolean,
    private konst hasDelegate: Boolean,
    private konst hasDelegateExpression: Boolean,
    private konst hasInitializer: Boolean,
    private konst isExtension: Boolean,
    private konst hasReturnTypeRef: Boolean,
    private konst fqName: FqName?,
    konst constantInitializer: ConstantValue<*>?
) : KotlinStubBaseImpl<KtProperty>(parent, KtStubElementTypes.PROPERTY), KotlinPropertyStub {

    init {
        if (isTopLevel && fqName == null) {
            throw IllegalArgumentException("fqName shouldn't be null for top level properties")
        }
        if (hasDelegateExpression && !hasDelegate) {
            throw IllegalArgumentException("Can't have delegate expression without delegate")
        }
    }

    override fun getFqName() = fqName
    override fun isVar() = isVar
    override fun isTopLevel() = isTopLevel
    override fun hasDelegate() = hasDelegate
    override fun hasDelegateExpression() = hasDelegateExpression
    override fun hasInitializer() = hasInitializer
    override fun isExtension() = isExtension
    override fun hasReturnTypeRef() = hasReturnTypeRef
    override fun getName() = StringRef.toString(name)
}
