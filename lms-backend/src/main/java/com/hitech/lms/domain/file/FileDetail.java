package com.hitech.lms.domain.file;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class FileDetail {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long fileDetailId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "file_att_id", nullable = false)
	private FileAtt fileAtt;

	@Column(length = 1000, nullable = false)
	private String fileDetailRoute;     // 저장 경로

	@Column(length = 100, nullable = false)
	private String fileDetailSaveNm;    // 서버에 저장된 파일명

	@Column(length = 1000, nullable = false)
	private String fileDetailRealNm;    // 원본 파일명

	@Column(length = 20, nullable = false)
	private String fileDetailExtension;

	@Column(length = 1000)
	private String fileDetailText;

	@Column(nullable = false)
	private Long fileDetailSize;        // bytes
}
