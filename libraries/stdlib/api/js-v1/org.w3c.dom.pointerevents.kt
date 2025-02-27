/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline fun PointerEventInit(pointerId: kotlin.Int? = ..., width: kotlin.Double? = ..., height: kotlin.Double? = ..., pressure: kotlin.Float? = ..., tangentialPressure: kotlin.Float? = ..., tiltX: kotlin.Int? = ..., tiltY: kotlin.Int? = ..., twist: kotlin.Int? = ..., pointerType: kotlin.String? = ..., isPrimary: kotlin.Boolean? = ..., screenX: kotlin.Int? = ..., screenY: kotlin.Int? = ..., clientX: kotlin.Int? = ..., clientY: kotlin.Int? = ..., button: kotlin.Short? = ..., buttons: kotlin.Short? = ..., relatedTarget: org.w3c.dom.events.EventTarget? = ..., region: kotlin.String? = ..., ctrlKey: kotlin.Boolean? = ..., shiftKey: kotlin.Boolean? = ..., altKey: kotlin.Boolean? = ..., metaKey: kotlin.Boolean? = ..., modifierAltGraph: kotlin.Boolean? = ..., modifierCapsLock: kotlin.Boolean? = ..., modifierFn: kotlin.Boolean? = ..., modifierFnLock: kotlin.Boolean? = ..., modifierHyper: kotlin.Boolean? = ..., modifierNumLock: kotlin.Boolean? = ..., modifierScrollLock: kotlin.Boolean? = ..., modifierSuper: kotlin.Boolean? = ..., modifierSymbol: kotlin.Boolean? = ..., modifierSymbolLock: kotlin.Boolean? = ..., view: org.w3c.dom.Window? = ..., detail: kotlin.Int? = ..., bubbles: kotlin.Boolean? = ..., cancelable: kotlin.Boolean? = ..., composed: kotlin.Boolean? = ...): org.w3c.dom.pointerevents.PointerEventInit
/*∆*/ 
/*∆*/ public open external class PointerEvent : org.w3c.dom.events.MouseEvent {
/*∆*/     public constructor PointerEvent(type: kotlin.String, eventInitDict: org.w3c.dom.pointerevents.PointerEventInit = ...)
/*∆*/ 
/*∆*/     public open konst height: kotlin.Double { get; }
/*∆*/ 
/*∆*/     public open konst isPrimary: kotlin.Boolean { get; }
/*∆*/ 
/*∆*/     public open konst pointerId: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open konst pointerType: kotlin.String { get; }
/*∆*/ 
/*∆*/     public open konst pressure: kotlin.Float { get; }
/*∆*/ 
/*∆*/     public open konst tangentialPressure: kotlin.Float { get; }
/*∆*/ 
/*∆*/     public open konst tiltX: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open konst tiltY: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open konst twist: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open konst width: kotlin.Double { get; }
/*∆*/ 
/*∆*/     public companion object of PointerEvent {
/*∆*/         public final konst AT_TARGET: kotlin.Short { get; }
/*∆*/ 
/*∆*/         public final konst BUBBLING_PHASE: kotlin.Short { get; }
/*∆*/ 
/*∆*/         public final konst CAPTURING_PHASE: kotlin.Short { get; }
/*∆*/ 
/*∆*/         public final konst NONE: kotlin.Short { get; }
/*∆*/     }
/*∆*/ }
/*∆*/ 
/*∆*/ public external interface PointerEventInit : org.w3c.dom.events.MouseEventInit {
/*∆*/     public open var height: kotlin.Double? { get; set; }
/*∆*/ 
/*∆*/     public open var isPrimary: kotlin.Boolean? { get; set; }
/*∆*/ 
/*∆*/     public open var pointerId: kotlin.Int? { get; set; }
/*∆*/ 
/*∆*/     public open var pointerType: kotlin.String? { get; set; }
/*∆*/ 
/*∆*/     public open var pressure: kotlin.Float? { get; set; }
/*∆*/ 
/*∆*/     public open var tangentialPressure: kotlin.Float? { get; set; }
/*∆*/ 
/*∆*/     public open var tiltX: kotlin.Int? { get; set; }
/*∆*/ 
/*∆*/     public open var tiltY: kotlin.Int? { get; set; }
/*∆*/ 
/*∆*/     public open var twist: kotlin.Int? { get; set; }
/*∆*/ 
/*∆*/     public open var width: kotlin.Double? { get; set; }
/*∆*/ }