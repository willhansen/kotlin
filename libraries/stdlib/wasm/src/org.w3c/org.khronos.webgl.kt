/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// NOTE: THIS FILE IS AUTO-GENERATED, DO NOT EDIT!
// See github.com/kotlin/dukat for details

package org.khronos.webgl

import kotlin.js.*
import org.w3c.dom.*
import org.w3c.dom.events.*

public external interface WebGLContextAttributes : JsAny {
    var alpha: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var depth: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var stencil: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var antialias: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var premultipliedAlpha: Boolean? /* = true */
        get() = definedExternally
        set(konstue) = definedExternally
    var preserveDrawingBuffer: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var preferLowPowerToHighPerformance: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
    var failIfMajorPerformanceCaveat: Boolean? /* = false */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun WebGLContextAttributes(alpha: Boolean? = true, depth: Boolean? = true, stencil: Boolean? = false, antialias: Boolean? = true, premultipliedAlpha: Boolean? = true, preserveDrawingBuffer: Boolean? = false, preferLowPowerToHighPerformance: Boolean? = false, failIfMajorPerformanceCaveat: Boolean? = false): WebGLContextAttributes { js("return { alpha, depth, stencil, antialias, premultipliedAlpha, preserveDrawingBuffer, preferLowPowerToHighPerformance, failIfMajorPerformanceCaveat };") }

public external abstract class WebGLObject : JsAny

/**
 * Exposes the JavaScript [WebGLBuffer](https://developer.mozilla.org/en/docs/Web/API/WebGLBuffer) to Kotlin
 */
public external abstract class WebGLBuffer : WebGLObject, JsAny

/**
 * Exposes the JavaScript [WebGLFramebuffer](https://developer.mozilla.org/en/docs/Web/API/WebGLFramebuffer) to Kotlin
 */
public external abstract class WebGLFramebuffer : WebGLObject, JsAny

/**
 * Exposes the JavaScript [WebGLProgram](https://developer.mozilla.org/en/docs/Web/API/WebGLProgram) to Kotlin
 */
public external abstract class WebGLProgram : WebGLObject, JsAny

/**
 * Exposes the JavaScript [WebGLRenderbuffer](https://developer.mozilla.org/en/docs/Web/API/WebGLRenderbuffer) to Kotlin
 */
public external abstract class WebGLRenderbuffer : WebGLObject, JsAny

/**
 * Exposes the JavaScript [WebGLShader](https://developer.mozilla.org/en/docs/Web/API/WebGLShader) to Kotlin
 */
public external abstract class WebGLShader : WebGLObject, JsAny

/**
 * Exposes the JavaScript [WebGLTexture](https://developer.mozilla.org/en/docs/Web/API/WebGLTexture) to Kotlin
 */
public external abstract class WebGLTexture : WebGLObject, JsAny

/**
 * Exposes the JavaScript [WebGLUniformLocation](https://developer.mozilla.org/en/docs/Web/API/WebGLUniformLocation) to Kotlin
 */
public external abstract class WebGLUniformLocation : JsAny

/**
 * Exposes the JavaScript [WebGLActiveInfo](https://developer.mozilla.org/en/docs/Web/API/WebGLActiveInfo) to Kotlin
 */
public external abstract class WebGLActiveInfo : JsAny {
    open konst size: Int
    open konst type: Int
    open konst name: String
}

/**
 * Exposes the JavaScript [WebGLShaderPrecisionFormat](https://developer.mozilla.org/en/docs/Web/API/WebGLShaderPrecisionFormat) to Kotlin
 */
public external abstract class WebGLShaderPrecisionFormat : JsAny {
    open konst rangeMin: Int
    open konst rangeMax: Int
    open konst precision: Int
}

@Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")
public external interface WebGLRenderingContextBase : JsAny {
    konst canvas: HTMLCanvasElement
    konst drawingBufferWidth: Int
    konst drawingBufferHeight: Int
    fun getContextAttributes(): WebGLContextAttributes?
    fun isContextLost(): Boolean
    fun getSupportedExtensions(): JsArray<JsString>?
    fun getExtension(name: String): JsAny?
    fun activeTexture(texture: Int)
    fun attachShader(program: WebGLProgram?, shader: WebGLShader?)
    fun bindAttribLocation(program: WebGLProgram?, index: Int, name: String)
    fun bindBuffer(target: Int, buffer: WebGLBuffer?)
    fun bindFramebuffer(target: Int, framebuffer: WebGLFramebuffer?)
    fun bindRenderbuffer(target: Int, renderbuffer: WebGLRenderbuffer?)
    fun bindTexture(target: Int, texture: WebGLTexture?)
    fun blendColor(red: Float, green: Float, blue: Float, alpha: Float)
    fun blendEquation(mode: Int)
    fun blendEquationSeparate(modeRGB: Int, modeAlpha: Int)
    fun blendFunc(sfactor: Int, dfactor: Int)
    fun blendFuncSeparate(srcRGB: Int, dstRGB: Int, srcAlpha: Int, dstAlpha: Int)
    fun bufferData(target: Int, size: Int, usage: Int)
    fun bufferData(target: Int, data: BufferDataSource?, usage: Int)
    fun bufferSubData(target: Int, offset: Int, data: BufferDataSource?)
    fun checkFramebufferStatus(target: Int): Int
    fun clear(mask: Int)
    fun clearColor(red: Float, green: Float, blue: Float, alpha: Float)
    fun clearDepth(depth: Float)
    fun clearStencil(s: Int)
    fun colorMask(red: Boolean, green: Boolean, blue: Boolean, alpha: Boolean)
    fun compileShader(shader: WebGLShader?)
    fun compressedTexImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, data: ArrayBufferView)
    fun compressedTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, data: ArrayBufferView)
    fun copyTexImage2D(target: Int, level: Int, internalformat: Int, x: Int, y: Int, width: Int, height: Int, border: Int)
    fun copyTexSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, x: Int, y: Int, width: Int, height: Int)
    fun createBuffer(): WebGLBuffer?
    fun createFramebuffer(): WebGLFramebuffer?
    fun createProgram(): WebGLProgram?
    fun createRenderbuffer(): WebGLRenderbuffer?
    fun createShader(type: Int): WebGLShader?
    fun createTexture(): WebGLTexture?
    fun cullFace(mode: Int)
    fun deleteBuffer(buffer: WebGLBuffer?)
    fun deleteFramebuffer(framebuffer: WebGLFramebuffer?)
    fun deleteProgram(program: WebGLProgram?)
    fun deleteRenderbuffer(renderbuffer: WebGLRenderbuffer?)
    fun deleteShader(shader: WebGLShader?)
    fun deleteTexture(texture: WebGLTexture?)
    fun depthFunc(func: Int)
    fun depthMask(flag: Boolean)
    fun depthRange(zNear: Float, zFar: Float)
    fun detachShader(program: WebGLProgram?, shader: WebGLShader?)
    fun disable(cap: Int)
    fun disableVertexAttribArray(index: Int)
    fun drawArrays(mode: Int, first: Int, count: Int)
    fun drawElements(mode: Int, count: Int, type: Int, offset: Int)
    fun enable(cap: Int)
    fun enableVertexAttribArray(index: Int)
    fun finish()
    fun flush()
    fun framebufferRenderbuffer(target: Int, attachment: Int, renderbuffertarget: Int, renderbuffer: WebGLRenderbuffer?)
    fun framebufferTexture2D(target: Int, attachment: Int, textarget: Int, texture: WebGLTexture?, level: Int)
    fun frontFace(mode: Int)
    fun generateMipmap(target: Int)
    fun getActiveAttrib(program: WebGLProgram?, index: Int): WebGLActiveInfo?
    fun getActiveUniform(program: WebGLProgram?, index: Int): WebGLActiveInfo?
    fun getAttachedShaders(program: WebGLProgram?): JsArray<WebGLShader>?
    fun getAttribLocation(program: WebGLProgram?, name: String): Int
    fun getBufferParameter(target: Int, pname: Int): JsAny?
    fun getParameter(pname: Int): JsAny?
    fun getError(): Int
    fun getFramebufferAttachmentParameter(target: Int, attachment: Int, pname: Int): JsAny?
    fun getProgramParameter(program: WebGLProgram?, pname: Int): JsAny?
    fun getProgramInfoLog(program: WebGLProgram?): String?
    fun getRenderbufferParameter(target: Int, pname: Int): JsAny?
    fun getShaderParameter(shader: WebGLShader?, pname: Int): JsAny?
    fun getShaderPrecisionFormat(shadertype: Int, precisiontype: Int): WebGLShaderPrecisionFormat?
    fun getShaderInfoLog(shader: WebGLShader?): String?
    fun getShaderSource(shader: WebGLShader?): String?
    fun getTexParameter(target: Int, pname: Int): JsAny?
    fun getUniform(program: WebGLProgram?, location: WebGLUniformLocation?): JsAny?
    fun getUniformLocation(program: WebGLProgram?, name: String): WebGLUniformLocation?
    fun getVertexAttrib(index: Int, pname: Int): JsAny?
    fun getVertexAttribOffset(index: Int, pname: Int): Int
    fun hint(target: Int, mode: Int)
    fun isBuffer(buffer: WebGLBuffer?): Boolean
    fun isEnabled(cap: Int): Boolean
    fun isFramebuffer(framebuffer: WebGLFramebuffer?): Boolean
    fun isProgram(program: WebGLProgram?): Boolean
    fun isRenderbuffer(renderbuffer: WebGLRenderbuffer?): Boolean
    fun isShader(shader: WebGLShader?): Boolean
    fun isTexture(texture: WebGLTexture?): Boolean
    fun lineWidth(width: Float)
    fun linkProgram(program: WebGLProgram?)
    fun pixelStorei(pname: Int, param: Int)
    fun polygonOffset(factor: Float, units: Float)
    fun readPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int, pixels: ArrayBufferView?)
    fun renderbufferStorage(target: Int, internalformat: Int, width: Int, height: Int)
    fun sampleCoverage(konstue: Float, invert: Boolean)
    fun scissor(x: Int, y: Int, width: Int, height: Int)
    fun shaderSource(shader: WebGLShader?, source: String)
    fun stencilFunc(func: Int, ref: Int, mask: Int)
    fun stencilFuncSeparate(face: Int, func: Int, ref: Int, mask: Int)
    fun stencilMask(mask: Int)
    fun stencilMaskSeparate(face: Int, mask: Int)
    fun stencilOp(fail: Int, zfail: Int, zpass: Int)
    fun stencilOpSeparate(face: Int, fail: Int, zfail: Int, zpass: Int)
    fun texImage2D(target: Int, level: Int, internalformat: Int, width: Int, height: Int, border: Int, format: Int, type: Int, pixels: ArrayBufferView?)
    fun texImage2D(target: Int, level: Int, internalformat: Int, format: Int, type: Int, source: TexImageSource?)
    fun texParameterf(target: Int, pname: Int, param: Float)
    fun texParameteri(target: Int, pname: Int, param: Int)
    fun texSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, width: Int, height: Int, format: Int, type: Int, pixels: ArrayBufferView?)
    fun texSubImage2D(target: Int, level: Int, xoffset: Int, yoffset: Int, format: Int, type: Int, source: TexImageSource?)
    fun uniform1f(location: WebGLUniformLocation?, x: Float)
    fun uniform1fv(location: WebGLUniformLocation?, v: Float32Array)
    fun uniform1fv(location: WebGLUniformLocation?, v: JsArray<JsNumber>)
    fun uniform1i(location: WebGLUniformLocation?, x: Int)
    fun uniform1iv(location: WebGLUniformLocation?, v: Int32Array)
    fun uniform1iv(location: WebGLUniformLocation?, v: JsArray<JsNumber>)
    fun uniform2f(location: WebGLUniformLocation?, x: Float, y: Float)
    fun uniform2fv(location: WebGLUniformLocation?, v: Float32Array)
    fun uniform2fv(location: WebGLUniformLocation?, v: JsArray<JsNumber>)
    fun uniform2i(location: WebGLUniformLocation?, x: Int, y: Int)
    fun uniform2iv(location: WebGLUniformLocation?, v: Int32Array)
    fun uniform2iv(location: WebGLUniformLocation?, v: JsArray<JsNumber>)
    fun uniform3f(location: WebGLUniformLocation?, x: Float, y: Float, z: Float)
    fun uniform3fv(location: WebGLUniformLocation?, v: Float32Array)
    fun uniform3fv(location: WebGLUniformLocation?, v: JsArray<JsNumber>)
    fun uniform3i(location: WebGLUniformLocation?, x: Int, y: Int, z: Int)
    fun uniform3iv(location: WebGLUniformLocation?, v: Int32Array)
    fun uniform3iv(location: WebGLUniformLocation?, v: JsArray<JsNumber>)
    fun uniform4f(location: WebGLUniformLocation?, x: Float, y: Float, z: Float, w: Float)
    fun uniform4fv(location: WebGLUniformLocation?, v: Float32Array)
    fun uniform4fv(location: WebGLUniformLocation?, v: JsArray<JsNumber>)
    fun uniform4i(location: WebGLUniformLocation?, x: Int, y: Int, z: Int, w: Int)
    fun uniform4iv(location: WebGLUniformLocation?, v: Int32Array)
    fun uniform4iv(location: WebGLUniformLocation?, v: JsArray<JsNumber>)
    fun uniformMatrix2fv(location: WebGLUniformLocation?, transpose: Boolean, konstue: Float32Array)
    fun uniformMatrix2fv(location: WebGLUniformLocation?, transpose: Boolean, konstue: JsArray<JsNumber>)
    fun uniformMatrix3fv(location: WebGLUniformLocation?, transpose: Boolean, konstue: Float32Array)
    fun uniformMatrix3fv(location: WebGLUniformLocation?, transpose: Boolean, konstue: JsArray<JsNumber>)
    fun uniformMatrix4fv(location: WebGLUniformLocation?, transpose: Boolean, konstue: Float32Array)
    fun uniformMatrix4fv(location: WebGLUniformLocation?, transpose: Boolean, konstue: JsArray<JsNumber>)
    fun useProgram(program: WebGLProgram?)
    fun konstidateProgram(program: WebGLProgram?)
    fun vertexAttrib1f(index: Int, x: Float)
    fun vertexAttrib1fv(index: Int, konstues: JsAny?)
    fun vertexAttrib2f(index: Int, x: Float, y: Float)
    fun vertexAttrib2fv(index: Int, konstues: JsAny?)
    fun vertexAttrib3f(index: Int, x: Float, y: Float, z: Float)
    fun vertexAttrib3fv(index: Int, konstues: JsAny?)
    fun vertexAttrib4f(index: Int, x: Float, y: Float, z: Float, w: Float)
    fun vertexAttrib4fv(index: Int, konstues: JsAny?)
    fun vertexAttribPointer(index: Int, size: Int, type: Int, normalized: Boolean, stride: Int, offset: Int)
    fun viewport(x: Int, y: Int, width: Int, height: Int)

    companion object {
        konst DEPTH_BUFFER_BIT: Int
        konst STENCIL_BUFFER_BIT: Int
        konst COLOR_BUFFER_BIT: Int
        konst POINTS: Int
        konst LINES: Int
        konst LINE_LOOP: Int
        konst LINE_STRIP: Int
        konst TRIANGLES: Int
        konst TRIANGLE_STRIP: Int
        konst TRIANGLE_FAN: Int
        konst ZERO: Int
        konst ONE: Int
        konst SRC_COLOR: Int
        konst ONE_MINUS_SRC_COLOR: Int
        konst SRC_ALPHA: Int
        konst ONE_MINUS_SRC_ALPHA: Int
        konst DST_ALPHA: Int
        konst ONE_MINUS_DST_ALPHA: Int
        konst DST_COLOR: Int
        konst ONE_MINUS_DST_COLOR: Int
        konst SRC_ALPHA_SATURATE: Int
        konst FUNC_ADD: Int
        konst BLEND_EQUATION: Int
        konst BLEND_EQUATION_RGB: Int
        konst BLEND_EQUATION_ALPHA: Int
        konst FUNC_SUBTRACT: Int
        konst FUNC_REVERSE_SUBTRACT: Int
        konst BLEND_DST_RGB: Int
        konst BLEND_SRC_RGB: Int
        konst BLEND_DST_ALPHA: Int
        konst BLEND_SRC_ALPHA: Int
        konst CONSTANT_COLOR: Int
        konst ONE_MINUS_CONSTANT_COLOR: Int
        konst CONSTANT_ALPHA: Int
        konst ONE_MINUS_CONSTANT_ALPHA: Int
        konst BLEND_COLOR: Int
        konst ARRAY_BUFFER: Int
        konst ELEMENT_ARRAY_BUFFER: Int
        konst ARRAY_BUFFER_BINDING: Int
        konst ELEMENT_ARRAY_BUFFER_BINDING: Int
        konst STREAM_DRAW: Int
        konst STATIC_DRAW: Int
        konst DYNAMIC_DRAW: Int
        konst BUFFER_SIZE: Int
        konst BUFFER_USAGE: Int
        konst CURRENT_VERTEX_ATTRIB: Int
        konst FRONT: Int
        konst BACK: Int
        konst FRONT_AND_BACK: Int
        konst CULL_FACE: Int
        konst BLEND: Int
        konst DITHER: Int
        konst STENCIL_TEST: Int
        konst DEPTH_TEST: Int
        konst SCISSOR_TEST: Int
        konst POLYGON_OFFSET_FILL: Int
        konst SAMPLE_ALPHA_TO_COVERAGE: Int
        konst SAMPLE_COVERAGE: Int
        konst NO_ERROR: Int
        konst INVALID_ENUM: Int
        konst INVALID_VALUE: Int
        konst INVALID_OPERATION: Int
        konst OUT_OF_MEMORY: Int
        konst CW: Int
        konst CCW: Int
        konst LINE_WIDTH: Int
        konst ALIASED_POINT_SIZE_RANGE: Int
        konst ALIASED_LINE_WIDTH_RANGE: Int
        konst CULL_FACE_MODE: Int
        konst FRONT_FACE: Int
        konst DEPTH_RANGE: Int
        konst DEPTH_WRITEMASK: Int
        konst DEPTH_CLEAR_VALUE: Int
        konst DEPTH_FUNC: Int
        konst STENCIL_CLEAR_VALUE: Int
        konst STENCIL_FUNC: Int
        konst STENCIL_FAIL: Int
        konst STENCIL_PASS_DEPTH_FAIL: Int
        konst STENCIL_PASS_DEPTH_PASS: Int
        konst STENCIL_REF: Int
        konst STENCIL_VALUE_MASK: Int
        konst STENCIL_WRITEMASK: Int
        konst STENCIL_BACK_FUNC: Int
        konst STENCIL_BACK_FAIL: Int
        konst STENCIL_BACK_PASS_DEPTH_FAIL: Int
        konst STENCIL_BACK_PASS_DEPTH_PASS: Int
        konst STENCIL_BACK_REF: Int
        konst STENCIL_BACK_VALUE_MASK: Int
        konst STENCIL_BACK_WRITEMASK: Int
        konst VIEWPORT: Int
        konst SCISSOR_BOX: Int
        konst COLOR_CLEAR_VALUE: Int
        konst COLOR_WRITEMASK: Int
        konst UNPACK_ALIGNMENT: Int
        konst PACK_ALIGNMENT: Int
        konst MAX_TEXTURE_SIZE: Int
        konst MAX_VIEWPORT_DIMS: Int
        konst SUBPIXEL_BITS: Int
        konst RED_BITS: Int
        konst GREEN_BITS: Int
        konst BLUE_BITS: Int
        konst ALPHA_BITS: Int
        konst DEPTH_BITS: Int
        konst STENCIL_BITS: Int
        konst POLYGON_OFFSET_UNITS: Int
        konst POLYGON_OFFSET_FACTOR: Int
        konst TEXTURE_BINDING_2D: Int
        konst SAMPLE_BUFFERS: Int
        konst SAMPLES: Int
        konst SAMPLE_COVERAGE_VALUE: Int
        konst SAMPLE_COVERAGE_INVERT: Int
        konst COMPRESSED_TEXTURE_FORMATS: Int
        konst DONT_CARE: Int
        konst FASTEST: Int
        konst NICEST: Int
        konst GENERATE_MIPMAP_HINT: Int
        konst BYTE: Int
        konst UNSIGNED_BYTE: Int
        konst SHORT: Int
        konst UNSIGNED_SHORT: Int
        konst INT: Int
        konst UNSIGNED_INT: Int
        konst FLOAT: Int
        konst DEPTH_COMPONENT: Int
        konst ALPHA: Int
        konst RGB: Int
        konst RGBA: Int
        konst LUMINANCE: Int
        konst LUMINANCE_ALPHA: Int
        konst UNSIGNED_SHORT_4_4_4_4: Int
        konst UNSIGNED_SHORT_5_5_5_1: Int
        konst UNSIGNED_SHORT_5_6_5: Int
        konst FRAGMENT_SHADER: Int
        konst VERTEX_SHADER: Int
        konst MAX_VERTEX_ATTRIBS: Int
        konst MAX_VERTEX_UNIFORM_VECTORS: Int
        konst MAX_VARYING_VECTORS: Int
        konst MAX_COMBINED_TEXTURE_IMAGE_UNITS: Int
        konst MAX_VERTEX_TEXTURE_IMAGE_UNITS: Int
        konst MAX_TEXTURE_IMAGE_UNITS: Int
        konst MAX_FRAGMENT_UNIFORM_VECTORS: Int
        konst SHADER_TYPE: Int
        konst DELETE_STATUS: Int
        konst LINK_STATUS: Int
        konst VALIDATE_STATUS: Int
        konst ATTACHED_SHADERS: Int
        konst ACTIVE_UNIFORMS: Int
        konst ACTIVE_ATTRIBUTES: Int
        konst SHADING_LANGUAGE_VERSION: Int
        konst CURRENT_PROGRAM: Int
        konst NEVER: Int
        konst LESS: Int
        konst EQUAL: Int
        konst LEQUAL: Int
        konst GREATER: Int
        konst NOTEQUAL: Int
        konst GEQUAL: Int
        konst ALWAYS: Int
        konst KEEP: Int
        konst REPLACE: Int
        konst INCR: Int
        konst DECR: Int
        konst INVERT: Int
        konst INCR_WRAP: Int
        konst DECR_WRAP: Int
        konst VENDOR: Int
        konst RENDERER: Int
        konst VERSION: Int
        konst NEAREST: Int
        konst LINEAR: Int
        konst NEAREST_MIPMAP_NEAREST: Int
        konst LINEAR_MIPMAP_NEAREST: Int
        konst NEAREST_MIPMAP_LINEAR: Int
        konst LINEAR_MIPMAP_LINEAR: Int
        konst TEXTURE_MAG_FILTER: Int
        konst TEXTURE_MIN_FILTER: Int
        konst TEXTURE_WRAP_S: Int
        konst TEXTURE_WRAP_T: Int
        konst TEXTURE_2D: Int
        konst TEXTURE: Int
        konst TEXTURE_CUBE_MAP: Int
        konst TEXTURE_BINDING_CUBE_MAP: Int
        konst TEXTURE_CUBE_MAP_POSITIVE_X: Int
        konst TEXTURE_CUBE_MAP_NEGATIVE_X: Int
        konst TEXTURE_CUBE_MAP_POSITIVE_Y: Int
        konst TEXTURE_CUBE_MAP_NEGATIVE_Y: Int
        konst TEXTURE_CUBE_MAP_POSITIVE_Z: Int
        konst TEXTURE_CUBE_MAP_NEGATIVE_Z: Int
        konst MAX_CUBE_MAP_TEXTURE_SIZE: Int
        konst TEXTURE0: Int
        konst TEXTURE1: Int
        konst TEXTURE2: Int
        konst TEXTURE3: Int
        konst TEXTURE4: Int
        konst TEXTURE5: Int
        konst TEXTURE6: Int
        konst TEXTURE7: Int
        konst TEXTURE8: Int
        konst TEXTURE9: Int
        konst TEXTURE10: Int
        konst TEXTURE11: Int
        konst TEXTURE12: Int
        konst TEXTURE13: Int
        konst TEXTURE14: Int
        konst TEXTURE15: Int
        konst TEXTURE16: Int
        konst TEXTURE17: Int
        konst TEXTURE18: Int
        konst TEXTURE19: Int
        konst TEXTURE20: Int
        konst TEXTURE21: Int
        konst TEXTURE22: Int
        konst TEXTURE23: Int
        konst TEXTURE24: Int
        konst TEXTURE25: Int
        konst TEXTURE26: Int
        konst TEXTURE27: Int
        konst TEXTURE28: Int
        konst TEXTURE29: Int
        konst TEXTURE30: Int
        konst TEXTURE31: Int
        konst ACTIVE_TEXTURE: Int
        konst REPEAT: Int
        konst CLAMP_TO_EDGE: Int
        konst MIRRORED_REPEAT: Int
        konst FLOAT_VEC2: Int
        konst FLOAT_VEC3: Int
        konst FLOAT_VEC4: Int
        konst INT_VEC2: Int
        konst INT_VEC3: Int
        konst INT_VEC4: Int
        konst BOOL: Int
        konst BOOL_VEC2: Int
        konst BOOL_VEC3: Int
        konst BOOL_VEC4: Int
        konst FLOAT_MAT2: Int
        konst FLOAT_MAT3: Int
        konst FLOAT_MAT4: Int
        konst SAMPLER_2D: Int
        konst SAMPLER_CUBE: Int
        konst VERTEX_ATTRIB_ARRAY_ENABLED: Int
        konst VERTEX_ATTRIB_ARRAY_SIZE: Int
        konst VERTEX_ATTRIB_ARRAY_STRIDE: Int
        konst VERTEX_ATTRIB_ARRAY_TYPE: Int
        konst VERTEX_ATTRIB_ARRAY_NORMALIZED: Int
        konst VERTEX_ATTRIB_ARRAY_POINTER: Int
        konst VERTEX_ATTRIB_ARRAY_BUFFER_BINDING: Int
        konst IMPLEMENTATION_COLOR_READ_TYPE: Int
        konst IMPLEMENTATION_COLOR_READ_FORMAT: Int
        konst COMPILE_STATUS: Int
        konst LOW_FLOAT: Int
        konst MEDIUM_FLOAT: Int
        konst HIGH_FLOAT: Int
        konst LOW_INT: Int
        konst MEDIUM_INT: Int
        konst HIGH_INT: Int
        konst FRAMEBUFFER: Int
        konst RENDERBUFFER: Int
        konst RGBA4: Int
        konst RGB5_A1: Int
        konst RGB565: Int
        konst DEPTH_COMPONENT16: Int
        konst STENCIL_INDEX: Int
        konst STENCIL_INDEX8: Int
        konst DEPTH_STENCIL: Int
        konst RENDERBUFFER_WIDTH: Int
        konst RENDERBUFFER_HEIGHT: Int
        konst RENDERBUFFER_INTERNAL_FORMAT: Int
        konst RENDERBUFFER_RED_SIZE: Int
        konst RENDERBUFFER_GREEN_SIZE: Int
        konst RENDERBUFFER_BLUE_SIZE: Int
        konst RENDERBUFFER_ALPHA_SIZE: Int
        konst RENDERBUFFER_DEPTH_SIZE: Int
        konst RENDERBUFFER_STENCIL_SIZE: Int
        konst FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE: Int
        konst FRAMEBUFFER_ATTACHMENT_OBJECT_NAME: Int
        konst FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL: Int
        konst FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE: Int
        konst COLOR_ATTACHMENT0: Int
        konst DEPTH_ATTACHMENT: Int
        konst STENCIL_ATTACHMENT: Int
        konst DEPTH_STENCIL_ATTACHMENT: Int
        konst NONE: Int
        konst FRAMEBUFFER_COMPLETE: Int
        konst FRAMEBUFFER_INCOMPLETE_ATTACHMENT: Int
        konst FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT: Int
        konst FRAMEBUFFER_INCOMPLETE_DIMENSIONS: Int
        konst FRAMEBUFFER_UNSUPPORTED: Int
        konst FRAMEBUFFER_BINDING: Int
        konst RENDERBUFFER_BINDING: Int
        konst MAX_RENDERBUFFER_SIZE: Int
        konst INVALID_FRAMEBUFFER_OPERATION: Int
        konst UNPACK_FLIP_Y_WEBGL: Int
        konst UNPACK_PREMULTIPLY_ALPHA_WEBGL: Int
        konst CONTEXT_LOST_WEBGL: Int
        konst UNPACK_COLORSPACE_CONVERSION_WEBGL: Int
        konst BROWSER_DEFAULT_WEBGL: Int
    }
}

