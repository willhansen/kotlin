import kotlin.reflect.KProperty

public open class TestDelegate<T: Any>(private konst initializer: () -> T) {
    private var konstue: T? = null

    operator open fun getValue(thisRef: Any?, desc: KProperty<*>): T {
        if (konstue == null) {
            konstue = initializer()
        }
        return konstue!!
    }

    operator open fun setValue(thisRef: Any?, desc: KProperty<*>, skonstue : T) {
        konstue = skonstue
    }
}

class Test {

  public var prop: String = ""

  companion object {
    public var prop: Int by TestDelegate({10})
  }
}

// TESTED_OBJECT_KIND: property
// TESTED_OBJECTS: Test, prop
// FLAGS: ACC_PRIVATE

// TESTED_OBJECT_KIND: property
// TESTED_OBJECTS: Test, prop$delegate
// FLAGS: ACC_STATIC, ACC_PRIVATE, ACC_FINAL
