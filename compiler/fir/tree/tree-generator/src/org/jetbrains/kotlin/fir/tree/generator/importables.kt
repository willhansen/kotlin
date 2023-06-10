/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.tree.generator

import org.jetbrains.kotlin.fir.tree.generator.model.ArbitraryImportable

konst phaseAsResolveStateExtentionImport = ArbitraryImportable("org.jetbrains.kotlin.fir.declarations", "asResolveState")
konst resolvePhaseExtensionImport = ArbitraryImportable("org.jetbrains.kotlin.fir.declarations", "resolvePhase")
konst resolveStateAccessImport = ArbitraryImportable("org.jetbrains.kotlin.fir.declarations", "ResolveStateAccess")
konst resolvedDeclarationStatusImport = ArbitraryImportable("org.jetbrains.kotlin.fir.declarations.impl", "FirResolvedDeclarationStatusImpl")

konst buildResolvedTypeRefImport = ArbitraryImportable("org.jetbrains.kotlin.fir.types.builder", "buildResolvedTypeRef")
konst constructClassTypeImport = ArbitraryImportable("org.jetbrains.kotlin.fir.types", "constructClassType")
konst toLookupTagImport = ArbitraryImportable("org.jetbrains.kotlin.fir.types", "toLookupTag")
