
package com.ellectronic.voting.storage.provider.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import com.ellectronic.voting.common.exception.BusinessRuleException;
import com.ellectronic.voting.storage.dto.StoredFile;
import com.ellectronic.voting.storage.service.FileStorageService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryStorageService implements FileStorageService {

    private static final Logger log =
            LoggerFactory.getLogger(CloudinaryStorageService.class);

    private final Cloudinary cloudinary;

    public CloudinaryStorageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public StoredFile upload(MultipartFile file, String folder) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folder,
                            "resource_type", "image",
                            "type", "authenticated"
                    )
            );

            return new StoredFile(
                    "CLOUDINARY",
                    (String) result.get("public_id"),
                    file.getContentType(),
                    file.getSize()
            );

        } catch (IOException | RuntimeException exception) {

            log.error(
                    "Cloudinary upload failed. Folder: {}, File size: {}",
                    folder,
                    file.getSize(),
                    exception
            );

            throw new BusinessRuleException(
                    "Unable to upload verification document."
            );
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Map<?, ?> result = cloudinary.uploader().destroy(
                    storageKey,
                    ObjectUtils.asMap(
                            "resource_type", "image",
                            "type", "authenticated",
                            "invalidate", true
                    )
            );

            Object resultStatus = result.get("result");

            if (!"ok".equals(resultStatus)
                    && !"not found".equals(resultStatus)) {
                throw new IllegalStateException(
                        "Cloudinary could not delete uploaded file."
                );
            }

        } catch (IOException | RuntimeException exception) {

            log.error(
                    "Cloudinary deletion failed for storage key: {}",
                    storageKey,
                    exception
            );

            throw new IllegalStateException(
                    "Unable to delete the stored verification document.",
                    exception
            );
        }
    }
}
