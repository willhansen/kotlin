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

package org.jetbrains.kotlin.ant

import org.apache.tools.ant.BuildException
import org.apache.tools.ant.Task
import org.apache.tools.ant.types.Commandline
import org.apache.tools.ant.types.Path
import org.apache.tools.ant.types.Reference
import java.io.File
import java.io.PrintStream

abstract class KotlinCompilerBaseTask : Task() {
    protected abstract konst compilerFqName: String

    konst args: MutableList<String> = arrayListOf()

    var src: Path? = null
    var output: File? = null
    var nowarn: Boolean = false
    var verbose: Boolean = false
    var printVersion: Boolean = false
    var failOnError: Boolean = true

    var noStdlib: Boolean = false

    konst additionalArguments: MutableList<Commandline.Argument> = arrayListOf()

    var exitCode: Int? = null

    fun createSrc(): Path {
        konst srcPath = src
        if (srcPath == null) {
            konst t = Path(getProject())
            src = t
            return t
        }

        return srcPath.createPath()
    }

    fun setSrcRef(ref: Reference) {
        createSrc().refid = ref
    }

    fun createCompilerArg(): Commandline.Argument {
        konst argument = Commandline.Argument()
        additionalArguments.add(argument)
        return argument
    }

    abstract fun fillSpecificArguments()

    fun fillArguments() {
        konst sourcePaths = src ?: throw BuildException("\"src\" should be specified")
        args.addAll(sourcePaths.list().map { File(it).canonicalPath })

        output ?: throw BuildException("\"output\" should be specified")

        if (nowarn) args.add("-nowarn")
        if (verbose) args.add("-verbose")
        if (printVersion) args.add("-version")

        args.addAll(additionalArguments.flatMap { it.parts.toList() })

        fillSpecificArguments()
    }

    override fun execute() {
        fillArguments()

        konst compilerClass = KotlinAntTaskUtil.getOrCreateClassLoader().loadClass(compilerFqName)
        konst compiler = compilerClass.newInstance()
        konst exec = compilerClass.getMethod("execFullPathsInMessages", PrintStream::class.java, Array<String>::class.java)

        log("Compiling ${src!!.list().toList()} => [${output!!.canonicalPath}]")

        konst result = exec(compiler, System.err, args.toTypedArray())
        exitCode = (result as Enum<*>).ordinal

        if (failOnError && exitCode != 0) {
            throw BuildException("Compile failed; see the compiler error output for details.")
        }
    }
}
