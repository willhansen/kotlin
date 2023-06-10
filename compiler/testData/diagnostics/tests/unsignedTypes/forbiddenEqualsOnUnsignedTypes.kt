// !DIAGNOSTICS: -UNUSED_VARIABLE

fun test(
    ub1: UByte, ub2: UByte,
    us1: UShort, us2: UShort,
    ui1: UInt, ui2: UInt,
    ul1: ULong, ul2: ULong
) {
    konst ub = <!FORBIDDEN_IDENTITY_EQUALS!>ub1 === ub2<!> || <!FORBIDDEN_IDENTITY_EQUALS!>ub1 !== ub2<!>
    konst us = <!FORBIDDEN_IDENTITY_EQUALS!>us1 === us2<!> || <!FORBIDDEN_IDENTITY_EQUALS!>us1 !== us2<!>
    konst ui = <!FORBIDDEN_IDENTITY_EQUALS!>ui1 === ui2<!> || <!FORBIDDEN_IDENTITY_EQUALS!>ui1 !== ui2<!>
    konst ul = <!FORBIDDEN_IDENTITY_EQUALS!>ul1 === ul2<!> || <!FORBIDDEN_IDENTITY_EQUALS!>ul1 !== ul2<!>

    konst u = <!EQUALITY_NOT_APPLICABLE, FORBIDDEN_IDENTITY_EQUALS!>ub1 === ul1<!>

    konst a1 = <!FORBIDDEN_IDENTITY_EQUALS!>1u === 2u<!> || <!FORBIDDEN_IDENTITY_EQUALS!>1u !== 2u<!>
    konst a2 = <!FORBIDDEN_IDENTITY_EQUALS!>0xFFFF_FFFF_FFFF_FFFFu === 0xFFFF_FFFF_FFFF_FFFFu<!>

    konst bu1 = 1u
    konst bu2 = 1u

    konst c1 = <!FORBIDDEN_IDENTITY_EQUALS!>bu1 === bu2<!> || <!FORBIDDEN_IDENTITY_EQUALS!>bu1 !== bu2<!>
}