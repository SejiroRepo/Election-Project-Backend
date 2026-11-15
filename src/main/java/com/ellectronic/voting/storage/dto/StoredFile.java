package com.ellectronic.voting.storage.dto;

public record StoredFile(
  String provider,
  String storageKey,
  String contentType,
  long size
){}
