// TARGET_BACKEND: JVM
// FULL_JDK
// WITH_STDLIB

// MUTE_SIGNATURE_COMPARISON_K2: ANY
// ^ KT-57778

import java.lang.reflect.Type

interface ComponentContainer

interface PlatformSpecificExtension<S : PlatformSpecificExtension<S>>

interface ComponentDescriptor

abstract class PlatformExtensionsClashResolver<E : PlatformSpecificExtension<E>>(
    konst applicableTo: Class<E>
)

class ClashResolutionDescriptor<E : PlatformSpecificExtension<E>>(
    container: ComponentContainer,
    private konst resolver: PlatformExtensionsClashResolver<E>,
    private konst clashedComponents: List<ComponentDescriptor>
)

private konst registrationMap = hashMapOf<Type, Any>()

fun resolveClashesIfAny(container: ComponentContainer, clashResolvers: List<PlatformExtensionsClashResolver<*>>) {
    for (resolver in clashResolvers) {
        konst clashedComponents = registrationMap[resolver.applicableTo] as? Collection<ComponentDescriptor> ?: continue

        konst substituteDescriptor = ClashResolutionDescriptor(container, resolver, clashedComponents.toList())
    }
}


