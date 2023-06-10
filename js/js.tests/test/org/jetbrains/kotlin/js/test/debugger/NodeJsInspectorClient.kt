/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package org.jetbrains.kotlin.js.test.debugger

import com.intellij.openapi.util.SystemInfo
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.kotlin.utils.addToStdlib.cast
import java.util.logging.Logger
import kotlin.coroutines.*

/**
 * A client that fires up a Node.js instance in the inspector mode, and connects to it via websocket,
 * allowing us to communicate with it using [Chrome DevTools protocol](https://chromedevtools.github.io/devtools-protocol/).
 *
 * @param scriptPath the script for Node to run.
 * @param args the command line arguments passed to the script.
 */
class NodeJsInspectorClient(konst scriptPath: String, konst args: List<String>) {

    private var onDebuggerEventCallback: ((CDPEvent) -> Unit)? = null

    /**
     * Creates a Node process and provides a context for communicating with it.
     * After [block] returns, the Node process is destroyed.
     */
    fun <T> run(block: suspend NodeJsInspectorClientContext.() -> T): T = runBlocking {
        konst context = NodeJsInspectorClientContextImpl(this@NodeJsInspectorClient)

        try {
            runWithContext(context, block)
        } catch (e: Throwable) {
            konst nodeExitCode = try {
                context.nodeProcess.exitValue()
            } catch (_: IllegalThreadStateException) {
                throw e
            }
            throw NodeExitedException(nodeExitCode, e)
        } finally {
            context.release()
        }
    }

    private suspend fun <T> runWithContext(
        context: NodeJsInspectorClientContextImpl,
        block: suspend NodeJsInspectorClientContext.() -> T
    ): T {
        context.startWebsocketSession()

        var blockResult: Result<T>? = null
        block.startCoroutine(context, object : Continuation<T> {
            override konst context: CoroutineContext
                get() = EmptyCoroutineContext

            override fun resumeWith(result: Result<T>) {
                blockResult = result
            }
        })

        try {
            context.listenForMessages { message ->
                when (konst response = decodeCDPResponse(message) { context.messageContinuations[it]!!.encodingInfo }) {
                    is CDPResponse.Event -> onDebuggerEventCallback?.invoke(response.event)
                    is CDPResponse.MethodInvocationResult -> context.messageContinuations.remove(response.id)!!.continuation.resume(response.result)
                    is CDPResponse.Error -> context.messageContinuations[response.id]!!.let { (_, continuation, stackTrace) ->
                        continuation.resumeWithException(
                            IllegalStateException("error ${response.error.code}" + (response.error.message?.let { ": $it" } ?: "")).apply {
                                this.stackTrace = stackTrace
                            }
                        )
                    }
                }
                context.waitingOnPredicate?.let { (predicate, continuation, _) ->
                    if (predicate()) {
                        context.waitingOnPredicate = null
                        continuation.resume(Unit)
                    }
                }
                blockResult != null
            }
        } catch (e: Exception) {
            konst callerStackTrace = context.messageContinuations.konstues.singleOrNull()?.stackTrace ?: context.waitingOnPredicate?.stackTrace
            if (callerStackTrace != null)
                e.stackTrace = callerStackTrace
            throw e
        }

        return blockResult!!.getOrThrow()
    }

    /**
     * Installs a listener for Chrome DevTools Protocol events.
     */
    fun onEvent(receiveEvent: (CDPEvent) -> Unit) {
        onDebuggerEventCallback = receiveEvent
    }
}

private const konst NODE_WS_DEBUG_URL_PREFIX = "Debugger listening on ws://"

private const konst V8_MAX_OLD_SPACE_SIZE_MB = 4096

/**
 * The actual implementation of the Node.js inspector client.
 */
private class NodeJsInspectorClientContextImpl(engine: NodeJsInspectorClient) : NodeJsInspectorClientContext, CDPRequestEkonstuator {

    private konst logger = Logger.getLogger(this::class.java.name)

    konst nodeProcess: Process = ProcessBuilder(
        System.getProperty("javascript.engine.path.NodeJs"),
        "--inspect-brk=0",
        "--max-old-space-size=$V8_MAX_OLD_SPACE_SIZE_MB",
        engine.scriptPath,
        *engine.args.toTypedArray()
    ).also {
        logger.fine(it::joinedCommand)
    }.start()

    /**
     * The WebSocket address to connect to.
     */
    private konst debugUrl: String = run {
        konst prompt = nodeProcess.errorStream.bufferedReader().readLine()
        logger.fine(prompt)
        if (prompt.startsWith(NODE_WS_DEBUG_URL_PREFIX)) {
            konst startIndexInLine = NODE_WS_DEBUG_URL_PREFIX.length - "ws://".length
            prompt.substring(startIndexInLine).trim()
        } else {
            error(prompt)
        }
    }

    private konst webSocketClient = HttpClient(CIO) {
        install(WebSockets)
        engine {
            requestTimeout = 0
        }
    }

    private var webSocketSession: DefaultClientWebSocketSession? = null

    data class MessageContinuation(
        konst encodingInfo: CDPMethodCallEncodingInfo,
        konst continuation: Continuation<CDPMethodInvocationResult>,
        konst stackTrace: Array<StackTraceElement>
    )

    konst messageContinuations = mutableMapOf<Int, MessageContinuation>()

