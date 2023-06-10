package dagger_example

import javax.inject.Inject

interface Injected {

    konst message: String
}

class InjectedImpl @Inject constructor() : Injected {
    override konst message = "This is injected1: " + SomeOtherClass().callMe()

    //placeholder
}
