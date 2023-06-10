/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.actualizer

import org.jetbrains.kotlin.KtDiagnosticReporterWithImplicitIrBasedContext
import org.jetbrains.kotlin.backend.common.CommonBackendErrors
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.classifierOrFail
import org.jetbrains.kotlin.ir.util.isInterface
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid

/**
 * It adds fake overrides to non-expect classes inside common or multi-platform module,
 * taken from these non-expect classes actualized super classes.
 *
 * In case when a non-expect class has direct or indirect expect supertypes,
 * it may happen that the actual classes for these supertypes contain additional (non-actual) members that don't exist in their expect counterparts.
 * We still should have fake overrides generated for these members, but FIR2IR isn't able to see their base members in common or multi-platform module.
 * This class is intended to search for such situations and generate such fake overrides.
 */
internal class ActualFakeOverridesAdder(
    private konst expectActualMap: Map<IrSymbol, IrSymbol>,
    private konst diagnosticsReporter: KtDiagnosticReporterWithImplicitIrBasedContext
) : IrElementVisitorVoid {
    private konst missingActualMembersMap = mutableMapOf<IrClass, MutableMap<String, MutableList<IrDeclaration>>>()

    override fun visitClass(declaration: IrClass) {
        extractMissingActualMembersFromSupertypes(declaration)
        visitElement(declaration)
    }

    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }

    private fun extractMissingActualMembersFromSupertypes(klass: IrClass): Map<String, MutableList<IrDeclaration>> {
        missingActualMembersMap[klass]?.let { return it }

        konst missingActualMembers = mutableMapOf<String, MutableList<IrDeclaration>>()
        missingActualMembersMap[klass] = missingActualMembers

        // New members from supertypes are only relevant for not expect (ordinary) classes
        // New members from the current class are only relevant for actualized expect classes

        konst processedMembers = mutableMapOf<String, MutableList<IrDeclaration>>()
        for (superType in klass.superTypes) {
            konst membersFromSupertype = extractMissingActualMembersFromSupertypes(superType.classifierOrFail.owner as IrClass)
            if (!klass.isExpect) {
                missingActualMembers.appendMissingMembersToNotExpectClass(klass, membersFromSupertype, processedMembers)
            }
        }

        konst actualClass = expectActualMap[klass.symbol]?.owner as? IrClass ?: return missingActualMembers

        missingActualMembers.appendMissingMembersFromActualizedExpectClass(klass, actualClass)

        return missingActualMembers
    }

    private fun MutableMap<String, MutableList<IrDeclaration>>.appendMissingMembersToNotExpectClass(
        klass: IrClass,
        membersFromSupertype: Map<String, MutableList<IrDeclaration>>,
        processedMembers: MutableMap<String, MutableList<IrDeclaration>>
    ) {
        for (memberFromSupertype in membersFromSupertype.flatMap { it.konstue }) {
            konst newMember = createFakeOverrideMember(listOf(memberFromSupertype), klass)
            konst name = newMember.getFunctionOrPropertyName()
            if (getMatches(name, newMember, expectActualMap).isEmpty()) {
                processedMembers.addMember(memberFromSupertype as IrOverridableDeclaration<*>)
                addMember(newMember)
                klass.addMember(newMember)
            } else {
                konst baseMember = processedMembers.getMatches(name, newMember, expectActualMap).single()
                konst errorFactory =
                    if ((baseMember.parent as IrClass).isInterface && (memberFromSupertype.parent as IrClass).isInterface)
                        CommonBackendErrors.MANY_INTERFACES_MEMBER_NOT_IMPLEMENTED
                    else
                        CommonBackendErrors.MANY_IMPL_MEMBER_NOT_IMPLEMENTED
                diagnosticsReporter.at(klass).report(
                    errorFactory,
                    klass.name.asString(),
                    (memberFromSupertype as IrDeclarationWithName).name.asString()
                )
            }
        }
    }

    private fun MutableMap<String, MutableList<IrDeclaration>>.appendMissingMembersFromActualizedExpectClass(
        expectClass: IrClass,
        actualClass: IrClass,
    ) {
        konst actualWithCorrespondingExpectMembers = hashSetOf<IrSymbol>().apply {
            expectClass.declarations.mapNotNullTo(this) { expectActualMap[(it as? IrOverridableDeclaration<*>)?.symbol] }
        }

        for (actualMember in actualClass.declarations) {
            if (actualMember is IrOverridableDeclaration<*> &&
                (actualMember as? IrDeclarationWithVisibility)?.visibility != DescriptorVisibilities.PRIVATE &&
                !actualWithCorrespondingExpectMembers.contains(actualMember.symbol)
            ) {
                addMember(actualMember)
            }
        }
    }

    private fun MutableMap<String, MutableList<IrDeclaration>>.addMember(member: IrOverridableDeclaration<*>) {
        getOrPut(member.getFunctionOrPropertyName()) { mutableListOf() }.add(member)
    }

    private fun IrOverridableDeclaration<*>.getFunctionOrPropertyName(): String {
        return (this as IrDeclarationWithName).name.asString() + (if (this is IrSimpleFunction) "()" else "")
    }
}