/**
 * Exposes the JavaScript [WebGLRenderingContext](https://developer.mozilla.org/en/docs/Web/API/WebGLRenderingContext) to Kotlin
 */
public external abstract class WebGLRenderingContext : WebGLRenderingContextBase, RenderingContext, JsAny {
    companion object {
        konst DEPTH_BUFFER_BIT: Int
        konst STENCIL_BUFFER_BIT: Int
        konst COLOR_BUFFER_BIT: Int
        konst POINTS: Int
        konst LINES: Int
        konst LINE_LOOP: Int
        konst LINE_STRIP: Int
        konst TRIANGLES: Int
        konst TRIANGLE_STRIP: Int
        konst TRIANGLE_FAN: Int
        konst ZERO: Int
        konst ONE: Int
        konst SRC_COLOR: Int
        konst ONE_MINUS_SRC_COLOR: Int
        konst SRC_ALPHA: Int
        konst ONE_MINUS_SRC_ALPHA: Int
        konst DST_ALPHA: Int
        konst ONE_MINUS_DST_ALPHA: Int
        konst DST_COLOR: Int
        konst ONE_MINUS_DST_COLOR: Int
        konst SRC_ALPHA_SATURATE: Int
        konst FUNC_ADD: Int
        konst BLEND_EQUATION: Int
        konst BLEND_EQUATION_RGB: Int
        konst BLEND_EQUATION_ALPHA: Int
        konst FUNC_SUBTRACT: Int
        konst FUNC_REVERSE_SUBTRACT: Int
        konst BLEND_DST_RGB: Int
        konst BLEND_SRC_RGB: Int
        konst BLEND_DST_ALPHA: Int
        konst BLEND_SRC_ALPHA: Int
        konst CONSTANT_COLOR: Int
        konst ONE_MINUS_CONSTANT_COLOR: Int
        konst CONSTANT_ALPHA: Int
        konst ONE_MINUS_CONSTANT_ALPHA: Int
        konst BLEND_COLOR: Int
        konst ARRAY_BUFFER: Int
        konst ELEMENT_ARRAY_BUFFER: Int
        konst ARRAY_BUFFER_BINDING: Int
        konst ELEMENT_ARRAY_BUFFER_BINDING: Int
        konst STREAM_DRAW: Int
        konst STATIC_DRAW: Int
        konst DYNAMIC_DRAW: Int
        konst BUFFER_SIZE: Int
        konst BUFFER_USAGE: Int
        konst CURRENT_VERTEX_ATTRIB: Int
        konst FRONT: Int
        konst BACK: Int
        konst FRONT_AND_BACK: Int
        konst CULL_FACE: Int
        konst BLEND: Int
        konst DITHER: Int
        konst STENCIL_TEST: Int
        konst DEPTH_TEST: Int
        konst SCISSOR_TEST: Int
        konst POLYGON_OFFSET_FILL: Int
        konst SAMPLE_ALPHA_TO_COVERAGE: Int
        konst SAMPLE_COVERAGE: Int
        konst NO_ERROR: Int
        konst INVALID_ENUM: Int
        konst INVALID_VALUE: Int
        konst INVALID_OPERATION: Int
        konst OUT_OF_MEMORY: Int
        konst CW: Int
        konst CCW: Int
        konst LINE_WIDTH: Int
        konst ALIASED_POINT_SIZE_RANGE: Int
        konst ALIASED_LINE_WIDTH_RANGE: Int
        konst CULL_FACE_MODE: Int
        konst FRONT_FACE: Int
        konst DEPTH_RANGE: Int
        konst DEPTH_WRITEMASK: Int
        konst DEPTH_CLEAR_VALUE: Int
        konst DEPTH_FUNC: Int
        konst STENCIL_CLEAR_VALUE: Int
        konst STENCIL_FUNC: Int
        konst STENCIL_FAIL: Int
        konst STENCIL_PASS_DEPTH_FAIL: Int
        konst STENCIL_PASS_DEPTH_PASS: Int
        konst STENCIL_REF: Int
        konst STENCIL_VALUE_MASK: Int
        konst STENCIL_WRITEMASK: Int
        konst STENCIL_BACK_FUNC: Int
        konst STENCIL_BACK_FAIL: Int
        konst STENCIL_BACK_PASS_DEPTH_FAIL: Int
        konst STENCIL_BACK_PASS_DEPTH_PASS: Int
        konst STENCIL_BACK_REF: Int
        konst STENCIL_BACK_VALUE_MASK: Int
        konst STENCIL_BACK_WRITEMASK: Int
        konst VIEWPORT: Int
        konst SCISSOR_BOX: Int
        konst COLOR_CLEAR_VALUE: Int
        konst COLOR_WRITEMASK: Int
        konst UNPACK_ALIGNMENT: Int
        konst PACK_ALIGNMENT: Int
        konst MAX_TEXTURE_SIZE: Int
        konst MAX_VIEWPORT_DIMS: Int
        konst SUBPIXEL_BITS: Int
        konst RED_BITS: Int
        konst GREEN_BITS: Int
        konst BLUE_BITS: Int
        konst ALPHA_BITS: Int
        konst DEPTH_BITS: Int
        konst STENCIL_BITS: Int
        konst POLYGON_OFFSET_UNITS: Int
        konst POLYGON_OFFSET_FACTOR: Int
        konst TEXTURE_BINDING_2D: Int
        konst SAMPLE_BUFFERS: Int
        konst SAMPLES: Int
        konst SAMPLE_COVERAGE_VALUE: Int
        konst SAMPLE_COVERAGE_INVERT: Int
        konst COMPRESSED_TEXTURE_FORMATS: Int
        konst DONT_CARE: Int
        konst FASTEST: Int
        konst NICEST: Int
        konst GENERATE_MIPMAP_HINT: Int
        konst BYTE: Int
        konst UNSIGNED_BYTE: Int
        konst SHORT: Int
        konst UNSIGNED_SHORT: Int
        konst INT: Int
        konst UNSIGNED_INT: Int
        konst FLOAT: Int
        konst DEPTH_COMPONENT: Int
        konst ALPHA: Int
        konst RGB: Int
        konst RGBA: Int
        konst LUMINANCE: Int
        konst LUMINANCE_ALPHA: Int
        konst UNSIGNED_SHORT_4_4_4_4: Int
        konst UNSIGNED_SHORT_5_5_5_1: Int
        konst UNSIGNED_SHORT_5_6_5: Int
        konst FRAGMENT_SHADER: Int
        konst VERTEX_SHADER: Int
        konst MAX_VERTEX_ATTRIBS: Int
        konst MAX_VERTEX_UNIFORM_VECTORS: Int
        konst MAX_VARYING_VECTORS: Int
        konst MAX_COMBINED_TEXTURE_IMAGE_UNITS: Int
        konst MAX_VERTEX_TEXTURE_IMAGE_UNITS: Int
        konst MAX_TEXTURE_IMAGE_UNITS: Int
        konst MAX_FRAGMENT_UNIFORM_VECTORS: Int
        konst SHADER_TYPE: Int
        konst DELETE_STATUS: Int
        konst LINK_STATUS: Int
        konst VALIDATE_STATUS: Int
        konst ATTACHED_SHADERS: Int
        konst ACTIVE_UNIFORMS: Int
        konst ACTIVE_ATTRIBUTES: Int
        konst SHADING_LANGUAGE_VERSION: Int
        konst CURRENT_PROGRAM: Int
        konst NEVER: Int
        konst LESS: Int
        konst EQUAL: Int
        konst LEQUAL: Int
        konst GREATER: Int
        konst NOTEQUAL: Int
        konst GEQUAL: Int
        konst ALWAYS: Int
        konst KEEP: Int
        konst REPLACE: Int
        konst INCR: Int
        konst DECR: Int
        konst INVERT: Int
        konst INCR_WRAP: Int
        konst DECR_WRAP: Int
        konst VENDOR: Int
        konst RENDERER: Int
        konst VERSION: Int
        konst NEAREST: Int
        konst LINEAR: Int
        konst NEAREST_MIPMAP_NEAREST: Int
        konst LINEAR_MIPMAP_NEAREST: Int
        konst NEAREST_MIPMAP_LINEAR: Int
        konst LINEAR_MIPMAP_LINEAR: Int
        konst TEXTURE_MAG_FILTER: Int
        konst TEXTURE_MIN_FILTER: Int
        konst TEXTURE_WRAP_S: Int
        konst TEXTURE_WRAP_T: Int
        konst TEXTURE_2D: Int
        konst TEXTURE: Int
        konst TEXTURE_CUBE_MAP: Int
        konst TEXTURE_BINDING_CUBE_MAP: Int
        konst TEXTURE_CUBE_MAP_POSITIVE_X: Int
        konst TEXTURE_CUBE_MAP_NEGATIVE_X: Int
        konst TEXTURE_CUBE_MAP_POSITIVE_Y: Int
        konst TEXTURE_CUBE_MAP_NEGATIVE_Y: Int
        konst TEXTURE_CUBE_MAP_POSITIVE_Z: Int
        konst TEXTURE_CUBE_MAP_NEGATIVE_Z: Int
        konst MAX_CUBE_MAP_TEXTURE_SIZE: Int
        konst TEXTURE0: Int
        konst TEXTURE1: Int
        konst TEXTURE2: Int
        konst TEXTURE3: Int
        konst TEXTURE4: Int
        konst TEXTURE5: Int
        konst TEXTURE6: Int
        konst TEXTURE7: Int
        konst TEXTURE8: Int
        konst TEXTURE9: Int
        konst TEXTURE10: Int
        konst TEXTURE11: Int
        konst TEXTURE12: Int
        konst TEXTURE13: Int
        konst TEXTURE14: Int
        konst TEXTURE15: Int
        konst TEXTURE16: Int
        konst TEXTURE17: Int
        konst TEXTURE18: Int
        konst TEXTURE19: Int
        konst TEXTURE20: Int
        konst TEXTURE21: Int
        konst TEXTURE22: Int
        konst TEXTURE23: Int
        konst TEXTURE24: Int
        konst TEXTURE25: Int
        konst TEXTURE26: Int
        konst TEXTURE27: Int
        konst TEXTURE28: Int
        konst TEXTURE29: Int
        konst TEXTURE30: Int
        konst TEXTURE31: Int
        konst ACTIVE_TEXTURE: Int
        konst REPEAT: Int
        konst CLAMP_TO_EDGE: Int
        konst MIRRORED_REPEAT: Int
        konst FLOAT_VEC2: Int
        konst FLOAT_VEC3: Int
        konst FLOAT_VEC4: Int
        konst INT_VEC2: Int
        konst INT_VEC3: Int
        konst INT_VEC4: Int
        konst BOOL: Int
        konst BOOL_VEC2: Int
        konst BOOL_VEC3: Int
        konst BOOL_VEC4: Int
        konst FLOAT_MAT2: Int
        konst FLOAT_MAT3: Int
        konst FLOAT_MAT4: Int
        konst SAMPLER_2D: Int
        konst SAMPLER_CUBE: Int
        konst VERTEX_ATTRIB_ARRAY_ENABLED: Int
        konst VERTEX_ATTRIB_ARRAY_SIZE: Int
        konst VERTEX_ATTRIB_ARRAY_STRIDE: Int
        konst VERTEX_ATTRIB_ARRAY_TYPE: Int
        konst VERTEX_ATTRIB_ARRAY_NORMALIZED: Int
        konst VERTEX_ATTRIB_ARRAY_POINTER: Int
        konst VERTEX_ATTRIB_ARRAY_BUFFER_BINDING: Int
        konst IMPLEMENTATION_COLOR_READ_TYPE: Int
        konst IMPLEMENTATION_COLOR_READ_FORMAT: Int
        konst COMPILE_STATUS: Int
        konst LOW_FLOAT: Int
        konst MEDIUM_FLOAT: Int
        konst HIGH_FLOAT: Int
        konst LOW_INT: Int
        konst MEDIUM_INT: Int
        konst HIGH_INT: Int
        konst FRAMEBUFFER: Int
        konst RENDERBUFFER: Int
        konst RGBA4: Int
        konst RGB5_A1: Int
        konst RGB565: Int
        konst DEPTH_COMPONENT16: Int
        konst STENCIL_INDEX: Int
        konst STENCIL_INDEX8: Int
        konst DEPTH_STENCIL: Int
        konst RENDERBUFFER_WIDTH: Int
        konst RENDERBUFFER_HEIGHT: Int
        konst RENDERBUFFER_INTERNAL_FORMAT: Int
        konst RENDERBUFFER_RED_SIZE: Int
        konst RENDERBUFFER_GREEN_SIZE: Int
        konst RENDERBUFFER_BLUE_SIZE: Int
        konst RENDERBUFFER_ALPHA_SIZE: Int
        konst RENDERBUFFER_DEPTH_SIZE: Int
        konst RENDERBUFFER_STENCIL_SIZE: Int
        konst FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE: Int
        konst FRAMEBUFFER_ATTACHMENT_OBJECT_NAME: Int
        konst FRAMEBUFFER_ATTACHMENT_TEXTURE_LEVEL: Int
        konst FRAMEBUFFER_ATTACHMENT_TEXTURE_CUBE_MAP_FACE: Int
        konst COLOR_ATTACHMENT0: Int
        konst DEPTH_ATTACHMENT: Int
        konst STENCIL_ATTACHMENT: Int
        konst DEPTH_STENCIL_ATTACHMENT: Int
        konst NONE: Int
        konst FRAMEBUFFER_COMPLETE: Int
        konst FRAMEBUFFER_INCOMPLETE_ATTACHMENT: Int
        konst FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT: Int
        konst FRAMEBUFFER_INCOMPLETE_DIMENSIONS: Int
        konst FRAMEBUFFER_UNSUPPORTED: Int
        konst FRAMEBUFFER_BINDING: Int
        konst RENDERBUFFER_BINDING: Int
        konst MAX_RENDERBUFFER_SIZE: Int
        konst INVALID_FRAMEBUFFER_OPERATION: Int
        konst UNPACK_FLIP_Y_WEBGL: Int
        konst UNPACK_PREMULTIPLY_ALPHA_WEBGL: Int
        konst CONTEXT_LOST_WEBGL: Int
        konst UNPACK_COLORSPACE_CONVERSION_WEBGL: Int
        konst BROWSER_DEFAULT_WEBGL: Int
    }
}

