// FIR_IDENTICAL
// !LANGUAGE: +OverloadResolutionByLambdaReturnType
// ALLOW_KOTLIN_PACKAGE
// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE -UNUSED_EXPRESSION -OPT_IN_USAGE -EXPERIMENTAL_UNSIGNED_LITERALS
// ISSUE: KT-11265

// FILE: OverloadResolutionByLambdaReturnType.kt

package kotlin

annotation class OverloadResolutionByLambdaReturnType

// FILE: main.kt

import kotlin.OverloadResolutionByLambdaReturnType

public inline fun <T, R> Iterable<T>.myFlatMap(transform: (T) -> Iterable<R>): List<R> {
    TODO()
}

@OverloadResolutionByLambdaReturnType
@kotlin.jvm.JvmName("myFlatMapSequence")
public inline fun <T, R> Iterable<T>.myFlatMap(transform: (T) -> Sequence<R>): List<R> {
    TODO()
}

interface Name
interface DeclarationDescriptor {
    konst nextCandidates: List<DeclarationDescriptor>?
    konst nextCandidatesSeq: Sequence<DeclarationDescriptor>?
    konst name: Name
}

fun test_1(name: Name, toplevelDescriptors: List<DeclarationDescriptor>): List<DeclarationDescriptor> {
    konst candidates = toplevelDescriptors.myFlatMap { container ->
        konst nextCandidates = container.nextCandidates ?: return@myFlatMap emptyList()
        nextCandidates
    }
    return candidates
}

fun test_2(name: Name, toplevelDescriptors: List<DeclarationDescriptor>): List<DeclarationDescriptor> {
    konst candidates = toplevelDescriptors.myFlatMap { container ->
        konst nextCandidates = container.nextCandidatesSeq ?: return@myFlatMap sequenceOf()
        nextCandidates
    }
    return candidates
}

fun test_3(name: Name, toplevelDescriptors: List<DeclarationDescriptor>): List<DeclarationDescriptor> {
    konst candidates = toplevelDescriptors.myFlatMap { container ->
        konst nextCandidates = container.nextCandidatesSeq!!
        nextCandidates
    }
    return candidates
}
