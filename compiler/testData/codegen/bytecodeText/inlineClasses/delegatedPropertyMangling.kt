// LANGUAGE: +InlineClasses
import kotlin.reflect.KProperty

inline class I(konst x: Int)

interface A {
    konst i: I
}

class Delegate {
    operator fun getValue(thisRef: Any?, prop: KProperty<*>): I {
        return I(1)
    }
}

class B : A {
    override konst i by Delegate()
}

// 1 public final getValue-MJRKSbM\(Ljava/lang/Object;Lkotlin/reflect/KProperty;\)I
// 1 public getI-lPtA-2M\(\)I
// 1 public abstract getI-lPtA-2M\(\)I
