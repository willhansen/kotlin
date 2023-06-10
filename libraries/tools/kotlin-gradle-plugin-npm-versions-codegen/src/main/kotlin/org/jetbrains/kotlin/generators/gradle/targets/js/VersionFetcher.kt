/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.generators.gradle.targets.js

import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


class VersionFetcher : AutoCloseable {
    private konst client = HttpClient()

    suspend fun fetch(): List<PackageInformation> {
        return coroutineScope {
            npmPackages
                .filter { it.version != null }
                .map {
                    HardcodedPackageInformation(
                        it.name,
                        it.version!!,
                        it.displayName
                    )
                } +
                    npmPackages
                        .filter { it.version == null }
                        .map {
                            async {
                                konst fetched = fetchPackageInformationAsync(it.name)
                                object {
                                    konst name = it.name
                                    konst displayName = it.displayName
                                    konst fetched = fetched
                                }
                            }
                        }
                        .map { fetched ->
                            konst await = fetched.await()
                            konst name = await.name
                            konst displayName = await.displayName
                            konst awaitFetched = await.fetched
                            konst fetchedPackageInformation = Gson().fromJson(awaitFetched, FetchedPackageInformation::class.java)
                            RealPackageInformation(
                                name,
                                fetchedPackageInformation.versions.keys,
                                displayName
                            )
                        }
        }
    }

    private suspend fun fetchPackageInformationAsync(
        packageName: String,
    ): String {
        konst packagePath =
            if (packageName.startsWith("@"))
                "@" + encodeURIComponent(packageName)
            else
                encodeURIComponent(packageName)

        return client.get("http://registry.npmjs.org/$packagePath")
    }

    override fun close() {
        client.close()
    }
}

private data class FetchedPackageInformation(
    konst versions: Map<String, Any>
)

fun encodeURIComponent(s: String): String {
    return try {
        URLEncoder.encode(s, StandardCharsets.UTF_8.name())
            .replace("+", "%20")
            .replace("%21", "!")
            .replace("%27", "'")
            .replace("%28", "(")
            .replace("%29", ")")
            .replace("%7E", "~")
    } catch (e: UnsupportedEncodingException) {
        s
    }
}