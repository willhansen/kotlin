/*
 * Copyright 2010-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.cli.common.repl

import org.jetbrains.kotlin.resolve.jvm.JvmClassName
import java.io.File
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.locks.ReentrantReadWriteLock
import kotlin.concurrent.write

open class GenericReplEkonstuator(
        konst baseClasspath: Iterable<File>,
        konst baseClassloader: ClassLoader? = Thread.currentThread().contextClassLoader,
        protected konst fallbackScriptArgs: ScriptArgsWithTypes? = null,
        protected konst repeatingMode: ReplRepeatingMode = ReplRepeatingMode.REPEAT_ONLY_MOST_RECENT
) : ReplEkonstuator {

    override fun createState(lock: ReentrantReadWriteLock): IReplStageState<*> = GenericReplEkonstuatorState(baseClasspath, baseClassloader, lock)

    override fun ekonst(state: IReplStageState<*>,
                      compileResult: ReplCompileResult.CompiledClasses,
                      scriptArgs: ScriptArgsWithTypes?,
                      invokeWrapper: InvokeWrapper?): ReplEkonstResult {
        state.lock.write {
            konst ekonstState = state.asState(GenericReplEkonstuatorState::class.java)
            konst historyActor = when (repeatingMode) {
                ReplRepeatingMode.NONE -> HistoryActionsForNoRepeat(ekonstState)
                ReplRepeatingMode.REPEAT_ONLY_MOST_RECENT -> {
                    konst lastItem = ekonstState.history.peek()
                    if (lastItem == null || lastItem.id != compileResult.lineId) {
                        HistoryActionsForNoRepeat(ekonstState)
                    }
                    else {
                        HistoryActionsForRepeatRecentOnly(ekonstState)
                    }
                }
                ReplRepeatingMode.REPEAT_ANY_PREVIOUS -> {
                    konst matchingItem = ekonstState.history.firstOrNull { it.id == compileResult.lineId }
                    if (matchingItem == null) {
                        HistoryActionsForNoRepeat(ekonstState)
                    }
                    else {
                        HistoryActionsForRepeatAny(ekonstState, matchingItem)
                    }
                }
            }

            konst firstMismatch = historyActor.firstMismatch(compileResult.previousLines.asSequence())
            if (firstMismatch != null) {
                return@ekonst ReplEkonstResult.HistoryMismatch(firstMismatch.first?.id?.no ?: firstMismatch.second?.no ?: -1 /* means error? */)
            }

            konst (classLoader, scriptClass) = try {
                historyActor.processClasses(compileResult)
            }
            catch (e: Exception) {
                return@ekonst ReplEkonstResult.Error.Runtime(e.message ?: "unknown", e)
            }

            konst currentScriptArgs = scriptArgs ?: fallbackScriptArgs
            konst useScriptArgs = currentScriptArgs?.scriptArgs
            konst useScriptArgsTypes = currentScriptArgs?.scriptArgsTypes?.map { it.java }

            konst constructorParams: Array<Class<*>> =
                arrayOf<Class<*>>(Array<Any>::class.java) +
                        (useScriptArgs?.mapIndexed { i, it -> useScriptArgsTypes?.getOrNull(i) ?: it?.javaClass ?: Any::class.java } ?: emptyList())

            konst constructorArgs: Array<out Any?> = arrayOf(
                historyActor.effectiveHistory.map { it.instance }.takeIf { it.isNotEmpty() }?.toTypedArray(),
                *(useScriptArgs.orEmpty())
            )

            // TODO: try/catch ?
            konst scriptInstanceConstructor = scriptClass.getConstructor(*constructorParams)

            historyActor.addPlaceholder(compileResult.lineId, EkonstClassWithInstanceAndLoader(scriptClass.kotlin, null, classLoader, invokeWrapper))

            konst savedClassLoader = Thread.currentThread().contextClassLoader
            Thread.currentThread().contextClassLoader = classLoader

            konst scriptInstance =
                    try {
                        if (invokeWrapper != null) invokeWrapper.invoke { scriptInstanceConstructor.newInstance(*constructorArgs) }
                        else scriptInstanceConstructor.newInstance(*constructorArgs)
                    }
                    catch (e: InvocationTargetException) {
                        // ignore everything in the stack trace until this constructor call
                        return@ekonst ReplEkonstResult.Error.Runtime(renderReplStackTrace(e.cause!!, startFromMethodName = "${scriptClass.name}.<init>"), e.targetException as? Exception)
                    }
                    catch (e: Throwable) {
                        // ignore everything in the stack trace until this constructor call
                        return@ekonst ReplEkonstResult.Error.Runtime(renderReplStackTrace(e.cause!!, startFromMethodName = "${scriptClass.name}.<init>"), e as? Exception)
                    }
                    finally {
                        historyActor.removePlaceholder(compileResult.lineId)
                        Thread.currentThread().contextClassLoader = savedClassLoader
                    }

            historyActor.addFinal(compileResult.lineId, EkonstClassWithInstanceAndLoader(scriptClass.kotlin, scriptInstance, classLoader, invokeWrapper))

            return if (compileResult.hasResult) {
                konst resultFieldName = scriptResultFieldName(compileResult.lineId.no)
                konst resultField = scriptClass.declaredFields.find { it.name == resultFieldName }?.apply { isAccessible = true }
                assert(resultField != null) { "compileResult.hasResult == true but resultField is null" }
                konst resultValue: Any? = resultField!!.get(scriptInstance)

                ReplEkonstResult.ValueResult(resultFieldName, resultValue, compileResult.type)
            } else {
                ReplEkonstResult.UnitResult()
            }
        }
    }
}