/**
 * Exposes the JavaScript [WebGLContextEvent](https://developer.mozilla.org/en/docs/Web/API/WebGLContextEvent) to Kotlin
 */
public external open class WebGLContextEvent(type: String, eventInit: WebGLContextEventInit = definedExternally) : Event, JsAny {
    open konst statusMessage: String

    companion object {
        konst NONE: Short
        konst CAPTURING_PHASE: Short
        konst AT_TARGET: Short
        konst BUBBLING_PHASE: Short
    }
}

public external interface WebGLContextEventInit : EventInit, JsAny {
    var statusMessage: String? /* = "" */
        get() = definedExternally
        set(konstue) = definedExternally
}

@Suppress("UNUSED_PARAMETER")
public fun WebGLContextEventInit(statusMessage: String? = "", bubbles: Boolean? = false, cancelable: Boolean? = false, composed: Boolean? = false): WebGLContextEventInit { js("return { statusMessage, bubbles, cancelable, composed };") }

/**
 * Exposes the JavaScript [ArrayBuffer](https://developer.mozilla.org/en/docs/Web/API/ArrayBuffer) to Kotlin
 */
public external open class ArrayBuffer(length: Int) : BufferDataSource, JsAny {
    open konst byteLength: Int
    fun slice(begin: Int, end: Int = definedExternally): ArrayBuffer

    companion object {
        fun isView(konstue: JsAny?): Boolean
    }
}

