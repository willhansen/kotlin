//Generated by the protocol buffer compiler. DO NOT EDIT!
// source: proto_kpm.proto

package org.jetbrains.kotlin.gradle.idea.proto.generated.kpm;

@kotlin.jvm.JvmName("-initializeideaKpmJvmPlatformProto")
inline fun ideaKpmJvmPlatformProto(block: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProtoKt.Dsl.() -> kotlin.Unit): org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto =
  org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProtoKt.Dsl._create(org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto.newBuilder()).apply { block() }._build()
object IdeaKpmJvmPlatformProtoKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  class Dsl private constructor(
    private konst _builder: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto.Builder
  ) {
    companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto = _builder.build()

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
    konst IdeaKpmJvmPlatformProtoKt.Dsl.extrasOrNull: org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto?
      get() = _builder.extrasOrNull

    /**
     * <code>optional string jvm_target = 2;</code>
     */
    var jvmTarget: kotlin.String
      @JvmName("getJvmTarget")
      get() = _builder.getJvmTarget()
      @JvmName("setJvmTarget")
      set(konstue) {
        _builder.setJvmTarget(konstue)
      }
    /**
     * <code>optional string jvm_target = 2;</code>
     */
    fun clearJvmTarget() {
      _builder.clearJvmTarget()
    }
    /**
     * <code>optional string jvm_target = 2;</code>
     * @return Whether the jvmTarget field is set.
     */
    fun hasJvmTarget(): kotlin.Boolean {
      return _builder.hasJvmTarget()
    }
  }
}
@kotlin.jvm.JvmSynthetic
inline fun org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto.copy(block: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProtoKt.Dsl.() -> kotlin.Unit): org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProto =
  org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProtoKt.Dsl._create(this.toBuilder()).apply { block() }._build()

konst org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmJvmPlatformProtoOrBuilder.extrasOrNull: org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto?
  get() = if (hasExtras()) getExtras() else null

