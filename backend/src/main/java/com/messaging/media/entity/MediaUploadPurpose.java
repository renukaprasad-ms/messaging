package com.messaging.media.entity;

public enum MediaUploadPurpose {
  PROFILE_PHOTO("profiles", true),
  COMPANY_LOGO("companies", true),
  MESSAGE_MEDIA("messages", false),
  TEMPLATE_MEDIA("templates", false);

  private final String folder;
  private final boolean imageOnly;

  MediaUploadPurpose(String folder, boolean imageOnly) {
    this.folder = folder;
    this.imageOnly = imageOnly;
  }

  public String folder() {
    return folder;
  }

  public boolean imageOnly() {
    return imageOnly;
  }
}
