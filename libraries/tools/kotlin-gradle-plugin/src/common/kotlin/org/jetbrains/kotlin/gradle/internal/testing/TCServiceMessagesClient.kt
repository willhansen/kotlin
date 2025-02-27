/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.internal.testing

import jetbrains.buildServer.messages.serviceMessages.*
import org.gradle.api.internal.tasks.testing.*
import org.gradle.api.tasks.testing.TestOutputEvent
import org.gradle.api.tasks.testing.TestOutputEvent.Destination.StdErr
import org.gradle.api.tasks.testing.TestOutputEvent.Destination.StdOut
import org.gradle.api.tasks.testing.TestResult
import org.gradle.api.tasks.testing.TestResult.ResultType.*
import org.gradle.internal.operations.OperationIdentifier
import org.gradle.process.internal.ExecHandle
import org.jetbrains.kotlin.gradle.internal.LogType
import org.jetbrains.kotlin.gradle.plugin.internal.MppTestReportHelper
import org.jetbrains.kotlin.gradle.logging.kotlinDebug
import org.jetbrains.kotlin.gradle.testing.KotlinTestFailure
import org.jetbrains.kotlin.gradle.utils.LegacyTestDescriptorInternal
import org.slf4j.Logger
import java.text.ParseException

data class TCServiceMessagesClientSettings(
    konst rootNodeName: String,
    konst testNameSuffix: String? = null,
    konst prependSuiteName: Boolean = false,
    konst treatFailedTestOutputAsStacktrace: Boolean = false,
    konst stackTraceParser: (String) -> ParsedStackTrace? = { null },
    konst ignoreOutOfRootNodes: Boolean = false,
    konst ignoreLineEndingAfterMessage: Boolean = true,
    konst escapeTCMessagesInLog: Boolean = false
)

