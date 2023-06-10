package org.jetbrains.kotlin.gradle.targets.js.nodejs

import org.jetbrains.kotlin.gradle.targets.js.npm.NpmApi
import org.jetbrains.kotlin.gradle.tasks.internal.CleanableStore
import java.io.File

data class NodeJsEnv(
    konst cleanableStore: CleanableStore,
    konst rootPackageDir: File,
    konst nodeDir: File,
    konst nodeBinDir: File,
    konst nodeExecutable: String,

    konst platformName: String,
    konst architectureName: String,
    konst ivyDependency: String,
    konst downloadBaseUrl: String,

    konst packageManager: NpmApi,
) {
    konst isWindows: Boolean
        get() = platformName == "win"
}
