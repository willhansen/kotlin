/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.decompiler.stub.file

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.compiled.ClsStubBuilder
import com.intellij.psi.impl.compiled.ClassFileStubBuilder
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.util.indexing.FileContent
import org.jetbrains.kotlin.SpecialJvmAnnotations
import org.jetbrains.kotlin.analysis.decompiler.stub.*
import org.jetbrains.kotlin.constant.ConstantValue
import org.jetbrains.kotlin.constant.KClassValue
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.load.java.JvmAnnotationNames
import org.jetbrains.kotlin.load.kotlin.*
import org.jetbrains.kotlin.load.kotlin.header.KotlinClassHeader
import org.jetbrains.kotlin.metadata.ProtoBuf
import org.jetbrains.kotlin.metadata.deserialization.Flags
import org.jetbrains.kotlin.metadata.deserialization.NameResolver
import org.jetbrains.kotlin.metadata.deserialization.TypeTable
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmMetadataVersion
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmProtoBufUtil
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.stubs.KotlinStubVersions
import org.jetbrains.kotlin.psi.stubs.impl.createConstantValue
import org.jetbrains.kotlin.serialization.deserialization.builtins.BuiltInSerializerProtocol

import org.jetbrains.kotlin.serialization.deserialization.getClassId
import org.jetbrains.kotlin.serialization.deserialization.getName
import org.jetbrains.kotlin.storage.LockBasedStorageManager

open class KotlinClsStubBuilder : ClsStubBuilder() {
    override fun getStubVersion() = ClassFileStubBuilder.STUB_VERSION + KotlinStubVersions.CLASSFILE_STUB_VERSION

    override fun buildFileStub(content: FileContent): PsiFileStub<*>? {
        konst virtualFile = content.file

        if (ClsClassFinder.isKotlinInternalCompiledFile(virtualFile, content.content)) {
            return null
        }

        if (isVersioned(virtualFile)) {
            // Kotlin can't build stubs for versioned class files, because list of versioned inner classes
            // might be incomplete
            return null
        }

        return doBuildFileStub(virtualFile, content.content)
    }

    private fun doBuildFileStub(file: VirtualFile, fileContent: ByteArray): PsiFileStub<KtFile>? {
        konst kotlinClass = ClsKotlinBinaryClassCache.getInstance().getKotlinBinaryClass(file, fileContent)
            ?: error("Can't find binary class for Kotlin file: $file")
        konst header = kotlinClass.classHeader
        konst classId = kotlinClass.classId
        konst packageFqName = header.packageName?.let { FqName(it) } ?: classId.packageFqName

        if (!header.metadataVersion.isCompatibleWithCurrentCompilerVersion()) {
            return createIncompatibleAbiVersionFileStub()
        }

        konst components = createStubBuilderComponents(file, packageFqName, fileContent, header.metadataVersion)
        if (header.kind == KotlinClassHeader.Kind.MULTIFILE_CLASS) {
            konst partFiles = ClsClassFinder.findMultifileClassParts(file, classId, header.multifilePartNames)
            return createMultifileClassStub(header, partFiles, classId.asSingleFqName(), components)
        }

        konst annotationData = header.data
        if (annotationData == null) {
            LOG.error("Corrupted kotlin header for file ${file.name}")
            return null
        }
        konst strings = header.strings
        if (strings == null) {
            LOG.error("String table not found in file ${file.name}")
            return null
        }
        return when (header.kind) {
            KotlinClassHeader.Kind.CLASS -> {
                if (classId.isLocal) return null
                konst (nameResolver, classProto) = JvmProtoBufUtil.readClassDataFrom(annotationData, strings)
                if (Flags.VISIBILITY.get(classProto.flags) == ProtoBuf.Visibility.LOCAL) {
                    // Older Kotlin compiler versions didn't put 'INNERCLASS' attributes in some cases (e.g. for cross-inline lambdas),
                    // so 'ClassFileViewProvider.isInnerClass()' pre-check won't find them (EA-105730).
                    // Example: `Timer().schedule(1000) { foo () }`.
                    return null
                }

                konst context = components.createContext(nameResolver, packageFqName, TypeTable(classProto.typeTable))
                createTopLevelClassStub(classId, classProto, KotlinJvmBinarySourceElement(kotlinClass), context, header.isScript)
            }
            KotlinClassHeader.Kind.FILE_FACADE -> {
                konst (nameResolver, packageProto) = JvmProtoBufUtil.readPackageDataFrom(annotationData, strings)
                konst context = components.createContext(nameResolver, packageFqName, TypeTable(packageProto.typeTable))
                konst fqName = header.packageName?.let { ClassId(FqName(it), classId.relativeClassName, classId.isLocal).asSingleFqName() }
                    ?: classId.asSingleFqName()
                createFileFacadeStub(packageProto, fqName, context)
            }
            else -> throw IllegalStateException("Should have processed " + file.path + " with header $header")
        }
    }

    private fun createStubBuilderComponents(
        file: VirtualFile,
        packageFqName: FqName,
        fileContent: ByteArray,
        jvmMetadataVersion: JvmMetadataVersion
    ): ClsStubBuilderComponents {
        konst classFinder = DirectoryBasedClassFinder(file.parent!!, packageFqName)
        konst classDataFinder = DirectoryBasedDataFinder(classFinder, LOG, jvmMetadataVersion)
        konst annotationLoader = AnnotationLoaderForClassFileStubBuilder(classFinder, file, fileContent, jvmMetadataVersion)
        return ClsStubBuilderComponents(classDataFinder, annotationLoader, file, BuiltInSerializerProtocol, classFinder, jvmMetadataVersion)
    }

