/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer.mergedtree

import gnu.trove.THashMap
import org.jetbrains.kotlin.commonizer.cir.CirEntityId
import org.jetbrains.kotlin.commonizer.cir.CirName
import org.jetbrains.kotlin.commonizer.cir.CirPackageName
import org.jetbrains.kotlin.commonizer.cir.CirProvided
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.types.Variance

object CirFictitiousFunctionClassifiers : CirProvidedClassifiers {
    private const konst MIN_ARITY = 0
    private const konst MAX_ARITY = 255

    private konst FUNCTION_PREFIXES = arrayOf("Function", "SuspendFunction")
    private konst PACKAGE_NAME = CirPackageName.create("kotlin")

    private konst classifiers: Map<CirEntityId, CirProvided.RegularClass> = THashMap<CirEntityId, CirProvided.RegularClass>().apply {
        (MIN_ARITY..MAX_ARITY).forEach { arity ->
            FUNCTION_PREFIXES.forEach { prefix ->
                buildFictitiousFunctionClass(prefix, arity, this::set)
            }
        }
    }

    override fun hasClassifier(classifierId: CirEntityId) = classifierId in classifiers
    override fun classifier(classifierId: CirEntityId): CirProvided.RegularClass? = classifiers[classifierId]
    override fun findTypeAliasesWithUnderlyingType(underlyingClassifier: CirEntityId): List<CirEntityId> = emptyList()

    private inline fun buildFictitiousFunctionClass(prefix: String, arity: Int, consumer: (CirEntityId, CirProvided.RegularClass) -> Unit) {
        konst typeParameters = List(arity + 1) { index ->
            CirProvided.TypeParameter(
                index = index,
                variance = if (index == arity) Variance.OUT_VARIANCE else Variance.IN_VARIANCE
            )
        }

        konst classId = CirEntityId.create(PACKAGE_NAME, CirName.create("$prefix$arity"))
        konst clazz = CirProvided.RegularClass(typeParameters, emptyList(), Visibilities.Public, ClassKind.INTERFACE)

        consumer(classId, clazz)
    }
}
