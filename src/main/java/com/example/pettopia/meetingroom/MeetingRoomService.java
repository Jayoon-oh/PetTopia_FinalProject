package com.example.pettopia.meetingroom;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.pettopia.meetingroomimg.MeetingRoomImgMapper;
import com.example.pettopia.meetingroomrsv.MeetingRoomRsvMapper;
import com.example.pettopia.util.Page;
import com.example.pettopia.util.TeamColor;
import com.example.pettopia.vo.MeetingRoom;
import com.example.pettopia.vo.MeetingRoomForm;
import com.example.pettopia.vo.MeetingRoomImg;
import com.example.pettopia.vo.MeetingRoomRsv;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class MeetingRoomService {
	
	@Autowired MeetingRoomMapper meetingRoomMapper;
	@Autowired MeetingRoomImgMapper meetingRoomImgMapper;
	@Autowired MeetingRoomRsvMapper meetingRoomRsvMapper;
	
	
	
	// 회의실 등록
	public Integer addMeetingRoom(MeetingRoomForm meetingRoomForm, String path) {
		
		log.debug(TeamColor.KMJ+"[MeetingRoomService - addMeetingroom()]");
		
		log.debug(TeamColor.KMJ+"meetingRoomForm : " + meetingRoomForm.toString());
		
		
		// 회의실 정보 등록 -> roomNo 반환
		MeetingRoom meetingRoom = new MeetingRoom();
		
		meetingRoom.setRoomName(meetingRoomForm.getRoomName());
		meetingRoom.setRoomInfo(meetingRoomForm.getRoomInfo());
		meetingRoom.setRoomCapacity(meetingRoomForm.getRoomCapacity());
		meetingRoom.setRoomLocation(meetingRoomForm.getRoomLocation());
		
		meetingRoomMapper.insertMeetingRoom(meetingRoom);
		
		Integer roomNo = meetingRoom.getRoomNo();
		
		if(roomNo != null) {// 회의실 정보 등록 성공
			
			// 회의실 이미지 등록
			MultipartFile roomImg = meetingRoomForm.getRoomImg();
			
			int dotIndex = roomImg.getOriginalFilename().lastIndexOf(".");					// 확장자와 파일명 구분을 위한 . 인덱스 찾기
			String orginFileName = roomImg.getOriginalFilename().substring(0, dotIndex);	// 원본파일명
			String fileName = UUID.randomUUID().toString().replace("-", "");				// 랜덤으로 생성한 파일명
			String ext = roomImg.getOriginalFilename().substring(dotIndex);					// 확장자 (.jpg)
			String fileType = roomImg.getContentType();										// 파일 타입

			log.debug(TeamColor.KMJ + "dotIndex :" + dotIndex);
			log.debug(TeamColor.KMJ + "orginFileName :" + orginFileName);
			log.debug(TeamColor.KMJ + "fileName :" + fileName);
			log.debug(TeamColor.KMJ + "ext :" + ext);
			log.debug(TeamColor.KMJ + "fileType :" + fileType);
			
			
			// 파일 유효성 검사 (이미지 파일만 가능)
			// Spring Boot에서 Multipart 파일의 최대 업로드 크기는 기본적으로 1MB로 설정함. -> 수정하려면 application.yml 에서 설정해야함
			List<String> allowedExtensions = Arrays.asList(".jpg", ".png", ".jpeg");
			if (!allowedExtensions.contains(ext.toLowerCase())) {
			    throw new IllegalArgumentException("이미지 파일만 가능");
			}

			
			MeetingRoomImg meetingRoomImg = new MeetingRoomImg();
			
			meetingRoomImg.setRoomNo(roomNo);
			meetingRoomImg.setFileName(fileName);
			meetingRoomImg.setOriginFileName(orginFileName);
			meetingRoomImg.setFileType(fileType);
			meetingRoomImg.setFileExt(ext);

			
			// db insert
			Integer fileResultRow = meetingRoomImgMapper.insertMeetingRoomImg(meetingRoomImg);
			log.debug(TeamColor.KMJ + "fileResultRow :" + fileResultRow);
			
			if(fileResultRow == 1) {
				// 물리적 파일 저장하기
				try {
					roomImg.transferTo(new File(path + fileName + ext)); // /meetingRoomFile/manggom.jpg
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException();
				}
			}

		}

		return roomNo;
	}
	
	
	// 회의실 목록
	public List<Map<String, Object>> getMeetingRoomList(){
		log.debug(TeamColor.KMJ+"[MeetingRoomService - getMeetingRoomList()]");
		
		List<Map<String, Object>> meetingRoomList = meetingRoomMapper.selectMeetingRoomList();
		
		log.debug(TeamColor.KMJ+"meetingRoomList : " + meetingRoomList.toString());
		
		return meetingRoomList;
		
	}
	
	// 회의실 상세보기
	public Map<String, Object> getMeetingRoomOne(String roomNo){
		log.debug(TeamColor.KMJ+"[MeetingRoomService - getMeetingRoomOne()]");
		
		Map<String, Object> roomInfo = meetingRoomMapper.selectMeetingRoomOne(roomNo);
		
		log.debug(TeamColor.KMJ+"roomInfo : " + roomInfo.toString());
		
		return roomInfo;
	}
	
	// 회의실 정보 수정
	public boolean modifyMeetingRoomInfo(MeetingRoom meetingRoom) {
		log.debug(TeamColor.KMJ+"[MeetingRoomService - modifyMeetingRoomInfo()]");
		
		boolean result = false; // 정보 수정 성공시 true

		Integer row = meetingRoomMapper.updateMeetingRoomInfo(meetingRoom);
		
		log.debug(TeamColor.KMJ+"row : " + row);
		
		if(row != 0 ) {
			result = true;
		}
		
		log.debug(TeamColor.KMJ+"result : " + result);
		return result;
	}
	
	// 회의실 이미지 수정
	public boolean modifyMeetingRoomImg(String path, MultipartFile roomImg, String roomImgNo, Integer roomNo) {
		log.debug(TeamColor.KMJ+"[MeetingRoomService - modifyMeetingRoomImg()]");
		
		boolean result = false; // 정보 수정 성공시 true

		// MultipartFile roomImg
		int dotIndex = roomImg.getOriginalFilename().lastIndexOf(".");					// 확장자와 파일명 구분을 위한 . 인덱스 찾기
		String orginFileName = roomImg.getOriginalFilename().substring(0, dotIndex);	// 원본파일명
		String fileName = UUID.randomUUID().toString().replace("-", "");				// 랜덤으로 생성한 파일명
		String ext = roomImg.getOriginalFilename().substring(dotIndex);					// 확장자 (.jpg)
		String fileType = roomImg.getContentType();										// 파일 타입

		log.debug(TeamColor.KMJ + "dotIndex :" + dotIndex);
		log.debug(TeamColor.KMJ + "orginFileName :" + orginFileName);
		log.debug(TeamColor.KMJ + "fileName :" + fileName);
		log.debug(TeamColor.KMJ + "ext :" + ext);
		log.debug(TeamColor.KMJ + "fileType :" + fileType);
		
		
		// 파일 유효성 검사 (이미지 파일만 가능)
		// Spring Boot에서 Multipart 파일의 최대 업로드 크기는 기본적으로 1MB로 설정함. -> 수정하려면 application.yml 에서 설정해야함
		List<String> allowedExtensions = Arrays.asList(".jpg", ".png", ".jpeg");
		if (!allowedExtensions.contains(ext.toLowerCase())) {
		    throw new IllegalArgumentException("이미지 파일만 가능");
		}
		
		MeetingRoomImg meetingRoomImg = new MeetingRoomImg();
		
		meetingRoomImg.setRoomImgNo(Integer.parseInt(roomImgNo));
		meetingRoomImg.setRoomNo(roomNo);
		meetingRoomImg.setFileName(fileName);
		meetingRoomImg.setOriginFileName(orginFileName);
		meetingRoomImg.setFileType(fileType);
		meetingRoomImg.setFileExt(ext);

		
		// db 수정 전 물리적으로 삭제할 파일 이름 가져오기
		Integer roomImgNum = Integer.parseInt(roomImgNo);
		
		String deleteFileName =  meetingRoomImgMapper.selectMeetingRoomImgOne(roomImgNum);
		log.debug(TeamColor.KMJ+"deleteFileName : " + deleteFileName);
		
		
		// db 수정
		Integer row = meetingRoomImgMapper.updateMeetingRoomImg(meetingRoomImg);
		log.debug(TeamColor.KMJ+"row : " + row);
		
		if(row == 1) {
			
			// 새로운 물리적 파일 저장하기
			try {
				roomImg.transferTo(new File(path + fileName + ext)); // /meetingRoomFile/manggom5(UUID로 변환).jpg
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException();
			}
			
			
			File f = new File(path, deleteFileName); 	// 해당 path에 deleteFileName의 파일 가져옴
			if(f.exists()) {						 	// 파일이 존재한다면
				boolean deleteFileResult = f.delete();	// 파일 삭제
				
				if(deleteFileResult == false) {			// 파일 삭제에 실패한다면  예외 발생시키기
					throw new RuntimeException("파일 삭제에 실패했습니다: " + deleteFileName); // 물리적 파일 삭제는 @transactional이 관리하지 않으므로 실패시 RuntimeException을 만들어줌	
				
				}

			}

		}
		
		result = true;
		
		log.debug(TeamColor.KMJ+"result : " + result);
		return result;
	}
	
	
	// 회의실 삭제
	public boolean removeMeetingRoom(String roomNo, String path) {
		log.debug(TeamColor.KMJ+"[MeetingRoomService - removeMeetingRoom()]");
		boolean result = false;
		
		// 물리적 파일 삭제를 위해 이미지 이름 조회
		Integer roomNum = Integer.parseInt(roomNo);
		
		String deleteFileName = meetingRoomImgMapper.selectMeetingRoomImgOne(roomNum);
		log.debug(TeamColor.KMJ+"deleteFileName : " + deleteFileName);
		
		
		// 예약 내역 삭제
		Integer rsvRow = meetingRoomRsvMapper.selectMeetingRoomRsvCntByRoomNo(roomNum);
		boolean deleteRsvResult = false;
		
		if(rsvRow != 0) {// 예약 내역이 존재하면 예약 내역 삭제
			Integer row = meetingRoomRsvMapper.deleteRsvByRoomNo(roomNum);
			
			if(row != 0) {
				deleteRsvResult = true;
			}
			
		}else {// 예약 내역이 없을 경우
			deleteRsvResult = true;
		}
		
		// 이미지 삭제
		boolean deleteImgResult = false;
		
		Integer row = meetingRoomImgMapper.deleteMeetingRoomImg(roomNum);
		
		if(row != 0) {
			deleteImgResult = true;
		}
		
		// 회의실 삭제
		Integer dRow = meetingRoomMapper.deleteMeetingRoomInfo(roomNum);
		boolean deleteResult = false;
		
		if(dRow != 0) {
			deleteResult = true;
		}
		
		if(deleteRsvResult && deleteImgResult && deleteResult) { // 모두 삭제에 성공하면 이미지 파일 삭제
			
			File f = new File(path, deleteFileName); 	// 해당 path에 deleteFileName의 파일 가져옴
			if(f.exists()) {						 	// 파일이 존재한다면
				boolean deleteFileResult = f.delete();	// 파일 삭제
				
				if(deleteFileResult == false) {			// 파일 삭제에 실패한다면  예외 발생시키기
					throw new RuntimeException("파일 삭제에 실패했습니다: " + deleteFileName); // 물리적 파일 삭제는 @transactional이 관리하지 않으므로 실패시 RuntimeException을 만들어줌	
				
				}else {
					result = true;
				}

			}
		}
		
		
		log.debug(TeamColor.KMJ+"result : " + result);
		
		return result;
	}
	
	
	// 회의실 예약 가능 시간대
	public List<Map<String, Object>> getReserveTime(String roomNo, String rsvDate){
	    log.debug(TeamColor.KMJ+"[MeetingRoomService - getReserveTime()]");

	    MeetingRoomRsv mrr = new MeetingRoomRsv();
	    mrr.setRoomNo(Integer.parseInt(roomNo));
	    mrr.setRsvDate(rsvDate);

	    // DB에서 예약된 시간대 가져오기 (S1, S2, S3, ...)
	    List<String> reservedTimes = meetingRoomRsvMapper.selectRsvTime(mrr);
	    
	    // 전체 회의 시간대 정의 순서 유지를 위해 LinkedHashMap 사용
	    Map<String, String> allTimes = new LinkedHashMap<>();
	    allTimes.put("S1", "09:00 AM – 11:00 AM");
	    allTimes.put("S2", "11:00 AM – 12:00 PM");
	    allTimes.put("S3", "01:00 PM – 03:00 PM");
	    allTimes.put("S4", "03:00 PM – 05:00 PM");
	    allTimes.put("S5", "05:00 PM – 07:00 PM");

	    // 예약되지 않은 시간대 
	    List<Map<String, Object>> availableTimes = new ArrayList<>();

	    // 예약된 시간대 비교
	    for (Map.Entry<String, String> entry : allTimes.entrySet()) {
	        String timeSession = entry.getKey(); 
	        String timeRange = entry.getValue(); 

	        // 예약된 시간대 목록에 해당 시간이 없다면 예약 가능
	        if (!reservedTimes.contains(timeSession)) {
	            // 예약되지 않은 시간대와 시간 범위를 리스트에 추가
	            Map<String, Object> timeMap = new HashMap<>();
	            timeMap.put("timePeriod", timeSession);
	            timeMap.put("timeRange", timeRange); 
	            availableTimes.add(timeMap);
	        }
	    }

	    // 예약되지 않은 시간대 리스트 정렬 : 시간 순서 정렬
	    availableTimes.sort(Comparator.comparing(map -> map.get("timePeriod").toString()));

	    log.debug(TeamColor.KMJ+"availableTimes : " + availableTimes.toString());
	    
	    return availableTimes;
	}
	
	
	// 회의실 예약 등록
	public boolean addRsvMeetingRoom(MeetingRoomRsv mrr) {
		
		boolean result = false;
		
		Integer row = meetingRoomRsvMapper.insertMeetingRoomRsv(mrr);
		
		if(row != 0) {
			result = true;
		}
		
		return result;
	}
	
	// 회의실 예약 조회
	public Map<String, Object> getMeetingRoomRsvList(Page page){
		
		List<Map<String, Object>> rsvList = new ArrayList<>();
		
		// 페이지네이션
		// 기본 설정
	    page.setRowPerPage(10);
	    Integer beginRow = page.getBeginRow();
	    Integer rowPerPage = page.getRowPerPage();
		
	    // paramMap에 값 추가
	    Map<String, Object> paramMap = new HashMap<>();
	    paramMap.put("beginRow", beginRow);
	    paramMap.put("rowPerPage", rowPerPage);
		
	    // 총 예약 수
	    Integer totalCount = meetingRoomRsvMapper.selectCountMeetingRoomRsvList();
	    log.debug(TeamColor.KMJ+"totalCount : " + totalCount);
	    
	    // 마지막 페이지 계산
	    Integer lastPage = totalCount / rowPerPage;
	    if (totalCount % rowPerPage != 0) {
	        lastPage++;
	    }
	    
	    log.debug(TeamColor.KMJ+"LastPage : " + lastPage);
	    page.setLastPage(lastPage);
	    
		// 예약 List
		List<Map<String, Object>> list = meetingRoomRsvMapper.selectMeetingRoomRsvList(paramMap);
		
		// "S1", "09:00 AM – 11:00 AM""S2", "11:00 AM – 12:00 PM" "S3", "01:00 PM – 03:00 PM" "S4", "03:00 PM – 05:00 PM""S5", "05:00 PM – 07:00 PM"
		
		for(int i=0; i<list.size(); i++) {
			Map<String, Object> map = new HashMap<>();
		
			map.put("roomNo", list.get(i).get("roomNo"));
			map.put("roomName", list.get(i).get("roomName"));
			map.put("empNo", list.get(i).get("empNo"));
			map.put("empName", list.get(i).get("empName"));
			map.put("deptName", list.get(i).get("deptName"));
			map.put("divisionName", list.get(i).get("divisionName"));
			map.put("rsvDate", list.get(i).get("rsvDate"));
			map.put("conferenceTitle", list.get(i).get("conferenceTitle"));
			map.put("conferenceDesc", list.get(i).get("conferenceDesc"));
			map.put("conferenceUsers", list.get(i).get("conferenceUsers"));
			map.put("timePeriod", list.get(i).get("timePeriod"));
			
			switch ((String)list.get(i).get("timePeriod")) {
				case "S1": map.put("timeRange", "09:00 AM – 11:00 AM"); break;
				case "S2": map.put("timeRange", "11:00 AM – 12:00 PM"); break;
				case "S3": map.put("timeRange", "01:00 PM – 03:00 PM"); break;
				case "S4": map.put("timeRange", "03:00 PM – 05:00 PM"); break;
				default : map.put("timeRange", "05:00 PM – 07:00 PM"); break;
			
			}
			
			rsvList.add(map);
		
		}
		
		
		// 최종
	    Map<String, Object> resultMap = new HashMap<>();
	    resultMap.put("rsvList", rsvList);
	    resultMap.put("page", page);
		
		
		
		return resultMap;
	}
	
	
	
	
	
	

}
