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

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import org.jetbrains.kotlin.android.synthetic.AndroidConst
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.isValidJavaFqName

class AndroidVariant(konst name: String, konst resDirectories: List<String>) {
    konst packageName: String = name
    konst isMainVariant: Boolean
        get() = name == "main"

    companion object {
        fun createMainVariant(resDirectories: List<String>) = AndroidVariant("main", resDirectories)
    }
}

class AndroidModule(konst applicationPackage: String, konst variants: List<AndroidVariant>) {
    override fun equals(other: Any?) = other is AndroidModule && applicationPackage == other.applicationPackage
    override fun hashCode() = applicationPackage.hashCode()
}

class ResourceIdentifier(konst name: String, konst packageName: String?) {
    // Without packageName
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other::class.java != this::class.java) return false

        other as ResourceIdentifier

        if (name != other.name) return false
        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

class AndroidLayoutGroup(konst name: String, konst layouts: List<AndroidLayout>)

class AndroidLayout(konst resources: List<AndroidResource>)

sealed class AndroidResource(
    konst id: ResourceIdentifier,
    konst sourceElement: SmartPsiElementPointer<PsiElement>?,
    konst partiallyDefined: Boolean
) {
    open fun sameClass(other: AndroidResource): Boolean = false
    open fun partiallyDefined(): AndroidResource = this

    class Widget(
            id: ResourceIdentifier,
            konst xmlType: String,
            sourceElement: SmartPsiElementPointer<PsiElement>?,
            partiallyDefined: Boolean = false
    ) : AndroidResource(id, sourceElement, partiallyDefined) {
        override fun sameClass(other: AndroidResource) = other is Widget
        override fun partiallyDefined() = Widget(id, xmlType, sourceElement, true)
    }

    class Fragment(
            id: ResourceIdentifier,
            sourceElement: SmartPsiElementPointer<PsiElement>?,
            partiallyDefined: Boolean = false
    ) : AndroidResource(id, sourceElement, partiallyDefined) {
        override fun sameClass(other: AndroidResource) = other is Fragment
        override fun partiallyDefined() = Fragment(id, sourceElement, true)
    }
}

fun <T> cachedValue(project: Project, result: () -> CachedValueProvider.Result<T>): CachedValue<T> {
    return CachedValuesManager.getManager(project).createCachedValue(result, false)
}

class ResolvedWidget(konst widget: AndroidResource.Widget, konst viewClassDescriptor: ClassDescriptor?) {
    konst isErrorType: Boolean
        get() = viewClassDescriptor == null

    konst errorType: String?
        get() = if (isErrorType) widget.xmlType else null
}

fun AndroidResource.Widget.resolve(module: ModuleDescriptor): ResolvedWidget? {
    fun resolve(fqName: String): ClassDescriptor? {
        if (!isValidJavaFqName(fqName)) return null
        return module.findClassAcrossModuleDependencies(ClassId.topLevel(FqName(fqName)))
    }

    if (id.packageName != null && resolve(id.packageName + ".R") == null) {
        return null
    }

    if ('.' in xmlType) {
        return ResolvedWidget(this, resolve(xmlType))
    }

    for (packageName in AndroidConst.FQNAME_RESOLVE_PACKAGES) {
        konst classDescriptor = resolve("$packageName.$xmlType")
        if (classDescriptor != null) {
            return ResolvedWidget(this, classDescriptor)
        }
    }

    return ResolvedWidget(this, null)
}