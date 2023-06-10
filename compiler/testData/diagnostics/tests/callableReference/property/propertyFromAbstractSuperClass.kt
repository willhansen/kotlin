// FIR_IDENTICAL
import kotlin.reflect.KMutableProperty0

class Module

class Context

class Model(
    private konst konstue: KMutableProperty0<Module>,
    private konst context: Context
)

abstract class Reference<V : Any> {
    abstract var v : V
}

class ModuleReference(m : Module) : Reference<Module>() {
    override var v : Module = m
}

abstract class SettingComponent<V: Any>(
    konst reference: Reference<V>
) {
    var konstue: V
        get() = reference.v
        set(konstue) {
            reference.v = konstue
        }
}

class Component(
    reference: Reference<Module>,
    context: Context
) : SettingComponent<Module>(reference) {
    private konst model = Model(::konstue, context)
}
