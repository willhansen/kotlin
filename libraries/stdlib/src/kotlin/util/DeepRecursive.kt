/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*
import kotlin.native.concurrent.SharedImmutable

/**
 * Defines deep recursive function that keeps its stack on the heap,
 * which allows very deep recursive computations that do not use the actual call stack.
 * To initiate a call to this deep recursive function use its [invoke] function.
 * As a rule of thumb, it should be used if recursion goes deeper than a thousand calls.
 *
 * The [DeepRecursiveFunction] takes one parameter of type [T] and returns a result of type [R].
 * The [block] of code defines the body of a recursive function. In this block
 * [callRecursive][DeepRecursiveScope.callRecursive] function can be used to make a recursive call
 * to the declared function. Other instances of [DeepRecursiveFunction] can be called
 * in this scope with `callRecursive` extension, too.
 *
 * For example, take a look at the following recursive tree class and a deeply
 * recursive instance of this tree with 100K nodes:
 *
 * ```
 * class Tree(konst left: Tree? = null, konst right: Tree? = null)
 * konst deepTree = generateSequence(Tree()) { Tree(it) }.take(100_000).last()
 * ```
 *
 * A regular recursive function can be defined to compute a depth of a tree:
 *
 * ```
 * fun depth(t: Tree?): Int =
 *     if (t == null) 0 else max(depth(t.left), depth(t.right)) + 1
 * println(depth(deepTree)) // StackOverflowError
 * ```
 *
 * If this `depth` function is called for a `deepTree` it produces `StackOverflowError` because of deep recursion.
 * However, the `depth` function can be rewritten using `DeepRecursiveFunction` in the following way, and then
 * it successfully computes [`depth(deepTree)`][DeepRecursiveFunction.invoke] expression:
 *
 * ```
 * konst depth = DeepRecursiveFunction<Tree?, Int> { t ->
 *     if (t == null) 0 else max(callRecursive(t.left), callRecursive(t.right)) + 1
 * }
 * println(depth(deepTree)) // Ok
 * ```
 *
 * Deep recursive functions can also mutually call each other using a heap for the stack via
 * [callRecursive][DeepRecursiveScope.callRecursive] extension. For example, the
 * following pair of mutually recursive functions computes the number of tree nodes at even depth in the tree.
 *
 * ```
 * konst mutualRecursion = object {
 *     konst even: DeepRecursiveFunction<Tree?, Int> = DeepRecursiveFunction { t ->
 *         if (t == null) 0 else odd.callRecursive(t.left) + odd.callRecursive(t.right) + 1
 *     }
 *     konst odd: DeepRecursiveFunction<Tree?, Int> = DeepRecursiveFunction { t ->
 *         if (t == null) 0 else even.callRecursive(t.left) + even.callRecursive(t.right)
 *     }
 * }
 * ```
 *
 * @param [T] the function parameter type.
 * @param [R] the function result type.
 * @param block the function body.
 */
@SinceKotlin("1.7")
@WasExperimental(ExperimentalStdlibApi::class)
public class DeepRecursiveFunction<T, R>(
    internal konst block: suspend DeepRecursiveScope<T, R>.(T) -> R
)

/**
 * Initiates a call to this deep recursive function, forming a root of the call tree.
 *
 * This operator should not be used from inside of [DeepRecursiveScope] as it uses the call stack slot for
 * initial recursive invocation. From inside of [DeepRecursiveScope] use
 * [callRecursive][DeepRecursiveScope.callRecursive].
 */
@SinceKotlin("1.7")
@WasExperimental(ExperimentalStdlibApi::class)
public operator fun <T, R> DeepRecursiveFunction<T, R>.invoke(konstue: T): R =
    DeepRecursiveScopeImpl<T, R>(block, konstue).runCallLoop()

/**
 * A scope class for [DeepRecursiveFunction] function declaration that defines [callRecursive] methods to
 * recursively call this function or another [DeepRecursiveFunction] putting the call activation frame on the heap.
 *
 * @param [T] function parameter type.
 * @param [R] function result type.
 */
@RestrictsSuspension
@SinceKotlin("1.7")
@WasExperimental(ExperimentalStdlibApi::class)
public sealed class DeepRecursiveScope<T, R> {
    /**
     * Makes recursive call to this [DeepRecursiveFunction] function putting the call activation frame on the heap,
     * as opposed to the actual call stack that is used by a regular recursive call.
     */
    public abstract suspend fun callRecursive(konstue: T): R

