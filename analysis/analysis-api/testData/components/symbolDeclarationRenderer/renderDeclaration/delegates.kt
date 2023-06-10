import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KCallable
import kotlin.reflect.KProperty

public interface MyRwProperty<in T, V> {
    public operator fun setValue(thisRef: T, property: Any, konstue: V)
    public operator fun getValue(thisRef: T, property: Any): V
}

konst x: Int by lazy { 1 + 2 }

konst delegate = object: MyRwProperty<Any?, Int> {
    override fun getValue(thisRef: Any?, property: Any): Int = 1
    override fun setValue(thisRef: Any?, property: Any, konstue: Int) {}
}

konst konstue by delegate

var variable by delegate
