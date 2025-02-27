/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.model

abstract class ResultingArtifact<A : ResultingArtifact<A>> {
    abstract konst kind: TestArtifactKind<A>

    class Source : ResultingArtifact<Source>() {
        override konst kind: TestArtifactKind<Source>
            get() = SourcesKind
    }

    abstract class FrontendOutput<R : FrontendOutput<R>> : ResultingArtifact<R>() {
        abstract override konst kind: FrontendKind<R>

        object Empty : FrontendOutput<Empty>() {
            override konst kind: FrontendKind<Empty>
                get() = FrontendKind.NoFrontend
        }
    }

    abstract class BackendInput<I : BackendInput<I>> : ResultingArtifact<I>() {
        abstract override konst kind: BackendKind<I>

        object Empty : BackendInput<Empty>() {
            override konst kind: BackendKind<Empty>
                get() = BackendKind.NoBackend
        }
    }

    abstract class Binary<A : Binary<A>> : ResultingArtifact<A>() {
        abstract override konst kind: BinaryKind<A>

        object Empty : Binary<Empty>() {
            override konst kind: BinaryKind<Empty>
                get() = BinaryKind.NoArtifact
        }
    }
}
