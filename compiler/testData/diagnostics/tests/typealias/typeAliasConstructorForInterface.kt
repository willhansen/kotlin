// FIR_IDENTICAL
interface IFoo

typealias Test = IFoo

konst testAsFunction = <!RESOLUTION_TO_CLASSIFIER!>Test<!>()
konst testAsValue = <!NO_COMPANION_OBJECT!>Test<!>