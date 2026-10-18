package com.hitech.lms.domain.file;

import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

// 첨부파일 내려받기 - 로그인한 사람만
//   화면에서는 <a th:href="@{|/file/download/${f.fileDetailId}|}"> 로 사용
@Controller
@RequiredArgsConstructor
@RequestMapping("/file")
public class FileController {

	private final FileService fs;

	@PreAuthorize("isAuthenticated()")
	@GetMapping("/download/{id}")
	public ResponseEntity<Resource> download(@PathVariable("id") Long id) {
		FileDetail detail = fs.getFile(id);
		Resource resource = fs.load(detail);

		// 한글 파일 이름이 깨지지 않게 UTF-8 로
		ContentDisposition cd = ContentDisposition.attachment()
				.filename(detail.getFileDetailRealNm(), StandardCharsets.UTF_8)
				.build();

		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.header(HttpHeaders.CONTENT_DISPOSITION, cd.toString())
				.contentLength(detail.getFileDetailSize())
				.body(resource);
	}
}
