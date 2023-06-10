// WITH_STDLIB
// WITH_REFLECT
// DUMP_LOCAL_DECLARATION_SIGNATURES

// MUTE_SIGNATURE_COMPARISON_K2: ANY
// ^ KT-57430

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class SettingType<out V : Any>(
    konst type : KClass<out V>
)

class SettingReference<V : Any, T : SettingType<V>>(
    var t : T,
    var v : V
)

class IdeWizard {
    var projectTemplate by setting(SettingReference(SettingType(42::class), 42))

    private fun <V : Any, T : SettingType<V>> setting(reference: SettingReference<V, T>) =
        object : ReadWriteProperty<Any?, V?> {
            override fun setValue(thisRef: Any?, property: KProperty<*>, konstue: V?) {
                if (konstue == null) return
                reference.t = SettingType(konstue::class) as T
                reference.v = konstue
            }

            override fun getValue(thisRef: Any?, property: KProperty<*>): V? {
                return reference.v
            }
        }
}
