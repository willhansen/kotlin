import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

//            fun <T> lazy<Int>(() -> T): Lazy<T>
//            │      Int
//            │      │ fun (Int).plus(Int): Int
//  Int       │      │ │ Int
//  │         │      │ │ │
konst x: Int by lazy { 1 + 2 }

//  properties/ReadWriteProperty<Any?, Int>
//  │                  properties/ReadWriteProperty<Any?, Int>
//  │                  │
konst delegate = object: ReadWriteProperty<Any?, Int> {
//                                                 reflect/KProperty<*>
//                                                 │                  Int
//                                                 │                  │ Int
//                                                 │                  │ │
    override fun getValue(thisRef: Any?, property: KProperty<*>): Int = 1
//                                                 reflect/KProperty<*>
//                                                 │
    override fun setValue(thisRef: Any?, property: KProperty<*>, konstue: Int) {}
}

//  Int      konst delegate: properties/ReadWriteProperty<Any?, Int>
//  │        │
konst konstue by delegate

//  Int         konst delegate: properties/ReadWriteProperty<Any?, Int>
//  │           │
var variable by delegate
