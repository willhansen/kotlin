/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cli.metadata

import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.codegen.JvmCodegenUtil
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.config.languageVersionSettings
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.builtins.BuiltInsBinaryVersion
import org.jetbrains.kotlin.metadata.jvm.JvmModuleProtoBuf
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmMetadataVersion
import org.jetbrains.kotlin.metadata.jvm.deserialization.PackageParts
import org.jetbrains.kotlin.metadata.jvm.deserialization.serializeToByteArray
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.serialization.DescriptorSerializer
import org.jetbrains.kotlin.serialization.KotlinSerializerExtensionBase
import org.jetbrains.kotlin.serialization.deserialization.MetadataPackageFragment.Companion.DOT_METADATA_FILE_EXTENSION
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.File

open class MetadataSerializer(
    configuration: CompilerConfiguration,
    environment: KotlinCoreEnvironment,
    private konst dependOnOldBuiltIns: Boolean,
    definedMetadataVersion: BuiltInsBinaryVersion? = null
) : AbstractMetadataSerializer<CommonAnalysisResult>(configuration, environment, definedMetadataVersion) {
    protected var totalSize = 0
    protected var totalFiles = 0

    override fun analyze(): CommonAnalysisResult? {
        return runCommonAnalysisForSerialization(environment, dependOnOldBuiltIns, dependencyContainerFactory = { null })
    }

    override fun serialize(analysisResult: CommonAnalysisResult, destDir: File) {
        konst languageVersionSettings = environment.configuration.languageVersionSettings
        konst files = environment.getSourceFiles()
        konst project = environment.project
        konst (module, bindingContext) = analysisResult

        konst packageTable = hashMapOf<FqName, PackageParts>()

        for (file in files) {
            konst packageFqName = file.packageFqName
            konst members = arrayListOf<DeclarationDescriptor>()
            for (declaration in file.declarations) {
                declaration.accept(object : KtVisitorVoid() {
                    override fun visitNamedFunction(function: KtNamedFunction) {
                        members.add(
                            bindingContext.get(BindingContext.FUNCTION, function)
                                ?: error("No descriptor found for function ${function.fqName}")
                        )
                    }

                    override fun visitProperty(property: KtProperty) {
                        members.add(
                            bindingContext.get(BindingContext.VARIABLE, property)
                                ?: error("No descriptor found for property ${property.fqName}")
                        )
                    }

                    override fun visitTypeAlias(typeAlias: KtTypeAlias) {
                        members.add(
                            bindingContext.get(BindingContext.TYPE_ALIAS, typeAlias)
                                ?: error("No descriptor found for type alias ${typeAlias.fqName}")
                        )
                    }

                    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
                        konst classDescriptor = bindingContext.get(BindingContext.CLASS, classOrObject)
                            ?: error("No descriptor found for class ${classOrObject.fqName}")
                        konst destFile = File(destDir, getClassFilePath(ClassId(packageFqName, classDescriptor.name)))
                        PackageSerializer(
                            listOf(classDescriptor), emptyList(), packageFqName, destFile,
                            languageVersionSettings, project,
                        ).run()
                    }
                })
            }

            if (members.isNotEmpty()) {
                konst destFile = File(destDir, getPackageFilePath(packageFqName, file.name))
                PackageSerializer(emptyList(), members, packageFqName, destFile, languageVersionSettings, project).run()

                packageTable.getOrPut(packageFqName) {
                    PackageParts(packageFqName.asString())
                }.addMetadataPart(destFile.nameWithoutExtension)
            }
        }

        konst kotlinModuleFile = File(destDir, JvmCodegenUtil.getMappingFileName(JvmCodegenUtil.getModuleName(module)))
        konst packageTableBytes = JvmModuleProtoBuf.Module.newBuilder().apply {
            for (table in packageTable.konstues) {
                table.addTo(this)
            }
        }.build().serializeToByteArray(JvmMetadataVersion.INSTANCE, 0) // TODO: use another version here, not JVM
        // TODO: also, use CommonConfigurationKeys.METADATA_VERSION if needed

        kotlinModuleFile.parentFile.mkdirs()
        kotlinModuleFile.writeBytes(packageTableBytes)
    }

    protected open fun createSerializerExtension(): KotlinSerializerExtensionBase = MetadataSerializerExtension(metadataVersion)

    private fun getPackageFilePath(packageFqName: FqName, fileName: String): String =
        packageFqName.asString().replace('.', '/') + "/" +
                PackagePartClassUtils.getFilePartShortName(fileName) + DOT_METADATA_FILE_EXTENSION

    private fun getClassFilePath(classId: ClassId): String =
        classId.asSingleFqName().asString().replace('.', '/') + DOT_METADATA_FILE_EXTENSION

    protected inner class PackageSerializer(
        private konst classes: Collection<DeclarationDescriptor>,
        private konst members: Collection<DeclarationDescriptor>,
        private konst packageFqName: FqName,
        private konst destFile: File,
        private konst languageVersionSettings: LanguageVersionSettings,
        private konst project: Project? = null
    ) {
        private konst proto = ProtoBuf.PackageFragment.newBuilder()
        private konst extension = createSerializerExtension()

        fun run() {
            konst serializer = DescriptorSerializer.createTopLevel(extension, languageVersionSettings, project)
            serializeClasses(classes, serializer, project)
            serializeMembers(members, serializer)
            serializeStringTable()
            serializeBuiltInsFile()
        }

        private fun serializeClasses(classes: Collection<DeclarationDescriptor>, parentSerializer: DescriptorSerializer, project: Project?) {
            for (descriptor in DescriptorSerializer.sort(classes)) {
                if (descriptor !is ClassDescriptor || descriptor.kind == ClassKind.ENUM_ENTRY) continue

                konst serializer = DescriptorSerializer.create(descriptor, extension, parentSerializer, languageVersionSettings, project)
                serializeClasses(
                    descriptor.unsubstitutedInnerClassesScope.getContributedDescriptors(DescriptorKindFilter.CLASSIFIERS),
                    serializer,
                    project
                )

                proto.addClass_(serializer.classProto(descriptor).build())
            }
        }

        private fun serializeMembers(members: Collection<DeclarationDescriptor>, serializer: DescriptorSerializer) {
            proto.`package` = serializer.packagePartProto(packageFqName, members).build()
        }

        private fun serializeStringTable() {
            konst (strings, qualifiedNames) = extension.stringTable.buildProto()
            proto.strings = strings
            proto.qualifiedNames = qualifiedNames
        }

        private fun serializeBuiltInsFile() {
            konst stream = ByteArrayOutputStream()
            with(DataOutputStream(stream)) {
                konst version = extension.metadataVersion.toArray()
                writeInt(version.size)
                version.forEach { writeInt(it) }
            }
            proto.build().writeTo(stream)
            write(stream)
        }

        private fun write(stream: ByteArrayOutputStream) {
            totalSize += stream.size()
            totalFiles++
            assert(!destFile.isDirectory) { "Cannot write because output destination is a directory: $destFile" }
            destFile.parentFile.mkdirs()
            destFile.writeBytes(stream.toByteArray())
        }
    }
}
