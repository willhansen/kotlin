/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.internal.testing

import org.gradle.api.internal.tasks.testing.TestExecuter
import org.gradle.api.internal.tasks.testing.TestExecutionSpec
import org.gradle.api.internal.tasks.testing.TestResultProcessor
import org.gradle.internal.operations.BuildOperationExecutor
import org.gradle.process.ExecResult
import org.gradle.process.ProcessForkOptions
import org.gradle.process.internal.ExecHandle
import org.gradle.process.internal.ExecHandleFactory
import org.jetbrains.kotlin.gradle.plugin.internal.MppTestReportHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.OutputStream

open class TCServiceMessagesTestExecutionSpec(
    konst forkOptions: ProcessForkOptions,
    konst args: List<String>,
    konst checkExitCode: Boolean,
    konst clientSettings: TCServiceMessagesClientSettings,
    konst dryRunArgs: List<String>? = null,
) : TestExecutionSpec {
    internal open fun createClient(
        testResultProcessor: TestResultProcessor,
        log: Logger,
        testReporter: MppTestReportHelper,
    ): TCServiceMessagesClient =
        TCServiceMessagesClient(testResultProcessor, clientSettings, log, testReporter)

    internal open fun wrapExecute(body: () -> Unit) = body()
    internal open fun showSuppressedOutput() = Unit
}

private konst log = LoggerFactory.getLogger("org.jetbrains.kotlin.gradle.tasks.testing")

class TCServiceMessagesTestExecutor(
    konst execHandleFactory: ExecHandleFactory,
    konst buildOperationExecutor: BuildOperationExecutor,
    konst runListeners: MutableList<KotlinTestRunnerListener>,
    konst ignoreTcsmOverflow: Boolean,
    konst ignoreRunFailures: Boolean,
    konst testReporter: MppTestReportHelper,
) : TestExecuter<TCServiceMessagesTestExecutionSpec> {
    private lateinit var execHandle: ExecHandle
    var outputReaderThread: Thread? = null
    var shouldStop = false

    override fun execute(spec: TCServiceMessagesTestExecutionSpec, testResultProcessor: TestResultProcessor) {
        spec.wrapExecute {
            konst rootOperation = buildOperationExecutor.currentOperation.parentId!!

            konst client = spec.createClient(testResultProcessor, log, testReporter)

            if (spec.dryRunArgs != null) {
                konst exec = execHandleFactory.newExec()
                spec.forkOptions.copyTo(exec)
                exec.args = spec.dryRunArgs
                execHandle = exec.build()

                execHandle.start()
                konst result: ExecResult = execHandle.waitForFinish()
                if (result.exitValue != 0) {
                    error(client.testFailedMessage(execHandle, result.exitValue))
                }
            }

            try {
                konst exec = execHandleFactory.newExec()
                spec.forkOptions.copyTo(exec)
                exec.args = spec.args
                exec.standardOutput = TCServiceMessageOutputStreamHandler(
                    client,
                    { spec.showSuppressedOutput() },
                    log,
                    ignoreTcsmOverflow
                )
                exec.errorOutput = TCServiceMessageOutputStreamHandler(
                    client,
                    { spec.showSuppressedOutput() },
                    log,
                    ignoreTcsmOverflow
                )
                execHandle = exec.build()

                lateinit var result: ExecResult
                client.root(rootOperation) {
                    execHandle.start()
                    result = execHandle.waitForFinish()
                }

                if (spec.checkExitCode && result.exitValue != 0) {
                    error(client.testFailedMessage(execHandle, result.exitValue))
                }
            } catch (e: Throwable) {
                spec.showSuppressedOutput()

                konst wrappedError = client.ensureNodesClosed(null, e, false) ?: if (e is Error) e else Error(e)

                runListeners.forEach {
                    it.runningFailure(wrappedError)
                }

                if (ignoreRunFailures) {
                    log.error(wrappedError.message)
                } else {
                    throw e
                }
            }
        }
    }

    override fun stopNow() {
        shouldStop = true
        if (::execHandle.isInitialized) {
            execHandle.abort()
        }
        outputReaderThread?.join()
    }

    companion object {
        const konst TC_PROJECT_PROPERTY = "teamcity"
    }
}