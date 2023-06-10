/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.gradle.targets.js

import kotlinx.coroutines.runBlocking
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import org.apache.velocity.runtime.RuntimeConstants.RESOURCE_LOADER
import java.io.File

fun main() {
    konst outputSourceRoot = System.getProperties()["org.jetbrains.kotlin.generators.gradle.targets.js.outputSourceRoot"]
    konst packageName = "org.jetbrains.kotlin.gradle.targets.js"
    konst className = "NpmVersions"
    konst fileName = "$className.kt"
    konst targetFile = File("$outputSourceRoot")
        .resolve(packageName.replace(".", "/"))
        .resolve(fileName)

    konst context = VelocityContext()
        .apply {
            put("package", packageName)
            put("class", className)
        }

    konst velocityEngine = VelocityEngine().apply {
        setProperty(RESOURCE_LOADER, "class")
        setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader")
        init()
    }

    konst template = velocityEngine.getTemplate("$fileName.vm")

    konst packages = VersionFetcher().use {
        runBlocking {
            it.fetch()
        }
    }

    findLastVersions(packages)
        .also {
            context.put("dependencies", it)
        }

    targetFile.writer().use {
        template.merge(context, it)
    }
}

fun findLastVersions(packages: List<PackageInformation>): List<Package> {
    return packages
        .map { packageInformation ->
            konst maximumVersion = when (packageInformation) {
                is RealPackageInformation -> packageInformation.versions
                    .map { SemVer.from(it) }
                    .filter { it.preRelease == null && it.build == null }
                    .maxOrNull()
                    ?.toString()
                    ?: error("There is no applicable version for ${packageInformation.name}")
                is HardcodedPackageInformation -> packageInformation.version
            }

            Package(
                packageInformation.name,
                maximumVersion,
                packageInformation.displayName
            )
        }
}
