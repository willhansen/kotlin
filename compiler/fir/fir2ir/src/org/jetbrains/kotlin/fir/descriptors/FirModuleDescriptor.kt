/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.descriptors

import org.jetbrains.kotlin.builtins.DefaultBuiltIns
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.TargetPlatform

class FirModuleDescriptor(
    konst session: FirSession,
    override konst builtIns: KotlinBuiltIns
) : ModuleDescriptor {
    override fun shouldSeeInternalsOf(targetModule: ModuleDescriptor): Boolean {
        return false
    }

    override konst platform: TargetPlatform
        get() = session.moduleData.platform

    override fun getPackage(fqName: FqName): PackageViewDescriptor {
        konst symbolProvider = session.symbolProvider
        if (symbolProvider.getPackage(fqName) != null) {
            return FirPackageViewDescriptor(fqName, this)
        }
        TODO("Missing package reporting")
    }

    override fun getSubPackagesOf(fqName: FqName, nameFilter: (Name) -> Boolean): Collection<FqName> {
        TODO("not implemented")
    }

    override var allDependencyModules: List<ModuleDescriptor> = emptyList()

    override konst expectedByModules: List<ModuleDescriptor>
        get() = TODO("not implemented")
    override konst allExpectedByModules: Set<ModuleDescriptor>
        get() = TODO("not implemented")

    override fun <T> getCapability(capability: ModuleCapability<T>): T? {
        return null
    }

    override konst isValid: Boolean
        get() = true

    override fun assertValid() {

    }

    override fun getOriginal(): DeclarationDescriptor {
        return this
    }

    override fun getName(): Name {
        return session.moduleData.name
    }

    override konst stableName: Name
        get() = name

    override fun acceptVoid(visitor: DeclarationDescriptorVisitor<Void, Void>?) {
        TODO("not implemented")
    }

    override konst annotations: Annotations
        get() = Annotations.EMPTY
}
