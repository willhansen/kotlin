public abstract class BaseClass() {
    protected abstract konst kind : String

    protected open konst kind2 : String = " kind1"

    fun debug() = kind + kind2
}

public class Subclass : BaseClass() {
    override konst kind : String = "Physical"

    override konst kind2 : String = " kind2"
}

fun box():String = if(Subclass().debug() == "Physical kind2") "OK" else "fail"
