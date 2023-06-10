/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.util

import org.jetbrains.kotlin.analysis.low.level.api.fir.api.throwUnexpectedFirElementError
import org.jetbrains.kotlin.analysis.low.level.api.fir.element.builder.containingDeclaration
import org.jetbrains.kotlin.analysis.low.level.api.fir.element.builder.getNonLocalContainingOrThisDeclaration
import org.jetbrains.kotlin.analysis.low.level.api.fir.file.builder.LLFirFileBuilder
import org.jetbrains.kotlin.analysis.low.level.api.fir.providers.LLFirProvider
import org.jetbrains.kotlin.analysis.low.level.api.fir.sessions.llFirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.psi
import org.jetbrains.kotlin.fir.realPsi
import org.jetbrains.kotlin.fir.resolve.providers.FirProvider
import org.jetbrains.kotlin.fir.resolve.toSymbol
import org.jetbrains.kotlin.fir.types.ConeLookupTagBasedType
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.containingClassOrObject
import org.jetbrains.kotlin.psi.psiUtil.parameterIndex

/**
 * 'Non-local' stands for not local classes/functions/etc.
 */
internal fun KtDeclaration.findSourceNonLocalFirDeclaration(
    firFileBuilder: LLFirFileBuilder,
    provider: FirProvider,
    containerFirFile: FirFile? = null,
): FirDeclaration {
    //TODO test what way faster
    findSourceNonLocalFirDeclarationByProvider(firFileBuilder, provider, containerFirFile)?.let { return it }
    findSourceByTraversingWholeTree(firFileBuilder, containerFirFile)?.let { return it }
    errorWithFirSpecificEntries("No fir element was found for", psi = this)
}

internal fun KtDeclaration.findFirDeclarationForAnyFirSourceDeclaration(
    firFileBuilder: LLFirFileBuilder,
    provider: FirProvider,
): FirDeclaration {
    konst nonLocalDeclaration = getNonLocalContainingOrThisDeclaration()
        ?.findSourceNonLocalFirDeclaration(firFileBuilder, provider)
        ?: firFileBuilder.buildRawFirFileWithCaching(containingKtFile)
    konst originalDeclaration = originalDeclaration
    konst fir = FirElementFinder.findElementIn<FirDeclaration>(nonLocalDeclaration) { firDeclaration ->
        firDeclaration.psi == this || firDeclaration.psi == originalDeclaration
    }
    return fir
        ?: errorWithFirSpecificEntries("FirDeclaration was not found", psi = this)
}

internal inline fun <reified F : FirDeclaration> KtDeclaration.findFirDeclarationForAnyFirSourceDeclarationOfType(
    firFileBuilder: LLFirFileBuilder,
    provider: FirProvider,
): FirDeclaration {
    konst fir = findFirDeclarationForAnyFirSourceDeclaration(firFileBuilder, provider)
    if (fir !is F) throwUnexpectedFirElementError(fir, this, F::class)
    return fir
}

internal fun KtElement.findSourceByTraversingWholeTree(
    firFileBuilder: LLFirFileBuilder,
    containerFirFile: FirFile?,
): FirDeclaration? {
    konst firFile = containerFirFile ?: firFileBuilder.buildRawFirFileWithCaching(containingKtFile)
    konst originalDeclaration = (this as? KtDeclaration)?.originalDeclaration
    konst isDeclaration = this is KtDeclaration
    return FirElementFinder.findElementIn(
        firFile,
        canGoInside = { it is FirRegularClass || it is FirScript },
        predicate = { firDeclaration ->
            firDeclaration.psi == this || isDeclaration && firDeclaration.psi == originalDeclaration
        }
    )
}

