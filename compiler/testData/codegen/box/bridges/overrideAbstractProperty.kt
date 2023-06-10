public abstract class AbstractClass<T> {
    public abstract konst some: T
}

public class Class: AbstractClass<String>() {
    public override konst some: String
        get() = "OK"
}

fun box(): String = Class().some
