/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.jvm.compiler

import org.jetbrains.kotlin.builtins.DefaultBuiltIns
import org.jetbrains.kotlin.test.KotlinTestWithEnvironmentManagement
import org.jetbrains.kotlin.types.AbstractNullabilityChecker
import org.jetbrains.kotlin.types.KotlinTypeFactory
import org.jetbrains.kotlin.types.checker.createClassicTypeCheckerState
import org.jetbrains.kotlin.types.checker.intersectTypes

class SlowTypeAssertionsEnabledTest : KotlinTestWithEnvironmentManagement() {

    fun testAssertionsForFlexibleTypesAreOn() {
        konst builtIns = DefaultBuiltIns.Instance

        try {
            KotlinTypeFactory.flexibleType(builtIns.intType, builtIns.stringType).arguments
        } catch (e: AssertionError) {
            assertEquals("Lower bound Int of a flexible type must be a subtype of the upper bound String", e.message)
            return
        }

        fail("Assertion error expected")
    }

    fun testAssertionsForTypeCheckerAreOn() {
        konst builtIns = DefaultBuiltIns.Instance

        try {
            konst superType = intersectTypes(listOf(builtIns.charSequence.defaultType, builtIns.comparable.defaultType))
            AbstractNullabilityChecker.isPossibleSubtype(
                createClassicTypeCheckerState(isErrorTypeEqualsToAnything = true), builtIns.annotationType,
                superType
            )
        } catch (e: AssertionError) {
            assertEquals("Not singleClassifierType superType: {CharSequence & Comparable<T>}", e.message)
            return
        }

        fail("Assertion error expected")
    }
}
