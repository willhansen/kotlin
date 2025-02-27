// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: compiler/ir/serialization.common/src/KotlinIr.proto

package org.jetbrains.kotlin.backend.common.serialization.proto;

/**
 * Protobuf type {@code org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias}
 */
public final class IrTypeAlias extends
    org.jetbrains.kotlin.protobuf.GeneratedMessageLite implements
    // @@protoc_insertion_point(message_implements:org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias)
    IrTypeAliasOrBuilder {
  // Use IrTypeAlias.newBuilder() to construct.
  private IrTypeAlias(org.jetbrains.kotlin.protobuf.GeneratedMessageLite.Builder builder) {
    super(builder);
    this.unknownFields = builder.getUnknownFields();
  }
  private IrTypeAlias(boolean noInit) { this.unknownFields = org.jetbrains.kotlin.protobuf.ByteString.EMPTY;}

  private static final IrTypeAlias defaultInstance;
  public static IrTypeAlias getDefaultInstance() {
    return defaultInstance;
  }

  public IrTypeAlias getDefaultInstanceForType() {
    return defaultInstance;
  }

  private final org.jetbrains.kotlin.protobuf.ByteString unknownFields;
  private IrTypeAlias(
      org.jetbrains.kotlin.protobuf.CodedInputStream input,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    initFields();
    int mutable_bitField0_ = 0;
    org.jetbrains.kotlin.protobuf.ByteString.Output unknownFieldsOutput =
        org.jetbrains.kotlin.protobuf.ByteString.newOutput();
    org.jetbrains.kotlin.protobuf.CodedOutputStream unknownFieldsCodedOutput =
        org.jetbrains.kotlin.protobuf.CodedOutputStream.newInstance(
            unknownFieldsOutput, 1);
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          default: {
            if (!parseUnknownField(input, unknownFieldsCodedOutput,
                                   extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
          case 10: {
            org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase.Builder subBuilder = null;
            if (((bitField0_ & 0x00000001) == 0x00000001)) {
              subBuilder = base_.toBuilder();
            }
            base_ = input.readMessage(org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase.PARSER, extensionRegistry);
            if (subBuilder != null) {
              subBuilder.mergeFrom(base_);
              base_ = subBuilder.buildPartial();
            }
            bitField0_ |= 0x00000001;
            break;
          }
          case 16: {
            bitField0_ |= 0x00000002;
            nameType_ = input.readInt64();
            break;
          }
          case 26: {
            if (!((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
              typeParameter_ = new java.util.ArrayList<org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter>();
              mutable_bitField0_ |= 0x00000004;
            }
            typeParameter_.add(input.readMessage(org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter.PARSER, extensionRegistry));
            break;
          }
        }
      }
    } catch (org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException(
          e.getMessage()).setUnfinishedMessage(this);
    } finally {
      if (((mutable_bitField0_ & 0x00000004) == 0x00000004)) {
        typeParameter_ = java.util.Collections.unmodifiableList(typeParameter_);
      }
      try {
        unknownFieldsCodedOutput.flush();
      } catch (java.io.IOException e) {
      // Should not happen
      } finally {
        unknownFields = unknownFieldsOutput.toByteString();
      }
      makeExtensionsImmutable();
    }
  }
  public static org.jetbrains.kotlin.protobuf.Parser<IrTypeAlias> PARSER =
      new org.jetbrains.kotlin.protobuf.AbstractParser<IrTypeAlias>() {
    public IrTypeAlias parsePartialFrom(
        org.jetbrains.kotlin.protobuf.CodedInputStream input,
        org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
        throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
      return new IrTypeAlias(input, extensionRegistry);
    }
  };

  @java.lang.Override
  public org.jetbrains.kotlin.protobuf.Parser<IrTypeAlias> getParserForType() {
    return PARSER;
  }

  private int bitField0_;
  public static final int BASE_FIELD_NUMBER = 1;
  private org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase base_;
  /**
   * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase base = 1;</code>
   */
  public boolean hasBase() {
    return ((bitField0_ & 0x00000001) == 0x00000001);
  }
  /**
   * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase base = 1;</code>
   */
  public org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase getBase() {
    return base_;
  }

  public static final int NAME_TYPE_FIELD_NUMBER = 2;
  private long nameType_;
  /**
   * <code>required int64 name_type = 2;</code>
   */
  public boolean hasNameType() {
    return ((bitField0_ & 0x00000002) == 0x00000002);
  }
  /**
   * <code>required int64 name_type = 2;</code>
   */
  public long getNameType() {
    return nameType_;
  }

  public static final int TYPE_PARAMETER_FIELD_NUMBER = 3;
  private java.util.List<org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter> typeParameter_;
  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
   */
  public java.util.List<org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter> getTypeParameterList() {
    return typeParameter_;
  }
  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
   */
  public java.util.List<? extends org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameterOrBuilder> 
      getTypeParameterOrBuilderList() {
    return typeParameter_;
  }
  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
   */
  public int getTypeParameterCount() {
    return typeParameter_.size();
  }
  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
   */
  public org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter getTypeParameter(int index) {
    return typeParameter_.get(index);
  }
  /**
   * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
   */
  public org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameterOrBuilder getTypeParameterOrBuilder(
      int index) {
    return typeParameter_.get(index);
  }

  private void initFields() {
    base_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase.getDefaultInstance();
    nameType_ = 0L;
    typeParameter_ = java.util.Collections.emptyList();
  }
  private byte memoizedIsInitialized = -1;
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    if (!hasBase()) {
      memoizedIsInitialized = 0;
      return false;
    }
    if (!hasNameType()) {
      memoizedIsInitialized = 0;
      return false;
    }
    if (!getBase().isInitialized()) {
      memoizedIsInitialized = 0;
      return false;
    }
    for (int i = 0; i < getTypeParameterCount(); i++) {
      if (!getTypeParameter(i).isInitialized()) {
        memoizedIsInitialized = 0;
        return false;
      }
    }
    memoizedIsInitialized = 1;
    return true;
  }

  public void writeTo(org.jetbrains.kotlin.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    getSerializedSize();
    if (((bitField0_ & 0x00000001) == 0x00000001)) {
      output.writeMessage(1, base_);
    }
    if (((bitField0_ & 0x00000002) == 0x00000002)) {
      output.writeInt64(2, nameType_);
    }
    for (int i = 0; i < typeParameter_.size(); i++) {
      output.writeMessage(3, typeParameter_.get(i));
    }
    output.writeRawBytes(unknownFields);
  }

  private int memoizedSerializedSize = -1;
  public int getSerializedSize() {
    int size = memoizedSerializedSize;
    if (size != -1) return size;

    size = 0;
    if (((bitField0_ & 0x00000001) == 0x00000001)) {
      size += org.jetbrains.kotlin.protobuf.CodedOutputStream
        .computeMessageSize(1, base_);
    }
    if (((bitField0_ & 0x00000002) == 0x00000002)) {
      size += org.jetbrains.kotlin.protobuf.CodedOutputStream
        .computeInt64Size(2, nameType_);
    }
    for (int i = 0; i < typeParameter_.size(); i++) {
      size += org.jetbrains.kotlin.protobuf.CodedOutputStream
        .computeMessageSize(3, typeParameter_.get(i));
    }
    size += unknownFields.size();
    memoizedSerializedSize = size;
    return size;
  }

  private static final long serialVersionUID = 0L;
  @java.lang.Override
  protected java.lang.Object writeReplace()
      throws java.io.ObjectStreamException {
    return super.writeReplace();
  }

  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias parseFrom(
      org.jetbrains.kotlin.protobuf.ByteString data)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias parseFrom(
      org.jetbrains.kotlin.protobuf.ByteString data,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias parseFrom(byte[] data)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias parseFrom(
      byte[] data,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return PARSER.parseFrom(input);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias parseFrom(
      java.io.InputStream input,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseFrom(input, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return PARSER.parseDelimitedFrom(input);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias parseDelimitedFrom(
      java.io.InputStream input,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseDelimitedFrom(input, extensionRegistry);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias parseFrom(
      org.jetbrains.kotlin.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return PARSER.parseFrom(input);
  }
  public static org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias parseFrom(
      org.jetbrains.kotlin.protobuf.CodedInputStream input,
      org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return PARSER.parseFrom(input, extensionRegistry);
  }

  public static Builder newBuilder() { return Builder.create(); }
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder(org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias prototype) {
    return newBuilder().mergeFrom(prototype);
  }
  public Builder toBuilder() { return newBuilder(this); }

  /**
   * Protobuf type {@code org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias}
   */
  public static final class Builder extends
      org.jetbrains.kotlin.protobuf.GeneratedMessageLite.Builder<
        org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias, Builder>
      implements
      // @@protoc_insertion_point(builder_implements:org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias)
      org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAliasOrBuilder {
    // Construct using org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private void maybeForceBuilderInitialization() {
    }
    private static Builder create() {
      return new Builder();
    }

    public Builder clear() {
      super.clear();
      base_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase.getDefaultInstance();
      bitField0_ = (bitField0_ & ~0x00000001);
      nameType_ = 0L;
      bitField0_ = (bitField0_ & ~0x00000002);
      typeParameter_ = java.util.Collections.emptyList();
      bitField0_ = (bitField0_ & ~0x00000004);
      return this;
    }

    public Builder clone() {
      return create().mergeFrom(buildPartial());
    }

    public org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias getDefaultInstanceForType() {
      return org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias.getDefaultInstance();
    }

    public org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias build() {
      org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    public org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias buildPartial() {
      org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias result = new org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias(this);
      int from_bitField0_ = bitField0_;
      int to_bitField0_ = 0;
      if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
        to_bitField0_ |= 0x00000001;
      }
      result.base_ = base_;
      if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
        to_bitField0_ |= 0x00000002;
      }
      result.nameType_ = nameType_;
      if (((bitField0_ & 0x00000004) == 0x00000004)) {
        typeParameter_ = java.util.Collections.unmodifiableList(typeParameter_);
        bitField0_ = (bitField0_ & ~0x00000004);
      }
      result.typeParameter_ = typeParameter_;
      result.bitField0_ = to_bitField0_;
      return result;
    }

    public Builder mergeFrom(org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias other) {
      if (other == org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias.getDefaultInstance()) return this;
      if (other.hasBase()) {
        mergeBase(other.getBase());
      }
      if (other.hasNameType()) {
        setNameType(other.getNameType());
      }
      if (!other.typeParameter_.isEmpty()) {
        if (typeParameter_.isEmpty()) {
          typeParameter_ = other.typeParameter_;
          bitField0_ = (bitField0_ & ~0x00000004);
        } else {
          ensureTypeParameterIsMutable();
          typeParameter_.addAll(other.typeParameter_);
        }
        
      }
      setUnknownFields(
          getUnknownFields().concat(other.unknownFields));
      return this;
    }

    public final boolean isInitialized() {
      if (!hasBase()) {
        
        return false;
      }
      if (!hasNameType()) {
        
        return false;
      }
      if (!getBase().isInitialized()) {
        
        return false;
      }
      for (int i = 0; i < getTypeParameterCount(); i++) {
        if (!getTypeParameter(i).isInitialized()) {
          
          return false;
        }
      }
      return true;
    }

    public Builder mergeFrom(
        org.jetbrains.kotlin.protobuf.CodedInputStream input,
        org.jetbrains.kotlin.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (org.jetbrains.kotlin.protobuf.InkonstidProtocolBufferException e) {
        parsedMessage = (org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias) e.getUnfinishedMessage();
        throw e;
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }
    private int bitField0_;

    private org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase base_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase.getDefaultInstance();
    /**
     * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase base = 1;</code>
     */
    public boolean hasBase() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    /**
     * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase base = 1;</code>
     */
    public org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase getBase() {
      return base_;
    }
    /**
     * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase base = 1;</code>
     */
    public Builder setBase(org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase konstue) {
      if (konstue == null) {
        throw new NullPointerException();
      }
      base_ = konstue;

      bitField0_ |= 0x00000001;
      return this;
    }
    /**
     * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase base = 1;</code>
     */
    public Builder setBase(
        org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase.Builder builderForValue) {
      base_ = builderForValue.build();

      bitField0_ |= 0x00000001;
      return this;
    }
    /**
     * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase base = 1;</code>
     */
    public Builder mergeBase(org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase konstue) {
      if (((bitField0_ & 0x00000001) == 0x00000001) &&
          base_ != org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase.getDefaultInstance()) {
        base_ =
          org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase.newBuilder(base_).mergeFrom(konstue).buildPartial();
      } else {
        base_ = konstue;
      }

      bitField0_ |= 0x00000001;
      return this;
    }
    /**
     * <code>required .org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase base = 1;</code>
     */
    public Builder clearBase() {
      base_ = org.jetbrains.kotlin.backend.common.serialization.proto.IrDeclarationBase.getDefaultInstance();

      bitField0_ = (bitField0_ & ~0x00000001);
      return this;
    }

    private long nameType_ ;
    /**
     * <code>required int64 name_type = 2;</code>
     */
    public boolean hasNameType() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    /**
     * <code>required int64 name_type = 2;</code>
     */
    public long getNameType() {
      return nameType_;
    }
    /**
     * <code>required int64 name_type = 2;</code>
     */
    public Builder setNameType(long konstue) {
      bitField0_ |= 0x00000002;
      nameType_ = konstue;
      
      return this;
    }
    /**
     * <code>required int64 name_type = 2;</code>
     */
    public Builder clearNameType() {
      bitField0_ = (bitField0_ & ~0x00000002);
      nameType_ = 0L;
      
      return this;
    }

    private java.util.List<org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter> typeParameter_ =
      java.util.Collections.emptyList();
    private void ensureTypeParameterIsMutable() {
      if (!((bitField0_ & 0x00000004) == 0x00000004)) {
        typeParameter_ = new java.util.ArrayList<org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter>(typeParameter_);
        bitField0_ |= 0x00000004;
       }
    }

    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
     */
    public java.util.List<org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter> getTypeParameterList() {
      return java.util.Collections.unmodifiableList(typeParameter_);
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
     */
    public int getTypeParameterCount() {
      return typeParameter_.size();
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
     */
    public org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter getTypeParameter(int index) {
      return typeParameter_.get(index);
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
     */
    public Builder setTypeParameter(
        int index, org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter konstue) {
      if (konstue == null) {
        throw new NullPointerException();
      }
      ensureTypeParameterIsMutable();
      typeParameter_.set(index, konstue);

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
     */
    public Builder setTypeParameter(
        int index, org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter.Builder builderForValue) {
      ensureTypeParameterIsMutable();
      typeParameter_.set(index, builderForValue.build());

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
     */
    public Builder addTypeParameter(org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter konstue) {
      if (konstue == null) {
        throw new NullPointerException();
      }
      ensureTypeParameterIsMutable();
      typeParameter_.add(konstue);

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
     */
    public Builder addTypeParameter(
        int index, org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter konstue) {
      if (konstue == null) {
        throw new NullPointerException();
      }
      ensureTypeParameterIsMutable();
      typeParameter_.add(index, konstue);

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
     */
    public Builder addTypeParameter(
        org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter.Builder builderForValue) {
      ensureTypeParameterIsMutable();
      typeParameter_.add(builderForValue.build());

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
     */
    public Builder addTypeParameter(
        int index, org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter.Builder builderForValue) {
      ensureTypeParameterIsMutable();
      typeParameter_.add(index, builderForValue.build());

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
     */
    public Builder addAllTypeParameter(
        java.lang.Iterable<? extends org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter> konstues) {
      ensureTypeParameterIsMutable();
      org.jetbrains.kotlin.protobuf.AbstractMessageLite.Builder.addAll(
          konstues, typeParameter_);

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
     */
    public Builder clearTypeParameter() {
      typeParameter_ = java.util.Collections.emptyList();
      bitField0_ = (bitField0_ & ~0x00000004);

      return this;
    }
    /**
     * <code>repeated .org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeParameter type_parameter = 3;</code>
     */
    public Builder removeTypeParameter(int index) {
      ensureTypeParameterIsMutable();
      typeParameter_.remove(index);

      return this;
    }

    // @@protoc_insertion_point(builder_scope:org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias)
  }

  static {
    defaultInstance = new IrTypeAlias(true);
    defaultInstance.initFields();
  }

  // @@protoc_insertion_point(class_scope:org.jetbrains.kotlin.backend.common.serialization.proto.IrTypeAlias)
}
