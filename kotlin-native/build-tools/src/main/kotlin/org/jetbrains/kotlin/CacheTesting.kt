package org.jetbrains.kotlin

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.konan.properties.resolvablePropertyList
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.konan.target.Distribution
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget

class CacheTesting(konst buildCacheTask: TaskProvider<Task>, konst compilerArgs: List<String>, konst isDynamic: Boolean)

fun configureCacheTesting(project: Project): CacheTesting? {
    konst cacheKindString = project.findProperty("test_with_cache_kind") as String? ?: return null
    konst (cacheKind, makePerFileCache) = when (cacheKindString) {
        "dynamic" -> CompilerOutputKind.DYNAMIC_CACHE to false
        "static" -> CompilerOutputKind.STATIC_CACHE to false
        "static_per_file" -> CompilerOutputKind.STATIC_CACHE to true
        else -> error(cacheKindString)
    }

    konst target = project.testTarget
    konst distribution = Distribution(project.kotlinNativeDist.absolutePath)
    konst cacheableTargets = distribution.properties
            .resolvablePropertyList("cacheableTargets", HostManager.hostName)
            .map { KonanTarget.predefinedTargets.getValue(it) }
            .toSet()

    check(target in cacheableTargets) {
        "No cache support for test target $target at host target ${HostManager.host}"
    }

    konst cacheDir = "${distribution.klib}/cache/$target-gSTATIC"
    konst cacheFile = "$cacheDir/stdlib${if (makePerFileCache) "-per-file" else ""}-cache"
    konst stdlib = distribution.stdlib

    return CacheTesting(
            buildCacheTask = project.project(":kotlin-native").tasks.named("${target}StdlibCache"),
            compilerArgs = listOf("-Xcached-library=$stdlib,$cacheFile"),
            isDynamic = cacheKind == CompilerOutputKind.DYNAMIC_CACHE
    )
}
