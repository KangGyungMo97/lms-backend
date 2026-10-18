package com.hitech.lms.domain.file;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FileDetailRepository extends JpaRepository<FileDetail, Long> {

	// 묶음(FileAtt)에 들어 있는 파일들 - 올린 순서대로
	List<FileDetail> findByFileAttOrderByFileDetailIdAsc(FileAtt fileAtt);
}