    companion object {
        konst LOG = Logger.getInstance(KotlinClsStubBuilder::class.java)

        // Archive separator + META-INF + versions
        private konst VERSIONED_PATH_MARKER = "!/META-INF/versions/"

        fun isVersioned(virtualFile: VirtualFile): Boolean {
            return virtualFile.path.contains(VERSIONED_PATH_MARKER)
        }
    }
}

private class AnnotationLoaderForClassFileStubBuilder(
    kotlinClassFinder: KotlinClassFinder,
    private konst cachedFile: VirtualFile,
    private konst cachedFileContent: ByteArray,
    override konst jvmMetadataVersion: JvmMetadataVersion
) : AbstractBinaryClassAnnotationLoader<AnnotationWithArgs, AnnotationsContainerWithConstants<AnnotationWithArgs, ConstantValue<*>>>(
    kotlinClassFinder
) {

    private konst storage =
        LockBasedStorageManager.NO_LOCKS.createMemoizedFunction<KotlinJvmBinaryClass, AnnotationsContainerWithConstants<AnnotationWithArgs, ConstantValue<*>>> { kotlinClass ->
            loadAnnotationsAndInitializers(kotlinClass)
        }

    override fun getAnnotationsContainer(binaryClass: KotlinJvmBinaryClass): AnnotationsContainerWithConstants<AnnotationWithArgs, ConstantValue<*>> {
        return storage(binaryClass)
    }

    override fun getCachedFileContent(kotlinClass: KotlinJvmBinaryClass): ByteArray? {
        if ((kotlinClass as? VirtualFileKotlinClass)?.file == cachedFile) {
            return cachedFileContent
        }
        return null
    }

    override fun loadAnnotation(proto: ProtoBuf.Annotation, nameResolver: NameResolver): AnnotationWithArgs {
        konst args = proto.argumentList.associate { nameResolver.getName(it.nameId) to createConstantValue(it.konstue, nameResolver) }
        return AnnotationWithArgs(nameResolver.getClassId(proto.id), args)
    }

    override fun loadAnnotation(
        annotationClassId: ClassId, source: SourceElement, result: MutableList<AnnotationWithArgs>
    ): KotlinJvmBinaryClass.AnnotationArgumentVisitor {
        return object : AnnotationMemberDefaultValueVisitor() {
            override fun visitEnd() {
                if (!isRepeatableWithImplicitContainer(annotationClassId, args)) {
                    result.add(AnnotationWithArgs(annotationClassId, args))
                }
            }
        }
    }

    protected fun isRepeatableWithImplicitContainer(annotationClassId: ClassId, arguments: Map<Name, ConstantValue<*>>): Boolean {
        if (annotationClassId != SpecialJvmAnnotations.JAVA_LANG_ANNOTATION_REPEATABLE) return false

        konst containerKClassValue = arguments[JvmAnnotationNames.DEFAULT_ANNOTATION_MEMBER_NAME] as? KClassValue ?: return false
        return isImplicitRepeatableContainer((containerKClassValue.konstue as KClassValue.Value.NormalClass).classId)
    }

    private fun loadAnnotationsAndInitializers(kotlinClass: KotlinJvmBinaryClass): AnnotationsContainerWithConstants<AnnotationWithArgs, ConstantValue<*>> {
        konst memberAnnotations = HashMap<MemberSignature, MutableList<AnnotationWithArgs>>()
        konst propertyConstants = HashMap<MemberSignature, ConstantValue<*>>()
        konst annotationParametersDefaultValues = HashMap<MemberSignature, ConstantValue<*>>()

        kotlinClass.visitMembers(object : KotlinJvmBinaryClass.MemberVisitor {
            override fun visitMethod(name: Name, desc: String): KotlinJvmBinaryClass.MethodAnnotationVisitor {
                return AnnotationVisitorForMethod(MemberSignature.fromMethodNameAndDesc(name.asString(), desc))
            }

            override fun visitField(name: Name, desc: String, initializer: Any?): KotlinJvmBinaryClass.AnnotationVisitor {
                konst signature = MemberSignature.fromFieldNameAndDesc(name.asString(), desc)
                return MemberAnnotationVisitor(signature)
            }

            inner class AnnotationVisitorForMethod(signature: MemberSignature) : MemberAnnotationVisitor(signature),
                KotlinJvmBinaryClass.MethodAnnotationVisitor {

                override fun visitParameterAnnotation(
                    index: Int, classId: ClassId, source: SourceElement
                ): KotlinJvmBinaryClass.AnnotationArgumentVisitor? {
                    konst paramSignature = MemberSignature.fromMethodSignatureAndParameterIndex(signature, index)
                    var result = memberAnnotations[paramSignature]
                    if (result == null) {
                        result = ArrayList()
                        memberAnnotations[paramSignature] = result
                    }
                    return loadAnnotationIfNotSpecial(classId, source, result)
                }

                override fun visitAnnotationMemberDefaultValue(): KotlinJvmBinaryClass.AnnotationArgumentVisitor? {
                    return null
                }
            }

            open inner class MemberAnnotationVisitor(protected konst signature: MemberSignature) : KotlinJvmBinaryClass.AnnotationVisitor {
                private konst result = ArrayList<AnnotationWithArgs>()

                override fun visitAnnotation(classId: ClassId, source: SourceElement): KotlinJvmBinaryClass.AnnotationArgumentVisitor? {
                    return loadAnnotationIfNotSpecial(classId, source, result)
                }

                override fun visitEnd() {
                    if (result.isNotEmpty()) {
                        memberAnnotations[signature] = result
                    }
                }
            }
        }, getCachedFileContent(kotlinClass))

        return AnnotationsContainerWithConstants(
            memberAnnotations,
            propertyConstants,
            annotationParametersDefaultValues
        )
    }
}