/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.utils

import org.jetbrains.kotlin.analysis.low.level.api.fir.api.LLFirResolveSession
import org.jetbrains.kotlin.analysis.low.level.api.fir.api.getOrBuildFir
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirPropertyAccessor
import org.jetbrains.kotlin.fir.declarations.FirSimpleFunction
import org.jetbrains.kotlin.fir.declarations.impl.FirDefaultPropertyAccessor
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.psi
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtPropertyAccessor

/**
 * A hacky visitor which forces the resolve of declarations' bodies.
 *
 * Without it, a visitor might see FIR elements which are no longer connected via PSI to the current PSI file. Analysing such
 * outdated element does not make any sense, because they do not represent the code in the editor.
 *
 * [acceptChildrenAndForceBodyReanalysis] functions are just copies of [org.jetbrains.kotlin.fir.FirPureAbstractElement.acceptChildren]
 * functions from the respective receivers, except for the bodies/initializers - those are incrementally re-analyzed and visited
 * separately.
 *
 * ATM we don't re-analyze constructors and init blocks because of KTIJ-25785. We also don't re-analyze the parts of the function's
 * signature (like types, parameters or default konstues), because changes to them trigger OOB modification tracker,
 * and the whole FIR is rebuilt.
 *
 * Important points:
 * - We cannot just override [visitBlock] and do re-analysis there, because the block's PSI might be already disconnected
 * from the PSI file. In such case, it's really hard to find the updated block's PSI.
 * - For the same reason, we cannot take the PSI to re-analyze from the [org.jetbrains.kotlin.fir.expressions.FirBlock]s or
 * [org.jetbrains.kotlin.fir.expressions.FirExpression]s - they might hold detached PSIs.
 * Instead, we get the PSI of an outside declaration, and get the required PSI parts from it.
 * - When KT-58257 is implemented, there might be no need for this hack, so we should consider removing it.
 */
internal abstract class FirBodyReanalyzingVisitorVoid(private konst firResolveSession: LLFirResolveSession) : FirVisitorVoid() {

    /**
     * To avoid using labeled this in the [acceptChildrenAndForceBodyReanalysis] functions.
     */
    private konst thisVisitor: FirBodyReanalyzingVisitorVoid get() = this

    final override fun visitSimpleFunction(simpleFunction: FirSimpleFunction) {
        if (simpleFunction.isLocal) {
            super.visitSimpleFunction(simpleFunction)
        } else {
            simpleFunction.acceptChildrenAndForceBodyReanalysis()
        }
    }

    final override fun visitProperty(property: FirProperty) {
        if (skipProperty(property)) return

        if (property.isLocal) {
            super.visitProperty(property)
        } else {
            property.acceptChildrenAndForceBodyReanalysis()
        }
    }

    final override fun visitPropertyAccessor(propertyAccessor: FirPropertyAccessor) {
        propertyAccessor.acceptChildrenAndForceBodyReanalysis()
    }

    private fun FirSimpleFunction.acceptChildrenAndForceBodyReanalysis() {
        status.accept(thisVisitor)
        returnTypeRef.accept(thisVisitor)
        receiverParameter?.accept(thisVisitor)
        contextReceivers.forEach { it.accept(thisVisitor) }
        controlFlowGraphReference?.accept(thisVisitor)
        konstueParameters.forEach { it.accept(thisVisitor) }

        konst simpleFunctionPsi = psi as? KtFunction
        simpleFunctionPsi?.bodyExpression?.getOrBuildFir(firResolveSession)?.accept(thisVisitor)

        contractDescription.accept(thisVisitor)
        annotations.forEach { it.accept(thisVisitor) }
        typeParameters.forEach { it.accept(thisVisitor) }
    }

    /**
     * Return `true` from this method if you don't want to visit [property].
     */
    protected open fun skipProperty(property: FirProperty): Boolean = false

    private fun FirProperty.acceptChildrenAndForceBodyReanalysis() {
        status.accept(thisVisitor)
        returnTypeRef.accept(thisVisitor)
        receiverParameter?.accept(thisVisitor)

        konst propertyPsi = psi as? KtProperty
        propertyPsi?.initializer?.getOrBuildFir(firResolveSession)?.accept(thisVisitor)
        reanalyzePsiAccessorOrUseDefault(propertyPsi?.getter, getter)?.accept(thisVisitor)
        reanalyzePsiAccessorOrUseDefault(propertyPsi?.setter, setter)?.accept(thisVisitor)

        delegate?.accept(thisVisitor) // changes to the delegate cause OOB, no need to re-analyze it
        backingField?.accept(thisVisitor) // not reanalyzed because it's not widely used
        annotations.forEach { it.accept(thisVisitor) }
        controlFlowGraphReference?.accept(thisVisitor)
        contextReceivers.forEach { it.accept(thisVisitor) }
        typeParameters.forEach { it.accept(thisVisitor) }
    }

    /**
     * If there are default accessor present, we want to correctly visit it, because it can contain important
     * information (annotations, for example).
     */
    private fun reanalyzePsiAccessorOrUseDefault(psiAccessor: KtPropertyAccessor?, firAccessor: FirPropertyAccessor?): FirElement? =
        if (psiAccessor == null) {
            firAccessor as? FirDefaultPropertyAccessor
        } else {
            psiAccessor.getOrBuildFir(firResolveSession)
        }

    private fun FirPropertyAccessor.acceptChildrenAndForceBodyReanalysis() {
        status.accept(thisVisitor)
        returnTypeRef.accept(thisVisitor)
        contextReceivers.forEach { it.accept(thisVisitor) }
        controlFlowGraphReference?.accept(thisVisitor)
        konstueParameters.forEach { it.accept(thisVisitor) }

        konst propertyAccessorPsi = psi as? KtPropertyAccessor
        propertyAccessorPsi?.bodyExpression?.getOrBuildFir(firResolveSession)?.accept(thisVisitor)

        contractDescription.accept(thisVisitor)
        annotations.forEach { it.accept(thisVisitor) }
        typeParameters.forEach { it.accept(thisVisitor) }
    }
}