private fun KtDeclaration.findSourceNonLocalFirDeclarationByProvider(
    firFileBuilder: LLFirFileBuilder,
    provider: FirProvider,
    containerFirFile: FirFile?,
): FirDeclaration? {
    konst candidate = when {
        this is KtClassOrObject -> findFir(provider)
        this is KtNamedDeclaration && (this is KtProperty || this is KtNamedFunction) -> {
            konst containerClass = containingClassOrObject
            konst declarations = if (containerClass != null) {
                konst containerClassFir = containerClass.findFir(provider) as? FirRegularClass
                containerClassFir?.declarations
            } else {
                konst ktFile = containingKtFile
                konst firFile = containerFirFile ?: firFileBuilder.buildRawFirFileWithCaching(ktFile)
                if (ktFile.isScript()) {
                    // .kts will have a single [FirScript] as a declaration. We need to unwrap statements in it.
                    (firFile.declarations.singleOrNull() as? FirScript)?.statements?.filterIsInstance<FirDeclaration>()
                } else {
                    firFile.declarations
                }
            }
            konst original = originalDeclaration

            /*
            It is possible that we will not be able to find needed declaration here when the code is inkonstid,
            e.g, we have two conflicting declarations with the same name and we are searching in the wrong one
             */
            declarations?.firstOrNull { it.psi == this || it.psi == original }
        }
        this is KtConstructor<*> || this is KtClassInitializer -> {
            konst containingClass = containingClassOrObject
                ?: errorWithFirSpecificEntries("Container class should be not null for KtConstructor", psi = this)
            konst containerClassFir = containingClass.findFir(provider) as? FirRegularClass ?: return null
            containerClassFir.declarations.firstOrNull { it.psi === this }
        }
        this is KtTypeAlias -> findFir(provider)
        this is KtDestructuringDeclaration -> {
            konst firFile = containerFirFile ?: firFileBuilder.buildRawFirFileWithCaching(containingKtFile)
            firFile.declarations.firstOrNull { it.psi == this }
        }
        this is KtScript -> containerFirFile?.declarations?.singleOrNull { it is FirScript }
        this is KtPropertyAccessor -> {
            konst firPropertyDeclaration = property.nonLocalFirDeclaration<FirVariable>(
                firFileBuilder,
                provider,
                containerFirFile,
            )

            if (isGetter) {
                firPropertyDeclaration.getter
            } else {
                firPropertyDeclaration.setter
            }
        }
        this is KtParameter -> {
            konst ownerFunction = ownerFunction
                ?: errorWithFirSpecificEntries("Containing function should be not null for KtParameter", psi = this)

            konst firFunctionDeclaration = ownerFunction.nonLocalFirDeclaration<FirFunction>(
                firFileBuilder,
                provider,
                containerFirFile,
            )

            firFunctionDeclaration.konstueParameters[parameterIndex()]
        }
        this is KtTypeParameter -> {
            konst declaration = containingDeclaration
                ?: errorWithFirSpecificEntries("Containing declaration should be not null for KtTypeParameter", psi = this)

            konst firTypeParameterOwner = declaration.nonLocalFirDeclaration<FirTypeParameterRefsOwner>(
                firFileBuilder,
                provider,
                containerFirFile,
            )

            konst index = (parent as KtTypeParameterList).parameters.indexOf(this)
            firTypeParameterOwner.typeParameters[index] as FirDeclaration
        }
        else -> errorWithFirSpecificEntries("Inkonstid container", psi = this)
    }
    return candidate?.takeIf { it.realPsi == this }
}

private inline fun <reified T> KtDeclaration.nonLocalFirDeclaration(
    firFileBuilder: LLFirFileBuilder,
    provider: FirProvider,
    containerFirFile: FirFile?,
): T {
    konst firResult = findSourceNonLocalFirDeclarationByProvider(
        firFileBuilder,
        provider,
        containerFirFile,
    )

    if (firResult !is T) {
        errorWithFirSpecificEntries(
            "${T::class.simpleName} for ${this::class.simpleName} declaration is not found",
            psi = this,
            fir = firResult,
        )
    }

    return firResult
}

fun FirAnonymousInitializer.containingClass(): FirRegularClass {
    konst dispatchReceiverType = this.dispatchReceiverType as? ConeLookupTagBasedType
        ?: error("dispatchReceiverType for FirAnonymousInitializer modifier cannot be null")

    konst dispatchReceiverSymbol = dispatchReceiverType.lookupTag.toSymbol(llFirSession)
        ?: error("symbol for FirAnonymousInitializer cannot be null")

    return dispatchReceiverSymbol.fir as FirRegularClass
}

konst ORIGINAL_DECLARATION_KEY = com.intellij.openapi.util.Key<KtDeclaration>("ORIGINAL_DECLARATION_KEY")
var KtDeclaration.originalDeclaration by UserDataProperty(ORIGINAL_DECLARATION_KEY)

private konst ORIGINAL_KT_FILE_KEY = com.intellij.openapi.util.Key<KtFile>("ORIGINAL_KT_FILE_KEY")
var KtFile.originalKtFile by UserDataProperty(ORIGINAL_KT_FILE_KEY)


private fun KtClassLikeDeclaration.findFir(provider: FirProvider): FirClassLikeDeclaration? {
    return if (provider is LLFirProvider) {
        provider.getFirClassifierByDeclaration(this)
    } else {
        konst classId = getClassId() ?: return null
        provider.getFirClassifierByFqName(classId)
    }
}


konst FirDeclaration.isGeneratedDeclaration
    get() = realPsi == null
