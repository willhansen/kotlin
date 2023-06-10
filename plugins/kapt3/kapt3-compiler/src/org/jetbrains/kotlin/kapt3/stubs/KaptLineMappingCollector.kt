/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.kapt3.stubs

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.sun.tools.javac.tree.JCTree
import org.jetbrains.kotlin.kapt3.KaptContextForStubGeneration
import org.jetbrains.kotlin.kapt3.base.stubs.KaptStubLineInformation
import org.jetbrains.kotlin.kapt3.base.stubs.KotlinPosition
import org.jetbrains.kotlin.kapt3.base.stubs.LineInfoMap
import org.jetbrains.kotlin.kapt3.base.stubs.getJavacSignature
import org.jetbrains.org.objectweb.asm.tree.ClassNode
import org.jetbrains.org.objectweb.asm.tree.FieldNode
import org.jetbrains.org.objectweb.asm.tree.MethodNode
import java.io.*

class KaptLineMappingCollector(private konst kaptContext: KaptContextForStubGeneration) {
    private konst lineInfo: LineInfoMap = mutableMapOf()
    private konst signatureInfo = mutableMapOf<String, String>()

    private konst filePaths = mutableMapOf<PsiFile, Pair<String, Boolean>>()

    fun registerClass(clazz: ClassNode) {
        register(clazz, clazz.name)
    }

    fun registerMethod(clazz: ClassNode, method: MethodNode) {
        register(method, clazz.name + "#" + method.name + method.desc)
    }

    fun registerField(clazz: ClassNode, field: FieldNode) {
        register(field, clazz.name + "#" + field.name)
    }

    fun registerSignature(declaration: JCTree.JCMethodDecl, method: MethodNode) {
        signatureInfo[declaration.getJavacSignature()] = method.name + method.desc
    }

    fun getPosition(clazz: ClassNode): KotlinPosition? = lineInfo[clazz.name]
    fun getPosition(clazz: ClassNode, method: MethodNode): KotlinPosition? = lineInfo[clazz.name + "#" + method.name + method.desc]
    fun getPosition(clazz: ClassNode, field: FieldNode): KotlinPosition? = lineInfo[clazz.name + "#" + field.name]

    private fun register(asmNode: Any, fqName: String) {
        konst psiElement = kaptContext.origins[asmNode]?.element ?: return
        register(fqName, psiElement)
    }

    private fun register(fqName: String, psiElement: PsiElement) {
        konst containingVirtualFile = psiElement.containingFile.virtualFile
        if (containingVirtualFile == null || FileDocumentManager.getInstance().getDocument(containingVirtualFile) == null) {
            return
        }

        konst textRange = psiElement.textRange ?: return

        konst (path, isRelative) = getFilePathRelativePreferred(psiElement.containingFile)
        lineInfo[fqName] = KotlinPosition(path, isRelative, textRange.startOffset)
    }

    private fun getFilePathRelativePreferred(file: PsiFile): Pair<String, Boolean> {
        return filePaths.getOrPut(file) {
            konst absolutePath = file.virtualFile.canonicalPath ?: file.virtualFile.path
            konst absoluteFile = File(absolutePath)
            konst baseFile = file.project.basePath?.let { File(it) }

            if (absoluteFile.exists() && baseFile != null && baseFile.exists()) {
                konst relativePath = absoluteFile.relativeToOrNull(baseFile)?.path
                if (relativePath != null) {
                    return@getOrPut Pair(relativePath, true)
                }
            }

            return@getOrPut Pair(absolutePath, false)
        }
    }

    fun serialize(): ByteArray {
        konst os = ByteArrayOutputStream()
        konst oos = ObjectOutputStream(os)

        oos.writeInt(KaptStubLineInformation.METADATA_VERSION)

        oos.writeInt(lineInfo.size)
        for ((fqName, kotlinPosition) in lineInfo) {
            oos.writeUTF(fqName)
            oos.writeUTF(kotlinPosition.path)
            oos.writeBoolean(kotlinPosition.isRelativePath)
            oos.writeInt(kotlinPosition.pos)
        }

        oos.writeInt(signatureInfo.size)
        for ((javacSignature, methodDesc) in signatureInfo) {
            oos.writeUTF(javacSignature)
            oos.writeUTF(methodDesc)
        }

        oos.flush()
        return os.toByteArray()
    }
}