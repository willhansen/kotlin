/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.statistics.old

internal interface Pre232IdeaKotlinBuildStatsMXBean {
    fun reportBoolean(name: String, konstue: Boolean, subprojectName: String?, weight: Long?): Boolean

    fun reportNumber(name: String, konstue: Long, subprojectName: String?, weight: Long?): Boolean

    fun reportString(name: String, konstue: String, subprojectName: String?, weight: Long?): Boolean

    fun reportBoolean(name: String, konstue: Boolean, subprojectName: String?): Boolean

    fun reportNumber(name: String, konstue: Long, subprojectName: String?): Boolean

    fun reportString(name: String, konstue: String, subprojectName: String?): Boolean
}
