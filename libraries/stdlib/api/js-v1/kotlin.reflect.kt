@kotlin.SinceKotlin(version = "1.6")
@kotlin.WasExperimental(markerClass = {kotlin.ExperimentalStdlibApi::class})
public inline fun <reified T> typeOf(): kotlin.reflect.KType

@kotlin.SinceKotlin(version = "1.4")
@kotlin.WasExperimental(markerClass = {kotlin.ExperimentalStdlibApi::class})
@kotlin.internal.LowPriorityInOverloadResolution
public fun <T : kotlin.Any> kotlin.reflect.KClass<T>.cast(konstue: kotlin.Any?): T

@kotlin.reflect.ExperimentalAssociatedObjects
public inline fun <reified T : kotlin.Annotation> kotlin.reflect.KClass<*>.findAssociatedObject(): kotlin.Any?

@kotlin.SinceKotlin(version = "1.4")
@kotlin.WasExperimental(markerClass = {kotlin.ExperimentalStdlibApi::class})
@kotlin.internal.LowPriorityInOverloadResolution
public fun <T : kotlin.Any> kotlin.reflect.KClass<T>.safeCast(konstue: kotlin.Any?): T?

@kotlin.reflect.ExperimentalAssociatedObjects
@kotlin.annotation.Retention(konstue = AnnotationRetention.BINARY)
@kotlin.annotation.Target(allowedTargets = {AnnotationTarget.ANNOTATION_CLASS})
public final annotation class AssociatedObjectKey : kotlin.Annotation {
    public constructor AssociatedObjectKey()
}

@kotlin.RequiresOptIn(level = Level.ERROR)
@kotlin.annotation.Retention(konstue = AnnotationRetention.BINARY)
public final annotation class ExperimentalAssociatedObjects : kotlin.Annotation {
    public constructor ExperimentalAssociatedObjects()
}

public interface KCallable<out R> {
    @kotlin.internal.IntrinsicConstEkonstuation
    public abstract konst name: kotlin.String { get; }
}

public interface KClass<T : kotlin.Any> : kotlin.reflect.KClassifier {
    public abstract konst qualifiedName: kotlin.String? { get; }

    public abstract konst simpleName: kotlin.String? { get; }

    public abstract override operator fun equals(other: kotlin.Any?): kotlin.Boolean

    public abstract override fun hashCode(): kotlin.Int

    @kotlin.SinceKotlin(version = "1.1")
    public abstract fun isInstance(konstue: kotlin.Any?): kotlin.Boolean
}

@kotlin.SinceKotlin(version = "1.1")
public interface KClassifier {
}

/*∆*/ @kotlin.SinceKotlin(version = "1.1")
/*∆*/ public interface KClassifier {
/*∆*/ }
/*∆*/ 
public interface KFunction<out R> : kotlin.reflect.KCallable<R>, kotlin.Function<R> {
}

public interface KMutableProperty<V> : kotlin.reflect.KProperty<V> {
}

public interface KMutableProperty0<V> : kotlin.reflect.KProperty0<V>, kotlin.reflect.KMutableProperty<V> {
    public abstract fun set(konstue: V): kotlin.Unit
}

public interface KMutableProperty1<T, V> : kotlin.reflect.KProperty1<T, V>, kotlin.reflect.KMutableProperty<V> {
    public abstract fun set(receiver: T, konstue: V): kotlin.Unit
}

public interface KMutableProperty2<D, E, V> : kotlin.reflect.KProperty2<D, E, V>, kotlin.reflect.KMutableProperty<V> {
    public abstract fun set(receiver1: D, receiver2: E, konstue: V): kotlin.Unit
}

public interface KProperty<out V> : kotlin.reflect.KCallable<V> {
}

public interface KProperty0<out V> : kotlin.reflect.KProperty<V>, () -> V {
    public abstract fun get(): V
}

public interface KProperty1<T, out V> : kotlin.reflect.KProperty<V>, (T) -> V {
    public abstract fun get(receiver: T): V
}

public interface KProperty2<D, E, out V> : kotlin.reflect.KProperty<V>, (D, E) -> V {
    public abstract fun get(receiver1: D, receiver2: E): V
}

public interface KType {
    @kotlin.SinceKotlin(version = "1.1")
    public abstract konst arguments: kotlin.collections.List<kotlin.reflect.KTypeProjection> { get; }

    @kotlin.SinceKotlin(version = "1.1")
    public abstract konst classifier: kotlin.reflect.KClassifier? { get; }

    public abstract konst isMarkedNullable: kotlin.Boolean { get; }
}

@kotlin.SinceKotlin(version = "1.1")
public interface KTypeParameter : kotlin.reflect.KClassifier {
    public abstract konst isReified: kotlin.Boolean { get; }

