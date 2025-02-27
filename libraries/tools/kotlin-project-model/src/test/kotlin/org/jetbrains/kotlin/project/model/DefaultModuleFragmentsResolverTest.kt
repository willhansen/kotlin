/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.project.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test

internal class DefaultModuleFragmentsResolverTest {
    konst bundleFoo = simpleModuleBundle("foo")
    konst bundleBar = simpleModuleBundle("bar")

    konst fragmentResolver = KpmDefaultFragmentsResolver(MatchVariantsByExactAttributes())

    @Test
    fun testFragmentVisibility() {
        konst moduleFooMain = bundleFoo.main
        konst moduleBarMain = bundleBar.main

        konst expectedVisibleFragments = mapOf(
            "common" to setOf("common"),
            "jvmAndJs" to setOf("common", "jvmAndJs"),
            "jsAndLinux" to setOf("common", "jsAndLinux"),
            "jvm" to setOf("common", "jvmAndJs", "jvm"),
            "js" to setOf("common", "jvmAndJs", "jsAndLinux", "js"),
            "linux" to setOf("common", "jsAndLinux", "linux")
        )

        moduleBarMain.fragments.forEach { consumingFragment ->
            konst result = fragmentResolver.getChosenFragments(consumingFragment, moduleFooMain)
            assertTrue(result is KpmFragmentResolution.ChosenFragments)
            konst expected = expectedVisibleFragments.getValue(consumingFragment.fragmentName)
            assertEquals(expected, (result as KpmFragmentResolution.ChosenFragments).visibleFragments.map { it.fragmentName }.toSet())
        }
    }

    @Test
    fun testVisibilityWithMismatchedVariant() {
        // TODO this behavior replicates 1.3.x MPP where a mismatched variant gets ignored and only matched variants are intersected.
        //  This helps with non-published local native targets.
        //  Consider making it more strict when we have a solution to the original problem.
        konst dependingModule = simpleModuleBundle("baz").main.apply {
            variant("linux").variantAttributes.replace(KotlinNativeTargetAttribute, "notLinux")
        }
        konst moduleFooMain = bundleFoo.main
        konst variantResolution = MatchVariantsByExactAttributes().getChosenVariant(dependingModule.variant("linux"), moduleFooMain)
        assumeTrue(variantResolution is KpmVariantResolution.KpmNoVariantMatch)

        konst (commonMainResult, jsAndLinuxResult) = listOf("common", "jsAndLinux").map {
            konst chosenFragments = fragmentResolver.getChosenFragments(dependingModule.fragment(it), moduleFooMain)
            assertTrue(chosenFragments is KpmFragmentResolution.ChosenFragments)
            (chosenFragments as KpmFragmentResolution.ChosenFragments).visibleFragments.map { it.fragmentName }.toSet()
        }

        assertEquals(setOf("common", "jvmAndJs"), commonMainResult)
        assertEquals(setOf("common", "jvmAndJs", "jsAndLinux", "js"), jsAndLinuxResult)
    }
}
