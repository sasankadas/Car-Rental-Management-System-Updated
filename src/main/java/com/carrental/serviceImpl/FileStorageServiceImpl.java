package com.carrental.serviceImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.carrental.service.FileStorageService;

@Service
public class FileStorageServiceImpl implements FileStorageService {

	@Value("${file.upload-dir:uploads}")
	private String uploadDir;

	@Override
	public String store(MultipartFile file, String subDir) {
		if (file == null || file.isEmpty()) {
			return null;
		}
		try {
			Path dir = Paths.get(uploadDir, subDir == null ? "" : subDir).toAbsolutePath().normalize();
			Files.createDirectories(dir);

			String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
			String extension = "";
			int dot = original.lastIndexOf('.');
			if (dot >= 0) {
				extension = original.substring(dot);
			}
			String filename = UUID.randomUUID().toString().replace("-", "") + extension;

			Path target = dir.resolve(filename);
			Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

			return filename;
		} catch (IOException e) {
			throw new RuntimeException("Failed to store uploaded file: " + e.getMessage(), e);
		}
	}
}