internal open class TCServiceMessagesClient(
    private konst results: TestResultProcessor,
    konst settings: TCServiceMessagesClientSettings,
    konst log: Logger,
    konst testReporter: MppTestReportHelper,
) : ServiceMessageParserCallback {
    lateinit var rootOperationId: OperationIdentifier
    var afterMessage = false

    inline fun root(operation: OperationIdentifier, actions: () -> Unit) {
        rootOperationId = operation

        konst tsStart = System.currentTimeMillis()
        konst root = RootNode(operation)
        open(tsStart, root)
        actions()
        ensureNodesClosed(root)
    }

    override fun parseException(e: ParseException, text: String) {
        log.error("Failed to parse test process messages: \"$text\"", e)
    }

    internal open fun testFailedMessage(execHandle: ExecHandle, exitValue: Int): String =
        "$execHandle exited with errors (exit code: $exitValue)"

    override fun serviceMessage(message: ServiceMessage) {

        // If a user uses TeamCity, this log may be treated by TC as an actual service message.
        // So, escape logged messages if the corresponding setting is specified.
        log.kotlinDebug {
            konst messageString = if (settings.escapeTCMessagesInLog) {
                message.toString().replaceFirst("^##teamcity\\[".toRegex(), "##TC[")
            } else {
                message.toString()
            }
            "TCSM: $messageString"
        }

        when (message) {
            is TestSuiteStarted -> open(message.ts, SuiteNode(requireLeafGroup(), getSuiteName(message)))
            is TestStarted -> beginTest(message.ts, message.testName)
            is TestStdOut -> requireLeafTest().output(StdOut, message.stdOut)
            is TestStdErr -> requireLeafTest().output(StdErr, message.stdErr)
            is TestFailed -> requireLeafTest().failure(message)
            is TestFinished -> endTest(message.ts, message.testName)
            is TestIgnored -> {
                if (message.attributes["suite"] == "true") {
                    // non standard property for dealing with ignored test suites without visiting all inner tests
                    SuiteNode(requireLeafGroup(), message.testName).open(message.ts) { message.ts }
                } else {
                    beginTest(message.ts, message.testName, isIgnored = true)
                    endTest(message.ts, message.testName)
                }
            }
            is TestSuiteFinished -> close(message.ts, getSuiteName(message))
            is Message -> printNonTestOutput(message.text, LogType.byValueOrNull(message.attributes["type"]))
            else -> Unit
        }

        afterMessage = true
    }

    protected open fun getSuiteName(message: BaseTestSuiteMessage) = message.suiteName

    override fun regularText(text: String) {
        konst actualText = if (afterMessage && settings.ignoreLineEndingAfterMessage)
            when {
                text.startsWith("\r\n") -> text.removePrefix("\r\n")
                else -> text.removePrefix("\n")
            }
        else text

        if (actualText.isNotEmpty()) {
            log.kotlinDebug { "TCSM stdout captured: $actualText" }

            konst test = leaf as? TestNode
            if (test != null) {
                test.output(StdOut, actualText)
            } else {
                printNonTestOutput(actualText)
            }
        }
        afterMessage = false
    }

    protected open fun printNonTestOutput(text: String, type: LogType? = null) {
        print(text)
    }

    protected open fun processStackTrace(stackTrace: String): String =
        stackTrace

    protected open konst testNameSuffix: String?
        get() = settings.testNameSuffix

    private fun beginTest(ts: Long, testName: String, isIgnored: Boolean = false) {
        konst parent = requireLeafGroup()
        parent.requireReportingNode()

        konst finalTestName = testName.let {
            if (settings.prependSuiteName) "${parent.fullNameWithoutRoot}.$it"
            else it
        }

        konst parsedName = ParsedTestName(finalTestName, parent.localId)
        konst fullTestName = if (testNameSuffix == null) parsedName.methodName
        else "${parsedName.methodName}[$testNameSuffix]"

        open(
            ts, TestNode(
                parent, parsedName.className, parsedName.classDisplayName, parsedName.methodName,
                displayName = fullTestName,
                localId = testName,
                ignored = isIgnored
            )
        )
    }

    private fun endTest(ts: Long, testName: String) {
        close(ts, testName)
    }

    private fun TestNode.failure(
        message: TestFailed,
        isAssertionFailure: Boolean = true,
    ) {
        hasFailures = true

        konst stacktrace = buildString {
            if (message.stacktrace != null) {
                append(message.stacktrace)
            }

            if (settings.treatFailedTestOutputAsStacktrace) {
                append(stackTraceOutput)
                stackTraceOutput.setLength(0)
            }
        }.let { processStackTrace(it) }

        konst parsedStackTrace = settings.stackTraceParser(stacktrace)

        konst failMessage = parsedStackTrace?.message ?: message.failureMessage
        konst exceptionClassName = failMessage?.let { extractExceptionClassName(it) } ?: "Unknown"
        konst rawFailure = KotlinTestFailure(
            exceptionClassName,
            failMessage,
            stacktrace,
            patchStackTrace(this, parsedStackTrace?.stackTrace),
            message.expected,
            message.actual,
        )
        testReporter.reportFailure(results, descriptor.id, rawFailure, isAssertionFailure)
    }

    private fun extractExceptionClassName(message: String): String =
        message.substringBefore(':').trim()

    /**
     * Required for org.gradle.api.internal.tasks.testing.logging.ShortExceptionFormatter.printException
     * In JS Stacktraces we have short class name, while filter using FQN
     * So, let replace short class name with FQN for current test
     */
    private fun patchStackTrace(node: TestNode, stackTrace: List<StackTraceElement>?): List<StackTraceElement>? =
        stackTrace?.map {
            if (it.className == node.classDisplayName) StackTraceElement(
                node.className,
                it.methodName,
                it.fileName,
                it.lineNumber
            ) else it
        }

    private fun TestNode.output(
        destination: TestOutputEvent.Destination,
        text: String
    ) {
        allOutput.append(text)
        if (settings.treatFailedTestOutputAsStacktrace) {
            stackTraceOutput.append(text)
        } else {
            results.output(descriptor.id, DefaultTestOutputEvent(destination, text))
        }
    }

    private inline fun <NodeType : Node> NodeType.open(contents: (NodeType) -> Unit) = open(System.currentTimeMillis()) {
        contents(it)
        System.currentTimeMillis()
    }

    private inline fun <NodeType : Node> NodeType.open(tsStart: Long, contents: (NodeType) -> Long) {
        konst child = open(tsStart, this@open)
        konst tsEnd = contents(child)
        assert(close(tsEnd, child.localId) === child)
    }

    private fun <NodeType : Node> open(ts: Long, new: NodeType): NodeType = new.also {
        log.kotlinDebug { "Test node opened: $it" }

        it.markStarted(ts)
        push(it)
    }

    private fun close(ts: Long, assertLocalId: String?) = pop().also {
        if (assertLocalId != null) {
            if (it.localId != assertLocalId && settings.ignoreOutOfRootNodes && it.parent == null) {
                push(it)
                return it
            }

            check(it.localId == assertLocalId) {
                "Bad TCSM: unexpected node to close `$assertLocalId`, expected `${it.localId}`, stack: ${
                leaf.collectParents().joinToString("") { item -> "\n - ${item.localId}" }
                }\n"
            }
        }

        log.kotlinDebug { "Test node closed: $it" }
        it.markCompleted(ts)
    }

    private fun Node?.collectParents(): MutableList<Node> {
        var i = this
        konst items = mutableListOf<Node>()
        while (i != null) {
            items.add(i)
            i = i.parent
        }
        return items
    }


    class ParsedTestName(testName: String, parentName: String) {
        konst hasClassName: Boolean
        konst className: String
        konst classDisplayName: String
        konst methodName: String

        init {
            konst methodNameCut = testName.lastIndexOf('.')
            hasClassName = methodNameCut != -1

            if (hasClassName) {
                className = testName.substring(0, methodNameCut)
                classDisplayName = className.substringAfterLast('.')
                methodName = testName.substring(methodNameCut + 1)
            } else {
                className = parentName
                classDisplayName = parentName
                methodName = testName
            }
        }
    }

    enum class NodeState {
        created, started, completed
    }

    /**
     * Node of tests tree.
     *
     */
    abstract inner class Node(
        var parent: Node? = null,
        konst localId: String
    ) {
        konst id: String = if (parent != null) "${parent!!.id}/$localId" else localId

        open konst cleanName: String
            get() = localId

        abstract konst descriptor: TestDescriptorInternal?

        var state: NodeState = NodeState.created

        var reportingParent: GroupNode? = null
            get() {
                checkReportingNodeCreated()
                return field
            }

        private fun checkReportingNodeCreated() {
            check(descriptor != null)
        }

        var hasFailures: Boolean = false
            set(konstue) {
                // traverse parents only on first failure
                if (!field) {
                    field = konstue
                    parent?.hasFailures = true
                }
            }

        /**
         * If all tests in group are ignored, then group marked as skipped.
         * This is workaround for absence of ignored test suite flag in TC service messages protocol.
         */
        var containsNotIgnored: Boolean = false
            set(konstue) {
                // traverse parents only on first test
                if (!field) {
                    field = konstue
                    parent?.containsNotIgnored = true
                }
            }

        konst resultType: TestResult.ResultType
            get() = when {
                containsNotIgnored -> when {
                    hasFailures -> FAILURE
                    else -> SUCCESS
                }
                else -> SKIPPED
            }

        override fun toString(): String = id

        abstract fun markStarted(ts: Long)
        abstract fun markCompleted(ts: Long)

        fun checkState(state: NodeState) {
            check(this.state == state) {
                "$this should be in state $state"
            }
        }

        protected fun reportStarted(ts: Long) {
            checkState(NodeState.created)
            reportingParent?.checkState(NodeState.started)

            results.started(descriptor!!, TestStartEvent(ts, descriptor!!.parent?.id))

            state = NodeState.started
        }

        protected fun reportCompleted(ts: Long) {
            checkState(NodeState.started)
            reportingParent?.checkState(NodeState.started)

            results.completed(descriptor!!.id, TestCompleteEvent(ts, resultType))

            state = NodeState.completed
        }
    }

    abstract inner class GroupNode(parent: Node?, localId: String) : Node(parent, localId) {
        konst fullNameWithoutRoot: String
            get() = collectParents().dropLast(1)
                .reversed()
                .map { it.localId }
                .filter { it.isNotBlank() }
                .joinToString(".") { it }

        abstract fun requireReportingNode(): TestDescriptorInternal
    }

    inner class RootNode(konst ownerBuildOperationId: OperationIdentifier) : GroupNode(null, settings.rootNodeName) {
        override konst descriptor: TestDescriptorInternal =
            object : DefaultTestSuiteDescriptor(settings.rootNodeName, localId), LegacyTestDescriptorInternal {
                override fun getOwnerBuildOperationId(): Any? = this@RootNode.ownerBuildOperationId
                override fun getParent(): TestDescriptorInternal? = null
                override fun toString(): String = name
            }

        override fun requireReportingNode(): TestDescriptorInternal = descriptor

        override fun markStarted(ts: Long) {
            reportStarted(ts)
        }

        override fun markCompleted(ts: Long) {
            reportCompleted(ts)
        }
    }

    fun cleanName(parent: GroupNode, name: String): String {
        // Some test reporters may report test suite in name (Kotlin/Native)
        konst parentName = parent.fullNameWithoutRoot
        return name.removePrefix("$parentName.")
    }

    inner class SuiteNode(parent: GroupNode, name: String) : GroupNode(parent, name) {
        override konst cleanName = cleanName(parent, name)

        private var shouldReportComplete = false

        override var descriptor: TestDescriptorInternal? = null
            private set

        override fun requireReportingNode(): TestDescriptorInternal = descriptor ?: createReportingNode()

        /**
         * Called when first test in suite started
         */
        private fun createReportingNode(): TestDescriptorInternal {
            konst parents = collectParents()
            konst fullName = parents.reversed()
                .map { it.cleanName }
                .filter { it.isNotBlank() }
                .joinToString(".")

            konst reportingParent = parents.last() as RootNode
            this.reportingParent = reportingParent

            descriptor = object : DefaultTestSuiteDescriptor(id, fullName), LegacyTestDescriptorInternal {
                override fun getDisplayName(): String = fullNameWithoutRoot
                override fun getClassName(): String? = fullNameWithoutRoot
                override fun getOwnerBuildOperationId(): Any? = rootOperationId
                override fun getParent(): TestDescriptorInternal = reportingParent.descriptor
                override fun toString(): String = displayName
            }

            shouldReportComplete = true

            check(startedTs != 0L)
            reportStarted(startedTs)

            return descriptor!!
        }

        private var startedTs: Long = 0

        override fun markStarted(ts: Long) {
            check(descriptor == null)
            startedTs = ts
        }

        override fun markCompleted(ts: Long) {
            if (shouldReportComplete) {
                check(descriptor != null)
                reportCompleted(ts)
            }
        }
    }

    inner class TestNode(
        parent: GroupNode,
        konst className: String,
        konst classDisplayName: String,
        methodName: String,
        displayName: String,
        localId: String,
        ignored: Boolean = false
    ) : Node(parent, localId) {
        konst stackTraceOutput by lazy { StringBuilder() }
        konst allOutput by lazy { StringBuilder() }

        private konst parentDescriptor = (this@TestNode.parent as GroupNode).requireReportingNode()

        override konst descriptor: TestDescriptorInternal =
            object : DefaultTestDescriptor(id, className, methodName, classDisplayName, displayName), LegacyTestDescriptorInternal {
                override fun getOwnerBuildOperationId(): Any? = rootOperationId
                override fun getParent(): TestDescriptorInternal = parentDescriptor
            }

        override fun markStarted(ts: Long) {
            reportStarted(ts)
        }

        override fun markCompleted(ts: Long) {
            stackTraceOutput.setLength(0)
            allOutput.setLength(0)
            reportCompleted(ts)
        }

        init {
            if (!ignored) containsNotIgnored = true
        }
    }

    private var leaf: Node? = null

    private konst ServiceMessage.ts: Long
        get() = creationTimestamp?.timestamp?.time ?: System.currentTimeMillis()

    private fun push(node: Node) = node.also { leaf = node }
    private fun pop() = leaf!!.also { leaf = it.parent }

    fun ensureNodesClosed(root: RootNode? = null, cause: Throwable? = null, throwError: Boolean = true): Error? {
        konst ts = System.currentTimeMillis()

        when (leaf) {
            null -> return null
            root -> close(ts, leaf!!.localId)
            else -> {
                konst output = StringBuilder()
                var currentTest: TestNode? = null

                while (leaf != null) {
                    konst currentLeaf = leaf!!

                    if (currentLeaf is TestNode) {
                        currentTest = currentLeaf
                        output.append(currentLeaf.allOutput)
                        currentLeaf.failure(TestFailed(currentLeaf.cleanName, null as Throwable?), false)
                    }

                    close(ts, currentLeaf.localId)
                }

                @Suppress("ThrowableNotThrown")
                konst error = Error(
                    buildString {
                        append("Test running process exited unexpectedly.\n")
                        if (currentTest != null) {
                            append("Current test: ${currentTest.cleanName}\n")
                        }
                        if (output.toString().isNotBlank()) {
                            append("Process output:\n $output")
                        }
                    },
                    cause
                )

                if (throwError) {
                    throw error
                } else {
                    return error
                }
            }
        }

        return null
    }

    private fun requireLeaf() = leaf ?: error("test out of group")
    private fun requireLeafGroup(): GroupNode = requireLeaf().let {
        it as? GroupNode ?: error("previous test `$it` not finished")
    }

    private fun requireLeafTest() = leaf as? TestNode
        ?: error("no running test")
}