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

import com.alibaba.fastjson.JSONObject;
import fy.liangzs.modules.system.domain.*;
import fy.liangzs.modules.system.repository.*;
import fy.liangzs.modules.system.service.dto.*;
import fy.liangzs.utils.*;
import lombok.RequiredArgsConstructor;
import fy.liangzs.modules.system.service.RunTaskService;
import fy.liangzs.modules.system.service.mapstruct.MachineRunTaskMapper;
import fy.liangzs.modules.system.service.mapstruct.RunTaskMapper;
import fy.liangzs.modules.system.service.mapstruct.TestCaseSortMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author liangzisheng
 * @website https://eladmin.vip
 * @description 服务实现
 * @date 2024-11-14
 **/
@Service
@RequiredArgsConstructor
public class RunTaskServiceImpl implements RunTaskService {

    private final RunTaskRepository runTaskRepository;
    private final RunTaskMapper runTaskMapper;
    private final MachineRunTaskMapper machineRunTaskMapper;
    private final TestCaseRepository testCaseRepository;
    private final TaskCaseRepository taskCaseRepository;
    private final TestCaseSortMapper testCaseSortMapper;
    private final TestReportLogRepository testReportLogRepository;
    private final TestTaskRepository testTaskRepository;
    private final TestEnvRepository testEnvRepository;
    private final StopRunTaskRepository stopRunTaskRepository;

    @Override
    public PageResult<RunTaskDto> queryAll(RunTaskQueryCriteria criteria, Pageable pageable) {
        criteria.setStatus("pending");
        criteria.setRunType("task");
        Page<RunTask> page = runTaskRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(runTaskMapper::toDto));
    }

    @Override
    public List<RunTaskDto> queryAll(RunTaskQueryCriteria criteria, Boolean isQuery) {
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        return runTaskMapper
                .toDto(runTaskRepository
                        .findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), sort));
    }

    @Override
    public PageResult<MachineRunTaskDto> queryMachineAll(MachineRunTaskQueryCriteria criteria, Pageable pageable) {
        List<String> status = new ArrayList<>();
        status.add("running");
        status.add("pending");
        criteria.setStatus(status);
        Page<RunTask> page = runTaskRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(machineRunTaskMapper::toDto));
    }

    @Override
    @Transactional
    public RunTaskDto findById(Long rId) {
        RunTask runTask = runTaskRepository.getById(rId);
        ValidationUtil.isNull(runTask.getId(), "RunTask", "rId", rId);
        return runTaskMapper.toDto(runTask);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(RunTask resources) {
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        resources.setCreateBy(userDetails.getUsername());
        resources.setUpdateBy(userDetails.getUsername());
        TestEnv testEnv = testEnvRepository.getById(resources.getRunEnv().getId());
        String envName = testEnv.getName();
        resources.setName(resources.getName().replace("replaceEnvName", envName));
        resources.setProgress(0);
        resources.setResult(0);
        RunTask task = runTaskRepository.save(resources);
        return task.getId();
    }

    @Override
    public void createScdJob(RunTask resources) {
        runTaskRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(JSONObject task) {
        Long taskId = task.getLong("taskId");
        Timestamp now = new Timestamp(System.currentTimeMillis());
        RunTask runTask = runTaskRepository.getById(taskId);
        String status = task.getString("status");
        if("running".equals(status)){
            List<StopRunTask> stopRunTasks =  stopRunTaskRepository.findByMid(runTask.getRunMachine().getId());
            for(StopRunTask stopTask:stopRunTasks){
                stopRunTaskRepository.delete(stopTask);
            }
        }
        String taskType = runTask.getRunType();
        if (taskType.equals("task") && "termination".equals(status)) {
                runTask.setResult(4);
        }
        runTask.setEndTime(task.getLong("endTime"));
        runTask.setStatus(status);
        runTask.setUpdateTime(now);
        runTaskRepository.save(runTask);
    }

    @Override
    public List<TestCaseDtoSort> getRunTestCase(Long rId) {
        List<TestCaseDtoSort> testCaseList = new ArrayList<>();
        RunTask runTask = runTaskRepository.getById(rId);
        String runType = runTask.getRunType();
        TestCaseDtoSort testCaseDtoSort;
        switch (runType) {
            case "sigleCase":
                String runCase = runTask.getRunCase();
                testCaseDtoSort = JSONObject.parseObject(runCase, TestCaseDtoSort.class);
                testCaseDtoSort.setExecuteSort(1);
                testCaseList.add(testCaseDtoSort);
                break;
            case "batchCase":
                String caseIds = runTask.getCaseIds();
                String[] ids = caseIds.split(",");
                int sort = 1;
                for (String id : ids) {
                    Long caseId = Long.valueOf(id);
                    testCaseDtoSort = testCaseSortMapper.toDto(testCaseRepository.getById(caseId));
                    testCaseDtoSort.setExecuteSort(sort);
                    testCaseList.add(testCaseDtoSort);
                    sort++;
                }
                break;
            case "task":
                Long taskId = runTask.getTaskId();
                List<TaskCase> taskCaseList = taskCaseRepository.getByTaskId(taskId);
                for (TaskCase taskCase : taskCaseList) {
                    testCaseDtoSort = testCaseSortMapper.toDto(testCaseRepository.getById(taskCase.getCaseId()));
                    testCaseDtoSort.setExecuteSort(taskCase.getExecuteSort());
                    testCaseDtoSort.setProjectID(runTask.getProjectID());
                    testCaseList.add(testCaseDtoSort);
                }
                break;
        }
        return testCaseList;
    }

    @Override
    public List<RunTask> getPendingTaskByMachine(Long macId) {
        return runTaskRepository.findByMachineId(macId);
    }

    @Override
    public void deleteById(Long id) {
        runTaskRepository.deleteById(id);
    }


}