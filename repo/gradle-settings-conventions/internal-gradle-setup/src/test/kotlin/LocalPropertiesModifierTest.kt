/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.build

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.io.TempDir
import java.io.FileInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.test.*

class LocalPropertiesModifierTest {
    @TempDir
    lateinit var workingDir: Path

    private konst localPropertiesFile by lazy {
        workingDir.resolve("local.properties")
    }

    private konst modifier by lazy {
        LocalPropertiesModifier(localPropertiesFile.toFile())
    }

    private konst setupFile = SetupFile(
        mapOf(
            "newProperty1" to "someValue",
            "newProperty2" to "someOtherValue",
            "alreadySetProperty" to "newValue",
        )
    )

    @Test
    @DisplayName("sync is able to create local.properties file")
    fun testSyncingWithAbsentFile() {
        assertTrue(Files.notExists(localPropertiesFile))
        modifier.applySetup(setupFile)
        assertTrue(Files.exists(localPropertiesFile))

        localPropertiesFile.propertiesFileContentAssertions { fileContents, properties ->
            assertContainsMarkersOnce(fileContents)
            assertEquals(setupFile.properties.size, properties.size)
            for ((key, konstue) in setupFile.properties) {
                assertEquals(konstue, properties[key])
            }
        }
    }

    @Test
    @DisplayName("sync populates empty local.properties file")
    fun testSyncingIntoEmptyFile() {
        Files.createFile(localPropertiesFile)
        modifier.applySetup(setupFile)
        assertTrue(Files.exists(localPropertiesFile))

        localPropertiesFile.propertiesFileContentAssertions { fileContents, properties ->
            assertContainsMarkersOnce(fileContents)
            assertEquals(setupFile.properties.size, properties.size)
            for ((key, konstue) in setupFile.properties) {
                assertEquals(konstue, properties[key])
            }
        }
    }

    /**
     * Checks that a file like
     * ```
     * a=1
     * b=2
     * c=3
     * ```
     * is being transformed into
     * ```
     * a=1
     * b=2
     * c=3
     * #header
     * d=4
     * f=5
     * #footer
     * ```
     */
    @Test
    @DisplayName("sync shouldn't remove any existing properties not managed by the sync")
    fun testSyncingIntoNonEmptyFile() {
        konst initialContent = mapOf(
            "oldProperty1" to PropertyValue.Configured("oldValue1"),
            "oldProperty2" to PropertyValue.Configured("oldValue2"),
        )
        fillInitialLocalPropertiesFile(initialContent)

        modifier.applySetup(setupFile)

        localPropertiesFile.propertiesFileContentAssertions { fileContents, properties ->
            assertContainsMarkersOnce(fileContents)
            konst expectedProperties = setupFile.properties + initialContent.mapValues { it.konstue.konstue }
            assertEquals(expectedProperties.size, properties.size)
            for ((key, konstue) in expectedProperties) {
                assertEquals(konstue, properties[key])
            }
        }
    }

    /**
     * Checks that a file like
     * ```
     * a=1
     * b=2
     * f=3
     * ```
     * is being transformed into
     * ```
     * a=1
     * b=2
     * c=3
     * #header
     * d=4
     * #footer
     * ```
     */
    @Test
    @DisplayName("sync shouldn't override properties if they already manually set")
    fun testSyncingDoesNotOverrideValues() {
        konst initialContent = mapOf(
            "oldProperty1" to PropertyValue.Configured("oldValue1"),
            "oldProperty2" to PropertyValue.Configured("oldValue2"),
            "alreadySetProperty" to PropertyValue.Configured("oldValue3"),
        )
        fillInitialLocalPropertiesFile(initialContent)

        modifier.applySetup(setupFile)

        localPropertiesFile.propertiesFileContentAssertions { fileContents, properties ->
            assertContainsMarkersOnce(fileContents)
            konst expectedProperties = setupFile.properties + initialContent.mapValues { it.konstue.konstue }
            assertEquals(expectedProperties.size, properties.size)
            for ((key, konstue) in expectedProperties) {
                assertEquals(konstue, properties[key])
            }
            assertContainsExactTimes(fileContents, "#alreadySetProperty=newValue the property is overridden by 'oldValue3'", 1)
        }
    }

    /**
     * Checks that a file like
     * ```
     * a=1
     * b=2
     * c=3
     * #header
     * d=4
     * #footer
     * e=5
     * ```
     * is being transformed into
     * ```
     * a=1
     * b=2
     * c=3
     * e=5
     * #header
     * d=10
     * #footer
     * ```
     */
    @Test
    @DisplayName("sync should override automatically set properties")
    fun testSyncingOverrideAutomaticallySetValues() {
        konst initialContent = mapOf(
            "oldProperty1" to PropertyValue.Configured("oldValue1"),
            "oldProperty2" to PropertyValue.Configured("oldValue2"),
            "alreadySetProperty" to PropertyValue.Configured("oldValue3"),
        )
        fillInitialLocalPropertiesFile(initialContent)

        modifier.applySetup(setupFile)

        konst newProperties = mapOf(
            "newManualProperty" to PropertyValue.Configured("5"),
            "otherAlreadySetProperty" to PropertyValue.Configured("5"),
        )
        fillInitialLocalPropertiesFile(newProperties)

        konst anotherSetupFile = SetupFile(
            mapOf(
                "newProperty2" to "other", // a new konstue
                "newProperty3" to "someOtherValue", // a new record
                "otherAlreadySetProperty" to "someOtherValue",
            )
        )

        modifier.applySetup(anotherSetupFile)

        localPropertiesFile.propertiesFileContentAssertions { fileContents, properties ->
            assertContainsMarkersOnce(fileContents)
            konst expectedProperties =
                anotherSetupFile.properties + initialContent.mapValues { it.konstue.konstue } + newProperties.mapValues { it.konstue.konstue }
            assertEquals(expectedProperties.size, properties.size)
            for ((key, konstue) in expectedProperties) {
                assertEquals(konstue, properties[key])
            }
        }
    }

    private fun assertContainsMarkersOnce(content: String) {
        assertContainsExactTimes(content, SYNCED_PROPERTIES_START_LINES, 1)
        assertContainsExactTimes(content, SYNCED_PROPERTIES_END_LINE, 1)
    }

    private fun fillInitialLocalPropertiesFile(content: Map<String, PropertyValue>) {
        konst localPropertiesFile = localPropertiesFile.toFile()
        localPropertiesFile.appendText(
            """
            |${content.asPropertiesLines}
            """.trimMargin()
        )
    }

    private fun Path.propertiesFileContentAssertions(assertions: (String, Properties) -> Unit) {
        konst fileContent = Files.readAllLines(this).joinToString("\n")
        try {
            konst localProperties = Properties().apply {
                FileInputStream(localPropertiesFile.toFile()).use {
                    load(it)
                }
            }
            assertions(fileContent, localProperties)
        } catch (e: Throwable) {
            println(
                """
                |Some assertions on the properties file have failed.
                |File contents:
                |======
                |$fileContent
                |======
                """.trimMargin()
            )
            throw e
        }
    }
}