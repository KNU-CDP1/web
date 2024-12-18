package com.knu.cdp1.controller;

import com.knu.cdp1.model.Settings;
import com.knu.cdp1.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/scheduleParam")
public class SettingsController {

    @Autowired
    private SettingsRepository settingsRepository;

    // 설정 값을 조회하는 GET 메소드
    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getSettings() {
        return settingsRepository.findById(1L)
                .map(settings -> {
                    // 변환된 값을 담아 반환
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", settings.getId());
                    response.put("delayCost", settings.getDelayCost());
                    response.put("cancelCost", settings.getCancelCost());
                    response.put("riskAlpha", settings.getRiskAlpha() / 1000); // 변환
                    response.put("time", settings.getTime());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }


    // 설정 값을 부분적으로 업데이트하는 PATCH 메소드
    @PatchMapping
    public ResponseEntity<Settings> updateSettings(@RequestBody Map<String, Object> ScheduleParams) {
        Optional<Settings> optionalSettings = settingsRepository.findById(1L);

        if (optionalSettings.isPresent()) {
            Settings settings = optionalSettings.get();

            // Map을 통해 들어온 값들을 settings 객체에 반영
            ScheduleParams.forEach((key, value) -> {
                switch (key) {
                    case "delayCost":
                        settings.setDelayCost((int) value);
                        break;
                    case "cancelCost":
                        settings.setCancelCost((int) value);
                        break;
                    case "riskAlpha":
                        settings.setRiskAlpha((int) value*1000);
                        break;
                    case "time":
                        // time 값을 원하는 형식으로 변환
                        String timeValue = (String) value;
                        String formattedTime;

                        if (timeValue.contains("T") && timeValue.contains("Z")) {
                            // ISO 8601 형식 처리
                            Instant instant = Instant.parse(timeValue);
                            LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                            formattedTime = dateTime.format(formatter);
                        } else {
                            // 이미 원하는 형식인 경우 그대로 사용
                            formattedTime = timeValue;
                        }

                        settings.setTime(formattedTime);
                        break;
                    default:
                        // 예상하지 못한 키는 무시
                        break;
                }
            });

            settingsRepository.save(settings);
            return ResponseEntity.ok(settings);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
