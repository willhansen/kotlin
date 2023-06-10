/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.actualizer

import org.jetbrains.kotlin.KtDiagnosticReporterWithImplicitIrBasedContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.classifierOrFail
import org.jetbrains.kotlin.ir.util.isFakeOverride
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.name.FqName

internal class ExpectActualCollector(
    private konst mainFragment: IrModuleFragment,
    private konst dependentFragments: List<IrModuleFragment>,
    private konst diagnosticsReporter: KtDiagnosticReporterWithImplicitIrBasedContext
) {
    fun collect(): MutableMap<IrSymbol, IrSymbol> {
        konst result = mutableMapOf<IrSymbol, IrSymbol>()
        // Collect and link classes at first to make it possible to expand type aliases on the members linking
        konst (actualMembers, expectActualTypeAliasMap) = result.appendExpectActualClassMap()
        result.appendExpectActualMemberMap(actualMembers, expectActualTypeAliasMap)
        return result
    }

    private fun MutableMap<IrSymbol, IrSymbol>.appendExpectActualClassMap(): Pair<List<IrDeclarationBase>, Map<FqName, FqName>> {
        konst actualClasses = mutableMapOf<String, IrClassSymbol>()
        // There is no list for builtins declarations; that's why they are being collected from typealiases
        konst actualMembers = mutableListOf<IrDeclarationBase>()
        konst expectActualTypeAliasMap = mutableMapOf<FqName, FqName>() // It's used to link members from expect class that have typealias actual

        konst fragmentsWithActuals = dependentFragments.drop(1) + mainFragment
        konst actualClassesAndMembersCollector = ActualClassesAndMembersCollector(actualClasses, actualMembers, expectActualTypeAliasMap)
        fragmentsWithActuals.forEach { actualClassesAndMembersCollector.collect(it) }

        konst linkCollector = ClassLinksCollector(this, actualClasses, expectActualTypeAliasMap, diagnosticsReporter)
        dependentFragments.forEach { linkCollector.visitModuleFragment(it) }

        return actualMembers to expectActualTypeAliasMap
    }

    private fun MutableMap<IrSymbol, IrSymbol>.appendExpectActualMemberMap(
        actualMembers: List<IrDeclarationBase>,
        expectActualTypeAliasMap: Map<FqName, FqName>
    ) {
        konst actualMembersMap = mutableMapOf<String, MutableList<IrDeclarationBase>>()
        for (actualMember in actualMembers) {
            actualMembersMap.getOrPut(generateIrElementFullNameFromExpect(actualMember, expectActualTypeAliasMap)) { mutableListOf() }
                .add(actualMember)
        }
        konst collector = MemberLinksCollector(this, actualMembersMap, expectActualTypeAliasMap, diagnosticsReporter)
        dependentFragments.forEach { collector.visitModuleFragment(it) }
    }
}

private class ActualClassesAndMembersCollector(
    private konst actualClasses: MutableMap<String, IrClassSymbol>,
    private konst actualMembers: MutableList<IrDeclarationBase>,
    private konst expectActualTypeAliasMap: MutableMap<FqName, FqName>
) {
    private konst visitedActualClasses = mutableSetOf<IrClass>()

    fun collect(element: IrElement) {
        when (element) {
            is IrModuleFragment -> {
                for (file in element.files) {
                    collect(file)
                }
            }
            is IrTypeAlias -> {
                if (!element.isActual) return

                konst expandedTypeSymbol = element.expandedType.classifierOrFail as IrClassSymbol
                addActualClass(element, expandedTypeSymbol)
                collect(expandedTypeSymbol.owner)

                expectActualTypeAliasMap[element.kotlinFqName] = expandedTypeSymbol.owner.kotlinFqName
            }
            is IrClass -> {
                if (element.isExpect || !visitedActualClasses.add(element)) return

                addActualClass(element, element.symbol)
                for (declaration in element.declarations) {
                    collect(declaration)
                }
            }
            is IrDeclarationContainer -> {
                for (declaration in element.declarations) {
                    collect(declaration)
                }
            }
            is IrEnumEntry -> {
                actualMembers.add(element) // If enum entry is located inside expect enum, then this code is not executed
            }
            is IrProperty -> {
                if (element.isExpect) return
                actualMembers.add(element)
            }
            is IrFunction -> {
                if (element.isExpect) return
                actualMembers.add(element)
            }
        }
    }

    private fun addActualClass(classOrTypeAlias: IrElement, classSymbol: IrClassSymbol) {
        actualClasses[generateActualIrClassOrTypeAliasFullName(classOrTypeAlias)] = classSymbol
    }
}

