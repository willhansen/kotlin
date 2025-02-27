# Changelog 1.1.X

## 1.1.60

### Android

#### New Features

- [`KT-20051`](https://youtrack.jetbrains.com/issue/KT-20051) Quickfixes to support @Parcelize
#### Fixes

- [`KT-19747`](https://youtrack.jetbrains.com/issue/KT-19747) Android extensions + Parcelable: VerifyError in case of RawValue annotation on a type when it's unknown how to parcel it
- [`KT-19899`](https://youtrack.jetbrains.com/issue/KT-19899) Parcelize: Building with ProGuard enabled
- [`KT-19988`](https://youtrack.jetbrains.com/issue/KT-19988) [Android Extensions] inner class LayoutContainer causes NoSuchMethodError
- [`KT-20002`](https://youtrack.jetbrains.com/issue/KT-20002) Parcelize explodes on LongArray
- [`KT-20019`](https://youtrack.jetbrains.com/issue/KT-20019) Parcelize does not propogate flags argument when writing nested Parcelable
- [`KT-20020`](https://youtrack.jetbrains.com/issue/KT-20020) Parcelize does not use primitive array read/write methods on Parcel
- [`KT-20021`](https://youtrack.jetbrains.com/issue/KT-20021) Parcelize does not serialize Parcelable enum as Parcelable
- [`KT-20022`](https://youtrack.jetbrains.com/issue/KT-20022) Parcelize should dispatch directly to java.lang.Enum when writing an enum.
- [`KT-20034`](https://youtrack.jetbrains.com/issue/KT-20034) Application installation failed (INSTALL_FAILED_DEXOPT) in Android 4.3 devices if I use Parcelize
- [`KT-20057`](https://youtrack.jetbrains.com/issue/KT-20057) Parcelize should use specialized write/create methods where available.
- [`KT-20062`](https://youtrack.jetbrains.com/issue/KT-20062) Parceler should allow otherwise un-parcelable property types in enclosing class.

### Compiler

#### Performance Improvements

- [`KT-20462`](https://youtrack.jetbrains.com/issue/KT-20462) Don't create an array copy for '*<array-constructor-fun>(...)'
#### Fixes

- [`KT-14697`](https://youtrack.jetbrains.com/issue/KT-14697) Use-site targeted annotation is not correctly loaded from class file
- [`KT-17680`](https://youtrack.jetbrains.com/issue/KT-17680) Android Studio and multiple tests in single file
- [`KT-19251`](https://youtrack.jetbrains.com/issue/KT-19251) Stack spilling in constructor arguments breaks Quasar
- [`KT-19592`](https://youtrack.jetbrains.com/issue/KT-19592) Apply JSR 305 default nullability qualifiers with to generic type arguments if they're applicable for TYPE_USE
- [`KT-20016`](https://youtrack.jetbrains.com/issue/KT-20016) JSR 305: default nullability qualifiers are ignored in TYPE_USE and PARAMETER positions
- [`KT-20131`](https://youtrack.jetbrains.com/issue/KT-20131) Support @NonNull(when = NEVER) nullability annotation
- [`KT-20158`](https://youtrack.jetbrains.com/issue/KT-20158) Preserve flexibility for Java types annotated with @NonNull(when = UNKNOWN)
- [`KT-20337`](https://youtrack.jetbrains.com/issue/KT-20337) No multifile class facade is generated for files with type aliases only
- [`KT-20387`](https://youtrack.jetbrains.com/issue/KT-20387) Wrong argument generated for accessor call of a protected generic 'operator fun get/set' from base class with primitive type as type parameter
- [`KT-20418`](https://youtrack.jetbrains.com/issue/KT-20418) Wrong code generated for literal long range with mixed integer literal ends
- [`KT-20491`](https://youtrack.jetbrains.com/issue/KT-20491) Incorrect synthetic accessor generated for a generic base class function specialized with primitive type
- [`KT-20651`](https://youtrack.jetbrains.com/issue/KT-20651) "Don't know how to generate outer expression" for enum-konstues with non-trivial self-closures
- [`KT-20707`](https://youtrack.jetbrains.com/issue/KT-20707) Support when by enum in kotlin scripts
- [`KT-20879`](https://youtrack.jetbrains.com/issue/KT-20879) Compiler problem in when-expressions

### IDE

#### New Features

- [`KT-14175`](https://youtrack.jetbrains.com/issue/KT-14175) Surround with try ... catch (... finally) doesn't work for expressions
- [`KT-15769`](https://youtrack.jetbrains.com/issue/KT-15769) Join lines could "convert to expression body"
- [`KT-19134`](https://youtrack.jetbrains.com/issue/KT-19134) IntelliJ Color Scheme editor - allow changing color of colons and double colons
- [`KT-20308`](https://youtrack.jetbrains.com/issue/KT-20308) New Gradle with Kotlin DSL project wizard
#### Fixes

- [`KT-15932`](https://youtrack.jetbrains.com/issue/KT-15932) Attempt to rename private property finds unrelated usages
- [`KT-18996`](https://youtrack.jetbrains.com/issue/KT-18996) After Kotlin compiler settings change: 'Apply' button doesn't work
- [`KT-19458`](https://youtrack.jetbrains.com/issue/KT-19458) Resolver for 'completion/highlighting in ScriptModuleInfo for build.gradle.kts / JVM' does not know how to resolve LibraryInfo
- [`KT-19474`](https://youtrack.jetbrains.com/issue/KT-19474) Kotlin Gradle Script: highlighting fails on unresolved references
- [`KT-19823`](https://youtrack.jetbrains.com/issue/KT-19823) Kotlin Gradle project import into IntelliJ: import kapt generated classes into classpath
- [`KT-19958`](https://youtrack.jetbrains.com/issue/KT-19958) Android: kotlinOptions from build.gradle are not imported into facet
- [`KT-19972`](https://youtrack.jetbrains.com/issue/KT-19972) AssertionError “Resolver for 'completion/highlighting in ModuleProductionSourceInfo(module=Module: 'kotlin-pure_main') for files dummy.kt for platform JVM' does not know how to resolve SdkInfo“ on copying Kotlin file with kotlin.* imports from other project
- [`KT-20112`](https://youtrack.jetbrains.com/issue/KT-20112) maven dependency type test-jar with scope compile not 
- [`KT-20185`](https://youtrack.jetbrains.com/issue/KT-20185) Stub and PSI element type mismatch for "var nullableSuspend: (suspend (P) -> Unit)? = null"
- [`KT-20199`](https://youtrack.jetbrains.com/issue/KT-20199) Cut action is not available during indexing
- [`KT-20331`](https://youtrack.jetbrains.com/issue/KT-20331) Wrong EAP repository
- [`KT-20346`](https://youtrack.jetbrains.com/issue/KT-20346) Can't build tests in common code due to missing org.jetbrains.kotlin:kotlin-test-js testCompile dependency in JS
- [`KT-20419`](https://youtrack.jetbrains.com/issue/KT-20419) Android Studio plugin 1.1.50 show multiple gutter icon for the same item
- [`KT-20519`](https://youtrack.jetbrains.com/issue/KT-20519) Error “Parameter specified as non-null is null: method ModuleGrouperKt.isQualifiedModuleNamesEnabled” on creating Gradle (Kotlin DSL) project from scratch
- [`KT-20550`](https://youtrack.jetbrains.com/issue/KT-20550) Spring: "Navigate to autowired candidates" gutter action is missed (IDEA 2017.3)
- [`KT-20566`](https://youtrack.jetbrains.com/issue/KT-20566) Spring: "Navigate to the spring beans declaration" gutter action for `@ComponentScan` is missed (IDEA 2017.3)
- [`KT-20621`](https://youtrack.jetbrains.com/issue/KT-20621) Provide automatic migration from JetRunConfigurationType to KotlinRunConfigurationType
- [`KT-20648`](https://youtrack.jetbrains.com/issue/KT-20648) Do we need a separate ProjectImportProvider for gradle kotlin dsl projects?
- [`KT-20782`](https://youtrack.jetbrains.com/issue/KT-20782) Non-atomic trees update
- [`KT-20789`](https://youtrack.jetbrains.com/issue/KT-20789) Can't navigate to inline call/inline use site when runner is delegated to Gradle
- [`KT-20843`](https://youtrack.jetbrains.com/issue/KT-20843) Kotlin TypeDeclarationProvider may stop other declarations providers execution
- [`KT-20929`](https://youtrack.jetbrains.com/issue/KT-20929) Import Project from Gradle wizard: the same page is shown twice

### IDE. Completion

- [`KT-16383`](https://youtrack.jetbrains.com/issue/KT-16383) IllegalStateException: Failed to create expression from text: '<init>' on choosing ByteArray from completion list
- [`KT-18458`](https://youtrack.jetbrains.com/issue/KT-18458) Spring: code completion does not suggest bean names inside `@Qualifier` before function parameter
- [`KT-20256`](https://youtrack.jetbrains.com/issue/KT-20256) java.lang.Throwable “Inkonstid range specified” on editing template inside string literal

### IDE. Inspections and Intentions

#### New Features

- [`KT-14695`](https://youtrack.jetbrains.com/issue/KT-14695) Simplify comparison intention produces meaningless statement for assert()
- [`KT-17204`](https://youtrack.jetbrains.com/issue/KT-17204) Add `Assign to property quickfix`
- [`KT-18220`](https://youtrack.jetbrains.com/issue/KT-18220) Add data modifier to a class quickfix
- [`KT-18742`](https://youtrack.jetbrains.com/issue/KT-18742) Add quick-fix for CANNOT_CHECK_FOR_ERASED
- [`KT-19735`](https://youtrack.jetbrains.com/issue/KT-19735) Add quickfix for type mismatch that converts Sequence/Array/List
- [`KT-20259`](https://youtrack.jetbrains.com/issue/KT-20259) Show warning if arrays are compared by '!='
#### Fixes

- [`KT-10546`](https://youtrack.jetbrains.com/issue/KT-10546) Wrong "Unused property" warning on using inline object syntax
- [`KT-16394`](https://youtrack.jetbrains.com/issue/KT-16394) "Convert reference to lambda" generates wrong code
- [`KT-16808`](https://youtrack.jetbrains.com/issue/KT-16808) Intention "Remove unnecessary parantheses" is erroneously proposed for elvis operator on LHS of `in` operator if RHS of elvis is return with konstue
- [`KT-17437`](https://youtrack.jetbrains.com/issue/KT-17437) Class highlighted as unused even if Companion methods/fields really used
- [`KT-19377`](https://youtrack.jetbrains.com/issue/KT-19377) Inspections are run for Kotlin Gradle DSL sources
- [`KT-19420`](https://youtrack.jetbrains.com/issue/KT-19420) Kotlin Gradle script editor: suggestion to import required class from stdlib fails with AE: ResolverForProjectImpl.descriptorForModule()
- [`KT-19626`](https://youtrack.jetbrains.com/issue/KT-19626) (Specify type explicitly) Descriptor was not found for VALUE_PARAMETER
- [`KT-19674`](https://youtrack.jetbrains.com/issue/KT-19674) 'Convert property initializer to getter' intention fails on incompilable initializer with AssertionError at SpecifyTypeExplicitlyIntention$Companion.addTypeAnnotationWithTemplate() 
- [`KT-19782`](https://youtrack.jetbrains.com/issue/KT-19782) Surround with if else doesn't work for expressions
- [`KT-20010`](https://youtrack.jetbrains.com/issue/KT-20010) 'Replace safe access expression with 'if' expression' IDEA Kotlin plugin intention may failed
- [`KT-20104`](https://youtrack.jetbrains.com/issue/KT-20104) "Recursive property accessor" reports false positive when property reference is used in the assignment
- [`KT-20218`](https://youtrack.jetbrains.com/issue/KT-20218) AE on calling intention “Convert to secondary constructor” for already referred argument 
- [`KT-20231`](https://youtrack.jetbrains.com/issue/KT-20231) False positive 'Redundant override' when delegated member hides super type override
- [`KT-20261`](https://youtrack.jetbrains.com/issue/KT-20261) Incorrect "Redundant Unit return type" inspection for Nothing-typed expression
- [`KT-20315`](https://youtrack.jetbrains.com/issue/KT-20315) "call chain on collection type may be simplified" generates code that does not compile
- [`KT-20333`](https://youtrack.jetbrains.com/issue/KT-20333) Assignment can be lifted out of try is applied too broadly
- [`KT-20366`](https://youtrack.jetbrains.com/issue/KT-20366) Code cleanup: some inspections are broken 
- [`KT-20369`](https://youtrack.jetbrains.com/issue/KT-20369) Inspection messages with INFORMATION highlight type are shown in Code Inspect
- [`KT-20409`](https://youtrack.jetbrains.com/issue/KT-20409) useless warning "Remove curly braces" for Chinese character
- [`KT-20417`](https://youtrack.jetbrains.com/issue/KT-20417) Converting property getter to block body doesn't insert explicit return type

### IDE. Refactorings

#### Performance Improvements

- [`KT-18823`](https://youtrack.jetbrains.com/issue/KT-18823) Move class to a separate file is very slow in 'kotlin' project
- [`KT-20205`](https://youtrack.jetbrains.com/issue/KT-20205) Invoke MoveKotlinDeclarationsProcessor.findUsages() under progress
#### Fixes

- [`KT-15840`](https://youtrack.jetbrains.com/issue/KT-15840) Introduce type alias: don't change not-nullable type with nullable typealias
- [`KT-17949`](https://youtrack.jetbrains.com/issue/KT-17949) Rename private fun should not search it out of scope
- [`KT-18196`](https://youtrack.jetbrains.com/issue/KT-18196) Refactor / Copy: the copy is formatted
- [`KT-18594`](https://youtrack.jetbrains.com/issue/KT-18594) Refactor / Extract (Functional) Parameter are available for annotation arguments, but fail with AE: "Body element is not found"
- [`KT-19439`](https://youtrack.jetbrains.com/issue/KT-19439) Kotlin introduce parameter causes exception
- [`KT-19909`](https://youtrack.jetbrains.com/issue/KT-19909) copy a kotlin class removes imports and other modifications
- [`KT-19949`](https://youtrack.jetbrains.com/issue/KT-19949) AssertionError „Resolver for 'project source roots and libraries for platform JVM' does not know how to resolve ModuleProductionSourceInfo“ through MoveConflictChecker.getModuleDescriptor() on copying Kotlin file from other project
- [`KT-20092`](https://youtrack.jetbrains.com/issue/KT-20092) Refactor / Copy: copy of .kt file removes all the blank lines and 'hanging' comments
- [`KT-20335`](https://youtrack.jetbrains.com/issue/KT-20335) Refactor → Extract Type Parameter: “AWT events are not allowed inside write action” after processing duplicates
- [`KT-20402`](https://youtrack.jetbrains.com/issue/KT-20402) Throwable “PsiElement(IDENTIFIER) by KotlinInplaceParameterIntroducer” on calling Refactor → Extract Parameter for default konstues
- [`KT-20403`](https://youtrack.jetbrains.com/issue/KT-20403) AE “Body element is not found” on calling Refactor → Extract Parameter for default konstues in constructor of class without body

### JavaScript

#### Fixes

- [`KT-8285`](https://youtrack.jetbrains.com/issue/KT-8285) JS: don't generate tmp when only need one component
- [`KT-14549`](https://youtrack.jetbrains.com/issue/KT-14549) JS: Non-local returns from secondary constructors don't work
- [`KT-15294`](https://youtrack.jetbrains.com/issue/KT-15294) JS: parse error in `js()` function
- [`KT-17450`](https://youtrack.jetbrains.com/issue/KT-17450) PlatformDependent members of collections are compiled in JS
- [`KT-18010`](https://youtrack.jetbrains.com/issue/KT-18010) JS: JsName annotation in interfaces can cause runtime exception
- [`KT-18063`](https://youtrack.jetbrains.com/issue/KT-18063) Inlining does not work properly in JS for suspend functions from another module
- [`KT-18548`](https://youtrack.jetbrains.com/issue/KT-18548) JS: wrong string interpolation with generic or Any parameters
- [`KT-19794`](https://youtrack.jetbrains.com/issue/KT-19794) runtime crash with empty object (Javascript) 
- [`KT-19818`](https://youtrack.jetbrains.com/issue/KT-19818) JS: generate paths relative to .map file by default (unless "-source-map-prefix" is used)
- [`KT-19906`](https://youtrack.jetbrains.com/issue/KT-19906) JS: rename compiler option "-source-map-source-roots" to avoid misleading since sourcemaps have field called "sourceRoot"
- [`KT-20287`](https://youtrack.jetbrains.com/issue/KT-20287) Functions don't actually return Unit in Kotlin-JS -> unexpected null problems vs JDK version
- [`KT-20451`](https://youtrack.jetbrains.com/issue/KT-20451) KotlinJs - interface function with default parameter, overridden by implementor, can't be found at runtime
- [`KT-20650`](https://youtrack.jetbrains.com/issue/KT-20650) JS: compiler crashes in Java 9 with NoClassDefFoundError
- [`KT-20653`](https://youtrack.jetbrains.com/issue/KT-20653) JS: compiler crashes in Java 9 with TranslationRuntimeException
- [`KT-20820`](https://youtrack.jetbrains.com/issue/KT-20820) JS: IDEA project doesn't generate paths relative to .map

### Libraries

- [`KT-20596`](https://youtrack.jetbrains.com/issue/KT-20596) 'synchronized' does not allow non-local return in Kotlin JS
- [`KT-20600`](https://youtrack.jetbrains.com/issue/KT-20600) Typo in POMs for kotlin-runtime

### Tools

- [`KT-19692`](https://youtrack.jetbrains.com/issue/KT-19692) kotlin-jpa plugin doesn't support @MappedSuperclass annotation
- [`KT-20030`](https://youtrack.jetbrains.com/issue/KT-20030) Parcelize can directly reference writeToParcel and CREATOR for final, non-Parcelize Parcelable types in same compilation unit.
- [`KT-19742`](https://youtrack.jetbrains.com/issue/KT-19742) [Android extensions] Calling clearFindViewByIdCache causes NPE
- [`KT-19749`](https://youtrack.jetbrains.com/issue/KT-19749) Android extensions + Parcelable: NoSuchMethodError on attempt to pack into parcel a serializable object
- [`KT-20026`](https://youtrack.jetbrains.com/issue/KT-20026) Parcelize overrides describeContents despite being already implemented.
- [`KT-20027`](https://youtrack.jetbrains.com/issue/KT-20027) Parcelize uses wrong classloader when reading parcelable type.
- [`KT-20029`](https://youtrack.jetbrains.com/issue/KT-20029) Parcelize should not directly reference parcel methods on types outside compilation unit
- [`KT-20032`](https://youtrack.jetbrains.com/issue/KT-20032) Parcelize does not respect type nullability in case of Parcelize parcelables

### Tools. Gradle

- [`KT-3463`](https://youtrack.jetbrains.com/issue/KT-3463) Gradle plugin ignores kotlin compile options changes
- [`KT-16299`](https://youtrack.jetbrains.com/issue/KT-16299) Gradle build does not recompile annotated classes on changing compiler's plugins configuration
- [`KT-16764`](https://youtrack.jetbrains.com/issue/KT-16764) Kotlin Gradle plugin should replicate task dependencies of Java source directories
- [`KT-17564`](https://youtrack.jetbrains.com/issue/KT-17564) Applying Kotlin's Gradle plugin results in src/main/java being listed twice in sourceSets.main.allSource
- [`KT-17674`](https://youtrack.jetbrains.com/issue/KT-17674) Test code is not compiled incrementally when main is changed
- [`KT-18765`](https://youtrack.jetbrains.com/issue/KT-18765) Move incremental compilation message from Gradle's warning to info logging level
- [`KT-20036`](https://youtrack.jetbrains.com/issue/KT-20036) Gradle tasks up-to-date-ness

### Tools. J2K

- [`KT-19565`](https://youtrack.jetbrains.com/issue/KT-19565) Java code using Iterator#remove converted to red code
- [`KT-19651`](https://youtrack.jetbrains.com/issue/KT-19651) Java class with static-only methods can contain 'protected' members

### Tools. JPS

- [`KT-20082`](https://youtrack.jetbrains.com/issue/KT-20082) Java 9: incremental build reports bogus error for reference to Kotlin source
- [`KT-20671`](https://youtrack.jetbrains.com/issue/KT-20671) Kotlin plugin compiler exception when compiling under JDK9

### Tools. Maven

- [`KT-20064`](https://youtrack.jetbrains.com/issue/KT-20064) Maven + Java 9: compile task warns about module-info in the output path
- [`KT-20400`](https://youtrack.jetbrains.com/issue/KT-20400) Do not output module name, version and related information by default in Maven builds

### Tools. REPL

- [`KT-20167`](https://youtrack.jetbrains.com/issue/KT-20167) JDK 9 `unresolved supertypes: Object` when working with Kotlin Scripting API

### Tools. kapt

- [`KT-17923`](https://youtrack.jetbrains.com/issue/KT-17923) Reference to Dagger generated class is highlighted red
- [`KT-18923`](https://youtrack.jetbrains.com/issue/KT-18923) Kapt: Do not use the Kotlin error message collector to issue errors from kapt
- [`KT-19097`](https://youtrack.jetbrains.com/issue/KT-19097) Request: Decent support of `kapt.kotlin.generated` on Intellij/Android Studio
- [`KT-20877`](https://youtrack.jetbrains.com/issue/KT-20877) Butterknife: UninitializedPropertyAccessException: "lateinit property has not been initialized" for field annotated with `@BindView`.

## 1.1.50

### Android

- [`KT-14800`](https://youtrack.jetbrains.com/issue/KT-14800) Kotlin Lint: `@SuppressLint` annotation on local variable is ignored
- [`KT-16600`](https://youtrack.jetbrains.com/issue/KT-16600) False positive "For methods, permission annotation should specify one of `konstue`, `anyOf` or `allOf`"
- [`KT-16834`](https://youtrack.jetbrains.com/issue/KT-16834) Android Lint: Bogus warning on @setparam:StringRes
- [`KT-17785`](https://youtrack.jetbrains.com/issue/KT-17785) Kotlin Lint: "Incorrect support annotation usage" does not pick the konstue of const konst
- [`KT-18837`](https://youtrack.jetbrains.com/issue/KT-18837) Android Lint: Collection.removeIf is not flagged when used on RealmList
- [`KT-18893`](https://youtrack.jetbrains.com/issue/KT-18893) Android support annotations (ColorInt, etc) cannot be used on properties: "does not apply for type void"
- [`KT-18997`](https://youtrack.jetbrains.com/issue/KT-18997) KLint: False positive "Could not find property setter method setLevel on java.lang.Object" if using elvis with return on RHS
- [`KT-19671`](https://youtrack.jetbrains.com/issue/KT-19671) UAST: Parameter annotations not provided for konst parameters

### Compiler

#### Performance Improvements

- [`KT-17963`](https://youtrack.jetbrains.com/issue/KT-17963) Unnecessary boxing in case of primitive comparison to object
- [`KT-18589`](https://youtrack.jetbrains.com/issue/KT-18589) 'Equality check can be used instead of elvis' produces code that causes boxing
- [`KT-18693`](https://youtrack.jetbrains.com/issue/KT-18693) Optimize in-expression with optimizable range in RHS
- [`KT-18721`](https://youtrack.jetbrains.com/issue/KT-18721) Improve code generation for if-in-primitive-literal expression ('if (expr in low .. high)') 
- [`KT-18818`](https://youtrack.jetbrains.com/issue/KT-18818) Optimize null cases in `when` statement to avoid Intrinsics usage
- [`KT-18834`](https://youtrack.jetbrains.com/issue/KT-18834) Do not create ranges for 'x in low..high' where type of x doesn't match range element type
- [`KT-19029`](https://youtrack.jetbrains.com/issue/KT-19029) Use specialized equality implementations for 'when'
- [`KT-19149`](https://youtrack.jetbrains.com/issue/KT-19149) Use 'for-in-until' loop in intrinsic array constructors
- [`KT-19252`](https://youtrack.jetbrains.com/issue/KT-19252) Use 'for-in-until' loop for 'for-in-rangeTo' loops with constant upper bounds when possible
- [`KT-19256`](https://youtrack.jetbrains.com/issue/KT-19256) Destructuring assignment generates redundant code for temporary variable nullification
- [`KT-19457`](https://youtrack.jetbrains.com/issue/KT-19457) Extremely slow analysis for file with deeply nested lambdas
#### Fixes

- [`KT-10754`](https://youtrack.jetbrains.com/issue/KT-10754) Bogus unresolved extension function
- [`KT-11739`](https://youtrack.jetbrains.com/issue/KT-11739) Incorrect error message on getValue operator with KProperty<Something> parameter
- [`KT-11834`](https://youtrack.jetbrains.com/issue/KT-11834) INAPPLICABLE_LATEINIT_MODIFIER is confusing for a generic type parameter with nullable (default) upper bound
- [`KT-11963`](https://youtrack.jetbrains.com/issue/KT-11963) Exception: recursive call in a lazy konstue under LockBasedStorageManager
- [`KT-12737`](https://youtrack.jetbrains.com/issue/KT-12737) Confusing error message when calling extension function with an implicit receiver, passing konstue parameter of wrong type
- [`KT-12767`](https://youtrack.jetbrains.com/issue/KT-12767) Too much unnecessary information in "N type arguments expected" error message
- [`KT-12796`](https://youtrack.jetbrains.com/issue/KT-12796) IllegalArgumentException on referencing inner class constructor on an outer class instance
- [`KT-12899`](https://youtrack.jetbrains.com/issue/KT-12899) Platform null escapes if passed as an extension receiver to an inline function
- [`KT-13665`](https://youtrack.jetbrains.com/issue/KT-13665) Generic componentN() functions should provide better diagnostics when type cannot be inferred
- [`KT-16223`](https://youtrack.jetbrains.com/issue/KT-16223) Confusing diagnostic for local inline functions
- [`KT-16246`](https://youtrack.jetbrains.com/issue/KT-16246) CompilationException caused by intersection type overload and wrong type parameter
- [`KT-16746`](https://youtrack.jetbrains.com/issue/KT-16746) DslMarker doesn't work with typealiases
- [`KT-17444`](https://youtrack.jetbrains.com/issue/KT-17444) Accessors generated for private file functions should respect @JvmName
- [`KT-17464`](https://youtrack.jetbrains.com/issue/KT-17464) Calling super constructor with generic function call in arguments fails at runtime
- [`KT-17725`](https://youtrack.jetbrains.com/issue/KT-17725) java.lang.VerifyError when both dispatch receiver and extension receiver have smart casts
- [`KT-17745`](https://youtrack.jetbrains.com/issue/KT-17745) Unfriendly error message on creating an instance of interface via typealias
- [`KT-17748`](https://youtrack.jetbrains.com/issue/KT-17748) Equality for class literals of primitive types is not preserved by reification
- [`KT-17879`](https://youtrack.jetbrains.com/issue/KT-17879) Comparing T::class from a reified generic with a Class<*> and KClass<*> variable in when statement is broken
- [`KT-18356`](https://youtrack.jetbrains.com/issue/KT-18356) Argument reordering in super class constructor call for anonymous object fails with VerifyError
- [`KT-18819`](https://youtrack.jetbrains.com/issue/KT-18819) JVM BE treats 'if (a in low .. high)' as 'if (a >= low && a <= high)', so 'high' can be non-ekonstuated
- [`KT-18855`](https://youtrack.jetbrains.com/issue/KT-18855) Convert "Remove at from annotation argument" inspection into compiler error & quick-fix
- [`KT-18858`](https://youtrack.jetbrains.com/issue/KT-18858) Exception within typealias expansion with dynamic used as one of type arguments
- [`KT-18902`](https://youtrack.jetbrains.com/issue/KT-18902) NullPointerException when using provideDelegate with properties of the base class at runtime
- [`KT-18940`](https://youtrack.jetbrains.com/issue/KT-18940) REPEATED_ANNOTATION is reported on wrong location for typealias arguments
- [`KT-18944`](https://youtrack.jetbrains.com/issue/KT-18944) Type annotations are lost for dynamic type
- [`KT-18966`](https://youtrack.jetbrains.com/issue/KT-18966) Report full package FQ name in compilation errors related to visibility
- [`KT-18971`](https://youtrack.jetbrains.com/issue/KT-18971) Missing non-null assertion for platform type passed as a receiver to the member extension function
- [`KT-18982`](https://youtrack.jetbrains.com/issue/KT-18982) NoSuchFieldError on access to imported object property from the declaring object itself
- [`KT-18985`](https://youtrack.jetbrains.com/issue/KT-18985) Too large highlighting range for UNCHECKED_CAST
- [`KT-19058`](https://youtrack.jetbrains.com/issue/KT-19058) VerifyError: no CHECKAST on dispatch receiver of the synthetic property defined in Java interface
- [`KT-19100`](https://youtrack.jetbrains.com/issue/KT-19100) VerifyError: missing CHECKCAST on extension receiver of the extension property
- [`KT-19115`](https://youtrack.jetbrains.com/issue/KT-19115) Report warnings on usages of JSR 305-annotated declarations which rely on incorrect or missing nullability information
- [`KT-19128`](https://youtrack.jetbrains.com/issue/KT-19128) java.lang.VerifyError with smart cast to String from Any
- [`KT-19180`](https://youtrack.jetbrains.com/issue/KT-19180) Bad SAM conversion of Java interface causing ClassCastException: [...] cannot be cast to kotlin.jvm.functions.Function1
- [`KT-19205`](https://youtrack.jetbrains.com/issue/KT-19205) Poor diagnostic message for deprecated class referenced through typealias
- [`KT-19367`](https://youtrack.jetbrains.com/issue/KT-19367) NSFE if property with name matching companion object property name is referenced within lambda
- [`KT-19434`](https://youtrack.jetbrains.com/issue/KT-19434) Object inheriting generic class with a reified type parameter looses method annotations
- [`KT-19475`](https://youtrack.jetbrains.com/issue/KT-19475) AnalyserException in case of combination of `while (true)` + stack-spilling (coroutines/try-catch expressions)
- [`KT-19528`](https://youtrack.jetbrains.com/issue/KT-19528) Compiler exception on inline suspend function inside a generic class
- [`KT-19575`](https://youtrack.jetbrains.com/issue/KT-19575) Deprecated typealias is not marked as such in access to companion object
- [`KT-19601`](https://youtrack.jetbrains.com/issue/KT-19601) UPPER_BOUND_VIOLATED reported on type alias expansion in a recursive upper bound on a type parameter
- [`KT-19814`](https://youtrack.jetbrains.com/issue/KT-19814) Runtime annotations for open suspend function are not generated correctly
- [`KT-19892`](https://youtrack.jetbrains.com/issue/KT-19892) Overriding remove method on inheritance from TreeSet<Int>
- [`KT-19910`](https://youtrack.jetbrains.com/issue/KT-19910) Nullability assertions removed when inlining an anonymous object in crossinline lambda
- [`KT-19985`](https://youtrack.jetbrains.com/issue/KT-19985) JSR 305: nullability qualifier of Java function return type detected incorrectly in case of using annotation nickname

### IDE

#### New Features

- [`KT-6676`](https://youtrack.jetbrains.com/issue/KT-6676) Show enum constant ordinal in quick doc like in Java
- [`KT-12246`](https://youtrack.jetbrains.com/issue/KT-12246) Kotlin source files are not highlighted in Gradle build output in IntelliJ
#### Performance Improvements

- [`KT-19670`](https://youtrack.jetbrains.com/issue/KT-19670) When computing argument hints, don't resolve call if none of the arguments are unclear expressions
#### Fixes

- [`KT-9288`](https://youtrack.jetbrains.com/issue/KT-9288) Call hierarchy ends on function call inside local konst initializer expression
- [`KT-9669`](https://youtrack.jetbrains.com/issue/KT-9669) Join Lines should add semicolon when joining statements into the same line
- [`KT-14346`](https://youtrack.jetbrains.com/issue/KT-14346) IllegalArgumentException on attempt to call Show Hierarchy view on lambda
- [`KT-14428`](https://youtrack.jetbrains.com/issue/KT-14428) AssertionError in KotlinCallerMethodsTreeStructure.<init> on attempt to call Hierarchy view
- [`KT-19466`](https://youtrack.jetbrains.com/issue/KT-19466) Kotlin based Gradle build not recognized when added as a module
- [`KT-18083`](https://youtrack.jetbrains.com/issue/KT-18083) IDEA: Support extension main function
- [`KT-18863`](https://youtrack.jetbrains.com/issue/KT-18863) Formatter should add space after opening brace in a single-line enum declaration
- [`KT-19024`](https://youtrack.jetbrains.com/issue/KT-19024) build.gradle.kts is not supported as project
- [`KT-19124`](https://youtrack.jetbrains.com/issue/KT-19124) Creating source file with directory/package throws AE: "Write access is allowed inside write-action only" at NewKotlinFileAction$Companion.findOrCreateTarget()
- [`KT-19154`](https://youtrack.jetbrains.com/issue/KT-19154) Completion and auto-import does not suggest companion object members when inside an extension function
- [`KT-19202`](https://youtrack.jetbrains.com/issue/KT-19202) Applying 'ReplaceWith' fix in type alias can change program behaviour
- [`KT-19209`](https://youtrack.jetbrains.com/issue/KT-19209) "Stub and PSI element type mismatch" in when receiver type is annotated with @receiver
- [`KT-19277`](https://youtrack.jetbrains.com/issue/KT-19277) Optimize imports on the fly should not work in test data files
- [`KT-19278`](https://youtrack.jetbrains.com/issue/KT-19278) Optimize imports on the fly should not remove incomplete import while it's being typed
- [`KT-19322`](https://youtrack.jetbrains.com/issue/KT-19322) Script editor: Move Statement Down/Up can't move one out of top level lambda
- [`KT-19451`](https://youtrack.jetbrains.com/issue/KT-19451) "Unresolved reference" with Kotlin Android Extensions when layout defines the Android namespace as something other than "android"
- [`KT-19492`](https://youtrack.jetbrains.com/issue/KT-19492) Java 9: references from unnamed module to not exported classes of named module are compiled, but red in the editor
- [`KT-19493`](https://youtrack.jetbrains.com/issue/KT-19493) Java 9: references from named module to classes of unnamed module are not compiled, but green in the editor
- [`KT-19843`](https://youtrack.jetbrains.com/issue/KT-19843) Performance warning: LineMarker is supposed to be registered for leaf elements only
- [`KT-19889`](https://youtrack.jetbrains.com/issue/KT-19889) KotlinGradleModel : Unsupported major.minor version 52.0
- [`KT-19885`](https://youtrack.jetbrains.com/issue/KT-19885) 200% CPU for some time on Kotlin sources (PackagePartClassUtils.hasTopLevelCallables())
- [`KT-19901`](https://youtrack.jetbrains.com/issue/KT-19901) KotlinLanguageInjector#getLanguagesToInject can cancel any progress in which it was invoked
- [`KT-19903`](https://youtrack.jetbrains.com/issue/KT-19903) Copy Reference works incorrectly for const konst
- [`KT-20153`](https://youtrack.jetbrains.com/issue/KT-20153) Kotlin facet: Java 9 `-Xadd-modules` setting produces more and more identical sub-elements of `<additionalJavaModules>` in .iml file

### IDE. Completion

- [`KT-8848`](https://youtrack.jetbrains.com/issue/KT-8848) Code completion does not support import aliases
- [`KT-18040`](https://youtrack.jetbrains.com/issue/KT-18040) There is no auto-popup competion after typing "$x." anymore
- [`KT-19015`](https://youtrack.jetbrains.com/issue/KT-19015) Smart completion: parameter list completion is not available when some of parameters are already written

### IDE. Debugger

- [`KT-19429`](https://youtrack.jetbrains.com/issue/KT-19429) Breakpoint appears in random place during debug

### IDE. Inspections and Intentions

#### New Features

- [`KT-4748`](https://youtrack.jetbrains.com/issue/KT-4748) Remove double negation for boolean expressions intention + inspection
- [`KT-5878`](https://youtrack.jetbrains.com/issue/KT-5878) Quickfix for "variable initializer is redundant" (VARIABLE_WITH_REDUNDANT_INITIALIZER)
- [`KT-11991`](https://youtrack.jetbrains.com/issue/KT-11991) Kotlin should have an inspection to suggest the simplified format for a no argument lambda
- [`KT-12195`](https://youtrack.jetbrains.com/issue/KT-12195) Quickfix @JvmStatic on main() method in an object
- [`KT-12233`](https://youtrack.jetbrains.com/issue/KT-12233) "Package naming convention" inspection could show warning in .kt sources
- [`KT-12504`](https://youtrack.jetbrains.com/issue/KT-12504) Intention to make open class with only private constructors sealed
- [`KT-12523`](https://youtrack.jetbrains.com/issue/KT-12523) Quick-fix to remove `when` with only `else`
- [`KT-12613`](https://youtrack.jetbrains.com/issue/KT-12613) "Make abstract" on member of open or final class should make abstract both member and class
- [`KT-16033`](https://youtrack.jetbrains.com/issue/KT-16033) Automatically static import the enum konstue name when "Add remaining branches" on an enum from another class/file
- [`KT-16404`](https://youtrack.jetbrains.com/issue/KT-16404) Create from usage should allow generating nested classes
- [`KT-17322`](https://youtrack.jetbrains.com/issue/KT-17322) Intentions to generate a getter and a setter for a property
- [`KT-17888`](https://youtrack.jetbrains.com/issue/KT-17888) Inspection to warn about suspicious combination of == and ===
- [`KT-18826`](https://youtrack.jetbrains.com/issue/KT-18826) INAPPLICABLE_LATEINIT_MODIFIER should have a quickfix to remove initializer
- [`KT-18965`](https://youtrack.jetbrains.com/issue/KT-18965) Add quick-fix for USELESS_IS_CHECK
- [`KT-19126`](https://youtrack.jetbrains.com/issue/KT-19126) Add quickfix for 'Property initializes are not allowed in interfaces'
- [`KT-19282`](https://youtrack.jetbrains.com/issue/KT-19282) Support "flip equals" intention for String.equals extension from stdlib
- [`KT-19428`](https://youtrack.jetbrains.com/issue/KT-19428) Add inspection for redundant overrides that only call the super method
- [`KT-19514`](https://youtrack.jetbrains.com/issue/KT-19514) Redundant getter / setter inspection
#### Fixes

- [`KT-13985`](https://youtrack.jetbrains.com/issue/KT-13985) "Add remaining branches" action does not use back-ticks correctly
- [`KT-15422`](https://youtrack.jetbrains.com/issue/KT-15422) Reduce irrelevant reporting of Destructure inspection
- [`KT-17480`](https://youtrack.jetbrains.com/issue/KT-17480) Create from usage in expression body of override function should take base type into account
- [`KT-18482`](https://youtrack.jetbrains.com/issue/KT-18482) "Move lambda argument to parenthesis" action generate uncompilable code
- [`KT-18665`](https://youtrack.jetbrains.com/issue/KT-18665) "Use destructuring declaration" must not be suggested for invisible properties
- [`KT-18666`](https://youtrack.jetbrains.com/issue/KT-18666) "Use destructuring declaration" should not be reported on a variable used in destructuring declaration only
- [`KT-18978`](https://youtrack.jetbrains.com/issue/KT-18978) Intention Move to class body generates incorrect code for vararg konst/var
- [`KT-19006`](https://youtrack.jetbrains.com/issue/KT-19006) Inspection message "Equality check can be used instead of elvis" is slightly confusing
- [`KT-19011`](https://youtrack.jetbrains.com/issue/KT-19011) Unnecessary import for companion object property with extension function type is automatically inserted
- [`KT-19299`](https://youtrack.jetbrains.com/issue/KT-19299) Quickfix to correct overriding function signature keeps java NotNull annotations
- [`KT-19614`](https://youtrack.jetbrains.com/issue/KT-19614) Quickfix for INVISIBLE_MEMBER doesn't offer to make member protected if referenced from subclass
- [`KT-19666`](https://youtrack.jetbrains.com/issue/KT-19666) ClassCastException in IfThenToElvisIntention
- [`KT-19704`](https://youtrack.jetbrains.com/issue/KT-19704) Don't remove braces in redundant cascade if
- [`KT-19811`](https://youtrack.jetbrains.com/issue/KT-19811) Internal member incorrectly highlighted as unused
- [`KT-19926`](https://youtrack.jetbrains.com/issue/KT-19926) Naming convention inspections: pattern is konstidated while edited, PSE at Pattern.error()
- [`KT-19927`](https://youtrack.jetbrains.com/issue/KT-19927) "Package naming convention" inspection checks FQN, but default pattern looks like for simple name

### IDE. Refactorings

- [`KT-17266`](https://youtrack.jetbrains.com/issue/KT-17266) Refactor / Inline Function: reference to member of class containing extension function is inlined wrong
- [`KT-17776`](https://youtrack.jetbrains.com/issue/KT-17776) Inline method of inner class adds 'this' for methods from enclosing class
- [`KT-19161`](https://youtrack.jetbrains.com/issue/KT-19161) Safe delete conflicts are shown incorrectly for local declarations

### JavaScript

#### Performance Improvements

- [`KT-18329`](https://youtrack.jetbrains.com/issue/KT-18329) JS: for loop implementation depends on parentheses
#### Fixes

- [`KT-12970`](https://youtrack.jetbrains.com/issue/KT-12970) Empty block expression result is 'undefined' (expected: 'kotlin.Unit')
- [`KT-13930`](https://youtrack.jetbrains.com/issue/KT-13930) Safe call for a function returning 'Unit' result is 'undefined' or 'null' (instead of 'kotlin.Unit' or 'null')
- [`KT-13932`](https://youtrack.jetbrains.com/issue/KT-13932) 'kotlin.Unit' is not materialized in some functions returning supertype of 'Unit' ('undefined' returned instead)
- [`KT-16408`](https://youtrack.jetbrains.com/issue/KT-16408) JS: Inliner loses imported konstues when extending a class from another module
- [`KT-17014`](https://youtrack.jetbrains.com/issue/KT-17014) Different results in JVM and JavaScript on Unit-returning functions
- [`KT-17915`](https://youtrack.jetbrains.com/issue/KT-17915) JS: 'kotlin.Unit' is not materialized as result of try-catch block expression with empty catch
- [`KT-18166`](https://youtrack.jetbrains.com/issue/KT-18166) JS: Delegated property named with non-identifier symbols can crash in runtime.
- [`KT-18176`](https://youtrack.jetbrains.com/issue/KT-18176) JS: dynamic type should not allow methods and properties with incorrect identifier symbols
- [`KT-18216`](https://youtrack.jetbrains.com/issue/KT-18216) JS: Unit-returning expression used in loop can cause wrong behavior
- [`KT-18793`](https://youtrack.jetbrains.com/issue/KT-18793) Kotlin Javascript compiler null handling generates if-else block where else is always taken
- [`KT-19108`](https://youtrack.jetbrains.com/issue/KT-19108) JS: Inconsistent behaviour from JVM code when modifying variable whilst calling run on it
- [`KT-19495`](https://youtrack.jetbrains.com/issue/KT-19495) JS: Wrong compilation of nested conditions with if- and when-clauses
- [`KT-19540`](https://youtrack.jetbrains.com/issue/KT-19540) JS: prohibit to use illegal symbols on call site
- [`KT-19542`](https://youtrack.jetbrains.com/issue/KT-19542) JS: delegate field should have unique name otherwise it can be accidentally overwritten 
- [`KT-19712`](https://youtrack.jetbrains.com/issue/KT-19712) KotlinJS - providing default konstue of lambda-argument produces inkonstid js-code
- [`KT-19793`](https://youtrack.jetbrains.com/issue/KT-19793) build-crash with external varargs (Javascript)
- [`KT-19821`](https://youtrack.jetbrains.com/issue/KT-19821) JS remap sourcemaps in DCE
- [`KT-19891`](https://youtrack.jetbrains.com/issue/KT-19891) Runtime crash with inline function with reified type parameter and object expression: "T_0 is not defined" (JavaScript)
- [`KT-20005`](https://youtrack.jetbrains.com/issue/KT-20005) Inkonstid source map with option sourceMapEmbedSources = "always" 

### Libraries

- [`KT-19133`](https://youtrack.jetbrains.com/issue/KT-19133) Specialize `any` and `none` for Collection
- [`KT-18267`](https://youtrack.jetbrains.com/issue/KT-18267) Deprecate CharSequence.size extension function on the JS side
- [`KT-18992`](https://youtrack.jetbrains.com/issue/KT-18992) JS: Missing MutableMap.iterator()
- [`KT-19881`](https://youtrack.jetbrains.com/issue/KT-19881) Expand doc comment of @PublishedApi

### Tools. CLI

- [`KT-18859`](https://youtrack.jetbrains.com/issue/KT-18859) Strange error message when kotlin-embeddable-compiler is run without explicit -kotlin-home
- [`KT-19287`](https://youtrack.jetbrains.com/issue/KT-19287) Common module compilation: K2MetadataCompiler ignores coroutines state

### Tools. Gradle

- [`KT-17150`](https://youtrack.jetbrains.com/issue/KT-17150) Support 'packagePrefix' option in Gradle plugin
- [`KT-19956`](https://youtrack.jetbrains.com/issue/KT-19956) Support incremental compilation to JS in Gradle
- [`KT-13918`](https://youtrack.jetbrains.com/issue/KT-13918) Cannot access internal classes/methods in androidTest source set in an Android library module
- [`KT-17355`](https://youtrack.jetbrains.com/issue/KT-17355) Use `archivesBaseName` instead of `project.name` for module names, get rid of `_main` for `main` source set
- [`KT-18183`](https://youtrack.jetbrains.com/issue/KT-18183) Kotlin gradle plugin uses compile task output as "friends directory"
- [`KT-19248`](https://youtrack.jetbrains.com/issue/KT-19248) Documentation suggested way to enable coroutines (gradle) doesn't work
- [`KT-19397`](https://youtrack.jetbrains.com/issue/KT-19397) local.properties file not closed by KotlinProperties.kt

### Tools. Incremental Compile

- [`KT-19580`](https://youtrack.jetbrains.com/issue/KT-19580) IC does not detect  non-nested sealed class addition

### Tools. J2K

- [`KT-10375`](https://youtrack.jetbrains.com/issue/KT-10375) 0xFFFFFFFFFFFFFFFFL conversion issue
- [`KT-13552`](https://youtrack.jetbrains.com/issue/KT-13552) switch-to-when conversion creates broken code
- [`KT-17379`](https://youtrack.jetbrains.com/issue/KT-17379) Converting multiline expressions creates dangling operations
- [`KT-18232`](https://youtrack.jetbrains.com/issue/KT-18232) Kotlin code converter misses annotations
- [`KT-18786`](https://youtrack.jetbrains.com/issue/KT-18786) Convert Kotlin to Java generates error: Variable cannot be initialized before declaration
- [`KT-19523`](https://youtrack.jetbrains.com/issue/KT-19523) J2K produce inkonstid code when convert some numbers

### Tools. JPS

- [`KT-17397`](https://youtrack.jetbrains.com/issue/KT-17397) Kotlin JPS Builder can mark dirty files already compiled in round
- [`KT-19176`](https://youtrack.jetbrains.com/issue/KT-19176) Java 9: JPS build fails for Kotlin source referring exported Kotlin class from another module: "unresolved supertypes: kotlin.Any"
- [`KT-19833`](https://youtrack.jetbrains.com/issue/KT-19833) Cannot access class/superclass from SDK on compilation of JDK 9 module together with non-9 module

### Tools. REPL

- [`KT-11369`](https://youtrack.jetbrains.com/issue/KT-11369) REPL: Ctrl-C should interrupt the input, Ctrl-D should quit

### Tools. kapt

- [`KT-19996`](https://youtrack.jetbrains.com/issue/KT-19996) Error with 'kotlin-kapt' plugin and dagger2, clean project required

## 1.1.4-3

- [`KT-18062`](https://youtrack.jetbrains.com/issue/KT-18062) SamWithReceiver compiler plugin not used by IntelliJ for .kt files 
- [`KT-18497`](https://youtrack.jetbrains.com/issue/KT-18497) Gradle Kotlin Plugin does not work with the gradle java-library plugin 
- [`KT-19276`](https://youtrack.jetbrains.com/issue/KT-19276) Console spam when opening idea-community project in debug IDEA 
- [`KT-19433`](https://youtrack.jetbrains.com/issue/KT-19433) [Coroutines + Kapt3] Assertion failed in ClassClsStubBuilder.createNestedClassStub 
- [`KT-19680`](https://youtrack.jetbrains.com/issue/KT-19680) kapt3 & Parcelize: Compilation error 
- [`KT-19687`](https://youtrack.jetbrains.com/issue/KT-19687) Kotlin 1.1.4 noarg plugin breaks with sealed classes
- [`KT-19700`](https://youtrack.jetbrains.com/issue/KT-19700) Kapt error after updating to 1.1.4 - stub adds type parameters where there are none
- [`KT-19713`](https://youtrack.jetbrains.com/issue/KT-19713) Mocking of final named suspend methods with mockito fails
- [`KT-19729`](https://youtrack.jetbrains.com/issue/KT-19729) kapt3: not always including argument to @javax.inject.Named in generated stubs
- [`KT-19759`](https://youtrack.jetbrains.com/issue/KT-19759) "Convert to expression body" is not shown in 162 / AS23 branches for multi-liners 
- [`KT-19767`](https://youtrack.jetbrains.com/issue/KT-19767) NPE caused by Map<String, Boolean>?.get 
- [`KT-19769`](https://youtrack.jetbrains.com/issue/KT-19769) PerModulePackageCacheService calls getOrderEntriesForFile() for every file, even those that can't affect Kotlin resolve 
- [`KT-19774`](https://youtrack.jetbrains.com/issue/KT-19774) Provide an opt-out flag for separate classes directories (Gradle 4.0+) 
- [`KT-19847`](https://youtrack.jetbrains.com/issue/KT-19847) if an imported library already exists it should be redetected during gradle import 

## 1.1.4-2

- [`KT-19679`](https://youtrack.jetbrains.com/issue/KT-19679) CompilationException: Couldn't inline method call 'methodName' into... 
- [`KT-19690`](https://youtrack.jetbrains.com/issue/KT-19690) Lazy field in interface default method leads to ClassFormatError 
- [`KT-19716`](https://youtrack.jetbrains.com/issue/KT-19716) Quickdoc `Ctrl+Q` broken while browsing code completion list `Ctrl-Space`
- [`KT-19717`](https://youtrack.jetbrains.com/issue/KT-19717) Library kind incorrectly detected for vertx-web in Kotlin project 
- [`KT-19723`](https://youtrack.jetbrains.com/issue/KT-19723) "Insufficient maximum stack size" during compilation 

## 1.1.4

### Android

#### New Features

- [`KT-11048`](https://youtrack.jetbrains.com/issue/KT-11048) Android Extensions: cannot ekonstuate expression containing generated properties
#### Performance Improvements

- [`KT-10542`](https://youtrack.jetbrains.com/issue/KT-10542) Android Extensions: No cache for Views
- [`KT-18250`](https://youtrack.jetbrains.com/issue/KT-18250) Android Extensions: Allow to use SparseArray as a View cache
#### Fixes

- [`KT-11051`](https://youtrack.jetbrains.com/issue/KT-11051) Android Extensions: completion of generated properties is unclear for ambiguous ids
- [`KT-14086`](https://youtrack.jetbrains.com/issue/KT-14086) Android-extensions not generated using flavors dimension
- [`KT-14912`](https://youtrack.jetbrains.com/issue/KT-14912) Lint: "Code contains STOPSHIP marker" ignores suppress annotation
- [`KT-15164`](https://youtrack.jetbrains.com/issue/KT-15164) Kotlin Lint: problems in delegate expression are not reported
- [`KT-16934`](https://youtrack.jetbrains.com/issue/KT-16934) Android Extensions fails to compile when importing synthetic properties for layouts in other modules
- [`KT-17641`](https://youtrack.jetbrains.com/issue/KT-17641) Problem with Kotlin Android Extensions and Gradle syntax
- [`KT-17783`](https://youtrack.jetbrains.com/issue/KT-17783) Kotlin Lint: quick fixes to add inapplicable @RequiresApi and @SuppressLint make code incompilable
- [`KT-17786`](https://youtrack.jetbrains.com/issue/KT-17786) Kotlin Lint: "Surround with if()" quick fix is not suggested for single expression `get()`
- [`KT-17787`](https://youtrack.jetbrains.com/issue/KT-17787) Kotlin Lint: "Add @TargetApi" quick fix is not suggested for top level property accessor
- [`KT-17788`](https://youtrack.jetbrains.com/issue/KT-17788) Kotlin Lint: "Surround with if()" quick fix corrupts code in case of destructuring declaration
- [`KT-17890`](https://youtrack.jetbrains.com/issue/KT-17890) [kotlin-android-extensions] Renaming layout file does not rename import
- [`KT-18012`](https://youtrack.jetbrains.com/issue/KT-18012) Kotlin Android Extensions generates `@NotNull` properties for views present in a configuration and potentially missing in another
- [`KT-18545`](https://youtrack.jetbrains.com/issue/KT-18545) Accessing to synthetic properties on smart casted Android components crashed compiler

### Compiler

#### New Features

- [`KT-10942`](https://youtrack.jetbrains.com/issue/KT-10942) Support meta-annotations from JSR 305 for nullability qualifiers
- [`KT-14187`](https://youtrack.jetbrains.com/issue/KT-14187) Redundant "is" check is not detected
- [`KT-16603`](https://youtrack.jetbrains.com/issue/KT-16603) Support `inline suspend` function
- [`KT-17585`](https://youtrack.jetbrains.com/issue/KT-17585) Generate state machine for named functions in their bodies
#### Performance Improvements

- [`KT-3098`](https://youtrack.jetbrains.com/issue/KT-3098) Generate efficient comparisons
- [`KT-6247`](https://youtrack.jetbrains.com/issue/KT-6247) Optimization for 'in' and '..'
- [`KT-7571`](https://youtrack.jetbrains.com/issue/KT-7571) Don't box Double instance to call hashCode on Java 8
- [`KT-9900`](https://youtrack.jetbrains.com/issue/KT-9900) Optimize range operations for 'until' extension from stdlib
- [`KT-11959`](https://youtrack.jetbrains.com/issue/KT-11959) Unnceessary boxing/unboxing due to Comparable.compareTo
- [`KT-12158`](https://youtrack.jetbrains.com/issue/KT-12158) Optimize away boxing when comparing nullable primitive type konstue to primitive konstue
- [`KT-13682`](https://youtrack.jetbrains.com/issue/KT-13682) Reuse StringBuilder for concatenation and string interpolation
- [`KT-14323`](https://youtrack.jetbrains.com/issue/KT-14323) IntelliJ lockup when using Apache Spark UDF
- [`KT-14375`](https://youtrack.jetbrains.com/issue/KT-14375) Kotlin compiler failure with spark when creating a flexible type for scala.Function22
- [`KT-15235`](https://youtrack.jetbrains.com/issue/KT-15235) Escaped characters in template strings are generating inefficient implementations
- [`KT-17280`](https://youtrack.jetbrains.com/issue/KT-17280) Inline constant expressions in string templates
- [`KT-17903`](https://youtrack.jetbrains.com/issue/KT-17903) Generate 'for-in-indices' as a precondition loop
- [`KT-18157`](https://youtrack.jetbrains.com/issue/KT-18157) Optimize out trivial INSTANCEOF checks
- [`KT-18162`](https://youtrack.jetbrains.com/issue/KT-18162) Do not check nullability assertions twice for effectively same konstue
- [`KT-18164`](https://youtrack.jetbrains.com/issue/KT-18164) Do not check nullability for konstues that have been already checked with !!
- [`KT-18478`](https://youtrack.jetbrains.com/issue/KT-18478) Unnecessary nullification of bound variables
- [`KT-18558`](https://youtrack.jetbrains.com/issue/KT-18558) Flatten nested string concatenation
- [`KT-18777`](https://youtrack.jetbrains.com/issue/KT-18777) Unnecessary boolean negation generated for 'if (expr !in range)'
#### Fixes

- [`KT-1809`](https://youtrack.jetbrains.com/issue/KT-1809) Confusing diagnostics when wrong number of type arguments are specified and there are several callee candiates
- [`KT-2007`](https://youtrack.jetbrains.com/issue/KT-2007) Improve diagnostics when + in not resolved on a pair of nullable ints
- [`KT-5066`](https://youtrack.jetbrains.com/issue/KT-5066) Bad diagnostic message for ABSTRACT_MEMBER_NOT_IMPLEMENTED for (companion) object
- [`KT-5511`](https://youtrack.jetbrains.com/issue/KT-5511) Inconsistent handling of inner enum
- [`KT-7773`](https://youtrack.jetbrains.com/issue/KT-7773) Disallow to explicitly extend Enum<E> class
- [`KT-7975`](https://youtrack.jetbrains.com/issue/KT-7975) Unclear error message when redundant type arguments supplied
- [`KT-8340`](https://youtrack.jetbrains.com/issue/KT-8340) vararg in a property setter must be an error
- [`KT-8612`](https://youtrack.jetbrains.com/issue/KT-8612) Incorrect error message for var extension property without getter or setter
- [`KT-8829`](https://youtrack.jetbrains.com/issue/KT-8829) Type parameter of a class is not resolved in the constructor parameter's default konstue
- [`KT-8845`](https://youtrack.jetbrains.com/issue/KT-8845) Bogus diagnostic on infix operation "in"
- [`KT-9282`](https://youtrack.jetbrains.com/issue/KT-9282) Improve diagnostic on overload resolution ambiguity when a nullable argument is passed to non-null parameter
- [`KT-10045`](https://youtrack.jetbrains.com/issue/KT-10045) Not specific enough compiler error message in case of trying to call overloaded private methods
- [`KT-10164`](https://youtrack.jetbrains.com/issue/KT-10164) Incorrect error message for external inline method
- [`KT-10248`](https://youtrack.jetbrains.com/issue/KT-10248) Smart casts: Misleading error on overloaded function call
- [`KT-10657`](https://youtrack.jetbrains.com/issue/KT-10657) Confusing diagnostic when trying to invoke konstue as a function
- [`KT-10839`](https://youtrack.jetbrains.com/issue/KT-10839) Weird diagnostics on callable reference of unresolved class
- [`KT-11119`](https://youtrack.jetbrains.com/issue/KT-11119) Confusing error message when overloaded method is called on nullable receiver
- [`KT-12408`](https://youtrack.jetbrains.com/issue/KT-12408) Generic information lost for override konstues
- [`KT-12551`](https://youtrack.jetbrains.com/issue/KT-12551) Report "unused expression" on unused bound double colon expressions
- [`KT-13749`](https://youtrack.jetbrains.com/issue/KT-13749) Error highlighting range for no 'override' modifier is bigger than needed
- [`KT-14598`](https://youtrack.jetbrains.com/issue/KT-14598) Do not report "member is final and cannot be overridden" when overriding something from final class
- [`KT-14633`](https://youtrack.jetbrains.com/issue/KT-14633) "If must have both main and else branches" diagnostic range is too high
- [`KT-14647`](https://youtrack.jetbrains.com/issue/KT-14647) Confusing error message "'@receiver:' annotations could be applied only to extension function or extension property declarations"
- [`KT-14927`](https://youtrack.jetbrains.com/issue/KT-14927) TCE in QualifiedExpressionResolver
- [`KT-15243`](https://youtrack.jetbrains.com/issue/KT-15243) Report deprecation on usages of type alias expanded to a deprecated class
- [`KT-15804`](https://youtrack.jetbrains.com/issue/KT-15804) Prohibit having duplicate parameter names in functional types
- [`KT-15810`](https://youtrack.jetbrains.com/issue/KT-15810) destructuring declarations don't work in scripts on the top level
- [`KT-15931`](https://youtrack.jetbrains.com/issue/KT-15931) IllegalStateException: ClassDescriptor of superType should not be null: T by a
- [`KT-16016`](https://youtrack.jetbrains.com/issue/KT-16016) Compiler failure with NO_EXPECTED_TYPE
- [`KT-16448`](https://youtrack.jetbrains.com/issue/KT-16448) Inline suspend functions with inlined suspend invocations are miscompiled (VerifyError, ClassNotFound)
- [`KT-16576`](https://youtrack.jetbrains.com/issue/KT-16576) Wrong code generated with skynet benchmark
- [`KT-17007`](https://youtrack.jetbrains.com/issue/KT-17007) Kotlin is not optimizing away unreachable code based on const konsts
- [`KT-17188`](https://youtrack.jetbrains.com/issue/KT-17188) Do not propose to specify constructor invocation for classes without an accessible constructor
- [`KT-17611`](https://youtrack.jetbrains.com/issue/KT-17611) Unnecessary "Name shadowed" warning on parameter of local function or local class member
- [`KT-17692`](https://youtrack.jetbrains.com/issue/KT-17692) NPE in compiler when calling KClass.java on function result of type Unit
- [`KT-17820`](https://youtrack.jetbrains.com/issue/KT-17820) False "useless cast" when target type is flexible
- [`KT-17972`](https://youtrack.jetbrains.com/issue/KT-17972) Anonymous class generated from lambda captures its outer and tries to set nonexistent this$0 field.
- [`KT-18029`](https://youtrack.jetbrains.com/issue/KT-18029) typealias not working in .kts files
- [`KT-18085`](https://youtrack.jetbrains.com/issue/KT-18085) Compilation Error:Kotlin: [Internal Error] kotlin.TypeCastException: null cannot be cast to non-null type com.intellij.psi.PsiElement
- [`KT-18115`](https://youtrack.jetbrains.com/issue/KT-18115) Generic inherited classes in different packages with coroutine causes java.lang.VerifyError: Bad local variable type
- [`KT-18189`](https://youtrack.jetbrains.com/issue/KT-18189) Incorrect generic signature generated for implementation methods overriding special built-ins
- [`KT-18234`](https://youtrack.jetbrains.com/issue/KT-18234) Top-level variables in script aren't local variables
- [`KT-18413`](https://youtrack.jetbrains.com/issue/KT-18413) Strange compiler error - probably incremental compiler
- [`KT-18486`](https://youtrack.jetbrains.com/issue/KT-18486) Superfluos generation of suspend function state-machine because of inner suspension of different coroutine
- [`KT-18598`](https://youtrack.jetbrains.com/issue/KT-18598) Report error on access to declarations from non-exported packages and from inaccessible modules on Java 9
- [`KT-18698`](https://youtrack.jetbrains.com/issue/KT-18698) java.lang.IllegalStateException: resolveToInstruction: incorrect index -1 for label L12 in subroutine
- [`KT-18702`](https://youtrack.jetbrains.com/issue/KT-18702) Proguard warning with Kotlin 1.2-M1
- [`KT-18728`](https://youtrack.jetbrains.com/issue/KT-18728) Integer method reference application fails with CompilationException: Back-end (JVM) Internal error
- [`KT-18845`](https://youtrack.jetbrains.com/issue/KT-18845) Exception on building gradle project with collection literals
- [`KT-18867`](https://youtrack.jetbrains.com/issue/KT-18867) Getting constant "VerifyError: Operand stack underflow" from Kotlin plugin
- [`KT-18916`](https://youtrack.jetbrains.com/issue/KT-18916) Strange bytecode generated for 'null' passed as SAM adapter for Java interface
- [`KT-18983`](https://youtrack.jetbrains.com/issue/KT-18983) Coroutines: miscompiled suspend for loop (local variables are not spilled around suspension points)
- [`KT-19175`](https://youtrack.jetbrains.com/issue/KT-19175) Compiler generates different bytecode when classes are compiled separately or together
- [`KT-19246`](https://youtrack.jetbrains.com/issue/KT-19246) Using generic inline function inside inline extension function throws java.lang.VerifyError: Bad return type
- [`KT-19419`](https://youtrack.jetbrains.com/issue/KT-19419) Support JSR 305 meta-annotations in libraries even when JSR 305 JAR is not on the classpath

### IDE

#### New Features

- [`KT-2638`](https://youtrack.jetbrains.com/issue/KT-2638) Inline property (with accessors) refactoring
- [`KT-7107`](https://youtrack.jetbrains.com/issue/KT-7107) Rename refactoring for labels
- [`KT-9818`](https://youtrack.jetbrains.com/issue/KT-9818) Code style for method expression bodies
- [`KT-11994`](https://youtrack.jetbrains.com/issue/KT-11994) Data flow analysis support for Kotlin in IntelliJ
- [`KT-14126`](https://youtrack.jetbrains.com/issue/KT-14126) Code style wrapping options for enum constants
- [`KT-14929`](https://youtrack.jetbrains.com/issue/KT-14929) Deprecated ReplaceWith for type aliases
- [`KT-14950`](https://youtrack.jetbrains.com/issue/KT-14950) Code Style: Wrapping and Braces / "Local variable annotations" setting could be supported
- [`KT-14965`](https://youtrack.jetbrains.com/issue/KT-14965) "Configure Kotlin in project" should support build.gradle.kts
- [`KT-15504`](https://youtrack.jetbrains.com/issue/KT-15504) Add code style options to limit number of blank lines
- [`KT-16558`](https://youtrack.jetbrains.com/issue/KT-16558) Code Style: Add Options for "Spaces Before Parentheses"
- [`KT-18113`](https://youtrack.jetbrains.com/issue/KT-18113) Add new line options to code style for method parameters
- [`KT-18605`](https://youtrack.jetbrains.com/issue/KT-18605) Option to not use continuation indent in chained calls
- [`KT-18607`](https://youtrack.jetbrains.com/issue/KT-18607) Options to put blank lines between 'when' branches
#### Performance Improvements

- [`KT-14606`](https://youtrack.jetbrains.com/issue/KT-14606) Code completion calculates decompiled text when building lookup elements for PSI from compiled classes
- [`KT-17751`](https://youtrack.jetbrains.com/issue/KT-17751) Kotlin slows down java inspections big time
- [`KT-17835`](https://youtrack.jetbrains.com/issue/KT-17835) 10s hang on IDEA project open
- [`KT-18842`](https://youtrack.jetbrains.com/issue/KT-18842) Very slow typing in certain files of Kotlin project
- [`KT-18921`](https://youtrack.jetbrains.com/issue/KT-18921) Configure library kind explicitly
#### Fixes

- [`KT-6610`](https://youtrack.jetbrains.com/issue/KT-6610) Language injection doesn't work with String Interpolation
- [`KT-8893`](https://youtrack.jetbrains.com/issue/KT-8893) Quick documentation shows type for top-level object-type elements, but "no name provided" for local ones
- [`KT-9359`](https://youtrack.jetbrains.com/issue/KT-9359) "Accidental override" error message does not mention class (type) names
- [`KT-10736`](https://youtrack.jetbrains.com/issue/KT-10736) Highlighting usages doesn't work for synthetic properties created by the Android Extensions
- [`KT-11980`](https://youtrack.jetbrains.com/issue/KT-11980) Spring: Generate Constructor, Setter Dependency in XML for Kotlin class: IOE at LightElement.add()
- [`KT-12123`](https://youtrack.jetbrains.com/issue/KT-12123) Formatter: always indent after newline in variable initialization
- [`KT-12910`](https://youtrack.jetbrains.com/issue/KT-12910) spring: create init-method/destroy-method from usage results in IOE
- [`KT-13072`](https://youtrack.jetbrains.com/issue/KT-13072) Kotlin struggles to index JDK 9 classes
- [`KT-13099`](https://youtrack.jetbrains.com/issue/KT-13099) formatting in angle brackets ignored and not fixed
- [`KT-14083`](https://youtrack.jetbrains.com/issue/KT-14083) Formatting of where clasuses
- [`KT-14271`](https://youtrack.jetbrains.com/issue/KT-14271) Value captured in closure doesn't always get highlighted
- [`KT-14561`](https://youtrack.jetbrains.com/issue/KT-14561) Use regular indent for the primary constructor parameters
- [`KT-14974`](https://youtrack.jetbrains.com/issue/KT-14974) "Find Usages" hangs in ExpressionsOfTypeProcessor
- [`KT-15093`](https://youtrack.jetbrains.com/issue/KT-15093) Navigation to library may not work if there's another module in same project that references same jar via a different library
- [`KT-15270`](https://youtrack.jetbrains.com/issue/KT-15270) Quickfix to migrate from @native***
- [`KT-16352`](https://youtrack.jetbrains.com/issue/KT-16352) Create from usage inserts extra space in first step
- [`KT-16725`](https://youtrack.jetbrains.com/issue/KT-16725) Formatter does not fix spaces before square brackets
- [`KT-16999`](https://youtrack.jetbrains.com/issue/KT-16999) "Parameter info" shows duplicates on toString
- [`KT-17357`](https://youtrack.jetbrains.com/issue/KT-17357) BuiltIns for module build with project LV settings, not with facet module settings
- [`KT-17394`](https://youtrack.jetbrains.com/issue/KT-17394) Core formatting is wrong for expression body properties
- [`KT-17759`](https://youtrack.jetbrains.com/issue/KT-17759) Breakpoints not working in JS
- [`KT-17771`](https://youtrack.jetbrains.com/issue/KT-17771) Kotlin IntelliJ plugin should resolve Gradle script classpath asynchronously
- [`KT-17818`](https://youtrack.jetbrains.com/issue/KT-17818) Formatting of long constructors is inconsistent with Kotlin code conventions
- [`KT-17849`](https://youtrack.jetbrains.com/issue/KT-17849) Automatically insert trimMargin() or trimIndent() on enter in multi-line strings
- [`KT-17855`](https://youtrack.jetbrains.com/issue/KT-17855) Main function is shown as unused
- [`KT-17894`](https://youtrack.jetbrains.com/issue/KT-17894) String `trimIndent` support inserts wrong indent in some cases
- [`KT-17942`](https://youtrack.jetbrains.com/issue/KT-17942) Enter in multiline string with injection doesn't add a proper indent
- [`KT-17956`](https://youtrack.jetbrains.com/issue/KT-17956) Type hints for properties that only consist of constructor calls don't add much konstue
- [`KT-18006`](https://youtrack.jetbrains.com/issue/KT-18006) Copying part of string literal with escape sequences converts this sequences to special characters
- [`KT-18030`](https://youtrack.jetbrains.com/issue/KT-18030) Parameters hints: `kotlin.arrayOf(elements)` should be on the blacklist by default
- [`KT-18059`](https://youtrack.jetbrains.com/issue/KT-18059) Kotlin Lint: False positive error "requires api level 24" for interface method with body
- [`KT-18149`](https://youtrack.jetbrains.com/issue/KT-18149) PIEAE "Element class CompositeElement of type REFERENCE_EXPRESSION (class KtNameReferenceExpressionElementType)" at PsiInkonstidElementAccessException.createByNode()
- [`KT-18151`](https://youtrack.jetbrains.com/issue/KT-18151) Do not import jdkHome from Gradle/Maven model
- [`KT-18158`](https://youtrack.jetbrains.com/issue/KT-18158) Expand selection should select the comment after expression getter on the same line
- [`KT-18186`](https://youtrack.jetbrains.com/issue/KT-18186) Create function from usage should infer expected return type
- [`KT-18221`](https://youtrack.jetbrains.com/issue/KT-18221) AE at org.jetbrains.kotlin.analyzer.ResolverForProjectImpl.descriptorForModule
- [`KT-18269`](https://youtrack.jetbrains.com/issue/KT-18269) Find Usages fails to find operator-style usages of `invoke()` defined as extension
- [`KT-18298`](https://youtrack.jetbrains.com/issue/KT-18298) spring: strange menu at "Navige to the spring bean" gutter
- [`KT-18309`](https://youtrack.jetbrains.com/issue/KT-18309) Join lines breaks code
- [`KT-18373`](https://youtrack.jetbrains.com/issue/KT-18373) Facet: can't change target platform between JVM versions
- [`KT-18376`](https://youtrack.jetbrains.com/issue/KT-18376) Maven import fails with NPE at ArgumentUtils.convertArgumentsToStringList() if `jvmTarget` setting is absent
- [`KT-18418`](https://youtrack.jetbrains.com/issue/KT-18418) Generate equals and hashCode should be available for classes without properties
- [`KT-18429`](https://youtrack.jetbrains.com/issue/KT-18429) Android strings resources folding false positives
- [`KT-18444`](https://youtrack.jetbrains.com/issue/KT-18444) Type hints don't work for destructuring declarations
- [`KT-18475`](https://youtrack.jetbrains.com/issue/KT-18475) Gradle/IntelliJ sync can result in IntelliJ modules getting gradle artifacts added to the classpath, breaking compilation
- [`KT-18479`](https://youtrack.jetbrains.com/issue/KT-18479) Can't find usages of invoke operator with vararg parameter
- [`KT-18501`](https://youtrack.jetbrains.com/issue/KT-18501) Quick Documentation doesn't show when @Supress("unused") is above the javadoc
- [`KT-18566`](https://youtrack.jetbrains.com/issue/KT-18566) Long find usages for operators when there are several operators for the same type
- [`KT-18596`](https://youtrack.jetbrains.com/issue/KT-18596) "Generate hashCode" produces poorly formatted code
- [`KT-18725`](https://youtrack.jetbrains.com/issue/KT-18725) Android: `kotlin-language` facet disappears on reopening the project
- [`KT-18974`](https://youtrack.jetbrains.com/issue/KT-18974) Type hints shouldn't appear for negative literals
- [`KT-19054`](https://youtrack.jetbrains.com/issue/KT-19054) Lags in typing in string literal
- [`KT-19062`](https://youtrack.jetbrains.com/issue/KT-19062) Member navigation doesn't work in expression bodies of getters with inferred property type
- [`KT-19210`](https://youtrack.jetbrains.com/issue/KT-19210) Command line flags like -Xload-jsr305-annotations have no effect in IDE
- [`KT-19303`](https://youtrack.jetbrains.com/issue/KT-19303) Project language version settings are used to analyze libraries, disabling module-specific analysis flags like -Xjsr305-annotations

### IDE. Completion

- [`KT-8208`](https://youtrack.jetbrains.com/issue/KT-8208) Support static member completion with not-imported-yet classes
- [`KT-12104`](https://youtrack.jetbrains.com/issue/KT-12104) Smart completion does not work with "invoke" when receiver is expression
- [`KT-17074`](https://youtrack.jetbrains.com/issue/KT-17074) Incorrect autocomplete suggestions for contexts affected by @DslMarker
- [`KT-18443`](https://youtrack.jetbrains.com/issue/KT-18443) IntelliJ not handling default constructor argument from companion object well
- [`KT-19191`](https://youtrack.jetbrains.com/issue/KT-19191) Disable completion binding context caching by default

### IDE. Debugger

- [`KT-14845`](https://youtrack.jetbrains.com/issue/KT-14845) Ekonstuate expression freezes debugger while ekonstuating filter, for time proportional to number of elements in collection.
- [`KT-17120`](https://youtrack.jetbrains.com/issue/KT-17120) Ekonstuate expression: cannot find local variable
- [`KT-18453`](https://youtrack.jetbrains.com/issue/KT-18453) Support 'Step over' and 'Force step over' action for suspended calls
- [`KT-18577`](https://youtrack.jetbrains.com/issue/KT-18577) Debug: Smart Step Into does not enter functions passed as variable or parameter: "Method invoke() has not been called"
- [`KT-18632`](https://youtrack.jetbrains.com/issue/KT-18632) Debug: Smart Step Into does not enter functions passed as variable or parameter when signature of lambda and parameter doesn't match
- [`KT-18949`](https://youtrack.jetbrains.com/issue/KT-18949) Can't stop on breakpoint after call to inline in Android Studio
- [`KT-19403`](https://youtrack.jetbrains.com/issue/KT-19403) 30s complete hangs of application on breakpoints stop attempt

### IDE. Inspections and Intentions

#### New Features

- [`KT-12119`](https://youtrack.jetbrains.com/issue/KT-12119) Intention to replace .addAll() on a mutable collection with +=
- [`KT-13436`](https://youtrack.jetbrains.com/issue/KT-13436) Replace 'when' with return: handle case when all branches jump out (return Nothing)
- [`KT-13458`](https://youtrack.jetbrains.com/issue/KT-13458) Cascade "replace with return" for if/when expressions
- [`KT-13676`](https://youtrack.jetbrains.com/issue/KT-13676) Add better quickfix for 'let' and 'error  'only not null or asserted calls are allowed'
- [`KT-14648`](https://youtrack.jetbrains.com/issue/KT-14648) Add quickfix for @receiver annotation being applied to extension member instead of extension type
- [`KT-14799`](https://youtrack.jetbrains.com/issue/KT-14799) Add inspection to simplify successive null checks into safe-call and null check
- [`KT-14900`](https://youtrack.jetbrains.com/issue/KT-14900) "Lift return out of when/if" should work with control flow expressions
- [`KT-15257`](https://youtrack.jetbrains.com/issue/KT-15257) JS: quickfix to migrate from @native to external
- [`KT-15368`](https://youtrack.jetbrains.com/issue/KT-15368) Add intention to convert Boolean? == true to ?: false and vice versa
- [`KT-15893`](https://youtrack.jetbrains.com/issue/KT-15893) "Array property in data class" inspection could have a quick fix to generate `equals()` and `hashcode()`
- [`KT-15958`](https://youtrack.jetbrains.com/issue/KT-15958) Inspection to inline "unnecessary" variables
- [`KT-16063`](https://youtrack.jetbrains.com/issue/KT-16063) Inspection to suggest converting block body to expression body
- [`KT-17198`](https://youtrack.jetbrains.com/issue/KT-17198) Inspection to replace filter calls followed by functions with a predicate variant
- [`KT-17580`](https://youtrack.jetbrains.com/issue/KT-17580) Add remaning branches intention should be available for sealed classes
- [`KT-17583`](https://youtrack.jetbrains.com/issue/KT-17583) Support "Declaration access can be weaker"  inspection for kotlin properties
- [`KT-17815`](https://youtrack.jetbrains.com/issue/KT-17815) Quick-fix "Replace with safe call & elvis"
- [`KT-17842`](https://youtrack.jetbrains.com/issue/KT-17842) Add quick-fix for NO_CONSTRUCTOR error
- [`KT-17895`](https://youtrack.jetbrains.com/issue/KT-17895) Inspection to replace 'a .. b-1' with 'a until b'
- [`KT-17919`](https://youtrack.jetbrains.com/issue/KT-17919) Add "Simplify if" intention/inspection
- [`KT-17920`](https://youtrack.jetbrains.com/issue/KT-17920) Add intention/inspection removing redundant spread operator for arrayOf call
- [`KT-17970`](https://youtrack.jetbrains.com/issue/KT-17970) Intention actions to format parameter/argument list placing each on separate line
- [`KT-18236`](https://youtrack.jetbrains.com/issue/KT-18236) Add inspection for potentially wrongly placed unary operators
- [`KT-18274`](https://youtrack.jetbrains.com/issue/KT-18274) Add inspection to replace map+joinTo with joinTo(transform)
- [`KT-18386`](https://youtrack.jetbrains.com/issue/KT-18386) Inspection to detect safe calls of orEmpty()
- [`KT-18438`](https://youtrack.jetbrains.com/issue/KT-18438) Add inspection for empty ranges with start > endInclusive
- [`KT-18460`](https://youtrack.jetbrains.com/issue/KT-18460) Add intentions to apply De Morgan's laws to conditions
- [`KT-18516`](https://youtrack.jetbrains.com/issue/KT-18516) Add inspection to detect & remove redundant Unit
- [`KT-18517`](https://youtrack.jetbrains.com/issue/KT-18517) Provide "Remove explicit type" inspection for some obvious cases
- [`KT-18534`](https://youtrack.jetbrains.com/issue/KT-18534) Quick-fix to add empty brackets after primary constructor
- [`KT-18540`](https://youtrack.jetbrains.com/issue/KT-18540) Add quickfix to create data class property from usage in destructuring declaration
- [`KT-18615`](https://youtrack.jetbrains.com/issue/KT-18615) Inspection to replace if with three or more options with when
- [`KT-18749`](https://youtrack.jetbrains.com/issue/KT-18749) Inspection for useless operations on collection with not-null elements
- [`KT-18830`](https://youtrack.jetbrains.com/issue/KT-18830) "Lift return out of try"
#### Fixes

- [`KT-11906`](https://youtrack.jetbrains.com/issue/KT-11906) Spring: "Create getter / setter" quick fixes cause IOE at LightElement.add()
- [`KT-12524`](https://youtrack.jetbrains.com/issue/KT-12524) Wrong "redundant semicolon" for semicolon inside an enum class before the companion object declaration
- [`KT-13870`](https://youtrack.jetbrains.com/issue/KT-13870) Wrong caption "Change to property access" for Quick Fix to convert class instantiation to object reference
- [`KT-13886`](https://youtrack.jetbrains.com/issue/KT-13886) Unused variable intention should remove constant initializer
- [`KT-14092`](https://youtrack.jetbrains.com/issue/KT-14092) "Make <modifier>" intention inserts modifier between annotation and class keywords
- [`KT-14093`](https://youtrack.jetbrains.com/issue/KT-14093) "Make <modifier>" intention available only on modifier when declaration already have a visibility modifier
- [`KT-14643`](https://youtrack.jetbrains.com/issue/KT-14643) "Add non-null asserted call" quickfix should not be offered on literal null constants
- [`KT-15242`](https://youtrack.jetbrains.com/issue/KT-15242) Create type from usage should include constraints into base types
- [`KT-16046`](https://youtrack.jetbrains.com/issue/KT-16046) Globally unused typealias is not marked as such
- [`KT-16069`](https://youtrack.jetbrains.com/issue/KT-16069) "Simplify if statement" doesn't work in specific case
- [`KT-17026`](https://youtrack.jetbrains.com/issue/KT-17026) "Replace explicit parameter" should not be shown on destructuring declaration
- [`KT-17092`](https://youtrack.jetbrains.com/issue/KT-17092) Create function from usage works incorrectly with ::class expression
- [`KT-17353`](https://youtrack.jetbrains.com/issue/KT-17353) "Create type parameter from usage" should not be offered for unresolved annotations
- [`KT-17537`](https://youtrack.jetbrains.com/issue/KT-17537) Create from Usage should suggest Boolean return type if function is used in if condition
- [`KT-17623`](https://youtrack.jetbrains.com/issue/KT-17623) "Remove explicit type arguments" is too conservative sometimes
- [`KT-17651`](https://youtrack.jetbrains.com/issue/KT-17651) Create property from usage should make lateinit var
- [`KT-17726`](https://youtrack.jetbrains.com/issue/KT-17726) Nullability quick-fixes operate incorrectly with implicit nullable receiver
- [`KT-17740`](https://youtrack.jetbrains.com/issue/KT-17740) CME at MakeOverriddenMemberOpenFix.getText()
- [`KT-18506`](https://youtrack.jetbrains.com/issue/KT-18506) Inspection on final Kotlin spring components is false positive
- [`KT-17823`](https://youtrack.jetbrains.com/issue/KT-17823) Intention "Make private" and friends should respect modifier order
- [`KT-17917`](https://youtrack.jetbrains.com/issue/KT-17917) Superfluos suggestion to add replaceWith for DeprecationLevel.HIDDEN
- [`KT-17954`](https://youtrack.jetbrains.com/issue/KT-17954) Setting error severity on "Kotlin | Function or property has platform type" does not show up as error in IDE
- [`KT-17996`](https://youtrack.jetbrains.com/issue/KT-17996) Android Studio Default Constructor Command Removes Custom Setter
- [`KT-18033`](https://youtrack.jetbrains.com/issue/KT-18033) Do not suggest to cast expression to non-nullable type when it's the same as !!
- [`KT-18035`](https://youtrack.jetbrains.com/issue/KT-18035) Quickfix for "CanBePrimaryConstructorProperty" does not work correctly with vararg constructor properties
- [`KT-18044`](https://youtrack.jetbrains.com/issue/KT-18044) "Move to class body" intention: better placement in the body
- [`KT-18074`](https://youtrack.jetbrains.com/issue/KT-18074) Suggestion in Intention 'Specify return type explicitly' doesn't support generic type parameter
- [`KT-18120`](https://youtrack.jetbrains.com/issue/KT-18120) Recursive property accessor gives false positives
- [`KT-18148`](https://youtrack.jetbrains.com/issue/KT-18148) Incorrect, not working quickfix - final and can't be overridden
- [`KT-18160`](https://youtrack.jetbrains.com/issue/KT-18160) Circular autofix actions between redundant modality and non-final variable with allopen plugin
- [`KT-18194`](https://youtrack.jetbrains.com/issue/KT-18194) "Protected in final" inspection works incorrectly with all-open
- [`KT-18195`](https://youtrack.jetbrains.com/issue/KT-18195) "Redundant modality" is not reported with all-open
- [`KT-18197`](https://youtrack.jetbrains.com/issue/KT-18197) Redundant "make open" for abstract class member with all-open
- [`KT-18253`](https://youtrack.jetbrains.com/issue/KT-18253) Wrong location of "Redundant 'toString()' call in string template" quickfix
- [`KT-18347`](https://youtrack.jetbrains.com/issue/KT-18347) Nullability quickfixes are not helpful when using invoke operator
- [`KT-18368`](https://youtrack.jetbrains.com/issue/KT-18368) "Cast expression x to Type" fails for expression inside argument list
- [`KT-18375`](https://youtrack.jetbrains.com/issue/KT-18375) Backticked function name is suggested to be renamed to the same name
- [`KT-18385`](https://youtrack.jetbrains.com/issue/KT-18385) Spring: Generate Dependency causes Throwable "AWT events are not allowed inside write action"
- [`KT-18407`](https://youtrack.jetbrains.com/issue/KT-18407) "Move property to constructor" action should not appear on properties declared in interfaces
- [`KT-18425`](https://youtrack.jetbrains.com/issue/KT-18425) Make <modifier> intention inserts modifier at wrong position for sealed class
- [`KT-18529`](https://youtrack.jetbrains.com/issue/KT-18529) Add '!!' quick fix applies to wrong expression on operation 'in'
- [`KT-18642`](https://youtrack.jetbrains.com/issue/KT-18642) Remove unused parameter intention transfers default konstue to another parameter
- [`KT-18683`](https://youtrack.jetbrains.com/issue/KT-18683) Wrong 'equals' is generated for Kotlin JS project
- [`KT-18709`](https://youtrack.jetbrains.com/issue/KT-18709) "Lift assignment out of if" changes semantics
- [`KT-18711`](https://youtrack.jetbrains.com/issue/KT-18711) "Lift return out of when" changes semantics for functional type
- [`KT-18717`](https://youtrack.jetbrains.com/issue/KT-18717) Report MemberVisibilityCanBePrivate on visibility modifier if present
- [`KT-18722`](https://youtrack.jetbrains.com/issue/KT-18722) Correct "before" sample in description for intention Convert to enum class
- [`KT-18723`](https://youtrack.jetbrains.com/issue/KT-18723) Correct "after" sample for intention Convert to apply
- [`KT-18852`](https://youtrack.jetbrains.com/issue/KT-18852) "Lift return out of when" does not work for exhaustive when without else
- [`KT-18928`](https://youtrack.jetbrains.com/issue/KT-18928) In IDE, "Replace 'if' expression with safe access expression incorrectly replace expression when using property
- [`KT-18954`](https://youtrack.jetbrains.com/issue/KT-18954) Kotlin plugin updater activates in headless mode
- [`KT-18970`](https://youtrack.jetbrains.com/issue/KT-18970) Do not report "property can be private" on JvmField properties
- [`KT-19232`](https://youtrack.jetbrains.com/issue/KT-19232) Replace Math.min with coerceAtMost intention is broken
- [`KT-19272`](https://youtrack.jetbrains.com/issue/KT-19272) Do not report "function can be private" on JUnit 3 test methods

### IDE. Refactorings

#### New Features

- [`KT-4379`](https://youtrack.jetbrains.com/issue/KT-4379) Support renaming import alias
- [`KT-8180`](https://youtrack.jetbrains.com/issue/KT-8180) Copy Class
- [`KT-17547`](https://youtrack.jetbrains.com/issue/KT-17547) Refactor / Move: Problems Detected / Conflicts in View: only referencing file is mentioned
#### Fixes

- [`KT-9054`](https://youtrack.jetbrains.com/issue/KT-9054) Copy / pasting a Kotlin file should bring up the Copy Class dialog
- [`KT-13437`](https://youtrack.jetbrains.com/issue/KT-13437) Change signature replaces return type with Unit when it's not requested
- [`KT-15859`](https://youtrack.jetbrains.com/issue/KT-15859) Renaming variables or functions with backticks removes the backticks
- [`KT-16180`](https://youtrack.jetbrains.com/issue/KT-16180) Opened decompiled editor blocks refactoring of involved element
- [`KT-17062`](https://youtrack.jetbrains.com/issue/KT-17062) Field/property inline refactoring works incorrectly with Kotlin & Java usages
- [`KT-17128`](https://youtrack.jetbrains.com/issue/KT-17128) Refactor / Rename in the last position of label name throws Throwable "PsiElement(IDENTIFIER) by com.intellij.refactoring.rename.inplace.MemberInplaceRenamer" at InplaceRefactoring.buildTemplateAndStart()
- [`KT-17489`](https://youtrack.jetbrains.com/issue/KT-17489) Refactor / Inline Property: cannot inline konst with the following plusAssign
- [`KT-17571`](https://youtrack.jetbrains.com/issue/KT-17571) Refactor / Move warns about using private/internal class from Java, but this is not related to the move
- [`KT-17622`](https://youtrack.jetbrains.com/issue/KT-17622) Refactor / Inline Function loses type arguments
- [`KT-18034`](https://youtrack.jetbrains.com/issue/KT-18034) Copy Class refactoring replaces all usages of the class with the new one!
- [`KT-18076`](https://youtrack.jetbrains.com/issue/KT-18076) Refactor / Rename on alias of Java class suggests to select between refactoring handlers
- [`KT-18096`](https://youtrack.jetbrains.com/issue/KT-18096) Refactor / Rename on import alias usage of a class member element tries to rename the element itself
- [`KT-18098`](https://youtrack.jetbrains.com/issue/KT-18098) Refactor / Copy can't generate proper import if original code uses import alias of java member
- [`KT-18135`](https://youtrack.jetbrains.com/issue/KT-18135) Refactor: no Problems Detected for Copy/Move source using platform type to another platform's module
- [`KT-18200`](https://youtrack.jetbrains.com/issue/KT-18200) Refactor / Copy is enabled for Java source selected with Kotlin file, but not for Java source selected with Kotlin class
- [`KT-18241`](https://youtrack.jetbrains.com/issue/KT-18241) Refactor / Copy (and Move) fails for chain of lambdas and invoke()'s with IllegalStateException: "No selector for PARENTHESIZED" at KtSimpleNameReference.changeQualifiedName()
- [`KT-18325`](https://youtrack.jetbrains.com/issue/KT-18325) Renaming a parameter name in one implementation silently rename it in all implementations
- [`KT-18390`](https://youtrack.jetbrains.com/issue/KT-18390) Refactor / Copy called for Java class opens only Copy File dialog
- [`KT-18699`](https://youtrack.jetbrains.com/issue/KT-18699) Refactor / Copy, Move loses necessary parentheses
- [`KT-18738`](https://youtrack.jetbrains.com/issue/KT-18738) Misleading quick fix message for an 'open' modifier on an interface member
- [`KT-19130`](https://youtrack.jetbrains.com/issue/KT-19130) Refactor / Inline konst: "Show inline dialog for local variables" setting is ignored

### JavaScript

#### Performance Improvements

- [`KT-18331`](https://youtrack.jetbrains.com/issue/KT-18331) JS: compilation performance degrades fast when inlined nested labels are used
#### Fixes

- [`KT-4078`](https://youtrack.jetbrains.com/issue/KT-4078) JS sourcemaps should contain relative path. The relative base & prefix should be set from project/module preferences
- [`KT-8020`](https://youtrack.jetbrains.com/issue/KT-8020) JS: String? plus operator crashes on runtime
- [`KT-13919`](https://youtrack.jetbrains.com/issue/KT-13919) JS: Source map weirdness
- [`KT-15456`](https://youtrack.jetbrains.com/issue/KT-15456) JS: inlining doesn't work for array constructor with size and lambda
- [`KT-16984`](https://youtrack.jetbrains.com/issue/KT-16984) KotlinJS - 1 > 2 > false causes unhandled javascript exception
- [`KT-17285`](https://youtrack.jetbrains.com/issue/KT-17285) JS: wrong result when call function with default parameter overridden by delegation by function from another interface
- [`KT-17445`](https://youtrack.jetbrains.com/issue/KT-17445) JS: minifier for Kotlin JS apps
- [`KT-17476`](https://youtrack.jetbrains.com/issue/KT-17476) JS: Some symbols in identifiers compile, but are not legal
- [`KT-17871`](https://youtrack.jetbrains.com/issue/KT-17871) JS: spread vararg call doesn't work on functions imported with @JsModule
- [`KT-18027`](https://youtrack.jetbrains.com/issue/KT-18027) JS: Illegal symbols are possible in backticked labels, but cause crash in runtime and malformed js code
- [`KT-18032`](https://youtrack.jetbrains.com/issue/KT-18032) JS: Illegal symbols are possible in backticked package names, but cause crash in runtime and malformed js code
- [`KT-18169`](https://youtrack.jetbrains.com/issue/KT-18169) JS: reified generic backticked type name containing non-identifier symbols causes malformed JS and runtime crash
- [`KT-18187`](https://youtrack.jetbrains.com/issue/KT-18187) JS backend does not copy non-abstract method of interface to implementing class in some cases
- [`KT-18201`](https://youtrack.jetbrains.com/issue/KT-18201) JS backend generates wrong code for inline function which calls non-inline function from another module
- [`KT-18652`](https://youtrack.jetbrains.com/issue/KT-18652) JS: Objects from same package but from different libraries are incorrectly accessed

### Libraries

- [`KT-18526`](https://youtrack.jetbrains.com/issue/KT-18526) Small typo in documentation for kotlin-stdlib / kotlin.collections / retainAll
- [`KT-18624`](https://youtrack.jetbrains.com/issue/KT-18624) JS: Bad return type for Promise.all
- [`KT-18670`](https://youtrack.jetbrains.com/issue/KT-18670) Incorrect documentation of MutableMap.konstues
- [`KT-18671`](https://youtrack.jetbrains.com/issue/KT-18671) Provide implementation for CoroutineContext.Element functions.

### Reflection

- [`KT-15222`](https://youtrack.jetbrains.com/issue/KT-15222) Support reflection for local delegated properties
- [`KT-14094`](https://youtrack.jetbrains.com/issue/KT-14094) IllegalAccessException when try to get members annotated by private annotation with parameter
- [`KT-16399`](https://youtrack.jetbrains.com/issue/KT-16399) Embedded Tomcat fails to load Class-Path: kotlin-runtime.jar from kotlin-reflect-1.0.6.jar
- [`KT-16810`](https://youtrack.jetbrains.com/issue/KT-16810) Do not include incorrect ExternalOverridabilityCondition service file into kotlin-reflect.jar
- [`KT-18404`](https://youtrack.jetbrains.com/issue/KT-18404) “KotlinReflectionInternalError: This callable does not support a default call” when function or constructor has more than 32 parameters
- [`KT-18476`](https://youtrack.jetbrains.com/issue/KT-18476) KClass<*>.superclasses does not contain Any::class
- [`KT-18480`](https://youtrack.jetbrains.com/issue/KT-18480) Kotlin Reflection unable to call getter of protected read-only konst with custom getter from parent class

### Tools

- [`KT-18245`](https://youtrack.jetbrains.com/issue/KT-18245) NoArg: IllegalAccessError on instantiating sealed class child via Java reflection
- [`KT-18874`](https://youtrack.jetbrains.com/issue/KT-18874) Crash during compilation after switching to 1.1.3-release-IJ2017.2-2
- [`KT-19047`](https://youtrack.jetbrains.com/issue/KT-19047) Private methods are final event if used with the all-open-plugin.

### Tools. CLI

- [`KT-17297`](https://youtrack.jetbrains.com/issue/KT-17297) Report error when CLI compiler is not being run under Java 8+
- [`KT-18599`](https://youtrack.jetbrains.com/issue/KT-18599) Support -Xmodule-path and -Xadd-modules arguments for modular compilation on Java 9
- [`KT-18794`](https://youtrack.jetbrains.com/issue/KT-18794) kotlinc-jvm prints an irrelevant error message when a JVM Home directory does not exist
- [`KT-3045`](https://youtrack.jetbrains.com/issue/KT-3045) Report error instead of failing with exception on "kotlinc -script foo.kt"
- [`KT-18754`](https://youtrack.jetbrains.com/issue/KT-18754) Rename CLI argument "-module" to "-Xbuild-file"
- [`KT-18927`](https://youtrack.jetbrains.com/issue/KT-18927) run kotlin app crashes eclipse

### Tools. Gradle

- [`KT-10537`](https://youtrack.jetbrains.com/issue/KT-10537) Gradle plugin doesn't pick up changed project.buildDir
- [`KT-17031`](https://youtrack.jetbrains.com/issue/KT-17031) JVM crash on in-process compilation in Gradle with debug
- [`KT-17035`](https://youtrack.jetbrains.com/issue/KT-17035) Gradle Kotlin Plugin can not compile tests calling source internal fields/variables if compileJava dumps classes to a different directory and then copied classes are moved to sourceSets.main.output.classesDir by a different task
- [`KT-17197`](https://youtrack.jetbrains.com/issue/KT-17197) Gradle Kotlin plugin does not wire task dependencies correctly, causing compilation failures
- [`KT-17618`](https://youtrack.jetbrains.com/issue/KT-17618) Pass freeCompilerArgs to compiler unchanged
- [`KT-18262`](https://youtrack.jetbrains.com/issue/KT-18262) kotlin-spring should also open @SpringBootTest classes
- [`KT-18647`](https://youtrack.jetbrains.com/issue/KT-18647) Kotlin incremental compile cannot be disabled.
- [`KT-18832`](https://youtrack.jetbrains.com/issue/KT-18832) Java version parsing error with Gradle Kotlin plugin + JDK 9

### Tools. J2K

- [`KT-10762`](https://youtrack.jetbrains.com/issue/KT-10762) J2K removes empty lines from Doc-comments
- [`KT-13146`](https://youtrack.jetbrains.com/issue/KT-13146) J2K goes into infinite loop with anonymous inner class that references itself
- [`KT-15761`](https://youtrack.jetbrains.com/issue/KT-15761) Converting Java to Kotlin corrupts string which includes escaped backslash
- [`KT-16133`](https://youtrack.jetbrains.com/issue/KT-16133) Converting switch statement inserts dead code (possibly as a false positive for fall-through)
- [`KT-16142`](https://youtrack.jetbrains.com/issue/KT-16142) Kotlin Konverter produces empty line in Kdoc
- [`KT-18038`](https://youtrack.jetbrains.com/issue/KT-18038) Java to Kotlin converter messes up empty lines while converting from JavaDoc to KDoc
- [`KT-18051`](https://youtrack.jetbrains.com/issue/KT-18051) Doesn't work the auto-convert Java to Kotlin in Android Studio 3.0
- [`KT-18141`](https://youtrack.jetbrains.com/issue/KT-18141) J2K changes semantic when while does not have a body
- [`KT-18142`](https://youtrack.jetbrains.com/issue/KT-18142) J2K changes semantics when `if` does not have a body
- [`KT-18512`](https://youtrack.jetbrains.com/issue/KT-18512) J2K Incorrect null parameter conversion

### Tools. JPS

- [`KT-14848`](https://youtrack.jetbrains.com/issue/KT-14848) JPS: inkonstid compiler argument causes exception (see also EA-92062)
- [`KT-16057`](https://youtrack.jetbrains.com/issue/KT-16057) Provide better error message when the same compiler argument is set twice
- [`KT-19155`](https://youtrack.jetbrains.com/issue/KT-19155) IllegalArgumentException: Unsupported kind: PACKAGE_LOCAL_VARIABLE_LIST in incremental compilation

### Tools. Maven

- [`KT-18022`](https://youtrack.jetbrains.com/issue/KT-18022) kotlin maven plugin - adding dependencies overwrites arguments.pluginClassPath preventing kapt goal from running
- [`KT-18224`](https://youtrack.jetbrains.com/issue/KT-18224) Maven compilation with JDK 9 fails with InaccessibleObjectException

### Tools. REPL

- [`KT-5620`](https://youtrack.jetbrains.com/issue/KT-5620) REPL: Support destructuring declarations
- [`KT-12564`](https://youtrack.jetbrains.com/issue/KT-12564) Kotlin REPL Doesn't Perform Many Checks
- [`KT-15172`](https://youtrack.jetbrains.com/issue/KT-15172) REPL: function declarations that contain empty lines throw error
- [`KT-18181`](https://youtrack.jetbrains.com/issue/KT-18181) REPL: support non-headless execution for Swing code
- [`KT-18349`](https://youtrack.jetbrains.com/issue/KT-18349) REPL: do not show warnings when there are errors

### Tools. kapt

- [`KT-18682`](https://youtrack.jetbrains.com/issue/KT-18682) Kapt: Anonymous class types are not rendered properly in stubs
- [`KT-18758`](https://youtrack.jetbrains.com/issue/KT-18758) Kotlin 1.1.3  / Kapt fails with gradle
- [`KT-18799`](https://youtrack.jetbrains.com/issue/KT-18799) Kapt3, IC: Kapt does not generate annotation konstue for constant konstues in documented types
- [`KT-19178`](https://youtrack.jetbrains.com/issue/KT-19178) Kapt: Build dependencies from 'kapt' configuration should go into the 'kaptCompile' task dependencies
- [`KT-19179`](https://youtrack.jetbrains.com/issue/KT-19179) Kapt: Gradle silently skips 'kotlinKapt' task sometimes
- [`KT-19211`](https://youtrack.jetbrains.com/issue/KT-19211) Kapt3: Generated classes output is not synchronized with Java classes output in pure Java projects (Gradle 4+)

## 1.1.3-2

#### Fixes

-   Noarg compiler plugin reverted to 1.1.2 behavior: by default, it will not run any initialization code
    from the generated default constructor. If you want to run initializers, you need to enable
    the corresponding option as described in the documentation.
    Note that if a @noarg class has initializers that depend on constructor parameters, you will get incorrect
    compiler behavior, so you shouldn't enable this option if you have such classes in your project.
    This resolves [`KT-18667`](https://youtrack.jetbrains.com/issue/KT-18667) and [`KT-18668`](https://youtrack.jetbrains.com/issue/KT-18668).
- [`KT-18689`](https://youtrack.jetbrains.com/issue/KT-18689) Incorrect bytecode generated when passing a bound member reference to an inline function with default argument konstues
- [`KT-18377`](https://youtrack.jetbrains.com/issue/KT-18377) Syntax error while generating kapt stubs
- [`KT-18411`](https://youtrack.jetbrains.com/issue/KT-18411) Slow debugger step-in into inlined function
- [`KT-18687`](https://youtrack.jetbrains.com/issue/KT-18687) Deadlock in resolve with Kotlin 1.1.3
- [`KT-18726`](https://youtrack.jetbrains.com/issue/KT-18726) Frequent UI hangs in 2017.2 EAPs

## 1.1.3

### Android

#### New Features

- [`KT-12049`](https://youtrack.jetbrains.com/issue/KT-12049) Kotlin Lint: "Missing Parcelable CREATOR field" could suggest "Add implementation" quick fix
- [`KT-16712`](https://youtrack.jetbrains.com/issue/KT-16712) Show warning in IDEA when using Java 1.8 api in Android
- [`KT-16843`](https://youtrack.jetbrains.com/issue/KT-16843) Android: provide gutter icons for resources like colors and drawables
- [`KT-17389`](https://youtrack.jetbrains.com/issue/KT-17389) Implement Intention "Add Activity / BroadcastReceiver / Service to manifest" 
- [`KT-17465`](https://youtrack.jetbrains.com/issue/KT-17465) Add intentions Add/Remove/Redo parcelable implementation
#### Fixes

- [`KT-14970`](https://youtrack.jetbrains.com/issue/KT-14970) ClassCastException: butterknife.lint.LintRegistry cannot be cast to com.android.tools.klint.client.api.IssueRegistry
- [`KT-17287`](https://youtrack.jetbrains.com/issue/KT-17287) Configure kotlin in Android Studio: don't show menu Choose Configurator with single choice
- [`KT-17288`](https://youtrack.jetbrains.com/issue/KT-17288) Android Studio: please remove menu item Configure Kotlin (JavaScript) in Project
- [`KT-17289`](https://youtrack.jetbrains.com/issue/KT-17289) Android Studio: Hide "Configure Kotlin" balloon if Kotlin is configured from a kt file banner
- [`KT-17291`](https://youtrack.jetbrains.com/issue/KT-17291) Android Studio 2.4: Cannot get property 'metaClass' on null object error after Kotlin configuring
- [`KT-17610`](https://youtrack.jetbrains.com/issue/KT-17610) "Unknown reference: kotlinx"

### Compiler

#### New Features

- [`KT-11167`](https://youtrack.jetbrains.com/issue/KT-11167) Support compilation against JRE 9
- [`KT-17497`](https://youtrack.jetbrains.com/issue/KT-17497) Warn about redundant else branch in exhaustive when
#### Performance Improvements

- [`KT-7931`](https://youtrack.jetbrains.com/issue/KT-7931) Optimize iteration over strings/charsequences on JVM
- [`KT-10848`](https://youtrack.jetbrains.com/issue/KT-10848) Optimize substitution of inline function with default parameters
- [`KT-12497`](https://youtrack.jetbrains.com/issue/KT-12497) Optimize inlined bytecode for functions with default parameters
- [`KT-17342`](https://youtrack.jetbrains.com/issue/KT-17342) Optimize control-flow for case of many variables
- [`KT-17562`](https://youtrack.jetbrains.com/issue/KT-17562) Optimize KtFile::isScript
#### Fixes

- [`KT-4960`](https://youtrack.jetbrains.com/issue/KT-4960) Redeclaration is not reported for type parameters of interfaces
- [`KT-5160`](https://youtrack.jetbrains.com/issue/KT-5160) No warning when a lambda parameter hides a variable
- [`KT-5246`](https://youtrack.jetbrains.com/issue/KT-5246) is check fails on cyclic type parameter bounds
- [`KT-5354`](https://youtrack.jetbrains.com/issue/KT-5354) Wrong label resolution when label name clash with fun name
- [`KT-7645`](https://youtrack.jetbrains.com/issue/KT-7645) Prohibit default konstue for `catch`-block parameter
- [`KT-7724`](https://youtrack.jetbrains.com/issue/KT-7724) Can never import private member 
- [`KT-7796`](https://youtrack.jetbrains.com/issue/KT-7796) Wrong scope for default parameter konstue resolution
- [`KT-7984`](https://youtrack.jetbrains.com/issue/KT-7984) Unexpected "Unresolved reference" in a default konstue expression in a local function
- [`KT-7985`](https://youtrack.jetbrains.com/issue/KT-7985) Unexpected "Unresolved reference to a type parameter" in a default konstue expression in a local function
- [`KT-8320`](https://youtrack.jetbrains.com/issue/KT-8320) It should not be possible to catch a type parameter type
- [`KT-8877`](https://youtrack.jetbrains.com/issue/KT-8877) Automatic labeling doesn't work for infix calls
- [`KT-9251`](https://youtrack.jetbrains.com/issue/KT-9251) Qualified this does not work with labeled function literals
- [`KT-9551`](https://youtrack.jetbrains.com/issue/KT-9551) False warning "No cast needed"
- [`KT-9645`](https://youtrack.jetbrains.com/issue/KT-9645) Incorrect inspection: No cast Needed
- [`KT-9986`](https://youtrack.jetbrains.com/issue/KT-9986) 'null as T' should be unchecked cast
- [`KT-10397`](https://youtrack.jetbrains.com/issue/KT-10397) java.lang.reflect.GenericSignatureFormatError when generic inner class is mentioned in function signature
- [`KT-11474`](https://youtrack.jetbrains.com/issue/KT-11474) ISE: Requested A, got foo.A in JavaClassFinderImpl on Java file with package not matching directory
- [`KT-11622`](https://youtrack.jetbrains.com/issue/KT-11622) False "No cast needed" when ambiguous call because of smart cast
- [`KT-12245`](https://youtrack.jetbrains.com/issue/KT-12245) Code with annotation that has an unresolved identifier as a parameter compiles successfully
- [`KT-12269`](https://youtrack.jetbrains.com/issue/KT-12269) False "Non-null type is checked for instance of nullable type"
- [`KT-12683`](https://youtrack.jetbrains.com/issue/KT-12683) A problem with `is` operator and non-reified type-parameters
- [`KT-12690`](https://youtrack.jetbrains.com/issue/KT-12690) USELESS_CAST compiler warning may break code when fix is applied
- [`KT-13348`](https://youtrack.jetbrains.com/issue/KT-13348) Report useless cast on safe cast from nullable type to the same not null type
- [`KT-13597`](https://youtrack.jetbrains.com/issue/KT-13597) No check for accessing final field in local object in constructor
- [`KT-13997`](https://youtrack.jetbrains.com/issue/KT-13997) Incorrect "Property must be initialized or be abstract" error for property with external accessors
- [`KT-14381`](https://youtrack.jetbrains.com/issue/KT-14381) Possible konst reassignment not detected inside init block
- [`KT-14564`](https://youtrack.jetbrains.com/issue/KT-14564) java.lang.VerifyError: Bad local variable type
- [`KT-14801`](https://youtrack.jetbrains.com/issue/KT-14801) Invoke error message if nested class has the same name as a function from base class
- [`KT-14977`](https://youtrack.jetbrains.com/issue/KT-14977) IDE doesn't warn about checking null konstue of variable that cannot be null
- [`KT-15085`](https://youtrack.jetbrains.com/issue/KT-15085) Label and function naming conflict is resolved in unintuitive way
- [`KT-15161`](https://youtrack.jetbrains.com/issue/KT-15161) False warning "no cast needed" for array creation
- [`KT-15480`](https://youtrack.jetbrains.com/issue/KT-15480) Cannot destruct a list when "if" has an "else" branch
- [`KT-15495`](https://youtrack.jetbrains.com/issue/KT-15495) Internal typealiases in the same module are inaccessible on incremental compilation
- [`KT-15566`](https://youtrack.jetbrains.com/issue/KT-15566) Object member imported in file scope used in delegation expression in object declaration should be a compiler error
- [`KT-16016`](https://youtrack.jetbrains.com/issue/KT-16016) Compiler failure with NO_EXPECTED_TYPE
- [`KT-16426`](https://youtrack.jetbrains.com/issue/KT-16426) Return statement resolved to function instead of property getter
- [`KT-16813`](https://youtrack.jetbrains.com/issue/KT-16813) Anonymous objects returned from private-in-file members should behave as for private class members
- [`KT-16864`](https://youtrack.jetbrains.com/issue/KT-16864) Local delegate + ad-hoc object leads to CCE
- [`KT-17144`](https://youtrack.jetbrains.com/issue/KT-17144) Breakpoint inside `when`
- [`KT-17149`](https://youtrack.jetbrains.com/issue/KT-17149) Incorrect warning "Kotlin: This operation has led to an overflow"
- [`KT-17156`](https://youtrack.jetbrains.com/issue/KT-17156) No re-parse after lambda was converted to block
- [`KT-17318`](https://youtrack.jetbrains.com/issue/KT-17318) Typo in DSL Marker message `cant`
- [`KT-17384`](https://youtrack.jetbrains.com/issue/KT-17384) break/continue expression in inlined function parameter argument causes compilation exception
- [`KT-17457`](https://youtrack.jetbrains.com/issue/KT-17457) Suspend + LongRange couldn't transform method node issue in Kotlin 1.1.1
- [`KT-17479`](https://youtrack.jetbrains.com/issue/KT-17479) konst reassign is allowed via explicit this receiver
- [`KT-17560`](https://youtrack.jetbrains.com/issue/KT-17560) Overload resolution ambiguity on semi-konstid class-files generated by Scala
- [`KT-17572`](https://youtrack.jetbrains.com/issue/KT-17572) try-catch expression in inlined function parameter argument causes compilation exception
- [`KT-17573`](https://youtrack.jetbrains.com/issue/KT-17573) try-finally expression in inlined function parameter argument fails with VerifyError
- [`KT-17588`](https://youtrack.jetbrains.com/issue/KT-17588) Compiler error while optimizer tries to get rid of captured variable
- [`KT-17590`](https://youtrack.jetbrains.com/issue/KT-17590) conditional return in inline function parameter argument causes compilation exception
- [`KT-17591`](https://youtrack.jetbrains.com/issue/KT-17591) non-conditional return in inline function parameter argument causes compilation exception
- [`KT-17613`](https://youtrack.jetbrains.com/issue/KT-17613) 'this' expression referring to deprecated class instance is highlighted as deprecated in IDE
- [`KT-18358`](https://youtrack.jetbrains.com/issue/KT-18358) Keep smart pointers instead of PSI elements in JavaElementImpl and its descendants

### IDE

#### New Features

- [`KT-7810`](https://youtrack.jetbrains.com/issue/KT-7810) Separate icon for abstract class
- [`KT-8617`](https://youtrack.jetbrains.com/issue/KT-8617) Recognize TODO method usages and highlight them same as TODO-comment
- [`KT-12629`](https://youtrack.jetbrains.com/issue/KT-12629) Add rainbow/semantic-highlighting for local variables
- [`KT-14109`](https://youtrack.jetbrains.com/issue/KT-14109) support parameter hints in idea plugin
- [`KT-16645`](https://youtrack.jetbrains.com/issue/KT-16645) Support inlay type hints for implicitly typed konsts, properties, and functions
- [`KT-17807`](https://youtrack.jetbrains.com/issue/KT-17807) Add Smart Enter processor for object expessions
#### Performance Improvements

- [`KT-16995`](https://youtrack.jetbrains.com/issue/KT-16995) Typing during in-place refactorings is impossibly laggy
- [`KT-17331`](https://youtrack.jetbrains.com/issue/KT-17331) Frequent long editor freezes
- [`KT-17383`](https://youtrack.jetbrains.com/issue/KT-17383) Slow editing in Kotlin files If breadcrumbs are enabled in module with many dependencies
- [`KT-17495`](https://youtrack.jetbrains.com/issue/KT-17495) Much time spent in LibraryDependenciesCache.getLibrariesAndSdksUsedWith
#### Fixes

- [`KT-7848`](https://youtrack.jetbrains.com/issue/KT-7848) When you paste text into a string literal special symbols should be escaped
- [`KT-7954`](https://youtrack.jetbrains.com/issue/KT-7954) 'Go to symbol' doesn't show containing declaration for local symbols
- [`KT-9091`](https://youtrack.jetbrains.com/issue/KT-9091) Sometimes backticks of the method name with spaces are highlighted with rose background
- [`KT-10577`](https://youtrack.jetbrains.com/issue/KT-10577) Refactor / Move Kotlin + Java files adds wrong import in very specific case
- [`KT-12856`](https://youtrack.jetbrains.com/issue/KT-12856) Import fold region is not updated to include imports added while editing file
- [`KT-14161`](https://youtrack.jetbrains.com/issue/KT-14161) Navigate to symbol doesn't see local named functions
- [`KT-14601`](https://youtrack.jetbrains.com/issue/KT-14601) Formatter inserts unnecessary indent before 'else'
- [`KT-14639`](https://youtrack.jetbrains.com/issue/KT-14639) Incorrect name of code style setting: Align in columns 'case' branches
- [`KT-15029`](https://youtrack.jetbrains.com/issue/KT-15029) "Go to symbol" action doesn't find properties declared in primary constructors
- [`KT-15255`](https://youtrack.jetbrains.com/issue/KT-15255) Move cursor to a better place when creating a new Kotlin file
- [`KT-15273`](https://youtrack.jetbrains.com/issue/KT-15273) Kotlin IDE plugin adds `import java.lang.String` with "Optimize Imports", making project broken
- [`KT-16159`](https://youtrack.jetbrains.com/issue/KT-16159) Wrong "Constructor call" highlighting if operator is called on newly created object
- [`KT-16392`](https://youtrack.jetbrains.com/issue/KT-16392) Gradle/Maven java module: Add framework support/ Kotlin (Java or JavaScript) adds nothing
- [`KT-16423`](https://youtrack.jetbrains.com/issue/KT-16423) Show expression type doesn't work when selecting from the middle of expression with "Expand Selection"
- [`KT-16635`](https://youtrack.jetbrains.com/issue/KT-16635) Do not show kotlin-specific live templates macros for all context types
- [`KT-16755`](https://youtrack.jetbrains.com/issue/KT-16755) No "Is sublassed by" icon for sealed class
- [`KT-16775`](https://youtrack.jetbrains.com/issue/KT-16775) Rewrite at slice CLASS key: OBJECT_DECLARATION while writing code in IDE
- [`KT-16803`](https://youtrack.jetbrains.com/issue/KT-16803) Suspending iteration is not marked in the gutter by IDEA as suspending invocation
- [`KT-17037`](https://youtrack.jetbrains.com/issue/KT-17037) Editor suggests to import `EmptyCoroutineContext.plus` for any unresolved `+`
- [`KT-17046`](https://youtrack.jetbrains.com/issue/KT-17046) Kotlin facet, Compiler plugins: last line is shown empty when not selected
- [`KT-17088`](https://youtrack.jetbrains.com/issue/KT-17088) Settings: Kotlin Compiler: "Destination directory" should be enabled if "Copy library runtime files" is on on the dialog opening 
- [`KT-17094`](https://youtrack.jetbrains.com/issue/KT-17094) Kotlin facet, additional command line parameters dialog: please provide a title
- [`KT-17138`](https://youtrack.jetbrains.com/issue/KT-17138) Configure Kotlin in Project: Choose Configurator popup: names could be unified
- [`KT-17145`](https://youtrack.jetbrains.com/issue/KT-17145) Kotlin facet: IllegalArgumentException on attempt to show settings common for several javascript kotlin facets with different moduleKind
- [`KT-17223`](https://youtrack.jetbrains.com/issue/KT-17223) Absolute path to Kotlin compiler plugin in IML
- [`KT-17293`](https://youtrack.jetbrains.com/issue/KT-17293) Project Structure dialog is opened too slow for a project with a lot of empty gradle modules
- [`KT-17304`](https://youtrack.jetbrains.com/issue/KT-17304) IDEA shows wrong type for expressions
- [`KT-17439`](https://youtrack.jetbrains.com/issue/KT-17439) Kotlin: 'autoscroll from source' doesn't work in Structure view
- [`KT-17448`](https://youtrack.jetbrains.com/issue/KT-17448) Regression: Sample Resolve 
- [`KT-17482`](https://youtrack.jetbrains.com/issue/KT-17482) Set jvmTarget to 1.8 by default when configuring a project with JDK 1.8
- [`KT-17492`](https://youtrack.jetbrains.com/issue/KT-17492) -jvm-target is ignored by IntelliJ
- [`KT-17505`](https://youtrack.jetbrains.com/issue/KT-17505) LazyLightClassMemberMatchingError from collection implementation
- [`KT-17517`](https://youtrack.jetbrains.com/issue/KT-17517) Compiler options specified as properties are not handled by Maven importer
- [`KT-17521`](https://youtrack.jetbrains.com/issue/KT-17521) Quickfix to enable coroutines should work for Maven projects
- [`KT-17525`](https://youtrack.jetbrains.com/issue/KT-17525) IDE: KNPE at KotlinAddImportActionKt.createSingleImportActionForConstructor() on inkonstid reference to inner class constructor
- [`KT-17578`](https://youtrack.jetbrains.com/issue/KT-17578) Throwable: "Reported element PsiIdentifier:AnnotationConfiguration is not from the file 'PsiFile:InSource.kt' the inspection 'ImplicitSubclassInspection'"
- [`KT-17638`](https://youtrack.jetbrains.com/issue/KT-17638) ISE in KotlinElementDescriptionProvider.renderShort
- [`KT-17698`](https://youtrack.jetbrains.com/issue/KT-17698) Unknown library format - prevents IDEA from configuring Kotlin JS
- [`KT-17714`](https://youtrack.jetbrains.com/issue/KT-17714) UAST inspection on non-physical element
- [`KT-17722`](https://youtrack.jetbrains.com/issue/KT-17722) IntelliJ plugin uses wrong JVM target when Kotlin Facet is not configured
- [`KT-17770`](https://youtrack.jetbrains.com/issue/KT-17770) Kotlin IntelliJ plugin fails to re-index Gradle script classpath after change to the `plugins` block
- [`KT-17777`](https://youtrack.jetbrains.com/issue/KT-17777) Logger$EmptyThrowable: "Facet Kotlin (app) [kotlin-language] not found" at FacetModelImpl.removeFacet()
- [`KT-17810`](https://youtrack.jetbrains.com/issue/KT-17810) Exception from unused import inspection leads to code analysis hangs
- [`KT-17821`](https://youtrack.jetbrains.com/issue/KT-17821) In Kotlin's plugin KotlinJsMetadataVersionIndex loads file with VfsUtilCore.loadText
- [`KT-17840`](https://youtrack.jetbrains.com/issue/KT-17840) Show expression type on `this` shows bogus disambiguation
- [`KT-17845`](https://youtrack.jetbrains.com/issue/KT-17845) Searching for usages of override property in primary constructor doesn't suggest base property search
- [`KT-17847`](https://youtrack.jetbrains.com/issue/KT-17847) Kotlin facet: strange warning if API version = 1.2
- [`KT-17857`](https://youtrack.jetbrains.com/issue/KT-17857) Java should see classes affected by "allopen" plugin as open
- [`KT-17861`](https://youtrack.jetbrains.com/issue/KT-17861) Setting 'kotlin.experimental.coroutines "enable"' doesn't work for Android projects
- [`KT-17875`](https://youtrack.jetbrains.com/issue/KT-17875) New Project/Module with Kotlin: on attempt to use libraries from plugin IDE suggests to rewrite them
- [`KT-17876`](https://youtrack.jetbrains.com/issue/KT-17876) New Project/Module with Kotlin: with "Copy to" option only part of jars are copied
- [`KT-17899`](https://youtrack.jetbrains.com/issue/KT-17899) Navigate to symbol: vararg signatures are indistinguishable from non-vararg ones
- [`KT-18070`](https://youtrack.jetbrains.com/issue/KT-18070) KtLightModifierList.hasExplicitModifier("default") is true for interface method with body

### IDE. Completion

#### New Features

- [`KT-11250`](https://youtrack.jetbrains.com/issue/KT-11250) Auto-completion for convention function names in 'operator fun' definitions
- [`KT-12293`](https://youtrack.jetbrains.com/issue/KT-12293) Autocompletion should propose `lateinit var` in addition to `lateinit`
- [`KT-13673`](https://youtrack.jetbrains.com/issue/KT-13673) Add 'companion { ... }'  code completion opsion
#### Performance Improvements

- [`KT-10978`](https://youtrack.jetbrains.com/issue/KT-10978) Kotlin + JOOQ + Intellij performance is unusable 
- [`KT-16715`](https://youtrack.jetbrains.com/issue/KT-16715) Typing is very slow since 1.1
- [`KT-16850`](https://youtrack.jetbrains.com/issue/KT-16850) UI freeze for several seconds during inserting selected completion variant
#### Fixes

- [`KT-13524`](https://youtrack.jetbrains.com/issue/KT-13524) Completing the keyword 'constructor' before a primary constructor wrongly inserts parentheses
- [`KT-14665`](https://youtrack.jetbrains.com/issue/KT-14665) No completion for "else" keyword
- [`KT-15603`](https://youtrack.jetbrains.com/issue/KT-15603) Annoying completion when making a primary constructor private
- [`KT-16161`](https://youtrack.jetbrains.com/issue/KT-16161) Completion of 'onEach' inserts unneeded angular brackets
- [`KT-16856`](https://youtrack.jetbrains.com/issue/KT-16856) Code completion optimization

### IDE. Debugger

- [`KT-15823`](https://youtrack.jetbrains.com/issue/KT-15823) Breakpoints not work inside crossinline from init of object passed into collection
- [`KT-15854`](https://youtrack.jetbrains.com/issue/KT-15854) Debugger not able to ekonstuate internal member functions
- [`KT-16025`](https://youtrack.jetbrains.com/issue/KT-16025) Step into suspend functions stops at the function end
- [`KT-17295`](https://youtrack.jetbrains.com/issue/KT-17295) Can't stop in kotlin.concurrent.timer lambda parameter

### IDE. Inspections and Intentions

#### New Features

- [`KT-10981`](https://youtrack.jetbrains.com/issue/KT-10981) Quickfix for INAPPLICABLE_JVM_FIELD to replace with 'const' when possible
- [`KT-14046`](https://youtrack.jetbrains.com/issue/KT-14046) Add intention to add inline keyword if a function has parameter with noinline and/or crossinline modifier
- [`KT-14137`](https://youtrack.jetbrains.com/issue/KT-14137) Add intention to convert top level konst with object expression to object
- [`KT-15903`](https://youtrack.jetbrains.com/issue/KT-15903) QuickFix to add/remove suspend in hierarchies
- [`KT-16786`](https://youtrack.jetbrains.com/issue/KT-16786) Intention to add "open" modifier to a non-private method or property in an open class
- [`KT-16851`](https://youtrack.jetbrains.com/issue/KT-16851) Quickfix adding qualifier `@call` to unallowed 'return' in closures
- [`KT-17053`](https://youtrack.jetbrains.com/issue/KT-17053) Inspection to detect use of callable reference as a lambda body
- [`KT-17054`](https://youtrack.jetbrains.com/issue/KT-17054) Intention/ inspection to convert 'if' with 'is' check to 'as?' with safe call 
- [`KT-17191`](https://youtrack.jetbrains.com/issue/KT-17191) Intention to name anonymous (_) parameter
- [`KT-17221`](https://youtrack.jetbrains.com/issue/KT-17221) Inspection for recursive calls in property accessors
- [`KT-17520`](https://youtrack.jetbrains.com/issue/KT-17520) Quickfix to update language/API version should work for Maven projects
- [`KT-17650`](https://youtrack.jetbrains.com/issue/KT-17650) Add quickfix inserting 'lateinit' modifier for not-initialized property
- [`KT-17660`](https://youtrack.jetbrains.com/issue/KT-17660) Inspection: data class copy without named argument(s)
#### Fixes

- [`KT-10211`](https://youtrack.jetbrains.com/issue/KT-10211) "Replace infix call with ordinary call" appears both as a quickfix and as an intention in the pop-up
- [`KT-11003`](https://youtrack.jetbrains.com/issue/KT-11003) Inkonstid quickfix in companion object for open properties
- [`KT-12805`](https://youtrack.jetbrains.com/issue/KT-12805) False positive redundant semicolon after while without block expression
- [`KT-14335`](https://youtrack.jetbrains.com/issue/KT-14335) Unexpected range of "convert lambda to reference" intention
- [`KT-14435`](https://youtrack.jetbrains.com/issue/KT-14435) "Use destructuring declaration" should be available as intention even without usages
- [`KT-14443`](https://youtrack.jetbrains.com/issue/KT-14443) IDEA intention suggest to make a method in an interface final
- [`KT-14820`](https://youtrack.jetbrains.com/issue/KT-14820) Convert function to property shouldn't insert explicit type if it was inferred previously
- [`KT-15076`](https://youtrack.jetbrains.com/issue/KT-15076) Replace if with elvis inspection should not be reported in some complex cases
- [`KT-15543`](https://youtrack.jetbrains.com/issue/KT-15543) "Convert receiver to parameter" refactoring breaks code
- [`KT-15942`](https://youtrack.jetbrains.com/issue/KT-15942) "Convert to secondary constructor" intention is available for data class
- [`KT-16136`](https://youtrack.jetbrains.com/issue/KT-16136) Wrong type parameter variance suggested if type parameter is used in nested anonymous object
- [`KT-16339`](https://youtrack.jetbrains.com/issue/KT-16339) Incorrect warning: 'protected' visibility is effectively 'private' in a final class
- [`KT-16577`](https://youtrack.jetbrains.com/issue/KT-16577) "Redundant semicolon" is not reported for semicolon after package statement in file with no imports
- [`KT-17079`](https://youtrack.jetbrains.com/issue/KT-17079) Kotlin: Bad conversion of double comparison to range check if bounds have mixed types
- [`KT-17372`](https://youtrack.jetbrains.com/issue/KT-17372) Specify explicit lambda signature handles anonymous parameters incorrectly
- [`KT-17404`](https://youtrack.jetbrains.com/issue/KT-17404) Editor: attempt to pass type parameter as reified argument causes AE "Classifier descriptor of a type should be of type ClassDescriptor" at DescriptorUtils.getClassDescriptorForTypeConstructor()
- [`KT-17408`](https://youtrack.jetbrains.com/issue/KT-17408) "Convert to secondary constructor" intention is available for annotation parameters
- [`KT-17503`](https://youtrack.jetbrains.com/issue/KT-17503) Intention "To raw string literal" should handle string concatenations
- [`KT-17599`](https://youtrack.jetbrains.com/issue/KT-17599) "Make primary constructor internal" intention is available for annotation class 
- [`KT-17600`](https://youtrack.jetbrains.com/issue/KT-17600) "Make primary constructor private" intention is available for annotation class
- [`KT-17707`](https://youtrack.jetbrains.com/issue/KT-17707) "Final declaration can't be overridden at runtime" inspection reports Kotlin classes non final due to compiler plugin
- [`KT-17708`](https://youtrack.jetbrains.com/issue/KT-17708) "Move to class body" intention is available for annotation parameters
- [`KT-17762`](https://youtrack.jetbrains.com/issue/KT-17762) 'Convert to range' intention generates inequikonstent code for doubles

### IDE. Refactorings

#### Performance Improvements

- [`KT-17234`](https://youtrack.jetbrains.com/issue/KT-17234) Refactor / Inline on library property is rejected after GUI freeze for a while
- [`KT-17333`](https://youtrack.jetbrains.com/issue/KT-17333) KotlinChangeInfo retains 132MB of the heap
#### Fixes

- [`KT-8370`](https://youtrack.jetbrains.com/issue/KT-8370) "Can't move to original file" should not be an error
- [`KT-8930`](https://youtrack.jetbrains.com/issue/KT-8930) Refactor / Move preivew: moved element is shown as reference, and its file as subject
- [`KT-9158`](https://youtrack.jetbrains.com/issue/KT-9158) Refactor / Move preview mentions the package statement of moved class as a usage
- [`KT-13192`](https://youtrack.jetbrains.com/issue/KT-13192) Refactor / Move: to another class: "To" field code completion suggests facade and Java classes
- [`KT-13466`](https://youtrack.jetbrains.com/issue/KT-13466) Refactor / Move: class to upper level: the package statement is not updated
- [`KT-15519`](https://youtrack.jetbrains.com/issue/KT-15519) KDoc comments for data class konstues get removed by Change Signature
- [`KT-17211`](https://youtrack.jetbrains.com/issue/KT-17211) Refactor / Move several files: superfluous FQN is inserted into reference to same file's element
- [`KT-17213`](https://youtrack.jetbrains.com/issue/KT-17213) Refactor / Inline Function: parameters of lambda as call argument turn incompilable
- [`KT-17272`](https://youtrack.jetbrains.com/issue/KT-17272) Refactor / Inline Function: unused String literal in parameters is kept (while unsed Int is not)
- [`KT-17273`](https://youtrack.jetbrains.com/issue/KT-17273) Refactor / Inline Function: PIEAE: "Element: class org.jetbrains.kotlin.psi.KtCallExpression because: different providers" at PsiUtilCore.ensureValid()
- [`KT-17296`](https://youtrack.jetbrains.com/issue/KT-17296) Refactor / Inline Function: UOE at ExpressionReplacementPerformer.findOrCreateBlockToInsertStatement() for call of multi-statement function in declaration
- [`KT-17330`](https://youtrack.jetbrains.com/issue/KT-17330) Inline kotlin function causes an infinite loop
- [`KT-17395`](https://youtrack.jetbrains.com/issue/KT-17395) Refactor / Inline Function: arguments passed to lambda turns code to incompilable
- [`KT-17496`](https://youtrack.jetbrains.com/issue/KT-17496) Refactor / Move: calls to moved extension function type properties are updated (incorrectly)
- [`KT-17515`](https://youtrack.jetbrains.com/issue/KT-17515) Refactor / Move inner class to another class, Move companion object: disabled in editor, but available in Move dialog
- [`KT-17526`](https://youtrack.jetbrains.com/issue/KT-17526) Refactor / Move: reference to companion member gets superfluous companion name in certain cases
- [`KT-17538`](https://youtrack.jetbrains.com/issue/KT-17538) Refactor / Move: moving file with import alias removes alias usage from code
- [`KT-17545`](https://youtrack.jetbrains.com/issue/KT-17545) Refactor / Move: false Problems Detected on moving class using parent's protected class, object
- [`KT-18018`](https://youtrack.jetbrains.com/issue/KT-18018) F5 (for Copy) does not work for Kotlin files anymore
- [`KT-18205`](https://youtrack.jetbrains.com/issue/KT-18205) Moving multiple classes causes imports to be converted to fully qualified class names

### Infrastructure

- [`KT-14988`](https://youtrack.jetbrains.com/issue/KT-14988) Support running the Kotlin compiler on Java 9
- [`KT-17112`](https://youtrack.jetbrains.com/issue/KT-17112) IncompatibleClassChangeError on invoking Kotlin compiler daemon on JDK 9

### JavaScript

#### Fixes

- [`KT-12926`](https://youtrack.jetbrains.com/issue/KT-12926) JS: use # instead of @ when linking to sourcemap from generated code
- [`KT-13577`](https://youtrack.jetbrains.com/issue/KT-13577) Double.hashCode is zero for big numbers
- [`KT-15135`](https://youtrack.jetbrains.com/issue/KT-15135) JS: support friend modules
- [`KT-15484`](https://youtrack.jetbrains.com/issue/KT-15484) JS: (node): println with object /number argument leads to "TypeError: Inkonstid data, chunk must be a string or buffer, not object/number"
- [`KT-16658`](https://youtrack.jetbrains.com/issue/KT-16658) JS: Suspend function with default param konstue in interface
- [`KT-16717`](https://youtrack.jetbrains.com/issue/KT-16717) KotlinJs - copy() on data class doesn't work with when there is a secondary constructor
- [`KT-16745`](https://youtrack.jetbrains.com/issue/KT-16745) JS: initialize enum fields before calling companion objects's initializer
- [`KT-16951`](https://youtrack.jetbrains.com/issue/KT-16951) JS: coroutine suspension point is not inserted when inlining suspend function with tail call to another suspend function
- [`KT-16979`](https://youtrack.jetbrains.com/issue/KT-16979) Kotlin.js : Intellij test and productions sources produce a AMD module with the same name
- [`KT-17067`](https://youtrack.jetbrains.com/issue/KT-17067) JS: suspendCoroutine not working as expected
- [`KT-17219`](https://youtrack.jetbrains.com/issue/KT-17219) Hexadecimal literals in js(...) argument are replaced by wrong decimal constants
- [`KT-17281`](https://youtrack.jetbrains.com/issue/KT-17281) JS: wrong code generated for a recursive call in suspend function
- [`KT-17446`](https://youtrack.jetbrains.com/issue/KT-17446) JS: incorrect code generated for call to `suspendCoroutineOrReturn` when the same function calls another suspend function
- [`KT-17540`](https://youtrack.jetbrains.com/issue/KT-17540) Incorrect inlining optimization of `also`/`apply` function
- [`KT-17700`](https://youtrack.jetbrains.com/issue/KT-17700) Wrong code generated for 'str += (nullableChar ?: break)'
- [`KT-17966`](https://youtrack.jetbrains.com/issue/KT-17966) JS: Char literal inside of string template

### Libraries

- [`KT-17453`](https://youtrack.jetbrains.com/issue/KT-17453) Array iterators throw IndexOutOfBoundsException instead of NoSuchElementException
- [`KT-17635`](https://youtrack.jetbrains.com/issue/KT-17635) Document String#toIntOfNull may throw an exception
- [`KT-17686`](https://youtrack.jetbrains.com/issue/KT-17686) takeLast(n) incorrectly performs drop(n) for Lists without random access
- [`KT-17704`](https://youtrack.jetbrains.com/issue/KT-17704) Update JavaDoc for ReentrantReadWriteLock.write to put more stress on the fact that to upgrade to write lock, read lock is first released.
- [`KT-17853`](https://youtrack.jetbrains.com/issue/KT-17853) JS: Confusing parameter names in 'Math.atan2`
- [`KT-18092`](https://youtrack.jetbrains.com/issue/KT-18092) Issue using kotlin-reflect with proguard: missing annotations Mutable and ReadOnly
- [`KT-18210`](https://youtrack.jetbrains.com/issue/KT-18210) JS String::match(regex) should have nullable return type

### Reflection

- [`KT-17055`](https://youtrack.jetbrains.com/issue/KT-17055) NPE in hashCode and equals of kotlin.jvm.internal.FunctionReference (on local functions)
- [`KT-17594`](https://youtrack.jetbrains.com/issue/KT-17594) Cache the result of konst Class<T>.kotlin: KClass<T>
- [`KT-18494`](https://youtrack.jetbrains.com/issue/KT-18494) KNPE from Kotlin reflection (sometimes) in UtilKt.toJavaClass

### Tools

- [`KT-16692`](https://youtrack.jetbrains.com/issue/KT-16692) No-Arg-Constructor plugin should generate code to initialize delegates

### Tools. CLI

- [`KT-17696`](https://youtrack.jetbrains.com/issue/KT-17696) Allow kotlinc to take friend modules as .jar files
- [`KT-17697`](https://youtrack.jetbrains.com/issue/KT-17697) Allow kotlinc to take .java files as arguments
- [`KT-9370`](https://youtrack.jetbrains.com/issue/KT-9370) not possible to pass an argument that starts with "-" to a script using kotlinc
- [`KT-17100`](https://youtrack.jetbrains.com/issue/KT-17100) "kotlin" launcher script: do not add current working directory to classpath if explicit "-classpath" is specified
- [`KT-17140`](https://youtrack.jetbrains.com/issue/KT-17140) Warning "classpath entry points to a file that is not a jar file" could just be disabled
- [`KT-17264`](https://youtrack.jetbrains.com/issue/KT-17264) Change the format of advanced CLI arguments ("-X...") to require konstue after "=", not a whitespace
- [`KT-18180`](https://youtrack.jetbrains.com/issue/KT-18180) Modules not exported by java.se are not readable when compiling against JRE 9

### Tools. Gradle

- [`KT-15151`](https://youtrack.jetbrains.com/issue/KT-15151) Kapt3: Support incremental compilation of Java stubs
- [`KT-16298`](https://youtrack.jetbrains.com/issue/KT-16298) Gradle: IOException "Parent file doesn't exist:/.../artifact-difference.tab.len" on non-incremental clean after incremental build
- [`KT-17681`](https://youtrack.jetbrains.com/issue/KT-17681) Support the new API of Android Gradle plugin (2.4.0+)
- [`KT-17936`](https://youtrack.jetbrains.com/issue/KT-17936) Circular dependency between gradle tasks dataBindingExportBuildInfoDebug and compileDebugKotlin
- [`KT-17960`](https://youtrack.jetbrains.com/issue/KT-17960) Improve test of memory leak with Gradle daemon
- [`KT-18047`](https://youtrack.jetbrains.com/issue/KT-18047) Gradle kotlin options should use unset konstue as default for languageVersion and apiVersion

### Tools. J2K

- [`KT-16754`](https://youtrack.jetbrains.com/issue/KT-16754) J2K: Apply quick-fixes from EDT thread only
- [`KT-16816`](https://youtrack.jetbrains.com/issue/KT-16816) Java To Kotlin bug: if + chained assignment doesn't include brackets
- [`KT-17230`](https://youtrack.jetbrains.com/issue/KT-17230) J2K Deadlock
- [`KT-17712`](https://youtrack.jetbrains.com/issue/KT-17712) Exception in J2K during InlineCodegen convertion: com.intellij.psi.impl.source.JavaDummyHolder cannot be cast to com.intellij.psi.PsiJavaFile

### Tools. JPS

- [`KT-16568`](https://youtrack.jetbrains.com/issue/KT-16568) modulesWhoseInternalsAreVisible in ModuleDependencies are not filled in for JS projects
- [`KT-17387`](https://youtrack.jetbrains.com/issue/KT-17387) When compiling in the IDE, progress tracker says "configuring the compilation environment" when it clearly isn't
- [`KT-17665`](https://youtrack.jetbrains.com/issue/KT-17665) JPS: Kotlin: The '-d' option with a directory destination is ignored because '-module' is specified
- [`KT-17801`](https://youtrack.jetbrains.com/issue/KT-17801) Unresolved supertypes from JRE on JDK 9 in JPS

### Tools. Maven

- [`KT-17093`](https://youtrack.jetbrains.com/issue/KT-17093) Import from maven: please provide a special tag for coroutine option
- [`KT-10028`](https://youtrack.jetbrains.com/issue/KT-10028) Support parallel builds in maven
- [`KT-15050`](https://youtrack.jetbrains.com/issue/KT-15050) Random build failures using maven 3 (multi-thread) + bamboo
- [`KT-15318`](https://youtrack.jetbrains.com/issue/KT-15318) Intermitent Kotlin compilation errors
- [`KT-16283`](https://youtrack.jetbrains.com/issue/KT-16283) Maven compiler plugin warns, "Source root doesn't exist"
- [`KT-16743`](https://youtrack.jetbrains.com/issue/KT-16743) Update configuration options in Kotlin Maven plugin
- [`KT-16762`](https://youtrack.jetbrains.com/issue/KT-16762) Maven: JS compiler option main is missing

### Tools. REPL

- [`KT-5822`](https://youtrack.jetbrains.com/issue/KT-5822) Exception on package directive in REPL
- [`KT-10060`](https://youtrack.jetbrains.com/issue/KT-10060) REPL: Cannot execute more than 255 lines
- [`KT-17365`](https://youtrack.jetbrains.com/issue/KT-17365) REPL crash when referencing a variable whose definition threw an exception

### Tools. kapt

- [`KT-17245`](https://youtrack.jetbrains.com/issue/KT-17245) Kapt: Javac compiler arguments can't be specified in Gradle
- [`KT-17418`](https://youtrack.jetbrains.com/issue/KT-17418) "The following options were not recognized by any processor: '[kapt.kotlin.generated]'" warning from Javac shouldn't be shown even if no processor supports the generated annotation
- [`KT-17456`](https://youtrack.jetbrains.com/issue/KT-17456) kapt3: NoClassDefFound com/sun/tools/javac/util/Context
- [`KT-17567`](https://youtrack.jetbrains.com/issue/KT-17567) Kapt (1.1.2-eap-77) generates inkonstid Java stub for internal class
- [`KT-17620`](https://youtrack.jetbrains.com/issue/KT-17620) Kapt3 IC: avoid running AP when API is not changed
- [`KT-17959`](https://youtrack.jetbrains.com/issue/KT-17959) Kapt3 doesn't preserve method parameter names for abstract methods
- [`KT-17999`](https://youtrack.jetbrains.com/issue/KT-17999) Cannot use KAPT3 1.1.2-4 in Android Studio java libs (null TypeCastException to WrappedVariantData<*> on Gradle Sync)

## 1.1.2

### Compiler

#### Front-end

- [`KT-16113`](https://youtrack.jetbrains.com/issue/KT-16113) Support destructuring parameters of suspend lambda with suspend componentX
- [`KT-3805`](https://youtrack.jetbrains.com/issue/KT-3805) Report error on double constants out of range
- [`KT-6014`](https://youtrack.jetbrains.com/issue/KT-6014) Wrong ABSTRACT_MEMBER_NOT_IMPLEMENTED for toString implemented by delegation
- [`KT-8959`](https://youtrack.jetbrains.com/issue/KT-8959) Missing diagnostic when trying to call inner class constructor qualificated with outer class name
- [`KT-12477`](https://youtrack.jetbrains.com/issue/KT-12477) Do not report 'const' inapplicability on property of error type
- [`KT-11010`](https://youtrack.jetbrains.com/issue/KT-11010) NDFDE for local object with type parameters
- [`KT-12881`](https://youtrack.jetbrains.com/issue/KT-12881) Descriptor wasn't found for declaration TYPE_PARAMETER
- [`KT-13342`](https://youtrack.jetbrains.com/issue/KT-13342) Unqualified super call should not resolve to a method of supertype overriden in another supertype
- [`KT-14236`](https://youtrack.jetbrains.com/issue/KT-14236) Allow to use emptyArray in annotation
- [`KT-14536`](https://youtrack.jetbrains.com/issue/KT-14536) IllegalStateException: Type parameter T not found for lazy class Companion at LazyDeclarationResolver visitTypeParameter
- [`KT-14865`](https://youtrack.jetbrains.com/issue/KT-14865) Throwable exception at KotlinParser parseLambdaExpression on typing { inside a string inside a lambda
- [`KT-15516`](https://youtrack.jetbrains.com/issue/KT-15516) Compiler error when passing suspending extension-functions as parameter and casting stuff to Any
- [`KT-15802`](https://youtrack.jetbrains.com/issue/KT-15802) Java constant referenced using subclass is not considered a constant expression
- [`KT-15872`](https://youtrack.jetbrains.com/issue/KT-15872) Constant folding is mistakenly triggered for user function
- [`KT-15901`](https://youtrack.jetbrains.com/issue/KT-15901) Unstable smart cast target after type check
- [`KT-15951`](https://youtrack.jetbrains.com/issue/KT-15951) Callable reference to class constructor from object is not resolved
- [`KT-16232`](https://youtrack.jetbrains.com/issue/KT-16232) Prohibit objects inside inner classes
- [`KT-16233`](https://youtrack.jetbrains.com/issue/KT-16233) Prohibit inner sealed classes
- [`KT-16250`](https://youtrack.jetbrains.com/issue/KT-16250) Import methods from typealias to object throws compiler exception  "Should be class or package: typealias"
- [`KT-16272`](https://youtrack.jetbrains.com/issue/KT-16272) Missing deprecation and SinceKotlin-related diagnostic for variable as function call
- [`KT-16278`](https://youtrack.jetbrains.com/issue/KT-16278) Public member method can't be used for callable reference because of private static with the same name
- [`KT-16372`](https://youtrack.jetbrains.com/issue/KT-16372) 'mod is deprecated' warning should not be shown when language version is 1.0
- [`KT-16484`](https://youtrack.jetbrains.com/issue/KT-16484) SimpleTypeImpl should not be created for error type: ErrorScope
- [`KT-16528`](https://youtrack.jetbrains.com/issue/KT-16528) Error: Loop in supertypes when using Java classes with type parameters having raw interdependent supertypes
- [`KT-16538`](https://youtrack.jetbrains.com/issue/KT-16538) No smart cast when equals is present
- [`KT-16782`](https://youtrack.jetbrains.com/issue/KT-16782) Enum entry is incorrectly forbidden on LHS of '::' with language version 1.0
- [`KT-16815`](https://youtrack.jetbrains.com/issue/KT-16815) Assertion error from compiler: unexpected classifier: class DeserializedTypeAliasDescriptor
- [`KT-16931`](https://youtrack.jetbrains.com/issue/KT-16931) Compiler cannot see inner class when for outer class exist folder with same name
- [`KT-16956`](https://youtrack.jetbrains.com/issue/KT-16956) Prohibit using function calls inside default parameter konstues of annotations
- [`KT-8187`](https://youtrack.jetbrains.com/issue/KT-8187) IAE on anonymous object in the delegation specifier list
- [`KT-8813`](https://youtrack.jetbrains.com/issue/KT-8813) Do not report unused parameters for anonymous functions
- [`KT-12112`](https://youtrack.jetbrains.com/issue/KT-12112) Do not consider nullability of error functions and properties for smart casts
- [`KT-12276`](https://youtrack.jetbrains.com/issue/KT-12276) No warning for unnecessary non-null assertion after method call with generic return type
- [`KT-13648`](https://youtrack.jetbrains.com/issue/KT-13648) Spurious warning: "Elvis operator (?:) always returns the left operand of non-nullable type (???..???)"
- [`KT-16264`](https://youtrack.jetbrains.com/issue/KT-16264) Forbid usage of _ without backticks
- [`KT-16875`](https://youtrack.jetbrains.com/issue/KT-16875) Decrease severity of unused parameter in lambda to weak warning
- [`KT-17136`](https://youtrack.jetbrains.com/issue/KT-17136) ModuleDescriptorImpl.allImplementingModules should be ekonstuated lazily
- [`KT-17214`](https://youtrack.jetbrains.com/issue/KT-17214) Do not show warning about useless elvis for error function types
- [`KT-13740`](https://youtrack.jetbrains.com/issue/KT-13740) Plugin crashes at accidentally wrong annotation argument type
- [`KT-17597`](https://youtrack.jetbrains.com/issue/KT-17597) Pattern::compile resolves to private instance method in 1.1.2

#### Back-end

- [`KT-8689`](https://youtrack.jetbrains.com/issue/KT-8689) NoSuchMethodError on local functions inside inlined lambda with variables captured from outer context
- [`KT-11314`](https://youtrack.jetbrains.com/issue/KT-11314) Abstract generic class with Array<Array<T>> parameter compiles fine but fails at runtime with "Bad type on operand stack" VerifyError
- [`KT-12839`](https://youtrack.jetbrains.com/issue/KT-12839) Two null checks are generated when manually null checking platform type
- [`KT-14565`](https://youtrack.jetbrains.com/issue/KT-14565) Cannot pop operand off empty stack when compiling enum class
- [`KT-14566`](https://youtrack.jetbrains.com/issue/KT-14566) Make kotlin.jvm.internal.Ref$...Ref classes serializable
- [`KT-14567`](https://youtrack.jetbrains.com/issue/KT-14567) VerifyError: Bad type on operand stack (generics with operator methods)
- [`KT-14607`](https://youtrack.jetbrains.com/issue/KT-14607) Incorrect class name "ava/lang/Void from AsyncTask extension function
- [`KT-14811`](https://youtrack.jetbrains.com/issue/KT-14811) Unecessary checkcast generated in parameterized functions.
- [`KT-14963`](https://youtrack.jetbrains.com/issue/KT-14963) unnecessary checkcast java/lang/Object
- [`KT-15105`](https://youtrack.jetbrains.com/issue/KT-15105) Comparing Chars in a Pair results in ClassCastException
- [`KT-15109`](https://youtrack.jetbrains.com/issue/KT-15109) Subclass from a type alias with named parameter in constructor will produce compiler exception
- [`KT-15192`](https://youtrack.jetbrains.com/issue/KT-15192) Compiler crashes on certain companion objects: "Error generating constructors of class Companion with kind IMPLEMENTATION"
- [`KT-15424`](https://youtrack.jetbrains.com/issue/KT-15424) javac crash when calling Kotlin function having generic varargs with default and @JvmOverloads
- [`KT-15574`](https://youtrack.jetbrains.com/issue/KT-15574) Can't instantiate Array through Type Alias
- [`KT-15594`](https://youtrack.jetbrains.com/issue/KT-15594) java.lang.VerifyError when referencing normal getter in @JvmStatic getters inside an object
- [`KT-15759`](https://youtrack.jetbrains.com/issue/KT-15759) tailrec suspend function fails to compile
- [`KT-15862`](https://youtrack.jetbrains.com/issue/KT-15862) Inline generic functions can unexpectedly box primitives
- [`KT-15871`](https://youtrack.jetbrains.com/issue/KT-15871) Unnecessary boxing for equality operator on inlined primitive konstues
- [`KT-15993`](https://youtrack.jetbrains.com/issue/KT-15993) Property annotations are stored in private fields and killed by obfuscators
- [`KT-15997`](https://youtrack.jetbrains.com/issue/KT-15997) Reified generics don't work properly with crossinline functions
- [`KT-16077`](https://youtrack.jetbrains.com/issue/KT-16077) Redundant private getter for private var in a class within a JvmMultifileClass annotated file
- [`KT-16194`](https://youtrack.jetbrains.com/issue/KT-16194) Code with unnecessary safe call contains redundant boxing/unboxing for primitive konstues
- [`KT-16245`](https://youtrack.jetbrains.com/issue/KT-16245) Redundant null-check generated for a cast of already non-nullable konstue
- [`KT-16532`](https://youtrack.jetbrains.com/issue/KT-16532) Kotlin 1.1 RC - Android cross-inline synchronized won't run
- [`KT-16555`](https://youtrack.jetbrains.com/issue/KT-16555) VerifyError: Bad type on operand stack
- [`KT-16713`](https://youtrack.jetbrains.com/issue/KT-16713) Insufficient maximum stack size
- [`KT-16720`](https://youtrack.jetbrains.com/issue/KT-16720) ClassCastException during compilation
- [`KT-16732`](https://youtrack.jetbrains.com/issue/KT-16732) Type 'java/lang/Number' (current frame, stack[0]) is not assignable to 'java/lang/Character
- [`KT-16929`](https://youtrack.jetbrains.com/issue/KT-16929) `VerifyError` when using bound method reference on generic property
- [`KT-16412`](https://youtrack.jetbrains.com/issue/KT-16412) Exception from compiler when try call SAM constructor where argument is callable reference to nested class inside object
- [`KT-17210`](https://youtrack.jetbrains.com/issue/KT-17210) Smartcast failure results in "Bad type operand on stack" runtime error

### Tools

- [`KT-15420`](https://youtrack.jetbrains.com/issue/KT-15420) Maven, all-open plugin: in console the settings of all-open are always reported as empty
- [`KT-11916`](https://youtrack.jetbrains.com/issue/KT-11916) Provide incremental compilation for Maven
- [`KT-15946`](https://youtrack.jetbrains.com/issue/KT-15946) Kotlin-JPA plugin support for @Embeddable
- [`KT-16627`](https://youtrack.jetbrains.com/issue/KT-16627) Do not make private members open in all-open plugin
- [`KT-16699`](https://youtrack.jetbrains.com/issue/KT-16699) Script resolving doesn't work with custom templates located in an external jar
- [`KT-16812`](https://youtrack.jetbrains.com/issue/KT-16812) import in .kts file does not works
- [`KT-16927`](https://youtrack.jetbrains.com/issue/KT-16927) Using `KotlinJsr223JvmLocalScriptEngineFactory` causes multiple warnings
- [`KT-15562`](https://youtrack.jetbrains.com/issue/KT-15562) Service is dying
- [`KT-17125`](https://youtrack.jetbrains.com/issue/KT-17125) > Failed to apply plugin [id 'kotlin'] > For input string: “”

#### Kapt

- [`KT-12432`](https://youtrack.jetbrains.com/issue/KT-12432) Dagger 2 does not generate Component which was referenced from Kotlin file.
- [`KT-8558`](https://youtrack.jetbrains.com/issue/KT-8558) KAPT only works with service-declared annotation processors
- [`KT-16753`](https://youtrack.jetbrains.com/issue/KT-16753) kapt3 generates inkonstid stubs when IC is enabled
- [`KT-16458`](https://youtrack.jetbrains.com/issue/KT-16458) kotlin-kapt / kapt3:  "cannot find symbol" error for companion object with same name as enclosing class
- [`KT-14478`](https://youtrack.jetbrains.com/issue/KT-14478) Add APT / Kapt support to the maven plugin
- [`KT-14070`](https://youtrack.jetbrains.com/issue/KT-14070) Kapt3: kapt doesn't compile generated Kotlin files and doesn't use the "kapt.kotlin.generated" folder anymore
- [`KT-16990`](https://youtrack.jetbrains.com/issue/KT-16990) Kapt3: java.io.File cannot be cast to java.lang.String
- [`KT-16965`](https://youtrack.jetbrains.com/issue/KT-16965) Error:Kotlin: Multiple konstues are not allowed for plugin option org.jetbrains.kotlin.kapt:output
- [`KT-16184`](https://youtrack.jetbrains.com/issue/KT-16184) AbstractMethodError in Kapt3ComponentRegistrar while compiling from IntelliJ  2016.3.4 using Kotlin 1.1.0-beta-38

#### Gradle

- [`KT-15084`](https://youtrack.jetbrains.com/issue/KT-15084) Navigation into sources of gradle-script-kotlin doesn't work
- [`KT-16003`](https://youtrack.jetbrains.com/issue/KT-16003) Gradle Plugin Fails When Run From Jenkins On Multiple Nodes
- [`KT-16585`](https://youtrack.jetbrains.com/issue/KT-16585) Kotlin Gradle Plugin makes using Gradle Java incremental compiler not work
- [`KT-16902`](https://youtrack.jetbrains.com/issue/KT-16902) Gradle plugin compilation on daemon fails on Linux ARM
- [`KT-14619`](https://youtrack.jetbrains.com/issue/KT-14619) Gradle: The '-d' option with a directory destination is ignored because '-module' is specified
- [`KT-12792`](https://youtrack.jetbrains.com/issue/KT-12792) Automatically configure standard library dependency and set its version equal to compiler version if not specified
- [`KT-15994`](https://youtrack.jetbrains.com/issue/KT-15994) Compiler arguments are not copied from the main compile task to kapt task
- [`KT-16820`](https://youtrack.jetbrains.com/issue/KT-16820) Changing compileKotlin.destinationDir leads to failure in :copyMainKotlinClasses task due to an NPE
- [`KT-16917`](https://youtrack.jetbrains.com/issue/KT-16917) First connection to daemon after start timeouts when DNS is slow
- [`KT-16580`](https://youtrack.jetbrains.com/issue/KT-16580) Kotlin gradle plugin cannot resolve the kotlin compiler 

### Android support

- [`KT-16624`](https://youtrack.jetbrains.com/issue/KT-16624) Implement quickfix "Add TargetApi/RequiresApi annotation" for Android api issues
- [`KT-16625`](https://youtrack.jetbrains.com/issue/KT-16625) Implement quickfix "Surround with if (VERSION.SDK_INT >= VERSION_CODES.SOME_VERSION) { ... }" for Android api issues
- [`KT-16840`](https://youtrack.jetbrains.com/issue/KT-16840) Kotlin Gradle plugin fails with Android Gradle plugin 2.4.0-alpha1
- [`KT-16897`](https://youtrack.jetbrains.com/issue/KT-16897) Gradle plugin 1.1.1 duplicates all main classes into Android instrumentation test APK
- [`KT-16957`](https://youtrack.jetbrains.com/issue/KT-16957) Android Extensions: Support Dialog class
- [`KT-15023`](https://youtrack.jetbrains.com/issue/KT-15023) Android `gradle installDebugAndroidTest` fails unless you first call `gradle assembleDebugAndroidTest`
- [`KT-12769`](https://youtrack.jetbrains.com/issue/KT-12769) "Name for method must be provided" error occurs on trying to use spaces in method name in integration tests in Android
- [`KT-12819`](https://youtrack.jetbrains.com/issue/KT-12819) Kotlin Lint: False positive for "Unconditional layout inflation" when using elvis operator
- [`KT-15116`](https://youtrack.jetbrains.com/issue/KT-15116) Kotlin Lint: problems in property accessors are not reported
- [`KT-15156`](https://youtrack.jetbrains.com/issue/KT-15156) Kotlin Lint: problems in annotation parameters are not reported
- [`KT-15179`](https://youtrack.jetbrains.com/issue/KT-15179) Kotlin Lint: problems inside local function are not reported
- [`KT-14870`](https://youtrack.jetbrains.com/issue/KT-14870) Kotlin Lint: problems inside local class are not reported
- [`KT-14920`](https://youtrack.jetbrains.com/issue/KT-14920) Kotlin Lint: "Android Lint for Kotlin | Incorrect support annotation usage" inspection does not report problems
- [`KT-14947`](https://youtrack.jetbrains.com/issue/KT-14947) Kotlin Lint: "Calling new methods on older versions" could suggest specific quick fixes
- [`KT-12741`](https://youtrack.jetbrains.com/issue/KT-12741) Android Extensions: Enable IDE plugin only if it is enabled in the build.gradle file
- [`KT-13122`](https://youtrack.jetbrains.com/issue/KT-13122) Implement '@RequiresApi' intention for android and don't report warning on annotated classes
- [`KT-16680`](https://youtrack.jetbrains.com/issue/KT-16680) Stack overflow in UAST containsLocalTypes()
- [`KT-15451`](https://youtrack.jetbrains.com/issue/KT-15451) Support "Android String Reference" folding in Kotlin files
- [`KT-16132`](https://youtrack.jetbrains.com/issue/KT-16132) Renaming property provided by kotlinx leads to renaming another members
- [`KT-17200`](https://youtrack.jetbrains.com/issue/KT-17200) Unable to build an android project
- [`KT-13104`](https://youtrack.jetbrains.com/issue/KT-13104) Incorrect resource name in Activity after renaming ID attribute konstue in layout file
- [`KT-17436`](https://youtrack.jetbrains.com/issue/KT-17436) Refactor | Rename android:id corrupts R.id references in kotlin code
- [`KT-17255`](https://youtrack.jetbrains.com/issue/KT-17255) Kotlin 1.1.2 EAP is broken with 2.4.0-alpha3
- [`KT-17610`](https://youtrack.jetbrains.com/issue/KT-17610) "Unknown reference: kotlinx"

### IDE

- [`KT-6159`](https://youtrack.jetbrains.com/issue/KT-6159) Inline Method refactoring
- [`KT-4578`](https://youtrack.jetbrains.com/issue/KT-4578) Intention to move property between class body and constructor parameter
- [`KT-8568`](https://youtrack.jetbrains.com/issue/KT-8568) Provide a QuickFix to replace type `Array<Int>` in annotation with `IntArray`
- [`KT-10393`](https://youtrack.jetbrains.com/issue/KT-10393) Detect calls to functions returning a lambda from expression body which ignore the return konstue
- [`KT-11393`](https://youtrack.jetbrains.com/issue/KT-11393) Inspection to highlight and warn on usage of internal members in other module from Java
- [`KT-12004`](https://youtrack.jetbrains.com/issue/KT-12004) IDE inspection that destructuring variable name matches the other name in data class
- [`KT-12183`](https://youtrack.jetbrains.com/issue/KT-12183) Intention converting several calls with same receiver to 'with'/`apply`/`run`
- [`KT-13111`](https://youtrack.jetbrains.com/issue/KT-13111) Support bound references in lambda-to-reference intention / inspection
- [`KT-15966`](https://youtrack.jetbrains.com/issue/KT-15966) Create quickfix for DELEGATED_MEMBER_HIDES_SUPERTYPE_OVERRIDE
- [`KT-16074`](https://youtrack.jetbrains.com/issue/KT-16074) Introduce a quick-fix adding noinline modifier for a konstue parameter of suspend function type 
- [`KT-16131`](https://youtrack.jetbrains.com/issue/KT-16131) Add quickfix for: Cannot access member: it is invisible (private in supertype)
- [`KT-16188`](https://youtrack.jetbrains.com/issue/KT-16188) Add create class quickfix
- [`KT-16258`](https://youtrack.jetbrains.com/issue/KT-16258) Add intention to add missing components to destructuring assignment
- [`KT-16292`](https://youtrack.jetbrains.com/issue/KT-16292) Support "Reference to lambda" for bound references
- [`KT-11234`](https://youtrack.jetbrains.com/issue/KT-11234) Debugger won't hit breakpoint in nested lamba
- [`KT-12002`](https://youtrack.jetbrains.com/issue/KT-12002) Improve completion for closure parameters to work in more places
- [`KT-15768`](https://youtrack.jetbrains.com/issue/KT-15768) It would be nice to show in Kotlin facet what compiler plugins are on and their options
- [`KT-16022`](https://youtrack.jetbrains.com/issue/KT-16022) Kotlin facet: provide UI to navigate to project settings
- [`KT-16214`](https://youtrack.jetbrains.com/issue/KT-16214) Do not hide package kotlin.reflect.jvm.internal from auto-import and completion, inside package "kotlin.reflect"
- [`KT-16647`](https://youtrack.jetbrains.com/issue/KT-16647) Don't create kotlinc.xml if the settings don't differ from the defaults
- [`KT-16649`](https://youtrack.jetbrains.com/issue/KT-16649) All Gradle related classes should be moved to optional dependency section of plugin.xml
- [`KT-16800`](https://youtrack.jetbrains.com/issue/KT-16800) Autocomplete for closure with single arguments

#### Bug fixes

- [`KT-16316`](https://youtrack.jetbrains.com/issue/KT-16316) IDE: don't show Kotlin Scripting section when target platform is JavaScript
- [`KT-16317`](https://youtrack.jetbrains.com/issue/KT-16317) IDE: some fields stay enabled in an facet when use project settings was chosen
- [`KT-16596`](https://youtrack.jetbrains.com/issue/KT-16596) Hang in IntelliJ while scanning zips
- [`KT-16646`](https://youtrack.jetbrains.com/issue/KT-16646) The flag to enable coroutines does not sync from gradle file in Android Studio
- [`KT-16788`](https://youtrack.jetbrains.com/issue/KT-16788) Importing Kotlin Maven projects results in inkonstid .iml
- [`KT-16827`](https://youtrack.jetbrains.com/issue/KT-16827) kotlin javascript module not recognized by gradle sync when an android module is present
- [`KT-16848`](https://youtrack.jetbrains.com/issue/KT-16848) Regression: completion after dot in string interpolation expression doesn't work if there are no curly braces
- [`KT-16888`](https://youtrack.jetbrains.com/issue/KT-16888) "Multiple konstues are not allowed for plugin option org.jetbrains.kotlin.android:package" when rebuilding project
- [`KT-16980`](https://youtrack.jetbrains.com/issue/KT-16980) Accessing language version settings for a module performs runtime version detection on every access with no caching
- [`KT-16991`](https://youtrack.jetbrains.com/issue/KT-16991) Navigate to receiver from this in extension function
- [`KT-16992`](https://youtrack.jetbrains.com/issue/KT-16992) Navigate to lambda start from auto-generated 'it' parameter 
- [`KT-12264`](https://youtrack.jetbrains.com/issue/KT-12264) AssertionError: Resolver for 'completion/highlighting in LibrarySourceInfo for platform JVM' does not know how to resolve ModuleProductionSourceInfo
- [`KT-13734`](https://youtrack.jetbrains.com/issue/KT-13734) Annotated element search is slow
- [`KT-14710`](https://youtrack.jetbrains.com/issue/KT-14710) Sample references aren't resolved in IDE
- [`KT-16415`](https://youtrack.jetbrains.com/issue/KT-16415) Dependency leakage with Kotlin IntelliJ plugin, using gradle-script-kotlin, and the gradle-intellij-plugin
- [`KT-16837`](https://youtrack.jetbrains.com/issue/KT-16837) Slow typing in Kotlin file because of ImportFixBase
- [`KT-16926`](https://youtrack.jetbrains.com/issue/KT-16926) 'implement' dependency is not transitive when importing gradle project to IDEA
- [`KT-17141`](https://youtrack.jetbrains.com/issue/KT-17141) Running test from gutter icon fails in AS 2.4 Preview 3
- [`KT-17162`](https://youtrack.jetbrains.com/issue/KT-17162) Plain-text Java copy-paste to Kotlin file results in exception
- [`KT-16714`](https://youtrack.jetbrains.com/issue/KT-16714) J2K: Write access is allowed from event dispatch thread only
- [`KT-14058`](https://youtrack.jetbrains.com/issue/KT-14058) Unexpected error MISSING_DEPENDENCY_CLASS
- [`KT-9275`](https://youtrack.jetbrains.com/issue/KT-9275) Unhelpful IDE warning "Configure Kotlin"
- [`KT-15279`](https://youtrack.jetbrains.com/issue/KT-15279) 'Kotlin not configured message' should not be displayed while gradle sync is in progress
- [`KT-11828`](https://youtrack.jetbrains.com/issue/KT-11828) Configure Kotlin in Project: failure for Gradle modules without build.gradle (IDEA creates them)
- [`KT-16571`](https://youtrack.jetbrains.com/issue/KT-16571) Configure Kotlin in Project does not suggest just published version
- [`KT-16590`](https://youtrack.jetbrains.com/issue/KT-16590) Configure kotlin warning popup after each sync gradle
- [`KT-16353`](https://youtrack.jetbrains.com/issue/KT-16353) Configure Kotlin in Project: configurators are not suggested for Gradle module in non-Gradle project with separate sub-modules for source sets
- [`KT-16381`](https://youtrack.jetbrains.com/issue/KT-16381) Configure Kotlin dialog suggests modules already configured with other platforms
- [`KT-16401`](https://youtrack.jetbrains.com/issue/KT-16401) Configure Kotlin in the project adds incorrect dependency kotlin-stdlib-jre8 to 1.0.x language
- [`KT-12261`](https://youtrack.jetbrains.com/issue/KT-12261) Partial body resolve doesn't resolve anything in object literal used as an expression body of a method
- [`KT-13013`](https://youtrack.jetbrains.com/issue/KT-13013) "Go to Type Declaration" doesn't work for extension receiver and implict lambda parameter
- [`KT-13135`](https://youtrack.jetbrains.com/issue/KT-13135) IDE goes in an infinite indexing loop if a .kotlin_module file is corrupted
- [`KT-14129`](https://youtrack.jetbrains.com/issue/KT-14129) for/iter postfix templates should be applied for string, ranges and mutable collections
- [`KT-14134`](https://youtrack.jetbrains.com/issue/KT-14134) Allow to apply for/iter postfix template to map
- [`KT-14871`](https://youtrack.jetbrains.com/issue/KT-14871) Idea and Maven is not in sync with ModuleKind for Kotlin projects
- [`KT-14986`](https://youtrack.jetbrains.com/issue/KT-14986) Disable postfix completion when typing package statements
- [`KT-15200`](https://youtrack.jetbrains.com/issue/KT-15200) Show implementation should show inherited classes if a typealias to base class/interface is used
- [`KT-15398`](https://youtrack.jetbrains.com/issue/KT-15398) Annotations find usages in annotation instance site
- [`KT-15536`](https://youtrack.jetbrains.com/issue/KT-15536) Highlight usages: Class with primary constructor isn't highlighted when caret is on constructor invocation
- [`KT-15628`](https://youtrack.jetbrains.com/issue/KT-15628) Change error message if both KotlinJavaRuntime and KotlinJavaScript libraries are present in the module dependencies
- [`KT-15947`](https://youtrack.jetbrains.com/issue/KT-15947) Kotlin facet: Target platform on importing from a maven project should be filled the same way for different artifacts
- [`KT-16023`](https://youtrack.jetbrains.com/issue/KT-16023) Kotlin facet: When "Use project settings" is enabled, respective fields should show konstues from the project settings
- [`KT-16698`](https://youtrack.jetbrains.com/issue/KT-16698) Kotlin facet: modules created from different gradle sourcesets have the same module options
- [`KT-16700`](https://youtrack.jetbrains.com/issue/KT-16700) Kotlin facet: jdkHome path containing spaces splits into several additional args after import
- [`KT-16776`](https://youtrack.jetbrains.com/issue/KT-16776) Kotlin facet, import from maven: free arguments from submodule doesn't override arguments from parent module
- [`KT-16550`](https://youtrack.jetbrains.com/issue/KT-16550) Kotlin facet from Maven: provide error messages if additional command line parameters are set several times
- [`KT-16313`](https://youtrack.jetbrains.com/issue/KT-16313) Kotlin facet: unify filling up information about included AllOpen/NoArg plugins on importing from Maven and Gradle
- [`KT-16342`](https://youtrack.jetbrains.com/issue/KT-16342) Kotlin facet: JavaScript platform is not detected if there are 2 versions of stdlib in dependencies
- [`KT-16032`](https://youtrack.jetbrains.com/issue/KT-16032) Kotlin code formatter merges comment line with non-comment line
- [`KT-16038`](https://youtrack.jetbrains.com/issue/KT-16038) UI blocked on pasting java code into a kotlin file
- [`KT-16062`](https://youtrack.jetbrains.com/issue/KT-16062) Kotlin breakpoint doesn't work in some lambda in Rider project.
- [`KT-15855`](https://youtrack.jetbrains.com/issue/KT-15855) Can't ekonstuate expression in @JvmStatic method
- [`KT-16667`](https://youtrack.jetbrains.com/issue/KT-16667) Kotlin debugger "smart step into" fail on method defined in the middle of class hierarchy
- [`KT-16078`](https://youtrack.jetbrains.com/issue/KT-16078) Formatter puts empty body braces on different lines when KDoc is present
- [`KT-16265`](https://youtrack.jetbrains.com/issue/KT-16265) Parameter info doesn't work with type alias constructor
- [`KT-14727`](https://youtrack.jetbrains.com/issue/KT-14727) Wrong samples for some postfix templates

##### Inspections / Quickfixes

- [`KT-17002`](https://youtrack.jetbrains.com/issue/KT-17002) Make "Lambda to Reference" inspection off by default
- [`KT-14402`](https://youtrack.jetbrains.com/issue/KT-14402) Inspection "Use destructuring declaration" for lambdas doesn't work when parameter is of type Pair
- [`KT-16857`](https://youtrack.jetbrains.com/issue/KT-16857) False "Remove redundant 'let'" suggestion
- [`KT-16928`](https://youtrack.jetbrains.com/issue/KT-16928) Surround with null check quickfix works badly in case of qualifier
- [`KT-15870`](https://youtrack.jetbrains.com/issue/KT-15870) Move quick fix of "Package name does not match containing directory" inspection: Throwable "AWT events are not allowed inside write action"
- [`KT-16128`](https://youtrack.jetbrains.com/issue/KT-16128) 'Add label to loop' QF proposed when there's already a label
- [`KT-16828`](https://youtrack.jetbrains.com/issue/KT-16828) Don't suggest destructing declarations if not all components are used
- [`KT-17022`](https://youtrack.jetbrains.com/issue/KT-17022) Replace deprecated in the whole project may miss some usages in expression body

##### Refactorings, Intentions

- [`KT-7516`](https://youtrack.jetbrains.com/issue/KT-7516) Rename refactoring doesn't rename related labels
- [`KT-7520`](https://youtrack.jetbrains.com/issue/KT-7520) Exception when try rename label from usage
- [`KT-8955`](https://youtrack.jetbrains.com/issue/KT-8955) Refactor / Move package: KNPE at KotlinMoveDirectoryWithClassesHelper.postProcessUsages() with not matching package statement
- [`KT-11863`](https://youtrack.jetbrains.com/issue/KT-11863) Refactor / Move: moving referred file level elements to another package keeps reference to old FQN
- [`KT-13190`](https://youtrack.jetbrains.com/issue/KT-13190) Refactor / Move: no warning on moving class containing internal member to different module
- [`KT-13341`](https://youtrack.jetbrains.com/issue/KT-13341) Convert lambda to function reference intention is not available for object member calls
- [`KT-13755`](https://youtrack.jetbrains.com/issue/KT-13755) When (java?) class is moved redundant imports are not removed
- [`KT-13911`](https://youtrack.jetbrains.com/issue/KT-13911) Refactor / Move: "Problems Detected" dialog is not shown on moving whole .kt file
- [`KT-14401`](https://youtrack.jetbrains.com/issue/KT-14401) Can't rename implicit lambda parameter 'it' when caret is placed right after the last character
- [`KT-14483`](https://youtrack.jetbrains.com/issue/KT-14483) "Argument of NotNull parameter must be not null" in KotlinTryCatchSurrounder when using "try" postfix template
- [`KT-15075`](https://youtrack.jetbrains.com/issue/KT-15075) KNPE in "Specify explicit lambda signature"
- [`KT-15190`](https://youtrack.jetbrains.com/issue/KT-15190) Refactor / Move: false Problems Detected on moving class using parent's protected member
- [`KT-15250`](https://youtrack.jetbrains.com/issue/KT-15250) Convert anonymous object to lambda is shown when conversion not possible due implicit calls on this
- [`KT-15339`](https://youtrack.jetbrains.com/issue/KT-15339) Extract Superclass is enabled for any element: CommonRefactoringUtil$RefactoringErrorHintException: "Superclass cannot be extracted from interface" at ExtractSuperRefactoring.performRefactoring()
- [`KT-15559`](https://youtrack.jetbrains.com/issue/KT-15559) Kotlin: Moving classes to different packages breaks references to companion object's properties
- [`KT-15556`](https://youtrack.jetbrains.com/issue/KT-15556) Convert lambda to reference isn't proposed for parameterless constructor
- [`KT-15586`](https://youtrack.jetbrains.com/issue/KT-15586) ISE during "Move to a separate file"
- [`KT-15822`](https://youtrack.jetbrains.com/issue/KT-15822) Move class refactoring leaves unused imports
- [`KT-16108`](https://youtrack.jetbrains.com/issue/KT-16108) Cannot rename class on the companion object reference
- [`KT-16198`](https://youtrack.jetbrains.com/issue/KT-16198) Extract method refactoring should order parameters by first usage
- [`KT-17006`](https://youtrack.jetbrains.com/issue/KT-17006) Refactor / Move: usage of library function is reported as problem on move between modules with different library versions
- [`KT-17032`](https://youtrack.jetbrains.com/issue/KT-17032) Refactor / Move updates references to not moved class from the same file
- [`KT-11907`](https://youtrack.jetbrains.com/issue/KT-11907) Move to package renames file to temp.kt
- [`KT-16468`](https://youtrack.jetbrains.com/issue/KT-16468) Destructure declaration intention should be applicable for Pair
- [`KT-16162`](https://youtrack.jetbrains.com/issue/KT-16162) IAE for destructuring declaration entry from KotlinFinalClassOrFunSpringInspection
- [`KT-16556`](https://youtrack.jetbrains.com/issue/KT-16556) Move refactoring shows Refactoring cannot be performed warning.
- [`KT-16605`](https://youtrack.jetbrains.com/issue/KT-16605) NPE caused by Rename Refactoring of backing field when caret is after the last character
- [`KT-16809`](https://youtrack.jetbrains.com/issue/KT-16809) Move refactoring fails badly
- [`KT-16903`](https://youtrack.jetbrains.com/issue/KT-16903) "Convert to primary constructor" doesn't update supertype constructor call in supertypes list in case of implicit superclass constructor call

### JS

- [`KT-6627`](https://youtrack.jetbrains.com/issue/KT-6627) JS: test sources doesn't compile from IDE
- [`KT-13610`](https://youtrack.jetbrains.com/issue/KT-13610) JS: boxed Double.NaN is not equal to itself
- [`KT-16012`](https://youtrack.jetbrains.com/issue/KT-16012) JS: prohibit nested declarations, except interfaces inside external interface
- [`KT-16043`](https://youtrack.jetbrains.com/issue/KT-16043) IDL: mark inline helper function as InlineOnly
- [`KT-16058`](https://youtrack.jetbrains.com/issue/KT-16058) JS: getValue/setValue don't work if they are declared as suspend
- [`KT-16164`](https://youtrack.jetbrains.com/issue/KT-16164) JS: Bad getCallableRef in suspend function
- [`KT-16350`](https://youtrack.jetbrains.com/issue/KT-16350) KotlinJS - wrong code generated when temporary variables generated for RHS of `&&` operation
- [`KT-16377`](https://youtrack.jetbrains.com/issue/KT-16377) JS: losing declarations of temporary variables in secondary constructors
- [`KT-16545`](https://youtrack.jetbrains.com/issue/KT-16545) JS: ::class crashes at runtime for primitive types (e.g. Int::class, or Double::class)
- [`KT-16144`](https://youtrack.jetbrains.com/issue/KT-16144) JS: inliner can't find function called through inheritor ("fake" override) from another module

### Reflection

- [`KT-9453`](https://youtrack.jetbrains.com/issue/KT-9453) ClassCastException: java.lang.Class cannot be cast to kotlin.reflect.KClass
- [`KT-11254`](https://youtrack.jetbrains.com/issue/KT-11254) Make callable references Serializable on JVM
- [`KT-11316`](https://youtrack.jetbrains.com/issue/KT-11316) NPE in hashCode of KProperty object created for delegated property
- [`KT-12630`](https://youtrack.jetbrains.com/issue/KT-12630) KotlinReflectionInternalError on referencing some functions from stdlib
- [`KT-14731`](https://youtrack.jetbrains.com/issue/KT-14731) When starting application from test source root, kotlin function reflection fails in objects defined in sources

### Libraries

- [`KT-16922`](https://youtrack.jetbrains.com/issue/KT-16922) buildSequence/Iterator: Infinite sequence terminates prematurely
- [`KT-16923`](https://youtrack.jetbrains.com/issue/KT-16923) Progression iterator doesn't throw after completion
- [`KT-16994`](https://youtrack.jetbrains.com/issue/KT-16994) Classify sequence operations as stateful/stateless and intermediate/terminal
- [`KT-9786`](https://youtrack.jetbrains.com/issue/KT-9786) String.trimIndent doc is misleading
- [`KT-16572`](https://youtrack.jetbrains.com/issue/KT-16572) Add links to Mozilla Developer Network to kdocs of classes that we generate from IDL
- [`KT-16252`](https://youtrack.jetbrains.com/issue/KT-16252) IDL2K: Add ItemArrayLike interface implementation to collection-like classes

## 1.1.1

### IDE
- [`KT-16714`](https://youtrack.jetbrains.com/issue/KT-16714) J2K: Write access is allowed from event dispatch thread only

### Compiler
- [`KT-16801`](https://youtrack.jetbrains.com/issue/KT-16801) Accessors of `@PublishedApi` property gets mangled
- [`KT-16673`](https://youtrack.jetbrains.com/issue/KT-16673) Potentially problematic code causes exception when work with SAM adapters

### Libraries
- [`KT-16557`](https://youtrack.jetbrains.com/issue/KT-16557) Correct `SinceKotlin(1.1)` for all declarations in `kotlin.reflect.full`

## 1.1.1-RC

### IDE
- [`KT-16481`](https://youtrack.jetbrains.com/issue/KT-16481) Kotlin debugger & bytecode fail on select statement blocks (IllegalStateException: More than one package fragment)

### Gradle support
- [`KT-15783`](https://youtrack.jetbrains.com/issue/KT-15783) Gradle builds don't use incremental compilation due to an error: "Could not connect to kotlin daemon"
- [`KT-16434`](https://youtrack.jetbrains.com/issue/KT-16434) Gradle plugin does not compile androidTest sources when Jack is enabled
- [`KT-16546`](https://youtrack.jetbrains.com/issue/KT-16546) Enable incremental compilation in gradle by default

### Compiler
- [`KT-16184`](https://youtrack.jetbrains.com/issue/KT-16184) AbstractMethodError in Kapt3ComponentRegistrar while compiling from IntelliJ using Kotlin 1.1.0
- [`KT-16578`](https://youtrack.jetbrains.com/issue/KT-16578) Fix substitutor for synthetic SAM adapters
- [`KT-16581`](https://youtrack.jetbrains.com/issue/KT-16581) VerifyError when calling default konstue parameter with jvm-target 1.8
- [`KT-16583`](https://youtrack.jetbrains.com/issue/KT-16583) Cannot access private file-level variables inside a class init within the same file if a secondary constructor is present
- [`KT-16587`](https://youtrack.jetbrains.com/issue/KT-16587) AbstractMethodError: Delegates not generated correctly for private interfaces
- [`KT-16598`](https://youtrack.jetbrains.com/issue/KT-16598) Incorrect error: The feature "bound callable references" is only available since language version 1.1
- [`KT-16621`](https://youtrack.jetbrains.com/issue/KT-16621) Kotlin compiler doesn't report an error if a class implements Annotation interface but doesn't implement annotationType method
- [`KT-16441`](https://youtrack.jetbrains.com/issue/KT-16441) `NoSuchFieldError: $$delegatedProperties` when delegating through `provideDelegate` in companion object

### JavaScript support
- Prohibit function types with receiver as parameter types of external declarations
- Remove extension receiver for function parameters in `jQuery` declarations

## 1.1

### Compiler exceptions
- [`KT-16411`](https://youtrack.jetbrains.com/issue/KT-16411) Exception from compiler when try to inline callable reference to class constructor inside object
- [`KT-16412`](https://youtrack.jetbrains.com/issue/KT-16412) Exception from compiler when try call SAM constructor where argument is callable reference to nested class inside object
- [`KT-16413`](https://youtrack.jetbrains.com/issue/KT-16413) When we create sam adapter for java.util.function.Function we add incorrect null-check for argument

### Standard library
- [`KT-6561`](https://youtrack.jetbrains.com/issue/KT-6561) Drop java.util.Collections package from js stdlib
- `javaClass` extension property is no more deprecated due to migration problems

### IDE
- [`KT-16329`](https://youtrack.jetbrains.com/issue/KT-16329) Inspection "Calls to staic methods in Java interfaces..." always reports warning undependent of jvm-target


## 1.1-RC

### Reflection
- [`KT-16358`](https://youtrack.jetbrains.com/issue/KT-16358) Incompatibility between kotlin-reflect 1.0 and kotlin-stdlib 1.1 fixed

### Compiler

#### Coroutine support
- [`KT-15938`](https://youtrack.jetbrains.com/issue/KT-15938) Changed error message for calling suspend function outside of suspendable context
- [`KT-16092`](https://youtrack.jetbrains.com/issue/KT-16092) Backend crash fixed: "Don't know how to generate outer expression" for destructuring suspend lambda
- [`KT-16093`](https://youtrack.jetbrains.com/issue/KT-16093) Annotations are retained during reading the binary representation of suspend functions
- [`KT-16122`](https://youtrack.jetbrains.com/issue/KT-16122) java.lang.VerifyError fixed in couroutines: (String, null, suspend () -> String)
- [`KT-16124`](https://youtrack.jetbrains.com/issue/KT-16124) Marked as UNSUPPORTED: suspension points in default parameters
- [`KT-16219`](https://youtrack.jetbrains.com/issue/KT-16219) Marked as UNSUPPORTED: suspend get/set, in/!in operators for
- [`KT-16145`](https://youtrack.jetbrains.com/issue/KT-16145) Beta-2 coroutine regression fixed (wrong code generation)

#### Kapt3
- [`KT-15524`](https://youtrack.jetbrains.com/issue/KT-15524) Fix javac error reporting in Kotlin daemon
- [`KT-15721`](https://youtrack.jetbrains.com/issue/KT-15721) JetBrains nullability annotations are now returned from Element.getAnnotationMirrors()
- [`KT-16146`](https://youtrack.jetbrains.com/issue/KT-16146) Fixed work in verbose mode
- [`KT-16153`](https://youtrack.jetbrains.com/issue/KT-16153) Ignore declarations with illegal Java identifiers
- [`KT-16167`](https://youtrack.jetbrains.com/issue/KT-16167) Fixed compilation error with kapt arguments in build.gradle
- [`KT-16170`](https://youtrack.jetbrains.com/issue/KT-16170) Stub generator now adds imports for corrected error types to stubs
- [`KT-16176`](https://youtrack.jetbrains.com/issue/KT-16176) javac's finalCompiler log is now used to determine annotation processing errors

#### Backward compatibility
- [`KT-16017`](https://youtrack.jetbrains.com/issue/KT-16017) More graceful error message for disabled features
- [`KT-16073`](https://youtrack.jetbrains.com/issue/KT-16073) Improved backward compatibility mode with version 1.0 on JDK dependent built-ins
- [`KT-16094`](https://youtrack.jetbrains.com/issue/KT-16094) Compiler considers API availability when compiling language features requiring runtime support
- [`KT-16171`](https://youtrack.jetbrains.com/issue/KT-16171) Fixed regression "Unexpected container error on Kotlin 1.0 project"
- [`KT-16199`](https://youtrack.jetbrains.com/issue/KT-16199) Do not import "kotlin.comparisons.*" by default in language version 1.0 mode

#### Various issues
- [`KT-16225`](https://youtrack.jetbrains.com/issue/KT-16225) enumValues non-reified stub implementation references nonexistent method no more
- [`KT-16291`](https://youtrack.jetbrains.com/issue/KT-16291) Smart cast works now when getting class of instance
- [`KT-16380`](https://youtrack.jetbrains.com/issue/KT-16380) Show warning when running the compiler under Java 6 or 7

### JavaScript backend
- [`KT-16144`](https://youtrack.jetbrains.com/issue/KT-16144) Fixed inlining of functions called through inheritor ("fake" override) from another module
- [`KT-16158`](https://youtrack.jetbrains.com/issue/KT-16158) Error is not reported now when library path contains JAR file without JS metadata, report warning instead
- [`KT-16160`](https://youtrack.jetbrains.com/issue/KT-16160) Companion object dispatch receiver translation fixed

### Standard library
- [`KT-7858`](https://youtrack.jetbrains.com/issue/KT-7858) Add extension function `takeUnless`
- `javaClass` extension property is deprecated, use `instance::class.java` instead
- Massive deprecations are coming in JS standard library in `kotlin.dom` and `kotlin.dom.build` packages

### IDE

#### Configuration issues
- [`KT-15899`](https://youtrack.jetbrains.com/issue/KT-15899) Kotlin facet: language and api version for submodule setup for 1.0 are filled now as 1.0 too
- [`KT-15914`](https://youtrack.jetbrains.com/issue/KT-15914) Kotlin facet works now with multi-selected modules in Project Settings too
- [`KT-15954`](https://youtrack.jetbrains.com/issue/KT-15954) Does not suggest to configure kotlin for the module after each new kt-file creation
- [`KT-16157`](https://youtrack.jetbrains.com/issue/KT-16157) freeCompilerArgs are now imported from Gradle into IDEA
- [`KT-16206`](https://youtrack.jetbrains.com/issue/KT-16206) Idea no more refuses to compile a kotlin project defined as a maven project
- [`KT-16312`](https://youtrack.jetbrains.com/issue/KT-16312) Kotlin facet: import from gradle: don't import options which are set implicitly already
- [`KT-16325`](https://youtrack.jetbrains.com/issue/KT-16325) Kotlin facet: correct configuration after upgrading the IDE plugin
- [`KT-16345`](https://youtrack.jetbrains.com/issue/KT-16345) Kotlin facet: detect JavaScript if the module has language 1.0 `kotlin-js-library` dependency

#### Coroutine support
- [`KT-16109`](https://youtrack.jetbrains.com/issue/KT-16109) Error fixed: The -Xcoroutines can only have one konstue 
- [`KT-16251`](https://youtrack.jetbrains.com/issue/KT-16251) Fix detection of suspend calls containing extracted parameters

#### Intention actions, inspections and quick-fixes

##### 2017.1 compatibility
- [`KT-15870`](https://youtrack.jetbrains.com/issue/KT-15870) "Package name does not match containing directory" inspection: fixed throwable "AWT events are not allowed inside write action"
- [`KT-15924`](https://youtrack.jetbrains.com/issue/KT-15924) Create Test action: fixed throwable "AWT events are not allowed inside write action"

##### Bug fixes
- [`KT-14831`](https://youtrack.jetbrains.com/issue/KT-14831) Import statement and FQN are not added on converting lambda to reference for typealias
- [`KT-15545`](https://youtrack.jetbrains.com/issue/KT-15545) Inspection "join with assignment" does not change now execution order for properties
- [`KT-15744`](https://youtrack.jetbrains.com/issue/KT-15744) Fix: intention to import `sleep` wrongly suggests `Thread.sleep`
- [`KT-16000`](https://youtrack.jetbrains.com/issue/KT-16000) Inspection "join with assignment" handles initialization with 'this' correctly
- [`KT-16009`](https://youtrack.jetbrains.com/issue/KT-16009) Auto-import for JDK classes in .kts files
- [`KT-16104`](https://youtrack.jetbrains.com/issue/KT-16104) Don't insert modifiers (e.g. suspend) before visibility

#### Completion
- [`KT-16076`](https://youtrack.jetbrains.com/issue/KT-16076) Completion does not insert more FQN kotlin.text.String
- [`KT-16088`](https://youtrack.jetbrains.com/issue/KT-16088) Completion does not insert more FQN for `kotlin` package
- [`KT-16110`](https://youtrack.jetbrains.com/issue/KT-16110) Keyword 'suspend' completion inside generic arguments
- [`KT-16243`](https://youtrack.jetbrains.com/issue/KT-16243) Performance enhanced after variable of type `ArrayList`

#### Various issues
- [`KT-15291`](https://youtrack.jetbrains.com/issue/KT-15291) 'Find usages' now does not report property access as usage of getter method in Java class with parameter
- [`KT-15647`](https://youtrack.jetbrains.com/issue/KT-15647) Exception fixed: KDoc link to member of class from different package and module
- [`KT-16071`](https://youtrack.jetbrains.com/issue/KT-16071) IDEA deadlock fixed: when typing "parse()" in .kt file
- [`KT-16149`](https://youtrack.jetbrains.com/issue/KT-16149) Intellij Idea 2017.1/Android Studio 2.3 beta3 and Kotlin plugin 1.1-beta2 deadlock fixed

### Coroutine libraries
- [`KT-15716`](https://youtrack.jetbrains.com/issue/KT-15716) Introduced startCoroutineUninterceptedOrReturn coroutine intrinsic
- [`KT-15718`](https://youtrack.jetbrains.com/issue/KT-15718) createCoroutine now returns safe continuation
- [`KT-16155`](https://youtrack.jetbrains.com/issue/KT-16155) Introduced createCoroutineUnchecked intrinsic


### Gradle support
- [`KT-15829`](https://youtrack.jetbrains.com/issue/KT-15829) Gradle Kotlin JS plugin: removed false "Duplicate source root:" warning for kotlin files
- [`KT-15902`](https://youtrack.jetbrains.com/issue/KT-15902) JS: gradle task output is now considered as source set output
- [`KT-16174`](https://youtrack.jetbrains.com/issue/KT-16174) Error fixed during IDEA-Gradle synchronization for Kotlin JS
- [`KT-16267`](https://youtrack.jetbrains.com/issue/KT-16267) JS: fixed regression in 1.1-beta2 for multi-module gradle project
- [`KT-16274`](https://youtrack.jetbrains.com/issue/KT-16274) Kotlin JS Gradle unexpected compiler error / absolute path to output file
- [`KT-16322`](https://youtrack.jetbrains.com/issue/KT-16322) Circlet project Gradle import issue fixed

### REPL
- [`KT-15861`](https://youtrack.jetbrains.com/issue/KT-15861) Use windows line separator in kotlin's JSR implementation
- [`KT-16126`](https://youtrack.jetbrains.com/issue/KT-16126) Proper `jvmTarget` for REPL compilation


## 1.1-Beta2

### Language related changes
- [`KT-7897`](https://youtrack.jetbrains.com/issue/KT-7897) Do not require to call enum constructor for each entry if all parameters have default konstues
- [`KT-8985`](https://youtrack.jetbrains.com/issue/KT-8985) Support T::class.java for T with no non-null upper bound
- [`KT-10711`](https://youtrack.jetbrains.com/issue/KT-10711) Type inference works now on generics for callable references
- [`KT-13130`](https://youtrack.jetbrains.com/issue/KT-13130) Support exhaustive when for sealed trees
- [`KT-15898`](https://youtrack.jetbrains.com/issue/KT-15898) Cannot use type alias to qualify enum entry
- [`KT-16061`](https://youtrack.jetbrains.com/issue/KT-16061) Smart type inference on callable references in 1.1 mode only

### Reflection
- [`KT-8384`](https://youtrack.jetbrains.com/issue/KT-8384) Access to the delegate object for a KProperty

### Compiler

#### Coroutine support
- [`KT-15016`](https://youtrack.jetbrains.com/issue/KT-15016) VerifyError with coroutine: fix processing of uninitialized instances
- [`KT-15527`](https://youtrack.jetbrains.com/issue/KT-15527) Coroutine compile error: wrong code generated for safe qualified suspension points
- [`KT-15552`](https://youtrack.jetbrains.com/issue/KT-15552) Accessor implementation of suspended function produces AbstractMethodError
- [`KT-15715`](https://youtrack.jetbrains.com/issue/KT-15715) Coroutine generate inkonstid invoke
- [`KT-15820`](https://youtrack.jetbrains.com/issue/KT-15820) Coroutine Internal Error regression with dispatcher + this@
- [`KT-15821`](https://youtrack.jetbrains.com/issue/KT-15821) Coroutine internal error regression: Could not inline method call apply
- [`KT-15824`](https://youtrack.jetbrains.com/issue/KT-15824) Coroutine iterator regression: Object cannot be cast to java.lang.Boolean
- [`KT-15827`](https://youtrack.jetbrains.com/issue/KT-15827) Show Kotlin Bytecode shows wrong bytecode for suspending functions
- [`KT-15907`](https://youtrack.jetbrains.com/issue/KT-15907) Bogus error about platform declaration clash with private suspend functions
- [`KT-15933`](https://youtrack.jetbrains.com/issue/KT-15933) Suspend getValue/setValue/provideDelegate do not work properly
- [`KT-15935`](https://youtrack.jetbrains.com/issue/KT-15935) Private suspend function in file causes UnsupportedOperationException: Context does not have a "this"
- [`KT-15963`](https://youtrack.jetbrains.com/issue/KT-15963) Coroutine: runtime error if returned object "equals" does not like comparison to SUSPENDED_MARKER
- [`KT-16068`](https://youtrack.jetbrains.com/issue/KT-16068) Prohibit inline lambda parameters of suspend function type

#### Diagnostics
- [`KT-1560`](https://youtrack.jetbrains.com/issue/KT-1560) Report diagnostic for a declaration of extension function which will be always shadowed by member function
- [`KT-12846`](https://youtrack.jetbrains.com/issue/KT-12846) Forbid vararg of Nothing
- [`KT-13227`](https://youtrack.jetbrains.com/issue/KT-13227) NO_ELSE_IN_WHEN in when by sealed class instance if is-check for base sealed class is used
- [`KT-13355`](https://youtrack.jetbrains.com/issue/KT-13355) Type mismatch on inheritance is not reported on abstract class
- [`KT-15010`](https://youtrack.jetbrains.com/issue/KT-15010) Missing error on an usage of non-constant property in annotation default argument 
- [`KT-15201`](https://youtrack.jetbrains.com/issue/KT-15201) Compiler is complaining about when statement without null condition even if null is checked before.
- [`KT-15736`](https://youtrack.jetbrains.com/issue/KT-15736) Report an error on type alias expanded to a nullable type on LHS of a class literal
- [`KT-15740`](https://youtrack.jetbrains.com/issue/KT-15740) Report error on expression of a nullable type on LHS of a class literal
- [`KT-15844`](https://youtrack.jetbrains.com/issue/KT-15844) Do not allow to access primary constructor parameters from property with custom getter
- [`KT-15878`](https://youtrack.jetbrains.com/issue/KT-15878) Extension shadowed by member should not be reported for infix/operator extensions when member is non-infix/operator
- [`KT-16010`](https://youtrack.jetbrains.com/issue/KT-16010) Do not highlight lambda parameters as unused in 1.0 compatibility mode

#### Kapt
- [`KT-15675`](https://youtrack.jetbrains.com/issue/KT-15675) Kapt3 does not generate classes annotated with AutoValue
- [`KT-15697`](https://youtrack.jetbrains.com/issue/KT-15697) If an annotation with AnnotationTarget.PROPERTY is tagged on a Kotlin property, it breaks annotation processing
- [`KT-15803`](https://youtrack.jetbrains.com/issue/KT-15803) Kotlin 1.0.6 broke Dagger
- [`KT-15814`](https://youtrack.jetbrains.com/issue/KT-15814) Regression: Kapt is not working in 1.0.6 / 1.1-M04 / 1.1-Beta
- [`KT-15838`](https://youtrack.jetbrains.com/issue/KT-15838) kapt3 1.1-beta: KaptError: Java file parsing error
- [`KT-15841`](https://youtrack.jetbrains.com/issue/KT-15841) 1.1-Beta + kapt3 fails to build the project with StackOverflowError
- [`KT-15915`](https://youtrack.jetbrains.com/issue/KT-15915) Kapt: Kotlin class target directory is cleared before compilation (and after kapt task)
- [`KT-16006`](https://youtrack.jetbrains.com/issue/KT-16006) Cannot determine if type is an error type during annotation processing

#### Exceptions / Errors
- [`KT-8264`](https://youtrack.jetbrains.com/issue/KT-8264) Internal compiler error: java.lang.ArithmeticException: BigInteger: modulus not positive
- [`KT-14547`](https://youtrack.jetbrains.com/issue/KT-14547) NoSuchElementException when compiling callable reference without stdlib in the classpath
- [`KT-14966`](https://youtrack.jetbrains.com/issue/KT-14966) Regression: VerifyError on access super implementation from delegate 
- [`KT-15017`](https://youtrack.jetbrains.com/issue/KT-15017) Throwing exception in the end of inline suspend-functions lead to internal compiler error
- [`KT-15439`](https://youtrack.jetbrains.com/issue/KT-15439) Resolved call is not completed for generic callable reference in if-expression
- [`KT-15500`](https://youtrack.jetbrains.com/issue/KT-15500) Exception passing freeCompilerArgs to gradle plugin
- [`KT-15646`](https://youtrack.jetbrains.com/issue/KT-15646) InconsistentDebugInfoException when stepping over `throw`
- [`KT-15726`](https://youtrack.jetbrains.com/issue/KT-15726) Kotlin compiles inkonstid bytecode for nested try-catch with return
- [`KT-15743`](https://youtrack.jetbrains.com/issue/KT-15743) Overloaded Kotlin extensions annotates wrong parameters in java
- [`KT-15868`](https://youtrack.jetbrains.com/issue/KT-15868) NPE when comparing nullable doubles for equality
- [`KT-15995`](https://youtrack.jetbrains.com/issue/KT-15995) Can't build project with DataBinding using Kotlin 1.1: incompatible language version
- [`KT-16047`](https://youtrack.jetbrains.com/issue/KT-16047) Internal Error: org.jetbrains.kotlin.util.KotlinFrontEndException while analyzing expression

#### Type inference issues
- [`KT-10268`](https://youtrack.jetbrains.com/issue/KT-10268) Wrong type inference related to captured types
- [`KT-11259`](https://youtrack.jetbrains.com/issue/KT-11259) Wrong type inference for Java 8 Stream.collect.
- [`KT-12802`](https://youtrack.jetbrains.com/issue/KT-12802) Type inference failed when irrelevant method reference is used
- [`KT-12964`](https://youtrack.jetbrains.com/issue/KT-12964) Support type inference for callable references from parameter types of an expected function type

#### Smart cast issues
- [`KT-13468`](https://youtrack.jetbrains.com/issue/KT-13468) Smart cast is broken after assignment of 'if' expression
- [`KT-14350`](https://youtrack.jetbrains.com/issue/KT-14350) Make smart-cast work as it does in 1.0 when -language-version 1.0 is used
- [`KT-14597`](https://youtrack.jetbrains.com/issue/KT-14597) When over smartcast enum is broken and breaks all other "when"
- [`KT-15792`](https://youtrack.jetbrains.com/issue/KT-15792) Wrong smart cast after y = x, x = null, y != null sequence

#### Various issues
- [`KT-15236`](https://youtrack.jetbrains.com/issue/KT-15236) False positive: Null can not be a konstue of a non-null type
- [`KT-15677`](https://youtrack.jetbrains.com/issue/KT-15677) Modifiers and annotations are lost on a (nullable) parenthesized type
- [`KT-15707`](https://youtrack.jetbrains.com/issue/KT-15707) IDEA unable to parallel compile different projects
- [`KT-15734`](https://youtrack.jetbrains.com/issue/KT-15734) Nullability is lost during expansion of a type alias
- [`KT-15748`](https://youtrack.jetbrains.com/issue/KT-15748) Type alias constructor return type should have a corresponding abbreviation
- [`KT-15775`](https://youtrack.jetbrains.com/issue/KT-15775) Annotations are lost on konstue parameter types of a function type
- [`KT-15780`](https://youtrack.jetbrains.com/issue/KT-15780) Treat Map.getOrDefault overrides in Java the same way as in 1.0.x compiler with language version 1.0
- [`KT-15794`](https://youtrack.jetbrains.com/issue/KT-15794) Refine backward compatibility mode for additional built-ins members from JDK
- [`KT-15848`](https://youtrack.jetbrains.com/issue/KT-15848) Implement additional annotation processing in the `KotlinScriptDefinitionFromAnnotatedTemplate` for SamWithReceiver plugin
- [`KT-15875`](https://youtrack.jetbrains.com/issue/KT-15875) Operation has lead to overflow for 'mod' with negative first operand
- [`KT-15945`](https://youtrack.jetbrains.com/issue/KT-15945) Feature Request: Andrey Breslav to grow a beard.

### JavaScript backend

#### Coroutine support
- [`KT-15834`](https://youtrack.jetbrains.com/issue/KT-15834) JS: Local delegate in suspend function
- [`KT-15892`](https://youtrack.jetbrains.com/issue/KT-15892) JS: safe call of suspend functions causes compiler to crash

#### Diagnostics
- [`KT-14668`](https://youtrack.jetbrains.com/issue/KT-14668) Do not allow declarations in 'kotlin' package or subpackages in JS
- [`KT-15184`](https://youtrack.jetbrains.com/issue/KT-15184) JS: prohibit `..` operation with `dynamic` on left-hand side
- [`KT-15253`](https://youtrack.jetbrains.com/issue/KT-15253) JS: no error when use class external class with JsModule in type context when compiling with plain module kind
- [`KT-15283`](https://youtrack.jetbrains.com/issue/KT-15283) JS: additional restrictions on dynamic
- [`KT-15961`](https://youtrack.jetbrains.com/issue/KT-15961) Could not implement external open class with function with optional parameter

#### Language feature support
- [`KT-14035`](https://youtrack.jetbrains.com/issue/KT-14035) JS: support implementing CharSequence
- [`KT-14036`](https://youtrack.jetbrains.com/issue/KT-14036) JS: use Int16 for Char when it possible and box to our Char otherwise
- [`KT-14097`](https://youtrack.jetbrains.com/issue/KT-14097) Wrong code generated for enum entry initialization using non-primary no-argument constructor
- [`KT-15312`](https://youtrack.jetbrains.com/issue/KT-15312) JS: map kotlin.Throwable to JS Error
- [`KT-15765`](https://youtrack.jetbrains.com/issue/KT-15765) JS: support callable references on built-in and intrinsic functions and properties
- [`KT-15900`](https://youtrack.jetbrains.com/issue/KT-15900) JS: Support enum entry with empty initializer with vararg constructor

#### Standard library support
- [`KT-4141`](https://youtrack.jetbrains.com/issue/KT-4141) JS: wrong return type for Date::getTime
- [`KT-4497`](https://youtrack.jetbrains.com/issue/KT-4497) JS: add String.toInt, String.toDouble etc extension functions, `parseInt` and `parseFloat` are deprecated in favor of these new ones
- [`KT-15940`](https://youtrack.jetbrains.com/issue/KT-15940) JS: rename all js standard library artifacts (both in maven and in compiler distribution) to `kotlin-stdlib-js.jar`
- Add `Promise<T>` external declaration to the standard library
- Types like `Date`, `Math`, `Console`, `Promise`, `RegExp`, `Json` require explicit import from `kotlin.js` package

#### External declarations
- [`KT-15144`](https://youtrack.jetbrains.com/issue/KT-15144) JS: rename `noImpl` to `definedExternally`
- [`KT-15306`](https://youtrack.jetbrains.com/issue/KT-15306) JS: allow to use `definedExternally` only inside a body of external declarations
- [`KT-15336`](https://youtrack.jetbrains.com/issue/KT-15336) JS: allow to inherit external classes from kotlin.Throwable
- [`KT-15905`](https://youtrack.jetbrains.com/issue/KT-15905) JS: add a way to control qualifier for external declarations inside file
- Deprecate `@native` annotation, to be removed in 1.1 release.

#### Exceptions / Errors
- [`KT-10894`](https://youtrack.jetbrains.com/issue/KT-10894) Infinite indexing at projects with JS modules
- [`KT-14124`](https://youtrack.jetbrains.com/issue/KT-14124) AssertionError: strings file not found on K2JS serialized data
 
#### Various issues
- [`KT-8211`](https://youtrack.jetbrains.com/issue/KT-8211) JS: generate dummy init for properties w/o initializer to avoid to have different hidden classes for different instances
- [`KT-12712`](https://youtrack.jetbrains.com/issue/KT-12712) JS: Json should not be a class
- [`KT-13312`](https://youtrack.jetbrains.com/issue/KT-13312) JS: can't use extension lambda where expected lambda and vice versa
- [`KT-13632`](https://youtrack.jetbrains.com/issue/KT-13632) Add template kotlin js project under gradle in "New Project" window
- [`KT-15278`](https://youtrack.jetbrains.com/issue/KT-15278) JS: don't treat property access through dynamic as side effect free
- [`KT-15285`](https://youtrack.jetbrains.com/issue/KT-15285) JS: take into account as many characteristics from the signature as possible when mangling
- [`KT-15678`](https://youtrack.jetbrains.com/issue/KT-15678) JS: Generated local variable named 'element' clashes with actual local variable named 'element'
- [`KT-15755`](https://youtrack.jetbrains.com/issue/KT-15755) JS compiler produces a lot of empty kotlin_file_table files for irrelevant packages
- [`KT-15770`](https://youtrack.jetbrains.com/issue/KT-15770) Name clash between recursive local functions with same name
- [`KT-15797`](https://youtrack.jetbrains.com/issue/KT-15797) JS: wrong code for accessing nested class inside js module
- [`KT-15863`](https://youtrack.jetbrains.com/issue/KT-15863) JS: Extension function reference shifts parameters loosing the receiver
- [`KT-16049`](https://youtrack.jetbrains.com/issue/KT-16049) JS: drop "-kjsm" command line option, merge the logic into "-meta-info"
- [`KT-16083`](https://youtrack.jetbrains.com/issue/KT-16083) JS: rename "-library-files" argument to "-libraries" and change separator from comma to system file separator

### Standard Library
- [`KT-13353`](https://youtrack.jetbrains.com/issue/KT-13353) Add Map.minus(key) and Map.minus(keys) 
- [`KT-13826`](https://youtrack.jetbrains.com/issue/KT-13826) Add parameter names in function types used in the standard library
- [`KT-14279`](https://youtrack.jetbrains.com/issue/KT-14279) Make String.matches(Regex) and Regex.matches(String) infix
- [`KT-15399`](https://youtrack.jetbrains.com/issue/KT-15399) Iterable.average() now returns NaN for an empty collection
- [`KT-15975`](https://youtrack.jetbrains.com/issue/KT-15975) Move coroutine-related runtime parts to `kotlin.coroutines.experimental` package
- [`KT-16030`](https://youtrack.jetbrains.com/issue/KT-16030) Move bitwise operations on Byte and Short to `kotlin.experimental` package
- [`KT-16026`](https://youtrack.jetbrains.com/issue/KT-16026) Classes compiled in 1.1 in 1.0-compatibility mode may contain references to CloseableKt class from 1.1

### IDE

#### Configuration issues
- [`KT-15621`](https://youtrack.jetbrains.com/issue/KT-15621) Copy compiler options konstues from project settings on creating a kotlin facet for Kotlin (JVM) project
- [`KT-15623`](https://youtrack.jetbrains.com/issue/KT-15623) Copy compiler options konstues from project settings on creating a kotlin facet for Kotlin (JavaScript) project
- [`KT-15624`](https://youtrack.jetbrains.com/issue/KT-15624) Set option "Use project settings" in newly created Kotlin facet
- [`KT-15712`](https://youtrack.jetbrains.com/issue/KT-15712) Configuring a project with Maven or Gradle should automatically use stdlib-jre7 or stdlib-jre8 instead of standard stdlib
- [`KT-15772`](https://youtrack.jetbrains.com/issue/KT-15772) Facet does not pick up api version from maven
- [`KT-15819`](https://youtrack.jetbrains.com/issue/KT-15819) It would be nice if compileKotlin options are imported into Kotlin facet from gradle/maven
- [`KT-16015`](https://youtrack.jetbrains.com/issue/KT-16015) Prohibit api-version > language-version in Facet and Project Settings

#### Coroutine support
- [`KT-14704`](https://youtrack.jetbrains.com/issue/KT-14704) Extract Method should work in coroutines
- [`KT-15955`](https://youtrack.jetbrains.com/issue/KT-15955) Quick-fix to enable coroutines through Gradle project configuration
- [`KT-16018`](https://youtrack.jetbrains.com/issue/KT-16018) Hide coroutines intrinsics from import and completion
- [`KT-16075`](https://youtrack.jetbrains.com/issue/KT-16075) Error:Kotlin: The -Xcoroutines can only have one konstue

#### Backward compatibility issues
- [`KT-15134`](https://youtrack.jetbrains.com/issue/KT-15134) Do not suggest using destructuring lambda if this will result in "available since 1.1" error
- [`KT-15918`](https://youtrack.jetbrains.com/issue/KT-15918) Quick fix "Set module language level to 1.1" should also set API version to 1.1
- [`KT-15969`](https://youtrack.jetbrains.com/issue/KT-15969) Replace operator with function should use either rem or mod for % depending on language version
- [`KT-15978`](https://youtrack.jetbrains.com/issue/KT-15978) Type alias from Kotlin 1.1 are suggested in completion even if language level is set to 1.0 in settings
- [`KT-15979`](https://youtrack.jetbrains.com/issue/KT-15979) Usages of type aliases are not shown as errors in editor if language version is set to 1.0
- [`KT-16019`](https://youtrack.jetbrains.com/issue/KT-16019) Do not suggest renaming to underscore in 1.0 compatibility mode
- [`KT-16036`](https://youtrack.jetbrains.com/issue/KT-16036) "Create type alias from usage" quick-fix should not be suggested at language level 1.0

#### Intention actions, inspections and quick-fixes

##### New features
- [`KT-9912`](https://youtrack.jetbrains.com/issue/KT-9912) Merge ifs intention
- [`KT-13427`](https://youtrack.jetbrains.com/issue/KT-13427) "Specify type explicitly" should support type aliases
- [`KT-15066`](https://youtrack.jetbrains.com/issue/KT-15066) "Make private/.." intention on type aliases
- [`KT-15709`](https://youtrack.jetbrains.com/issue/KT-15709) Add inspection for private primary constructors in data classes as they are accessible via the copy method
- [`KT-15738`](https://youtrack.jetbrains.com/issue/KT-15738) Intention to add `suspend` modifier to functional type
- [`KT-15800`](https://youtrack.jetbrains.com/issue/KT-15800) Quick-fix to convert a function to suspending on error when calling suspension inside

##### Bug fixes
- [`KT-13710`](https://youtrack.jetbrains.com/issue/KT-13710) Import intention action should not appear in import list
- [`KT-14680`](https://youtrack.jetbrains.com/issue/KT-14680) import statement to type alias reported as unused when using only TA constructor
- [`KT-14856`](https://youtrack.jetbrains.com/issue/KT-14856) TextView internationalisation intention does not report the problem
- [`KT-14993`](https://youtrack.jetbrains.com/issue/KT-14993) Keep destructuring declaration parameter on inspection "Remove explicit lambda parameter types"
- [`KT-14994`](https://youtrack.jetbrains.com/issue/KT-14994) PsiInkonstidElementAccessException and incorrect generation on inspection "Specify type explicitly" on destructuring parameter
- [`KT-15162`](https://youtrack.jetbrains.com/issue/KT-15162) "Remove explicit lambda parameter types" intentions fails with destructuring declaration with KNPE at KtPsiFactory.createLambdaParameterList()
- [`KT-15311`](https://youtrack.jetbrains.com/issue/KT-15311) "Add Import" intention generates incorrect code
- [`KT-15406`](https://youtrack.jetbrains.com/issue/KT-15406) Convert to secondary constructor for enum class should put new members after enum konstues
- [`KT-15553`](https://youtrack.jetbrains.com/issue/KT-15553) Copy concatenation text to clipboard with Kotlin and string interpolation does not work
- [`KT-15670`](https://youtrack.jetbrains.com/issue/KT-15670) 'Convert to lambda' quick fix in IDEA leaves single-line comment and } gets commented out
- [`KT-15873`](https://youtrack.jetbrains.com/issue/KT-15873) Alt+Enter menu isn't shown for deprecated mod function
- [`KT-15874`](https://youtrack.jetbrains.com/issue/KT-15874) Replace operator with function call replaces % with deprecated mod
- [`KT-15884`](https://youtrack.jetbrains.com/issue/KT-15884) False positive "Redundant .let call"
- [`KT-16072`](https://youtrack.jetbrains.com/issue/KT-16072) Intentions to convert suspend lambdas to callable references should not be shown

#### Android support
- [`KT-13275`](https://youtrack.jetbrains.com/issue/KT-13275) Kotlin Gradle plugin for Android does not work when jackOptions enabled
- [`KT-15150`](https://youtrack.jetbrains.com/issue/KT-15150) Android: Add quick-fix to generate View constructor convention
- [`KT-15282`](https://youtrack.jetbrains.com/issue/KT-15282) Issues debugging crossinline Android code

#### KDoc
- [`KT-14710`](https://youtrack.jetbrains.com/issue/KT-14710) Sample references are not resolved in IDE
- [`KT-15796`](https://youtrack.jetbrains.com/issue/KT-15796) Import of class referenced only in KDoc not preserved after copy-paste

#### Various issues
- [`KT-9011`](https://youtrack.jetbrains.com/issue/KT-9011) Shift+Enter should insert curly braces when invoked after class declaration
- [`KT-11308`](https://youtrack.jetbrains.com/issue/KT-11308) Hide kotlin.jvm.internal package contents from completion and auto-import
- [`KT-14252`](https://youtrack.jetbrains.com/issue/KT-14252) Completion could suggest constructors available via type aliases
- [`KT-14722`](https://youtrack.jetbrains.com/issue/KT-14722) Completion list isn't filled up for type alias to object
- [`KT-14767`](https://youtrack.jetbrains.com/issue/KT-14767) Type alias to annotation class should appear in the completion list
- [`KT-14859`](https://youtrack.jetbrains.com/issue/KT-14859) "Parameter Info" sometimes does not work properly inside lambda
- [`KT-15032`](https://youtrack.jetbrains.com/issue/KT-15032) Injected fragment: descriptor was not found for declaration: FUN
- [`KT-15153`](https://youtrack.jetbrains.com/issue/KT-15153) Support typeAlias extensions in completion and add import
- [`KT-15786`](https://youtrack.jetbrains.com/issue/KT-15786) NoSuchMethodError: com.intellij.util.containers.UtilKt.isNullOrEmpty
- [`KT-15883`](https://youtrack.jetbrains.com/issue/KT-15883) Generating equals() and hashCode(): hashCode does not correctly honor variable names with back ticks
- [`KT-15911`](https://youtrack.jetbrains.com/issue/KT-15911) Kotlin REPL will not launch: "Neither main class nor JAR path is specified"

### J2K
- [`KT-15789`](https://youtrack.jetbrains.com/issue/KT-15789) Kotlin plugin incorrectly converts for-loops from Java to Kotlin

### Gradle support
- [`KT-14830`](https://youtrack.jetbrains.com/issue/KT-14830) Kotlin Gradle plugin configuration should not add 'kotlin' source directory by default
- [`KT-15279`](https://youtrack.jetbrains.com/issue/KT-15279) 'Kotlin not configured message' should not be displayed while gradle sync is in progress
- [`KT-15812`](https://youtrack.jetbrains.com/issue/KT-15812) Create Kotlin facet on importing gradle project with unchecked option Create separate module per source set
- [`KT-15837`](https://youtrack.jetbrains.com/issue/KT-15837) Gradle compiler attempts to connect to daemon on address derived from DNS lookup
- [`KT-15909`](https://youtrack.jetbrains.com/issue/KT-15909) Copy Gradle compiler options to facets in Intellij/AS
- [`KT-15929`](https://youtrack.jetbrains.com/issue/KT-15929) Gradle project imported with wrong 'target platform'

### Other issues
- [`KT-15450`](https://youtrack.jetbrains.com/issue/KT-15450) JSR 223 - support ekonst with bindings


## 1.1.0-Beta

### Reflection
- [`KT-15540`](https://youtrack.jetbrains.com/issue/KT-15540) findAnnotation returns T?, but it throws NoSuchElementException when there is no matching annotation
- Reflection API in `kotlin-reflect` library is moved to `kotlin.reflect.full` package, declarations in the package `kotlin.reflect` are left deprecated. Please migrate according to the hints provided.

### Compiler

#### Coroutine support
- [`KT-15379`](https://youtrack.jetbrains.com/issue/KT-15379) Allow invoke on instances of suspend function type inside suspend function
- [`KT-15380`](https://youtrack.jetbrains.com/issue/KT-15380) Support suspend function type with konstue parameters
- [`KT-15391`](https://youtrack.jetbrains.com/issue/KT-15391) Prohibit suspend function type in supertype list
- [`KT-15392`](https://youtrack.jetbrains.com/issue/KT-15392) Prohibit local suspending function
- [`KT-15413`](https://youtrack.jetbrains.com/issue/KT-15413) Override regular functions with suspending ones and vice versa
- [`KT-15657`](https://youtrack.jetbrains.com/issue/KT-15657) Refine dispatchResume convention
- [`KT-15662`](https://youtrack.jetbrains.com/issue/KT-15662) Prohibit callable references to suspend functions

#### Diagnostics
- [`KT-9630`](https://youtrack.jetbrains.com/issue/KT-9630) Cannot create extension function on intersection of types
- [`KT-11398`](https://youtrack.jetbrains.com/issue/KT-11398) Possible false positive for INACCESSIBLE_TYPE
- [`KT-13593`](https://youtrack.jetbrains.com/issue/KT-13593) Do not report USELESS_ELVIS_RIGHT_IS_NULL for left argument with platform type
- [`KT-13859`](https://youtrack.jetbrains.com/issue/KT-13859) Wrong error about using unrepeatable annotation when mix implicit and explicit targets
- [`KT-14179`](https://youtrack.jetbrains.com/issue/KT-14179) Prohibit to use enum entry as type parameter
- [`KT-15097`](https://youtrack.jetbrains.com/issue/KT-15097) Inherited platform declarations clash: regression under 1.1 when indirectly inheriting from java.util.Map
- [`KT-15287`](https://youtrack.jetbrains.com/issue/KT-15287) Kotlin runtime 1.1 and runtime 1.0.x:  Overload resolution ambiguity
- [`KT-15334`](https://youtrack.jetbrains.com/issue/KT-15334) Incorrect "konst cannot be reassigned" inside do-while
- [`KT-15410`](https://youtrack.jetbrains.com/issue/KT-15410) "Protected function call from public-API inline function" for protected constructor call

#### Kapt3
- [`KT-15145`](https://youtrack.jetbrains.com/issue/KT-15145) Kapt3: Doesn't compile with multiple errors 
- [`KT-15232`](https://youtrack.jetbrains.com/issue/KT-15232) Kapt3 crash due to java codepage
- [`KT-15359`](https://youtrack.jetbrains.com/issue/KT-15359) Kapt3 exception while annotation processing (DataBindings AS2.3-beta1)
- [`KT-15375`](https://youtrack.jetbrains.com/issue/KT-15375) Kapt3 can't find ${env.JDK_18}/lib/tools.jar
- [`KT-15381`](https://youtrack.jetbrains.com/issue/KT-15381) Unresolved references: R with Kapt3
- [`KT-15397`](https://youtrack.jetbrains.com/issue/KT-15397) Kapt3 doesn't work with databinding
- [`KT-15409`](https://youtrack.jetbrains.com/issue/KT-15409) Kapt3 Cannot find the getter for attribute 'android:text' with konstue type java.lang.String on android.widget.EditText.
- [`KT-15421`](https://youtrack.jetbrains.com/issue/KT-15421) Kapt3: Substitute types from Psi instead of writing NonExistentClass for generated type names
- [`KT-15459`](https://youtrack.jetbrains.com/issue/KT-15459) Kapt3 doesn't generate code in test module
- [`KT-15524`](https://youtrack.jetbrains.com/issue/KT-15524) Kapt3 - Error messages should display associated element information (if available)
- [`KT-15713`](https://youtrack.jetbrains.com/issue/KT-15713) Kapt3: circular dependencies between Gradke tasks

#### Exceptions / Errors
- [`KT-11401`](https://youtrack.jetbrains.com/issue/KT-11401) Error type encountered for implicit invoke with function literal argument
- [`KT-12044`](https://youtrack.jetbrains.com/issue/KT-12044) Assertion "Rewrite at slice LEXICAL_SCOPE" for 'if' with property references 
- [`KT-14011`](https://youtrack.jetbrains.com/issue/KT-14011) Compiler crash when inlining: lateinit property allRecapturedParameters has not been initialized
- [`KT-14868`](https://youtrack.jetbrains.com/issue/KT-14868) CCE in runtime while converting Number to Char
- [`KT-15364`](https://youtrack.jetbrains.com/issue/KT-15364) VerifyError: Bad type on operand stack on ObserverIterator.hasNext
- [`KT-15373`](https://youtrack.jetbrains.com/issue/KT-15373) Internal error when running TestNG test
- [`KT-15437`](https://youtrack.jetbrains.com/issue/KT-15437) VerifyError: Bad local variable type on simplest provideDelegate
- [`KT-15446`](https://youtrack.jetbrains.com/issue/KT-15446) Property reference on an instance of subclass causes java.lang.VerifyError
- [`KT-15447`](https://youtrack.jetbrains.com/issue/KT-15447) Compiler backend error: "Don't know how to generate outer expression for class"
- [`KT-15449`](https://youtrack.jetbrains.com/issue/KT-15449) Back-end (JVM) Internal error: Couldn't inline method call
- [`KT-15464`](https://youtrack.jetbrains.com/issue/KT-15464) Regression: "Supertypes of the following classes cannot be resolved. Please make sure you have the required dependencies in the classpath:"
- [`KT-15575`](https://youtrack.jetbrains.com/issue/KT-15575) VerifyError: Bad type on operand stack

#### Various issues
- [`KT-11962`](https://youtrack.jetbrains.com/issue/KT-11962) Super call with default parameters check is generated for top-level function
- [`KT-11969`](https://youtrack.jetbrains.com/issue/KT-11969) ProGuard issue with private interface methods
- [`KT-12795`](https://youtrack.jetbrains.com/issue/KT-12795) Write information about sealed class inheritors to metadata
- [`KT-13718`](https://youtrack.jetbrains.com/issue/KT-13718) ClassFormatError on aspectj instrumentation
- [`KT-14162`](https://youtrack.jetbrains.com/issue/KT-14162) Support @InlineOnly on inline properties
- [`KT-14705`](https://youtrack.jetbrains.com/issue/KT-14705) Inconsistent smart casts on when enum subject
- [`KT-14917`](https://youtrack.jetbrains.com/issue/KT-14917) No way to pass additional java command line options to kontlinc on Windows
- [`KT-15112`](https://youtrack.jetbrains.com/issue/KT-15112) Compiler hangs on nested lock compilation
- [`KT-15225`](https://youtrack.jetbrains.com/issue/KT-15225) Scripts: generate classes with names that are konstid Java identifiers
- [`KT-15411`](https://youtrack.jetbrains.com/issue/KT-15411) Unnecessary CHECKCAST bytecode when dealing with null
- [`KT-15473`](https://youtrack.jetbrains.com/issue/KT-15473) Inkonstid KFunction byte code signature for callable references
- [`KT-15582`](https://youtrack.jetbrains.com/issue/KT-15582) Generated bytecode is sometimes incompatible with Java 9
- [`KT-15584`](https://youtrack.jetbrains.com/issue/KT-15584) Do not mark class files compiled with a release language version as pre-release
- [`KT-15589`](https://youtrack.jetbrains.com/issue/KT-15589) Upper bound for T in KClass<T> can be implicitly violated using generic function
- [`KT-15631`](https://youtrack.jetbrains.com/issue/KT-15631) Compiler hang in MethodAnalyzer.analyze() fixed 

### JavaScript backend

#### Coroutine support
- [`KT-15362`](https://youtrack.jetbrains.com/issue/KT-15362) JS: Regex doesn't work (properly) in coroutine
- [`KT-15366`](https://youtrack.jetbrains.com/issue/KT-15366) JS: error when calling inline function with optional parameters from another module inside coroutine lambda
- [`KT-15367`](https://youtrack.jetbrains.com/issue/KT-15367) JS: `for` against iterator with suspend `next` and `hasNext` functions does not work
- [`KT-15400`](https://youtrack.jetbrains.com/issue/KT-15400) suspendCoroutine is missing in JS BE
- [`KT-15597`](https://youtrack.jetbrains.com/issue/KT-15597) Support non-tail suspend calls inside named suspend functions 
- [`KT-15625`](https://youtrack.jetbrains.com/issue/KT-15625) JS: return statement without konstue surrounded by `try..finally` in suspend lambda causes compiler error
- [`KT-15698`](https://youtrack.jetbrains.com/issue/KT-15698) Move coroutine intrinsics to kotlin.coroutine.intrinsics package

#### Diagnostics
- [`KT-14577`](https://youtrack.jetbrains.com/issue/KT-14577) JS: do not report declaration clash when common redeclaration diagnostic applies
- [`KT-15136`](https://youtrack.jetbrains.com/issue/KT-15136) JS: prohibit inheritance from kotlin Function{N} interfaces

#### Language features support
- [`KT-12194`](https://youtrack.jetbrains.com/issue/KT-12194) Exhaustiveness check isn't generated for when expressions in JS at all
- [`KT-15590`](https://youtrack.jetbrains.com/issue/KT-15590) Support increment on inlined properties
 
#### Native / external
- [`KT-8081`](https://youtrack.jetbrains.com/issue/KT-8081) JS: native inherited class shouldn't require super or primary constructor call
- [`KT-13892`](https://youtrack.jetbrains.com/issue/KT-13892) JS: restrictions for native (external) functions and properties
- [`KT-15307`](https://youtrack.jetbrains.com/issue/KT-15307) JS: prohibit inline members inside external declarations
- [`KT-15308`](https://youtrack.jetbrains.com/issue/KT-15308) JS: prohibit non-abstract members inside external interfaces except nullable properties (with accessors)

#### Exceptions / Errors
- [`KT-7302`](https://youtrack.jetbrains.com/issue/KT-7302) KotlinJS - Trait with optional parameter causes compilation error
- [`KT-15325`](https://youtrack.jetbrains.com/issue/KT-15325) JS: ReferenceError: $receiver is not defined
- [`KT-15357`](https://youtrack.jetbrains.com/issue/KT-15357) JS: `when` expression in primary-from-secondary constructor call
- [`KT-15435`](https://youtrack.jetbrains.com/issue/KT-15435) Call to 'synchronize' crashes JS backend
- [`KT-15513`](https://youtrack.jetbrains.com/issue/KT-15513) JS: empty do..while loop crashes compiler

#### Various issues
- [`KT-4160`](https://youtrack.jetbrains.com/issue/KT-4160) JS: compiler produces wrong code for escaped variable names with characters which Illegal in JS (e.g. spaces)
- [`KT-7004`](https://youtrack.jetbrains.com/issue/KT-7004) JS: functions named `call` not inlined
- [`KT-7588`](https://youtrack.jetbrains.com/issue/KT-7588) JS: operators are not inlined
- [`KT-7733`](https://youtrack.jetbrains.com/issue/KT-7733) JS: Provide overflow behavior for integer arithmetic operations
- [`KT-8413`](https://youtrack.jetbrains.com/issue/KT-8413) JS: generated wrong code for some float constants
- [`KT-12598`](https://youtrack.jetbrains.com/issue/KT-12598) JS: comparisons for Enums always translates using strong operator
- [`KT-13523`](https://youtrack.jetbrains.com/issue/KT-13523) Augmented assignment with array access in LHS is translated incorrectly
- [`KT-13888`](https://youtrack.jetbrains.com/issue/KT-13888) JS: change how functions optional parameters get translated
- [`KT-15260`](https://youtrack.jetbrains.com/issue/KT-15260) JS: don't import module more than once
- [`KT-15475`](https://youtrack.jetbrains.com/issue/KT-15475) JS compiler deletes internal function name in js("") text block
- [`KT-15506`](https://youtrack.jetbrains.com/issue/KT-15506) JS: inkonstid ekonstuation order when passing arguments to function by name
- [`KT-15512`](https://youtrack.jetbrains.com/issue/KT-15512) JS: wrong result when use break/throw/return in || and && operators
- [`KT-15569`](https://youtrack.jetbrains.com/issue/KT-15569) js: Wrong code generated when calling an overloaded operator function on an inherited property

### Standard Library
- [`KEEP-23`](https://github.com/Kotlin/KEEP/blob/master/proposals/stdlib/group-and-fold.md) Operation to group by key and fold each group simultaneously
- [`KT-15774`](https://youtrack.jetbrains.com/issue/KT-15774) `buildSequence` and `buildIterator` functions with `yield` and `yieldAll` based on coroutines
- [`KT-6903`](https://youtrack.jetbrains.com/issue/KT-6903) Add `also` extension, which is like `apply`, but with `it` instead of `this` inside lambda.
- [`KT-7858`](https://youtrack.jetbrains.com/issue/KT-7858) Add extension function `takeIf` to match a konstue against predicate and return null when it does not match
- [`KT-11851`](https://youtrack.jetbrains.com/issue/KT-11851) Provide extension `Map.getValue(key: K): V` which throws or returns default when key is not found
- [`KT-7417`](https://youtrack.jetbrains.com/issue/KT-7417) Add min, max on two numbers to standard library
- [`KT-13898`](https://youtrack.jetbrains.com/issue/KT-13898) Allow to implement `toArray` in collections as protected and provide protected toArray in AbstractCollection.
- [`KT-14935`](https://youtrack.jetbrains.com/issue/KT-14935) Array-like list instantiation functions: `List(count) { init }` and `MutableList(count) { init }` 
- [`KT-15630`](https://youtrack.jetbrains.com/issue/KT-15630) Overloads of mutableListOf, mutableSetOf, mutableMapOf without parameters
- [`KT-15557`](https://youtrack.jetbrains.com/issue/KT-15557) Iterable<T>.joinTo loses information about each element by calling toString on them by default
- [`KT-15477`](https://youtrack.jetbrains.com/issue/KT-15477) Introduce Throwable.addSuppressed extension
- [`KT-15310`](https://youtrack.jetbrains.com/issue/KT-15310) Add dynamic.unsafeCast
- [`KT-15436`](https://youtrack.jetbrains.com/issue/KT-15436) JS stdlib: org.w3c.fetch.RequestInit has 12 parameters, all required
- [`KT-15458`](https://youtrack.jetbrains.com/issue/KT-15458) Add print and println to common stdlib

### IDE
- Project View: Fix presentation of Kotlin files and their members when @JvmName having the same name as the file itself

#### no-arg / all-open
- [`KT-15419`](https://youtrack.jetbrains.com/issue/KT-15419) IDE build doesn't pick settings of all-open plugin
- [`KT-15686`](https://youtrack.jetbrains.com/issue/KT-15686) IDE build doesn't pick settings of no-arg plugin
- [`KT-15735`](https://youtrack.jetbrains.com/issue/KT-15735) Facet loses compiler plugin settings on reopening project, when "Use project settings" = Yes

#### Formatter
- [`KT-15542`](https://youtrack.jetbrains.com/issue/KT-15542) Formatter doesn't handle spaces around 'by' keyword
- [`KT-15544`](https://youtrack.jetbrains.com/issue/KT-15544) Formatter doesn't remove spaces around function reference operator

#### Intention actions, inspections and quick-fixes

##### New features
- Implement quickfix which enables/disables coroutine support in module or project
- [`KT-5045`](https://youtrack.jetbrains.com/issue/KT-5045) Intention to convert between two comparisons and range check and vice versa
- [`KT-5629`](https://youtrack.jetbrains.com/issue/KT-5629) Quick-fix to import extension method when arguments of non-extension method do not match
- [`KT-6217`](https://youtrack.jetbrains.com/issue/KT-6217) Add warning for unused equals expression
- [`KT-6824`](https://youtrack.jetbrains.com/issue/KT-6824) Quick-fix for applying spread operator where vararg is expected
- [`KT-8855`](https://youtrack.jetbrains.com/issue/KT-8855) Implement "Create label" quick-fix
- [`KT-15056`](https://youtrack.jetbrains.com/issue/KT-15056) Implement intention which converts object literal to class
- [`KT-15068`](https://youtrack.jetbrains.com/issue/KT-15068) Implement intention which rename file according to the top-level class name
- [`KT-15564`](https://youtrack.jetbrains.com/issue/KT-15564) Add quick-fix for changing primitive cast to primitive conversion method

##### Bug fixes
- [`KT-14630`](https://youtrack.jetbrains.com/issue/KT-14630) Clearer diagnostic message for platform type inspection
- [`KT-14745`](https://youtrack.jetbrains.com/issue/KT-14745) KNPE in convert primary constructor to secondary
- [`KT-14889`](https://youtrack.jetbrains.com/issue/KT-14889) Replace 'if' with elvis operator produces red code if result is referenced in 'if'
- [`KT-14907`](https://youtrack.jetbrains.com/issue/KT-14907) Quick-fix for missing operator adds infix modifier to created function
- [`KT-15092`](https://youtrack.jetbrains.com/issue/KT-15092) Suppress inspection "use property access syntax" for some getters and fix completion for them
- [`KT-15227`](https://youtrack.jetbrains.com/issue/KT-15227) "Replace if with elvis" silently changes semantics
- [`KT-15412`](https://youtrack.jetbrains.com/issue/KT-15412) "Join declaration and assignment" can break code with smart casts
- [`KT-15501`](https://youtrack.jetbrains.com/issue/KT-15501) Intention "Add names to call arguments" shouldn't appear when the only argument is a trailing lambda

#### Refactorings (Extract / Pull)
- [`KT-15611`](https://youtrack.jetbrains.com/issue/KT-15611) Extract Interface/Superclass: Disable const-properties
- Pull Up: Fix pull-up from object to superclass
- [`KT-15602`](https://youtrack.jetbrains.com/issue/KT-15602) Extract Interface/Superclass: Disable "Make abstract" for inline/external/lateinit members
- Extract Interface: Disable inline/external/lateinit members
- [`KT-12704`](https://youtrack.jetbrains.com/issue/KT-12704), [`KT-15583`](https://youtrack.jetbrains.com/issue/KT-15583) Override/Implement Members: Support all nullability annotations respected by the Kotlin compiler
- [`KT-15563`](https://youtrack.jetbrains.com/issue/KT-15563) Override Members: Allow overriding virtual synthetic members (e.g. equals(), hashCode(), toString(), etc.) in data classes
- [`KT-15355`](https://youtrack.jetbrains.com/issue/KT-15355) Extract Interface: Disable "Make abstract" and assume it to be true for abstract members of an interface
- [`KT-15353`](https://youtrack.jetbrains.com/issue/KT-15353) Extract Superclass/Interface: Allow extracting class with special name (and quotes)
- [`KT-15643`](https://youtrack.jetbrains.com/issue/KT-15643) Extract Interface/Pull Up: Disable "Make abstract" and assume it to be true for primary constructor parameter when moving to an interface
- [`KT-15607`](https://youtrack.jetbrains.com/issue/KT-15607) Extract Interface/Pull Up: Disable internal/protected members when moving to an interface
- [`KT-15640`](https://youtrack.jetbrains.com/issue/KT-15640) Extract Interface/Pull Up: Drop 'final' modifier when moving to an interface
- [`KT-15639`](https://youtrack.jetbrains.com/issue/KT-15639) Extract Superclass/Interface/Pull Up: Add spaces between 'abstract' modifier and annotations
- [`KT-15606`](https://youtrack.jetbrains.com/issue/KT-15606) Extract Interface/Pull Up: Warn about private members with usages in the original class
- [`KT-15635`](https://youtrack.jetbrains.com/issue/KT-15635) Extract Superclass/Interface: Fix bogus visibility warning inside a member when it's being moved as abstract
- [`KT-15598`](https://youtrack.jetbrains.com/issue/KT-15598) Extract Interface: Red-highlight members inherited from a super-interface when that interface reference itself is not extracted
- [`KT-15674`](https://youtrack.jetbrains.com/issue/KT-15674) Extract Superclass: Drop inapplicable modifiers when converting property-parameter to ordinary parameter

#### Multi-platform project support
- [`KT-14908`](https://youtrack.jetbrains.com/issue/KT-14908) Actions (quick-fixes) to create implementations of header elements
- [`KT-15305`](https://youtrack.jetbrains.com/issue/KT-15305) Do not report UNUSED for header declarations with implementations and vice versa
- [`KT-15601`](https://youtrack.jetbrains.com/issue/KT-15601) Cannot suppress HEADER_WITHOUT_IMPLEMENTATION
- [`KT-15641`](https://youtrack.jetbrains.com/issue/KT-15641) Quick-fix "Create header interface implementation" does nothing

#### Android support

- [`KT-12884`](https://youtrack.jetbrains.com/issue/KT-12884) Android Extensions: Refactor / Rename of activity name does not change import extension statement
- [`KT-14308`](https://youtrack.jetbrains.com/issue/KT-14308) Android Studio randomly hangs due to Java static member import quick-fix lags
- [`KT-14358`](https://youtrack.jetbrains.com/issue/KT-14358) Kotlin extensions: rename layout file: Throwable: "PSI and index do not match" through KotlinFullClassNameIndex.get()
- [`KT-15483`](https://youtrack.jetbrains.com/issue/KT-15483) Kotlin lint throws unexpected exceptions in IDE

#### Various issues
- [`KT-12872`](https://youtrack.jetbrains.com/issue/KT-12872) Don't show "defined in <very long qualifier here>" in quick doc for local variables
- [`KT-13001`](https://youtrack.jetbrains.com/issue/KT-13001) "Go to Type Declaration" is broken for stdlib types
- [`KT-13067`](https://youtrack.jetbrains.com/issue/KT-13067) Syntax colouring doesn't work for KDoc tags
- [`KT-14815`](https://youtrack.jetbrains.com/issue/KT-14815) alt + enter -> "import" over a constructor reference is not working
- [`KT-14819`](https://youtrack.jetbrains.com/issue/KT-14819) Quick documentation for special Enum functions doesn't work
- [`KT-15141`](https://youtrack.jetbrains.com/issue/KT-15141) Bogus import popup for when function call cannot be resolved fully
- [`KT-15154`](https://youtrack.jetbrains.com/issue/KT-15154) IllegalStateException on attempt to convert import statement to * if last added import is to typealias
- [`KT-15329`](https://youtrack.jetbrains.com/issue/KT-15329) Regex not inspected properly for javaJavaIdentifierStart and javaJavaIdentifierPart
- [`KT-15383`](https://youtrack.jetbrains.com/issue/KT-15383) Kotlin Scripts can only resolve stdlib functions/classes if they are in a source directory
- [`KT-15440`](https://youtrack.jetbrains.com/issue/KT-15440) Improve extensions detection in IDEA
- [`KT-15548`](https://youtrack.jetbrains.com/issue/KT-15548) Kotlin plugin: @Language injections specified in another module are ignored
- Invoke `StorageComponentContainerContributor` extension for module dependencies container as well (needed for "sam-with-receiver" plugin to work with scripts)

### J2K
- [`KT-6790`](https://youtrack.jetbrains.com/issue/KT-6790) J2K: Static import of Map.Entry is lost during conversion
- [`KT-14736`](https://youtrack.jetbrains.com/issue/KT-14736) J2K: Incorrect conversion of back ticks in javadoc {@code} tag
- [`KT-15027`](https://youtrack.jetbrains.com/issue/KT-15027) J2K: Annotations are set on functions, but not on property accessors

### Gradle support

- [`KT-15376`](https://youtrack.jetbrains.com/issue/KT-15376) Kotlin incremental=true: fixed compatibility with AS 2.3
- [`KT-15433`](https://youtrack.jetbrains.com/issue/KT-15433) Kotlin daemon swallows exceptions: fixed stack trace reporting
- [`KT-15682`](https://youtrack.jetbrains.com/issue/KT-15682) Uncheck "Use project settings" option on import Kotlin project from gradle


## 1.1-M04 (EAP-4)

### Language related changes

- [`KT-4481`](https://youtrack.jetbrains.com/issue/KT-4481) compareTo on primitive floats/doubles should behave naturally
- [`KT-11016`](https://youtrack.jetbrains.com/issue/KT-11016) Allow to annotate internal API to be used inside public inline functions
- [`KT-11128`](https://youtrack.jetbrains.com/issue/KT-11128) Member vs SAM conversion with more specific signature
- [`KT-12215`](https://youtrack.jetbrains.com/issue/KT-12215) Allowing to access protected members in public inline members creates potential binary compatibility problem
- [`KT-12531`](https://youtrack.jetbrains.com/issue/KT-12531) Report error when delegated member hides a supertype member
- [`KT-14650`](https://youtrack.jetbrains.com/issue/KT-14650) mod function on integral types is inconsistent with BigInteger.mod
- [`KT-14651`](https://youtrack.jetbrains.com/issue/KT-14651) Floating point comparisons shall operate according to IEEE754
- [`KT-14852`](https://youtrack.jetbrains.com/issue/KT-14852) It should not be possible to use typealias that abbreviates a generic projection as a constructor
- [`KT-15226`](https://youtrack.jetbrains.com/issue/KT-15226) Restrict delegation to java 8 default methods

### Reflection

- [`KT-12250`](https://youtrack.jetbrains.com/issue/KT-12250) Provide API for getting a single annotation by its class
- [`KT-14939`](https://youtrack.jetbrains.com/issue/KT-14939) VerifyError in accessors for bound property reference with receiver 'null'

### Compiler

#### Coroutines

- Major coroutines redesign - see [`KEEP`](https://github.com/Kotlin/kotlin-coroutines/blob/master/kotlin-coroutines-informal.md) for details

#### Optimizations

- [`KT-11734`](https://youtrack.jetbrains.com/issue/KT-11734) Optimize const konsts by inlining them at call site
- [`KT-13570`](https://youtrack.jetbrains.com/issue/KT-13570) Generate TABLE/LOOKUPSWITCH if all when branches are const integer konstues
- [`KT-14746`](https://youtrack.jetbrains.com/issue/KT-14746) Captured Refs should not be volatile

#### Various issues

- [`KT-10982`](https://youtrack.jetbrains.com/issue/KT-10982) java.util.Map::compute* poor usability
- [`KT-12144`](https://youtrack.jetbrains.com/issue/KT-12144) Type inference incorporation error on SAM adapter call
- [`KT-14196`](https://youtrack.jetbrains.com/issue/KT-14196) Do not allow class literal with expression in annotation arguments
- [`KT-14453`](https://youtrack.jetbrains.com/issue/KT-14453) Regression: Type inference failed: inferred type is T but T was expected
- [`KT-14774`](https://youtrack.jetbrains.com/issue/KT-14774) Incorrect inner class modifier generated for sealed inner classes
- [`KT-14839`](https://youtrack.jetbrains.com/issue/KT-14839) CompilationException when calling inline fun with first arg of 2 (w/defaults) within catch block of Java exception type
- [`KT-14855`](https://youtrack.jetbrains.com/issue/KT-14855) Projection in type aliases should be allowed in supertypes and constructor invocations if they expand to non-toplevel projections
- [`KT-14887`](https://youtrack.jetbrains.com/issue/KT-14887) Unhelpful error "public-API inline function cannot access non-public-API" for unresolved call inside inline function
- [`KT-14930`](https://youtrack.jetbrains.com/issue/KT-14930) Android: creating Kotlin activity: UOE at EmptyList.removeAll()
- [`KT-15146`](https://youtrack.jetbrains.com/issue/KT-15146) Kapt3 no source files on unittest
- [`KT-15272`](https://youtrack.jetbrains.com/issue/KT-15272) Exception when building 2 projects at the same time

### JavaScript backend

#### dynamic type

- [`KT-8207`](https://youtrack.jetbrains.com/issue/KT-8207) Extension function on dynamic resolves on any type
- [`KT-6579`](https://youtrack.jetbrains.com/issue/KT-6579) JS: prohibit to use `in` and `!in` on dynamic
- [`KT-6580`](https://youtrack.jetbrains.com/issue/KT-6580) JS: prohibit to use more than one argument in indexed access on dynamic
- [`KT-13615`](https://youtrack.jetbrains.com/issue/KT-13615) JS: don't generate guard for catch with dynamic type

#### @native/external

- [`KT-13893`](https://youtrack.jetbrains.com/issue/KT-13893) JS: Replace @native annotation with external modifier
- [`KT-12877`](https://youtrack.jetbrains.com/issue/KT-12877) Allow to specify module for native JS declarations
- [`KT-14806`](https://youtrack.jetbrains.com/issue/KT-14806) JS: name of a local variable clashes with native declaration from global scope

#### Diagnostics

- [`KT-13889`](https://youtrack.jetbrains.com/issue/KT-13889) JS: prohibit overriding native functions with default konstues assigned to parameters
- [`KT-13894`](https://youtrack.jetbrains.com/issue/KT-13894) JS: prohibit native declaration inside non-native
- [`KT-13895`](https://youtrack.jetbrains.com/issue/KT-13895) JS: RUNTIME annotations
- [`KT-13896`](https://youtrack.jetbrains.com/issue/KT-13896) JS: prohibit external(native) extension functions and properties
- [`KT-13897`](https://youtrack.jetbrains.com/issue/KT-13897) JS: prohibit native(external) files and typealiases
- [`KT-13910`](https://youtrack.jetbrains.com/issue/KT-13910) JS: prohibit override members of native declaration with overloads
- [`KT-14027`](https://youtrack.jetbrains.com/issue/KT-14027) JS: prohibit native inner classes
- [`KT-14029`](https://youtrack.jetbrains.com/issue/KT-14029) JS: prohibit private members inside native declarations
- [`KT-14037`](https://youtrack.jetbrains.com/issue/KT-14037) JS: prohibit using native interfaces in RHS of IS
- [`KT-14038`](https://youtrack.jetbrains.com/issue/KT-14038) JS: warn when using native interface in RHS of AS
- [`KT-15130`](https://youtrack.jetbrains.com/issue/KT-15130) JS: prohibit inheritance native from non-native
- [`KT-12600`](https://youtrack.jetbrains.com/issue/KT-12600) JS: type check with a native interface compiles but crash at runtime
- [`KT-13307`](https://youtrack.jetbrains.com/issue/KT-13307) KotlinJS cannot cast to a marker interface.

#### Language features support

- [`KT-13573`](https://youtrack.jetbrains.com/issue/KT-13573) JS: support bound callable reference
- [`KT-14634`](https://youtrack.jetbrains.com/issue/KT-14634) JS: support enumValues / enumValueOf
- [`KT-15058`](https://youtrack.jetbrains.com/issue/KT-15058) JS: replace suspend function convention

#### Issues related to kotlin.Any

- [`KT-7664`](https://youtrack.jetbrains.com/issue/KT-7664) JS: "x is Any" is always false
- [`KT-7665`](https://youtrack.jetbrains.com/issue/KT-7665) JS: creating Any instance crashes on runtime
- [`KT-15131`](https://youtrack.jetbrains.com/issue/KT-15131) JS: don't mangle Any.equals

#### Various issues

- [`KT-14033`](https://youtrack.jetbrains.com/issue/KT-14033) JS: don't optimize (based on type information) by default expressions with any of "as, is, !is, as?, ?., !!"
- [`KT-13616`](https://youtrack.jetbrains.com/issue/KT-13616) JS: don't omit guard for catch with Throwable type
- [`KT-12976`](https://youtrack.jetbrains.com/issue/KT-12976) JS: human-friendly error message on wrong modules order
- [`KT-15212`](https://youtrack.jetbrains.com/issue/KT-15212) JS: link unqualified names in `js(...)` function to local functions in outer Kotlin function by name
- [`KT-14750`](https://youtrack.jetbrains.com/issue/KT-14750) JS: remove unnecessary functions from kotlin.js

#### Bugfixes

- [`KT-12566`](https://youtrack.jetbrains.com/issue/KT-12566) JS: inner local class should refer to captured variables via its outer class
- [`KT-12527`](https://youtrack.jetbrains.com/issue/KT-12527) Reified is-check works wrongly for chained calls
- [`KT-12586`](https://youtrack.jetbrains.com/issue/KT-12586) JS: compiler crashes when call inline function inside string templeate
- [`KT-13164`](https://youtrack.jetbrains.com/issue/KT-13164) Ecma TypeError on extending local class from inner one
- [`KT-14888`](https://youtrack.jetbrains.com/issue/KT-14888) JS: Compiler error: Cannot get FQ name of local class: lazy class <no name provided>
- [`KT-14748`](https://youtrack.jetbrains.com/issue/KT-14748) JS: eliminate unused functions
- [`KT-14999`](https://youtrack.jetbrains.com/issue/KT-14999) JS: Operator set + labeled lambdas
- [`KT-15007`](https://youtrack.jetbrains.com/issue/KT-15007) JS: Dies when checking if exception implements interface. TypeError: Cannot read property 'baseClasses' of undefined
- [`KT-15073`](https://youtrack.jetbrains.com/issue/KT-15073) KT to JS losing extension function's receiver
- [`KT-15169`](https://youtrack.jetbrains.com/issue/KT-15169) JS: compiler fails on annotated expression with TRE at Translation.doTranslateExpression()
- [`KT-13522`](https://youtrack.jetbrains.com/issue/KT-13522) JS: can't use captured reified type paramter in jsClass
- [`KT-13784`](https://youtrack.jetbrains.com/issue/KT-13784) JS: lambda was not inlined for function with reified parameter declared in another module
- [`KT-13792`](https://youtrack.jetbrains.com/issue/KT-13792) JS: inner class of local class does not capture enclosing class properly
- [`KT-15327`](https://youtrack.jetbrains.com/issue/KT-15327) JS: Enum `konstueOf` should throw IllegalArgumentException

### Standard library

- [`KT-7930`](https://youtrack.jetbrains.com/issue/KT-7930) Make String.toInt(), toLong(), etc. nullable instead of throwing exception
- [`KT-8220`](https://youtrack.jetbrains.com/issue/KT-8220) Add #peek method to Sequence similar to Stream.peek
- [`KT-8286`](https://youtrack.jetbrains.com/issue/KT-8286) Int.toString and String.toInt with base as parameter
- [`KT-14034`](https://youtrack.jetbrains.com/issue/KT-14034) JS: unsafeCast function
- [`KT-15181`](https://youtrack.jetbrains.com/issue/KT-15181) Some source files are missing from published sources on Bintray

### IDE

- [`KT-15205`](https://youtrack.jetbrains.com/issue/KT-15205) Implement quick-fix for increasing module language level to enable unsupported language features

###### Issues fixed
- [`KT-14693`](https://youtrack.jetbrains.com/issue/KT-14693) Introduce Type Alias: Do not suggest type qualifiers
- [`KT-14696`](https://youtrack.jetbrains.com/issue/KT-14696) Introduce Type Alias: Fix NPE during dialog repaint
- [`KT-14685`](https://youtrack.jetbrains.com/issue/KT-14685) Introduce Type Alias: Replace type usages in constructor calls
- [`KT-14861`](https://youtrack.jetbrains.com/issue/KT-14861) Introduce Type Alias: Support callable references/class literals
- [`KT-15204`](https://youtrack.jetbrains.com/issue/KT-15204) Implement navigation from header to its implementation and vice versa
- [`KT-15269`](https://youtrack.jetbrains.com/issue/KT-15269) Quickfix for external (native) extension declarations
- [`KT-15293`](https://youtrack.jetbrains.com/issue/KT-15293) Add 1.1 EAP repository when creating a new Gradle project with 1.1 EAP

### Scripting

- [`KT-14538`](https://youtrack.jetbrains.com/issue/KT-14538) Kotlin gradle script files appear totally unresolved
- [`KT-14706`](https://youtrack.jetbrains.com/issue/KT-14706) Support package declaration in scripting
- [`KT-14707`](https://youtrack.jetbrains.com/issue/KT-14707) Support javax.script.Invocable on the JSR 223 ScriptEngine
- [`KT-14708`](https://youtrack.jetbrains.com/issue/KT-14708) kotlin-script-runtime is not published
- [`KT-14713`](https://youtrack.jetbrains.com/issue/KT-14713) Make it possible to use JSR 223 support without specifying compiler JAR absolute path
- [`KT-15064`](https://youtrack.jetbrains.com/issue/KT-15064) Gradle build with script .kts file: NPE at ScriptCodegen.genConstructor()

### Gradle support

- [`KT-15080`](https://youtrack.jetbrains.com/issue/KT-15080) Gradle build fails with Gradle 3.2 (master)
- [`KT-15120`](https://youtrack.jetbrains.com/issue/KT-15120) Gradle JS test compile task doesn't pick up production code
- [`KT-15127`](https://youtrack.jetbrains.com/issue/KT-15127) JS "compiler jar not found" with Gradle 3.2
- [`KT-15133`](https://youtrack.jetbrains.com/issue/KT-15133) Recent gradle-script-kotlin 3.3 distributions are unusable
- [`KT-15218`](https://youtrack.jetbrains.com/issue/KT-15218) Isolate Gradle Kotlin compiler process


## 1.1-M03 (EAP-3)

### New language features

- [`KT-2964`](https://youtrack.jetbrains.com/issue/KT-2964) Underscores in integer literals
    (see [KEEP](https://github.com/Kotlin/KEEP/blob/master/proposals/underscores-in-numeric-literals.md))
- [`KT-3824`](https://youtrack.jetbrains.com/issue/KT-3824) Underscore in lambda for unused parameters
    (see [KEEP](https://github.com/Kotlin/KEEP/blob/master/proposals/underscore-for-unused-parameters.md))
- [`KT-2783`](https://youtrack.jetbrains.com/issue/KT-2783) Allow to skip some components in a multi-declaration
    (see the same [KEEP](https://github.com/Kotlin/KEEP/blob/master/proposals/underscore-for-unused-parameters.md))
- [`KT-11551`](https://youtrack.jetbrains.com/issue/KT-11551) limited scope for dsl writers 
    (see [KEEP](https://github.com/Kotlin/KEEP/blob/master/proposals/scope-control-for-implicit-receivers.md))

### Compiler

#### Coroutines related issues
- Make fields for storing lambda parameters non-final (as they get assigned within `invoke` call)
- [`KT-14719`](https://youtrack.jetbrains.com/issue/KT-14719) Make initial continuation able to be resumed with exception 
- [`KT-14636`](https://youtrack.jetbrains.com/issue/KT-14636) Coroutine fields should not be volatile
- [`KT-14718`](https://youtrack.jetbrains.com/issue/KT-14718) Validate label konstue of coroutine in case of no suspension points

#### Typealises related issues
- [`KT-13514`](https://youtrack.jetbrains.com/issue/KT-13514) Type inference doesn't work with generic typealiases
- [`KT-13837`](https://youtrack.jetbrains.com/issue/KT-13837) Error "Type alias expands to T, which is not a class, an interface, or an object" 
    should also appear for local type aliases
- [`KT-14307`](https://youtrack.jetbrains.com/issue/KT-14307) Local recursive type alias should be an error
- [`KT-14400`](https://youtrack.jetbrains.com/issue/KT-14400) Compiler Error IllegalStateException: kotlin.NotImplementedError when anonymous 
    object inherits from typealias
- [`KT-14377`](https://youtrack.jetbrains.com/issue/KT-14377) Expected error: Modifier 'companion' is not applicable to 'typealias'
- [`KT-14498`](https://youtrack.jetbrains.com/issue/KT-14498) typealias allows to circumvent variance annotations
- [`KT-14641`](https://youtrack.jetbrains.com/issue/KT-14641) An exception while processing a nested type alias access after a dot

#### Various issues
- [`KT-550`](https://youtrack.jetbrains.com/issue/KT-550) Properties without initializer but with get must infer type from getter
- [`KT-8816`](https://youtrack.jetbrains.com/issue/KT-8816) Generate Kotlin parameter names in the same form as expected for Java 8 reflection
- [`KT-10569`](https://youtrack.jetbrains.com/issue/KT-10569) Cannot iterate over konstues of an enum class when it is used as a generic parameter
    (see [KEEP](https://github.com/Kotlin/KEEP/blob/master/proposals/generic-konstues-and-konstueof-for-enums.md))
- [`KT-13557`](https://youtrack.jetbrains.com/issue/KT-13557) VerifyError with delegated local variable used in object expression
- [`KT-13890`](https://youtrack.jetbrains.com/issue/KT-13890) IllegalAccessError when invoking protected method with default arguments
- [`KT-14012`](https://youtrack.jetbrains.com/issue/KT-14012) Back-end (JVM) Internal error every first compilation after the source code change
- [`KT-14201`](https://youtrack.jetbrains.com/issue/KT-14201) UnsupportedOperationException: Don't know how to generate outer expression for anonymous 
    object with invoke and non-trivial closure
- [`KT-14318`](https://youtrack.jetbrains.com/issue/KT-14318) Repeated annotations resulting from type alias expansion should be reported
- [`KT-14347`](https://youtrack.jetbrains.com/issue/KT-14347) Report UNUSED_PARAMETER/VARIABLE on named unused lambda parameters/destructuring entries
- [`KT-14352`](https://youtrack.jetbrains.com/issue/KT-14352) @SinceKotlin is not taken into account for companion object member referenced via 
    type alias
- [`KT-14357`](https://youtrack.jetbrains.com/issue/KT-14357) Try-catch used in false condition generates CompilationException
- [`KT-14502`](https://youtrack.jetbrains.com/issue/KT-14502) Prohibit irrelevant modifiers and annotations on destructured parameters in lambda
- [`KT-14692`](https://youtrack.jetbrains.com/issue/KT-14692) Change resolution scope for componentX in lambda parameters
- [`KT-14824`](https://youtrack.jetbrains.com/issue/KT-14824) Back-end (JVM) Internal error: Couldn't inline method call 'get' into local final fun 
    StorageComponentContainer.<anonymous>(): kotlin.Unit
- [`KT-14798`](https://youtrack.jetbrains.com/issue/KT-14798) Gradle 3.2 AssertionError: Built-in class kotlin.ParameterName is not found

### JS

#### Feature support
- [`KT-6985`](https://youtrack.jetbrains.com/issue/KT-6985) Support Exceptions in JS
- [`KT-13574`](https://youtrack.jetbrains.com/issue/KT-13574) JS: support coroutines
- [`KT-14422`](https://youtrack.jetbrains.com/issue/KT-14422) JS: Support destructuring in lambda parameters
- [`KT-14507`](https://youtrack.jetbrains.com/issue/KT-14507) JS: allow to skip some components in a multi-declaration

#### Library updates
- [`KT-14637`](https://youtrack.jetbrains.com/issue/KT-14637) JS: Missing ArrayList.ensureCapacity
    
#### Other issues
- [`KT-2328`](https://youtrack.jetbrains.com/issue/KT-2328) js: kotlin exceptions must inherit Error
- [`KT-5537`](https://youtrack.jetbrains.com/issue/KT-5537) Drop Cloneable in JS
- [`KT-7014`](https://youtrack.jetbrains.com/issue/KT-7014) JS: generate code which more friendly to js tools (minifier, optimizer, linter etc)
- [`KT-8019`](https://youtrack.jetbrains.com/issue/KT-8019) JS: no stackTrace in exception subclasses
- [`KT-10911`](https://youtrack.jetbrains.com/issue/KT-10911) JS: Throwable properties aren't supported well
- [`KT-13912`](https://youtrack.jetbrains.com/issue/KT-13912) JS: Compiler NPE at JsSourceGenerationVisitor. Lambda with empty [if] block passed 
    to inline function
- [`KT-14535`](https://youtrack.jetbrains.com/issue/KT-14535) JS: Broken modification of captured variables defined by a destructuring declaration

### Standard Library
- [`KT-2084`](https://youtrack.jetbrains.com/issue/KT-2084) Common API should be available without referring to java.* packages

    Now those common types, which are supported on all platforms, are available in `kotlin.*` packages, and are imported by default. These include:
    - `ArrayList`, `HashSet`, `LinkedHashSet`, `HashMap`, `LinkedHashMap` in `kotlin.collections`
    - `Appendable` and `StringBuilder` in `kotlin.text`
    - `Comparator` in `kotlin.comparisons`
    On JVM these are just typealiases of the good old types from `java.util` and `java.lang`
- [`KT-13554`](https://youtrack.jetbrains.com/issue/KT-13554) Introduce bitwise operations `and`/`or`/`xor`/`inv` for Byte and Short
- [`KT-13582`](https://youtrack.jetbrains.com/issue/KT-13582)  New platform-agnostic extensions for arrays: `contentEquals` to compare arrays' 
    content for equality, `contentHashCode` to get hashcode of array's content, and `contentToString` to get the string representation of array elements.
- [`KT-14510`](https://youtrack.jetbrains.com/issue/KT-14510) Generic constraints of `Array.flatten` signature were relaxed a bit to make it just usable.
- [`KT-14789`](https://youtrack.jetbrains.com/issue/KT-14789) Provide `KotlinVersion` class, which allows to get the current version of the standard 
    library and compare it with some other `KotlinVersion` konstue.

### IDE
- [`KT-14409`](https://youtrack.jetbrains.com/issue/KT-14409) Incorrect "Variable can be declared immutable" inspection for local delegated variable
- [`KT-14431`](https://youtrack.jetbrains.com/issue/KT-14431) Create quick-fix on UNUSED_PARAMETER/VARIABLE when it can be replaced with one underscore
- [`KT-14794`](https://youtrack.jetbrains.com/issue/KT-14794) Add /Specify type/Remove explicit type intentions for property with getters if type 
    can be inferred
- [`KT-14752`](https://youtrack.jetbrains.com/issue/KT-14752) Exception while typing @JsName annotation in editor

## 1.1-M02 (EAP-2)

### Language features

+ **Destructuring for lambdas** ([proposal](https://github.com/Kotlin/KEEP/issues/32))

    Current limitations:

    - Nested destructuring is not supported
    - Destructuring in named functions/constructors is not supported
    - Is not supported for JS target
        
### Compiler

#### Smart cast enhancements
- [`KT-2127`](https://youtrack.jetbrains.com/issue/KT-2127) Smart cast receiver to not null after a not null safe call
- [`KT-6840`](https://youtrack.jetbrains.com/issue/KT-6840) Make data flow information the same for assigned and assignee
- [`KT-13426`](https://youtrack.jetbrains.com/issue/KT-13426) Fix exception when smartcast on both dispatch & extension receiver

#### Bound references related issues
- [`KT-12995`](https://youtrack.jetbrains.com/issue/KT-12995) Do not skip generation of the left-hand side for intrinsic bound references and class literals
- [`KT-13075`](https://youtrack.jetbrains.com/issue/KT-13075) Fix codegen for bound class reference
- [`KT-13110`](https://youtrack.jetbrains.com/issue/KT-13110) Fix type mismatch error on class literal with integer receiver expression
- [`KT-13172`](https://youtrack.jetbrains.com/issue/KT-13172) Report error on "this::class" in super constructor call
- [`KT-13271`](https://youtrack.jetbrains.com/issue/KT-13271) Fix incorrect unsupported error on synthetic extension call on LHS of ::
- [`KT-13367`](https://youtrack.jetbrains.com/issue/KT-13367) Inline bound callable reference if it's used only as a lambda

#### Coroutines related issues
- [`KT-13156`](https://youtrack.jetbrains.com/issue/KT-13156) Do not execute last Unit-typed coroutine statement twice
- [`KT-13246`](https://youtrack.jetbrains.com/issue/KT-13246) Fix VerifyError with coroutines on Dalvik
- [`KT-13289`](https://youtrack.jetbrains.com/issue/KT-13289) Fix VerifyError with coroutines: Bad type on operand stack
- [`KT-13409`](https://youtrack.jetbrains.com/issue/KT-13409) Fix generic variable spilling with coroutines
- [`KT-13531`](https://youtrack.jetbrains.com/issue/KT-13531) Fix ClassCastException when coercion to Unit interacts with generic await() and coroutines
- Prohibit `Continuation<*>` as a last parameter of suspend functions
- [`KT-13560`](https://youtrack.jetbrains.com/issue/KT-13560) Prohibit non-Unit suspend functions

#### Typealises related issues
- [`KT-13200`](https://youtrack.jetbrains.com/issue/KT-13200) Fix incorrect number of required type arguments reported on typealias
- [`KT-13181`](https://youtrack.jetbrains.com/issue/KT-13181) Fix unresolved reference for a type alias from a different module
- [`KT-13161`](https://youtrack.jetbrains.com/issue/KT-13161) Support java static methods calls with typealiases
- [`KT-13835`](https://youtrack.jetbrains.com/issue/KT-13835) Do not lose nullability information  while expanding type alias in projection position
- [`KT-13422`](https://youtrack.jetbrains.com/issue/KT-13422) Prohibit usage of type alias to exception class as an object in 'throw' expression 
- [`KT-13735`](https://youtrack.jetbrains.com/issue/KT-13735) Fix NoSuchMethodError for generic typealias access
- [`KT-13513`](https://youtrack.jetbrains.com/issue/KT-13513) Support SAM constructors for aliased java functional types
- [`KT-13822`](https://youtrack.jetbrains.com/issue/KT-13822) Fix exception for start-projection of a type alias
- [`KT-14071`](https://youtrack.jetbrains.com/issue/KT-14071) Prohibit using type alias as a qualifier for super
- [`KT-14282`](https://youtrack.jetbrains.com/issue/KT-14282) Report error on unused type alias with -language-version 1.0
- [`KT-14274`](https://youtrack.jetbrains.com/issue/KT-14274) Fix type alias resolution when it's used for supertype constructor call

#### JDK dependent built-in classes related issues
- [`KT-13209`](https://youtrack.jetbrains.com/issue/KT-13209) Change first parameter's type of Map.getOrDefault to K instead of Any
- [`KT-13069`](https://youtrack.jetbrains.com/issue/KT-13069) Do not emit inkonstid DefaultImpls delegation when interface extends MutableMap with JDK8

#### `data` classes and inheritance
- [`KT-11306`](https://youtrack.jetbrains.com/issue/KT-11306) Allow data classes to implement equals/hashCode/toString from base classes

#### Various JVM code generation issues
- [`KT-13182`](https://youtrack.jetbrains.com/issue/KT-13182) Fix compiler internal error at inline
- [`KT-13757`](https://youtrack.jetbrains.com/issue/KT-13757) Prohibit referencing nested classes by name with $
- [`KT-12985`](https://youtrack.jetbrains.com/issue/KT-12985) Do not create range instances for 'for' loop in CharSequence.indices
- [`KT-13931`](https://youtrack.jetbrains.com/issue/KT-13931) Optimize generated code for IntRange#contains

#### Various analysis & diagnostic issues
- [`KT-435`](https://youtrack.jetbrains.com/issue/KT-435) Use parameter names in error messages when calling a function-konstued expression
- [`KT-10001`](https://youtrack.jetbrains.com/issue/KT-10001) Fix false unnecessary non-null assertion on a pair element
- [`KT-12811`](https://youtrack.jetbrains.com/issue/KT-12811) Treat function declaration as final if it is a member of a final class
- [`KT-13961`](https://youtrack.jetbrains.com/issue/KT-13961) Report REDECLARATION on private-in-file 'foo' vs public 'foo' in different file

### JS

#### Feature support
- [`KT-13544`](https://youtrack.jetbrains.com/issue/KT-13544) Support type aliases in JS
- [`KT-13345`](https://youtrack.jetbrains.com/issue/KT-13345) Support class literals in JS

#### Library updates
- [`KT-18`](https://youtrack.jetbrains.com/issue/KT-18) Move exceptions from `java.lang` to `kotlin` package
- [`KT-12386`](https://youtrack.jetbrains.com/issue/KT-12386) Rewrite JS collections in Kotlin, move them to `kotlin.collections` package
- [`KT-7809`](https://youtrack.jetbrains.com/issue/KT-7809) Make Collection implementations conform to their declared interfaces
- [`KT-7473`](https://youtrack.jetbrains.com/issue/KT-7473) Make AbstractCollection.equals check object type
- [`KT-13429`](https://youtrack.jetbrains.com/issue/KT-13429) Make 'remove' on fresh iterator throw exception  instead of removing last element
- [`KT-13459`](https://youtrack.jetbrains.com/issue/KT-13459) Make JS implementation of ArrayList::add(index, element) check the index is in konstid range
- [`KT-8724`](https://youtrack.jetbrains.com/issue/KT-8724) Fix MutableIterator.remove() for HashMap
- [`KT-10786`](https://youtrack.jetbrains.com/issue/KT-10786) Make Map.keys return view of map keys instead of snapshot
- [`KT-14194`](https://youtrack.jetbrains.com/issue/KT-14194) Make HashMap.putAll implementation not to call getKey/getValue

### Standard Library

#### Backward compatibility
- [`KT-14297`](https://youtrack.jetbrains.com/issue/KT-14297) Add @SinceKotlin annotation to support compatibility with compilation against older standard library
- [`KT-14213`](https://youtrack.jetbrains.com/issue/KT-14213) Ensure printStackTrace can be called with -language-version 1.0

#### Enhancements
- [`KEEP-53`](https://github.com/Kotlin/KEEP/blob/master/proposals/stdlib/abstract-collections.md) Provide two distinct hierarchies of abstract collections: one for implementing read-only/immutable collections, and other for implementing mutable collections
- [`KEEP-13`](https://github.com/Kotlin/KEEP/blob/master/proposals/stdlib/map-copying.md) Provide extension functions to copy maps
- [`KT-18`](https://youtrack.jetbrains.com/issue/KT-18) Introduce type aliases for common exceptions from `java.lang` in `kotlin` package
- [`KT-12762`](https://youtrack.jetbrains.com/issue/KT-12762) Make `kotlin.ranges.until` return an empty range for "illegal" 'to' parameter
- [`KT-12894`](https://youtrack.jetbrains.com/issue/KT-12894) Allow nullable receiver for `use` extension

### Reflection

#### New features
- [`KT-8998`](https://youtrack.jetbrains.com/issue/KT-8998) Introduce comprehensive API to work with KType instances 
- [`KT-10447`](https://youtrack.jetbrains.com/issue/KT-10447) Provide a way to check if a KClass is a data class
- [`KT-11284`](https://youtrack.jetbrains.com/issue/KT-11284) Add KClass<T>.cast extension
- [`KT-13106`](https://youtrack.jetbrains.com/issue/KT-13106) Support annotation constructors in reflection

#### Optimizations
- [`KT-10651`](https://youtrack.jetbrains.com/issue/KT-10651) Optimize KClass.simpleName

### IDE

###### New features
- [`KT-12903`](https://youtrack.jetbrains.com/issue/KT-12903) Implement "Inline type alias" refactoring
- [`KT-12902`](https://youtrack.jetbrains.com/issue/KT-12902) Implement "Introduce type alias" refactoring
- [`KT-12904`](https://youtrack.jetbrains.com/issue/KT-12904) Implement "Create type alias from usage" quick fix
- [`KT-9016`](https://youtrack.jetbrains.com/issue/KT-9016) Make use of named higher order function parameters
- [`KT-12205`](https://youtrack.jetbrains.com/issue/KT-12205) Suggest import of Kotlin static members in editor with Java source
- [`KT-13941`](https://youtrack.jetbrains.com/issue/KT-13941) Implement intention for introducing destructured lambda parameters when it's possible
- [`KT-13943`](https://youtrack.jetbrains.com/issue/KT-13943) Implement inspection and quickfix for to detect a manual destructuring of for / lambda parameter

###### Issues fixed
- [`KT-13004`](https://youtrack.jetbrains.com/issue/KT-13004) Support bound method references in completion
- [`KT-13242`](https://youtrack.jetbrains.com/issue/KT-13242) Suggest 'typealias' keyword in completion
- [`KT-13244`](https://youtrack.jetbrains.com/issue/KT-13244) Override/Implement Members: Do not expand type aliases in the generated members
- [`KT-13611`](https://youtrack.jetbrains.com/issue/KT-13611) Go to Class: Fix presentation of type aliases
- [`KT-13759`](https://youtrack.jetbrains.com/issue/KT-13759) Rename: Process object-wrapping alias references
- [`KT-13955`](https://youtrack.jetbrains.com/issue/KT-13955) Find Usages: Add special type for usages inside of type aliases
- [`KT-13479`](https://youtrack.jetbrains.com/issue/KT-13479) Support navigation to type aliases from binaries
- [`KT-13766`](https://youtrack.jetbrains.com/issue/KT-13766) Fix optimize imports not to add wrong and unnecessary import because of type alias
- [`KT-12949`](https://youtrack.jetbrains.com/issue/KT-12949) Consider type aliases as candidates for import
- [`KT-13266`](https://youtrack.jetbrains.com/issue/KT-13266) Suggest non-imported type aliases in completion
- [`KT-13689`](https://youtrack.jetbrains.com/issue/KT-13689) Do not treat type alias constructor usage as original type usage for optimize imports

### Scripting

- A new library `kotlin-script-util` containing utilities for implementing kotlin script support  
- [`KT-7880`](https://youtrack.jetbrains.com/issue/KT-7880) Experimental support for JSR 223 Scripting API
- [`KT-13975`](https://youtrack.jetbrains.com/issue/KT-13975), [`KT-14264`](https://youtrack.jetbrains.com/issue/KT-14264) Convert error on retrieving gradle plugin settings to warning
- Implement support for custom template-based scripts in command-line compiler, maven and gradle plugins

## 1.1-M01 (EAP-1)

### Language features

+ **Coroutines (async/await, generators)** ([proposal](https://github.com/Kotlin/kotlin-coroutines))

    Current limitations:

    - for some cases type inference is not supported yet
    - limited IDE support
    - allowed only one `handleResult` function: [design](https://github.com/Kotlin/kotlin-coroutines/blob/master/kotlin-coroutines-informal.md#result-handlers)
    - handling `finally` blocks is not supported: [issue](https://github.com/Kotlin/kotlin-coroutines/issues/1)

+ **Bound callable references** ([proposal](https://github.com/Kotlin/KEEP/issues/5))

+ **Type aliases** ([proposal](https://github.com/Kotlin/KEEP/issues/4))

    Current limitations:
    - type alias constructors for inner classes are not supported yet
    - annotations on type alias are not supported yet
    - limited IDE support

+ **Local delegated properties** ([proposal](https://github.com/Kotlin/KEEP/issues/25))

+ **JDK dependent built-in classes** ([proposal](https://github.com/Kotlin/KEEP/issues/30))

+ **Sealed class inheritors in the same file** ([proposal](https://github.com/Kotlin/KEEP/issues/29))
+ **Allow base classes for data classes** ([proposal](https://github.com/Kotlin/KEEP/issues/31))


### Scripting

- Implement support for [Script Definition Template](https://github.com/Kotlin/KEEP/blob/da9f3ec5f78429e7560bfc284cb7f52e02282b1f/proposals/script-definition-template.md)
and related functionality, except the following parts:
  - automatic script templates discovery is not implemented
  - `@file:ScriptTemplate` annotation is not supported
  - the parameters `javaHome` and `scripts` from `KotlinScriptExternalDependencies` are not used yet
- Implement support for custom template-based scripts in IDEA: resolving, completion and navigation to symbols from script classpath and sources
- Implement GradleScriptTemplatesProvider extension that supplies a script template if gradle with
[kotlin script support](https://github.com/gradle/gradle-script-kotlin) is used in the project


### Compiler

###### Issues fixed
- [`KT-4779`](https://youtrack.jetbrains.com/issue/KT-4779) Generate default methods for implementations in interfaces
- [`KT-11780`](https://youtrack.jetbrains.com/issue/KT-11780) Fixed incorrect "No cast needed" warning
- [`KT-12156`](https://youtrack.jetbrains.com/issue/KT-12156) Fixed incorrect error on `inline` modifier inside final class
- [`KT-12358`](https://youtrack.jetbrains.com/issue/KT-12358) Report missing error "Abstract member not implemented" when a fake method of 'Any' is inherited from an interface
- [`KT-6206`](https://youtrack.jetbrains.com/issue/KT-6206) Generate equals/hashCode/toString in data class always unless it'll cause a JVM signature clash error
- [`KT-8990`](https://youtrack.jetbrains.com/issue/KT-8990) Fixed incorrect error "virtual member hidden" for a private method of an inner class
- [`KT-12429`](https://youtrack.jetbrains.com/issue/KT-12429) Fixed visibility checks for annotation usage on top-level declarations
- [`KT-5068`](https://youtrack.jetbrains.com/issue/KT-5068) Introduced a special diagnostic message for "type mismatch" errors such as `fun f(): Int = { 1 }`.

### Standard Library

- [`KT-8254`](https://youtrack.jetbrains.com/issue/KT-8254) Provide standard library supplement artifacts for using with JDK 7 and 8.
These artifacts include extensions for the types available in the latter JDKs, such as `AutoCloseable.use` ([`KT-5899`](https://youtrack.jetbrains.com/issue/KT-5899)) or `Stream.toList`.
- [`KT-12753`](https://youtrack.jetbrains.com/issue/KT-12753) Provide an access to named group matches of `Regex` match result (for JDK 8 only).
- Add `assertFails` overload with message to kotlin-test.


### IDE

###### New features

+ [`KT-12019`](https://youtrack.jetbrains.com/issue/KT-12019) Introduce "redundant `if`" inspection

###### Issues fixed

+ [`KT-12389`](https://youtrack.jetbrains.com/issue/KT-12389) Do not exit from REPL when toString() of user class throws an exception
+ [`KT-12129`](https://youtrack.jetbrains.com/issue/KT-12129) Fixed link on api reference page in KDoc
