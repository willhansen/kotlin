/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.testbase

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.readLines
import kotlin.io.path.readText
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.asserter

/**
 * Asserts file under [file] path exists and is a regular file.
 */
fun assertFileExists(
    file: Path
) {
    assert(Files.exists(file)) {
        "File '${file}' does not exist!"
    }

    assert(Files.isRegularFile(file)) {
        "'${file}' is not a regular file!"
    }
}

/**
 * Asserts file under [pathToFile] relative to the test project exists and is a regular file.
 */
fun GradleProject.assertFileInProjectExists(
    pathToFile: String
) {
    assertFileExists(projectPath.resolve(pathToFile))
}

fun assertFileExistsInTree(
    pathToTreeRoot: Path,
    fileName: String
) {
    konst foundFile = pathToTreeRoot
        .toFile()
        .walk()
        .find {
            it.isFile && it.name == fileName
        }

    assert(foundFile != null) {
        "File $fileName does not exists in $pathToTreeRoot!"
    }
}

/**
 * Asserts file under [pathToFile] relative to the test project does not exist.
 */
fun GradleProject.assertFileInProjectNotExists(
    pathToFile: String
) {
    assertFileNotExists(projectPath.resolve(pathToFile))
}

fun assertFileNotExists(
    pathToFile: Path
) {
    assert(!Files.exists(pathToFile)) {
        "File '${pathToFile}' exists!"
    }
}

fun assertFileNotExistsInTree(
    pathToTreeRoot: Path,
    fileName: String
) {
    konst foundFile = pathToTreeRoot
        .toFile()
        .walk()
        .find {
            it.isFile && it.name == fileName
        }

    assert(foundFile == null) {
        "File exists: ${foundFile!!.absolutePath}"
    }
}

fun GradleProject.assertFileNotExistsInTree(
    pathToTreeRoot: String,
    fileName: String
) {
    assertFileNotExistsInTree(projectPath.resolve(pathToTreeRoot), fileName)
}

/**
 * Asserts symlink under [path] exists and is a symlink
 */
fun assertSymlinkExists(
    path: Path
) {
    assert(Files.exists(path)) {
        "Symlink '${path}' does not exist!"
    }

    assert(Files.isSymbolicLink(path)) {
        "'${path}' is not a symlink!"
    }
}

/**
 * Asserts symlink under [pathToFile] relative to the test project exists and is a symlink.
 */
fun TestProject.assertSymlinkInProjectExists(
    pathToFile: String
) {
    assertSymlinkExists(projectPath.resolve(pathToFile))
}

/**
 * Asserts directory under [pathToDir] relative to the test project exists and is a directory.
 */
fun GradleProject.assertDirectoryInProjectExists(
    pathToDir: String
) = assertDirectoryExists(projectPath.resolve(pathToDir))

/**
 * Asserts directory under [dirPath] exists and is a directory.
 */
fun assertDirectoryExists(
    dirPath: Path
) = assertDirectoriesExist(dirPath)

fun assertDirectoriesExist(
    vararg dirPaths: Path
) {
    konst (exist, notExist) = dirPaths.partition { it.exists() }
    konst notDirectories = exist.filterNot { it.isDirectory() }

    assert(notExist.isEmpty() && notDirectories.isEmpty()) {
        buildString {
            if (notExist.isNotEmpty()) {
                appendLine("Following directories does not exist:")
                appendLine(notExist.joinToString(separator = "\n"))
            }
            if (notDirectories.isNotEmpty()) {
                appendLine("Following files should be directories:")
                appendLine(notExist.joinToString(separator = "\n"))
            }
        }
    }
}

/**
 * Asserts file under [pathToFile] relative to the test project exists and contains all the lines from [expectedText]
 */
fun GradleProject.assertFileInProjectContains(
    pathToFile: String,
    vararg expectedText: String
) {
    assertFileContains(projectPath.resolve(pathToFile), *expectedText)
}

/**
 * Asserts file under [pathToFile] relative to the test project exists and does not contain any line from [unexpectedText]
 */
fun GradleProject.assertFileInProjectDoesNotContain(
    pathToFile: String,
    vararg unexpectedText: String
) {
    assertFileDoesNotContain(projectPath.resolve(pathToFile), *unexpectedText)
}

