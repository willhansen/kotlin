/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline fun WebGLContextAttributes(alpha: kotlin.Boolean? = ..., depth: kotlin.Boolean? = ..., stencil: kotlin.Boolean? = ..., antialias: kotlin.Boolean? = ..., premultipliedAlpha: kotlin.Boolean? = ..., preserveDrawingBuffer: kotlin.Boolean? = ..., preferLowPowerToHighPerformance: kotlin.Boolean? = ..., failIfMajorPerformanceCaveat: kotlin.Boolean? = ...): org.khronos.webgl.WebGLContextAttributes
/*∆*/ 
/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline fun WebGLContextEventInit(statusMessage: kotlin.String? = ..., bubbles: kotlin.Boolean? = ..., cancelable: kotlin.Boolean? = ..., composed: kotlin.Boolean? = ...): org.khronos.webgl.WebGLContextEventInit
/*∆*/ 
/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline operator fun org.khronos.webgl.Float32Array.get(index: kotlin.Int): kotlin.Float
/*∆*/ 
/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline operator fun org.khronos.webgl.Float64Array.get(index: kotlin.Int): kotlin.Double
/*∆*/ 
/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline operator fun org.khronos.webgl.Int16Array.get(index: kotlin.Int): kotlin.Short
/*∆*/ 
/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline operator fun org.khronos.webgl.Int32Array.get(index: kotlin.Int): kotlin.Int
/*∆*/ 
/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline operator fun org.khronos.webgl.Int8Array.get(index: kotlin.Int): kotlin.Byte
/*∆*/ 
/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline operator fun org.khronos.webgl.Uint16Array.get(index: kotlin.Int): kotlin.Short
/*∆*/ 
/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline operator fun org.khronos.webgl.Uint32Array.get(index: kotlin.Int): kotlin.Int
/*∆*/ 
/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline operator fun org.khronos.webgl.Uint8Array.get(index: kotlin.Int): kotlin.Byte
/*∆*/ 
/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline operator fun org.khronos.webgl.Uint8ClampedArray.get(index: kotlin.Int): kotlin.Byte
/*∆*/ 
/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline operator fun org.khronos.webgl.Float32Array.set(index: kotlin.Int, konstue: kotlin.Float): kotlin.Unit
/*∆*/ 
/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline operator fun org.khronos.webgl.Float64Array.set(index: kotlin.Int, konstue: kotlin.Double): kotlin.Unit
/*∆*/ 
/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline operator fun org.khronos.webgl.Int16Array.set(index: kotlin.Int, konstue: kotlin.Short): kotlin.Unit
/*∆*/ 
/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline operator fun org.khronos.webgl.Int32Array.set(index: kotlin.Int, konstue: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline operator fun org.khronos.webgl.Int8Array.set(index: kotlin.Int, konstue: kotlin.Byte): kotlin.Unit
/*∆*/ 
/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline operator fun org.khronos.webgl.Uint16Array.set(index: kotlin.Int, konstue: kotlin.Short): kotlin.Unit
/*∆*/ 
/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline operator fun org.khronos.webgl.Uint32Array.set(index: kotlin.Int, konstue: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline operator fun org.khronos.webgl.Uint8Array.set(index: kotlin.Int, konstue: kotlin.Byte): kotlin.Unit
/*∆*/ 
/*∆*/ @kotlin.internal.InlineOnly
/*∆*/ public inline operator fun org.khronos.webgl.Uint8ClampedArray.set(index: kotlin.Int, konstue: kotlin.Byte): kotlin.Unit
/*∆*/ 
/*∆*/ public open external class ArrayBuffer : org.khronos.webgl.BufferDataSource {
/*∆*/     public constructor ArrayBuffer(length: kotlin.Int)
/*∆*/ 
/*∆*/     public open konst byteLength: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public final fun slice(begin: kotlin.Int, end: kotlin.Int = ...): org.khronos.webgl.ArrayBuffer
/*∆*/ 
/*∆*/     public companion object of ArrayBuffer {
/*∆*/         public final fun isView(konstue: kotlin.Any?): kotlin.Boolean
/*∆*/     }
/*∆*/ }
/*∆*/ 
/*∆*/ public external interface ArrayBufferView : org.khronos.webgl.BufferDataSource {
/*∆*/     public abstract konst buffer: org.khronos.webgl.ArrayBuffer { get; }
/*∆*/ 
/*∆*/     public abstract konst byteLength: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public abstract konst byteOffset: kotlin.Int { get; }
/*∆*/ }
/*∆*/ 
/*∆*/ public external interface BufferDataSource {
/*∆*/ }
/*∆*/ 
/*∆*/ public open external class DataView : org.khronos.webgl.ArrayBufferView {
/*∆*/     public constructor DataView(buffer: org.khronos.webgl.ArrayBuffer, byteOffset: kotlin.Int = ..., byteLength: kotlin.Int = ...)
/*∆*/ 
/*∆*/     public open override konst buffer: org.khronos.webgl.ArrayBuffer { get; }
/*∆*/ 
/*∆*/     public open override konst byteLength: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open override konst byteOffset: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public final fun getFloat32(byteOffset: kotlin.Int, littleEndian: kotlin.Boolean = ...): kotlin.Float
/*∆*/ 
/*∆*/     public final fun getFloat64(byteOffset: kotlin.Int, littleEndian: kotlin.Boolean = ...): kotlin.Double
/*∆*/ 
/*∆*/     public final fun getInt16(byteOffset: kotlin.Int, littleEndian: kotlin.Boolean = ...): kotlin.Short
/*∆*/ 
/*∆*/     public final fun getInt32(byteOffset: kotlin.Int, littleEndian: kotlin.Boolean = ...): kotlin.Int
/*∆*/ 
/*∆*/     public final fun getInt8(byteOffset: kotlin.Int): kotlin.Byte
/*∆*/ 
/*∆*/     public final fun getUint16(byteOffset: kotlin.Int, littleEndian: kotlin.Boolean = ...): kotlin.Short
/*∆*/ 
/*∆*/     public final fun getUint32(byteOffset: kotlin.Int, littleEndian: kotlin.Boolean = ...): kotlin.Int
/*∆*/ 
/*∆*/     public final fun getUint8(byteOffset: kotlin.Int): kotlin.Byte
/*∆*/ 
/*∆*/     public final fun setFloat32(byteOffset: kotlin.Int, konstue: kotlin.Float, littleEndian: kotlin.Boolean = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun setFloat64(byteOffset: kotlin.Int, konstue: kotlin.Double, littleEndian: kotlin.Boolean = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun setInt16(byteOffset: kotlin.Int, konstue: kotlin.Short, littleEndian: kotlin.Boolean = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun setInt32(byteOffset: kotlin.Int, konstue: kotlin.Int, littleEndian: kotlin.Boolean = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun setInt8(byteOffset: kotlin.Int, konstue: kotlin.Byte): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun setUint16(byteOffset: kotlin.Int, konstue: kotlin.Short, littleEndian: kotlin.Boolean = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun setUint32(byteOffset: kotlin.Int, konstue: kotlin.Int, littleEndian: kotlin.Boolean = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun setUint8(byteOffset: kotlin.Int, konstue: kotlin.Byte): kotlin.Unit
/*∆*/ }
/*∆*/ 
/*∆*/ public open external class Float32Array : org.khronos.webgl.ArrayBufferView {
/*∆*/     public constructor Float32Array(array: kotlin.Array<kotlin.Float>)
/*∆*/ 
/*∆*/     public constructor Float32Array(length: kotlin.Int)
/*∆*/ 
/*∆*/     public constructor Float32Array(buffer: org.khronos.webgl.ArrayBuffer, byteOffset: kotlin.Int = ..., length: kotlin.Int = ...)
/*∆*/ 
/*∆*/     public constructor Float32Array(array: org.khronos.webgl.Float32Array)
/*∆*/ 
/*∆*/     public open override konst buffer: org.khronos.webgl.ArrayBuffer { get; }
/*∆*/ 
/*∆*/     public open override konst byteLength: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open override konst byteOffset: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open konst length: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public final fun set(array: kotlin.Array<kotlin.Float>, offset: kotlin.Int = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun set(array: org.khronos.webgl.Float32Array, offset: kotlin.Int = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun subarray(start: kotlin.Int, end: kotlin.Int): org.khronos.webgl.Float32Array
/*∆*/ 
/*∆*/     public companion object of Float32Array {
/*∆*/         public final konst BYTES_PER_ELEMENT: kotlin.Int { get; }
/*∆*/     }
/*∆*/ }
/*∆*/ 
/*∆*/ public open external class Float64Array : org.khronos.webgl.ArrayBufferView {
/*∆*/     public constructor Float64Array(array: kotlin.Array<kotlin.Double>)
/*∆*/ 
/*∆*/     public constructor Float64Array(length: kotlin.Int)
/*∆*/ 
/*∆*/     public constructor Float64Array(buffer: org.khronos.webgl.ArrayBuffer, byteOffset: kotlin.Int = ..., length: kotlin.Int = ...)
/*∆*/ 
/*∆*/     public constructor Float64Array(array: org.khronos.webgl.Float64Array)
/*∆*/ 
/*∆*/     public open override konst buffer: org.khronos.webgl.ArrayBuffer { get; }
/*∆*/ 
/*∆*/     public open override konst byteLength: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open override konst byteOffset: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open konst length: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public final fun set(array: kotlin.Array<kotlin.Double>, offset: kotlin.Int = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun set(array: org.khronos.webgl.Float64Array, offset: kotlin.Int = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun subarray(start: kotlin.Int, end: kotlin.Int): org.khronos.webgl.Float64Array
/*∆*/ 
/*∆*/     public companion object of Float64Array {
/*∆*/         public final konst BYTES_PER_ELEMENT: kotlin.Int { get; }
/*∆*/     }
/*∆*/ }
/*∆*/ 
/*∆*/ public open external class Int16Array : org.khronos.webgl.ArrayBufferView {
/*∆*/     public constructor Int16Array(array: kotlin.Array<kotlin.Short>)
/*∆*/ 
/*∆*/     public constructor Int16Array(length: kotlin.Int)
/*∆*/ 
/*∆*/     public constructor Int16Array(buffer: org.khronos.webgl.ArrayBuffer, byteOffset: kotlin.Int = ..., length: kotlin.Int = ...)
/*∆*/ 
/*∆*/     public constructor Int16Array(array: org.khronos.webgl.Int16Array)
/*∆*/ 
/*∆*/     public open override konst buffer: org.khronos.webgl.ArrayBuffer { get; }
/*∆*/ 
/*∆*/     public open override konst byteLength: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open override konst byteOffset: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open konst length: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public final fun set(array: kotlin.Array<kotlin.Short>, offset: kotlin.Int = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun set(array: org.khronos.webgl.Int16Array, offset: kotlin.Int = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun subarray(start: kotlin.Int, end: kotlin.Int): org.khronos.webgl.Int16Array
/*∆*/ 
/*∆*/     public companion object of Int16Array {
/*∆*/         public final konst BYTES_PER_ELEMENT: kotlin.Int { get; }
/*∆*/     }
/*∆*/ }
/*∆*/ 
/*∆*/ public open external class Int32Array : org.khronos.webgl.ArrayBufferView {
/*∆*/     public constructor Int32Array(array: kotlin.Array<kotlin.Int>)
/*∆*/ 
/*∆*/     public constructor Int32Array(length: kotlin.Int)
/*∆*/ 
/*∆*/     public constructor Int32Array(buffer: org.khronos.webgl.ArrayBuffer, byteOffset: kotlin.Int = ..., length: kotlin.Int = ...)
/*∆*/ 
/*∆*/     public constructor Int32Array(array: org.khronos.webgl.Int32Array)
/*∆*/ 
/*∆*/     public open override konst buffer: org.khronos.webgl.ArrayBuffer { get; }
/*∆*/ 
/*∆*/     public open override konst byteLength: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open override konst byteOffset: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open konst length: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public final fun set(array: kotlin.Array<kotlin.Int>, offset: kotlin.Int = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun set(array: org.khronos.webgl.Int32Array, offset: kotlin.Int = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun subarray(start: kotlin.Int, end: kotlin.Int): org.khronos.webgl.Int32Array
/*∆*/ 
/*∆*/     public companion object of Int32Array {
/*∆*/         public final konst BYTES_PER_ELEMENT: kotlin.Int { get; }
/*∆*/     }
/*∆*/ }
/*∆*/ 
/*∆*/ public open external class Int8Array : org.khronos.webgl.ArrayBufferView {
/*∆*/     public constructor Int8Array(array: kotlin.Array<kotlin.Byte>)
/*∆*/ 
/*∆*/     public constructor Int8Array(length: kotlin.Int)
/*∆*/ 
/*∆*/     public constructor Int8Array(buffer: org.khronos.webgl.ArrayBuffer, byteOffset: kotlin.Int = ..., length: kotlin.Int = ...)
/*∆*/ 
/*∆*/     public constructor Int8Array(array: org.khronos.webgl.Int8Array)
/*∆*/ 
/*∆*/     public open override konst buffer: org.khronos.webgl.ArrayBuffer { get; }
/*∆*/ 
/*∆*/     public open override konst byteLength: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open override konst byteOffset: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open konst length: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public final fun set(array: kotlin.Array<kotlin.Byte>, offset: kotlin.Int = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun set(array: org.khronos.webgl.Int8Array, offset: kotlin.Int = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun subarray(start: kotlin.Int, end: kotlin.Int): org.khronos.webgl.Int8Array
/*∆*/ 
/*∆*/     public companion object of Int8Array {
/*∆*/         public final konst BYTES_PER_ELEMENT: kotlin.Int { get; }
/*∆*/     }
/*∆*/ }
/*∆*/ 
/*∆*/ public external interface TexImageSource {
/*∆*/ }
/*∆*/ 
/*∆*/ public open external class Uint16Array : org.khronos.webgl.ArrayBufferView {
/*∆*/     public constructor Uint16Array(array: kotlin.Array<kotlin.Short>)
/*∆*/ 
/*∆*/     public constructor Uint16Array(length: kotlin.Int)
/*∆*/ 
/*∆*/     public constructor Uint16Array(buffer: org.khronos.webgl.ArrayBuffer, byteOffset: kotlin.Int = ..., length: kotlin.Int = ...)
/*∆*/ 
/*∆*/     public constructor Uint16Array(array: org.khronos.webgl.Uint16Array)
/*∆*/ 
/*∆*/     public open override konst buffer: org.khronos.webgl.ArrayBuffer { get; }
/*∆*/ 
/*∆*/     public open override konst byteLength: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open override konst byteOffset: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open konst length: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public final fun set(array: kotlin.Array<kotlin.Short>, offset: kotlin.Int = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun set(array: org.khronos.webgl.Uint16Array, offset: kotlin.Int = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun subarray(start: kotlin.Int, end: kotlin.Int): org.khronos.webgl.Uint16Array
/*∆*/ 
/*∆*/     public companion object of Uint16Array {
/*∆*/         public final konst BYTES_PER_ELEMENT: kotlin.Int { get; }
/*∆*/     }
/*∆*/ }
/*∆*/ 
/*∆*/ public open external class Uint32Array : org.khronos.webgl.ArrayBufferView {
/*∆*/     public constructor Uint32Array(array: kotlin.Array<kotlin.Int>)
/*∆*/ 
/*∆*/     public constructor Uint32Array(length: kotlin.Int)
/*∆*/ 
/*∆*/     public constructor Uint32Array(buffer: org.khronos.webgl.ArrayBuffer, byteOffset: kotlin.Int = ..., length: kotlin.Int = ...)
/*∆*/ 
/*∆*/     public constructor Uint32Array(array: org.khronos.webgl.Uint32Array)
/*∆*/ 
/*∆*/     public open override konst buffer: org.khronos.webgl.ArrayBuffer { get; }
/*∆*/ 
/*∆*/     public open override konst byteLength: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open override konst byteOffset: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open konst length: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public final fun set(array: kotlin.Array<kotlin.Int>, offset: kotlin.Int = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun set(array: org.khronos.webgl.Uint32Array, offset: kotlin.Int = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun subarray(start: kotlin.Int, end: kotlin.Int): org.khronos.webgl.Uint32Array
/*∆*/ 
/*∆*/     public companion object of Uint32Array {
/*∆*/         public final konst BYTES_PER_ELEMENT: kotlin.Int { get; }
/*∆*/     }
/*∆*/ }
/*∆*/ 
/*∆*/ public open external class Uint8Array : org.khronos.webgl.ArrayBufferView {
/*∆*/     public constructor Uint8Array(array: kotlin.Array<kotlin.Byte>)
/*∆*/ 
/*∆*/     public constructor Uint8Array(length: kotlin.Int)
/*∆*/ 
/*∆*/     public constructor Uint8Array(buffer: org.khronos.webgl.ArrayBuffer, byteOffset: kotlin.Int = ..., length: kotlin.Int = ...)
/*∆*/ 
/*∆*/     public constructor Uint8Array(array: org.khronos.webgl.Uint8Array)
/*∆*/ 
/*∆*/     public open override konst buffer: org.khronos.webgl.ArrayBuffer { get; }
/*∆*/ 
/*∆*/     public open override konst byteLength: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open override konst byteOffset: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open konst length: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public final fun set(array: kotlin.Array<kotlin.Byte>, offset: kotlin.Int = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun set(array: org.khronos.webgl.Uint8Array, offset: kotlin.Int = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun subarray(start: kotlin.Int, end: kotlin.Int): org.khronos.webgl.Uint8Array
/*∆*/ 
/*∆*/     public companion object of Uint8Array {
/*∆*/         public final konst BYTES_PER_ELEMENT: kotlin.Int { get; }
/*∆*/     }
/*∆*/ }
/*∆*/ 
/*∆*/ public open external class Uint8ClampedArray : org.khronos.webgl.ArrayBufferView {
/*∆*/     public constructor Uint8ClampedArray(array: kotlin.Array<kotlin.Byte>)
/*∆*/ 
/*∆*/     public constructor Uint8ClampedArray(length: kotlin.Int)
/*∆*/ 
/*∆*/     public constructor Uint8ClampedArray(buffer: org.khronos.webgl.ArrayBuffer, byteOffset: kotlin.Int = ..., length: kotlin.Int = ...)
/*∆*/ 
/*∆*/     public constructor Uint8ClampedArray(array: org.khronos.webgl.Uint8ClampedArray)
/*∆*/ 
/*∆*/     public open override konst buffer: org.khronos.webgl.ArrayBuffer { get; }
/*∆*/ 
/*∆*/     public open override konst byteLength: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open override konst byteOffset: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open konst length: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public final fun set(array: kotlin.Array<kotlin.Byte>, offset: kotlin.Int = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun set(array: org.khronos.webgl.Uint8ClampedArray, offset: kotlin.Int = ...): kotlin.Unit
/*∆*/ 
/*∆*/     public final fun subarray(start: kotlin.Int, end: kotlin.Int): org.khronos.webgl.Uint8ClampedArray
/*∆*/ 
/*∆*/     public companion object of Uint8ClampedArray {
/*∆*/         public final konst BYTES_PER_ELEMENT: kotlin.Int { get; }
/*∆*/     }
/*∆*/ }
/*∆*/ 
/*∆*/ public abstract external class WebGLActiveInfo {
/*∆*/     public constructor WebGLActiveInfo()
/*∆*/ 
/*∆*/     public open konst name: kotlin.String { get; }
/*∆*/ 
/*∆*/     public open konst size: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open konst type: kotlin.Int { get; }
/*∆*/ }
/*∆*/ 
/*∆*/ public abstract external class WebGLBuffer : org.khronos.webgl.WebGLObject {
/*∆*/     public constructor WebGLBuffer()
/*∆*/ }
/*∆*/ 
/*∆*/ public external interface WebGLContextAttributes {
/*∆*/     public open var alpha: kotlin.Boolean? { get; set; }
/*∆*/ 
/*∆*/     public open var antialias: kotlin.Boolean? { get; set; }
/*∆*/ 
/*∆*/     public open var depth: kotlin.Boolean? { get; set; }
/*∆*/ 
/*∆*/     public open var failIfMajorPerformanceCaveat: kotlin.Boolean? { get; set; }
/*∆*/ 
/*∆*/     public open var preferLowPowerToHighPerformance: kotlin.Boolean? { get; set; }
/*∆*/ 
/*∆*/     public open var premultipliedAlpha: kotlin.Boolean? { get; set; }
/*∆*/ 
/*∆*/     public open var preserveDrawingBuffer: kotlin.Boolean? { get; set; }
/*∆*/ 
/*∆*/     public open var stencil: kotlin.Boolean? { get; set; }
/*∆*/ }
/*∆*/ 
/*∆*/ public open external class WebGLContextEvent : org.w3c.dom.events.Event {
/*∆*/     public constructor WebGLContextEvent(type: kotlin.String, eventInit: org.khronos.webgl.WebGLContextEventInit = ...)
/*∆*/ 
/*∆*/     public open konst statusMessage: kotlin.String { get; }
/*∆*/ 
/*∆*/     public companion object of WebGLContextEvent {
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
/*∆*/ public external interface WebGLContextEventInit : org.w3c.dom.EventInit {
/*∆*/     public open var statusMessage: kotlin.String? { get; set; }
/*∆*/ }
/*∆*/ 
/*∆*/ public abstract external class WebGLFramebuffer : org.khronos.webgl.WebGLObject {
/*∆*/     public constructor WebGLFramebuffer()
/*∆*/ }
/*∆*/ 
/*∆*/ public abstract external class WebGLObject {
/*∆*/     public constructor WebGLObject()
/*∆*/ }
/*∆*/ 
/*∆*/ public abstract external class WebGLProgram : org.khronos.webgl.WebGLObject {
/*∆*/     public constructor WebGLProgram()
/*∆*/ }
/*∆*/ 
/*∆*/ public abstract external class WebGLRenderbuffer : org.khronos.webgl.WebGLObject {
/*∆*/     public constructor WebGLRenderbuffer()
/*∆*/ }
/*∆*/ 
/*∆*/ public abstract external class WebGLRenderingContext : org.khronos.webgl.WebGLRenderingContextBase, org.w3c.dom.RenderingContext {
/*∆*/     public constructor WebGLRenderingContext()
/*∆*/ 
/*∆*/     public companion object of WebGLRenderingContext {
/*∆*/         public final konst ACTIVE_ATTRIBUTES: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ACTIVE_TEXTURE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ACTIVE_UNIFORMS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ALIASED_LINE_WIDTH_RANGE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ALIASED_POINT_SIZE_RANGE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ALPHA_BITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ALWAYS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ARRAY_BUFFER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ARRAY_BUFFER_BINDING: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ATTACHED_SHADERS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BACK: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLEND: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLEND_COLOR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLEND_DST_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLEND_DST_RGB: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLEND_EQUATION: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLEND_EQUATION_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLEND_EQUATION_RGB: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLEND_SRC_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLEND_SRC_RGB: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLUE_BITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BOOL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BOOL_VEC2: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BOOL_VEC3: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BOOL_VEC4: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BROWSER_DEFAULT_WEBGL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BUFFER_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BUFFER_USAGE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BYTE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CCW: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CLAMP_TO_EDGE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst COLOR_ATTACHMENT0: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst COLOR_BUFFER_BIT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst COLOR_CLEAR_VALUE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst COLOR_WRITEMASK: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst COMPILE_STATUS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst COMPRESSED_TEXTURE_FORMATS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CONSTANT_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CONSTANT_COLOR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CONTEXT_LOST_WEBGL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CULL_FACE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CULL_FACE_MODE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CURRENT_PROGRAM: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CURRENT_VERTEX_ATTRIB: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CW: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DECR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DECR_WRAP: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DELETE_STATUS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_ATTACHMENT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_BITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_BUFFER_BIT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_CLEAR_VALUE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_COMPONENT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_COMPONENT16: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_FUNC: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_RANGE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_STENCIL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_STENCIL_ATTACHMENT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_TEST: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_WRITEMASK: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DITHER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DONT_CARE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DST_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DST_COLOR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DYNAMIC_DRAW: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ELEMENT_ARRAY_BUFFER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ELEMENT_ARRAY_BUFFER_BINDING: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst EQUAL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FASTEST: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FLOAT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FLOAT_MAT2: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FLOAT_MAT3: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FLOAT_MAT4: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FLOAT_VEC2: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FLOAT_VEC3: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FLOAT_VEC4: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAGMENT_SHADER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_ATTACHMENT_OBJECT_NAME: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_BINDING: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_COMPLETE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_INCOMPLETE_ATTACHMENT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_INCOMPLETE_DIMENSIONS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_UNSUPPORTED: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRONT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRONT_AND_BACK: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRONT_FACE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FUNC_ADD: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FUNC_REVERSE_SUBTRACT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FUNC_SUBTRACT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst GENERATE_MIPMAP_HINT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst GEQUAL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst GREATER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst GREEN_BITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst HIGH_FLOAT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst HIGH_INT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst IMPLEMENTATION_COLOR_READ_FORMAT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst IMPLEMENTATION_COLOR_READ_TYPE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INCR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INCR_WRAP: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INT_VEC2: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INT_VEC3: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INT_VEC4: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INVALID_ENUM: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INVALID_FRAMEBUFFER_OPERATION: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INVALID_OPERATION: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INVALID_VALUE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INVERT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst KEEP: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LEQUAL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LESS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LINEAR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LINEAR_MIPMAP_LINEAR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LINEAR_MIPMAP_NEAREST: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LINES: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LINE_LOOP: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LINE_STRIP: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LINE_WIDTH: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LINK_STATUS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LOW_FLOAT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LOW_INT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LUMINANCE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LUMINANCE_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_COMBINED_TEXTURE_IMAGE_UNITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_CUBE_MAP_TEXTURE_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_FRAGMENT_UNIFORM_VECTORS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_RENDERBUFFER_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_TEXTURE_IMAGE_UNITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_TEXTURE_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_VARYING_VECTORS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_VERTEX_ATTRIBS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_VERTEX_TEXTURE_IMAGE_UNITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_VERTEX_UNIFORM_VECTORS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_VIEWPORT_DIMS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MEDIUM_FLOAT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MEDIUM_INT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MIRRORED_REPEAT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst NEAREST: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst NEAREST_MIPMAP_LINEAR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst NEAREST_MIPMAP_NEAREST: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst NEVER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst NICEST: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst NONE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst NOTEQUAL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst NO_ERROR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ONE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ONE_MINUS_CONSTANT_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ONE_MINUS_CONSTANT_COLOR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ONE_MINUS_DST_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ONE_MINUS_DST_COLOR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ONE_MINUS_SRC_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ONE_MINUS_SRC_COLOR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst OUT_OF_MEMORY: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst PACK_ALIGNMENT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst POINTS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst POLYGON_OFFSET_FACTOR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst POLYGON_OFFSET_FILL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst POLYGON_OFFSET_UNITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RED_BITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_ALPHA_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_BINDING: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_BLUE_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_DEPTH_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_GREEN_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_HEIGHT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_INTERNAL_FORMAT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_RED_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_STENCIL_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_WIDTH: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst REPEAT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst REPLACE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RGB: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RGB565: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RGB5_A1: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RGBA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RGBA4: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SAMPLER_2D: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SAMPLER_CUBE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SAMPLES: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SAMPLE_ALPHA_TO_COVERAGE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SAMPLE_BUFFERS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SAMPLE_COVERAGE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SAMPLE_COVERAGE_INVERT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SAMPLE_COVERAGE_VALUE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SCISSOR_BOX: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SCISSOR_TEST: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SHADER_TYPE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SHADING_LANGUAGE_VERSION: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SHORT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SRC_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SRC_ALPHA_SATURATE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SRC_COLOR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STATIC_DRAW: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_ATTACHMENT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_BACK_FAIL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_BACK_FUNC: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_BACK_PASS_DEPTH_FAIL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_BACK_PASS_DEPTH_PASS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_BACK_REF: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_BACK_VALUE_MASK: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_BACK_WRITEMASK: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_BITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_BUFFER_BIT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_CLEAR_VALUE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_FAIL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_FUNC: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_INDEX: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_INDEX8: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_PASS_DEPTH_FAIL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_PASS_DEPTH_PASS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_REF: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_TEST: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_VALUE_MASK: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_WRITEMASK: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STREAM_DRAW: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SUBPIXEL_BITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE0: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE1: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE10: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE11: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE12: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE13: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE14: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE15: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE16: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE17: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE18: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE19: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE2: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE20: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE21: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE22: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE23: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE24: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE25: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE26: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE27: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE28: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE29: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE3: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE30: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE31: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE4: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE5: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE6: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE7: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE8: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE9: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_2D: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_BINDING_2D: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_BINDING_CUBE_MAP: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_CUBE_MAP: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_CUBE_MAP_NEGATIVE_X: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_CUBE_MAP_NEGATIVE_Y: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_CUBE_MAP_NEGATIVE_Z: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_CUBE_MAP_POSITIVE_X: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_CUBE_MAP_POSITIVE_Y: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_CUBE_MAP_POSITIVE_Z: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_MAG_FILTER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_MIN_FILTER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_WRAP_S: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_WRAP_T: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TRIANGLES: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TRIANGLE_FAN: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TRIANGLE_STRIP: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNPACK_ALIGNMENT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNPACK_COLORSPACE_CONVERSION_WEBGL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNPACK_FLIP_Y_WEBGL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNPACK_PREMULTIPLY_ALPHA_WEBGL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNSIGNED_BYTE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNSIGNED_INT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNSIGNED_SHORT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNSIGNED_SHORT_4_4_4_4: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNSIGNED_SHORT_5_5_5_1: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNSIGNED_SHORT_5_6_5: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VALIDATE_STATUS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VENDOR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VERSION: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VERTEX_ATTRIB_ARRAY_BUFFER_BINDING: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VERTEX_ATTRIB_ARRAY_ENABLED: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VERTEX_ATTRIB_ARRAY_NORMALIZED: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VERTEX_ATTRIB_ARRAY_POINTER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VERTEX_ATTRIB_ARRAY_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VERTEX_ATTRIB_ARRAY_STRIDE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VERTEX_ATTRIB_ARRAY_TYPE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VERTEX_SHADER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VIEWPORT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ZERO: kotlin.Int { get; }
/*∆*/     }
/*∆*/ }
/*∆*/ 
/*∆*/ public external interface WebGLRenderingContextBase {
/*∆*/     public abstract konst canvas: org.w3c.dom.HTMLCanvasElement { get; }
/*∆*/ 
/*∆*/     public abstract konst drawingBufferHeight: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public abstract konst drawingBufferWidth: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public abstract fun activeTexture(texture: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun attachShader(program: org.khronos.webgl.WebGLProgram?, shader: org.khronos.webgl.WebGLShader?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun bindAttribLocation(program: org.khronos.webgl.WebGLProgram?, index: kotlin.Int, name: kotlin.String): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun bindBuffer(target: kotlin.Int, buffer: org.khronos.webgl.WebGLBuffer?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun bindFramebuffer(target: kotlin.Int, framebuffer: org.khronos.webgl.WebGLFramebuffer?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun bindRenderbuffer(target: kotlin.Int, renderbuffer: org.khronos.webgl.WebGLRenderbuffer?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun bindTexture(target: kotlin.Int, texture: org.khronos.webgl.WebGLTexture?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun blendColor(red: kotlin.Float, green: kotlin.Float, blue: kotlin.Float, alpha: kotlin.Float): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun blendEquation(mode: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun blendEquationSeparate(modeRGB: kotlin.Int, modeAlpha: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun blendFunc(sfactor: kotlin.Int, dfactor: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun blendFuncSeparate(srcRGB: kotlin.Int, dstRGB: kotlin.Int, srcAlpha: kotlin.Int, dstAlpha: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun bufferData(target: kotlin.Int, size: kotlin.Int, usage: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun bufferData(target: kotlin.Int, data: org.khronos.webgl.BufferDataSource?, usage: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun bufferSubData(target: kotlin.Int, offset: kotlin.Int, data: org.khronos.webgl.BufferDataSource?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun checkFramebufferStatus(target: kotlin.Int): kotlin.Int
/*∆*/ 
/*∆*/     public abstract fun clear(mask: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun clearColor(red: kotlin.Float, green: kotlin.Float, blue: kotlin.Float, alpha: kotlin.Float): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun clearDepth(depth: kotlin.Float): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun clearStencil(s: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun colorMask(red: kotlin.Boolean, green: kotlin.Boolean, blue: kotlin.Boolean, alpha: kotlin.Boolean): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun compileShader(shader: org.khronos.webgl.WebGLShader?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun compressedTexImage2D(target: kotlin.Int, level: kotlin.Int, internalformat: kotlin.Int, width: kotlin.Int, height: kotlin.Int, border: kotlin.Int, data: org.khronos.webgl.ArrayBufferView): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun compressedTexSubImage2D(target: kotlin.Int, level: kotlin.Int, xoffset: kotlin.Int, yoffset: kotlin.Int, width: kotlin.Int, height: kotlin.Int, format: kotlin.Int, data: org.khronos.webgl.ArrayBufferView): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun copyTexImage2D(target: kotlin.Int, level: kotlin.Int, internalformat: kotlin.Int, x: kotlin.Int, y: kotlin.Int, width: kotlin.Int, height: kotlin.Int, border: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun copyTexSubImage2D(target: kotlin.Int, level: kotlin.Int, xoffset: kotlin.Int, yoffset: kotlin.Int, x: kotlin.Int, y: kotlin.Int, width: kotlin.Int, height: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun createBuffer(): org.khronos.webgl.WebGLBuffer?
/*∆*/ 
/*∆*/     public abstract fun createFramebuffer(): org.khronos.webgl.WebGLFramebuffer?
/*∆*/ 
/*∆*/     public abstract fun createProgram(): org.khronos.webgl.WebGLProgram?
/*∆*/ 
/*∆*/     public abstract fun createRenderbuffer(): org.khronos.webgl.WebGLRenderbuffer?
/*∆*/ 
/*∆*/     public abstract fun createShader(type: kotlin.Int): org.khronos.webgl.WebGLShader?
/*∆*/ 
/*∆*/     public abstract fun createTexture(): org.khronos.webgl.WebGLTexture?
/*∆*/ 
/*∆*/     public abstract fun cullFace(mode: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun deleteBuffer(buffer: org.khronos.webgl.WebGLBuffer?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun deleteFramebuffer(framebuffer: org.khronos.webgl.WebGLFramebuffer?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun deleteProgram(program: org.khronos.webgl.WebGLProgram?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun deleteRenderbuffer(renderbuffer: org.khronos.webgl.WebGLRenderbuffer?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun deleteShader(shader: org.khronos.webgl.WebGLShader?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun deleteTexture(texture: org.khronos.webgl.WebGLTexture?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun depthFunc(func: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun depthMask(flag: kotlin.Boolean): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun depthRange(zNear: kotlin.Float, zFar: kotlin.Float): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun detachShader(program: org.khronos.webgl.WebGLProgram?, shader: org.khronos.webgl.WebGLShader?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun disable(cap: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun disableVertexAttribArray(index: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun drawArrays(mode: kotlin.Int, first: kotlin.Int, count: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun drawElements(mode: kotlin.Int, count: kotlin.Int, type: kotlin.Int, offset: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun enable(cap: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun enableVertexAttribArray(index: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun finish(): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun flush(): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun framebufferRenderbuffer(target: kotlin.Int, attachment: kotlin.Int, renderbuffertarget: kotlin.Int, renderbuffer: org.khronos.webgl.WebGLRenderbuffer?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun framebufferTexture2D(target: kotlin.Int, attachment: kotlin.Int, textarget: kotlin.Int, texture: org.khronos.webgl.WebGLTexture?, level: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun frontFace(mode: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun generateMipmap(target: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun getActiveAttrib(program: org.khronos.webgl.WebGLProgram?, index: kotlin.Int): org.khronos.webgl.WebGLActiveInfo?
/*∆*/ 
/*∆*/     public abstract fun getActiveUniform(program: org.khronos.webgl.WebGLProgram?, index: kotlin.Int): org.khronos.webgl.WebGLActiveInfo?
/*∆*/ 
/*∆*/     public abstract fun getAttachedShaders(program: org.khronos.webgl.WebGLProgram?): kotlin.Array<org.khronos.webgl.WebGLShader>?
/*∆*/ 
/*∆*/     public abstract fun getAttribLocation(program: org.khronos.webgl.WebGLProgram?, name: kotlin.String): kotlin.Int
/*∆*/ 
/*∆*/     public abstract fun getBufferParameter(target: kotlin.Int, pname: kotlin.Int): kotlin.Any?
/*∆*/ 
/*∆*/     public abstract fun getContextAttributes(): org.khronos.webgl.WebGLContextAttributes?
/*∆*/ 
/*∆*/     public abstract fun getError(): kotlin.Int
/*∆*/ 
/*∆*/     public abstract fun getExtension(name: kotlin.String): dynamic
/*∆*/ 
/*∆*/     public abstract fun getFramebufferAttachmentParameter(target: kotlin.Int, attachment: kotlin.Int, pname: kotlin.Int): kotlin.Any?
/*∆*/ 
/*∆*/     public abstract fun getParameter(pname: kotlin.Int): kotlin.Any?
/*∆*/ 
/*∆*/     public abstract fun getProgramInfoLog(program: org.khronos.webgl.WebGLProgram?): kotlin.String?
/*∆*/ 
/*∆*/     public abstract fun getProgramParameter(program: org.khronos.webgl.WebGLProgram?, pname: kotlin.Int): kotlin.Any?
/*∆*/ 
/*∆*/     public abstract fun getRenderbufferParameter(target: kotlin.Int, pname: kotlin.Int): kotlin.Any?
/*∆*/ 
/*∆*/     public abstract fun getShaderInfoLog(shader: org.khronos.webgl.WebGLShader?): kotlin.String?
/*∆*/ 
/*∆*/     public abstract fun getShaderParameter(shader: org.khronos.webgl.WebGLShader?, pname: kotlin.Int): kotlin.Any?
/*∆*/ 
/*∆*/     public abstract fun getShaderPrecisionFormat(shadertype: kotlin.Int, precisiontype: kotlin.Int): org.khronos.webgl.WebGLShaderPrecisionFormat?
/*∆*/ 
/*∆*/     public abstract fun getShaderSource(shader: org.khronos.webgl.WebGLShader?): kotlin.String?
/*∆*/ 
/*∆*/     public abstract fun getSupportedExtensions(): kotlin.Array<kotlin.String>?
/*∆*/ 
/*∆*/     public abstract fun getTexParameter(target: kotlin.Int, pname: kotlin.Int): kotlin.Any?
/*∆*/ 
/*∆*/     public abstract fun getUniform(program: org.khronos.webgl.WebGLProgram?, location: org.khronos.webgl.WebGLUniformLocation?): kotlin.Any?
/*∆*/ 
/*∆*/     public abstract fun getUniformLocation(program: org.khronos.webgl.WebGLProgram?, name: kotlin.String): org.khronos.webgl.WebGLUniformLocation?
/*∆*/ 
/*∆*/     public abstract fun getVertexAttrib(index: kotlin.Int, pname: kotlin.Int): kotlin.Any?
/*∆*/ 
/*∆*/     public abstract fun getVertexAttribOffset(index: kotlin.Int, pname: kotlin.Int): kotlin.Int
/*∆*/ 
/*∆*/     public abstract fun hint(target: kotlin.Int, mode: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun isBuffer(buffer: org.khronos.webgl.WebGLBuffer?): kotlin.Boolean
/*∆*/ 
/*∆*/     public abstract fun isContextLost(): kotlin.Boolean
/*∆*/ 
/*∆*/     public abstract fun isEnabled(cap: kotlin.Int): kotlin.Boolean
/*∆*/ 
/*∆*/     public abstract fun isFramebuffer(framebuffer: org.khronos.webgl.WebGLFramebuffer?): kotlin.Boolean
/*∆*/ 
/*∆*/     public abstract fun isProgram(program: org.khronos.webgl.WebGLProgram?): kotlin.Boolean
/*∆*/ 
/*∆*/     public abstract fun isRenderbuffer(renderbuffer: org.khronos.webgl.WebGLRenderbuffer?): kotlin.Boolean
/*∆*/ 
/*∆*/     public abstract fun isShader(shader: org.khronos.webgl.WebGLShader?): kotlin.Boolean
/*∆*/ 
/*∆*/     public abstract fun isTexture(texture: org.khronos.webgl.WebGLTexture?): kotlin.Boolean
/*∆*/ 
/*∆*/     public abstract fun lineWidth(width: kotlin.Float): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun linkProgram(program: org.khronos.webgl.WebGLProgram?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun pixelStorei(pname: kotlin.Int, param: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun polygonOffset(factor: kotlin.Float, units: kotlin.Float): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun readPixels(x: kotlin.Int, y: kotlin.Int, width: kotlin.Int, height: kotlin.Int, format: kotlin.Int, type: kotlin.Int, pixels: org.khronos.webgl.ArrayBufferView?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun renderbufferStorage(target: kotlin.Int, internalformat: kotlin.Int, width: kotlin.Int, height: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun sampleCoverage(konstue: kotlin.Float, invert: kotlin.Boolean): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun scissor(x: kotlin.Int, y: kotlin.Int, width: kotlin.Int, height: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun shaderSource(shader: org.khronos.webgl.WebGLShader?, source: kotlin.String): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun stencilFunc(func: kotlin.Int, ref: kotlin.Int, mask: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun stencilFuncSeparate(face: kotlin.Int, func: kotlin.Int, ref: kotlin.Int, mask: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun stencilMask(mask: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun stencilMaskSeparate(face: kotlin.Int, mask: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun stencilOp(fail: kotlin.Int, zfail: kotlin.Int, zpass: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun stencilOpSeparate(face: kotlin.Int, fail: kotlin.Int, zfail: kotlin.Int, zpass: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun texImage2D(target: kotlin.Int, level: kotlin.Int, internalformat: kotlin.Int, width: kotlin.Int, height: kotlin.Int, border: kotlin.Int, format: kotlin.Int, type: kotlin.Int, pixels: org.khronos.webgl.ArrayBufferView?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun texImage2D(target: kotlin.Int, level: kotlin.Int, internalformat: kotlin.Int, format: kotlin.Int, type: kotlin.Int, source: org.khronos.webgl.TexImageSource?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun texParameterf(target: kotlin.Int, pname: kotlin.Int, param: kotlin.Float): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun texParameteri(target: kotlin.Int, pname: kotlin.Int, param: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun texSubImage2D(target: kotlin.Int, level: kotlin.Int, xoffset: kotlin.Int, yoffset: kotlin.Int, width: kotlin.Int, height: kotlin.Int, format: kotlin.Int, type: kotlin.Int, pixels: org.khronos.webgl.ArrayBufferView?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun texSubImage2D(target: kotlin.Int, level: kotlin.Int, xoffset: kotlin.Int, yoffset: kotlin.Int, format: kotlin.Int, type: kotlin.Int, source: org.khronos.webgl.TexImageSource?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform1f(location: org.khronos.webgl.WebGLUniformLocation?, x: kotlin.Float): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform1fv(location: org.khronos.webgl.WebGLUniformLocation?, v: kotlin.Array<kotlin.Float>): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform1fv(location: org.khronos.webgl.WebGLUniformLocation?, v: org.khronos.webgl.Float32Array): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform1i(location: org.khronos.webgl.WebGLUniformLocation?, x: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform1iv(location: org.khronos.webgl.WebGLUniformLocation?, v: kotlin.Array<kotlin.Int>): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform1iv(location: org.khronos.webgl.WebGLUniformLocation?, v: org.khronos.webgl.Int32Array): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform2f(location: org.khronos.webgl.WebGLUniformLocation?, x: kotlin.Float, y: kotlin.Float): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform2fv(location: org.khronos.webgl.WebGLUniformLocation?, v: kotlin.Array<kotlin.Float>): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform2fv(location: org.khronos.webgl.WebGLUniformLocation?, v: org.khronos.webgl.Float32Array): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform2i(location: org.khronos.webgl.WebGLUniformLocation?, x: kotlin.Int, y: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform2iv(location: org.khronos.webgl.WebGLUniformLocation?, v: kotlin.Array<kotlin.Int>): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform2iv(location: org.khronos.webgl.WebGLUniformLocation?, v: org.khronos.webgl.Int32Array): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform3f(location: org.khronos.webgl.WebGLUniformLocation?, x: kotlin.Float, y: kotlin.Float, z: kotlin.Float): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform3fv(location: org.khronos.webgl.WebGLUniformLocation?, v: kotlin.Array<kotlin.Float>): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform3fv(location: org.khronos.webgl.WebGLUniformLocation?, v: org.khronos.webgl.Float32Array): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform3i(location: org.khronos.webgl.WebGLUniformLocation?, x: kotlin.Int, y: kotlin.Int, z: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform3iv(location: org.khronos.webgl.WebGLUniformLocation?, v: kotlin.Array<kotlin.Int>): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform3iv(location: org.khronos.webgl.WebGLUniformLocation?, v: org.khronos.webgl.Int32Array): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform4f(location: org.khronos.webgl.WebGLUniformLocation?, x: kotlin.Float, y: kotlin.Float, z: kotlin.Float, w: kotlin.Float): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform4fv(location: org.khronos.webgl.WebGLUniformLocation?, v: kotlin.Array<kotlin.Float>): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform4fv(location: org.khronos.webgl.WebGLUniformLocation?, v: org.khronos.webgl.Float32Array): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform4i(location: org.khronos.webgl.WebGLUniformLocation?, x: kotlin.Int, y: kotlin.Int, z: kotlin.Int, w: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform4iv(location: org.khronos.webgl.WebGLUniformLocation?, v: kotlin.Array<kotlin.Int>): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniform4iv(location: org.khronos.webgl.WebGLUniformLocation?, v: org.khronos.webgl.Int32Array): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniformMatrix2fv(location: org.khronos.webgl.WebGLUniformLocation?, transpose: kotlin.Boolean, konstue: kotlin.Array<kotlin.Float>): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniformMatrix2fv(location: org.khronos.webgl.WebGLUniformLocation?, transpose: kotlin.Boolean, konstue: org.khronos.webgl.Float32Array): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniformMatrix3fv(location: org.khronos.webgl.WebGLUniformLocation?, transpose: kotlin.Boolean, konstue: kotlin.Array<kotlin.Float>): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniformMatrix3fv(location: org.khronos.webgl.WebGLUniformLocation?, transpose: kotlin.Boolean, konstue: org.khronos.webgl.Float32Array): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniformMatrix4fv(location: org.khronos.webgl.WebGLUniformLocation?, transpose: kotlin.Boolean, konstue: kotlin.Array<kotlin.Float>): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun uniformMatrix4fv(location: org.khronos.webgl.WebGLUniformLocation?, transpose: kotlin.Boolean, konstue: org.khronos.webgl.Float32Array): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun useProgram(program: org.khronos.webgl.WebGLProgram?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun konstidateProgram(program: org.khronos.webgl.WebGLProgram?): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun vertexAttrib1f(index: kotlin.Int, x: kotlin.Float): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun vertexAttrib1fv(index: kotlin.Int, konstues: dynamic): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun vertexAttrib2f(index: kotlin.Int, x: kotlin.Float, y: kotlin.Float): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun vertexAttrib2fv(index: kotlin.Int, konstues: dynamic): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun vertexAttrib3f(index: kotlin.Int, x: kotlin.Float, y: kotlin.Float, z: kotlin.Float): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun vertexAttrib3fv(index: kotlin.Int, konstues: dynamic): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun vertexAttrib4f(index: kotlin.Int, x: kotlin.Float, y: kotlin.Float, z: kotlin.Float, w: kotlin.Float): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun vertexAttrib4fv(index: kotlin.Int, konstues: dynamic): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun vertexAttribPointer(index: kotlin.Int, size: kotlin.Int, type: kotlin.Int, normalized: kotlin.Boolean, stride: kotlin.Int, offset: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public abstract fun viewport(x: kotlin.Int, y: kotlin.Int, width: kotlin.Int, height: kotlin.Int): kotlin.Unit
/*∆*/ 
/*∆*/     public companion object of WebGLRenderingContextBase {
/*∆*/         public final konst ACTIVE_ATTRIBUTES: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ACTIVE_TEXTURE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ACTIVE_UNIFORMS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ALIASED_LINE_WIDTH_RANGE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ALIASED_POINT_SIZE_RANGE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ALPHA_BITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ALWAYS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ARRAY_BUFFER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ARRAY_BUFFER_BINDING: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ATTACHED_SHADERS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BACK: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLEND: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLEND_COLOR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLEND_DST_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLEND_DST_RGB: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLEND_EQUATION: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLEND_EQUATION_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLEND_EQUATION_RGB: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLEND_SRC_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLEND_SRC_RGB: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BLUE_BITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BOOL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BOOL_VEC2: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BOOL_VEC3: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BOOL_VEC4: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BROWSER_DEFAULT_WEBGL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BUFFER_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BUFFER_USAGE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst BYTE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CCW: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CLAMP_TO_EDGE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst COLOR_ATTACHMENT0: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst COLOR_BUFFER_BIT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst COLOR_CLEAR_VALUE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst COLOR_WRITEMASK: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst COMPILE_STATUS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst COMPRESSED_TEXTURE_FORMATS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CONSTANT_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CONSTANT_COLOR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CONTEXT_LOST_WEBGL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CULL_FACE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CULL_FACE_MODE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CURRENT_PROGRAM: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CURRENT_VERTEX_ATTRIB: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst CW: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DECR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DECR_WRAP: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DELETE_STATUS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_ATTACHMENT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_BITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_BUFFER_BIT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_CLEAR_VALUE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_COMPONENT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_COMPONENT16: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_FUNC: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_RANGE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_STENCIL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_STENCIL_ATTACHMENT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_TEST: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DEPTH_WRITEMASK: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DITHER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DONT_CARE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DST_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DST_COLOR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst DYNAMIC_DRAW: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ELEMENT_ARRAY_BUFFER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ELEMENT_ARRAY_BUFFER_BINDING: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst EQUAL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FASTEST: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FLOAT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FLOAT_MAT2: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FLOAT_MAT3: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FLOAT_MAT4: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FLOAT_VEC2: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FLOAT_VEC3: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FLOAT_VEC4: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAGMENT_SHADER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_ATTACHMENT_OBJECT_NAME: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_BINDING: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_COMPLETE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_INCOMPLETE_ATTACHMENT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_INCOMPLETE_DIMENSIONS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRAMEBUFFER_UNSUPPORTED: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRONT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRONT_AND_BACK: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FRONT_FACE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FUNC_ADD: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FUNC_REVERSE_SUBTRACT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst FUNC_SUBTRACT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst GENERATE_MIPMAP_HINT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst GEQUAL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst GREATER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst GREEN_BITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst HIGH_FLOAT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst HIGH_INT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst IMPLEMENTATION_COLOR_READ_FORMAT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst IMPLEMENTATION_COLOR_READ_TYPE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INCR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INCR_WRAP: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INT_VEC2: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INT_VEC3: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INT_VEC4: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INVALID_ENUM: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INVALID_FRAMEBUFFER_OPERATION: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INVALID_OPERATION: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INVALID_VALUE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst INVERT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst KEEP: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LEQUAL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LESS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LINEAR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LINEAR_MIPMAP_LINEAR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LINEAR_MIPMAP_NEAREST: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LINES: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LINE_LOOP: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LINE_STRIP: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LINE_WIDTH: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LINK_STATUS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LOW_FLOAT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LOW_INT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LUMINANCE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst LUMINANCE_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_COMBINED_TEXTURE_IMAGE_UNITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_CUBE_MAP_TEXTURE_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_FRAGMENT_UNIFORM_VECTORS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_RENDERBUFFER_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_TEXTURE_IMAGE_UNITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_TEXTURE_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_VARYING_VECTORS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_VERTEX_ATTRIBS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_VERTEX_TEXTURE_IMAGE_UNITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_VERTEX_UNIFORM_VECTORS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MAX_VIEWPORT_DIMS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MEDIUM_FLOAT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MEDIUM_INT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst MIRRORED_REPEAT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst NEAREST: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst NEAREST_MIPMAP_LINEAR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst NEAREST_MIPMAP_NEAREST: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst NEVER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst NICEST: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst NONE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst NOTEQUAL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst NO_ERROR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ONE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ONE_MINUS_CONSTANT_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ONE_MINUS_CONSTANT_COLOR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ONE_MINUS_DST_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ONE_MINUS_DST_COLOR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ONE_MINUS_SRC_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ONE_MINUS_SRC_COLOR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst OUT_OF_MEMORY: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst PACK_ALIGNMENT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst POINTS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst POLYGON_OFFSET_FACTOR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst POLYGON_OFFSET_FILL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst POLYGON_OFFSET_UNITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RED_BITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_ALPHA_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_BINDING: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_BLUE_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_DEPTH_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_GREEN_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_HEIGHT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_INTERNAL_FORMAT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_RED_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_STENCIL_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERBUFFER_WIDTH: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RENDERER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst REPEAT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst REPLACE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RGB: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RGB565: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RGB5_A1: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RGBA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst RGBA4: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SAMPLER_2D: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SAMPLER_CUBE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SAMPLES: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SAMPLE_ALPHA_TO_COVERAGE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SAMPLE_BUFFERS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SAMPLE_COVERAGE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SAMPLE_COVERAGE_INVERT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SAMPLE_COVERAGE_VALUE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SCISSOR_BOX: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SCISSOR_TEST: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SHADER_TYPE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SHADING_LANGUAGE_VERSION: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SHORT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SRC_ALPHA: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SRC_ALPHA_SATURATE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SRC_COLOR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STATIC_DRAW: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_ATTACHMENT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_BACK_FAIL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_BACK_FUNC: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_BACK_PASS_DEPTH_FAIL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_BACK_PASS_DEPTH_PASS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_BACK_REF: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_BACK_VALUE_MASK: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_BACK_WRITEMASK: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_BITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_BUFFER_BIT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_CLEAR_VALUE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_FAIL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_FUNC: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_INDEX: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_INDEX8: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_PASS_DEPTH_FAIL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_PASS_DEPTH_PASS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_REF: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_TEST: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_VALUE_MASK: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STENCIL_WRITEMASK: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst STREAM_DRAW: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst SUBPIXEL_BITS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE0: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE1: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE10: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE11: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE12: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE13: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE14: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE15: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE16: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE17: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE18: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE19: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE2: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE20: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE21: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE22: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE23: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE24: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE25: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE26: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE27: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE28: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE29: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE3: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE30: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE31: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE4: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE5: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE6: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE7: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE8: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE9: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_2D: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_BINDING_2D: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_BINDING_CUBE_MAP: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_CUBE_MAP: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_CUBE_MAP_NEGATIVE_X: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_CUBE_MAP_NEGATIVE_Y: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_CUBE_MAP_NEGATIVE_Z: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_CUBE_MAP_POSITIVE_X: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_CUBE_MAP_POSITIVE_Y: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_CUBE_MAP_POSITIVE_Z: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_MAG_FILTER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_MIN_FILTER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_WRAP_S: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TEXTURE_WRAP_T: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TRIANGLES: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TRIANGLE_FAN: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst TRIANGLE_STRIP: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNPACK_ALIGNMENT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNPACK_COLORSPACE_CONVERSION_WEBGL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNPACK_FLIP_Y_WEBGL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNPACK_PREMULTIPLY_ALPHA_WEBGL: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNSIGNED_BYTE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNSIGNED_INT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNSIGNED_SHORT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNSIGNED_SHORT_4_4_4_4: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNSIGNED_SHORT_5_5_5_1: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst UNSIGNED_SHORT_5_6_5: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VALIDATE_STATUS: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VENDOR: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VERSION: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VERTEX_ATTRIB_ARRAY_BUFFER_BINDING: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VERTEX_ATTRIB_ARRAY_ENABLED: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VERTEX_ATTRIB_ARRAY_NORMALIZED: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VERTEX_ATTRIB_ARRAY_POINTER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VERTEX_ATTRIB_ARRAY_SIZE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VERTEX_ATTRIB_ARRAY_STRIDE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VERTEX_ATTRIB_ARRAY_TYPE: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VERTEX_SHADER: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst VIEWPORT: kotlin.Int { get; }
/*∆*/ 
/*∆*/         public final konst ZERO: kotlin.Int { get; }
/*∆*/     }
/*∆*/ }
/*∆*/ 
/*∆*/ public abstract external class WebGLShader : org.khronos.webgl.WebGLObject {
/*∆*/     public constructor WebGLShader()
/*∆*/ }
/*∆*/ 
/*∆*/ public abstract external class WebGLShaderPrecisionFormat {
/*∆*/     public constructor WebGLShaderPrecisionFormat()
/*∆*/ 
/*∆*/     public open konst precision: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open konst rangeMax: kotlin.Int { get; }
/*∆*/ 
/*∆*/     public open konst rangeMin: kotlin.Int { get; }
/*∆*/ }
/*∆*/ 
/*∆*/ public abstract external class WebGLTexture : org.khronos.webgl.WebGLObject {
/*∆*/     public constructor WebGLTexture()
/*∆*/ }
/*∆*/ 
/*∆*/ public abstract external class WebGLUniformLocation {
/*∆*/     public constructor WebGLUniformLocation()
/*∆*/ }