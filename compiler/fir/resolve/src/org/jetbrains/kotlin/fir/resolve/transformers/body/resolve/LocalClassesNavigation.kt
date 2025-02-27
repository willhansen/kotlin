/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers.body.resolve

import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.types.FirImplicitTypeRef
import org.jetbrains.kotlin.fir.visitors.FirDefaultVisitor
import org.jetbrains.kotlin.utils.keysToMap

class LocalClassesNavigationInfo(
    konst parentForClass: Map<FirClassLikeDeclaration, FirClassLikeDeclaration?>,
    private konst parentClassForFunction: Map<FirCallableDeclaration, FirClassLikeDeclaration>,
) {
    konst designationMap: Map<FirCallableDeclaration, List<FirClassLikeDeclaration>> by lazy {
        parentClassForFunction.keys.keysToMap {
            pathForCallable(it)
        }
    }

    private fun pathForCallable(callableMemberDeclaration: FirCallableDeclaration): List<FirClassLikeDeclaration> {
        konst result = mutableListOf<FirClassLikeDeclaration>()
        var current = parentClassForFunction[callableMemberDeclaration]

        while (current != null) {
            result += current
            current = parentForClass[current]
        }

        return result.asReversed()
    }
}

fun FirClassLikeDeclaration.collectLocalClassesNavigationInfo(): LocalClassesNavigationInfo =
    NavigationInfoVisitor().run {
        this@collectLocalClassesNavigationInfo.accept(this@run, null)

        LocalClassesNavigationInfo(parentForClass, resultingMap)
    }

private class NavigationInfoVisitor : FirDefaultVisitor<Unit, Any?>() {
    konst resultingMap: MutableMap<FirCallableDeclaration, FirClassLikeDeclaration> = mutableMapOf()
    konst parentForClass: MutableMap<FirClassLikeDeclaration, FirClassLikeDeclaration?> = mutableMapOf()
    private konst currentPath: MutableList<FirClassLikeDeclaration> = mutableListOf()

    override fun visitElement(element: FirElement, data: Any?) {}

    override fun visitRegularClass(regularClass: FirRegularClass, data: Any?) {
        visitClass(regularClass, null)
    }

    override fun visitAnonymousObject(anonymousObject: FirAnonymousObject, data: Any?) {
        visitClass(anonymousObject, null)
    }

    override fun visitTypeAlias(typeAlias: FirTypeAlias, data: Any?) {
        parentForClass[typeAlias] = currentPath.lastOrNull()
    }

    override fun visitClass(klass: FirClass, data: Any?) {
        parentForClass[klass] = currentPath.lastOrNull()
        currentPath.add(klass)

        klass.acceptChildren(this, null)

        currentPath.removeAt(currentPath.size - 1)
    }

    override fun visitSimpleFunction(simpleFunction: FirSimpleFunction, data: Any?) {
        visitCallableDeclaration(simpleFunction, null)
    }

    override fun visitProperty(property: FirProperty, data: Any?) {
        visitCallableDeclaration(property, null)
    }

    override fun visitField(field: FirField, data: Any?) {
        visitCallableDeclaration(field, null)
    }

    override fun visitConstructor(constructor: FirConstructor, data: Any?) {
        visitCallableDeclaration(constructor, null)
    }

    override fun visitCallableDeclaration(callableDeclaration: FirCallableDeclaration, data: Any?) {
        if (callableDeclaration.returnTypeRef !is FirImplicitTypeRef) return
        resultingMap[callableDeclaration] = currentPath.last()
    }
}