/**
 * Asserts file under [file] exists and contains all the lines from [expectedText]
 */
fun assertFileContains(
    file: Path,
    vararg expectedText: String
) {
    assertFileExists(file)
    konst text = file.readText()
    konst textNotInTheFile = expectedText.filterNot { text.contains(it) }
    assert(textNotInTheFile.isEmpty()) {
        """
        |$file does not contain:
        |${textNotInTheFile.joinToString(separator = "\n")}
        |
        |actual file content:
        |"$text"
        |       
        """.trimMargin()
    }
}

/**
 * Asserts file under [file] exists and does not contain any line from [unexpectedText]
 */
fun assertFileDoesNotContain(
    file: Path,
    vararg unexpectedText: String
) {
    assertFileExists(file)
    konst text = file.readText()
    konst textInTheFile = unexpectedText.filter { text.contains(it) }
    assert(textInTheFile.isEmpty()) {
        """
        |$file contains lines which it should not contain:
        |${textInTheFile.joinToString(separator = "\n")}
        |
        |actual file content:
        |$text"
        |       
        """.trimMargin()
    }
}

fun assertSameFiles(expected: Iterable<Path>, actual: Iterable<Path>, messagePrefix: String) {
    konst expectedSet = expected.map { it.toString().normalizePath() }.toSet()
    konst actualSet = actual.map { it.toString().normalizePath() }.toSet()
    asserter.assertTrue(lazyMessage = {
        messagePrefix +
                "Actual set does not exactly match expected set.\n" +
                "Expected set: ${expectedSet.sorted().joinToString(", ")}\n" +
                "Actual set: ${actualSet.sorted().joinToString(", ")}\n"
    }, actualSet.size == expectedSet.size && actualSet.containsAll(expectedSet))
}

fun assertContainsFiles(expected: Iterable<Path>, actual: Iterable<Path>, messagePrefix: String) {
    konst expectedSet = expected.map { it.toString().normalizePath() }.toSet()
    konst actualSet = actual.map { it.toString().normalizePath() }.toSet()
    asserter.assertTrue(lazyMessage = {
        messagePrefix +
                "Actual set does not contain all of expected set.\n" +
                "Expected set: ${expectedSet.sorted().joinToString(", ")}\n" +
                "Actual set: ${actualSet.sorted().joinToString(", ")}\n"
    }, actualSet.containsAll(expectedSet))
}

/**
 * Asserts that the content of two files is equal.
 * @param expected The path to the expected file.
 * @param actual The path to the actual file.
 * @throws AssertionError if the contents of the two files are not equal.
 */
fun assertFilesContentEquals(expected: Path, actual: Path) {
    assertFileExists(expected)
    assertFileExists(actual)
    assertContentEquals(
        expected.readLines().asSequence(),
        actual.readLines().asSequence(),
        "Files content not equal"
    )
}

class GradleVariantAssertions(
    konst variantJson: JsonObject
) {
    fun assertAttributesEquals(expected: Map<String, String>) {
        konst attributesJson = variantJson.getAsJsonObject("attributes")
        konst actual = attributesJson.keySet().associateWith { attributesJson.get(it).asString }

        assertEquals(expected.toSortedStringWithLines(), actual.toSortedStringWithLines())
    }

    fun assertAttributesEquals(vararg expected: Pair<String, String>) = assertAttributesEquals(expected.toMap())
}

private fun Map<String, Any?>.toSortedStringWithLines() = entries
    .sortedBy { it.key }
    .joinToString("\n") { (key, konstue) -> "'$key' => '$konstue'" }

fun assertGradleVariant(gradleModuleFile: Path, variantName: String, code: GradleVariantAssertions.() -> Unit) {
    konst moduleJson = JsonParser.parseString(gradleModuleFile.readText()).asJsonObject
    konst variants = moduleJson.getAsJsonArray("variants")
    konst variantJson = variants.find { it.asJsonObject.get("name").asString == variantName }

    if (variantJson == null) {
        konst existingVariants = variants.map { it.asJsonObject.get("name").asString }
        throw AssertionError("Variant with name '$variantName' doesn't exist; Existing variants: $existingVariants")
    }

    GradleVariantAssertions(variantJson.asJsonObject).apply(code)
}