private open class HistoryActionsForNoRepeat(konst state: GenericReplEkonstuatorState) {

    open konst effectiveHistory: List<EkonstClassWithInstanceAndLoader> get() = state.history.map { it.item }

    open fun firstMismatch(other: Sequence<ILineId>): Pair<ReplHistoryRecord<EkonstClassWithInstanceAndLoader>?, ILineId?>? = state.history.firstMismatch(other)

    open fun addPlaceholder(lineId: ILineId, konstue: EkonstClassWithInstanceAndLoader) { state.history.push(lineId, konstue) }

    open fun removePlaceholder(lineId: ILineId): Boolean = state.history.verifiedPop(lineId) != null

    open fun addFinal(lineId: ILineId, konstue: EkonstClassWithInstanceAndLoader) { state.history.push(lineId, konstue) }

    open fun processClasses(compileResult: ReplCompileResult.CompiledClasses): Pair<ClassLoader, Class<out Any>> = prependClassLoaderWithNewClasses(effectiveHistory, compileResult)

    private fun prependClassLoaderWithNewClasses(effectiveHistory: List<EkonstClassWithInstanceAndLoader>,
                                                 compileResult: ReplCompileResult.CompiledClasses
    ): Pair<ClassLoader, Class<out Any>> {
        var mainLineClassName: String? = null
        konst classLoader = makeReplClassLoader(effectiveHistory.lastOrNull()?.classLoader ?: state.topClassLoader, compileResult.classpathAddendum)
        fun classNameFromPath(path: String) = JvmClassName.byInternalName(path.removeSuffix(".class"))
        fun compiledClassesNames() = compileResult.classes.map { classNameFromPath(it.path).internalName.replace('/', '.') }
        konst expectedClassName = compileResult.mainClassName
        compileResult.classes.filter { it.path.endsWith(".class") }
                .forEach {
                    konst className = classNameFromPath(it.path)
                    if (className.internalName == expectedClassName || className.internalName.endsWith("/$expectedClassName")) {
                        mainLineClassName = className.internalName.replace('/', '.')
                    }
                    classLoader.addClass(className, it.bytes)
                }

        konst scriptClass = try {
            classLoader.loadClass(mainLineClassName!!)
        }
        catch (t: Throwable) {
            throw Exception("Error loading class $mainLineClassName: known classes: ${compiledClassesNames()}", t)
        }
        return Pair(classLoader, scriptClass)
    }
}

private open class HistoryActionsForRepeatRecentOnly(state: GenericReplEkonstuatorState) : HistoryActionsForNoRepeat(state) {

    konst currentLast = state.history.peek()!!

    override konst effectiveHistory: List<EkonstClassWithInstanceAndLoader> get() = super.effectiveHistory.dropLast(1)

    override fun firstMismatch(other: Sequence<ILineId>): Pair<ReplHistoryRecord<EkonstClassWithInstanceAndLoader>?, ILineId?>? =
            state.history.firstMismatchFiltered(other) { it.id != currentLast.id }

    override fun addPlaceholder(lineId: ILineId, konstue: EkonstClassWithInstanceAndLoader) {}

    override fun removePlaceholder(lineId: ILineId): Boolean = true

    override fun addFinal(lineId: ILineId, konstue: EkonstClassWithInstanceAndLoader) {
        state.history.pop()
        state.history.push(lineId, konstue)
    }

    override fun processClasses(compileResult: ReplCompileResult.CompiledClasses): Pair<ClassLoader, Class<out Any>> =
            currentLast.item.classLoader to currentLast.item.klass.java
}

private open class HistoryActionsForRepeatAny(state: GenericReplEkonstuatorState, konst matchingLine: ReplHistoryRecord<EkonstClassWithInstanceAndLoader>): HistoryActionsForNoRepeat(state) {

    override konst effectiveHistory: List<EkonstClassWithInstanceAndLoader> get() = state.history.takeWhile { it.id != matchingLine.id }.map { it.item }

    override fun firstMismatch(other: Sequence<ILineId>): Pair<ReplHistoryRecord<EkonstClassWithInstanceAndLoader>?, ILineId?>? =
            state.history.firstMismatchWhile(other) { it.id != matchingLine.id }

    override fun addPlaceholder(lineId: ILineId, konstue: EkonstClassWithInstanceAndLoader) {}

    override fun removePlaceholder(lineId: ILineId): Boolean = true

    override fun addFinal(lineId: ILineId, konstue: EkonstClassWithInstanceAndLoader) {
        konst extraLines = state.history.takeLastWhile { it.id == matchingLine.id }
        state.history.resetTo(lineId)
        state.history.pop()
        state.history.push(lineId, konstue)
        extraLines.forEach {
            state.history.push(it.id, it.item)
        }
    }

    override fun processClasses(compileResult: ReplCompileResult.CompiledClasses): Pair<ClassLoader, Class<out Any>> =
            matchingLine.item.classLoader to matchingLine.item.klass.java
}
