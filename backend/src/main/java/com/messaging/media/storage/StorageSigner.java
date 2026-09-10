package com.messaging.media.storage;

public interface StorageSigner {

  SignedUpload signUpload(String path, String contentType);
}
