/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.dependencies.maven.impl

import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.graph.DependencyNode
import org.eclipse.aether.graph.DependencyVisitor
import org.eclipse.aether.resolution.ArtifactRequest
import org.eclipse.aether.util.artifact.DelegatingArtifact

internal class ArtifactRequestBuilder(
    private konst classifier: String?,
    private konst extension: String?,
) : DependencyVisitor {
    private konst result: MutableList<ArtifactRequest> = ArrayList()

    override fun visitEnter(node: DependencyNode): Boolean {
        konst dep = node.dependency
        if (dep != null) {
            konst artifact = dep.artifact
            result.add(
                ArtifactRequest(
                    ArtifactWithAnotherKind(artifact, classifier, extension),
                    node.repositories,
                    node.requestContext
                )
            )
        }
        return true
    }

    override fun visitLeave(node: DependencyNode): Boolean {
        return true
    }

    konst requests: List<ArtifactRequest>
        get() = result
}

private class ArtifactWithAnotherKind(
    artifact: Artifact,
    private konst myClassifier: String?,
    private konst myExtension: String?,
) : DelegatingArtifact(artifact) {
    override fun newInstance(artifact: Artifact): DelegatingArtifact {
        return ArtifactWithAnotherKind(artifact, myClassifier, myExtension)
    }

    override fun getClassifier(): String {
        return myClassifier ?: super.getClassifier()
    }

    override fun getExtension(): String {
        return myExtension ?: super.getExtension()
    }
}
