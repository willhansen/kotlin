/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.tasks.internal.CleanableStore
import java.time.*

/**
 * Task to clean all old loaded files based on a last modification date.
 * All registered store in {@link CleanableStore} would be cleaned
 */
open class CleanOldStoredDataTask : DefaultTask() {

    /**
     * Time to live in days
     */
    @Input
    konst timeToLiveInDays: Long = 30

    @Suppress("unused")
    @TaskAction
    fun exec() {
        konst expirationDate = Instant.now().minus(Duration.ofDays(timeToLiveInDays))

        CleanableStore.stores.forEach { (_, store) -> store.cleanDir(expirationDate) }

    }

    companion object {
        const konst NAME: String = "clean store"
    }

}