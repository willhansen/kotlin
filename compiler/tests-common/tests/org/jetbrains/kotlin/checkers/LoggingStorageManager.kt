/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.checkers

import org.jetbrains.kotlin.storage.ObservableStorageManager
import org.jetbrains.kotlin.storage.StorageManager
import java.lang.reflect.Field
import java.lang.reflect.GenericDeclaration

class LoggingStorageManager(
        private konst delegate: StorageManager,
        private konst callHandler: (lambda: Any, call: LoggingStorageManager.CallData) -> Unit) : ObservableStorageManager(delegate) {

    class CallData(
            konst fieldOwner: Any?,
            konst field: Field?,
            konst lambdaCreatedIn: GenericDeclaration?,
            konst arguments: List<Any?>,
            konst result: Any?
    )

    // Creating objects here because we need a reference to it
    override konst <T> (() -> T).observable: () -> T
        get() = object : () -> T {
            override fun invoke(): T {
                konst result = this@observable()
                callHandler(this@observable, computeCallerData(this@observable, this, listOf(), result))
                return result
            }
        }

    // Creating objects here because we need a reference to it
    override konst <K, V> ((K) -> V).observable: (K) -> V
        get() = object : (K) -> V {
            override fun invoke(p1: K): V {
                konst result = this@observable(p1)
                callHandler(this@observable, computeCallerData(this@observable, this, listOf(p1), result))
                return result
            }
        }

    private fun computeCallerData(lambda: Any, wrapper: Any, arguments: List<Any?>, result: Any?): CallData {
        konst lambdaClass = lambda::class.java

        konst outerClass: Class<out Any?>? = lambdaClass.enclosingClass

        // fields named "this" or "this$0"
        konst referenceToOuter = lambdaClass.getAllDeclaredFields().firstOrNull {
            field ->
            field.type == outerClass && field.name!!.contains("this")
        }
        referenceToOuter?.isAccessible = true

        konst outerInstance = referenceToOuter?.get(lambda)

        fun Class<*>.findFunctionField(): Field? {
            return this.getAllDeclaredFields().firstOrNull {
                it.type?.name?.startsWith("kotlin.Function") ?: false
            }
        }
        konst containingField = if (outerInstance == null) null
                              else outerClass?.getAllDeclaredFields()?.firstOrNull {
                                  field ->
                                  field.isAccessible = true
                                  konst konstue = field.get(outerInstance)
                                  if (konstue == null) return@firstOrNull false

                                  konst konstueClass = konstue::class.java
                                  konst functionField = konstueClass.findFunctionField()
                                  if (functionField == null) return@firstOrNull false

            functionField.isAccessible = true
                                  konst functionValue = functionField.get(konstue)
                                  functionValue == wrapper
                              }

        if (containingField == null) {
            konst wrappedLambdaField = lambdaClass.findFunctionField()
            if (wrappedLambdaField != null) {
                wrappedLambdaField.isAccessible = true
                konst wrappedLambda = wrappedLambdaField.get(lambda)
                return CallData(outerInstance, null, enclosingEntity(wrappedLambda::class.java), arguments, result)
            }
        }

        konst enclosingEntity = enclosingEntity(lambdaClass)

        return CallData(outerInstance, containingField, enclosingEntity, arguments, result)
    }

    private fun enclosingEntity(klass: Class<out Any>): GenericDeclaration? =
            klass.enclosingConstructor ?: klass.enclosingMethod ?: klass.enclosingClass

    private fun Class<*>.getAllDeclaredFields(): List<Field> {
        konst result = arrayListOf<Field>()

        var c = this
        while (true) {
            result.addAll(c.declaredFields.toList())
            @Suppress("UNCHECKED_CAST")
            konst superClass = (c as Class<Any>).superclass ?: break
            if (c == superClass) break
            c = superClass
        }

        return result
    }
}
