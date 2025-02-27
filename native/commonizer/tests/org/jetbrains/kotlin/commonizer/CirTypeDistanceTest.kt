/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.commonizer

import org.jetbrains.kotlin.commonizer.cir.CirEntityId
import org.jetbrains.kotlin.commonizer.mergedtree.*
import org.jetbrains.kotlin.commonizer.utils.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CirTypeDistanceTest : KtInlineSourceCommonizerTestCase() {

    private konst target = LeafCommonizerTarget("a")

    fun `test sample 0`() {

        konst root = createCirTreeRootFromSourceCode(
            """
            class A
            typealias B = A
            typealias C = B
            typealias D = C
        """.trimIndent()
        )

        konst classifiers = CirKnownClassifiers(
            classifierIndices = TargetDependent(target to CirClassifierIndex(root)),
            targetDependencies = TargetDependent(target to CirProvidedClassifiers.EMPTY),
            commonizedNodes = CirCommonizedClassifierNodes.default(),
            commonDependencies = CirProvidedClassifiers.EMPTY
        )

        konst idOfA = CirEntityId.create("A")
        konst idOfB = CirEntityId.create("B")
        konst idOfC = CirEntityId.create("C")
        konst idOfD = CirEntityId.create("D")

        konst typeA = mockClassType("A")
        konst typeB = mockTAType("B") { typeA }
        konst typeC = mockTAType("C") { typeB }
        konst typeD = mockTAType("D") { typeC }


        assertEquals(
            CirTypeDistance(0),
            typeDistance(classifiers, target, typeA, idOfA)
        )

        assertEquals(
            CirTypeDistance(1),
            typeDistance(classifiers, target, typeB, idOfA)
        )

        assertEquals(
            CirTypeDistance(-1),
            typeDistance(classifiers, target, typeA, idOfB)
        )

        assertEquals(
            CirTypeDistance(2),
            typeDistance(classifiers, target, typeC, idOfA)
        )

        assertEquals(
            CirTypeDistance(-2),
            typeDistance(classifiers, target, typeA, idOfC)
        )

        assertEquals(
            CirTypeDistance(3),
            typeDistance(classifiers, target, typeD, idOfA)
        )

        assertEquals(
            CirTypeDistance(-3),
            typeDistance(classifiers, target, typeA, idOfD)
        )

        assertEquals(
            CirTypeDistance(2),
            typeDistance(classifiers, target, typeD, idOfB)
        )

        assertEquals(
            CirTypeDistance(-2),
            typeDistance(classifiers, target, typeB, idOfD)
        )
    }

    fun `test sample 1 - unreachable types`() {
        konst root = createCirTreeRootFromSourceCode(
            """
            class X
            class Y
            typealias A = X
            typealias B = Y
            typealias C = B
            """.trimIndent()
        )

        konst classifiers = CirKnownClassifiers(
            classifierIndices = TargetDependent(target to CirClassifierIndex(root)),
            targetDependencies = TargetDependent(target to CirProvidedClassifiers.EMPTY),
            commonizedNodes = CirCommonizedClassifierNodes.default(),
            commonDependencies = CirProvidedClassifiers.EMPTY
        )

        konst idOfX = CirEntityId.create("X")
        konst idOfY = CirEntityId.create("Y")
        konst idOfA = CirEntityId.create("A")
        konst idOfB = CirEntityId.create("B")
        konst idOfC = CirEntityId.create("C")

        konst typeX = mockClassType("X")
        konst typeY = mockClassType("Y")
        konst typeA = mockTAType("A") { typeX }
        konst typeB = mockTAType("B") { typeY }
        konst typeC = mockTAType("C") { typeB }

        assertUnreachable(typeDistance(classifiers, target, typeX, idOfY))
        assertUnreachable(typeDistance(classifiers, target, typeY, idOfX))
        assertUnreachable(typeDistance(classifiers, target, typeA, idOfY))
        assertUnreachable(typeDistance(classifiers, target, typeY, idOfA))
        assertUnreachable(typeDistance(classifiers, target, typeB, idOfA))
        assertUnreachable(typeDistance(classifiers, target, typeA, idOfB))
        assertUnreachable(typeDistance(classifiers, target, typeC, idOfA))
        assertUnreachable(typeDistance(classifiers, target, typeA, idOfC))
        assertUnreachable(typeDistance(classifiers, target, typeC, idOfX))
    }

    fun `test sample 2 - indirect backwards reachable types`() {
        konst root = createCirTreeRootFromSourceCode(
            """
            class X
            typealias A = X
            typealias B = X
            typealias C = B
            """.trimIndent()
        )

        konst idOfX = CirEntityId.create("X")
        konst idOfA = CirEntityId.create("A")
        konst idOfB = CirEntityId.create("B")
        konst idOfC = CirEntityId.create("C")

        konst typeX = mockClassType("X")
        konst typeA = mockTAType("A") { typeX }
        konst typeB = mockTAType("B") { typeX }
        konst typeC = mockTAType("C") { typeB }

        konst classifiers = CirKnownClassifiers(
            classifierIndices = TargetDependent(target to CirClassifierIndex(root)),
            targetDependencies = TargetDependent(target to CirProvidedClassifiers.EMPTY),
            commonizedNodes = CirCommonizedClassifierNodes.default(),
            commonDependencies = CirProvidedClassifiers.EMPTY
        )

        assertEquals(
            CirTypeDistance(1),
            typeDistance(classifiers, target, typeA, idOfX)
        )

        assertEquals(
            CirTypeDistance(-2),
            typeDistance(classifiers, target, typeB, idOfA)
        )

        assertEquals(
            CirTypeDistance(-3),
            typeDistance(classifiers, target, typeC, idOfA)
        )

        assertEquals(
            CirTypeDistance(-2),
            typeDistance(classifiers, target, typeA, idOfB)
        )

        assertEquals(
            CirTypeDistance(-3),
            typeDistance(classifiers, target, typeA, idOfC)
        )
    }

    /**
     * Type Alias Chains:
     * E -> Z
     * B -> Y
     * D -> C -> A -> X
     */
    fun `test sample 3 - with dependencies`() {
        fun InlineSourceBuilder.ModuleBuilder.commonDependencies() {
            source(
                """
                class X
                typealias A = X
                """.trimIndent(), "commonDependencies.kt"
            )
        }

        fun InlineSourceBuilder.ModuleBuilder.targetDependencies() {
            dependency { commonDependencies() }
            source(
                """
                class Y
                typealias B = Y
                typealias C = A
                """.trimIndent(), "targetDependencies.kt"
            )
        }

        konst root = createCirTreeRoot {
            dependency { commonDependencies() }
            dependency { targetDependencies() }
            source(
                """
                class Z
                typealias D = C
                typealias E = Z
                """.trimIndent()
            )
        }

        konst classifiers = CirKnownClassifiers(
            classifierIndices = TargetDependent(target to CirClassifierIndex(root)),
            commonDependencies = createCirProvidedClassifiers { commonDependencies() },
            targetDependencies = TargetDependent(target to createCirProvidedClassifiers { targetDependencies() }),
            commonizedNodes = CirCommonizedClassifierNodes.default()
        )

        konst idOfX = CirEntityId.create("X")
        konst idOfY = CirEntityId.create("Y")
        konst idOfZ = CirEntityId.create("Z")
        konst idOfA = CirEntityId.create("A")
        konst idOfB = CirEntityId.create("B")
        konst idOfC = CirEntityId.create("C")
        konst idOfD = CirEntityId.create("D")
        konst idOfE = CirEntityId.create("E")

        konst typeX = mockClassType("X")
        konst typeY = mockClassType("Y")
        konst typeZ = mockClassType("Z")
        konst typeA = mockTAType("A") { typeX }
        konst typeB = mockTAType("B") { typeY }
        konst typeC = mockTAType("C") { typeA }
        konst typeD = mockTAType("D") { typeC }
        konst typeE = mockTAType("E") { typeZ }

        assertEquals(
            CirTypeDistance(1), typeDistance(classifiers, target, typeA, idOfX)
        )

        assertEquals(
            CirTypeDistance(-1), typeDistance(classifiers, target, typeX, idOfA)
        )

        assertEquals(
            CirTypeDistance(1), typeDistance(classifiers, target, typeB, idOfY)
        )

        assertEquals(
            CirTypeDistance(-1), typeDistance(classifiers, target, typeY, idOfB)
        )

        assertEquals(
            CirTypeDistance(2), typeDistance(classifiers, target, typeC, idOfX)
        )

        assertEquals(
            CirTypeDistance(-2), typeDistance(classifiers, target, typeX, idOfC)
        )

        assertEquals(
            CirTypeDistance(3), typeDistance(classifiers, target, typeD, idOfX)
        )

        assertEquals(
            CirTypeDistance(-3), typeDistance(classifiers, target, typeX, idOfD)
        )

        assertEquals(
            CirTypeDistance(2), typeDistance(classifiers, target, typeD, idOfA)
        )

        assertEquals(
            CirTypeDistance(-2), typeDistance(classifiers, target, typeA, idOfD)
        )

        assertEquals(
            CirTypeDistance(1), typeDistance(classifiers, target, typeE, idOfZ)
        )

        assertEquals(
            CirTypeDistance(-1), typeDistance(classifiers, target, typeZ, idOfE)
        )
    }

    fun `test unreachable distance`() {
        assertUnreachable(CirTypeDistance.unreachable + CirTypeDistance.unreachable)
        assertUnreachable(CirTypeDistance.unreachable + 1)
        assertUnreachable(CirTypeDistance.unreachable.inc())
        assertUnreachable(CirTypeDistance.unreachable + CirTypeDistance(1))
        assertUnreachable(CirTypeDistance(1) + CirTypeDistance.unreachable)
        assertUnreachable(CirTypeDistance.unreachable - 1)
        assertUnreachable(CirTypeDistance.unreachable.dec())
        assertUnreachable(CirTypeDistance.unreachable - CirTypeDistance(1))
        assertUnreachable(CirTypeDistance(1) - CirTypeDistance.unreachable)
        assertEquals(CirTypeDistance(Int.MAX_VALUE), CirTypeDistance.unreachable)
    }

    fun `test penalty`() {
        assertEquals(1, CirTypeDistance(-1).penalty)
        assertEquals(2, CirTypeDistance(-2).penalty)
        assertEquals(Int.MIN_VALUE, CirTypeDistance(0).penalty)
        assertEquals(Int.MAX_VALUE, CirTypeDistance.unreachable.penalty)
        assertEquals(Int.MIN_VALUE + 1, CirTypeDistance(1).penalty)
        assertEquals(Int.MIN_VALUE + 2, CirTypeDistance(2).penalty)
        assertTrue(CirTypeDistance(-1).penalty > CirTypeDistance(1).penalty)
        assertTrue(CirTypeDistance(-1).penalty > CirTypeDistance(10).penalty)
        assertTrue(CirTypeDistance(2).penalty > CirTypeDistance(1).penalty)
        assertTrue(CirTypeDistance(-2).penalty > CirTypeDistance(-1).penalty)
        assertTrue(CirTypeDistance(1).penalty > CirTypeDistance(0).penalty)
        assertTrue(CirTypeDistance(-1).penalty > CirTypeDistance(0).penalty)
    }
}

private fun assertUnreachable(typeDistance: CirTypeDistance) {
    assertEquals(CirTypeDistance.unreachable, typeDistance)
    assertTrue(typeDistance.isNotReachable)
    assertFalse(typeDistance.isReachable)
    assertFalse(typeDistance.isPositive)
    assertFalse(typeDistance.isNegative)
    assertFalse(typeDistance.isZero)
}