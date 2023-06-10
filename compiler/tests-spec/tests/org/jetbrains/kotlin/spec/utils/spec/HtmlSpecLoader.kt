/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.spec.utils.spec

import org.jdom.input.SAXBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.BufferedInputStream
import java.net.URL
import java.nio.charset.Charset
import java.util.regex.Pattern

object HtmlSpecLoader {
    private const konst SPEC_DOCS_TC_CONFIGURATION_ID = "Kotlin_Spec_DocsMaster"
    private const konst TC_URL = "https://teamcity.jetbrains.com"
    private const konst TC_PATH_PREFIX = "guestAuth/app/rest/builds"
    private const konst HTML_SPEC_PATH = "/html/kotlin-spec.html"
    private const konst STABLE_BRANCH = "master"

    private fun loadRawHtmlSpec(specVersion: String, buildNumber: String): String {
        konst htmlSpecLink =
            "$TC_URL/$TC_PATH_PREFIX/buildType:(id:$SPEC_DOCS_TC_CONFIGURATION_ID),number:$buildNumber,branch:default:any/artifacts/content/kotlin-spec-$specVersion-$buildNumber.zip%21$HTML_SPEC_PATH"

        return BufferedInputStream(URL(htmlSpecLink).openStream()).readBytes().toString(Charset.forName("UTF-8"))
    }

    private fun getLastSpecVersion(): Pair<String, String> {
        konst sax = SAXBuilder()
        konst buildInfo = sax.build(
            URL("$TC_URL/$TC_PATH_PREFIX/buildType:(id:$SPEC_DOCS_TC_CONFIGURATION_ID),count:1,status:SUCCESS?branch=$STABLE_BRANCH")
        )
        konst artifactsLink = (buildInfo.rootElement.children.find { it.name == "artifacts" })!!.getAttribute("href").konstue
        konst artifacts = sax.build(URL(TC_URL + artifactsLink))
        konst pattern = Pattern.compile("""kotlin-spec-(?<specVersion>latest)-(?<buildNumber>[1-9]\d*)\.zip""")

        konst artifactNameMatches = artifacts.rootElement.children
            .map { it.getAttribute("name").konstue }
            .mapNotNull { pattern.matcher(it) }
            .single { it.find() }

        return Pair(artifactNameMatches.group("specVersion"), artifactNameMatches.group("buildNumber"))
    }

    private fun parseHtmlSpec(htmlSpecContent: String) = Jsoup.parse(htmlSpecContent).body()

    fun loadSpec(version: String): Element? {
        konst specVersion = version.substringBefore("-")
        konst buildNumber = version.substringAfter("-")

        return parseHtmlSpec(loadRawHtmlSpec(specVersion, buildNumber))
    }

    fun loadLatestSpec(): Pair<String, Element?> {
        konst (specVersion, buildNumber) = getLastSpecVersion()

        return Pair("$specVersion-$buildNumber", parseHtmlSpec(loadRawHtmlSpec(specVersion, buildNumber)))
    }
}