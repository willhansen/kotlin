//Generated by the protocol buffer compiler. DO NOT EDIT!
// source: proto_kpm.proto

package org.jetbrains.kotlin.gradle.idea.proto.generated.kpm;

@kotlin.jvm.JvmName("-initializeideaKpmFragmentProto")
inline fun ideaKpmFragmentProto(block: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentProtoKt.Dsl.() -> kotlin.Unit): org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentProto =
  org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentProtoKt.Dsl._create(org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentProto.newBuilder()).apply { block() }._build()
object IdeaKpmFragmentProtoKt {
  @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
  @com.google.protobuf.kotlin.ProtoDslMarker
  class Dsl private constructor(
    private konst _builder: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentProto.Builder
  ) {
    companion object {
      @kotlin.jvm.JvmSynthetic
      @kotlin.PublishedApi
      internal fun _create(builder: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentProto.Builder): Dsl = Dsl(builder)
    }

    @kotlin.jvm.JvmSynthetic
    @kotlin.PublishedApi
    internal fun _build(): org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentProto = _builder.build()

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
    konst IdeaKpmFragmentProtoKt.Dsl.extrasOrNull: org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto?
      get() = _builder.extrasOrNull

    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentCoordinatesProto coordinates = 2;</code>
     */
    var coordinates: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentCoordinatesProto
      @JvmName("getCoordinates")
      get() = _builder.getCoordinates()
      @JvmName("setCoordinates")
      set(konstue) {
        _builder.setCoordinates(konstue)
      }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentCoordinatesProto coordinates = 2;</code>
     */
    fun clearCoordinates() {
      _builder.clearCoordinates()
    }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentCoordinatesProto coordinates = 2;</code>
     * @return Whether the coordinates field is set.
     */
    fun hasCoordinates(): kotlin.Boolean {
      return _builder.hasCoordinates()
    }
    konst IdeaKpmFragmentProtoKt.Dsl.coordinatesOrNull: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentCoordinatesProto?
      get() = _builder.coordinatesOrNull

    /**
     * An uninstantiable, behaviorless type to represent the field in
     * generics.
     */
    @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
    class PlatformsProxy private constructor() : com.google.protobuf.kotlin.DslProxy()
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto platforms = 3;</code>
     */
     konst platforms: com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto, PlatformsProxy>
      @kotlin.jvm.JvmSynthetic
      get() = com.google.protobuf.kotlin.DslList(
        _builder.getPlatformsList()
      )
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto platforms = 3;</code>
     * @param konstue The platforms to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addPlatforms")
    fun com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto, PlatformsProxy>.add(konstue: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto) {
      _builder.addPlatforms(konstue)
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto platforms = 3;</code>
     * @param konstue The platforms to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignPlatforms")
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto, PlatformsProxy>.plusAssign(konstue: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto) {
      add(konstue)
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto platforms = 3;</code>
     * @param konstues The platforms to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addAllPlatforms")
    fun com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto, PlatformsProxy>.addAll(konstues: kotlin.collections.Iterable<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto>) {
      _builder.addAllPlatforms(konstues)
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto platforms = 3;</code>
     * @param konstues The platforms to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignAllPlatforms")
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto, PlatformsProxy>.plusAssign(konstues: kotlin.collections.Iterable<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto>) {
      addAll(konstues)
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto platforms = 3;</code>
     * @param index The index to set the konstue at.
     * @param konstue The platforms to set.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("setPlatforms")
    operator fun com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto, PlatformsProxy>.set(index: kotlin.Int, konstue: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto) {
      _builder.setPlatforms(index, konstue)
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto platforms = 3;</code>
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("clearPlatforms")
    fun com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmPlatformProto, PlatformsProxy>.clear() {
      _builder.clearPlatforms()
    }


    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmLanguageSettingsProto language_settings = 4;</code>
     */
    var languageSettings: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmLanguageSettingsProto
      @JvmName("getLanguageSettings")
      get() = _builder.getLanguageSettings()
      @JvmName("setLanguageSettings")
      set(konstue) {
        _builder.setLanguageSettings(konstue)
      }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmLanguageSettingsProto language_settings = 4;</code>
     */
    fun clearLanguageSettings() {
      _builder.clearLanguageSettings()
    }
    /**
     * <code>optional .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmLanguageSettingsProto language_settings = 4;</code>
     * @return Whether the languageSettings field is set.
     */
    fun hasLanguageSettings(): kotlin.Boolean {
      return _builder.hasLanguageSettings()
    }
    konst IdeaKpmFragmentProtoKt.Dsl.languageSettingsOrNull: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmLanguageSettingsProto?
      get() = _builder.languageSettingsOrNull

    /**
     * An uninstantiable, behaviorless type to represent the field in
     * generics.
     */
    @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
    class DependenciesProxy private constructor() : com.google.protobuf.kotlin.DslProxy()
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmDependencyProto dependencies = 5;</code>
     */
     konst dependencies: com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmDependencyProto, DependenciesProxy>
      @kotlin.jvm.JvmSynthetic
      get() = com.google.protobuf.kotlin.DslList(
        _builder.getDependenciesList()
      )
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmDependencyProto dependencies = 5;</code>
     * @param konstue The dependencies to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addDependencies")
    fun com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmDependencyProto, DependenciesProxy>.add(konstue: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmDependencyProto) {
      _builder.addDependencies(konstue)
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmDependencyProto dependencies = 5;</code>
     * @param konstue The dependencies to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignDependencies")
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmDependencyProto, DependenciesProxy>.plusAssign(konstue: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmDependencyProto) {
      add(konstue)
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmDependencyProto dependencies = 5;</code>
     * @param konstues The dependencies to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addAllDependencies")
    fun com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmDependencyProto, DependenciesProxy>.addAll(konstues: kotlin.collections.Iterable<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmDependencyProto>) {
      _builder.addAllDependencies(konstues)
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmDependencyProto dependencies = 5;</code>
     * @param konstues The dependencies to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignAllDependencies")
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmDependencyProto, DependenciesProxy>.plusAssign(konstues: kotlin.collections.Iterable<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmDependencyProto>) {
      addAll(konstues)
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmDependencyProto dependencies = 5;</code>
     * @param index The index to set the konstue at.
     * @param konstue The dependencies to set.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("setDependencies")
    operator fun com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmDependencyProto, DependenciesProxy>.set(index: kotlin.Int, konstue: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmDependencyProto) {
      _builder.setDependencies(index, konstue)
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmDependencyProto dependencies = 5;</code>
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("clearDependencies")
    fun com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmDependencyProto, DependenciesProxy>.clear() {
      _builder.clearDependencies()
    }


    /**
     * An uninstantiable, behaviorless type to represent the field in
     * generics.
     */
    @kotlin.OptIn(com.google.protobuf.kotlin.OnlyForUseByGeneratedProtoCode::class)
    class SourceDirectoriesProxy private constructor() : com.google.protobuf.kotlin.DslProxy()
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto source_directories = 6;</code>
     */
     konst sourceDirectories: com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto, SourceDirectoriesProxy>
      @kotlin.jvm.JvmSynthetic
      get() = com.google.protobuf.kotlin.DslList(
        _builder.getSourceDirectoriesList()
      )
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto source_directories = 6;</code>
     * @param konstue The sourceDirectories to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addSourceDirectories")
    fun com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto, SourceDirectoriesProxy>.add(konstue: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto) {
      _builder.addSourceDirectories(konstue)
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto source_directories = 6;</code>
     * @param konstue The sourceDirectories to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignSourceDirectories")
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto, SourceDirectoriesProxy>.plusAssign(konstue: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto) {
      add(konstue)
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto source_directories = 6;</code>
     * @param konstues The sourceDirectories to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("addAllSourceDirectories")
    fun com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto, SourceDirectoriesProxy>.addAll(konstues: kotlin.collections.Iterable<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto>) {
      _builder.addAllSourceDirectories(konstues)
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto source_directories = 6;</code>
     * @param konstues The sourceDirectories to add.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("plusAssignAllSourceDirectories")
    @Suppress("NOTHING_TO_INLINE")
    inline operator fun com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto, SourceDirectoriesProxy>.plusAssign(konstues: kotlin.collections.Iterable<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto>) {
      addAll(konstues)
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto source_directories = 6;</code>
     * @param index The index to set the konstue at.
     * @param konstue The sourceDirectories to set.
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("setSourceDirectories")
    operator fun com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto, SourceDirectoriesProxy>.set(index: kotlin.Int, konstue: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto) {
      _builder.setSourceDirectories(index, konstue)
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto source_directories = 6;</code>
     */
    @kotlin.jvm.JvmSynthetic
    @kotlin.jvm.JvmName("clearSourceDirectories")
    fun com.google.protobuf.kotlin.DslList<org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmContentRootProto, SourceDirectoriesProxy>.clear() {
      _builder.clearSourceDirectories()
    }

  }
}
@kotlin.jvm.JvmSynthetic
inline fun org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentProto.copy(block: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentProtoKt.Dsl.() -> kotlin.Unit): org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentProto =
  org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentProtoKt.Dsl._create(this.toBuilder()).apply { block() }._build()

konst org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentProtoOrBuilder.extrasOrNull: org.jetbrains.kotlin.gradle.idea.proto.generated.IdeaExtrasProto?
  get() = if (hasExtras()) getExtras() else null

konst org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentProtoOrBuilder.coordinatesOrNull: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentCoordinatesProto?
  get() = if (hasCoordinates()) getCoordinates() else null

konst org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmFragmentProtoOrBuilder.languageSettingsOrNull: org.jetbrains.kotlin.gradle.idea.proto.generated.kpm.IdeaKpmLanguageSettingsProto?
  get() = if (hasLanguageSettings()) getLanguageSettings() else null

