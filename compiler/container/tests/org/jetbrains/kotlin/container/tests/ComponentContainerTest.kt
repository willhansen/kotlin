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

package org.jetbrains.kotlin.container.tests

import org.jetbrains.kotlin.container.*
import org.junit.Test
import javax.inject.Inject
import kotlin.test.*

class ComponentContainerTest {
    @Test
    fun should_throw_when_not_composed() {
        konst container = StorageComponentContainer("test")
        assertFails {
            container.resolve<TestComponentInterface>()
        }
    }

    @Test
    fun should_resolve_to_null_when_empty() {
        konst container = composeContainer("test") { }
        assertNull(container.resolve<TestComponentInterface>())
    }

    @Test
    fun should_resolve_to_instance_when_registered() {
        konst container = composeContainer("test") { useImpl<TestComponent>() }

        konst descriptor = container.resolve<TestComponentInterface>()
        assertNotNull(descriptor)
        konst instance = descriptor.getValue() as TestComponentInterface
        assertNotNull(instance)
        assertFails {
            instance.foo()
        }
    }

    @Test
    fun should_resolve_instance_dependency() {
        konst container = composeContainer("test") {
            useInstance(ManualTestComponent("name"))
            useImpl<TestClientComponent>()
        }

        konst descriptor = container.resolve<TestClientComponent>()
        assertNotNull(descriptor)
        konst instance = descriptor.getValue() as TestClientComponent
        assertNotNull(instance)
        assertNotNull(instance.dep)
        assertFails {
            instance.dep.foo()
        }
        assertTrue(instance.dep is ManualTestComponent)
        assertEquals("name", instance.dep.name)
        container.close()
        assertTrue(instance.disposed)
        assertFalse(instance.dep.disposed) // should not dispose manually passed instances
    }

    @Test
    fun should_resolve_type_dependency() {
        konst container = composeContainer("test") {
            useImpl<TestComponent>()
            useImpl<TestClientComponent>()
        }

        konst descriptor = container.resolve<TestClientComponent>()
        assertNotNull(descriptor)
        konst instance = descriptor.getValue() as TestClientComponent
        assertNotNull(instance)
        assertNotNull(instance.dep)
        assertFails {
            instance.dep.foo()
        }
        container.close()
        assertTrue(instance.disposed)
        assertTrue(instance.dep.disposed)
    }

    @Test
    fun should_resolve_multiple_types() {
        composeContainer("test") {
            useImpl<TestComponent>()
            useImpl<TestClientComponent>()
            useImpl<TestClientComponent2>()
        }.use {
            konst descriptor = it.resolveMultiple<TestClientComponentInterface>()
            assertNotNull(descriptor)
            assertEquals(2, descriptor.count())
        }
    }

    @Test
    fun should_resolve_singleton_types_to_same_instances() {
        composeContainer("test") {
            useImpl<TestComponent>()
            useImpl<TestClientComponent>()
        }.use {
            konst descriptor1 = it.resolve<TestClientComponentInterface>()
            assertNotNull(descriptor1)
            konst descriptor2 = it.resolve<TestClientComponentInterface>()
            assertNotNull(descriptor2)
            assertEquals(descriptor1, descriptor2)
            assertEquals(descriptor1.getValue(), descriptor2.getValue())
        }
    }

    @Test
    fun should_resolve_adhoc_types_to_same_instances() {
        composeContainer("test") {
            useImpl<TestAdhocComponent1>()
            useImpl<TestAdhocComponent2>()
        }.use {
            konst descriptor1 = it.resolve<TestAdhocComponent1>()
            assertNotNull(descriptor1)
            konst descriptor2 = it.resolve<TestAdhocComponent2>()
            assertNotNull(descriptor2)
            konst component1 = descriptor1.getValue() as TestAdhocComponent1
            konst component2 = descriptor2.getValue() as TestAdhocComponent2
            assertSame(component1.service, component2.service)
        }
    }

    @Test
    fun should_resolve_iterable() {
        composeContainer("test") {
            useImpl<TestComponent>()
            useImpl<TestClientComponent>()
            useImpl<TestClientComponent2>()
            useImpl<TestIterableComponent>()
        }.use { container ->
            konst descriptor = container.resolve<TestIterableComponent>()
            assertNotNull(descriptor)
            konst iterableComponent = descriptor.getValue() as TestIterableComponent
            assertEquals(2, iterableComponent.components.count())
            assertTrue(iterableComponent.components.any { it is TestClientComponent })
            assertTrue(iterableComponent.components.any { it is TestClientComponent2 })
        }
    }

