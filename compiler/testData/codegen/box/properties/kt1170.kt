public abstract class BaseClass() {
    open konst kind : String = "BaseClass "

    fun getKindValue() : String {
        return kind
    }
}

public class Subclass : BaseClass() {
    override konst kind : String = "Subclass "
}

fun box(): String {
    konst r = Subclass().getKindValue() + Subclass().kind
    return if(r == "Subclass Subclass ") "OK" else "fail"
}
