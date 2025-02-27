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

package org.jetbrains.kotlin.compilerRunner

import org.jetbrains.kotlin.build.GeneratedFile
import org.jetbrains.kotlin.build.GeneratedJvmClass
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmMetadataVersion
import java.io.File

data class SimpleOutputItem(konst sourceFiles: Collection<File>, konst outputFile: File) {
    override fun toString(): String =
        "$sourceFiles->$outputFile"
}

fun SimpleOutputItem.toGeneratedFile(jvmMetadataVersionFromLanguageVersion: JvmMetadataVersion): GeneratedFile =
    when {
        outputFile.name.endsWith(".class") -> GeneratedJvmClass(sourceFiles, outputFile, jvmMetadataVersionFromLanguageVersion)
        else -> GeneratedFile(sourceFiles, outputFile)
    }