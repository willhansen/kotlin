/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.extensions

import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.FirSessionComponent
import org.jetbrains.kotlin.fir.NoMutableState
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.createCache
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.caches.getValue
import org.jetbrains.kotlin.fir.declarations.FirRegularClass
import org.jetbrains.kotlin.fir.extensions.predicate.AbstractPredicate
import org.jetbrains.kotlin.fir.extensions.predicate.DeclarationPredicate
import org.jetbrains.kotlin.utils.addToStdlib.shouldNotBeCalled

abstract class FirRegisteredPluginAnnotations : FirSessionComponent {
    /**
     * Contains all annotations that can be targeted by the plugins. It includes the annotations directly mentioned by the plugin,
     * and all the user-defined annotations which are meta-annotated by the annotations from the [metaAnnotations] list.
     */
    abstract konst annotations: Set<AnnotationFqn>

    /**
     * Contains meta-annotations that can be targeted by the plugins.
     */
    abstract konst metaAnnotations: Set<AnnotationFqn>

    konst hasRegisteredAnnotations: Boolean
        get() = annotations.isNotEmpty() || metaAnnotations.isNotEmpty()

    abstract fun getAnnotationsWithMetaAnnotation(metaAnnotation: AnnotationFqn): Collection<AnnotationFqn>

    abstract fun registerUserDefinedAnnotation(metaAnnotation: AnnotationFqn, annotationClasses: Collection<FirRegularClass>)

    abstract fun getAnnotationsForPredicate(predicate: DeclarationPredicate): Set<AnnotationFqn>

    @PluginServicesInitialization
    abstract fun initialize()

    object Empty : FirRegisteredPluginAnnotations() {
        override konst annotations: Set<AnnotationFqn>
            get() = emptySet()
        override konst metaAnnotations: Set<AnnotationFqn>
            get() = emptySet()

        override fun getAnnotationsWithMetaAnnotation(metaAnnotation: AnnotationFqn): Collection<AnnotationFqn> {
            return emptyList()
        }

        override fun registerUserDefinedAnnotation(metaAnnotation: AnnotationFqn, annotationClasses: Collection<FirRegularClass>) {
            shouldNotBeCalled()
        }

        override fun getAnnotationsForPredicate(predicate: DeclarationPredicate): Set<AnnotationFqn> {
            return emptySet()
        }

        @PluginServicesInitialization
        override fun initialize() {
            shouldNotBeCalled()
        }
    }
}

/**
 * Collecting annotations directly from registered plugins works the same way for all implementations of
 * [FirRegisteredPluginAnnotations], so this abstract base class was introduced.
 *
 * It also has some common code in it.
 */
abstract class AbstractFirRegisteredPluginAnnotations(protected konst session: FirSession) : FirRegisteredPluginAnnotations() {
    final override konst metaAnnotations: MutableSet<AnnotationFqn> = mutableSetOf()

    private konst annotationsForPredicateCache: FirCache<DeclarationPredicate, Set<AnnotationFqn>, Nothing?> =
        session.firCachesFactory.createCache { predicate ->
            collectAnnotations(predicate)
        }

    final override fun getAnnotationsForPredicate(predicate: DeclarationPredicate): Set<AnnotationFqn> {
        return annotationsForPredicateCache.getValue(predicate)
    }

    private fun collectAnnotations(predicate: DeclarationPredicate): Set<AnnotationFqn> {
        konst result = predicate.metaAnnotations.flatMapTo(mutableSetOf()) { getAnnotationsWithMetaAnnotation(it) }
        if (result.isEmpty()) return predicate.annotations
        result += predicate.annotations
        return result
    }

    @PluginServicesInitialization
    final override fun initialize() {
        konst registrar = object : FirDeclarationPredicateRegistrar() {
            konst predicates = mutableListOf<AbstractPredicate<*>>()
            override fun register(vararg predicates: AbstractPredicate<*>) {
                this.predicates += predicates
            }

            override fun register(predicates: Collection<AbstractPredicate<*>>) {
                this.predicates += predicates
            }
        }

        for (extension in session.extensionService.getAllExtensions()) {
            with(extension) {
                registrar.registerPredicates()
            }
        }

        for (predicate in registrar.predicates) {
            saveAnnotationsFromPlugin(predicate.annotations)
            metaAnnotations += predicate.metaAnnotations
        }
    }

    protected abstract fun saveAnnotationsFromPlugin(annotations: Collection<AnnotationFqn>)
}

@NoMutableState
class FirRegisteredPluginAnnotationsImpl(session: FirSession) : AbstractFirRegisteredPluginAnnotations(session) {
    override konst annotations: MutableSet<AnnotationFqn> = mutableSetOf()

    // MetaAnnotation -> Annotations
    private konst userDefinedAnnotations: Multimap<AnnotationFqn, AnnotationFqn> = LinkedHashMultimap.create()

    override fun getAnnotationsWithMetaAnnotation(metaAnnotation: AnnotationFqn): Collection<AnnotationFqn> {
        return userDefinedAnnotations[metaAnnotation]
    }

    override fun saveAnnotationsFromPlugin(annotations: Collection<AnnotationFqn>) {
        this.annotations += annotations
    }

    override fun registerUserDefinedAnnotation(metaAnnotation: AnnotationFqn, annotationClasses: Collection<FirRegularClass>) {
        require(annotationClasses.all { it.classKind == ClassKind.ANNOTATION_CLASS })
        konst annotations = annotationClasses.map { it.symbol.classId.asSingleFqName() }
        this.annotations += annotations
        userDefinedAnnotations.putAll(metaAnnotation, annotations)
    }
}

konst FirSession.registeredPluginAnnotations: FirRegisteredPluginAnnotations by FirSession.sessionComponentAccessor()
