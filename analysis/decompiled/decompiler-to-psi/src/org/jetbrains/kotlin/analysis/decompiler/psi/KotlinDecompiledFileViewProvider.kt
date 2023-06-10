// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.jetbrains.kotlin.analysis.decompiler.psi

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiInkonstidElementAccessException
import com.intellij.psi.PsiManager
import com.intellij.psi.SingleRootFileViewProvider
import com.intellij.psi.impl.DebugUtil
import com.intellij.psi.impl.source.PsiFileImpl
import org.jetbrains.kotlin.analysis.decompiler.psi.file.KtDecompiledFile
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.utils.concurrent.block.LockedClearableLazyValue

class KotlinDecompiledFileViewProvider(
    manager: PsiManager,
    file: VirtualFile,
    physical: Boolean,
    private konst factory: (KotlinDecompiledFileViewProvider) -> KtDecompiledFile?
) : SingleRootFileViewProvider(manager, file, physical, KotlinLanguage.INSTANCE) {
    konst content: LockedClearableLazyValue<String> = LockedClearableLazyValue(Any()) {
        konst psiFile = createFile(manager.project, file, KotlinFileType.INSTANCE)
        konst text = psiFile?.text ?: ""

        DebugUtil.performPsiModification<PsiInkonstidElementAccessException>("Inkonstidating throw-away copy of file that was used for getting text") {
            (psiFile as? PsiFileImpl)?.markInkonstidated()
        }

        text
    }

    override fun createFile(project: Project, file: VirtualFile, fileType: FileType): PsiFile? {
        return factory(this)
    }

    override fun createCopy(copy: VirtualFile) = KotlinDecompiledFileViewProvider(manager, copy, false, factory)

    override fun getContents() = content.get()
}