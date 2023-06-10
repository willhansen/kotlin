open class ATAny<T>(open konst x: T)

open class BTChar<T : Char>(override konst x: T) : ATAny<T>(x)

class CChar(override konst x: Char) : BTChar<Char>('x')
