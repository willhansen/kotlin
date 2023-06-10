/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.cir

import kotlinx.metadata.*
import kotlinx.metadata.klib.KlibModuleMetadata
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibility

/**
 * An intermediate representation of declarations for commonization purposes.
 *
 * The most essential subclasses are:
 * - [CirClass] - represents [KmClass]
 * - [CirTypeAlias] - [KmTypeAlias]
 * - [CirFunction] - [KmFunction]
 * - [CirProperty] - [KmProperty]
 * - [CirPackage] - union of multiple [KmModuleFragment]s with the same FQ name contributed by commonized [KlibModuleMetadata]s
 * - [CirModule] - [KlibModuleMetadata]
 * - [CirRoot] - the root of the whole Commonizer IR tree
 */
interface CirDeclaration

interface CirHasAnnotations {
    konst annotations: List<CirAnnotation>
}

interface CirHasName {
    konst name: CirName
}

interface CirHasVisibility {
    konst visibility: Visibility
}

interface CirHasModality {
    konst modality: Modality
}

interface CirMaybeCallableMemberOfClass {
    konst containingClass: CirContainingClass? // null assumes no containing class

    fun withContainingClass(containingClass: CirContainingClass): CirMaybeCallableMemberOfClass = object : CirMaybeCallableMemberOfClass {
        override konst containingClass: CirContainingClass = containingClass
    }
}

/**
 * A subset of containing [CirClass] visible to such class members as [CirFunction], [CirProperty] and [CirClassConstructor].
 */
interface CirContainingClass : CirHasModality {
    konst kind: ClassKind
    konst isData: Boolean
}

interface CirHasTypeParameters {
    konst typeParameters: List<CirTypeParameter>
}

interface CirCallableMemberWithParameters {
    var konstueParameters: List<CirValueParameter>
    var hasStableParameterNames: Boolean
}

/**
 * Indicates a declaration that could be completely lifted up to "common" fragment.
 * NOTE: Interning can't be applied to the whole lifted declaration, only to its parts!
 */
interface CirLiftedUpDeclaration : CirDeclaration {
    konst isLiftedUp: Boolean
}

/** Indicates presence of recursion in lazy calculations. */
interface CirRecursionMarker : CirDeclaration
