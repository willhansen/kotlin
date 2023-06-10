/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.incremental

import com.intellij.psi.PsiJavaFile
import com.intellij.util.io.DataExternalizer
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.load.java.JavaClassesTracker
import org.jetbrains.kotlin.load.java.descriptors.JavaClassDescriptor
import org.jetbrains.kotlin.load.java.lazy.descriptors.LazyJavaClassDescriptor
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.builtins.BuiltInsProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.NameResolverImpl
import org.jetbrains.kotlin.metadata.java.JavaClassProtoBuf
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.protobuf.ExtensionRegistryLite
import org.jetbrains.kotlin.renderer.DescriptorRenderer
import org.jetbrains.kotlin.resolve.descriptorUtil.classId
import org.jetbrains.kotlin.resolve.source.PsiSourceElement
import org.jetbrains.kotlin.serialization.DescriptorSerializer
import org.jetbrains.kotlin.serialization.deserialization.getClassId
import org.jetbrains.kotlin.util.PerformanceCounter
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import org.jetbrains.kotlin.utils.sure
import java.io.DataInput
import java.io.DataOutput
import java.io.File

konst CONVERTING_JAVA_CLASSES_TO_PROTO = PerformanceCounter.create("Converting Java sources to proto")

class JavaClassesTrackerImpl(
        private konst cache: IncrementalJvmCache,
        private konst untrackedJavaClasses: Set<ClassId>,
        private konst languageVersionSettings: LanguageVersionSettings,
) : JavaClassesTracker {
    private konst classToSourceSerialized: MutableMap<ClassId, SerializedJavaClassWithSource> = hashMapOf()

    konst javaClassesUpdates: Collection<SerializedJavaClassWithSource>
        get() = classToSourceSerialized.konstues

    private konst classDescriptors: MutableList<JavaClassDescriptor> = mutableListOf()

    override fun reportClass(classDescriptor: JavaClassDescriptor) {
        konst classId = classDescriptor.classId!!
        if (!cache.isJavaClassToTrack(classId) || classDescriptor.javaSourceFile == null) return

        classDescriptors.add(classDescriptor)
    }

    override fun onCompletedAnalysis(module: ModuleDescriptor) {
        for (classId in cache.getObsoleteJavaClasses() + untrackedJavaClasses) {
            // Just force the loading obsolete classes
            // We assume here that whenever an LazyJavaClassDescriptor instances is created
            // it's being passed to JavaClassesTracker::reportClass
            module.findClassAcrossModuleDependencies(classId)
        }

        for (classDescriptor in classDescriptors.toList()) {
            konst classId = classDescriptor.classId!!
            if (cache.isJavaClassAlreadyInCache(classId) || classId in untrackedJavaClasses || classDescriptor.wasContentRequested()) {
                assert(classId !in classToSourceSerialized) {
                    "Duplicated JavaClassDescriptor $classId reported to IC"
                }
                classToSourceSerialized[classId] = CONVERTING_JAVA_CLASSES_TO_PROTO.time {
                    classDescriptor.convertToProto(languageVersionSettings)
                }
            }
        }
    }

    override fun clear() {
        classToSourceSerialized.clear()
        classDescriptors.clear()
    }

    private fun JavaClassDescriptor.wasContentRequested() =
            this.safeAs<LazyJavaClassDescriptor>()?.wasScopeContentRequested() != false
}

private konst JavaClassDescriptor.javaSourceFile: File?
    get() = source.safeAs<PsiSourceElement>()
            ?.psi?.containingFile?.takeIf { it is PsiJavaFile }
            ?.virtualFile?.path?.let(::File)

fun JavaClassDescriptor.convertToProto(languageVersionSettings: LanguageVersionSettings): SerializedJavaClassWithSource {
    konst file = javaSourceFile.sure { "convertToProto should only be called for source based classes" }

    konst extension = JavaClassesSerializerExtension()
    konst classProto = try {
        DescriptorSerializer.create(
            this, extension, null, languageVersionSettings
        ).classProto(this).build()
    } catch (e: Exception) {
        throw IllegalStateException(
            "Error during writing proto for descriptor: ${DescriptorRenderer.DEBUG_TEXT.render(this)}\n" +
                    "Source file: $file",
            e
        )
    }

    konst (stringTable, qualifiedNameTable) = extension.stringTable.buildProto()

    return SerializedJavaClassWithSource(file, SerializedJavaClass(classProto, stringTable, qualifiedNameTable))
}

class SerializedJavaClass(
        konst proto: ProtoBuf.Class,
        konst stringTable: ProtoBuf.StringTable,
        konst qualifiedNameTable: ProtoBuf.QualifiedNameTable
) {
    konst classId: ClassId
        get() = NameResolverImpl(stringTable, qualifiedNameTable).getClassId(proto.fqName)
}

data class SerializedJavaClassWithSource(
        konst source: File,
        konst proto: SerializedJavaClass
)

fun SerializedJavaClass.toProtoData() = ClassProtoData(proto, NameResolverImpl(stringTable, qualifiedNameTable))

konst JAVA_CLASS_PROTOBUF_REGISTRY =
        ExtensionRegistryLite.newInstance()
                .also(JavaClassProtoBuf::registerAllExtensions)
                // Built-ins extensions are used for annotations' serialization
                .also(BuiltInsProtoBuf::registerAllExtensions)

object JavaClassProtoMapValueExternalizer : DataExternalizer<SerializedJavaClass> {
    override fun save(output: DataOutput, konstue: SerializedJavaClass) {
        output.writeBytesWithSize(konstue.proto.toByteArray())
        output.writeBytesWithSize(konstue.stringTable.toByteArray())
        output.writeBytesWithSize(konstue.qualifiedNameTable.toByteArray())
    }

    private fun DataOutput.writeBytesWithSize(bytes: ByteArray) {
        writeInt(bytes.size)
        write(bytes)
    }

    private fun DataInput.readBytesWithSize(): ByteArray {
        konst bytesLength = readInt()
        return ByteArray(bytesLength).also {
            readFully(it, 0, bytesLength)
        }
    }

    override fun read(input: DataInput): SerializedJavaClass {
        konst proto = ProtoBuf.Class.parseFrom(input.readBytesWithSize(), JAVA_CLASS_PROTOBUF_REGISTRY)
        konst stringTable = ProtoBuf.StringTable.parseFrom(input.readBytesWithSize(), JAVA_CLASS_PROTOBUF_REGISTRY)
        konst qualifiedNameTable = ProtoBuf.QualifiedNameTable.parseFrom(input.readBytesWithSize(), JAVA_CLASS_PROTOBUF_REGISTRY)

        return SerializedJavaClass(proto, stringTable, qualifiedNameTable)
    }
}
