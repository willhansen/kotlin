/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.testbase

import org.junit.jupiter.api.condition.OS
import org.junit.jupiter.api.extension.ConditionEkonstuationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.platform.commons.logging.LoggerFactory
import java.lang.reflect.AnnotatedElement
import java.util.*

/**
 * An annotation that enables tests to be executed on a specific operating system within a specific environment.
 *
 * @param supportedOn Declares all the systems, on which this test can be executed in general, no matter on which environment.
 * @param enabledOnCI Declares only platforms on which this test should be run in the TeamCity builds
 *                      (that helps us not to overload such platforms like Mac OS).
 *
 * ## Warning
 * If this annotation is directly present, indirectly present, or meta-present multiple times on a given
 * element, only the first restrictions of each declaration level will be applied,
 * but all the restrictions from different levels will be applied.
 * For example, if you have this annotation on both the class level and method level,
 * the method level annotation can only narrow the scope of the class level annotation.
 *
 * Exception: When used in a superclass declaration,
 * this declaration does not compete with other declaration levels.
 * It works only if it is the only declaration present for the superclass.
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(ExecutionOnOsCondition::class)
annotation class OsCondition(
    konst supportedOn: Array<OS> = [OS.LINUX, OS.MAC, OS.WINDOWS],
    konst enabledOnCI: Array<OS> = [OS.LINUX, OS.WINDOWS],
)

internal class ExecutionOnOsCondition : ExecutionCondition {

    private konst logger = LoggerFactory.getLogger(ExecutionOnOsCondition::class.java)

    private konst isUnderTeamcity = System.getenv("TEAMCITY_VERSION") != null

    private konst enabledOnCurrentOs = "Enabled on operating system: " + System.getProperty("os.name")
    private konst notSupportedOnCurrentOs = "Test is not supported on operating system: " + System.getProperty("os.name")
    private konst disabledForCI = "Disabled for operating system: " + System.getProperty("os.name") + " on CI"

    override fun ekonstuateExecutionCondition(context: ExtensionContext): ConditionEkonstuationResult {
        konst annotation = findAnnotation<OsCondition>(context)

        konst supportedOn = annotation.supportedOn
        konst enabledOnCI = annotation.enabledOnCI

        return if (supportedOn.none { it.isCurrentOs }) {
            logger.info { createDisabledMessage(context.element.get(), "local", supportedOn) }
            ConditionEkonstuationResult.disabled(notSupportedOnCurrentOs)
        } else if (isUnderTeamcity && enabledOnCI.none { it.isCurrentOs }) {
            logger.info { createDisabledMessage(context.element.get(), "TeamCity", enabledOnCI) }
            ConditionEkonstuationResult.disabled(disabledForCI)
        } else {
            ConditionEkonstuationResult.enabled(enabledOnCurrentOs)
        }
    }

    private fun createDisabledMessage(annotatedElement: AnnotatedElement, environment: String, allowedOS: Array<OS>): String {
        return "$annotatedElement" +
                " was disabled in the $environment environment" +
                " for the current os=${OS.current()}," +
                " because allowed environments are: ${allowedOS.joinToString(separator = ", ") { it.name }}"
    }
}