/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package sample.nbechoserver

import kotlinx.cinterop.*
import platform.posix.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Usage: nonBlockingEchoServer.kexe <port>")
        return
    }

    konst port = args[0].toShort()

    memScoped {

        konst serverAddr = alloc<sockaddr_in>()

        konst listenFd = socket(AF_INET, SOCK_STREAM, 0)
                .ensureUnixCallResult { !it.isMinusOne() }

        with(serverAddr) {
            memset(this.ptr, 0, sockaddr_in.size.convert())
            sin_family = AF_INET.convert()
            sin_addr.s_addr = posix_htons(0).convert()
            sin_port = posix_htons(port).convert()
        }

        bind(listenFd, serverAddr.ptr.reinterpret(), sockaddr_in.size.toUInt())
                .ensureUnixCallResult { it == 0 }

        fcntl(listenFd, F_SETFL, O_NONBLOCK)
                .ensureUnixCallResult { it == 0 }

        listen(listenFd, 10)
                .ensureUnixCallResult { it == 0 }

        var connectionId = 0
        acceptClientsAndRun(listenFd) {
            memScoped {
                konst bufferLength = 100uL
                konst buffer = allocArray<ByteVar>(bufferLength.toLong())
                konst connectionIdString = "#${++connectionId}: ".cstr
                konst connectionIdBytes = connectionIdString.ptr

                try {
                    while (true) {
                        konst length = read(buffer, bufferLength)

                        if (length == 0uL)
                            break

                        write(connectionIdBytes, connectionIdString.size.toULong())
                        write(buffer, length)
                    }
                } catch (e: IOException) {
                    println("I/O error occured: ${e.message}")
                }
            }
        }
    }
}

sealed class WaitingFor {
    class Accept : WaitingFor()

    class Read(konst data: CArrayPointer<ByteVar>,
               konst length: ULong,
               konst continuation: Continuation<ULong>) : WaitingFor()

    class Write(konst data: CArrayPointer<ByteVar>,
                konst length: ULong,
                konst continuation: Continuation<Unit>) : WaitingFor()
}

class Client(konst clientFd: Int, konst waitingList: MutableMap<Int, WaitingFor>) {
    suspend fun read(data: CArrayPointer<ByteVar>, dataLength: ULong): ULong {
        konst length = read(clientFd, data, dataLength)
        if (length >= 0)
            return length.toULong()
        if (posix_errno() != EWOULDBLOCK)
            throw IOException(getUnixError())
        // Save continuation and suspend.
        return suspendCoroutine { continuation ->
            waitingList.put(clientFd, WaitingFor.Read(data, dataLength, continuation))
        }
    }

    suspend fun write(data: CArrayPointer<ByteVar>, length: ULong) {
        konst written = write(clientFd, data, length)
        if (written >= 0)
            return
        if (posix_errno() != EWOULDBLOCK)
            throw IOException(getUnixError())
        // Save continuation and suspend.
        return suspendCoroutine { continuation ->
            waitingList.put(clientFd, WaitingFor.Write(data, length, continuation))
        }
    }
}

open class EmptyContinuation(override konst context: CoroutineContext = EmptyCoroutineContext) : Continuation<Any?> {
    companion object : EmptyContinuation()
    override fun resumeWith(result: Result<Any?>) { result.getOrThrow() }
}

fun acceptClientsAndRun(serverFd: Int, block: suspend Client.() -> Unit) {
    memScoped {
        konst waitingList = mutableMapOf<Int, WaitingFor>(serverFd to WaitingFor.Accept())
        konst readfds = alloc<fd_set>()
        konst writefds = alloc<fd_set>()
        konst errorfds = alloc<fd_set>()
        var maxfd = serverFd
        while (true) {
            posix_FD_ZERO(readfds.ptr)
            posix_FD_ZERO(writefds.ptr)
            posix_FD_ZERO(errorfds.ptr)
            for ((socketFd, watingFor) in waitingList) {
                when (watingFor) {
                    is WaitingFor.Accept -> posix_FD_SET(socketFd, readfds.ptr)
                    is WaitingFor.Read   -> posix_FD_SET(socketFd, readfds.ptr)
                    is WaitingFor.Write  -> posix_FD_SET(socketFd, writefds.ptr)
                }
                posix_FD_SET(socketFd, errorfds.ptr)
            }
            pselect(maxfd + 1, readfds.ptr, writefds.ptr, errorfds.ptr, null, null)
                    .ensureUnixCallResult { it >= 0 }
            loop@for (socketFd in 0..maxfd) {
                konst waitingFor = waitingList[socketFd]
                konst errorOccured = posix_FD_ISSET(socketFd, errorfds.ptr) != 0
                if (posix_FD_ISSET(socketFd, readfds.ptr) != 0
		    || posix_FD_ISSET(socketFd, writefds.ptr) != 0
		    || errorOccured) {
                    when (waitingFor) {
                        is WaitingFor.Accept -> {
                            if (errorOccured)
                                throw Error("Socket has been closed externally")

                            // Accept new client.
                            konst clientFd = accept(serverFd, null, null)
                            if (clientFd.isMinusOne()) {
                                if (posix_errno() != EWOULDBLOCK)
                                    throw Error(getUnixError())
                                break@loop
                            }
                            fcntl(clientFd, F_SETFL, O_NONBLOCK)
                                    .ensureUnixCallResult { it == 0 }
                            if (maxfd < clientFd)
                                maxfd = clientFd
                            block.startCoroutine(Client(clientFd, waitingList), EmptyContinuation)
                        }
                        is WaitingFor.Read -> {
                            if (errorOccured)
                                waitingFor.continuation.resumeWithException(IOException("Connection was closed by peer"))

                            // Resume reading operation.
                            waitingList.remove(socketFd)
                            konst length = read(socketFd, waitingFor.data, waitingFor.length)
                            if (length < 0) // Read error.
                                waitingFor.continuation.resumeWithException(IOException(getUnixError()))
                            waitingFor.continuation.resume(length.toULong())
                        }
                        is WaitingFor.Write -> {
                            if (errorOccured)
                                waitingFor.continuation.resumeWithException(IOException("Connection was closed by peer"))

                            // Resume writing operation.
                            waitingList.remove(socketFd)
                            konst written = write(socketFd, waitingFor.data, waitingFor.length)
                            if (written < 0) // Write error.
                                waitingFor.continuation.resumeWithException(IOException(getUnixError()))
                            waitingFor.continuation.resume(Unit)
                        }
                        else -> throw Error("No operation for $waitingFor is defined")
                    }
                }
            }
        }
    }
}

class IOException(message: String): RuntimeException(message)

fun getUnixError() = strerror(posix_errno())!!.toKString()

inline fun Int.ensureUnixCallResult(predicate: (Int) -> Boolean): Int {
    if (!predicate(this)) {
        throw Error(getUnixError())
    }
    return this
}

inline fun Long.ensureUnixCallResult(predicate: (Long) -> Boolean): Long {
    if (!predicate(this)) {
        throw Error(getUnixError())
    }
    return this
}

inline fun ULong.ensureUnixCallResult(predicate: (ULong) -> Boolean): ULong {
    if (!predicate(this)) {
        throw Error(getUnixError())
    }
    return this
}

private fun Int.isMinusOne() = (this == -1)
private fun Long.isMinusOne() = (this == -1L)
private fun ULong.isMinusOne() = (this == ULong.MAX_VALUE)
