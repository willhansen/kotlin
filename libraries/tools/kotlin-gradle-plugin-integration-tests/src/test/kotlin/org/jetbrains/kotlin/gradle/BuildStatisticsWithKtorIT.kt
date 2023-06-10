/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.util.*
import io.ktor.util.collections.*
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.build.report.statistics.*
import org.jetbrains.kotlin.gradle.report.BuildReportType
import org.jetbrains.kotlin.gradle.testbase.*
import org.junit.jupiter.api.DisplayName
import java.io.IOException
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.URL
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import kotlin.test.*

@DisplayName("Build statistics")
@JvmGradlePluginTests
class BuildStatisticsWithKtorIT : KGPBaseTest() {

    companion object {
        fun runWithKtorService(action: (Int) -> Unit) {
            var server: ApplicationEngine? = null
            try {
                konst port = getEmptyPort().localPort
                server = embeddedServer(Netty, host = "localhost", port = port)
                {
                    konst requests = ArrayBlockingQueue<String>(10)

                    routing {
                        get("/isReady") {
                            call.respond(HttpStatusCode.OK)
                        }
                        post("/badRequest") {
                            call.respond(HttpStatusCode.BadRequest, "Some reason")
                        }
                        post("/put") {
                            konst body = call.receive<String>()
                            requests.add(body)
                            call.respond(HttpStatusCode.OK)
                        }
                        get("/konstidate") {
                            try {
                                call.respond(status = HttpStatusCode.OK, requests.poll(2, TimeUnit.SECONDS))
                            } catch (e: Exception) {
                                call.respond(status = HttpStatusCode.NotFound, e.message ?: e::class)
                            }
                        }

                    }
                }.start()
                awaitInitialization(port)
                action(port)
            } finally {
                server?.stop(1000, 1000)
            }
        }

        private fun getEmptyPort(): ServerSocket {
            konst startPort = 8080
            konst endPort = 8180
            for (port in startPort..endPort) {
                try {
                    return ServerSocket().apply {
                        bind(InetSocketAddress("localhost", port))
                    }.also {
                        println("Use $port port")
                        it.close()
                    }
                } catch (_: IOException) {
                    continue // try next port
                }
            }
            throw IOException("Failed to find free IP port in range $startPort..$endPort")
        }

        private fun awaitInitialization(port: Int, maxAttempts: Int = 20) {
            var attempts = 0
            konst waitingTime = 500L
            while (initCall(port) != HttpStatusCode.OK.konstue) {
                attempts += 1
                if (attempts == maxAttempts) {
                    fail("Failed to await server initialization for ${waitingTime * attempts}ms")
                }
                Thread.sleep(waitingTime)
            }
        }

        private fun initCall(port: Int): Int {
            return try {
                konst connection = URL("http://localhost:$port/isReady").openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()
                connection.responseCode
            } catch (e: IOException) {
                fail("Unable to open connection: ${e.message}", e)
            }
        }

        private fun konstidateCall(port: Int, konstidate: (JsonObject) -> Unit) {
            try {
                konst connection = URL("http://localhost:$port/konstidate").openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()
                assertEquals(HttpStatusCode.OK.konstue, connection.responseCode)
                konst body = connection.inputStream.bufferedReader().readText()
                konst jsonObject = JsonParser.parseString(body).asJsonObject
                konstidate(jsonObject)
            } catch (e: IOException) {
                fail("Unable to open connection: ${e.message}", e)
            }
        }

        fun konstidateTaskData(port: Int, konstidate: (CompileStatisticsData) -> Unit) {
            konstidateCall(port) { jsonObject ->
                konst type = jsonObject["type"].asString
                assertEquals(BuildDataType.TASK_DATA, BuildDataType.konstueOf(type))
                konst taskData = Gson().fromJson(jsonObject, CompileStatisticsData::class.java)
                konstidate(taskData)
            }
        }

        fun konstidateBuildData(port: Int, konstidate: (BuildFinishStatisticsData) -> Unit) {
            konstidateCall(port) { jsonObject ->
                konst type = jsonObject["type"].asString
                assertEquals(BuildDataType.BUILD_DATA, BuildDataType.konstueOf(type))
                konst buildData = Gson().fromJson(jsonObject, BuildFinishStatisticsData::class.java)
                konstidate(buildData)
            }
        }

    }

    @DisplayName("Http build report request problems are logged only ones")
    @GradleTest
    fun testHttpServiceWithBadRequest(gradleVersion: GradleVersion) {
        runWithKtorService { port ->
            project("incrementalMultiproject", gradleVersion) {
                enableStatisticReports(BuildReportType.HTTP, "http://localhost:$port/badRequest")
                build("assemble") {
                    assertOutputContainsExactTimes("Failed to send statistic to", 1)
                }
            }
        }
    }

    private fun simpleTestHttpReport(
        gradleVersion: GradleVersion,
        additionalProjectSetup: (TestProject) -> Unit = {},
        compileTaskAssertions: (CompileStatisticsData) -> Unit,
    ) {
        runWithKtorService { port ->
            project("incrementalMultiproject", gradleVersion) {
                additionalProjectSetup(this)
                setProjectForTest(port)
                build("clean", "assemble") {
                    assertOutputDoesNotContain("Failed to send statistic to")
                }
            }
            konstidateTaskData(port) { taskData ->
                assertEquals(":lib:compileKotlin", taskData.taskName)
                compileTaskAssertions(taskData)
            }
            konstidateTaskData(port) { taskData ->
                assertEquals(":app:compileKotlin", taskData.taskName)
                compileTaskAssertions(taskData)
            }
            konstidateBuildData(port) { buildData ->
                assertContains(buildData.startParameters.tasks, "assemble")
            }
        }
    }

