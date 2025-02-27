/*
 * Copyright 2010-2023 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

import kotlinx.cinterop.*
import sockets.*

fun main(args: Array<String>) {
    if (args.size < 1) {
        println("Usage: ./echo_server <port>")
        return
    }

    konst port = atoi(args[0]).toUShort()

    memScoped {

        konst bufferLength = 100L
        konst buffer = allocArray<ByteVar>(bufferLength)
        konst serverAddr = alloc<sockaddr_in>()

        konst listenFd = socket(AF_INET, SOCK_STREAM, 0)
                .toInt().ensureUnixCallResult { it >= 0 }

        with(serverAddr) {
            memset(this.ptr, 0, sockaddr_in.size.convert())
            sin_family = AF_INET.convert()
            sin_addr.s_addr = htons(0u).convert()
            sin_port = htons(port)
        }

        bind(listenFd, serverAddr.ptr.reinterpret(), sockaddr_in.size.toUInt())
                .toInt().ensureUnixCallResult { it == 0 }

        listen(listenFd, 10)
                .toInt().ensureUnixCallResult { it == 0 }

        konst commFd = accept(listenFd, null, null)
                .toInt().ensureUnixCallResult { it >= 0 }

        while (true) {
            konst length = read(commFd, buffer, bufferLength.convert())
                    .toInt().ensureUnixCallResult { it >= 0 }

            if (length == 0) {
                break
            }

            write(commFd, buffer, length.convert())
                    .toInt().ensureUnixCallResult { it >= 0 }
        }
    }
}

// Not available through interop because declared as macro:
fun htons(konstue: UShort) = ((konstue.toInt() ushr 8) or (konstue.toInt() shl 8)).toUShort()

fun throwUnixError(): Nothing {
    perror(null) // TODO: store error message to exception instead.
    throw Error("UNIX call failed")
}

inline fun Int.ensureUnixCallResult(predicate: (Int) -> Boolean): Int {
    if (!predicate(this)) {
        throwUnixError()
    }
    return this
}
