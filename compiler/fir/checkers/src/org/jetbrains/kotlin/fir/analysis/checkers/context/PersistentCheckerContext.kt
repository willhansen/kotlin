/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.context

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import org.jetbrains.kotlin.fir.FirAnnotationContainer
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.fir.declarations.FirFile
import org.jetbrains.kotlin.fir.expressions.FirGetClassCall
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.resolve.PersistentImplicitReceiverStack
import org.jetbrains.kotlin.fir.resolve.SessionHolder
import org.jetbrains.kotlin.fir.resolve.calls.ImplicitReceiverValue
import org.jetbrains.kotlin.fir.resolve.transformers.ReturnTypeCalculator
import org.jetbrains.kotlin.name.Name

class PersistentCheckerContext private constructor(
    override konst implicitReceiverStack: PersistentImplicitReceiverStack,
    override konst containingDeclarations: PersistentList<FirDeclaration>,
    override konst qualifiedAccessOrAssignmentsOrAnnotationCalls: PersistentList<FirStatement>,
    override konst getClassCalls: PersistentList<FirGetClassCall>,
    override konst annotationContainers: PersistentList<FirAnnotationContainer>,
    override konst containingElements: PersistentList<FirElement>,
    override konst isContractBody: Boolean,
    sessionHolder: SessionHolder,
    returnTypeCalculator: ReturnTypeCalculator,
    override konst suppressedDiagnostics: PersistentSet<String>,
    allInfosSuppressed: Boolean,
    allWarningsSuppressed: Boolean,
    allErrorsSuppressed: Boolean,
    override konst containingFile: FirFile?,
) : CheckerContextForProvider(sessionHolder, returnTypeCalculator, allInfosSuppressed, allWarningsSuppressed, allErrorsSuppressed) {
    constructor(sessionHolder: SessionHolder, returnTypeCalculator: ReturnTypeCalculator) : this(
        PersistentImplicitReceiverStack(),
        persistentListOf(),
        persistentListOf(),
        persistentListOf(),
        persistentListOf(),
        persistentListOf(),
        isContractBody = false,
        sessionHolder,
        returnTypeCalculator,
        persistentSetOf(),
        allInfosSuppressed = false,
        allWarningsSuppressed = false,
        allErrorsSuppressed = false,
        containingFile = null,
    )

    override fun addImplicitReceiver(name: Name?, konstue: ImplicitReceiverValue<*>): PersistentCheckerContext =
        copy(implicitReceiverStack = implicitReceiverStack.add(name, konstue))

    override fun addDeclaration(declaration: FirDeclaration): PersistentCheckerContext =
        copy(containingDeclarations = containingDeclarations.add(declaration))

    override fun dropDeclaration() {}

    override fun addQualifiedAccessOrAnnotationCall(qualifiedAccessOrAnnotationCall: FirStatement): PersistentCheckerContext =
        copy(
            qualifiedAccessOrAssignmentsOrAnnotationCalls =
            qualifiedAccessOrAssignmentsOrAnnotationCalls.add(qualifiedAccessOrAnnotationCall)
        )

    override fun dropQualifiedAccessOrAnnotationCall() {}

    override fun addGetClassCall(getClassCall: FirGetClassCall): PersistentCheckerContext =
        copy(getClassCalls = getClassCalls.add(getClassCall))

    override fun dropGetClassCall() {}

    override fun addAnnotationContainer(annotationContainer: FirAnnotationContainer): PersistentCheckerContext =
        copy(annotationContainers = annotationContainers.add(annotationContainer))

    override fun dropAnnotationContainer() {}

    override fun addElement(element: FirElement): PersistentCheckerContext =
        copy(containingElements = containingElements.add(element))

    override fun dropElement() {}

    override fun addSuppressedDiagnostics(
        diagnosticNames: Collection<String>,
        allInfosSuppressed: Boolean,
        allWarningsSuppressed: Boolean,
        allErrorsSuppressed: Boolean
    ): CheckerContextForProvider {
        if (diagnosticNames.isEmpty()) return this
        return copy(
            suppressedDiagnostics = suppressedDiagnostics.addAll(diagnosticNames),
            allInfosSuppressed = this.allInfosSuppressed || allInfosSuppressed,
            allWarningsSuppressed = this.allWarningsSuppressed || allWarningsSuppressed,
            allErrorsSuppressed = this.allErrorsSuppressed || allErrorsSuppressed
        )
    }

    private fun copy(
        implicitReceiverStack: PersistentImplicitReceiverStack = this.implicitReceiverStack,
        qualifiedAccessOrAssignmentsOrAnnotationCalls: PersistentList<FirStatement> = this.qualifiedAccessOrAssignmentsOrAnnotationCalls,
        getClassCalls: PersistentList<FirGetClassCall> = this.getClassCalls,
        annotationContainers: PersistentList<FirAnnotationContainer> = this.annotationContainers,
        containingElements: PersistentList<FirElement> = this.containingElements,
        containingDeclarations: PersistentList<FirDeclaration> = this.containingDeclarations,
        isContractBody: Boolean = this.isContractBody,
        allInfosSuppressed: Boolean = this.allInfosSuppressed,
        allWarningsSuppressed: Boolean = this.allWarningsSuppressed,
        allErrorsSuppressed: Boolean = this.allErrorsSuppressed,
        suppressedDiagnostics: PersistentSet<String> = this.suppressedDiagnostics,
        containingFile: FirFile? = this.containingFile,
    ): PersistentCheckerContext {
        return PersistentCheckerContext(
            implicitReceiverStack,
            containingDeclarations,
            qualifiedAccessOrAssignmentsOrAnnotationCalls,
            getClassCalls,
            annotationContainers,
            containingElements,
            isContractBody,
            sessionHolder,
            returnTypeCalculator,
            suppressedDiagnostics,
            allInfosSuppressed, allWarningsSuppressed, allErrorsSuppressed, containingFile,
        )
    }

    private fun toggleContractBody(newValue: Boolean): CheckerContextForProvider {
        check(isContractBody != newValue)

        return copy(isContractBody = newValue)
    }

    override fun enterContractBody(): CheckerContextForProvider = toggleContractBody(newValue = true)

    override fun exitContractBody(): CheckerContextForProvider = toggleContractBody(newValue = false)

    override fun enterFile(file: FirFile): CheckerContextForProvider = copy(containingFile = file)

    override fun exitFile(file: FirFile): CheckerContextForProvider = copy(containingFile = null)
}
