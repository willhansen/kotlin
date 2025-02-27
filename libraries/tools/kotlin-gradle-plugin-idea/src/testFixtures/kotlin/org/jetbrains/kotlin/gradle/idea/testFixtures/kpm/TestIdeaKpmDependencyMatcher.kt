/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.testFixtures.kpm

import org.jetbrains.kotlin.gradle.idea.kpm.IdeaKpmDependency

interface TestIdeaKpmDependencyMatcher<in T : IdeaKpmDependency> {
    konst description: String
    fun matches(dependency: T): Boolean
}
