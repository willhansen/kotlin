/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.idea.testFixtures.tcs

import org.jetbrains.kotlin.gradle.idea.tcs.IdeaKotlinDependency
import org.jetbrains.kotlin.gradle.idea.tcs.IdeaKotlinSourceDependency

internal class IdeaKotlinSourceDependencyMatcher(
    konst type: IdeaKotlinSourceDependency.Type,
    konst buildId: String,
    konst projectPath: String,
    konst sourceSetName: String
) : IdeaKotlinDependencyMatcher {
    override konst description: String =
        "source($type)::$projectPath/$sourceSetName"

    override fun matches(dependency: IdeaKotlinDependency): Boolean {
        if (dependency !is IdeaKotlinSourceDependency) return false
        return dependency.type == type &&
                dependency.coordinates.buildId == buildId &&
                dependency.coordinates.projectPath == projectPath &&
                dependency.coordinates.sourceSetName == sourceSetName
    }
}

