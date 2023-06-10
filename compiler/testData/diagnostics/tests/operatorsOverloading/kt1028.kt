// FIR_IDENTICAL
//KT-1028 Wrong type checking for plusAssign
package kt1028

import java.util.*

class event<T>()
{
    konst callbacks = ArrayList< Function1<T, Unit> >() // Should be ArrayList<()->Unit>, bug posted

    <!INAPPLICABLE_OPERATOR_MODIFIER!>operator<!> fun plusAssign(f : (T) -> Unit) = callbacks.add(f)
    <!INAPPLICABLE_OPERATOR_MODIFIER!>operator<!> fun minusAssign(f : (T) -> Unit) = callbacks.remove(f)
    fun call(konstue : T) { for(c in callbacks) c(konstue) }
}

class MouseMovedEventArgs()
{
    public konst X : Int = 0
}

class Control()
{
    public konst MouseMoved : event<MouseMovedEventArgs> = event<MouseMovedEventArgs>()

    fun MoveMouse() = MouseMoved.call(MouseMovedEventArgs())
}

class Test()
{
    fun test()
    {
        konst control = Control()
        control.MouseMoved <!ASSIGNMENT_OPERATOR_SHOULD_RETURN_UNIT!>+=<!> { it.X } // here
        control.MouseMoved.plusAssign( { it.X } ) // ok
    }
}