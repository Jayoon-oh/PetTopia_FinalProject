package com.example.pettopia.schedule;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.example.pettopia.vo.Schedule;

@Mapper
public interface ScheduleMapper {

	// scheduleCalendar → 캘린더 일정 조회하기
	List<Map<String , Object>> selectScheduleList(Map<String, Object> paramMap);
	
	// scheduleCalendar → 캘린더 일정 추가하기
	Integer insertSchedule(Schedule schedule);
		
	// scheduleCalendar → 캘린더 일정 삭제하기
	Integer deleteSchedule(Integer scheduleNo);
	
	// scheduleCalendar → 캘린더 일정 수정하기
	Integer updateSchedule(Schedule schedule);
	
	
	
}