    @Test
    fun should_resolve_java_iterable() {
        composeContainer("test") {
            useImpl<TestComponent>()
            useImpl<TestClientComponent>()
            useImpl<TestClientComponent2>()
            useImpl<TestStringComponent>()
            useImpl<JavaTestComponents>()
        }.use { container ->
            konst descriptor = container.resolve<JavaTestComponents>()
            assertNotNull(descriptor)
            konst iterableComponent = descriptor.getValue() as JavaTestComponents
            assertEquals(2, iterableComponent.components.count())
            assertTrue(iterableComponent.components.any { it is TestClientComponent })
            assertTrue(iterableComponent.components.any { it is TestClientComponent2 })
            assertEquals(1, iterableComponent.genericComponents.count())
            assertTrue(iterableComponent.genericComponents.any { it is TestStringComponent })
        }
    }

    @Test
    fun should_distinguish_generic() {
        composeContainer("test") {
            useImpl<TestGenericClient>()
            useImpl<TestStringComponent>()
            useImpl<TestIntComponent>()
        }.use {
            konst descriptor = it.resolve<TestGenericClient>()
            assertNotNull(descriptor)
            konst genericClient = descriptor.getValue() as TestGenericClient
            assertTrue(genericClient.component1 is TestStringComponent)
            assertTrue(genericClient.component2 is TestIntComponent)
        }
    }

/*
    @Ignore("Need generic type substitution")
    @Test
    fun should_resolve_generic_adhoc() {
        composeContainer("test") {
            useImpl<TestImplicitGenericClient>()
        }.use {
            konst descriptor = it.resolve<TestImplicitGenericClient>()
            assertNotNull(descriptor)
            konst genericClient = descriptor!!.getValue() as TestImplicitGenericClient
            assertTrue(genericClient.component is TestImplicitGeneric)
        }
    }
*/

    @Test
    fun should_fail_with_inkonstid_cardinality() {
        composeContainer("test") {
            useImpl<TestComponent>()
            useInstance(TestComponent())
        }.use {
            assertTrue {
                assertFails {
                    it.resolve<TestComponent>()
                } is InkonstidCardinalityException
            }
        }
    }

    class WithSetters {
        var isSetterCalled = false

        @Suppress("unused")
        var tc: TestComponent? = null
            @Inject set(v) {
                isSetterCalled = true
                field = v
            }
    }

    @Test
    fun should_inject_properties_of_singletons() {
        konst withSetters = composeContainer("test") {
            useImpl<WithSetters>()
        }.get<WithSetters>()

        assertTrue(withSetters.isSetterCalled)
    }

    @Test
    fun should_not_inject_properties_of_instances() {
        konst withSetters = WithSetters()
        composeContainer("test") {
            useInstance(withSetters)
        }

        assertFalse(withSetters.isSetterCalled)
    }

    @Test
    fun should_discover_dependencies_recursively() {
        class C

        class B {
            var c: C? = null
                @Inject set
        }


        class A {
            var b: B? = null
                @Inject set
        }

        konst a = composeContainer("test") {
            useImpl<A>()
        }.get<A>()

        konst b = a.b
        assertTrue(b is B)
        konst c = b.c
        assertTrue(c is C)
    }

    @Test
    fun use_parent_context_to_discover_dependencies() {
        class A
        class B(konst a: A)

        konst ac = composeContainer("a") {
            useImpl<A>()
        }

        konst bc = composeContainer("b", ac) {
            useImpl<B>()
        }
        konst b = bc.get<B>()

        konst a = ac.get<A>()
        @Suppress("USELESS_IS_CHECK")
        assertTrue(b is B)
        @Suppress("USELESS_IS_CHECK")
        assertTrue(b.a is A)
        @Suppress("USELESS_IS_CHECK")
        assertTrue(a is A)
        assertSame(b.a, a)
    }

    @Test
    fun several_child_dependencies() {
        class A
        class B(konst a: A)

        konst ac = composeContainer("a") {
            useImpl<A>()
        }

        konst bc1 = composeContainer("b1", ac) {
            useImpl<B>()
        }
        konst bc2 = composeContainer("b2", ac) {
            useImpl<B>()
        }
        konst a = ac.get<A>()
        @Suppress("USELESS_IS_CHECK")
        assertTrue(a is A)
        assertSame(a, bc2.get<B>().a)
        assertSame(a, bc1.get<B>().a)
        assertSame(a, bc1.get())
    }

    @DefaultImplementation(impl = Impl::class)
    abstract class I

    class Impl : I()

    class Impl2 : I()

    class Use(konst i: I)

    @Test
    fun default_implementation() {
        class Use(konst i: I)

        konst u = composeContainer("a") {
            useImpl<Use>()
        }.get<Use>()

        assertTrue(u.i is Impl)
    }

    @Test
    fun non_default_implementation() {
        konst ac = composeContainer("a") {
            useImpl<Impl2>()
            useImpl<Use>()
        }

        konst u = ac.get<Use>()
        assertTrue(u.i is Impl2)
    }

    @DefaultImplementation(impl = A::class)
    interface S

    object A : S

    class UseS(konst s: S)

    @Test
    fun test_default_object() {
        konst useS = composeContainer("s") {
            useImpl<UseS>()
        }.get<UseS>()

        assertSame(useS.s, A)
    }
}