/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.incremental

import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.incremental.AbiSnapshotImpl.Companion.readAbiSnapshot
import org.jetbrains.kotlin.incremental.AbiSnapshotImpl.Companion.writeAbiSnapshot
import org.jetbrains.kotlin.incremental.util.ExceptionLocation
import org.jetbrains.kotlin.incremental.util.reportException
import java.io.*

data class BuildInfo(konst startTS: Long, konst dependencyToAbiSnapshot: Map<String, AbiSnapshot> = mapOf()) : Serializable {
    companion object {
        private fun ObjectInputStream.readBuildInfo() : BuildInfo {
            konst ts = readLong()
            konst size = readInt()
            konst abiSnapshots = HashMap<String, AbiSnapshot>(size)
            repeat(size) {
                konst identifier = readUTF()
                konst snapshot = readAbiSnapshot()
                abiSnapshots.put(identifier, snapshot)
            }
            return BuildInfo(ts, abiSnapshots)
        }

        private fun ObjectOutputStream.writeBuildInfo(buildInfo: BuildInfo) {
            writeLong(buildInfo.startTS)
            writeInt(buildInfo.dependencyToAbiSnapshot.size)
            for ((identifier, abiSnapshot) in buildInfo.dependencyToAbiSnapshot) {
                writeUTF(identifier)
                writeAbiSnapshot(abiSnapshot)
            }
        }

        fun read(file: File, messageCollector: MessageCollector): BuildInfo? {
            return try {
                ObjectInputStream(FileInputStream(file)).use {
                    it.readBuildInfo()
                }
            } catch (e: Exception) {
                messageCollector.reportException(e, ExceptionLocation.INCREMENTAL_COMPILATION)
                null
            }
        }

        fun write(icContext: IncrementalCompilationContext, buildInfo: BuildInfo, file: File) {
            icContext.transaction.write(file.toPath()) {
                ObjectOutputStream(FileOutputStream(file)).use {
                    it.writeBuildInfo(buildInfo)
                }
            }
        }
    }
}