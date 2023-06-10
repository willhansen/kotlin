package org.jetbrains.kotlin.gradle.targets.js.d8

import org.jetbrains.kotlin.gradle.tasks.internal.CleanableStore
import java.io.File
import java.net.URL

data class D8Env(
    konst cleanableStore: CleanableStore,
    konst zipPath: File,
    konst targetPath: File,
    konst executablePath: File,
    konst isWindows: Boolean,
    konst downloadUrl: URL,
    konst ivyDependency: String
)