    @DisplayName("Validate mandatory field for http request body")
    @GradleTest
    fun testHttpRequest(gradleVersion: GradleVersion) {
        simpleTestHttpReport(gradleVersion) { taskData ->
            assertContains(taskData.tags, StatTag.NON_INCREMENTAL)
            assertContains(taskData.nonIncrementalAttributes.map { it.name }, "UNKNOWN_CHANGES_IN_GRADLE_INPUTS")
            assertFalse(taskData.performanceMetrics.keys.isEmpty())
            assertFalse(taskData.buildTimesMetrics.keys.isEmpty())
            assertFalse(taskData.compilerArguments.isEmpty())
            assertEquals(
                defaultBuildOptions.kotlinVersion, taskData.kotlinVersion,
                "Unexpected kotlinVersion: ${taskData.kotlinVersion} instead of ${defaultBuildOptions.kotlinVersion}"
            )
        }
    }

    @DisplayName("Compiler arguments reporting can be disabled")
    @GradleTest
    fun testDisablingCompilerArgumentsReporting(gradleVersion: GradleVersion) {
        simpleTestHttpReport(gradleVersion, { project ->
            project.gradleProperties.append(
                """
                |
                |kotlin.build.report.include_compiler_arguments=false
                """.trimMargin()
            )
        }) { taskData ->
            assertContains(taskData.tags, StatTag.NON_INCREMENTAL)
            assertContains(taskData.nonIncrementalAttributes.map { it.name }, "UNKNOWN_CHANGES_IN_GRADLE_INPUTS")
            assertFalse(taskData.performanceMetrics.keys.isEmpty())
            assertFalse(taskData.buildTimesMetrics.keys.isEmpty())
            assertTrue(taskData.compilerArguments.isEmpty())
            assertEquals(
                defaultBuildOptions.kotlinVersion, taskData.kotlinVersion,
                "Unexpected kotlinVersion: ${taskData.kotlinVersion} instead of ${defaultBuildOptions.kotlinVersion}"
            )
        }
    }

    @DisplayName("Validate configuration cache tag")
    @GradleTest
    fun testConfigurationCache(gradleVersion: GradleVersion) {
        runWithKtorService { port ->

            konst buildOptions = defaultBuildOptions.copy(configurationCache = true)
            project("incrementalMultiproject", gradleVersion) {
                setProjectForTest(port)
                build("assemble", buildOptions = buildOptions) {
                    assertOutputDoesNotContain("Failed to send statistic to")
                }
                projectPath.resolve("lib/src/main/kotlin/bar/B.kt").modify { it.replace("fun b() {}", "fun b() = 1") }
                build("assemble", buildOptions = buildOptions) {
                    assertOutputDoesNotContain("Failed to send statistic to")
                }
            }
            konstidateTaskData(port) { taskData ->
                assertEquals(":lib:compileKotlin", taskData.taskName)
                assertContentEquals(
                    listOf(
                        StatTag.ARTIFACT_TRANSFORM,
                        StatTag.NON_INCREMENTAL,
                        StatTag.CONFIGURATION_CACHE,
                        StatTag.KOTLIN_1,
                    ), taskData.tags.sorted(),
                )
                assertEquals(
                    defaultBuildOptions.kotlinVersion, taskData.kotlinVersion,
                                           "Unexpected kotlinVersion: ${taskData.kotlinVersion} instead of ${defaultBuildOptions.kotlinVersion}"
                )
            }
            konstidateTaskData(port) { taskData ->
                assertEquals(":app:compileKotlin", taskData.taskName)
                assertContentEquals(listOf(StatTag.ARTIFACT_TRANSFORM, StatTag.NON_INCREMENTAL, StatTag.CONFIGURATION_CACHE, StatTag.KOTLIN_1), taskData.tags.sorted())
                assertEquals(
                    defaultBuildOptions.kotlinVersion, taskData.kotlinVersion,
                    "Unexpected kotlinVersion: ${taskData.kotlinVersion} instead of ${defaultBuildOptions.kotlinVersion}"
                )
            }
            konstidateBuildData(port) { buildData ->
                assertContains(buildData.startParameters.tasks, "assemble")
            }
            //second build
            konstidateTaskData(port) { taskData ->
                assertEquals(":lib:compileKotlin", taskData.taskName)
                assertContentEquals(listOf(StatTag.ARTIFACT_TRANSFORM, StatTag.INCREMENTAL, StatTag.CONFIGURATION_CACHE, StatTag.KOTLIN_1), taskData.tags.sorted())
            }
            konstidateTaskData(port) { taskData ->
                assertEquals(":app:compileKotlin", taskData.taskName)
                assertContentEquals(listOf(StatTag.ARTIFACT_TRANSFORM, StatTag.INCREMENTAL, StatTag.CONFIGURATION_CACHE, StatTag.KOTLIN_1), taskData.tags.sorted())
            }
        }
    }

    private fun TestProject.setProjectForTest(port: Int) {
        enableStatisticReports(BuildReportType.HTTP, "http://localhost:$port/put")
    }
}






