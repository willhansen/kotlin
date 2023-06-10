fun box() = Class().printSome()

public abstract class AbstractClass<T> {
    public fun printSome() : T = some

    public abstract konst some: T
}

public class Class: AbstractClass<String>() {
    public override konst some: String
        get() = "OK"

}
