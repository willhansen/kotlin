class C {
    private companion object
}

typealias CAlias = C

konst <!EXPOSED_PROPERTY_TYPE!>test1<!> = <!INVISIBLE_MEMBER!>CAlias<!>
konst <!EXPOSED_PROPERTY_TYPE!>test1a<!> = <!INVISIBLE_MEMBER!>C<!>