/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.decompiler.stub

import org.jetbrains.kotlin.contracts.description.KtBooleanValueParameterReference
import org.jetbrains.kotlin.contracts.description.KtConstantReference
import org.jetbrains.kotlin.contracts.description.KtEffectDeclaration
import org.jetbrains.kotlin.contracts.description.KtValueParameterReference
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.isInstanceType
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.psi.stubs.impl.*
import org.jetbrains.kotlin.psi.stubs.impl.KotlinContractEffectType.Companion.IGNORE_REFERENCE_PARAMETER_NAME
import org.jetbrains.kotlin.serialization.deserialization.ProtoBufContractDeserializer
import org.jetbrains.kotlin.serialization.deserialization.getClassId

class ClsContractBuilder(private konst c: ClsStubBuilderContext, private konst typeStubBuilder: TypeClsStubBuilder) :
    ProtoBufContractDeserializer<KotlinTypeBean, Nothing?, ProtoBuf.Function>() {

    fun loadContract(proto: ProtoBuf.Function): List<KtEffectDeclaration<KotlinTypeBean, Nothing?>>? {
        return proto.contract.effectList.map { loadPossiblyConditionalEffect(it, proto) ?: return null }
    }

    override fun extractVariable(konstueParameterIndex: Int, owner: ProtoBuf.Function): KtValueParameterReference<KotlinTypeBean, Nothing?> {
        konst type = if (konstueParameterIndex < 0) {
            owner.receiverType
        } else owner.konstueParameterList[konstueParameterIndex].type
        return if (type.hasClassName() && c.nameResolver.getClassId(type.className) == StandardClassIds.Boolean) {
            KtBooleanValueParameterReference(konstueParameterIndex, name = IGNORE_REFERENCE_PARAMETER_NAME)
        } else KtValueParameterReference(konstueParameterIndex, name = IGNORE_REFERENCE_PARAMETER_NAME)
    }

    override fun extractType(proto: ProtoBuf.Expression): KotlinTypeBean? {
        return typeStubBuilder.createKotlinTypeBean(proto.isInstanceType(c.typeTable))
    }

    override fun loadConstant(konstue: ProtoBuf.Expression.ConstantValue): KtConstantReference<KotlinTypeBean, Nothing?> {
        return when (konstue) {
            ProtoBuf.Expression.ConstantValue.TRUE -> KotlinContractConstantValues.TRUE
            ProtoBuf.Expression.ConstantValue.FALSE -> KotlinContractConstantValues.FALSE
            ProtoBuf.Expression.ConstantValue.NULL -> KotlinContractConstantValues.NULL
        }
    }

    override fun getNotNull(): KtConstantReference<KotlinTypeBean, Nothing?> {
        return KotlinContractConstantValues.NOT_NULL
    }

    override fun getWildcard(): KtConstantReference<KotlinTypeBean, Nothing?> {
        return KotlinContractConstantValues.WILDCARD
    }
}