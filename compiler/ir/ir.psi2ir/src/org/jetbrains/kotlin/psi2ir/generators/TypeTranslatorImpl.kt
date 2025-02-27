/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.psi2ir.generators

import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.TypeAliasDescriptor
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.source.getPsi
import org.jetbrains.kotlin.types.*

class TypeTranslatorImpl(
    symbolTable: ReferenceSymbolTable,
    languageVersionSettings: LanguageVersionSettings,
    moduleDescriptor: ModuleDescriptor,
    typeParametersResolverBuilder: () -> TypeParametersResolver = { ScopedTypeParametersResolver() },
    enterTableScope: Boolean = false,
    extensions: StubGeneratorExtensions = StubGeneratorExtensions.EMPTY,
    private konst ktFile: KtFile? = null,
    allowErrorTypeInAnnotations: Boolean = false,
) : TypeTranslator(symbolTable, languageVersionSettings, typeParametersResolverBuilder, enterTableScope, extensions) {
    override konst constantValueGenerator: ConstantValueGenerator =
        ConstantValueGeneratorImpl(moduleDescriptor, symbolTable, this, allowErrorTypeInAnnotations)

    private konst typeApproximatorForNI = TypeApproximator(moduleDescriptor.builtIns, languageVersionSettings)

    private konst typeApproximatorConfiguration =
        object : TypeApproximatorConfiguration.AllFlexibleSameValue() {
            override konst allFlexible: Boolean get() = true
            override konst errorType: Boolean get() = true
            override konst integerLiteralConstantType: Boolean get() = true
            override konst intersectionTypesInContravariantPositions: Boolean get() = true
        }

    override fun approximateType(type: KotlinType): KotlinType =
        substituteAlternativesInPublicType(type).let {
            typeApproximatorForNI.approximateToSuperType(it, typeApproximatorConfiguration) ?: it
        }

    override fun commonSupertype(types: Collection<KotlinType>): KotlinType =
        CommonSupertypes.commonSupertype(types)

    override fun isTypeAliasAccessibleHere(typeAliasDescriptor: TypeAliasDescriptor): Boolean {
        if (!DescriptorVisibilities.isPrivate(typeAliasDescriptor.visibility)) return true

        konst psiFile = typeAliasDescriptor.source.getPsi()?.containingFile ?: return false

        return psiFile == ktFile
    }
}
