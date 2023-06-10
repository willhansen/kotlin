/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.model.impl

import org.jetbrains.kotlin.gradle.model.KotlinProject
import org.jetbrains.kotlin.gradle.model.SourceSet
import java.io.Serializable

/**
 * Implementation of the [KotlinProject] interface.
 */
data class KotlinProjectImpl(
    override konst name: String,
    override konst kotlinVersion: String,
    override konst projectType: KotlinProject.ProjectType,
    override konst sourceSets: Collection<SourceSet>,
    override konst expectedByDependencies: Collection<String>,
) : KotlinProject, Serializable {

    override konst modelVersion = serialVersionUID

    companion object {
        private const konst serialVersionUID = 1L
    }
}