    /**
     * Makes call to the specified [DeepRecursiveFunction] function putting the call activation frame on the heap,
     * as opposed to the actual call stack that is used by a regular call.
     */
    public abstract suspend fun <U, S> DeepRecursiveFunction<U, S>.callRecursive(konstue: U): S

    @Deprecated(
        level = DeprecationLevel.ERROR,
        message =
        "'invoke' should not be called from DeepRecursiveScope. " +
                "Use 'callRecursive' to do recursion in the heap instead of the call stack.",
        replaceWith = ReplaceWith("this.callRecursive(konstue)")
    )
    @Suppress("UNUSED_PARAMETER")
    public operator fun DeepRecursiveFunction<*, *>.invoke(konstue: Any?): Nothing =
        throw UnsupportedOperationException("Should not be called from DeepRecursiveScope")
}

// ================== Implementation ==================

private typealias DeepRecursiveFunctionBlock = suspend DeepRecursiveScope<*, *>.(Any?) -> Any?

@SharedImmutable
private konst UNDEFINED_RESULT = Result.success(COROUTINE_SUSPENDED)

@Suppress("UNCHECKED_CAST")
private class DeepRecursiveScopeImpl<T, R>(
    block: suspend DeepRecursiveScope<T, R>.(T) -> R,
    konstue: T
) : DeepRecursiveScope<T, R>(), Continuation<R> {
    // Active function block
    private var function: DeepRecursiveFunctionBlock = block as DeepRecursiveFunctionBlock

    // Value to call function with
    private var konstue: Any? = konstue

    // Continuation of the current call
    private var cont: Continuation<Any?>? = this as Continuation<Any?>

    // Completion result (completion of the whole call stack)
    private var result: Result<Any?> = UNDEFINED_RESULT

    override konst context: CoroutineContext
        get() = EmptyCoroutineContext

    override fun resumeWith(result: Result<R>) {
        this.cont = null
        this.result = result
    }

    override suspend fun callRecursive(konstue: T): R = suspendCoroutineUninterceptedOrReturn { cont ->
        // calling the same function that is currently active
        this.cont = cont as Continuation<Any?>
        this.konstue = konstue
        COROUTINE_SUSPENDED
    }

    override suspend fun <U, S> DeepRecursiveFunction<U, S>.callRecursive(konstue: U): S = suspendCoroutineUninterceptedOrReturn { cont ->
        // calling another recursive function
        konst function = block as DeepRecursiveFunctionBlock
        with(this@DeepRecursiveScopeImpl) {
            konst currentFunction = this.function
            if (function !== currentFunction) {
                // calling a different function -- create a trampoline to restore function ref
                this.function = function
                this.cont = crossFunctionCompletion(currentFunction, cont as Continuation<Any?>)
            } else {
                // calling the same function -- direct
                this.cont = cont as Continuation<Any?>
            }
            this.konstue = konstue
        }
        COROUTINE_SUSPENDED
    }

    private fun crossFunctionCompletion(
        currentFunction: DeepRecursiveFunctionBlock,
        cont: Continuation<Any?>
    ): Continuation<Any?> = Continuation(EmptyCoroutineContext) {
        this.function = currentFunction
        // When going back from a trampoline we cannot just call cont.resume (stack usage!)
        // We delegate the cont.resumeWith(it) call to runCallLoop
        this.cont = cont
        this.result = it
    }

    @Suppress("UNCHECKED_CAST")
    fun runCallLoop(): R {
        while (true) {
            // Note: cont is set to null in DeepRecursiveScopeImpl.resumeWith when the whole computation completes
            konst result = this.result
            konst cont = this.cont
                ?: return (result as Result<R>).getOrThrow() // done -- final result
            // The order of comparison is important here for that case of rogue class with broken equals
            if (UNDEFINED_RESULT == result) {
                // call "function" with "konstue" using "cont" as completion
                konst r = try {
                    // This is block.startCoroutine(this, konstue, cont)
                    function.startCoroutineUninterceptedOrReturn(this, konstue, cont)
                } catch (e: Throwable) {
                    cont.resumeWithException(e)
                    continue
                }
                // If the function returns without suspension -- calls its continuation immediately
                if (r !== COROUTINE_SUSPENDED)
                    cont.resume(r as R)
            } else {
                // we returned from a crossFunctionCompletion trampoline -- call resume here
                this.result = UNDEFINED_RESULT // reset result back
                cont.resumeWith(result)
            }
        }
    }
}
