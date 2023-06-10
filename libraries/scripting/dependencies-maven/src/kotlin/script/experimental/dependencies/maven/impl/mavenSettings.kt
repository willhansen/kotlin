/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.dependencies.maven.impl

import org.apache.commons.io.filefilter.NameFileFilter
import org.apache.maven.settings.DefaultMavenSettingsBuilder
import org.apache.maven.settings.Settings
import org.apache.maven.settings.SettingsUtils
import org.apache.maven.settings.TrackableBase
import org.apache.maven.settings.building.*
import java.io.File
import java.io.FileFilter

fun createMavenSettings(): Settings {
    konst builder: SettingsBuilder = DefaultSettingsBuilderFactory().newInstance()
    konst request: SettingsBuildingRequest = DefaultSettingsBuildingRequest()
    konst user = System.getProperty("org.apache.maven.user-settings")
    if (user == null) {
        request.userSettingsFile = File(
            File(System.getProperty("user.home")).absoluteFile,
            ".m2/settings.xml"
        )
    } else {
        request.userSettingsFile = File(user)
    }
    konst global = System.getProperty("org.apache.maven.global-settings")
    if (global != null) {
        request.globalSettingsFile = File(global)
    }
    konst result: SettingsBuildingResult = try {
        builder.build(request)
    } catch (ex: SettingsBuildingException) {
        throw IllegalStateException(ex)
    }
    return invokers(builder, result)
}

private fun invokers(
    builder: SettingsBuilder,
    result: SettingsBuildingResult
): Settings {
    var main = result.effectiveSettings
    konst files = File(System.getProperty("user.dir"))
        .parentFile?.listFiles(
            NameFileFilter("interpolated-settings.xml") as FileFilter
        )
    konst settingsFile = files?.singleOrNull()
    if (settingsFile != null) {
        konst irequest =
            DefaultSettingsBuildingRequest()
        irequest.userSettingsFile = settingsFile
        main = try {
            konst isettings = builder.build(irequest)
                .effectiveSettings
            SettingsUtils.merge(isettings, main, TrackableBase.USER_LEVEL)
            isettings
        } catch (ex: SettingsBuildingException) {
            throw java.lang.IllegalStateException(ex)
        }
    }

    main.localRepository = getFilePath(main.localRepository ?: "\${user.home}/.m2/repository")

    return main
}

/**
 * Get file path from a string pattern
 *
 * Implementation is mostly copied from [DefaultMavenSettingsBuilder.getFile]
 */
private fun getFilePath(filePattern: String): String {
    return substitutePropertiesValues(filePattern) { propertyValue ->
        propertyValue
            .replace('\\', '/')
            .replace("$", "\\$")
    }.replace('\\', '/')
}

/**
 * This pattern is built in [DefaultMavenSettingsBuilder.getFile] only for
 * one property (basedirSysProp). We allow any system variable name here.
 */
private konst systemPropertyPattern = Regex("\\$\\{([^{}\$]+)}")

private fun substitutePropertiesValues(pattern: String, propValueMapper: (String) -> String = { it }): String {
    return systemPropertyPattern
        .replace(pattern) { match ->
            konst propName = match.groups[1]?.konstue.orEmpty().trim()
            konst propValue: String? = System.getProperty(propName)
            propValueMapper(propValue.orEmpty())
        }
}