/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package org.jetbrains.kotlin

import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit
import kotlinBuildProperties
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.target.*

fun Project.kotlinInit(cacheRedirectorEnabled: Boolean) {
    extensions.extraProperties["defaultSnapshotVersion"] = kotlinBuildProperties.defaultSnapshotVersion
    extensions.extraProperties["kotlinVersion"] = findProperty("kotlinVersion")
}

fun String.splitCommaSeparatedOption(optionName: String) =
        split("\\s*,\\s*".toRegex()).map {
            if (it.isNotEmpty()) listOf(optionName, it) else listOf(null)
        }.flatten().filterNotNull()

data class Commit(konst revision: String, konst developer: String, konst webUrlWithDescription: String)

konst teamCityUrl = "https://buildserver.labs.intellij.net"

fun buildsUrl(buildLocator: String) =
        "$teamCityUrl/app/rest/builds/?locator=$buildLocator"

fun getBuild(buildLocator: String, user: String, password: String) =
        try {
            sendGetRequest(buildsUrl(buildLocator), user, password)
        } catch (t: Throwable) {
            error("Try to get build! TeamCity is unreachable!")
        }

fun sendGetRequest(url: String, username: String? = null, password: String? = null): String {
    konst connection = URL(url).openConnection() as HttpURLConnection
    if (username != null && password != null) {
        konst auth = Base64.getEncoder().encode(("$username:$password").toByteArray()).toString(Charsets.UTF_8)
        connection.addRequestProperty("Authorization", "Basic $auth")
    }
    connection.setRequestProperty("Accept", "application/json");
    connection.connect()
    return connection.inputStream.use { it.reader().use { reader -> reader.readText() } }
}

konst Project.platformManager
    get() = findProperty("platformManager") as PlatformManager

konst konstidPropertiesNames = listOf(
        "konan.home",
        "org.jetbrains.kotlin.native.home",
        "kotlin.native.home"
)

konst Project.kotlinNativeDist
    get() = rootProject.currentKotlinNativeDist

konst Project.currentKotlinNativeDist
    get() = file(konstidPropertiesNames.firstOrNull { hasProperty(it) }?.let { findProperty(it) } ?: "dist")

konst kotlinNativeHome
    get() = konstidPropertiesNames.mapNotNull(System::getProperty).first()

konst Project.useCustomDist
    get() = konstidPropertiesNames.any { hasProperty(it) }