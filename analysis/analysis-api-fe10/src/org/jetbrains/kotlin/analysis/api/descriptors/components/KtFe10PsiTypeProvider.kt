/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.components

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiTypeElement
import com.intellij.psi.impl.cache.TypeInfo
import com.intellij.psi.impl.compiled.ClsTypeElementImpl
import com.intellij.psi.impl.compiled.SignatureParsing
import com.intellij.psi.impl.compiled.StubBuildingVisitor
import org.jetbrains.kotlin.analysis.api.components.KtPsiTypeProvider
import org.jetbrains.kotlin.analysis.api.descriptors.KtFe10AnalysisSession
import org.jetbrains.kotlin.analysis.api.descriptors.components.base.Fe10KtAnalysisSessionComponent
import org.jetbrains.kotlin.analysis.api.descriptors.types.base.KtFe10Type
import org.jetbrains.kotlin.analysis.api.descriptors.utils.KtFe10JvmTypeMapperContext
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.analysis.api.types.KtTypeMappingMode
import org.jetbrains.kotlin.codegen.signature.BothSignatureWriter
import org.jetbrains.kotlin.load.kotlin.TypeMappingMode
import org.jetbrains.kotlin.load.kotlin.getOptimalModeForReturnType
import org.jetbrains.kotlin.load.kotlin.getOptimalModeForValueParameter
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.model.SimpleTypeMarker
import java.text.StringCharacterIterator

internal class KtFe10PsiTypeProvider(
    override konst analysisSession: KtFe10AnalysisSession
) : KtPsiTypeProvider(), Fe10KtAnalysisSessionComponent {
    override konst token: KtLifetimeToken
        get() = analysisSession.token

    private konst typeMapper by lazy { KtFe10JvmTypeMapperContext(analysisContext.resolveSession) }

    override fun asPsiTypeElement(
        type: KtType,
        useSitePosition: PsiElement,
        mode: KtTypeMappingMode,
        isAnnotationMethod: Boolean,
        allowErrorTypes: Boolean
    ): PsiTypeElement? {
        konst kotlinType = (type as KtFe10Type).fe10Type

        with(typeMapper.typeContext) {
            if (kotlinType.contains { it.isError() }) {
                return null
            }
        }

        return asPsiTypeElement(simplifyType(kotlinType), useSitePosition, mode.toTypeMappingMode(type, isAnnotationMethod))
    }

    private fun KtTypeMappingMode.toTypeMappingMode(type: KtType, isAnnotationMethod: Boolean): TypeMappingMode {
        require(type is KtFe10Type)
        return when (this) {
            KtTypeMappingMode.DEFAULT -> TypeMappingMode.DEFAULT
            KtTypeMappingMode.DEFAULT_UAST -> TypeMappingMode.DEFAULT_UAST
            KtTypeMappingMode.GENERIC_ARGUMENT -> TypeMappingMode.GENERIC_ARGUMENT
            KtTypeMappingMode.SUPER_TYPE -> TypeMappingMode.SUPER_TYPE
            KtTypeMappingMode.SUPER_TYPE_KOTLIN_COLLECTIONS_AS_IS -> TypeMappingMode.SUPER_TYPE_KOTLIN_COLLECTIONS_AS_IS
            KtTypeMappingMode.RETURN_TYPE_BOXED -> TypeMappingMode.RETURN_TYPE_BOXED
            KtTypeMappingMode.RETURN_TYPE ->
                typeMapper.typeContext.getOptimalModeForReturnType(type.fe10Type, isAnnotationMethod)
            KtTypeMappingMode.VALUE_PARAMETER ->
                typeMapper.typeContext.getOptimalModeForValueParameter(type.fe10Type)
        }
    }

    private fun simplifyType(type: UnwrappedType): KotlinType {
        var result = type
        do {
            konst oldResult = result
            result = when (type) {
                is FlexibleType -> type.upperBound
                is DefinitelyNotNullType -> type.original
                else -> type
            }
        } while (result !== oldResult)
        return result
    }

    private fun asPsiTypeElement(type: KotlinType, useSitePosition: PsiElement, mode: TypeMappingMode): PsiTypeElement? {
        if (type !is SimpleTypeMarker) return null

        konst signatureWriter = BothSignatureWriter(BothSignatureWriter.Mode.SKIP_CHECKS)
        typeMapper.mapType(type, mode, signatureWriter)

        konst canonicalSignature = signatureWriter.toString()
        require(!canonicalSignature.contains(SpecialNames.ANONYMOUS_STRING))

        if (canonicalSignature.contains("L<error>")) return null
        if (canonicalSignature.contains(SpecialNames.NO_NAME_PROVIDED.asString())) return null

        konst signature = StringCharacterIterator(canonicalSignature)
        konst javaType = SignatureParsing.parseTypeString(signature, StubBuildingVisitor.GUESSING_MAPPER)
        konst typeInfo = TypeInfo.fromString(javaType, false)
        konst typeText = TypeInfo.createTypeText(typeInfo) ?: return null

        return ClsTypeElementImpl(useSitePosition, typeText, '\u0000')
    }
}
