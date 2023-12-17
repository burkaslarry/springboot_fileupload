package com.uploadexcel.flutter.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Objects;

@SpringBootApplication
public class DemoApplication {
	private static final String UPLOAD_DIR = "uploads";

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public String uploadDir() {
		String uploadDir = System.getProperty("user.dir") + File.separator + UPLOAD_DIR;
		File dir = new File(uploadDir);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		return uploadDir;
	}

	// File upload endpoint
	// Accepts multipart file upload
	// Returns the uploaded file name with timestamp in the response
	// Example: POST /upload
	// Request body: Multipart file
	// Response body: Uploaded file name with timestamp
	// Content-Type: text/plain
	// Example response: myImage_1639690172.jpg
	@PostMapping("/upload")
	public String uploadFile(@RequestParam("file") MultipartFile file,
							 @RequestParam(value = "replace", defaultValue = "false") boolean replaceExisting,
							 @RequestParam(value = "return", defaultValue = "false") boolean returnValue
							 ) throws IOException {
		String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
		String newFileName = fileName;

		if (!replaceExisting) {
			int dotIndex = fileName.lastIndexOf(".");
			if (dotIndex >= 0) {
				String nameWithoutExtension = fileName.substring(0, dotIndex);
				String extension = fileName.substring(dotIndex);
				newFileName = nameWithoutExtension + "_" + Instant.now().getEpochSecond() + extension;
			} else {
				newFileName = fileName + "_" + Instant.now().getEpochSecond();
			}
		}

		Path uploadPath = Path.of(uploadDir());
		Path filePath = uploadPath.resolve(newFileName);
		Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

		if (returnValue) {
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(newFileName).toString();
		} else {
			return ResponseEntity.ok().build().toString();
		}
	}
}
