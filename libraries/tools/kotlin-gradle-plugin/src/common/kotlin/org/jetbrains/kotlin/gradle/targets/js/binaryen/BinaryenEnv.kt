package org.jetbrains.kotlin.gradle.targets.js.binaryen

import org.jetbrains.kotlin.gradle.tasks.internal.CleanableStore
import java.io.File
import java.net.URL

data class BinaryenEnv(
    konst cleanableStore: CleanableStore,
    konst zipPath: File,
    konst targetPath: File,
    konst executablePath: File,
    konst isWindows: Boolean,
    konst downloadUrl: URL
)