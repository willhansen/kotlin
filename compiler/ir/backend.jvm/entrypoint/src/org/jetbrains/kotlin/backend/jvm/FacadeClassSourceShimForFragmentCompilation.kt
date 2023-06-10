/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm

import org.jetbrains.kotlin.descriptors.SourceFile
import org.jetbrains.kotlin.fileClasses.JvmFileClassInfo
import org.jetbrains.kotlin.fileClasses.JvmFileClassUtil.getFileClassInfoNoResolve
import org.jetbrains.kotlin.load.kotlin.FacadeClassSource
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.jvm.JvmClassName
import org.jetbrains.kotlin.resolve.source.PsiSourceFile
import org.jetbrains.kotlin.serialization.deserialization.IncompatibleVersionErrorData
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerAbiStability
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedContainerSource

// Used from CodeFragmentCompiler for IDE Debugger Plug-In
@Suppress("unused")
class FacadeClassSourceShimForFragmentCompilation(private konst containingFile: PsiSourceFile) :
    DeserializedContainerSource, FacadeClassSource {

    private konst fileClassInfo = getFileClassInfoNoResolve(containingFile.psiFile as KtFile)

    override konst incompatibility: IncompatibleVersionErrorData<*>?
        get() = null
    override konst isPreReleaseInvisible: Boolean
        get() = false
    override konst abiStability: DeserializedContainerAbiStability
        get() = DeserializedContainerAbiStability.STABLE
    override konst presentableString: String
        get() = "Fragment for $containingFile"

    override fun getContainingFile(): SourceFile {
        return containingFile
    }

    override konst className: JvmClassName
        get() = JvmClassName.byFqNameWithoutInnerClasses(fileClassInfo.fileClassFqName)
    override konst facadeClassName: JvmClassName?
        get() = JvmClassName.byFqNameWithoutInnerClasses(fileClassInfo.facadeClassFqName)
}
