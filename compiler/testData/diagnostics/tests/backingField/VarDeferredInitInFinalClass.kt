// FIR_IDENTICAL
// DIAGNOSTICS: -DEBUG_INFO_LEAKING_THIS










//////////////////////////////////////////////////////////////////////////////////////////////////
/// READ THIS TEST AS A TABLE IN IDE (It may be not correctly displayed in Space or GitHub)!!! ///
//////////////////////////////////////////////////////////////////////////////////////////////////











// a = final + not initialized in place + deferred init
// e = final + not initialized in place
// c = final + initialized in place

// b = open + not initialized in place + deferred init
// f = open + not initialized in place
// d = open + initialized in place
class Foo : I {
    //                                             no setter;                                                                 setter with field;                                                                       setter with empty body;                                                        setter no field;
    // no getter
                                                   var a00: Int;                                <!MUST_BE_INITIALIZED!>var a01: Int<!>; set(v) { field = v };                                                               var a02: Int; set;                              <!MUST_BE_INITIALIZED!>var a03: Int<!>; set(v) {};
             <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var e00: Int<!>;                             <!MUST_BE_INITIALIZED!>var e01: Int<!>; set(v) { field = v };                         <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var e02: Int<!>; set;                           <!MUST_BE_INITIALIZED!>var e03: Int<!>; set(v) {};
                                                   var c00: Int = 1;                                                   var c01: Int = 1; set(v) { field = v };                                                              var c02: Int = 1; set;                                                 var c03: Int = 1; set(v) {};
                                          override var b00: Int;                       <!MUST_BE_INITIALIZED!>override var b01: Int<!>; set(v) { field = v };                                                      override var b02: Int; set;                     <!MUST_BE_INITIALIZED!>override var b03: Int<!>; set(v) {};
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>override var f00: Int<!>;                    <!MUST_BE_INITIALIZED!>override var f01: Int<!>; set(v) { field = v };                <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>override var f02: Int<!>; set;                  <!MUST_BE_INITIALIZED!>override var f03: Int<!>; set(v) {};
                                          override var d00: Int = 1;                                          override var d01: Int = 1; set(v) { field = v };                                                     override var d02: Int = 1; set;                                        override var d03: Int = 1; set(v) {};
    // getter with field
                                                   var a10: Int; get() = field;                 <!MUST_BE_INITIALIZED!>var a11: Int<!>; set(v) { field = v } get() = field;                                                 var a12: Int; set get() = field;                <!MUST_BE_INITIALIZED!>var a13: Int<!>; set(v) {} get() = field;
                            <!MUST_BE_INITIALIZED!>var e10: Int<!>; get() = field;              <!MUST_BE_INITIALIZED!>var e11: Int<!>; set(v) { field = v } get() = field;                          <!MUST_BE_INITIALIZED!>var e12: Int<!>; set get() = field;             <!MUST_BE_INITIALIZED!>var e13: Int<!>; set(v) {} get() = field;
                                                   var c10: Int = 1; get() = field;                                    var c11: Int = 1; set(v) { field = v } get() = field;                                                var c12: Int = 1; set get() = field;                                   var c13: Int = 1; set(v) {} get() = field;
                                          override var b10: Int; get() = field;        <!MUST_BE_INITIALIZED!>override var b11: Int<!>; set(v) { field = v } get() = field;                                        override var b12: Int; set get() = field;       <!MUST_BE_INITIALIZED!>override var b13: Int<!>; set(v) {} get() = field;
                   <!MUST_BE_INITIALIZED!>override var f10: Int<!>; get() = field;     <!MUST_BE_INITIALIZED!>override var f11: Int<!>; set(v) { field = v } get() = field;                 <!MUST_BE_INITIALIZED!>override var f12: Int<!>; set get() = field;    <!MUST_BE_INITIALIZED!>override var f13: Int<!>; set(v) {} get() = field;
                                          override var d10: Int = 1; get() = field;                           override var d11: Int = 1; set(v) { field = v } get() = field;                                       override var d12: Int = 1; set get() = field;                          override var d13: Int = 1; set(v) {} get() = field;
    // getter with empty body
                                                   var a20: Int; get;                           <!MUST_BE_INITIALIZED!>var a21: Int<!>; set(v) { field = v } get;                                                           var a22: Int; set get;                          <!MUST_BE_INITIALIZED!>var a23: Int<!>; set(v) {} get;
             <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var e20: Int<!>; get;                        <!MUST_BE_INITIALIZED!>var e21: Int<!>; set(v) { field = v } get;                     <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var e22: Int<!>; set get;                       <!MUST_BE_INITIALIZED!>var e23: Int<!>; set(v) {} get;
                                                   var c20: Int = 1; get;                                              var c21: Int = 1; set(v) { field = v } get;                                                          var c22: Int = 1; set get;                                             var c23: Int = 1; set(v) {} get;
                                          override var b20: Int; get;                  <!MUST_BE_INITIALIZED!>override var b21: Int<!>; set(v) { field = v } get;                                                  override var b22: Int; set get;                 <!MUST_BE_INITIALIZED!>override var b23: Int<!>; set(v) {} get;
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>override var f20: Int<!>; get;               <!MUST_BE_INITIALIZED!>override var f21: Int<!>; set(v) { field = v } get;            <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>override var f22: Int<!>; set get;              <!MUST_BE_INITIALIZED!>override var f23: Int<!>; set(v) {} get;
                                          override var d20: Int = 1; get;                                     override var d21: Int = 1; set(v) { field = v } get;                                                 override var d22: Int = 1; set get;                                    override var d23: Int = 1; set(v) {} get;
    // getter no field
                                                   var a30: Int; get() = 1;                     <!MUST_BE_INITIALIZED!>var a31: Int<!>; set(v) { field = v } get() = 1;                                                     var a32: Int; set get() = 1;                                           var a33: Int; set(v) {} get() = 1;
                            <!MUST_BE_INITIALIZED!>var e30: Int<!>; get() = 1;                  <!MUST_BE_INITIALIZED!>var e31: Int<!>; set(v) { field = v } get() = 1;                              <!MUST_BE_INITIALIZED!>var e32: Int<!>; set get() = 1;                                        var e33: Int; set(v) {} get() = 1;
                                                   var c30: Int = 1; get() = 1;                                        var c31: Int = 1; set(v) { field = v } get() = 1;                                                    var c32: Int = 1; set get() = 1;                                       var c33: Int = <!PROPERTY_INITIALIZER_NO_BACKING_FIELD!>1<!>; set(v) {} get() = 1;
                                          override var b30: Int; get() = 1;            <!MUST_BE_INITIALIZED!>override var b31: Int<!>; set(v) { field = v } get() = 1;                                            override var b32: Int; set get() = 1;                                  override var b33: Int; set(v) {} get() = 1;
                   <!MUST_BE_INITIALIZED!>override var f30: Int<!>; get() = 1;         <!MUST_BE_INITIALIZED!>override var f31: Int<!>; set(v) { field = v } get() = 1;                     <!MUST_BE_INITIALIZED!>override var f32: Int<!>; set get() = 1;                               override var f33: Int; set(v) {} get() = 1;
                                          override var d30: Int = 1; get() = 1;                               override var d31: Int = 1; set(v) { field = v } get() = 1;                                           override var d32: Int = 1; set get() = 1;                              override var d33: Int = <!PROPERTY_INITIALIZER_NO_BACKING_FIELD!>1<!>; set(v) {} get() = 1;

