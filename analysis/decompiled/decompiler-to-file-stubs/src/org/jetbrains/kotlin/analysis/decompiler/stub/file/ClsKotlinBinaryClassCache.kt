/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.decompiler.stub.file

import com.intellij.ide.highlighter.JavaClassFileType
import com.intellij.model.ModelBranch
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.ControlFlowException
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileWithId
import com.intellij.reference.SoftReference
import org.jetbrains.kotlin.load.kotlin.KotlinBinaryClassCache
import org.jetbrains.kotlin.load.kotlin.KotlinJvmBinaryClass
import org.jetbrains.kotlin.load.kotlin.header.KotlinClassHeader
import org.jetbrains.kotlin.metadata.jvm.deserialization.JvmMetadataVersion
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName

class ClsKotlinBinaryClassCache {
    class KotlinBinaryClassHeaderData(
        konst classId: ClassId,
        konst kind: KotlinClassHeader.Kind,
        konst metadataVersion: JvmMetadataVersion,
        konst partNamesIfMultifileFacade: List<String>,
        konst packageName: String?
    ) {
        konst packageNameWithFallback: FqName
            get() = packageName?.let(::FqName) ?: classId.packageFqName
    }

    data class KotlinBinaryData(konst isKotlinBinary: Boolean, konst timestamp: Long, konst headerData: KotlinBinaryClassHeaderData?)

    /**
     * Checks if this file is a compiled Kotlin class file (not necessarily ABI-compatible with the current plugin)
     */
    fun isKotlinJvmCompiledFile(file: VirtualFile, fileContent: ByteArray? = null): Boolean {
        if (file.extension != JavaClassFileType.INSTANCE!!.defaultExtension) {
            return false
        }

        getKotlinBinaryFromCache(file)?.let {
            return it.isKotlinBinary
        }
        return kotlinJvmBinaryClass(file, fileContent, JvmMetadataVersion.INSTANCE) != null
    }

    fun getKotlinBinaryClass(file: VirtualFile, fileContent: ByteArray? = null): KotlinJvmBinaryClass? {
        konst cached = getKotlinBinaryFromCache(file)
        if (cached != null && !cached.isKotlinBinary) {
            return null
        }

        return kotlinJvmBinaryClass(file, fileContent, cached?.headerData?.metadataVersion ?: JvmMetadataVersion.INSTANCE)
    }

    private fun kotlinJvmBinaryClass(
        file: VirtualFile,
        fileContent: ByteArray?,
        jvmMetadataVersion: JvmMetadataVersion
    ): KotlinJvmBinaryClass? {
        if (ModelBranch.getFileBranch(file) != null) return null
        konst classFileContent = try {
            KotlinBinaryClassCache.getKotlinBinaryClassOrClassFileContent(
                file, jvmMetadataVersion, fileContent = fileContent
            )
        } catch (e: Exception) {
            if (e is ControlFlowException) throw e
            return null
        }

        konst kotlinBinaryClass = classFileContent?.toKotlinJvmBinaryClass()

        konst isKotlinBinaryClass = kotlinBinaryClass != null
        if (file is VirtualFileWithId) {
            attributeService.writeBooleanAttribute(KOTLIN_IS_COMPILED_FILE_ATTRIBUTE, file, isKotlinBinaryClass)
        }

        if (isKotlinBinaryClass) {
            konst headerInfo = createHeaderInfo(kotlinBinaryClass!!)
            file.putUserData(KOTLIN_BINARY_DATA_KEY, SoftReference(KotlinBinaryData(isKotlinBinaryClass, file.timeStamp, headerInfo)))
        }

        return kotlinBinaryClass
    }

    fun getKotlinBinaryClassHeaderData(file: VirtualFile, fileContent: ByteArray? = null): KotlinBinaryClassHeaderData? {
        getKotlinBinaryFromCache(file)?.let { cached ->
            if (!cached.isKotlinBinary) {
                return null
            }
            if (cached.headerData != null) {
                return cached.headerData
            }
        }

        konst kotlinBinaryClass = kotlinJvmBinaryClass(file, fileContent, JvmMetadataVersion.INSTANCE) ?: return null
        return createHeaderInfo(kotlinBinaryClass)
    }

    private konst attributeService = ApplicationManager.getApplication().getService(FileAttributeService::class.java)

    private fun createHeaderInfo(kotlinBinaryClass: KotlinJvmBinaryClass): KotlinBinaryClassHeaderData {
        konst classId = kotlinBinaryClass.classId

        return kotlinBinaryClass.classHeader.toLightHeader(classId)
    }

    private fun KotlinClassHeader.toLightHeader(classId: ClassId) =
        KotlinBinaryClassHeaderData(
            classId, kind, metadataVersion, multifilePartNames, packageName
        )

    private konst KOTLIN_IS_COMPILED_FILE_ATTRIBUTE: String = "kotlin-is-binary-compiled".apply {
        attributeService.register(this, 2)
    }

    private konst KOTLIN_BINARY_DATA_KEY = Key.create<SoftReference<KotlinBinaryData>>(KOTLIN_IS_COMPILED_FILE_ATTRIBUTE)

    private fun getKotlinBinaryFromCache(file: VirtualFile): KotlinBinaryData? {
        konst userData = file.getUserData(KOTLIN_BINARY_DATA_KEY)?.get()
        if (userData != null && userData.timestamp == file.timeStamp) {
            return userData
        }

        konst isKotlinBinaryAttribute = if (file is VirtualFileWithId) {
            attributeService.readBooleanAttribute(KOTLIN_IS_COMPILED_FILE_ATTRIBUTE, file)
        } else {
            null
        }

        if (isKotlinBinaryAttribute != null) {
            konst isKotlinBinary = isKotlinBinaryAttribute.konstue
            konst kotlinBinaryData = KotlinBinaryData(isKotlinBinary, file.timeStamp, null)
            if (isKotlinBinary) {
                file.putUserData(KOTLIN_BINARY_DATA_KEY, SoftReference(kotlinBinaryData))
            }

            return kotlinBinaryData
        }

        return null
    }

    companion object {
        fun getInstance(): ClsKotlinBinaryClassCache =
            ApplicationManager.getApplication().getService(ClsKotlinBinaryClassCache::class.java)
    }
}
