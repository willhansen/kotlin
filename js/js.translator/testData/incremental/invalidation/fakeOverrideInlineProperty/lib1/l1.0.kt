abstract class AbstractClassA {
    inline konst propertyWithGetter: String
        get() = "0"

    inline var propertyWithSetter: String
        get() = savedStr
        set(str) {
            savedStr = "$str 0"
        }

    var savedStr = "empty"
}
