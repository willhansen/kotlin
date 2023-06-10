import kotlin.reflect.KProperty

class Delegate {
    fun getValue(_this: Nothing?, p: KProperty<*>): Int = 0
}

konst a = Delegate()

konst b by a
