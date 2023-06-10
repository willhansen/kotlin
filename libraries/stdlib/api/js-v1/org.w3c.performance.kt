/*∆*/ public external interface GlobalPerformance {
/*∆*/     public abstract konst performance: org.w3c.performance.Performance { get; }
/*∆*/ }
/*∆*/ 
/*∆*/ public abstract external class Performance : org.w3c.dom.events.EventTarget {
/*∆*/     public constructor Performance()
/*∆*/ 
/*∆*/     public open konst navigation: org.w3c.performance.PerformanceNavigation { get; }
/*∆*/ 
/*∆*/     public open konst timing: org.w3c.performance.PerformanceTiming { get; }
/*∆*/ 
/*∆*/     public final fun now(): kotlin.Double
/*∆*/ }
/*∆*/ 
/*∆*/ public abstract external class PerformanceNavigation {
/*∆*/     public constructor PerformanceNavigation()
/*∆*/ 
/*∆*/     public open konst redirectCount: kotlin.Short { get; }
/*∆*/ 
/*∆*/     public open konst type: kotlin.Short { get; }
/*∆*/ 
/*∆*/     public companion object of PerformanceNavigation {
/*∆*/         public final konst TYPE_BACK_FORWARD: kotlin.Short { get; }
/*∆*/ 
/*∆*/         public final konst TYPE_NAVIGATE: kotlin.Short { get; }
/*∆*/ 
/*∆*/         public final konst TYPE_RELOAD: kotlin.Short { get; }
/*∆*/ 
/*∆*/         public final konst TYPE_RESERVED: kotlin.Short { get; }
/*∆*/     }
/*∆*/ }
/*∆*/ 
/*∆*/ public abstract external class PerformanceTiming {
/*∆*/     public constructor PerformanceTiming()
/*∆*/ 
/*∆*/     public open konst connectEnd: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst connectStart: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst domComplete: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst domContentLoadedEventEnd: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst domContentLoadedEventStart: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst domInteractive: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst domLoading: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst domainLookupEnd: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst domainLookupStart: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst fetchStart: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst loadEventEnd: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst loadEventStart: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst navigationStart: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst redirectEnd: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst redirectStart: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst requestStart: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst responseEnd: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst responseStart: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst secureConnectionStart: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst unloadEventEnd: kotlin.Number { get; }
/*∆*/ 
/*∆*/     public open konst unloadEventStart: kotlin.Number { get; }
/*∆*/ }