/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.descriptors

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.scopes.MemberScope

class FirPackageViewDescriptor(override konst fqName: FqName, konst moduleDescriptor: ModuleDescriptor) : PackageViewDescriptor {
    override fun getContainingDeclaration(): PackageViewDescriptor? {
        TODO("not implemented")
    }

    override konst memberScope: MemberScope
        get() = MemberScope.Empty

    override konst module: ModuleDescriptor
        get() = moduleDescriptor

    override konst fragments: List<PackageFragmentDescriptor>
        get() = listOf(FirPackageFragmentDescriptor(fqName, moduleDescriptor))

    override fun getOriginal(): DeclarationDescriptor {
        TODO("not implemented")
    }

    override fun getName(): Name {
        TODO("not implemented")
    }

    override fun <R : Any?, D : Any?> accept(visitor: DeclarationDescriptorVisitor<R, D>?, data: D): R {
        TODO("not implemented")
    }

    override fun acceptVoid(visitor: DeclarationDescriptorVisitor<Void, Void>?) {
        TODO("not implemented")
    }

    override konst annotations: Annotations
        get() = Annotations.EMPTY

}