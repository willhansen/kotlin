//Generated by the protocol buffer compiler. DO NOT EDIT!
// source: proto_kpm.proto

package org.jetbrains.kotlin.gradle.idea.proto.generated.kpm;

@kotlin.jvm.JvmName("-initializeideaKpmContentRootProto")
inline fun ideaKpmContentRootProto(block: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProtoKt.Dsl.() -> kotlin.Unit): org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto =
  org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProtoKt.Dsl._create(org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto.newBuilder()).apply { block() }._build()
object IdeaKpmContentRootProtoKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  class Dsl private constructor(
    private konst _builder: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto.Builder
  ) {
    companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto = _builder.build()

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
    konst IdeaKpmContentRootProtoKt.Dsl.extrasOrNull: org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto?
      get() = _builder.extrasOrNull

    /**
     * <code>optional string absolute_path = 2;</code>
     */
    var absolutePath: kotlin.String
      @JvmName("getAbsolutePath")
      get() = _builder.getAbsolutePath()
      @JvmName("setAbsolutePath")
      set(konstue) {
        _builder.setAbsolutePath(konstue)
      }
    /**
     * <code>optional string absolute_path = 2;</code>
     */
    fun clearAbsolutePath() {
      _builder.clearAbsolutePath()
    }
    /**
     * <code>optional string absolute_path = 2;</code>
     * @return Whether the absolutePath field is set.
     */
    fun hasAbsolutePath(): kotlin.Boolean {
      return _builder.hasAbsolutePath()
    }

    /**
     * <code>optional string type = 3;</code>
     */
    var type: kotlin.String
      @JvmName("getType")
      get() = _builder.getType()
      @JvmName("setType")
      set(konstue) {
        _builder.setType(konstue)
      }
    /**
     * <code>optional string type = 3;</code>
     */
    fun clearType() {
      _builder.clearType()
    }
    /**
     * <code>optional string type = 3;</code>
     * @return Whether the type field is set.
     */
    fun hasType(): kotlin.Boolean {
      return _builder.hasType()
    }
  }
}
@kotlin.jvm.JvmSynthetic
inline fun org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto.copy(block: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProtoKt.Dsl.() -> kotlin.Unit): org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto =
  org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProtoKt.Dsl._create(this.toBuilder()).apply { block() }._build()

konst org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProtoOrBuilder.extrasOrNull: org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto?
  get() = if (hasExtras()) getExtras() else null

