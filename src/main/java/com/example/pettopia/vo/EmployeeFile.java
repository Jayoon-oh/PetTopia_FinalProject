package com.example.pettopia.vo;

import java.io.Serializable;

import lombok.Data;

@Data
public class EmployeeFile implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer empFileNo; // PK
	private String empNo; // FK
	private String filePurpose;
	private String originFileName;
	private String fileName;
	private String fileExt;
	private String fileType;
	private String createDatetime;
	private String updateDatetime;
}
