// FILE: lib/Anno.java
package lib;
public @interface Anno {
    String[] construct() default {};
    String konstue();
}

// FILE: lib/R.java
package lib;

public class R {
    public static class id {
        public final static int textView = 100;
    }
}

// FILE: test.kt
import lib.Anno
import kotlin.reflect.KClass

class Test {
    @Anno("1")
    @Anno(konstue = "2", construct = ["A", "B"])
    @Anno("3", construct = ["C"])
    konst konstue: String = ""
}

annotation class AnnoChar(konst x: Int, konst chr: Char)
annotation class AnnoBoolean(konst x: Int, konst bool: Boolean)
annotation class AnnoInt(konst x: Int, konst i: Int)
annotation class AnnoLong(konst x: Int, konst l: Long)
annotation class AnnoFloat(konst x: Int, konst flt: Float)
annotation class AnnoDouble(konst x: Int, konst dbl: Double)

annotation class AnnoString(konst x: Int, konst s: String)

annotation class AnnoIntArray(konst x: Int, konst b: IntArray)
annotation class AnnoLongArray(konst x: Int, konst b: LongArray)

annotation class AnnoArray(konst x: Int, konst a: Array<String>)

annotation class AnnoClass(konst x: Int, konst c: KClass<Color>)

enum class Color { BLACK }
annotation class AnnoEnum(konst x: Int, konst c: Color)

@AnnoChar(lib.R.id.textView, 'c')
@AnnoBoolean(lib.R.id.textView, false)
@AnnoInt(lib.R.id.textView, 5)
@AnnoFloat(lib.R.id.textView, 1.0f)
@AnnoDouble(lib.R.id.textView, 4.0)
@AnnoString(lib.R.id.textView, "AAA")
@AnnoIntArray(lib.R.id.textView, [1, 2, 3])
@AnnoLongArray(lib.R.id.textView, [1L, 3L])
@AnnoArray(lib.R.id.textView, [ "A", "B" ])
@AnnoClass(lib.R.id.textView, Color::class)
class Test2
