package com.hitech.lms.domain.file;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class FileService {

	private final FileAttRepository far;

	private final FileDetailRepository fdr;

	// 업로드 - 파일 저장 + FileAtt 1개, FileDetail 여러 개 저장 -> FileAtt 반환 (게시글에 연결)
	public FileAtt upload(List<MultipartFile> files) {
		return null;
	}

	// 파일 1개 정보 - findById, 없으면 DataNotFoundException
	public FileDetail getFile(Long fileId) {
		return null;
	}

	// 묶음에 들어있는 파일 목록
	public List<FileDetail> getFileList(FileAtt fileAtt) {
		return null;
	}

}
