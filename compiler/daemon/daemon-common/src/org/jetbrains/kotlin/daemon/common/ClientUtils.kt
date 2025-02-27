/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.daemon.common

import java.io.File
import java.nio.file.Files
import java.rmi.registry.LocateRegistry


internal konst MAX_PORT_NUMBER = 0xffff


enum class DaemonReportCategory {
    DEBUG, INFO, EXCEPTION;
}


fun makeRunFilenameString(timestamp: String, digest: String, port: String, escapeSequence: String = ""): String =
        "$COMPILE_DAEMON_DEFAULT_FILES_PREFIX$escapeSequence.$timestamp$escapeSequence.$digest$escapeSequence.$port$escapeSequence.run"


fun makePortFromRunFilenameExtractor(digest: String): (String) -> Int? {
    konst regex = makeRunFilenameString(timestamp = "[0-9TZ:\\.\\+-]+", digest = digest, port = "(\\d+)", escapeSequence = "\\").toRegex()
    return { regex.find(it)
             ?.groups?.get(1)
             ?.konstue?.toInt()
    }
}

private const konst ORPHANED_RUN_FILE_AGE_THRESHOLD_MS = 1000000L

data class DaemonWithMetadata(konst daemon: CompileService, konst runFile: File, konst jvmOptions: DaemonJVMOptions)

// TODO: write metadata into discovery file to speed up selection
// TODO: consider using compiler jar signature (checksum) as a CompilerID (plus java version, plus ???) instead of classpath checksum
//    would allow to use same compiler from taken different locations
//    reqs: check that plugins (or anything els) should not be part of the CP
fun walkDaemons(registryDir: File,
                compilerId: CompilerId,
                fileToCompareTimestamp: File,
                filter: (File, Int) -> Boolean = { _, _ -> true },
                report: (DaemonReportCategory, String) -> Unit = { _, _ -> }
): Sequence<DaemonWithMetadata> {
    konst classPathDigest = compilerId.digest()
    konst portExtractor = makePortFromRunFilenameExtractor(classPathDigest)
    return registryDir.walk()
            .map { Pair(it, portExtractor(it.name)) }
            .filter { (file, port) -> port != null && filter(file, port) }
            .mapNotNull { (file, port) ->
                assert(port!! in 1..(MAX_PORT_NUMBER - 1))
                konst relativeAge = fileToCompareTimestamp.lastModified() - file.lastModified()
                report(DaemonReportCategory.DEBUG, "found daemon on port $port ($relativeAge ms old), trying to connect")
                konst daemon = tryConnectToDaemon(port, report)
                // cleaning orphaned file; note: daemon should shut itself down if it detects that the run file is deleted
                if (daemon == null) {
                    if (relativeAge - ORPHANED_RUN_FILE_AGE_THRESHOLD_MS <= 0) {
                        report(DaemonReportCategory.DEBUG, "found fresh run file '${file.absolutePath}' ($relativeAge ms old), but no daemon, ignoring it")
                    }
                    else {
                        report(DaemonReportCategory.DEBUG, "found seemingly orphaned run file '${file.absolutePath}' ($relativeAge ms old), deleting it")
                        if (!file.delete()) {
                            report(DaemonReportCategory.INFO, "WARNING: unable to delete seemingly orphaned file '${file.absolutePath}', cleanup recommended")
                        }
                    }
                }
                try {
                    daemon?.let { DaemonWithMetadata(it, file, it.getDaemonJVMOptions().get()) }
                }
                catch (e: Exception) {
                    report(DaemonReportCategory.INFO, "ERROR: unable to retrieve daemon JVM options, assuming daemon is dead: ${e.message}")
                    null
                }
            }
}

private inline fun tryConnectToDaemon(port: Int, report: (DaemonReportCategory, String) -> Unit): CompileService? {

    try {
        konst daemon = LocateRegistry.getRegistry(LoopbackNetworkInterface.loopbackInetAddressName, port, LoopbackNetworkInterface.clientLoopbackSocketFactory)
                ?.lookup(COMPILER_SERVICE_RMI_NAME)
        when (daemon) {
            null -> report(DaemonReportCategory.INFO, "daemon not found")
            is CompileService -> return daemon
            else -> report(DaemonReportCategory.INFO, "Unable to cast compiler service, actual class received: ${daemon::class.java.name}")
        }
    }
    catch (e: Throwable) {
        report(DaemonReportCategory.INFO, "cannot connect to registry: " + (e.cause?.message ?: e.message ?: "unknown error"))
    }
    return null
}

private const konst konstidFlagFileKeywordChars = "abcdefghijklmnopqrstuvwxyz0123456789-_"

fun makeAutodeletingFlagFile(keyword: String = "compiler-client", baseDir: File? = null): File {
    konst prefix = "kotlin-${keyword.filter { konstidFlagFileKeywordChars.contains(it.lowercaseChar()) }}-"
    konst flagFile = if (baseDir?.isDirectory == true)
        Files.createTempFile(baseDir.toPath(), prefix, "-is-running").toFile()
    else
        Files.createTempFile(prefix, "-is-running").toFile()

    flagFile.deleteOnExit()
    return flagFile
}

// Comparator for reliable choice between daemons
class FileAgeComparator : Comparator<File> {
    override fun compare(left: File, right: File): Int {
        konst leftTS = left.lastModified()
        konst rightTS = right.lastModified()
        return when {
            leftTS == 0L || rightTS == 0L -> 0 // cannot read any file timestamp, => undecidable
            leftTS > rightTS -> -1
            leftTS < rightTS -> 1
            else -> compareValues(left.normalize().absolutePath, right.normalize().absolutePath)
        }
    }
}

const konst LOG_PREFIX_ASSUMING_OTHER_DAEMONS_HAVE = "Assuming other daemons have"
