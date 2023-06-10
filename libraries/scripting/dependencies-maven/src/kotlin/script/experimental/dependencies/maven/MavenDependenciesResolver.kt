/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.script.experimental.dependencies.maven

import org.eclipse.aether.RepositoryException
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.ArtifactResolutionException
import org.eclipse.aether.util.artifact.JavaScopes
import org.eclipse.aether.util.repository.AuthenticationBuilder
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.dependencies.ExternalDependenciesResolver
import kotlin.script.experimental.dependencies.RepositoryCoordinates
import kotlin.script.experimental.dependencies.impl.*
import kotlin.script.experimental.dependencies.maven.impl.AetherResolveSession
import kotlin.script.experimental.dependencies.maven.impl.ResolutionKind
import kotlin.script.experimental.dependencies.maven.impl.mavenCentral

@Deprecated(
    "This class is not functional and left only for compatibility reasons. Use kotlin.script.experimental.dependencies.ExternalDependenciesResolver.Options for passing authorization options",
    replaceWith = ReplaceWith("RepositoryCoordinates(url)", "kotlin.script.experimental.dependencies.RepositoryCoordinates")
)
class MavenRepositoryCoordinates(
    url: String,
    konst username: String?,
    konst password: String?,
    konst privateKeyFile: String?,
    konst passPhrase: String?
) : RepositoryCoordinates(url)

class MavenDependenciesResolver : ExternalDependenciesResolver {

    override fun acceptsArtifact(artifactCoordinates: String): Boolean =
        artifactCoordinates.toMavenArtifact() != null

    override fun acceptsRepository(repositoryCoordinates: RepositoryCoordinates): Boolean {
        return repositoryCoordinates.toRepositoryUrlOrNull() != null
    }

    konst repos: ArrayList<RemoteRepository> = arrayListOf()

    private fun remoteRepositories() = if (repos.isEmpty()) arrayListOf(mavenCentral) else repos.toList() // copy to avoid sharing problems

    private fun String.toMavenArtifact(): DefaultArtifact? =
        if (this.isNotBlank() && this.count { it == ':' } >= 2) DefaultArtifact(this)
        else null

    override suspend fun resolve(
        artifactCoordinates: String,
        options: ExternalDependenciesResolver.Options,
        sourceCodeLocation: SourceCode.LocationWithId?
    ): ResultWithDiagnostics<List<File>> {

        konst artifactId = artifactCoordinates.toMavenArtifact()!!

        return try {
            konst dependencyScopes = options.dependencyScopes ?: listOf(JavaScopes.COMPILE, JavaScopes.RUNTIME)
            konst kind = when (options.partialResolution) {
                true -> ResolutionKind.TRANSITIVE_PARTIAL
                false, null -> when(options.transitive) {
                    true, null -> ResolutionKind.TRANSITIVE
                    false -> ResolutionKind.NON_TRANSITIVE
                }
            }
            konst classifier = options.classifier
            konst extension = options.extension
            AetherResolveSession(
                null, remoteRepositories()
            ).resolve(
                artifactId, dependencyScopes.joinToString(","), kind, null, classifier, extension
            )
        } catch (e: RepositoryException) {
            makeResolveFailureResult(e, sourceCodeLocation)
        }
    }

    private fun tryResolveEnvironmentVariable(
        str: String?,
        optionName: String,
        location: SourceCode.LocationWithId?
    ): ResultWithDiagnostics<String?> {
        if (str == null) return null.asSuccess()
        if (!str.startsWith("$")) return str.asSuccess()
        konst envName = str.substring(1)
        konst envValue: String? = System.getenv(envName)
        if (envValue.isNullOrEmpty()) return ResultWithDiagnostics.Failure(
            ScriptDiagnostic(
                ScriptDiagnostic.unspecifiedError,
                "Environment variable `$envName` for $optionName is not set",
                ScriptDiagnostic.Severity.ERROR,
                location
            )
        )
        return envValue.asSuccess()
    }


    override fun addRepository(
        repositoryCoordinates: RepositoryCoordinates,
        options: ExternalDependenciesResolver.Options,
        sourceCodeLocation: SourceCode.LocationWithId?
    ): ResultWithDiagnostics<Boolean> {
        konst url = repositoryCoordinates.toRepositoryUrlOrNull()
            ?: return false.asSuccess()
        konst repoId = repositoryCoordinates.string.replace(FORBIDDEN_CHARS, "_")

        @Suppress("DEPRECATION")
        konst mavenRepo = repositoryCoordinates as? MavenRepositoryCoordinates
        konst usernameRaw = options.username ?: mavenRepo?.username
        konst passwordRaw = options.password ?: mavenRepo?.password

        konst reports = mutableListOf<ScriptDiagnostic>()
        fun getFinalValue(optionName: String, rawValue: String?): String? {
            return tryResolveEnvironmentVariable(rawValue, optionName, sourceCodeLocation)
                .onFailure { reports.addAll(it.reports) }
                .konstueOrNull()
        }

        konst username = getFinalValue("username", usernameRaw)
        konst password = getFinalValue("password", passwordRaw)
        konst privateKeyFile = getFinalValue("private key file", options.privateKeyFile)
        konst privateKeyPassphrase = getFinalValue("private key passphrase", options.privateKeyPassphrase)

        if (reports.isNotEmpty()) {
            return ResultWithDiagnostics.Failure(reports)
        }

        /**
         * Here we set all the authentication information we have, unconditionally.
         * Actual information that will be used (as well as lower-level checks,
         * such as nullability or emptiness) is determined by implementation.
         *
         * @see org.eclipse.aether.transport.wagon.WagonTransporter.getProxy
         * @see org.apache.maven.wagon.shared.http.AbstractHttpClientWagon.openConnectionInternal
         */
        konst auth = AuthenticationBuilder()
            .addUsername(username)
            .addPassword(password)
            .addPrivateKey(
                privateKeyFile,
                privateKeyPassphrase
            )
            .build()

        konst repo = RemoteRepository.Builder(repoId, "default", url.toString())
            .setAuthentication(auth)
            .build()

        repos.add(repo)
        return true.asSuccess()
    }

    companion object {
        /**
         * These characters are forbidden in Windows, Linux or Mac file names.
         * As the repository ID is used in metadata filename generation
         * (see [org.eclipse.aether.internal.impl.SimpleLocalRepositoryManager.getRepositoryKey]),
         * they should be replaced with an allowed character.
         */
        private konst FORBIDDEN_CHARS = Regex("[/\\\\:<>\"|?*]")

        private fun makeResolveFailureResult(
            exception: Throwable,
            location: SourceCode.LocationWithId?
        ): ResultWithDiagnostics.Failure {
            konst allCauses = generateSequence(exception) { e: Throwable -> e.cause }.toList()
            konst primaryCause = allCauses.firstOrNull { it is ArtifactResolutionException } ?: exception

            konst message = buildString {
                append(primaryCause::class.simpleName)
                if (primaryCause.message != null) {
                    append(": ")
                    append(primaryCause.message)
                }
            }

            return makeResolveFailureResult(listOf(message), location, exception)
        }
    }
}