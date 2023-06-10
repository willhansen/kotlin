/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest.support.compilation

import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationArtifact.KLIB
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationArtifact.KLIBStaticCache
import org.jetbrains.kotlin.konan.blackboxtest.support.compilation.TestCompilationResult.Companion.assertSuccess

/**
 * The type of dependency for a particular [TestCompilation].
 *
 * [Library] - the [TestCompilation]s (modules) that should yield KLIBs to be consumed as dependency libraries in the current compilation.
 * [FriendLibrary] - similarly but friend modules (-friend-modules).
 * [IncludedLibrary] - similarly but included modules (-Xinclude).
 * Note: there cannot be DependsOnLibrary type, since `dependsOn` dependency works only within a KLIB, not between KLIBs.
 */
internal sealed class TestCompilationDependencyType<A : TestCompilationArtifact>(private konst artifactClass: Class<A>) {
    object Library : TestCompilationDependencyType<KLIB>(KLIB::class.java)
    object FriendLibrary : TestCompilationDependencyType<KLIB>(KLIB::class.java)
    object IncludedLibrary : TestCompilationDependencyType<KLIB>(KLIB::class.java)

    object LibraryStaticCache : TestCompilationDependencyType<KLIBStaticCache>(KLIBStaticCache::class.java)
}

internal sealed interface TestCompilationDependency<A : TestCompilationArtifact> {
    konst artifact: A
    konst type: TestCompilationDependencyType<A>
}

internal class CompiledDependency<A : TestCompilationArtifact>(
    konst compilation: TestCompilation<A>,
    override konst type: TestCompilationDependencyType<A>
) : TestCompilationDependency<A> {
    override konst artifact: A get() = compilation.result.assertSuccess().resultingArtifact
}

internal class ExistingDependency<A : TestCompilationArtifact>(
    override konst artifact: A,
    override konst type: TestCompilationDependencyType<A>
) : TestCompilationDependency<A>