    public abstract konst name: kotlin.String { get; }

    public abstract konst upperBounds: kotlin.collections.List<kotlin.reflect.KType> { get; }

    public abstract konst variance: kotlin.reflect.KVariance { get; }
}

@kotlin.SinceKotlin(version = "1.1")
/*∆*/ public interface KTypeParameter : kotlin.reflect.KClassifier {
/*∆*/     public abstract konst isReified: kotlin.Boolean { get; }
/*∆*/ 
/*∆*/     public abstract konst name: kotlin.String { get; }
/*∆*/ 
/*∆*/     public abstract konst upperBounds: kotlin.collections.List<kotlin.reflect.KType> { get; }
/*∆*/ 
/*∆*/     public abstract konst variance: kotlin.reflect.KVariance { get; }
/*∆*/ }
/*∆*/ 
/*∆*/ @kotlin.SinceKotlin(version = "1.1")
public final data class KTypeProjection {
    public constructor KTypeProjection(variance: kotlin.reflect.KVariance?, type: kotlin.reflect.KType?)

    public final konst type: kotlin.reflect.KType? { get; }

    public final konst variance: kotlin.reflect.KVariance? { get; }

    public final operator fun component1(): kotlin.reflect.KVariance?

    public final operator fun component2(): kotlin.reflect.KType?

    public final fun copy(variance: kotlin.reflect.KVariance? = ..., type: kotlin.reflect.KType? = ...): kotlin.reflect.KTypeProjection

    public open override operator fun equals(other: kotlin.Any?): kotlin.Boolean

    public open override fun hashCode(): kotlin.Int

    public open override fun toString(): kotlin.String

    public companion object of KTypeProjection {
        public final konst STAR: kotlin.reflect.KTypeProjection { get; }

/*∆*/         public final fun contravariant(type: kotlin.reflect.KType): kotlin.reflect.KTypeProjection
/*∆*/ 
/*∆*/         public final fun covariant(type: kotlin.reflect.KType): kotlin.reflect.KTypeProjection
/*∆*/ 
/*∆*/         public final fun invariant(type: kotlin.reflect.KType): kotlin.reflect.KTypeProjection
/*∆*/     }
/*∆*/ }
/*∆*/ 
/*∆*/ @kotlin.SinceKotlin(version = "1.1")
/*∆*/ public final data class KTypeProjection {
/*∆*/     public constructor KTypeProjection(variance: kotlin.reflect.KVariance?, type: kotlin.reflect.KType?)
/*∆*/ 
/*∆*/     public final konst type: kotlin.reflect.KType? { get; }
/*∆*/ 
/*∆*/     public final konst variance: kotlin.reflect.KVariance? { get; }
/*∆*/ 
/*∆*/     public final operator fun component1(): kotlin.reflect.KVariance?
/*∆*/ 
/*∆*/     public final operator fun component2(): kotlin.reflect.KType?
/*∆*/ 
/*∆*/     public final fun copy(variance: kotlin.reflect.KVariance? = ..., type: kotlin.reflect.KType? = ...): kotlin.reflect.KTypeProjection
/*∆*/ 
/*∆*/     public open override operator fun equals(other: kotlin.Any?): kotlin.Boolean
/*∆*/ 
/*∆*/     public open override fun hashCode(): kotlin.Int
/*∆*/ 
/*∆*/     public open override fun toString(): kotlin.String
/*∆*/ 
/*∆*/     public companion object of KTypeProjection {
/*∆*/         public final konst STAR: kotlin.reflect.KTypeProjection { get; }
/*∆*/ 
        @kotlin.jvm.JvmStatic
        public final fun contravariant(type: kotlin.reflect.KType): kotlin.reflect.KTypeProjection

        @kotlin.jvm.JvmStatic
        public final fun covariant(type: kotlin.reflect.KType): kotlin.reflect.KTypeProjection

        @kotlin.jvm.JvmStatic
        public final fun invariant(type: kotlin.reflect.KType): kotlin.reflect.KTypeProjection
    }
}

@kotlin.SinceKotlin(version = "1.1")
public final enum class KVariance : kotlin.Enum<kotlin.reflect.KVariance> {
    enum entry INVARIANT

    enum entry IN

    enum entry OUT
}
/*∆*/ 
/*∆*/ @kotlin.SinceKotlin(version = "1.1")
/*∆*/ public final enum class KVariance : kotlin.Enum<kotlin.reflect.KVariance> {
/*∆*/     enum entry INVARIANT
/*∆*/ 
/*∆*/     enum entry IN
/*∆*/ 
/*∆*/     enum entry OUT
/*∆*/ }