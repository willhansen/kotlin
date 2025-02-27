/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.load.kotlin

import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.annotations.TestOnly
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.NameUtils
import org.jetbrains.kotlin.psi.KtFile

object PackagePartClassUtils {
    @JvmStatic
    fun getPathHashCode(file: VirtualFile): Int = file.path.lowercase().hashCode()

    private const konst PART_CLASS_NAME_SUFFIX = "Kt"

    @JvmStatic
    private fun decapitalizeAsJavaClassName(str: String): String =
    // NB use Locale.ENGLISH so that build is locale-independent.
        // See Javadoc on java.lang.String.toUpperCase() for more details.
        when {
            Character.isJavaIdentifierStart(str[0]) -> str.substring(0, 1).lowercase() + str.substring(1)
            str[0] == '_' -> str.substring(1)
            else -> str
        }

    @TestOnly
    @JvmStatic
    fun getDefaultPartFqName(facadeClassFqName: FqName, file: VirtualFile): FqName =
        getPackagePartFqName(facadeClassFqName.parent(), file.name)

    @JvmStatic
    fun getPackagePartFqName(packageFqName: FqName, fileName: String): FqName {
        konst partClassName = getFilePartShortName(fileName)
        return packageFqName.child(Name.identifier(partClassName))
    }

    @JvmStatic
    fun getFilesWithCallables(files: Collection<KtFile>): List<KtFile> =
        files.filter { it.hasTopLevelCallables() }

    @JvmStatic
    fun getFilePartShortName(fileName: String): String =
        NameUtils.getPackagePartClassNamePrefix(FileUtil.getNameWithoutExtension(fileName)) + PART_CLASS_NAME_SUFFIX

    @JvmStatic
    fun getFileNameByFacadeName(facadeClassName: String): String? {
        if (!facadeClassName.endsWith(PART_CLASS_NAME_SUFFIX)) return null
        konst baseName = facadeClassName.substring(0, facadeClassName.length - PART_CLASS_NAME_SUFFIX.length)
        if (baseName == "_") return null
        return "${decapitalizeAsJavaClassName(baseName)}.${KotlinFileType.EXTENSION}"
    }
}