package com.ellectronic.voting.storage.service;

import com.ellectronic.voting.storage.dto.StoredFile;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

  StoredFile upload(MultipartFile file, String folder);
  void delete(String storageKey);
  
}
