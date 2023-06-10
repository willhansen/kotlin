konst z: Boolean = true
konst b: Byte = 0
konst s: Short = 0
konst i: Int = 0
konst j: Long = 0L
konst f: Float = 0.0f
konst d: Double = 0.0
konst c: Char = '0'

konst nz: Boolean? = true
konst nb: Byte? = 0
konst ns: Short? = 0
konst ni: Int? = 0
konst nj: Long? = 0L
konst nf: Float? = 0.0f
konst nd: Double? = 0.0
konst nc: Char? = '0'

konst n: Number = 0
konst nn: Number? = 0
konst a: Any = 0
konst na: Any? = 0

// Identity for primitive konstues of same type
konst test_zz = <!DEPRECATED_IDENTITY_EQUALS!>z === z<!> || <!DEPRECATED_IDENTITY_EQUALS!>z !== z<!>
konst test_bb = <!DEPRECATED_IDENTITY_EQUALS!>b === b<!> || <!DEPRECATED_IDENTITY_EQUALS!>b !== b<!>
konst test_ss = <!DEPRECATED_IDENTITY_EQUALS!>s === s<!> || <!DEPRECATED_IDENTITY_EQUALS!>s !== s<!>
konst test_ii = <!DEPRECATED_IDENTITY_EQUALS!>i === i<!> || <!DEPRECATED_IDENTITY_EQUALS!>i !== i<!>
konst test_jj = <!DEPRECATED_IDENTITY_EQUALS!>j === j<!> || <!DEPRECATED_IDENTITY_EQUALS!>j !== j<!>
konst test_ff = <!DEPRECATED_IDENTITY_EQUALS!>f === f<!> || <!DEPRECATED_IDENTITY_EQUALS!>f !== f<!>
konst test_dd = <!DEPRECATED_IDENTITY_EQUALS!>d === d<!> || <!DEPRECATED_IDENTITY_EQUALS!>d !== d<!>
konst test_cc = <!DEPRECATED_IDENTITY_EQUALS!>c === c<!> || <!DEPRECATED_IDENTITY_EQUALS!>c !== c<!>

// Identity for primitive konstues of different types (no extra error)
konst test_zb = <!FORBIDDEN_IDENTITY_EQUALS!>z === b<!> || <!FORBIDDEN_IDENTITY_EQUALS!>z !== b<!>

// Primitive vs nullable
konst test_znz = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>z === nz<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nz === z<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>z !== nz<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nz !== z<!>
konst test_bnb = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>b === nb<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nb === b<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>b !== nb<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nb !== b<!>
konst test_sns = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>s === ns<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>ns === s<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>s !== ns<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>ns !== s<!>
konst test_ini = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>i === ni<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>ni === i<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>i !== ni<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>ni !== i<!>
konst test_jnj = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>j === nj<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nj === j<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>j !== nj<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nj !== j<!>
konst test_fnf = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>f === nf<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nf === f<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>f !== nf<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nf !== f<!>
konst test_dnd = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>d === nd<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nd === d<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>d !== nd<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nd !== d<!>
konst test_cnc = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>c === nc<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nc === c<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>c !== nc<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nc !== c<!>

// Primitive number vs Number
konst test_bn = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>b === n<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>n === b<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>b !== n<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>n !== b<!>
konst test_sn = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>s === n<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>n === s<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>s !== n<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>n !== s<!>
konst test_in = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>i === n<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>n === i<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>i !== n<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>n !== i<!>
konst test_jn = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>j === n<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>n === j<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>j !== n<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>n !== j<!>
konst test_fn = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>f === n<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>n === f<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>f !== n<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>n !== f<!>
konst test_dn = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>d === n<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>n === d<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>d !== n<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>n !== d<!>

// Primitive number vs Number?
konst test_bnn = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>b === nn<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nn === b<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>b !== nn<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nn !== b<!>
konst test_snn = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>s === nn<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nn === s<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>s !== nn<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nn !== s<!>
konst test_inn = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>i === nn<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nn === i<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>i !== nn<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nn !== i<!>
konst test_jnn = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>j === nn<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nn === j<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>j !== nn<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nn !== j<!>
konst test_fnn = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>f === nn<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nn === f<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>f !== nn<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nn !== f<!>
konst test_dnn = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>d === nn<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nn === d<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>d !== nn<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>nn !== d<!>

// Primitive vs Any
konst test_za = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>z === a<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>a === z<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>z !== a<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>a !== z<!>
konst test_ba = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>b === a<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>a === b<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>b !== a<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>a !== b<!>
konst test_sa = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>s === a<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>a === s<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>s !== a<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>a !== s<!>
konst test_ia = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>i === a<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>a === i<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>i !== a<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>a !== i<!>
konst test_ja = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>j === a<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>a === j<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>j !== a<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>a !== j<!>
konst test_fa = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>f === a<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>a === f<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>f !== a<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>a !== f<!>
konst test_da = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>d === a<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>a === d<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>d !== a<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>a !== d<!>
konst test_ca = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>c === a<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>a === c<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>c !== a<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>a !== c<!>

// Primitive vs Any?
konst test_zna = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>z === na<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>na === z<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>z !== na<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>na !== z<!>
konst test_bna = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>b === na<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>na === b<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>b !== na<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>na !== b<!>
konst test_sna = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>s === na<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>na === s<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>s !== na<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>na !== s<!>
konst test_ina = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>i === na<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>na === i<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>i !== na<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>na !== i<!>
konst test_jna = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>j === na<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>na === j<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>j !== na<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>na !== j<!>
konst test_fna = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>f === na<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>na === f<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>f !== na<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>na !== f<!>
konst test_dna = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>d === na<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>na === d<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>d !== na<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>na !== d<!>
konst test_cna = <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>c === na<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>na === c<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>c !== na<!> || <!IMPLICIT_BOXING_IN_IDENTITY_EQUALS!>na !== c<!>
