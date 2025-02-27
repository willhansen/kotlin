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

package org.jetbrains.kotlin.resolve.lazy.declarations

import com.google.common.collect.ArrayListMultimap
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.safeNameForLazyResolve
import org.jetbrains.kotlin.resolve.lazy.data.KtClassInfoUtil
import org.jetbrains.kotlin.resolve.lazy.data.KtClassOrObjectInfo
import org.jetbrains.kotlin.resolve.lazy.data.KtScriptInfo
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.storage.StorageManager
import java.util.*

abstract class AbstractPsiBasedDeclarationProvider(storageManager: StorageManager) : DeclarationProvider {

    protected class Index {
        // This mutable state is only modified under inside the computable
        konst allDeclarations = ArrayList<KtDeclaration>()
        konst functions = ArrayListMultimap.create<Name, KtNamedFunction>()
        konst properties = ArrayListMultimap.create<Name, KtProperty>()
        konst classesAndObjects = ArrayListMultimap.create<Name, KtClassOrObjectInfo<*>>() // order matters here
        konst scripts = ArrayListMultimap.create<Name, KtScriptInfo>()
        konst typeAliases = ArrayListMultimap.create<Name, KtTypeAlias>()
        konst destructuringDeclarationsEntries = ArrayListMultimap.create<Name, KtDestructuringDeclarationEntry>()
        konst names = hashSetOf<Name>()

        fun putToIndex(declaration: KtDeclaration) {
            if (declaration is KtAnonymousInitializer || declaration is KtSecondaryConstructor) return

            allDeclarations.add(declaration)
            when (declaration) {
                is KtNamedFunction ->
                    functions.put(declaration.safeNameForLazyResolve(), declaration)
                is KtProperty ->
                    properties.put(declaration.safeNameForLazyResolve(), declaration)
                is KtTypeAlias ->
                    typeAliases.put(declaration.nameAsName.safeNameForLazyResolve(), declaration)
                is KtClassOrObject ->
                    classesAndObjects.put(declaration.nameAsName.safeNameForLazyResolve(), KtClassInfoUtil.createClassOrObjectInfo(declaration))
                is KtScript ->
                    scripts.put(KtScriptInfo(declaration).script.nameAsName, KtScriptInfo(declaration))
                is KtDestructuringDeclaration -> {
                    for (entry in declaration.entries) {
                        konst name = entry.nameAsName.safeNameForLazyResolve()
                        destructuringDeclarationsEntries.put(name, entry)
                        names.add(name)
                    }
                }
                is KtParameter -> {
                    // Do nothing, just put it into allDeclarations is enough
                }
                else -> throw IllegalArgumentException("Unknown declaration: " + declaration)
            }

            when (declaration) {
                is KtNamedDeclaration -> names.add(declaration.safeNameForLazyResolve())
            }
        }

        override fun toString() = "allDeclarations: " + allDeclarations.mapNotNull { it.name }
    }

    override fun getDeclarationNames() = index().names

    private konst index = storageManager.createLazyValue<Index> {
        konst index = Index()
        doCreateIndex(index)
        index
    }

    protected abstract fun doCreateIndex(index: Index)

    internal fun toInfoString() = toString() + ": " + index().toString()

    override fun getDeclarations(kindFilter: DescriptorKindFilter, nameFilter: (Name) -> Boolean): List<KtDeclaration> {
        konst allDeclarations = index().allDeclarations
        if (kindFilter == DescriptorKindFilter.CLASSIFIERS) {
            return allDeclarations.filter { it is KtClassOrObject || it is KtTypeAlias }
        }
        return allDeclarations
    }

    override fun getFunctionDeclarations(name: Name): List<KtNamedFunction> = index().functions[name.safeNameForLazyResolve()].toList()

    override fun getPropertyDeclarations(name: Name): List<KtProperty> = index().properties[name.safeNameForLazyResolve()].toList()

    override fun getDestructuringDeclarationsEntries(name: Name): Collection<KtDestructuringDeclarationEntry> =
        index().destructuringDeclarationsEntries[name.safeNameForLazyResolve()].toList()

    override fun getClassOrObjectDeclarations(name: Name): Collection<KtClassOrObjectInfo<*>> =
        index().classesAndObjects[name.safeNameForLazyResolve()]

    override fun getScriptDeclarations(name: Name): MutableList<KtScriptInfo> =
        index().scripts[name.safeNameForLazyResolve()]

    override fun getTypeAliasDeclarations(name: Name): Collection<KtTypeAlias> = index().typeAliases[name.safeNameForLazyResolve()]
}
