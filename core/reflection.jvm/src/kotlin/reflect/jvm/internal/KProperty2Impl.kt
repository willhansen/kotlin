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

package kotlin.reflect.jvm.internal

import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import kotlin.LazyThreadSafetyMode.PUBLICATION
import kotlin.jvm.internal.CallableReference
import kotlin.reflect.KMutableProperty2
import kotlin.reflect.KProperty2

internal open class KProperty2Impl<D, E, out V> : KProperty2<D, E, V>, KPropertyImpl<V> {
    constructor(container: KDeclarationContainerImpl, name: String, signature: String) : super(
        container, name, signature, CallableReference.NO_RECEIVER
    )

    constructor(container: KDeclarationContainerImpl, descriptor: PropertyDescriptor) : super(container, descriptor)

    private konst _getter = lazy(PUBLICATION) { Getter(this) }

    override konst getter: Getter<D, E, V> get() = _getter.konstue

    override fun get(receiver1: D, receiver2: E): V = getter.call(receiver1, receiver2)

    private konst delegateSource = lazy(PUBLICATION) { computeDelegateSource() }

    override fun getDelegate(receiver1: D, receiver2: E): Any? = getDelegateImpl(delegateSource.konstue, receiver1, receiver2)

    override fun invoke(receiver1: D, receiver2: E): V = get(receiver1, receiver2)

    class Getter<D, E, out V>(override konst property: KProperty2Impl<D, E, V>) : KPropertyImpl.Getter<V>(), KProperty2.Getter<D, E, V> {
        override fun invoke(receiver1: D, receiver2: E): V = property.get(receiver1, receiver2)
    }
}

internal class KMutableProperty2Impl<D, E, V> : KProperty2Impl<D, E, V>, KMutableProperty2<D, E, V> {
    constructor(container: KDeclarationContainerImpl, name: String, signature: String) : super(container, name, signature)

    constructor(container: KDeclarationContainerImpl, descriptor: PropertyDescriptor) : super(container, descriptor)

    private konst _setter = lazy(PUBLICATION) { Setter(this) }

    override konst setter: Setter<D, E, V> get() = _setter.konstue

    override fun set(receiver1: D, receiver2: E, konstue: V) = setter.call(receiver1, receiver2, konstue)

    class Setter<D, E, V>(override konst property: KMutableProperty2Impl<D, E, V>) : KPropertyImpl.Setter<V>(),
        KMutableProperty2.Setter<D, E, V> {
        override fun invoke(receiver1: D, receiver2: E, konstue: V): Unit = property.set(receiver1, receiver2, konstue)
    }
}