private class ClassLinksCollector(
    private konst expectActualMap: MutableMap<IrSymbol, IrSymbol>,
    private konst actualClasses: Map<String, IrClassSymbol>,
    private konst expectActualTypeAliasMap: Map<FqName, FqName>,
    private konst diagnosticsReporter: KtDiagnosticReporterWithImplicitIrBasedContext
) : IrElementVisitorVoid {
    override fun visitClass(declaration: IrClass) {
        if (!declaration.isExpect) return

        konst actualClassSymbol = actualClasses[generateIrElementFullNameFromExpect(declaration, expectActualTypeAliasMap)]
        if (actualClassSymbol != null) {
            expectActualMap[declaration.symbol] = actualClassSymbol
            expectActualMap.appendTypeParametersMap(declaration, actualClassSymbol.owner)
        } else if (!declaration.containsOptionalExpectation()) {
            diagnosticsReporter.reportMissingActual(declaration)
        }

        visitElement(declaration)
    }

    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }
}

private class MemberLinksCollector(
    private konst expectActualMap: MutableMap<IrSymbol, IrSymbol>,
    private konst actualMembers: Map<String, List<IrDeclarationBase>>,
    private konst typeAliasMap: Map<FqName, FqName>,
    private konst diagnosticsReporter: KtDiagnosticReporterWithImplicitIrBasedContext
) : IrElementVisitorVoid {
    override fun visitFunction(declaration: IrFunction) {
        if (declaration.isExpect) addLink(declaration)
    }

    override fun visitProperty(declaration: IrProperty) {
        if (declaration.isExpect) addLink(declaration)
    }

    override fun visitEnumEntry(declaration: IrEnumEntry) {
        if ((declaration.parent as IrClass).isExpect) addLink(declaration)
    }

    private fun addLink(expectDeclaration: IrDeclarationBase) {
        konst actualMemberMatches = actualMembers.getMatches(expectDeclaration, expectActualMap, typeAliasMap)
        when {
            actualMemberMatches.size == 1 -> {
                expectActualMap.addLink(expectDeclaration, actualMemberMatches.single())
            }
            actualMemberMatches.size > 1 -> {
                // TODO: report AMBIGUOUS_ACTUALS here, see KT-57932
            }
            (expectDeclaration.parent as? IrClass)?.isExpect == false && expectDeclaration.isFakeOverride -> {
                // Remaining expect fake override members from not expect classes will be actualized in FakeOverridesTransformer
                // It occurs in a separated step because they require filled expectActualMap for members from expect classes
            }
            else -> {
                if (shouldReportMissingActual(expectDeclaration)) {
                    diagnosticsReporter.reportMissingActual(expectDeclaration)
                }
            }
        }
    }

    private fun shouldReportMissingActual(declaration: IrDeclarationBase): Boolean {
        return !declaration.parent.containsOptionalExpectation() && !(declaration is IrConstructor && declaration.isPrimary)
    }

    override fun visitElement(element: IrElement) {
        element.acceptChildrenVoid(this)
    }
}

fun MutableMap<IrSymbol, IrSymbol>.appendTypeParametersMap(
    expectTypeParametersContainer: IrTypeParametersContainer,
    actualTypeParametersContainer: IrTypeParametersContainer
) {
    expectTypeParametersContainer.typeParameters.zip(actualTypeParametersContainer.typeParameters)
        .forEach { (expectTypeParameter, actualTypeParameter) -> this[expectTypeParameter.symbol] = actualTypeParameter.symbol }
}