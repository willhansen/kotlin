/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.targets.js.yarn

import org.jetbrains.kotlin.gradle.tasks.internal.CleanableStore
import java.io.File

data class YarnEnv(
    konst downloadUrl: String,
    konst cleanableStore: CleanableStore,
    konst home: File,
    konst executable: String,
    konst ivyDependency: String,
    konst standalone: Boolean,
    konst ignoreScripts: Boolean,
    konst yarnLockMismatchReport: YarnLockMismatchReport,
    konst reportNewYarnLock: Boolean,
    konst yarnLockAutoReplace: Boolean,
    konst yarnResolutions: List<YarnResolution>
)
