//Generated by the protocol buffer compiler. DO NOT EDIT!
// source: proto_kpm.proto

package org.jetbrains.kotlin.gradle.idea.proto.generated.kpm;

@kotlin.jvm.JvmName("-initializeideaKpmSchemaInfoProto")
inline fun ideaKpmSchemaInfoProto(block: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmSchemaInfoProtoKt.Dsl.() -> kotlin.Unit): org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmSchemaInfoProto =
  org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmSchemaInfoProtoKt.Dsl._create(org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmSchemaInfoProto.newBuilder()).apply { block() }._build()
object IdeaKpmSchemaInfoProtoKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  class Dsl private constructor(
    private konst _builder: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmSchemaInfoProto.Builder
  ) {
    companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmSchemaInfoProto.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmSchemaInfoProto = _builder.build()

    /**
     * <code>optional uint32 since_schema_version_major = 1;</code>
     */
    var sinceSchemaVersionMajor: kotlin.Int
      @JvmName("getSinceSchemaVersionMajor")
      get() = _builder.getSinceSchemaVersionMajor()
      @JvmName("setSinceSchemaVersionMajor")
      set(konstue) {
        _builder.setSinceSchemaVersionMajor(konstue)
      }
    /**
     * <code>optional uint32 since_schema_version_major = 1;</code>
     */
    fun clearSinceSchemaVersionMajor() {
      _builder.clearSinceSchemaVersionMajor()
    }
    /**
     * <code>optional uint32 since_schema_version_major = 1;</code>
     * @return Whether the sinceSchemaVersionMajor field is set.
     */
    fun hasSinceSchemaVersionMajor(): kotlin.Boolean {
      return _builder.hasSinceSchemaVersionMajor()
    }

    /**
     * <code>optional uint32 since_schema_version_minor = 2;</code>
     */
    var sinceSchemaVersionMinor: kotlin.Int
      @JvmName("getSinceSchemaVersionMinor")
      get() = _builder.getSinceSchemaVersionMinor()
      @JvmName("setSinceSchemaVersionMinor")
      set(konstue) {
        _builder.setSinceSchemaVersionMinor(konstue)
      }
    /**
     * <code>optional uint32 since_schema_version_minor = 2;</code>
     */
    fun clearSinceSchemaVersionMinor() {
      _builder.clearSinceSchemaVersionMinor()
    }
    /**
     * <code>optional uint32 since_schema_version_minor = 2;</code>
     * @return Whether the sinceSchemaVersionMinor field is set.
     */
    fun hasSinceSchemaVersionMinor(): kotlin.Boolean {
      return _builder.hasSinceSchemaVersionMinor()
    }

    /**
     * <code>optional uint32 since_schema_version_patch = 3;</code>
     */
    var sinceSchemaVersionPatch: kotlin.Int
      @JvmName("getSinceSchemaVersionPatch")
      get() = _builder.getSinceSchemaVersionPatch()
      @JvmName("setSinceSchemaVersionPatch")
      set(konstue) {
        _builder.setSinceSchemaVersionPatch(konstue)
      }
    /**
     * <code>optional uint32 since_schema_version_patch = 3;</code>
     */
    fun clearSinceSchemaVersionPatch() {
      _builder.clearSinceSchemaVersionPatch()
    }
    /**
     * <code>optional uint32 since_schema_version_patch = 3;</code>
     * @return Whether the sinceSchemaVersionPatch field is set.
     */
    fun hasSinceSchemaVersionPatch(): kotlin.Boolean {
      return _builder.hasSinceSchemaVersionPatch()
    }

    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmSchemaInfoProto.Severity severity = 4;</code>
     */
     var severity: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmSchemaInfoProto.Severity
      @JvmName("getSeverity")
      get() = _builder.getSeverity()
      @JvmName("setSeverity")
      set(konstue) {
        _builder.setSeverity(konstue)
      }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmSchemaInfoProto.Severity severity = 4;</code>
     */
    fun clearSeverity() {
      _builder.clearSeverity()
    }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmSchemaInfoProto.Severity severity = 4;</code>
     * @return Whether the severity field is set.
     */
    fun hasSeverity(): kotlin.Boolean {
      return _builder.hasSeverity()
    }

    /**
     * <code>optional string message = 5;</code>
     */
    var message: kotlin.String
      @JvmName("getMessage")
      get() = _builder.getMessage()
      @JvmName("setMessage")
      set(konstue) {
        _builder.setMessage(konstue)
      }
    /**
     * <code>optional string message = 5;</code>
     */
    fun clearMessage() {
      _builder.clearMessage()
    }
    /**
     * <code>optional string message = 5;</code>
     * @return Whether the message field is set.
     */
    fun hasMessage(): kotlin.Boolean {
      return _builder.hasMessage()
    }
  }
}
@kotlin.jvm.JvmSynthetic
inline fun org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmSchemaInfoProto.copy(block: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmSchemaInfoProtoKt.Dsl.() -> kotlin.Unit): org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmSchemaInfoProto =
  org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmSchemaInfoProtoKt.Dsl._create(this.toBuilder()).apply { block() }._build()

