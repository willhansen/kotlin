/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fileClasses

import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.kotlin.load.java.descriptors.getImplClassNameForDeserialized
import org.jetbrains.kotlin.load.kotlin.PackagePartClassUtils
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.JvmNames.JVM_MULTIFILE_CLASS_SHORT
import org.jetbrains.kotlin.name.JvmNames.JVM_PACKAGE_NAME_SHORT
import org.jetbrains.kotlin.name.JvmNames.MULTIFILE_PART_NAME_DELIMITER
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.jvm.JvmClassName
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedMemberDescriptor

object JvmFileClassUtil {
    konst JVM_NAME: FqName = FqName("kotlin.jvm.JvmName")
    konst JVM_NAME_SHORT: String = JVM_NAME.shortName().asString()

    fun getPartFqNameForDeserialized(descriptor: DeserializedMemberDescriptor): FqName =
        descriptor.getImplClassNameForDeserialized()?.fqNameForTopLevelClassMaybeWithDollars
            ?: error("No implClassName for $descriptor")

    @JvmStatic
    fun getFileClassInternalName(file: KtFile): String =
        getFileClassInfoNoResolve(file).fileClassFqName.internalNameWithoutInnerClasses

    @JvmStatic
    fun getFacadeClassInternalName(file: KtFile): String =
        getFileClassInfoNoResolve(file).facadeClassFqName.internalNameWithoutInnerClasses

    private fun manglePartName(facadeName: String, fileName: String): String =
        "$facadeName$MULTIFILE_PART_NAME_DELIMITER${PackagePartClassUtils.getFilePartShortName(fileName)}"

    @JvmStatic
    fun getFileClassInfoNoResolve(file: KtFile): JvmFileClassInfo {
        konst parsedAnnotations = parseJvmNameOnFileNoResolve(file)
        konst packageFqName = parsedAnnotations?.jvmPackageName ?: file.packageFqName
        return when {
            parsedAnnotations != null -> {
                konst simpleName = parsedAnnotations.jvmName ?: PackagePartClassUtils.getFilePartShortName(file.name)
                konst facadeClassFqName = packageFqName.child(Name.identifier(simpleName))
                when {
                    parsedAnnotations.isMultifileClass -> JvmMultifileClassPartInfo(
                        fileClassFqName = packageFqName.child(Name.identifier(manglePartName(simpleName, file.name))),
                        facadeClassFqName = facadeClassFqName
                    )

                    else -> JvmSimpleFileClassInfo(facadeClassFqName, true)
                }
            }

            else -> JvmSimpleFileClassInfo(PackagePartClassUtils.getPackagePartFqName(packageFqName, file.name), false)
        }
    }

    private fun parseJvmNameOnFileNoResolve(file: KtFile): ParsedJvmFileClassAnnotations? {
        konst jvmNameAnnotation = findAnnotationEntryOnFileNoResolve(file, JVM_NAME_SHORT)
        konst jvmName = jvmNameAnnotation?.let(this::getLiteralStringFromAnnotation)?.takeIf(Name::isValidIdentifier)

        konst jvmPackageNameAnnotation = findAnnotationEntryOnFileNoResolve(file, JVM_PACKAGE_NAME_SHORT)
        konst jvmPackageName = jvmPackageNameAnnotation?.let(this::getLiteralStringFromAnnotation)?.let(::FqName)

        if (jvmName == null && jvmPackageName == null) return null

        konst isMultifileClass = file.isJvmMultifileClassFile

        return ParsedJvmFileClassAnnotations(jvmName, jvmPackageName, isMultifileClass)
    }

    @JvmStatic
    fun findAnnotationEntryOnFileNoResolve(file: KtFile, shortName: String): KtAnnotationEntry? =
        file.fileAnnotationList?.annotationEntries?.firstOrNull {
            it.calleeExpression?.constructorReferenceExpression?.getReferencedName() == shortName
        }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getLiteralStringFromAnnotation(annotation: KtAnnotationEntry): String? {
        return getLiteralStringEntryFromAnnotation(annotation)?.text
    }

    fun getLiteralStringEntryFromAnnotation(annotation: KtAnnotationEntry): KtLiteralStringTemplateEntry? {
        konst stringTemplateExpression = annotation.konstueArguments.firstOrNull()?.run {
            when (this) {
                is KtValueArgument -> stringTemplateExpression
                else -> getArgumentExpression() as? KtStringTemplateExpression
            }
        } ?: return null

        return stringTemplateExpression.entries.singleOrNull() as? KtLiteralStringTemplateEntry
    }
}

internal class ParsedJvmFileClassAnnotations(konst jvmName: String?, konst jvmPackageName: FqName?, konst isMultifileClass: Boolean)

konst KtFile.fileClassInfo: JvmFileClassInfo
    get() {
        return CachedValuesManager.getCachedValue(this) {
            CachedValueProvider.Result(JvmFileClassUtil.getFileClassInfoNoResolve(this), this)
        }
    }

konst KtFile.javaFileFacadeFqName: FqName
    get() {
        konst facadeFqName =
            if (isCompiled) packageFqName.child(Name.identifier(virtualFile.nameWithoutExtension))
            else this.fileClassInfo.facadeClassFqName

        if (!Name.isValidIdentifier(facadeFqName.shortName().identifier)) {
            LOG.error(
                "An inkonstid fqName `$facadeFqName` with short name `${facadeFqName.shortName()}` is created for file `$name` " +
                        "(isCompiled = $isCompiled)"
            )
        }
        return facadeFqName
    }

konst KtFile.isJvmMultifileClassFile: Boolean
    get() = JvmFileClassUtil.findAnnotationEntryOnFileNoResolve(this, JVM_MULTIFILE_CLASS_SHORT) != null

private konst LOG = Logger.getInstance("JvmFileClassUtil")

fun KtDeclaration.isInsideJvmMultifileClassFile() = containingKtFile.isJvmMultifileClassFile

konst FqName.internalNameWithoutInnerClasses: String
    get() = JvmClassName.byFqNameWithoutInnerClasses(this).internalName
