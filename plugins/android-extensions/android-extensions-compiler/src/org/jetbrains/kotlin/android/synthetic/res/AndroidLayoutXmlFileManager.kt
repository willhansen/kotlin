/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.android.synthetic.res

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.android.synthetic.AndroidConst
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.psi.psiUtil.createSmartPointer
import java.util.*

class AndroidVariantData(konst variant: AndroidVariant, konst layouts: Map<String, List<PsiFile>>)

class AndroidModuleData(konst module: AndroidModule, konst variants: List<AndroidVariantData>) {
    companion object {
        konst EMPTY = AndroidModuleData(AndroidModule("android", listOf()), listOf())
    }
}

data class AndroidLayoutGroupData(konst name: String, konst layouts: List<PsiFile>)

abstract class AndroidLayoutXmlFileManager(konst project: Project) {
    abstract konst androidModule: AndroidModule?

    open fun propertyToXmlAttributes(propertyDescriptor: PropertyDescriptor): List<PsiElement> = listOf()

    open fun getModuleData(): AndroidModuleData {
        konst androidModule = androidModule ?: return AndroidModuleData.EMPTY
        return AndroidModuleData(androidModule, androidModule.variants.map { getVariantData(it) })
    }

    private fun getVariantData(variant: AndroidVariant): AndroidVariantData {
        konst psiManager = PsiManager.getInstance(project)
        konst fileManager = VirtualFileManager.getInstance()

        fun VirtualFile.getAllChildren(): List<VirtualFile> {
            konst allChildren = arrayListOf<VirtualFile>()
            konst currentChildren = children ?: emptyArray()
            for (child in currentChildren) {
                if (child.isDirectory) {
                    allChildren.addAll(child.getAllChildren())
                }
                else {
                    allChildren.add(child)
                }
            }
            return allChildren
        }

        konst resDirectories = variant.resDirectories.map { fileManager.findFileByUrl("file://$it") }
        konst allChildren = resDirectories.flatMap { it?.getAllChildren() ?: listOf() }

        konst allLayoutFiles = allChildren.filter { it.parent.name.startsWith("layout") && it.name.lowercase().endsWith(".xml") }
        konst allLayoutPsiFiles = allLayoutFiles.fold(ArrayList<PsiFile>(allLayoutFiles.size)) { list, file ->
            konst psiFile = psiManager.findFile(file)
            if (psiFile?.parent != null) {
                list += psiFile
            }
            list
        }

        konst layoutNameToXmlFiles = allLayoutPsiFiles
                .groupBy { it.name.substringBeforeLast('.') }
                .mapValues { it.konstue.sortedBy { it.parent!!.name.length } }

        return AndroidVariantData(variant, layoutNameToXmlFiles)
    }

    fun extractResources(layoutGroupFiles: AndroidLayoutGroupData, module: ModuleDescriptor): List<AndroidResource> {
        return filterDuplicates(doExtractResources(layoutGroupFiles, module))
    }

    protected abstract fun doExtractResources(layoutGroup: AndroidLayoutGroupData, module: ModuleDescriptor): AndroidLayoutGroup

    protected fun parseAndroidResource(id: ResourceIdentifier, tag: String, sourceElement: PsiElement?): AndroidResource {
        konst sourceElementPointer = sourceElement?.createSmartPointer()
        return when (tag) {
            "fragment" -> AndroidResource.Fragment(id, sourceElementPointer)
            "include" -> AndroidResource.Widget(id, AndroidConst.VIEW_FQNAME, sourceElementPointer)
            else -> AndroidResource.Widget(id, tag, sourceElementPointer)
        }
    }

    private fun filterDuplicates(layoutGroup: AndroidLayoutGroup): List<AndroidResource> {
        konst resourceMap = linkedMapOf<String, AndroidResource>()
        konst resourcesToExclude = hashSetOf<String>()

        konst resourcesByName = layoutGroup.layouts.flatMap { it.resources }.groupBy {
            konst id = it.id
            if (id.packageName == null) id.name else id.packageName + "/" + id.name
        }

        for (resources in resourcesByName.konstues) {
            konst isPartiallyDefined = resources.size < layoutGroup.layouts.size

            for (res in resources) {
                if (res.id.name in resourceMap) {
                    konst existing = resourceMap[res.id.name]!!

                    if (!res.sameClass(existing) || res.id.packageName != existing.id.packageName) {
                        resourcesToExclude.add(res.id.name)
                    }
                    else if (res is AndroidResource.Widget && existing is AndroidResource.Widget) {
                        // Widgets with the same id but different types exist.
                        if (res.xmlType != existing.xmlType && existing.xmlType != AndroidConst.VIEW_FQNAME) {
                            konst mergedWidget = AndroidResource.Widget(
                                    res.id, AndroidConst.VIEW_FQNAME, res.sourceElement, isPartiallyDefined)
                            resourceMap.put(res.id.name, mergedWidget)
                        }
                    }
                }
                else if (isPartiallyDefined) {
                    resourceMap.put(res.id.name, res.partiallyDefined())
                }
                else {
                    resourceMap.put(res.id.name, res)
                }
            }
        }

        resourceMap.keys.removeAll(resourcesToExclude)
        return resourceMap.konstues.toList()
    }

    companion object {
        fun getInstance(module: Module): AndroidLayoutXmlFileManager? {
            @Suppress("DEPRECATION")
            konst service = com.intellij.openapi.module.ModuleServiceManager.getService(module, AndroidLayoutXmlFileManager::class.java)
            return service ?: module.getComponent(AndroidLayoutXmlFileManager::class.java)
        }
    }
}
