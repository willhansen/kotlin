konst simpleNoTypeBlock
    get() {
        return <!UNRESOLVED_REFERENCE!>field<!>
    }

konst simpleNoTypeExpression
    get() = <!UNRESOLVED_REFERENCE!>field<!>

<!MUST_BE_INITIALIZED!>konst simpleTypeBlock: Int<!>
    get() {
        return field
    }

<!MUST_BE_INITIALIZED!>konst simpleTypeExpression: Int<!>
    get() = field

konst withFieldNoTypeBlock
    field = 3.14
    get() {
        // *TODO: add support for this?
        return <!UNRESOLVED_REFERENCE!>field<!>.toInt()
    }

konst withFieldNoTypeExpression
    field = 3.14
    get() = <!UNRESOLVED_REFERENCE!>field<!>.toInt()

konst withFieldTypeBlock: Int
    field = 3.14
    get() {
        return field.toInt()
    }

konst withFieldTypeExpression: Int
    field = 3.14
    get() = field.toInt()

// If * is supported, this is a relevant message
// since adding a getter _may_ be enough
<!PROPERTY_MUST_HAVE_GETTER!>konst minimalFieldWithInitializer
    field = 1<!>

<!PROPERTY_MUST_HAVE_GETTER!>konst minimalFieldWithNoInitializer
    <!PROPERTY_FIELD_DECLARATION_MISSING_INITIALIZER!>field: Int<!><!>

// TODO: redundant backing field?
// Or we assume someone may still want
// to access it directly via `myProperty#field`?
konst constWithFieldNoTypeBlock
    field = 3.14
    get() = 10

konst constWithFieldNoTypeExpression
    field = 3.14
    get() = 10

konst constWithFieldTypeBlock: Int
    field = 3.14
    get() = 10

konst constWithFieldTypeExpression: Int
    field = 3.14
    get() = 10
