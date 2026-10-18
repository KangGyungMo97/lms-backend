package com.hitech.lms.domain.file;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hitech.lms.global.error.DataNotFoundException;

import lombok.RequiredArgsConstructor;

// 첨부파일 공통 (과제 · 제출 · 나중에 공지/게시판에서도 같이 씀)
//   FileAtt   = 첨부파일 묶음 1개 (게시글 · 과제 · 제출물에 file_att_id 로 연결)
//   FileDetail = 묶음 안의 파일 1개 (원본 이름 · 저장 이름 · 크기 ...)
//   실제 파일은 lms.upload-dir/yyyy/MM/UUID.확장자 로 저장 (같은 이름이 와도 안 겹치게)
@Service
@RequiredArgsConstructor
public class FileService {

	// 올릴 수 있는 확장자 (실행 파일 등은 막음)
	public static final Set<String> ALLOWED = Set.of(
			"pdf", "hwp", "hwpx", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt", "md", "csv",
			"zip", "jpg", "jpeg", "png", "gif", "java", "sql", "html", "css", "js", "py");

	public static final int MAX_FILES = 5;    // 한 번에 올릴 수 있는 개수

	private final FileAttRepository far;

	private final FileDetailRepository fdr;

	@Value("${lms.upload-dir:${user.home}/lms-upload}")
	private String uploadDir;

	// 업로드 - 파일 저장 + FileAtt 1개, FileDetail 여러 개 저장 → FileAtt 반환 (과제 · 제출물에 연결)
	//   파일을 하나도 안 골랐으면 null (첨부 없음)
	//   확장자 · 개수가 맞지 않으면 IllegalArgumentException (컨트롤러에서 화면 오류로 보여 줌)
	@Transactional
	public FileAtt upload(List<MultipartFile> files) {
		List<MultipartFile> list = new ArrayList<>();
		if (files != null) {
			for (MultipartFile f : files) {
				if (f != null && !f.isEmpty()) list.add(f);
			}
		}
		if (list.isEmpty()) return null;

		check(list);

		FileAtt fileAtt = new FileAtt();
		fileAtt.setFileAttCreateDate(LocalDateTime.now());
		fileAtt.setFileAttIsUse("Y");
		far.save(fileAtt);

		String route = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
		Path dir = Paths.get(uploadDir, route).toAbsolutePath().normalize();

		for (MultipartFile f : list) {
			String realNm = originalName(f);
			String ext = extension(realNm);
			String saveNm = UUID.randomUUID() + "." + ext;
			try {
				Files.createDirectories(dir);
				f.transferTo(dir.resolve(saveNm));
			} catch (IOException e) {
				throw new IllegalStateException("파일을 저장하지 못했습니다: " + realNm, e);
			}

			FileDetail detail = new FileDetail();
			detail.setFileAtt(fileAtt);
			detail.setFileDetailRoute(route);
			detail.setFileDetailSaveNm(saveNm);
			detail.setFileDetailRealNm(realNm);
			detail.setFileDetailExtension(ext);
			detail.setFileDetailSize(f.getSize());
			fdr.save(detail);
		}
		return fileAtt;
	}

	// 올리기 전에 확인 - 개수 · 확장자
	public void check(List<MultipartFile> list) {
		if (list.size() > MAX_FILES) {
			throw new IllegalArgumentException("첨부파일은 " + MAX_FILES + "개까지 올릴 수 있습니다.");
		}
		for (MultipartFile f : list) {
			String ext = extension(originalName(f));
			if (!ALLOWED.contains(ext)) {
				throw new IllegalArgumentException("올릴 수 없는 파일 형식입니다: " + originalName(f));
			}
		}
	}

	// 파일 1개 정보 - findById, 없으면 DataNotFoundException
	public FileDetail getFile(Long fileId) {
		return fdr.findById(fileId)
				.orElseThrow(() -> new DataNotFoundException("파일을 찾을 수 없습니다."));
	}

	// 묶음에 들어있는 파일 목록 (묶음이 없으면 빈 목록)
	public List<FileDetail> getFileList(FileAtt fileAtt) {
		if (fileAtt == null || !"Y".equals(fileAtt.getFileAttIsUse())) return List.of();
		return fdr.findByFileAttOrderByFileDetailIdAsc(fileAtt);
	}

	// 내려받기용 - 디스크의 실제 파일
	public Resource load(FileDetail detail) {
		Path base = Paths.get(uploadDir).toAbsolutePath().normalize();
		Path path = base.resolve(detail.getFileDetailRoute()).resolve(detail.getFileDetailSaveNm()).normalize();
		if (!path.startsWith(base)) {                       // 저장 폴더 밖은 못 읽게
			throw new DataNotFoundException("파일을 찾을 수 없습니다.");
		}
		try {
			Resource resource = new UrlResource(path.toUri());
			if (!resource.exists()) throw new DataNotFoundException("파일이 서버에 없습니다.");
			return resource;
		} catch (MalformedURLException e) {
			throw new DataNotFoundException("파일을 찾을 수 없습니다.");
		}
	}

	// 원본 파일 이름 (경로가 붙어 오는 브라우저 대비해서 이름만)
	private String originalName(MultipartFile f) {
		String name = f.getOriginalFilename();
		if (name == null || name.isBlank()) name = "file";
		name = name.replace('\\', '/');
		name = name.substring(name.lastIndexOf('/') + 1);
		return name.length() > 200 ? name.substring(name.length() - 200) : name;
	}

	// 확장자 (소문자, 없으면 빈 글자)
	private String extension(String name) {
		int dot = name.lastIndexOf('.');
		if (dot < 0 || dot == name.length() - 1) return "";
		String ext = name.substring(dot + 1).toLowerCase();
		return ext.length() > 20 ? ext.substring(0, 20) : ext;
	}
}
