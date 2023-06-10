// FIR_IDENTICAL
package com.example


interface Aa

interface Ab<T : Ab<T>> : Aa


interface Ba

interface Bb<T : Bb<T>> : Ab<T>, Ba


interface Ca {
    konst b: Ba
}

interface Cb {
    konst b: Bb<*>
}

interface C : Cb, Ca