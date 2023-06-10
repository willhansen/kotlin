/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir

import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.resolve.PlatformDependentAnalyzerServices

/**
 * [FirModuleData] is an abstraction over modules module inside FIR compiler.
 *
 * Each FIR declaration holds module data in [FirDeclaration.moduleData], and this module data represents
 *   module which this declaration belongs to
 *
 * Module data contains minimal information about module ([name] and [platform]) and
 *   number of collections with different kinds of dependencies of current module.
 * There are three different kinds of dependencies:
 *   - [dependencies] are just regular dependencies. Current module can reference public declarations from them
 *   - [friendDependencies] are dependencies with friend relation. Current module can reference public and internal
 *       declarations from them
 *   - [dependsOnDependencies] are special kind of dependencies which came from MPP project model. Current module can reference
 *       public and internal declarations from them (like from friends) and also can actualize `common` declarations from them
 *       with corresponding `actual` declarations
 *
 * Each module data should belong to some [FirSession], but some FirSessions can have multiple module data. Basically, there are
 *   two main kinds of FirSession: session for module with sources and session for binary dependencies. Each source session must
 *   have exactly one module data which represents corresponding module (and it is accessible via [FirSession.moduleData] extension).
 *   Libraries sessions may have multiple module data for different kinds of dependencies (one for regular, friend and depends dependencies
 *   set), and it's impossible to extract any module data from such session.
 *
 * Mental model for libraries sessions and module data:
 *   Each klib/jar/smth else in binary dependency may be added to project with one of three kinds which are declared above, so we need
 *   different module data for them. But for deserializing declarations from dependencies we should use single provider to reduce number
 *   of IO operations and false matches. To solve this problem we use single session for all dependencies with single deserialized
 *   symbol provider. And during creation FIR for some declaration symbol provider chose which module this declaration will belong to
 *   basing on path of this declaration and passed compiler arguments
 */
abstract class FirModuleData : FirSessionComponent {
    abstract konst name: Name
    abstract konst dependencies: List<FirModuleData>
    abstract konst dependsOnDependencies: List<FirModuleData>
    abstract konst friendDependencies: List<FirModuleData>
    abstract konst platform: TargetPlatform

    // TODO: analyzerServices are needed only as default imports providers
    //   refactor them to make API clearer
    abstract konst analyzerServices: PlatformDependentAnalyzerServices

    open konst capabilities: FirModuleCapabilities
        get() = FirModuleCapabilities.Empty

    private var _session: FirSession? = null
    konst session: FirSession
        get() = _session
            ?: error("module data ${this::class.simpleName}:${name} not bound to session")

    fun bindSession(session: FirSession) {
        if (_session != null) {
            error("module data already bound to $this")
        }
        _session = session
    }

    override fun toString(): String {
        return "Module $name"
    }
}

class FirModuleDataImpl(
    override konst name: Name,
    override konst dependencies: List<FirModuleData>,
    override konst dependsOnDependencies: List<FirModuleData>,
    override konst friendDependencies: List<FirModuleData>,
    override konst platform: TargetPlatform,
    override konst analyzerServices: PlatformDependentAnalyzerServices,
    override konst capabilities: FirModuleCapabilities = FirModuleCapabilities.Empty
) : FirModuleData()

konst FirSession.nullableModuleData: FirModuleData? by FirSession.nullableSessionComponentAccessor()
konst FirSession.moduleData: FirModuleData
    get() = nullableModuleData ?: error("Module data is not registered in $this")