/**
 * Exposes the JavaScript [ArrayBufferView](https://developer.mozilla.org/en/docs/Web/API/ArrayBufferView) to Kotlin
 */
public external interface ArrayBufferView : BufferDataSource, JsAny {
    konst buffer: ArrayBuffer
    konst byteOffset: Int
    konst byteLength: Int
}

/**
 * Exposes the JavaScript [Int8Array](https://developer.mozilla.org/en/docs/Web/API/Int8Array) to Kotlin
 */
public external open class Int8Array : ArrayBufferView, JsAny {
    constructor(length: Int)
    constructor(array: Int8Array)
    constructor(array: JsArray<JsNumber>)
    constructor(buffer: ArrayBuffer, byteOffset: Int = definedExternally, length: Int = definedExternally)
    open konst length: Int
    override konst buffer: ArrayBuffer
    override konst byteOffset: Int
    override konst byteLength: Int
    fun set(array: Int8Array, offset: Int = definedExternally)
    fun set(array: JsArray<JsNumber>, offset: Int = definedExternally)
    fun subarray(start: Int, end: Int): Int8Array

    companion object {
        konst BYTES_PER_ELEMENT: Int
    }
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForInt8Array(obj: Int8Array, index: Int): Byte { js("return obj[index];") }

public operator fun Int8Array.get(index: Int): Byte = getMethodImplForInt8Array(this, index)

@Suppress("UNUSED_PARAMETER")
internal fun setMethodImplForInt8Array(obj: Int8Array, index: Int, konstue: Byte) { js("obj[index] = konstue;") }

public operator fun Int8Array.set(index: Int, konstue: Byte) = setMethodImplForInt8Array(this, index, konstue)

/**
 * Exposes the JavaScript [Uint8Array](https://developer.mozilla.org/en/docs/Web/API/Uint8Array) to Kotlin
 */
public external open class Uint8Array : ArrayBufferView, JsAny {
    constructor(length: Int)
    constructor(array: Uint8Array)
    constructor(array: JsArray<JsNumber>)
    constructor(buffer: ArrayBuffer, byteOffset: Int = definedExternally, length: Int = definedExternally)
    open konst length: Int
    override konst buffer: ArrayBuffer
    override konst byteOffset: Int
    override konst byteLength: Int
    fun set(array: Uint8Array, offset: Int = definedExternally)
    fun set(array: JsArray<JsNumber>, offset: Int = definedExternally)
    fun subarray(start: Int, end: Int): Uint8Array

    companion object {
        konst BYTES_PER_ELEMENT: Int
    }
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForUint8Array(obj: Uint8Array, index: Int): Byte { js("return obj[index];") }

public operator fun Uint8Array.get(index: Int): Byte = getMethodImplForUint8Array(this, index)

@Suppress("UNUSED_PARAMETER")
internal fun setMethodImplForUint8Array(obj: Uint8Array, index: Int, konstue: Byte) { js("obj[index] = konstue;") }

public operator fun Uint8Array.set(index: Int, konstue: Byte) = setMethodImplForUint8Array(this, index, konstue)

/**
 * Exposes the JavaScript [Uint8ClampedArray](https://developer.mozilla.org/en/docs/Web/API/Uint8ClampedArray) to Kotlin
 */
public external open class Uint8ClampedArray : ArrayBufferView, JsAny {
    constructor(length: Int)
    constructor(array: Uint8ClampedArray)
    constructor(array: JsArray<JsNumber>)
    constructor(buffer: ArrayBuffer, byteOffset: Int = definedExternally, length: Int = definedExternally)
    open konst length: Int
    override konst buffer: ArrayBuffer
    override konst byteOffset: Int
    override konst byteLength: Int
    fun set(array: Uint8ClampedArray, offset: Int = definedExternally)
    fun set(array: JsArray<JsNumber>, offset: Int = definedExternally)
    fun subarray(start: Int, end: Int): Uint8ClampedArray

    companion object {
        konst BYTES_PER_ELEMENT: Int
    }
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForUint8ClampedArray(obj: Uint8ClampedArray, index: Int): Byte { js("return obj[index];") }

public operator fun Uint8ClampedArray.get(index: Int): Byte = getMethodImplForUint8ClampedArray(this, index)

@Suppress("UNUSED_PARAMETER")
internal fun setMethodImplForUint8ClampedArray(obj: Uint8ClampedArray, index: Int, konstue: Byte) { js("obj[index] = konstue;") }

public operator fun Uint8ClampedArray.set(index: Int, konstue: Byte) = setMethodImplForUint8ClampedArray(this, index, konstue)

/**
 * Exposes the JavaScript [Int16Array](https://developer.mozilla.org/en/docs/Web/API/Int16Array) to Kotlin
 */
public external open class Int16Array : ArrayBufferView, JsAny {
    constructor(length: Int)
    constructor(array: Int16Array)
    constructor(array: JsArray<JsNumber>)
    constructor(buffer: ArrayBuffer, byteOffset: Int = definedExternally, length: Int = definedExternally)
    open konst length: Int
    override konst buffer: ArrayBuffer
    override konst byteOffset: Int
    override konst byteLength: Int
    fun set(array: Int16Array, offset: Int = definedExternally)
    fun set(array: JsArray<JsNumber>, offset: Int = definedExternally)
    fun subarray(start: Int, end: Int): Int16Array

    companion object {
        konst BYTES_PER_ELEMENT: Int
    }
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForInt16Array(obj: Int16Array, index: Int): Short { js("return obj[index];") }

public operator fun Int16Array.get(index: Int): Short = getMethodImplForInt16Array(this, index)

@Suppress("UNUSED_PARAMETER")
internal fun setMethodImplForInt16Array(obj: Int16Array, index: Int, konstue: Short) { js("obj[index] = konstue;") }

public operator fun Int16Array.set(index: Int, konstue: Short) = setMethodImplForInt16Array(this, index, konstue)

/**
 * Exposes the JavaScript [Uint16Array](https://developer.mozilla.org/en/docs/Web/API/Uint16Array) to Kotlin
 */
public external open class Uint16Array : ArrayBufferView, JsAny {
    constructor(length: Int)
    constructor(array: Uint16Array)
    constructor(array: JsArray<JsNumber>)
    constructor(buffer: ArrayBuffer, byteOffset: Int = definedExternally, length: Int = definedExternally)
    open konst length: Int
    override konst buffer: ArrayBuffer
    override konst byteOffset: Int
    override konst byteLength: Int
    fun set(array: Uint16Array, offset: Int = definedExternally)
    fun set(array: JsArray<JsNumber>, offset: Int = definedExternally)
    fun subarray(start: Int, end: Int): Uint16Array

    companion object {
        konst BYTES_PER_ELEMENT: Int
    }
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForUint16Array(obj: Uint16Array, index: Int): Short { js("return obj[index];") }

public operator fun Uint16Array.get(index: Int): Short = getMethodImplForUint16Array(this, index)

@Suppress("UNUSED_PARAMETER")
internal fun setMethodImplForUint16Array(obj: Uint16Array, index: Int, konstue: Short) { js("obj[index] = konstue;") }

public operator fun Uint16Array.set(index: Int, konstue: Short) = setMethodImplForUint16Array(this, index, konstue)

/**
 * Exposes the JavaScript [Int32Array](https://developer.mozilla.org/en/docs/Web/API/Int32Array) to Kotlin
 */
public external open class Int32Array : ArrayBufferView, JsAny {
    constructor(length: Int)
    constructor(array: Int32Array)
    constructor(array: JsArray<JsNumber>)
    constructor(buffer: ArrayBuffer, byteOffset: Int = definedExternally, length: Int = definedExternally)
    open konst length: Int
    override konst buffer: ArrayBuffer
    override konst byteOffset: Int
    override konst byteLength: Int
    fun set(array: Int32Array, offset: Int = definedExternally)
    fun set(array: JsArray<JsNumber>, offset: Int = definedExternally)
    fun subarray(start: Int, end: Int): Int32Array

    companion object {
        konst BYTES_PER_ELEMENT: Int
    }
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForInt32Array(obj: Int32Array, index: Int): Int { js("return obj[index];") }

public operator fun Int32Array.get(index: Int): Int = getMethodImplForInt32Array(this, index)

@Suppress("UNUSED_PARAMETER")
internal fun setMethodImplForInt32Array(obj: Int32Array, index: Int, konstue: Int) { js("obj[index] = konstue;") }

public operator fun Int32Array.set(index: Int, konstue: Int) = setMethodImplForInt32Array(this, index, konstue)

/**
 * Exposes the JavaScript [Uint32Array](https://developer.mozilla.org/en/docs/Web/API/Uint32Array) to Kotlin
 */
public external open class Uint32Array : ArrayBufferView, JsAny {
    constructor(length: Int)
    constructor(array: Uint32Array)
    constructor(array: JsArray<JsNumber>)
    constructor(buffer: ArrayBuffer, byteOffset: Int = definedExternally, length: Int = definedExternally)
    open konst length: Int
    override konst buffer: ArrayBuffer
    override konst byteOffset: Int
    override konst byteLength: Int
    fun set(array: Uint32Array, offset: Int = definedExternally)
    fun set(array: JsArray<JsNumber>, offset: Int = definedExternally)
    fun subarray(start: Int, end: Int): Uint32Array

    companion object {
        konst BYTES_PER_ELEMENT: Int
    }
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForUint32Array(obj: Uint32Array, index: Int): Int { js("return obj[index];") }

public operator fun Uint32Array.get(index: Int): Int = getMethodImplForUint32Array(this, index)

@Suppress("UNUSED_PARAMETER")
internal fun setMethodImplForUint32Array(obj: Uint32Array, index: Int, konstue: Int) { js("obj[index] = konstue;") }

public operator fun Uint32Array.set(index: Int, konstue: Int) = setMethodImplForUint32Array(this, index, konstue)

/**
 * Exposes the JavaScript [Float32Array](https://developer.mozilla.org/en/docs/Web/API/Float32Array) to Kotlin
 */
public external open class Float32Array : ArrayBufferView, JsAny {
    constructor(length: Int)
    constructor(array: Float32Array)
    constructor(array: JsArray<JsNumber>)
    constructor(buffer: ArrayBuffer, byteOffset: Int = definedExternally, length: Int = definedExternally)
    open konst length: Int
    override konst buffer: ArrayBuffer
    override konst byteOffset: Int
    override konst byteLength: Int
    fun set(array: Float32Array, offset: Int = definedExternally)
    fun set(array: JsArray<JsNumber>, offset: Int = definedExternally)
    fun subarray(start: Int, end: Int): Float32Array

    companion object {
        konst BYTES_PER_ELEMENT: Int
    }
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForFloat32Array(obj: Float32Array, index: Int): Float { js("return obj[index];") }

public operator fun Float32Array.get(index: Int): Float = getMethodImplForFloat32Array(this, index)

@Suppress("UNUSED_PARAMETER")
internal fun setMethodImplForFloat32Array(obj: Float32Array, index: Int, konstue: Float) { js("obj[index] = konstue;") }

public operator fun Float32Array.set(index: Int, konstue: Float) = setMethodImplForFloat32Array(this, index, konstue)

/**
 * Exposes the JavaScript [Float64Array](https://developer.mozilla.org/en/docs/Web/API/Float64Array) to Kotlin
 */
public external open class Float64Array : ArrayBufferView, JsAny {
    constructor(length: Int)
    constructor(array: Float64Array)
    constructor(array: JsArray<JsNumber>)
    constructor(buffer: ArrayBuffer, byteOffset: Int = definedExternally, length: Int = definedExternally)
    open konst length: Int
    override konst buffer: ArrayBuffer
    override konst byteOffset: Int
    override konst byteLength: Int
    fun set(array: Float64Array, offset: Int = definedExternally)
    fun set(array: JsArray<JsNumber>, offset: Int = definedExternally)
    fun subarray(start: Int, end: Int): Float64Array

    companion object {
        konst BYTES_PER_ELEMENT: Int
    }
}

@Suppress("UNUSED_PARAMETER")
internal fun getMethodImplForFloat64Array(obj: Float64Array, index: Int): Double { js("return obj[index];") }

public operator fun Float64Array.get(index: Int): Double = getMethodImplForFloat64Array(this, index)

@Suppress("UNUSED_PARAMETER")
internal fun setMethodImplForFloat64Array(obj: Float64Array, index: Int, konstue: Double) { js("obj[index] = konstue;") }

public operator fun Float64Array.set(index: Int, konstue: Double) = setMethodImplForFloat64Array(this, index, konstue)

/**
 * Exposes the JavaScript [DataView](https://developer.mozilla.org/en/docs/Web/API/DataView) to Kotlin
 */
public external open class DataView(buffer: ArrayBuffer, byteOffset: Int = definedExternally, byteLength: Int = definedExternally) : ArrayBufferView, JsAny {
    override konst buffer: ArrayBuffer
    override konst byteOffset: Int
    override konst byteLength: Int
    fun getInt8(byteOffset: Int): Byte
    fun getUint8(byteOffset: Int): Byte
    fun getInt16(byteOffset: Int, littleEndian: Boolean = definedExternally): Short
    fun getUint16(byteOffset: Int, littleEndian: Boolean = definedExternally): Short
    fun getInt32(byteOffset: Int, littleEndian: Boolean = definedExternally): Int
    fun getUint32(byteOffset: Int, littleEndian: Boolean = definedExternally): Int
    fun getFloat32(byteOffset: Int, littleEndian: Boolean = definedExternally): Float
    fun getFloat64(byteOffset: Int, littleEndian: Boolean = definedExternally): Double
    fun setInt8(byteOffset: Int, konstue: Byte)
    fun setUint8(byteOffset: Int, konstue: Byte)
    fun setInt16(byteOffset: Int, konstue: Short, littleEndian: Boolean = definedExternally)
    fun setUint16(byteOffset: Int, konstue: Short, littleEndian: Boolean = definedExternally)
    fun setInt32(byteOffset: Int, konstue: Int, littleEndian: Boolean = definedExternally)
    fun setUint32(byteOffset: Int, konstue: Int, littleEndian: Boolean = definedExternally)
    fun setFloat32(byteOffset: Int, konstue: Float, littleEndian: Boolean = definedExternally)
    fun setFloat64(byteOffset: Int, konstue: Double, littleEndian: Boolean = definedExternally)
}

public external interface BufferDataSource

public external interface TexImageSource