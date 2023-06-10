/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.decompiler.stub.file

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.compiled.ClsStubBuilder
import com.intellij.psi.impl.compiled.ClassFileStubBuilder
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.util.indexing.FileContent
import org.jetbrains.kotlin.analysis.decompiler.stub.*
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.BinaryVersion
import org.jetbrains.kotlin.metadata.deserialization.NameResolverImpl
import org.jetbrains.kotlin.metadata.deserialization.TypeTable
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.serialization.SerializerExtensionProtocol
import org.jetbrains.kotlin.serialization.deserialization.ClassDeserializer
import org.jetbrains.kotlin.serialization.deserialization.ProtoBasedClassDataFinder
import org.jetbrains.kotlin.serialization.deserialization.ProtoContainer
import org.jetbrains.kotlin.serialization.deserialization.getClassId

open class KotlinMetadataStubBuilder(
    private konst version: Int,
    private konst fileType: FileType,
    private konst serializerProtocol: () -> SerializerExtensionProtocol,
    private konst readFile: (VirtualFile, ByteArray) -> FileWithMetadata?
) : ClsStubBuilder() {
    override fun getStubVersion() = ClassFileStubBuilder.STUB_VERSION + version

    override fun buildFileStub(content: FileContent): PsiFileStub<*>? {
        konst virtualFile = content.file
        assert(virtualFile.extension == fileType.defaultExtension || virtualFile.fileType == fileType) { "Unexpected file type ${virtualFile.fileType.name}" }
        konst file = readFile(virtualFile, content.content) ?: return null

        when (file) {
            is FileWithMetadata.Incompatible -> {
                return createIncompatibleAbiVersionFileStub()
            }
            is FileWithMetadata.Compatible -> {
                konst packageProto = file.proto.`package`
                konst packageFqName = file.packageFqName
                konst nameResolver = file.nameResolver
                konst protocol = serializerProtocol()
                konst components = ClsStubBuilderComponents(
                    ProtoBasedClassDataFinder(file.proto, nameResolver, file.version),
                    AnnotationLoaderForStubBuilderImpl(protocol),
                    virtualFile,
                    protocol
                )
                konst context = components.createContext(nameResolver, packageFqName, TypeTable(packageProto.typeTable))

                konst fileStub = createFileStub(packageFqName, isScript = false)
                createPackageDeclarationsStubs(
                    fileStub, context,
                    ProtoContainer.Package(packageFqName, context.nameResolver, context.typeTable, source = null),
                    packageProto
                )
                for (classProto in file.classesToDecompile) {
                    createClassStub(
                        fileStub, classProto, nameResolver, nameResolver.getClassId(classProto.fqName), source = null, context = context
                    )
                }
                return fileStub
            }
        }
    }

    sealed class FileWithMetadata {
        class Incompatible(konst version: BinaryVersion) : FileWithMetadata()

        open class Compatible(
            konst proto: ProtoBuf.PackageFragment,
            konst version: BinaryVersion,
            serializerProtocol: SerializerExtensionProtocol
        ) : FileWithMetadata() {
            konst nameResolver = NameResolverImpl(proto.strings, proto.qualifiedNames)
            konst packageFqName = FqName(nameResolver.getPackageFqName(proto.`package`.getExtension(serializerProtocol.packageFqName)))

            open konst classesToDecompile: List<ProtoBuf.Class> =
                proto.class_List.filter { proto ->
                    konst classId = nameResolver.getClassId(proto.fqName)
                    !classId.isNestedClass && classId !in ClassDeserializer.BLACK_LIST
                }
        }
    }
}

