package com.carrental.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

	/**
	 * Stores the given file inside uploads/{subDir}/ (subDir may be "" to store directly
	 * under uploads/) and returns the generated, unique filename (no path) which can be
	 * persisted on the entity and later resolved through the /images/** resource mapping.
	 */
	String store(MultipartFile file, String subDir);
}