    init {
        a00 = 1
        a01 = 1
        a02 = 1
        a03 = 1
        a10 = 1
        a11 = 1
        a12 = 1
        a13 = 1
        a20 = 1
        a21 = 1
        a22 = 1
        a23 = 1
        a30 = 1
        a31 = 1
        a32 = 1
        a33 = 1

        b00 = 1
        b01 = 1
        b02 = 1
        b03 = 1
        b10 = 1
        b11 = 1
        b12 = 1
        b13 = 1
        b20 = 1
        b21 = 1
        b22 = 1
        b23 = 1
        b30 = 1
        b31 = 1
        b32 = 1
        b33 = 1
    }
}

interface I {
    konst b00: Int
    konst b01: Int
    konst b02: Int
    konst b03: Int
    konst b10: Int
    konst b11: Int
    konst b12: Int
    konst b13: Int
    konst b20: Int
    konst b21: Int
    konst b22: Int
    konst b23: Int
    konst b30: Int
    konst b31: Int
    konst b32: Int
    konst b33: Int

    konst f00: Int
    konst f01: Int
    konst f02: Int
    konst f03: Int
    konst f10: Int
    konst f11: Int
    konst f12: Int
    konst f13: Int
    konst f20: Int
    konst f21: Int
    konst f22: Int
    konst f23: Int
    konst f30: Int
    konst f31: Int
    konst f32: Int
    konst f33: Int

    konst d00: Int
    konst d01: Int
    konst d02: Int
    konst d03: Int
    konst d10: Int
    konst d11: Int
    konst d12: Int
    konst d13: Int
    konst d20: Int
    konst d21: Int
    konst d22: Int
    konst d23: Int
    konst d30: Int
    konst d31: Int
    konst d32: Int
    konst d33: Int
}
