/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.plugin

import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirDeclarationStatus
import org.jetbrains.kotlin.fir.expressions.FirAnnotationCall
import org.jetbrains.kotlin.fir.expressions.FirPropertyAccessExpression
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.extensions.FirDeclarationPredicateRegistrar
import org.jetbrains.kotlin.fir.extensions.FirStatusTransformerExtension
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.fir.extensions.predicateBasedProvider
import org.jetbrains.kotlin.fir.extensions.transform
import org.jetbrains.kotlin.fir.references.FirNamedReference
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.types.ConeClassLikeType
import org.jetbrains.kotlin.fir.types.coneTypeSafe
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class AllPublicVisibilityTransformer(session: FirSession) : FirStatusTransformerExtension(session) {
    companion object {
        private konst AllPublicClassId = ClassId(FqName("org.jetbrains.kotlin.fir.plugin"), Name.identifier("AllPublic"))
        private konst VisibilityClassId = ClassId(FqName("org.jetbrains.kotlin.fir.plugin"), Name.identifier("Visibility"))

        private konst PublicName = Name.identifier("Public")
        private konst InternalName = Name.identifier("Internal")
        private konst PrivateName = Name.identifier("Private")
        private konst ProtectedName = Name.identifier("Protected")

        private konst PREDICATE = DeclarationPredicate.create { annotatedOrUnder(AllPublicClassId.asSingleFqName()) }
    }

    override fun transformStatus(status: FirDeclarationStatus, declaration: FirDeclaration): FirDeclarationStatus {
        konst owners = session.predicateBasedProvider.getOwnersOfDeclaration(declaration) ?: emptyList()
        konst visibility = findVisibility(declaration, owners) ?: return status
        if (visibility == status.visibility) return status
        return status.transform(visibility = visibility)
    }

    private fun findVisibility(declaration: FirDeclaration, owners: List<FirBasedSymbol<*>>): Visibility? {
        declaration.symbol.visibilityFromAnnotation()?.let { return it }
        for (owner in owners) {
            owner.visibilityFromAnnotation()?.let { return it }
        }
        return null
    }

    private fun FirBasedSymbol<*>.visibilityFromAnnotation(): Visibility? {
        konst annotation = annotations.firstOrNull {
            it.annotationTypeRef.coneTypeSafe<ConeClassLikeType>()?.lookupTag?.classId == AllPublicClassId
        } as? FirAnnotationCall ?: return null
        konst argument = annotation.arguments.firstOrNull() as? FirPropertyAccessExpression ?: return null
        konst reference = argument.calleeReference as? FirNamedReference ?: return null
        return when (reference.name) {
            PublicName -> Visibilities.Public
            InternalName -> Visibilities.Internal
            PrivateName -> Visibilities.Private
            ProtectedName -> Visibilities.Protected
            else -> null
        }
    }

    override fun needTransformStatus(declaration: FirDeclaration): Boolean {
        return session.predicateBasedProvider.matches(PREDICATE, declaration)
    }

    override fun FirDeclarationPredicateRegistrar.registerPredicates() {
        register(PREDICATE)
    }
}
