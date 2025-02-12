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

import fy.liangzs.utils.*;
import lombok.RequiredArgsConstructor;
import fy.liangzs.exception.BadRequestException;
import fy.liangzs.modules.quartz.domain.QuartzJob;
import fy.liangzs.modules.quartz.service.QuartzJobService;
import fy.liangzs.modules.system.domain.TestTask;
import fy.liangzs.modules.system.repository.TestTaskRepository;
import fy.liangzs.modules.system.service.TestTaskService;
import fy.liangzs.modules.system.service.dto.TestTaskDto;
import fy.liangzs.modules.system.service.dto.TestTaskQueryCriteria;
import fy.liangzs.modules.system.service.mapstruct.TestTaskMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
* @website https://eladmin.vip
* @description 服务实现
* @author liangzisheng
* @date 2024-11-14
**/
@Service
@RequiredArgsConstructor
public class TestTaskServiceImpl implements TestTaskService {

    private final TestTaskRepository testTaskRepository;
    private final TestTaskMapper testTaskMapper;
    private final QuartzJobService quartzJobService;

    @Override
    public PageResult<TestTaskDto> queryAll(TestTaskQueryCriteria criteria, Pageable pageable){
        Page<TestTask> page = testTaskRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(testTaskMapper::toDto));
    }

    @Override
    public List<TestTaskDto> queryAll(TestTaskQueryCriteria criteria, Boolean isQuery){
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        return testTaskMapper
                .toDto(testTaskRepository
                        .findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),sort));
    }

    @Override
    @Transactional
    public TestTaskDto findById(Long tId) {
        TestTask testTask = testTaskRepository.getById(tId);
        ValidationUtil.isNull(testTask.getId(),"TestTask","tId", tId);
        return testTaskMapper.toDto(testTask);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(TestTask resources) {
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        String userName = userDetails.getUsername();
        QuartzJob quartzJob = new QuartzJob();
        quartzJob.setJobName("[定时任务]" + resources.getName());
        quartzJob.setDescription("自动化定时任务调度");
        quartzJob.setBeanName("testTaskDemo");
        quartzJob.setMethodName("createRunTask");
        quartzJob.setCronExpression(resources.getTExpression());
        quartzJob.setPauseAfterFailure(false);
        quartzJob.setIsPause(resources.getIsPause());
        quartzJob.setParams(String.valueOf(resources.getId()));
        quartzJob.setPersonInCharge(userName);
        Long jid = quartzJobService.create(quartzJob);
        resources.setSchedId(jid);
        resources.setCreateBy(userName);
        resources.setUpdateBy(userName);
        testTaskRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TestTask resources) {
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        String userName = userDetails.getUsername();
        resources.setUpdateBy(userName);
        testTaskRepository.save(resources);
        QuartzJob quartzJob = quartzJobService.findById(resources.getSchedId());
        quartzJob.setJobName("[定时任务]" + resources.getName());
        quartzJob.setDescription("自动化定时任务调度");
        quartzJob.setBeanName("testTaskDemo");
        quartzJob.setMethodName("createRunTask");
        quartzJob.setCronExpression(resources.getTExpression());
        quartzJob.setPauseAfterFailure(false);
        quartzJob.setIsPause(resources.getIsPause());
        quartzJob.setParams(String.valueOf(resources.getId()));
        quartzJob.setPersonInCharge(userName);
        quartzJobService.update(quartzJob);
    }

    @Override
    public void deleteAll(Set<Long> ids) {
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        for (Long tId : ids) {
            TestTask testTask = testTaskRepository.getById(tId);
            if(testTask.getCreateBy().equals(userDetails.getUsername()) || userDetails.getUsername().equals("admin")){
                Set<Long> scdIds = new HashSet<>();
                scdIds.add(testTask.getSchedId());
                testTaskRepository.deleteById(tId);
                quartzJobService.delete(scdIds);
            }else {
                throw new BadRequestException("需要管理员或任务的创建用户才可以删除");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<TestTaskDto> testTaskDtos) {
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        for (TestTaskDto testTaskDto : testTaskDtos) {
            if(testTaskDto.getCreateBy().equals(userDetails.getUsername()) || userDetails.getUsername().equals("admin")){
                testTaskRepository.deleteById(testTaskDto.getId());
            }else {
                throw new BadRequestException("需要管理员或任务的创建用户才可以删除");
            }
        }
    }
}