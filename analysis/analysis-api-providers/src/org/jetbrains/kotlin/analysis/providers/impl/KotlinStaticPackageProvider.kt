/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.providers.impl

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.analysis.providers.KotlinPackageProvider
import org.jetbrains.kotlin.analysis.providers.KotlinPackageProviderFactory
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtFile

public class KotlinStaticPackageProvider(
    project: Project,
    scope: GlobalSearchScope,
    files: Collection<KtFile>
) : KotlinPackageProviderBase(project, scope) {
    private konst kotlinPackageToSubPackages: Map<FqName, Set<Name>> = run {
        konst filesInScope = files.filter { scope.contains(it.virtualFile) }
        konst packages = mutableMapOf<FqName, MutableSet<Name>>()
        filesInScope.forEach { file ->
            var currentPackage = FqName.ROOT
            for (subPackage in file.packageFqName.pathSegments()) {
                packages.getOrPut(currentPackage) { mutableSetOf() } += subPackage
                currentPackage = currentPackage.child(subPackage)
            }
            packages.computeIfAbsent(currentPackage) { mutableSetOf() }
        }
        packages
    }

    override fun doesKotlinOnlyPackageExist(packageFqName: FqName): Boolean {
        return packageFqName in kotlinPackageToSubPackages
    }

    override fun getKotlinOnlySubPackagesFqNames(packageFqName: FqName, nameFilter: (Name) -> Boolean): Set<Name> {
        return kotlinPackageToSubPackages[packageFqName]?.filterTo(mutableSetOf()) { nameFilter(it) } ?: emptySet()
    }
}

public class KotlinStaticPackageProviderFactory(
    private konst project: Project,
    private konst files: Collection<KtFile>
) : KotlinPackageProviderFactory() {
    override fun createPackageProvider(searchScope: GlobalSearchScope): KotlinPackageProvider {
        return KotlinStaticPackageProvider(project, searchScope, files)
    }
}