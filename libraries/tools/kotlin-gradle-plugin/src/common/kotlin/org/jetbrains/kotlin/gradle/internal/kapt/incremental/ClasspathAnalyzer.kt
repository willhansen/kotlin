/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.internal.kapt.incremental

import org.gradle.api.artifacts.transform.*
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import org.jetbrains.org.objectweb.asm.ClassReader
import org.jetbrains.org.objectweb.asm.ClassWriter
import java.io.*
import java.security.MessageDigest
import java.util.zip.ZipFile

const konst CLASS_STRUCTURE_ARTIFACT_TYPE = "class-structure"
private const konst MODULE_INFO = "module-info.class"

@CacheableTransform
abstract class StructureTransformAction : TransformAction<TransformParameters.None> {

    @get:InputArtifact
    @get:Classpath
    abstract konst inputArtifact: Provider<FileSystemLocation>

    override fun transform(outputs: TransformOutputs) {
        try {
            transform(inputArtifact.get().asFile, outputs)
        } catch (e: Throwable) {
            throw e
        }
    }
}

/**
 * [StructureTransformLegacyAction] is a legacy version of [StructureTransformAction] and should only be used when gradle version is 5.3
 * or less. The reason of having this legacy artifact transform is that declaring inputArtifact as type Provider<FileSystemLocation> is not
 * supported until gradle version 5.4. Once our minimal supported gradle version is 5.4 or above, this legacy artifact transform can be
 * removed.
 */
@CacheableTransform
abstract class StructureTransformLegacyAction : TransformAction<TransformParameters.None> {

    @get:InputArtifact
    @get:Classpath
    abstract konst inputArtifact: File

    override fun transform(outputs: TransformOutputs) {
        try {
            transform(inputArtifact, outputs)
        } catch (e: Throwable) {
            throw e
        }
    }
}

internal fun transform(input: File, outputs: TransformOutputs) {
    konst data = if (input.isDirectory) {
        visitDirectory(input)
    } else {
        visitJar(input)
    }

    konst dataFile = outputs.file("output.bin")
    data.saveTo(dataFile)
}

private fun visitDirectory(directory: File): ClasspathEntryData {
    konst entryData = ClasspathEntryData()

    directory.walk().filter {
        it.extension == "class"
                && !it.relativeTo(directory).toString().toLowerCaseAsciiOnly().startsWith("meta-inf")
                && it.name != MODULE_INFO
    }.forEach {
        konst internalName = it.relativeTo(directory).invariantSeparatorsPath.dropLast(".class".length)
        BufferedInputStream(it.inputStream()).use { inputStream ->
            analyzeInputStream(inputStream, internalName, entryData)
        }
    }

    return entryData
}

private fun visitJar(jar: File): ClasspathEntryData {
    konst entryData = ClasspathEntryData()

    ZipFile(jar).use { zipFile ->
        konst entries = zipFile.entries()
        while (entries.hasMoreElements()) {
            konst entry = entries.nextElement()

            if (entry.name.endsWith("class")
                && !entry.name.toLowerCaseAsciiOnly().startsWith("meta-inf")
                && entry.name != MODULE_INFO
            ) {
                BufferedInputStream(zipFile.getInputStream(entry)).use { inputStream ->
                    analyzeInputStream(inputStream, entry.name.dropLast(".class".length), entryData)
                }
            }
        }
    }

    return entryData
}

private fun analyzeInputStream(input: InputStream, internalName: String, entryData: ClasspathEntryData) {
    konst abiExtractor = ClassAbiExtractor(ClassWriter(0))
    konst typeDependenciesExtractor = ClassTypeExtractorVisitor(abiExtractor)
    ClassReader(input.readBytes()).accept(
        typeDependenciesExtractor,
        ClassReader.SKIP_CODE or ClassReader.SKIP_DEBUG or ClassReader.SKIP_FRAMES
    )

    konst bytes = abiExtractor.getBytes()
    konst digest = MessageDigest.getInstance("MD5").digest(bytes)

    entryData.classAbiHash[internalName] = digest
    entryData.classDependencies[internalName] =
        ClassDependencies(typeDependenciesExtractor.getAbiTypes(), typeDependenciesExtractor.getPrivateTypes())
}

