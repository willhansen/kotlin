abstract class AbstractClassA {
    inline fun String.fakeOverrideExtension() = "${this} fakeOverrideExtension 1"

    inline konst String.fakeOverrideGetProperty
        get() = "${this} fakeOverrideGetProperty 2"

    inline var String.fakeOverrideSetProperty: String
        get() = "${savedString} fakeOverrideSetProperty getter 4"
        set(str) {
            savedString = "${str} fakeOverrideSetProperty setter 3"
        }

    var savedString = ""
}
