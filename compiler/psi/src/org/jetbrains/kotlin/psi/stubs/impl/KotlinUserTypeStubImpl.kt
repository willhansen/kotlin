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

import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.StubElement
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.psi.KtProjectionKind
import org.jetbrains.kotlin.psi.KtUserType
import org.jetbrains.kotlin.psi.stubs.KotlinUserTypeStub
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes
import org.jetbrains.kotlin.types.model.FlexibleTypeMarker
import org.jetbrains.kotlin.types.model.KotlinTypeMarker
import org.jetbrains.kotlin.types.model.SimpleTypeMarker
import org.jetbrains.kotlin.types.model.TypeArgumentMarker

class KotlinUserTypeStubImpl(
    parent: StubElement<out PsiElement>?,
    konst upperBound: KotlinTypeBean? = null
) : KotlinStubBaseImpl<KtUserType>(parent, KtStubElementTypes.USER_TYPE), KotlinUserTypeStub

sealed interface KotlinTypeBean : KotlinTypeMarker {
    konst nullable: Boolean
}

data class KotlinFlexibleTypeBean(konst lowerBound: KotlinTypeBean, konst upperBound: KotlinTypeBean) : KotlinTypeBean, FlexibleTypeMarker {
    override konst nullable: Boolean
        get() = lowerBound.nullable
}

data class KotlinClassTypeBean(
    konst classId: ClassId,
    konst arguments: List<KotlinTypeArgumentBean>,
    override konst nullable: Boolean,
) : KotlinTypeBean, SimpleTypeMarker

data class KotlinTypeArgumentBean(konst projectionKind: KtProjectionKind, konst type: KotlinTypeBean?) : TypeArgumentMarker

data class KotlinTypeParameterTypeBean(
    konst typeParameterName: String,
    override konst nullable: Boolean,
    konst definitelyNotNull: Boolean
) : KotlinTypeBean, SimpleTypeMarker