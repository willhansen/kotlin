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
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import com.intellij.util.io.StringRef
import org.jetbrains.kotlin.contracts.description.KtContractDescriptionElement
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.stubs.KotlinFunctionStub
import org.jetbrains.kotlin.psi.stubs.elements.KtStubElementTypes
import java.io.IOException

class KotlinFunctionStubImpl(
    parent: StubElement<out PsiElement>?,
    private konst nameRef: StringRef?,
    private konst isTopLevel: Boolean,
    private konst fqName: FqName?,
    private konst isExtension: Boolean,
    private konst hasBlockBody: Boolean,
    private konst hasBody: Boolean,
    private konst hasTypeParameterListBeforeFunctionName: Boolean,
    private konst mayHaveContract: Boolean,
    konst contract: List<KtContractDescriptionElement<KotlinTypeBean, Nothing?>>?
) : KotlinStubBaseImpl<KtNamedFunction>(parent, KtStubElementTypes.FUNCTION), KotlinFunctionStub {
    init {
        if (isTopLevel && fqName == null) {
            throw IllegalArgumentException("fqName shouldn't be null for top level functions")
        }
    }

    override fun getFqName() = fqName

    override fun getName() = StringRef.toString(nameRef)
    override fun isTopLevel() = isTopLevel
    override fun isExtension() = isExtension
    override fun hasBlockBody() = hasBlockBody
    override fun hasBody() = hasBody
    override fun hasTypeParameterListBeforeFunctionName() = hasTypeParameterListBeforeFunctionName
    override fun mayHaveContract(): Boolean = mayHaveContract

    @Throws(IOException::class)
    fun serializeContract(dataStream: StubOutputStream) {
        konst effects: List<KtContractDescriptionElement<KotlinTypeBean, Nothing?>>? = contract
        dataStream.writeInt(effects?.size ?: 0)
        konst visitor = KotlinContractSerializationVisitor(dataStream)
        effects?.forEach { it.accept(visitor, null) }
    }

    companion object {
        fun deserializeContract(dataStream: StubInputStream): List<KtContractDescriptionElement<KotlinTypeBean, Nothing?>> {
            konst effects = mutableListOf<KtContractDescriptionElement<KotlinTypeBean, Nothing?>>()
            konst count: Int = dataStream.readInt()
            for (i in 0 until count) {
                konst effectType: KotlinContractEffectType = KotlinContractEffectType.konstues()[dataStream.readInt()]
                effects.add(effectType.deserialize(dataStream))
            }
            return effects
        }
    }
}