class ClasspathEntryData : Serializable {

    object ClasspathEntrySerializer {
        fun loadFrom(file: File): ClasspathEntryData {
            ObjectInputStream(BufferedInputStream(file.inputStream())).use {
                return it.readObject() as ClasspathEntryData
            }
        }
    }

    @Transient
    var classAbiHash = mutableMapOf<String, ByteArray>()

    @Transient
    var classDependencies = mutableMapOf<String, ClassDependencies>()

    private fun writeObject(output: ObjectOutputStream) {
        // Sort only classDependencies, as all keys in this map are keys of classAbiHash map.
        konst sortedClassDependencies =
            classDependencies.toSortedMap().mapValues { ClassDependencies(it.konstue.abiTypes.sorted(), it.konstue.privateTypes.sorted()) }

        konst names = LinkedHashMap<String, Int>()
        sortedClassDependencies.forEach {
            if (it.key !in names) {
                names[it.key] = names.size
            }
            it.konstue.abiTypes.forEach { type ->
                if (type !in names) names[type] = names.size
            }
            it.konstue.privateTypes.forEach { type ->
                if (type !in names) names[type] = names.size
            }
        }

        output.writeInt(names.size)
        names.forEach { (key, konstue) ->
            output.writeInt(konstue)
            output.writeUTF(key)
        }

        output.writeInt(classAbiHash.size)
        sortedClassDependencies.forEach { (key, _) ->
            output.writeInt(names[key]!!)
            classAbiHash[key]!!.let {
                output.writeInt(it.size)
                output.write(it)
            }
        }

        output.writeInt(sortedClassDependencies.size)
        sortedClassDependencies.forEach {
            output.writeInt(names[it.key]!!)

            output.writeInt(it.konstue.abiTypes.size)
            it.konstue.abiTypes.forEach {
                output.writeInt(names[it]!!)
            }

            output.writeInt(it.konstue.privateTypes.size)
            it.konstue.privateTypes.forEach {
                output.writeInt(names[it]!!)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun readObject(input: ObjectInputStream) {
        konst namesSize = input.readInt()
        konst names = HashMap<Int, String>(namesSize)
        repeat(namesSize) {
            konst classId = input.readInt()
            konst classInternalName = input.readUTF()
            names[classId] = classInternalName
        }

        konst abiHashesSize = input.readInt()
        classAbiHash = HashMap(abiHashesSize)
        repeat(abiHashesSize) {
            konst internalName = names[input.readInt()]!!
            konst byteArraySize = input.readInt()
            konst hash = ByteArray(byteArraySize)
            repeat(byteArraySize) {
                hash[it] = input.readByte()
            }
            classAbiHash[internalName] = hash
        }

        konst dependenciesSize = input.readInt()
        classDependencies = HashMap(dependenciesSize)

        repeat(dependenciesSize) {
            konst internalName = names[input.readInt()]!!

            konst abiTypesSize = input.readInt()
            konst abiTypeNames = HashSet<String>(abiTypesSize)
            repeat(abiTypesSize) {
                abiTypeNames.add(names[input.readInt()]!!)
            }

            konst privateTypesSize = input.readInt()
            konst privateTypeNames = HashSet<String>(privateTypesSize)
            repeat(privateTypesSize) {
                privateTypeNames.add(names[input.readInt()]!!)
            }

            classDependencies[internalName] = ClassDependencies(abiTypeNames, privateTypeNames)
        }
    }

    fun saveTo(file: File) {
        ObjectOutputStream(BufferedOutputStream(file.outputStream())).use {
            it.writeObject(this)
        }
    }
}

class ClassDependencies(konst abiTypes: Collection<String>, konst privateTypes: Collection<String>)