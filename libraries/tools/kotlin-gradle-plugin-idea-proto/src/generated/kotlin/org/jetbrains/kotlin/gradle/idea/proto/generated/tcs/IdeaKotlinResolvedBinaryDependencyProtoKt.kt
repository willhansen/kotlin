//Generated by the protocol buffer compiler. DO NOT EDIT!
// source: proto_tcs.proto

package org.jetbrains.kotlin.gradle.idea.proto.generated.tcs;

@kotlin.jvm.JvmName("-initializeideaKotlinResolvedBinaryDependencyProto")
inline fun ideaKotlinResolvedBinaryDependencyProto(block: org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinResolvedBinaryDependencyProtoKt.Dsl.() -> kotlin.Unit): org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinResolvedBinaryDependencyProto =
  org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinResolvedBinaryDependencyProtoKt.Dsl._create(org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinResolvedBinaryDependencyProto.newBuilder()).apply { block() }._build()
object IdeaKotlinResolvedBinaryDependencyProtoKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  class Dsl private constructor(
    private konst _builder: org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinResolvedBinaryDependencyProto.Builder
  ) {
    companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinResolvedBinaryDependencyProto.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinResolvedBinaryDependencyProto = _builder.build()

    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
     */
    var extras: org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto
      @JvmName("getExtras")
      get() = _builder.getExtras()
      @JvmName("setExtras")
      set(konstue) {
        _builder.setExtras(konstue)
      }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
     */
    fun clearExtras() {
      _builder.clearExtras()
    }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto extras = 1;</code>
     * @return Whether the extras field is set.
     */
    fun hasExtras(): kotlin.Boolean {
      return _builder.hasExtras()
    }
    konst IdeaKotlinResolvedBinaryDependencyProtoKt.Dsl.extrasOrNull: org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto?
      get() = _builder.extrasOrNull

    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinBinaryCoordinatesProto coordinates = 2;</code>
     */
    var coordinates: org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinBinaryCoordinatesProto
      @JvmName("getCoordinates")
      get() = _builder.getCoordinates()
      @JvmName("setCoordinates")
      set(konstue) {
        _builder.setCoordinates(konstue)
      }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinBinaryCoordinatesProto coordinates = 2;</code>
     */
    fun clearCoordinates() {
      _builder.clearCoordinates()
    }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinBinaryCoordinatesProto coordinates = 2;</code>
     * @return Whether the coordinates field is set.
     */
    fun hasCoordinates(): kotlin.Boolean {
      return _builder.hasCoordinates()
    }
    konst IdeaKotlinResolvedBinaryDependencyProtoKt.Dsl.coordinatesOrNull: org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinBinaryCoordinatesProto?
      get() = _builder.coordinatesOrNull

    /**
     * <code>optional string binary_type = 3;</code>
     */
    var binaryType: kotlin.String
      @JvmName("getBinaryType")
      get() = _builder.getBinaryType()
      @JvmName("setBinaryType")
      set(konstue) {
        _builder.setBinaryType(konstue)
      }
    /**
     * <code>optional string binary_type = 3;</code>
     */
    fun clearBinaryType() {
      _builder.clearBinaryType()
    }
    /**
     * <code>optional string binary_type = 3;</code>
     * @return Whether the binaryType field is set.
     */
    fun hasBinaryType(): kotlin.Boolean {
      return _builder.hasBinaryType()
    }

    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinClasspathProto classpath = 4;</code>
     */
    var classpath: org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinClasspathProto
      @JvmName("getClasspath")
      get() = _builder.getClasspath()
      @JvmName("setClasspath")
      set(konstue) {
        _builder.setClasspath(konstue)
      }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinClasspathProto classpath = 4;</code>
     */
    fun clearClasspath() {
      _builder.clearClasspath()
    }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinClasspathProto classpath = 4;</code>
     * @return Whether the classpath field is set.
     */
    fun hasClasspath(): kotlin.Boolean {
      return _builder.hasClasspath()
    }
    konst IdeaKotlinResolvedBinaryDependencyProtoKt.Dsl.classpathOrNull: org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinClasspathProto?
      get() = _builder.classpathOrNull
  }
}
@kotlin.jvm.JvmSynthetic
inline fun org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinResolvedBinaryDependencyProto.copy(block: org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinResolvedBinaryDependencyProtoKt.Dsl.() -> kotlin.Unit): org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinResolvedBinaryDependencyProto =
  org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinResolvedBinaryDependencyProtoKt.Dsl._create(this.toBuilder()).apply { block() }._build()

konst org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinResolvedBinaryDependencyProtoOrBuilder.extrasOrNull: org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto?
  get() = if (hasExtras()) getExtras() else null

konst org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinResolvedBinaryDependencyProtoOrBuilder.coordinatesOrNull: org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinBinaryCoordinatesProto?
  get() = if (hasCoordinates()) getCoordinates() else null

konst org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinResolvedBinaryDependencyProtoOrBuilder.classpathOrNull: org.jetbrains.kotlin.gradle.idea.proto.generated.tcs.IdeaKotlinClasspathProto?
  get() = if (hasClasspath()) getClasspath() else null