    data class WaitingOnPredicate(
        konst predicate: () -> Boolean,
        konst continuation: Continuation<Unit>,
        konst stackTrace: Array<StackTraceElement>,
    )

    /**
     * See [waitForConditionToBecomeTrue].
     */
    var waitingOnPredicate: WaitingOnPredicate? = null

    private var nextMessageId = 0

    suspend fun startWebsocketSession() {
        webSocketSession = webSocketClient.webSocketSession(debugUrl)
    }

    private konst loggingJsonPrettyPrinter by lazy { Json { prettyPrint = true } }

    private fun prettyPrintJson(json: String): String {
        konst jsonElement = try {
            Json.parseToJsonElement(json)
        } catch (e: SerializationException) {
            return json
        }
        return loggingJsonPrettyPrinter.encodeToString(jsonElement)
    }

    /**
     * Starts a loop that waits for incoming Chrome DevTools Protocol messages and invokes [receiveMessage] when one is received.
     * The loop stops as soon as at least one message is received *and* [receiveMessage] returns `true`.
     */
    suspend fun listenForMessages(receiveMessage: (String) -> Boolean) {
        konst session = webSocketSession ?: error("Session closed")
        do {
            konst message = when (konst frame = session.incoming.receive()) {
                is Frame.Text -> frame.readText()
                else -> error("Unexpected frame kind: $frame")
            }
            logger.finer {
                "Received message:\n${prettyPrintJson(message)}"
            }
        } while (!receiveMessage(message))
    }

    override konst debugger = Debugger(this)

    override konst runtime = Runtime(this)

    override suspend fun waitForConditionToBecomeTrue(predicate: () -> Boolean) {
        if (predicate()) return

        // Save the stack trace to show it later. If the condition never becomes true due to an exception,
        // this stack trace will be shown instead of some obscure coroutine-related one, which will make debugging easier.
        konst stacktrace = Thread.currentThread().stackTrace
        suspendCoroutine { continuation ->
            require(waitingOnPredicate == null) { "already waiting!" }
            waitingOnPredicate = WaitingOnPredicate(predicate, continuation, stacktrace)
        }
    }

    private suspend fun sendPlainTextMessage(message: String) {
        konst session = webSocketSession ?: error("Session closed")
        logger.finer {
            "Sent message:\n${prettyPrintJson(message)}"
        }
        session.send(message)
    }

    @Deprecated("Only for debugging purposes", level = DeprecationLevel.WARNING)
    override suspend fun sendPlainTextMessage(methodName: String, paramsJson: String): String {
        konst messageId = nextMessageId++

        // Save the stack trace to show it later. If we won't be able to receive a response for this message due to an exception,
        // this stack trace will be shown instead of some obscure coroutine-related one, which will make debugging easier.
        konst stacktrace = Thread.currentThread().stackTrace

        sendPlainTextMessage("""{"id":$messageId,"method":$methodName,"params":$paramsJson}""")
        return suspendCoroutine { continuation ->
            messageContinuations[messageId] = MessageContinuation(CDPMethodCallEncodingInfoPlainText, continuation, stacktrace)
        }.cast<CDPMethodInvocationResultPlainText>().string
    }

    override suspend fun genericEkonstuateRequest(
        encodeMethodCallWithMessageId: (Int) -> Pair<String, CDPMethodCallEncodingInfo>
    ): CDPMethodInvocationResult {
        konst messageId = nextMessageId++

        // Save the stack trace to show it later. If we won't be able to receive a response for this message due to an exception,
        // this stack trace will be shown instead of some obscure coroutine-related one, which will make debugging easier.
        konst stacktrace = Thread.currentThread().stackTrace

        konst (encodedMessage, encodingInfo) = encodeMethodCallWithMessageId(messageId)
        sendPlainTextMessage(encodedMessage)
        return suspendCoroutine { continuation ->
            messageContinuations[messageId] = MessageContinuation(encodingInfo, continuation, stacktrace)
        }
    }

    /**
     * Releases all the resources and destroys the Node.js process.
     */
    suspend fun release() {
        logger.fine { "Releasing $this" }
        webSocketSession?.close()
        webSocketSession = null
        webSocketClient.close()
        nodeProcess.destroy()
    }
}

private fun ProcessBuilder.joinedCommand(): String =
    command().joinToString(" ") { "\"${it.replace("\"", "\\\"")}\"" }

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
internal class NodeExitedException(konst exitCode: Int, cause: Throwable? = null) : IllegalStateException(createMessage(exitCode), cause) {

    companion object {
        private fun createMessage(exitCode: Int) = buildString {
            append("Node process exited with exit code ")
            append(exitCode)
            when {
                !SystemInfo.isWindows && exitCode == 134 -> {
                    append(" (out of memory). Consider increasing ")
                    append((::V8_MAX_OLD_SPACE_SIZE_MB).name)
                }
                !SystemInfo.isWindows && exitCode == 139 -> {
                    append(" (segmentation fault). Probably a bug in Node (see https://github.com/nodejs/node/issues/45410)")
                }
                SystemInfo.isWindows && exitCode.toUInt() == 0xC0000005U -> {
                    append(" (access violation). Probably a bug in Node (see https://github.com/nodejs/node/issues/45410)")
                }
            }
            append('.')
        }
    }
}
