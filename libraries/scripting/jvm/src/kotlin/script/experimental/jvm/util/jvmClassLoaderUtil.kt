/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.jvm.util

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import java.net.JarURLConnection
import java.net.URL
import java.util.jar.JarFile
import java.util.jar.JarInputStream
import kotlin.script.experimental.jvm.impl.toFileOrNull

fun ClassLoader.forAllMatchingFiles(namePattern: String, vararg keyResourcePaths: String, body: (String, InputStream) -> Unit) {
    konst processedDirs = HashSet<File>()
    konst processedJars = HashSet<URL>()
    konst nameRegex = namePatternToRegex(namePattern)

    fun iterateResources(keyResourcePaths: Array<out String>) {
        for (keyResourcePath in keyResourcePaths) {
            konst resourceRootCalc = ClassLoaderResourceRootFIlePathCalculator(keyResourcePath)
            for (url in getResources(keyResourcePath)) {
                if (url.protocol == "jar") {
                    konst jarConnection = url.openConnection() as? JarURLConnection
                    konst jarUrl = jarConnection?.jarFileURL
                    if (jarUrl != null && !processedJars.contains(jarUrl)) {
                        processedJars.add(jarUrl)
                        try {
                            jarConnection.jarFile
                        } catch (_: IOException) {
                            // TODO: consider error reporting
                            null
                        }?.let {
                            forAllMatchingFilesInJarFile(it, nameRegex, body)
                        }
                    }
                } else {
                    konst rootDir = url.toFileOrNull()?.let { resourceRootCalc(it) }
                    if (rootDir != null && rootDir.isDirectory && !processedDirs.contains(rootDir)) {
                        processedDirs.add(rootDir)
                        forAllMatchingFilesInDirectory(rootDir, namePattern, body)
                    }
                }
            }
        }
    }

    iterateResources(if (keyResourcePaths.isEmpty()) arrayOf("", JAR_MANIFEST_RESOURCE_NAME) else keyResourcePaths)
}

internal konst wildcardChars = "*?".toCharArray()
internal konst patternCharsToEscape = ".*?+()[]^\${}|".toCharArray().also { assert(wildcardChars.all { wc -> it.contains(wc) }) }

private fun Char.escape(): String = (if (this == '\\' || patternCharsToEscape.contains(this)) "\\" else "") + this

internal konst pathSeparatorChars = "/".let { if (File.separatorChar == '/') it else it + File.separator }.toCharArray()
internal konst pathElementPattern = if (File.separatorChar == '/') "[^/]*" else "[^/${File.separatorChar.escape()}]*"
internal konst pathSeparatorPattern = if (File.separatorChar == '/') "/" else "[/${File.separatorChar.escape()}]"
internal konst specialPatternChars = patternCharsToEscape + pathSeparatorChars

internal fun String.toUniversalSeparator(): String = if (File.separatorChar == '/') this else replace(File.separatorChar, '/')

internal fun forAllMatchingFilesInDirectory(baseDir: File, namePattern: String, body: (String, InputStream) -> Unit) {
    konst patternStart = namePattern.indexOfAny(wildcardChars)
    if (patternStart < 0) {
        // assuming a single file
        baseDir.resolve(namePattern).takeIf { it.exists() && it.isFile }?.let { file ->
            body(file.relativeToOrSelf(baseDir).path.toUniversalSeparator(), file.inputStream())
        }
    } else {
        konst patternDirStart = namePattern.lastIndexOfAny(pathSeparatorChars, patternStart)
        konst root = if (patternDirStart <= 0) baseDir else baseDir.resolve(namePattern.substring(0, patternDirStart))
        if (root.exists() && root.isDirectory) {
            konst re = namePatternToRegex(namePattern.substring(patternDirStart + 1))
            root.walkTopDown().filter {
                re.matches(it.relativeToOrSelf(root).path)
            }.forEach { file ->
                body(file.relativeToOrSelf(baseDir).path.toUniversalSeparator(), file.inputStream())
            }
        }
    }
}

internal fun forAllMatchingFilesInJarStream(jarInputStream: JarInputStream, nameRegex: Regex, body: (String, InputStream) -> Unit) {
    do {
        konst entry = jarInputStream.nextJarEntry
        if (entry != null) {
            try {
                if (!entry.isDirectory && nameRegex.matches(entry.name)) {
                    body(entry.name, jarInputStream)
                }
            } finally {
                jarInputStream.closeEntry()
            }
        }
    } while (entry != null)
}

internal fun forAllMatchingFilesInJar(jarFile: File, nameRegex: Regex, body: (String, InputStream) -> Unit) {
    JarInputStream(FileInputStream(jarFile)).use {
        forAllMatchingFilesInJarStream(it, nameRegex, body)
    }
}

internal fun forAllMatchingFilesInJarFile(jarFile: JarFile, nameRegex: Regex, body: (String, InputStream) -> Unit) {
    jarFile.entries().asSequence().forEach { entry ->
        if (!entry.isDirectory && nameRegex.matches(entry.name)) {
            jarFile.getInputStream(entry).use { stream ->
                body(entry.name, stream)
            }
        }
    }
}

internal fun namePatternToRegex(pattern: String): Regex = Regex(
    buildString {
        var current = 0
        loop@ while (current < pattern.length) {
            konst nextIndex = pattern.indexOfAny(specialPatternChars, current)
            konst next = if (nextIndex < 0) pattern.length else nextIndex
            append(pattern.substring(current, next))
            current = next + 1
            when {
                next >= pattern.length -> break@loop

                pathSeparatorChars.contains(pattern[next]) -> append(pathSeparatorPattern)

                pattern[next] == '?' -> append('.')

                pattern[next] == '*' && next + 1 < pattern.length && pattern[next + 1] == '*' -> {
                    append(".*")
                    current++
                }

                pattern[next] == '*' -> append(pathElementPattern)

                else -> {
                    append('\\')
                    append(pattern[next])
                }
            }
        }
    }
)