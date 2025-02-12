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
import fy.liangzs.exception.EntityNotFoundException;
import fy.liangzs.modules.system.domain.TestCase;
import fy.liangzs.modules.system.domain.TestStep;
import fy.liangzs.modules.system.repository.TaskCaseRepository;
import fy.liangzs.modules.system.repository.TestCaseRepository;
import fy.liangzs.modules.system.repository.TestTaskRepository;
import fy.liangzs.modules.system.service.TestCaseService;
import fy.liangzs.modules.system.service.dto.TestCaseDto;
import fy.liangzs.modules.system.service.dto.TestCaseQueryCriteria;
import fy.liangzs.modules.system.service.mapstruct.TestCaseMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class TestCaseServiceImpl implements TestCaseService {

    private final TestCaseRepository testCaseRepository;
    private final TestCaseMapper testCaseMapper;
    private final TaskCaseRepository taskCaseRepository;
    private final TestTaskRepository testTaskRepository;


    @Override
    public PageResult<TestCaseDto> queryAll(TestCaseQueryCriteria criteria, Pageable pageable) {
        Page<TestCase> page = testCaseRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(testCaseMapper::toDto));
    }

    @Override
    public TestCaseDto findById(Long caseId) {
        TestCase testCase = testCaseRepository.findById(caseId).orElseGet(TestCase::new);
        ValidationUtil.isNull(testCase.getId(),"TestCaseCategory","caseId", caseId);
        return testCaseMapper.toDto(testCase);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(TestCase resources) {
        int countNo = testCaseRepository.countByCaseNo(resources.getCaseNo(), resources.getProjectID());
        if (countNo > 0) {
            throw new BadRequestException("已存在的用例编号，请修改");
        }
        int count = testCaseRepository.countByCaseTitle(resources.getCaseTitle(), resources.getProjectID());
        if (count > 0) {
            throw new BadRequestException("已存在的用例标题，请修改");
        }
        UserDetails userDtails = SecurityUtils.getCurrentUser();
        resources.setCreateBy(userDtails.getUsername());
        resources.setUpdateBy(userDtails.getUsername());
        testCaseRepository.save(resources);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TestCase resources) {
        Long caseId = resources.getId();
        int count = testCaseRepository.countByIdTitle(resources.getCaseTitle(), caseId);
        if (count > 0) {
            throw new BadRequestException("已存在的用例编号，请修改");
        }
        UserDetails userDtails = SecurityUtils.getCurrentUser();
        TestCase testCase = testCaseRepository.findById(caseId).orElseGet(TestCase::new);
        testCase.getTestSteps().clear();
        testCase.setCaseNo(resources.getCaseNo());
        testCase.setCategory(resources.getCategory());
        testCase.setCaseTitle(resources.getCaseTitle());
        testCase.setDesc(resources.getDesc());
        List<TestStep> testSteps = resources.getTestSteps();
        for(TestStep testStep : testSteps) {
            testStep.setCaseId(caseId);
        }
        testCase.getTestSteps().addAll(testSteps);
        testCase.setUpdateBy(userDtails.getUsername());
        TestCase saveCase = testCaseRepository.save(testCase);
        //更新测试任务中的用例
        if(!taskCaseRepository.getByCaseId(caseId).isEmpty()){
            taskCaseRepository.updateByCaseId(caseId,
                    saveCase.getCaseNo(),
                    saveCase.getCaseTitle(),
                    saveCase.getDesc(),
                    saveCase.getCategory().getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(Set<Long> ids) {
        for (Long id : ids) {
            if (!testCaseRepository.findById(id).isPresent()) {
                throw new EntityNotFoundException(TestCase.class, "id", id.toString());
            }
            if(!taskCaseRepository.getByCaseId(id).isEmpty()) {
                Set<Long> taskIds = taskCaseRepository.getTaskIdsByCaseId(id);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("用例[")
                        .append(testCaseRepository.getById(id).getCaseTitle())
                        .append("]已被关联至如下测试任务：");
                for(Long taskId : taskIds) {
                    stringBuilder.append("[")
                            .append(testTaskRepository.getById(taskId).getName())
                            .append("],");
                }
                stringBuilder.append("请先在任务中移除再执行删除。");
                throw new BadRequestException(stringBuilder.toString());
            }
//            TestCaseParamConfRepository.deleteByTestCaseId(id);
            testCaseRepository.deleteById(id);
        }
    }
}