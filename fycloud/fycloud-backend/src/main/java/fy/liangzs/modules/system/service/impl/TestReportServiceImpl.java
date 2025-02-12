/*
*  Copyright 2019-2020 Zheng Jie
*
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/
package fy.liangzs.modules.system.service.impl;

import lombok.RequiredArgsConstructor;
import fy.liangzs.modules.system.domain.TestReport;
import fy.liangzs.modules.system.domain.TestReportLog;
import fy.liangzs.modules.system.repository.TaskCaseRepository;
import fy.liangzs.modules.system.repository.TestReportLogRepository;
import fy.liangzs.modules.system.repository.TestReportRepository;
import fy.liangzs.modules.system.service.TestReportService;
import fy.liangzs.modules.system.service.dto.TestReportDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author liangzisheng
* &#064;date  2024-11-14
 **/
@Service
@RequiredArgsConstructor
public class TestReportServiceImpl implements TestReportService {

    private final TestReportRepository testReportRepository;
    private final TestReportLogRepository testReportLogRepository;
    private final TaskCaseRepository taskCaseRepository;


    @Override
    public TestReportDto findById(Long id) {
        TestReport testReport = testReportRepository.findByTaskId(id);
//        ValidationUtil.isNull(testReport.getId(),"TestReport","id", id);
        if (testReport == null) {
            return new TestReportDto();
        }
        List<TestReportLog> testReportLogList = testReportLogRepository.findByTaskId(id);
        TestReportDto testReportDto = new TestReportDto();
        testReportDto.setTaskName(testReport.getRunTask().getName());
        testReportDto.setProgress(testReport.getRunTask().getProgress());
        testReportDto.setResult(testReport.getRunTask().getResult());
        testReportDto.setTestReportLogList(testReportLogList);
        Long taskId = testReport.getRunTask().getTaskId();
        int total = 1;
        int successCount = testReportLogRepository.successCountByTaskId(id);
        int failCount = testReportLogRepository.failCountByTaskId(id);
        int errorCount = testReportLogRepository.errorCountByTaskId(id);
        if(testReport.getRunTask().getRunType().equals("task")){
            total = taskCaseRepository.countByTaskId(taskId);
        } else if (testReport.getRunTask().getRunType().equals("batchCase")) {
            String caseIds = testReport.getRunTask().getCaseIds();
            total = caseIds.split(",").length;
        }
        testReportDto.setTotal(total);
        testReportDto.setStartTime(testReport.getRunTask().getCreateTime());
        testReportDto.setSuccessCount(successCount);
        testReportDto.setFailCount(failCount);
        testReportDto.setErrorCount(errorCount);
        testReportDto.setStatus(testReport.getRunTask().getStatus());
        testReportDto.setCreateBy(testReport.getRunTask().getCreateBy());
        String taskStatus = testReport.getRunTask().getStatus();
        if(taskStatus.equals("complete") || taskStatus.equals("timeout") || taskStatus.equals("termination")){
            testReportDto.setEndTime(testReport.getRunTask().getUpdateTime());
            String costTime = getCostTime(testReport);
            testReportDto.setCostTime(costTime);
        }
        return testReportDto;
    }

    private static String getCostTime(TestReport testReport) {
        long timePassed = testReport.getRunTask().getEndTime() - testReport.getRunTask().getStartTime();
        String costTime = "";
        if(timePassed < 60 * 1000){
            costTime = String.format("%.2f", timePassed / 1000.0) + "秒";
        }else if(timePassed < 60 * 60 * 1000){
            costTime = String.format("%02d分%02d秒", timePassed / 60000, timePassed % 6000);
        }else {
            long seconds = timePassed / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            seconds %= 60;
            minutes %= 60;
            hours %= 24;
            costTime = String.format("%02d时%02d分%02d秒", hours, minutes, seconds);
        }
        return costTime;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(TestReport resource) {
        testReportRepository.save(resource);
    }

}