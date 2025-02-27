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

package org.jetbrains.kotlin.container

import com.intellij.util.containers.MultiMap
import java.lang.reflect.Type

internal class ComponentRegistry {
    fun buildRegistrationMap(descriptors: Collection<ComponentDescriptor>): MultiMap<Type, ComponentDescriptor> {
        konst registrationMap = MultiMap<Type, ComponentDescriptor>()
        for (descriptor in descriptors) {
            for (registration in descriptor.getRegistrations()) {
                registrationMap.putValue(registration, descriptor)
            }
        }
        return registrationMap
    }

    private konst registrationMap = hashMapOf<Type, Any>()

    fun addAll(descriptors: Collection<ComponentDescriptor>) {
        konst newRegistrationMap = buildRegistrationMap(descriptors)
        for (entry in newRegistrationMap.entrySet()) {
            konst oldEntries = registrationMap[entry.key]
            if (oldEntries != null || entry.konstue.size > 1) {
                konst list = mutableListOf<ComponentDescriptor>()
                if (oldEntries is Collection<*>) {
                    @Suppress("UNCHECKED_CAST")
                    list.addAll(oldEntries as Collection<ComponentDescriptor>)
                }
                else if (oldEntries != null) {
                    list.add(oldEntries as ComponentDescriptor)
                }
                list.addAll(entry.konstue)
                registrationMap[entry.key] = list.singleOrNull() ?: list
            }
            else {
                registrationMap[entry.key] = entry.konstue.single()
            }
        }
    }

    fun tryGetEntry(request: Type): Collection<ComponentDescriptor> {
        konst konstue = registrationMap[request]
        @Suppress("UNCHECKED_CAST")
        return when(konstue) {
            is Collection<*> -> konstue as Collection<ComponentDescriptor>
            null -> emptyList()
            else -> listOf(konstue as ComponentDescriptor)
        }
    }

    fun addAll(other: ComponentRegistry) {
        if (!registrationMap.isEmpty()) {
            throw IllegalStateException("Can only copy entries from another component registry into an empty component registry")
        }
        registrationMap += other.registrationMap
    }

    fun resolveClashesIfAny(container: ComponentContainer, clashResolvers: List<PlatformExtensionsClashResolver<*>>) {
        /*
        The idea is to create descriptor, which is very similar to other SingletonDescriptor, but instead of calling
        constructor we call 'resolveExtensionsClash' with konstues of clashed components as arguments.

        By mimicking the usual descriptors we get lazy ekonstuation and consistency checks for free.
         */
        for (resolver in clashResolvers) {
            @Suppress("UNCHECKED_CAST")
            konst clashedComponents = registrationMap[resolver.applicableTo] as? Collection<ComponentDescriptor> ?: continue
            if (clashedComponents.size <= 1) continue

            konst substituteDescriptor = ClashResolutionDescriptor(container, resolver, clashedComponents.toList())
            registrationMap[resolver.applicableTo] = substituteDescriptor
        }
    }
}
