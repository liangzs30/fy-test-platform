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
import fy.liangzs.modules.system.domain.Project;
import fy.liangzs.modules.system.service.dto.ProjectDto;
import fy.liangzs.modules.system.service.dto.ProjectQueryCriteria;
import fy.liangzs.modules.system.service.dto.ProjectSmallDto;
import lombok.RequiredArgsConstructor;
import fy.liangzs.exception.BadRequestException;
import fy.liangzs.exception.EntityExistException;
import fy.liangzs.modules.system.domain.*;
import fy.liangzs.modules.system.repository.ErrorTopRepository;
import fy.liangzs.modules.system.repository.ProjectRepository;
import fy.liangzs.modules.system.repository.TestCaseRepository;
import fy.liangzs.modules.system.repository.TestReportLogRepository;
import fy.liangzs.modules.system.service.ProjectService;
import fy.liangzs.modules.system.service.dto.*;
import fy.liangzs.modules.system.service.mapstruct.ProjectMapper;
import fy.liangzs.modules.system.service.mapstruct.ProjectSmallMapper;
import fy.liangzs.utils.*;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Zheng Jie
 * @date 2018-12-03
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "role")
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final ProjectSmallMapper projectSmallMapper;
    private final TestCaseRepository testCaseRepository;
    private final TestReportLogRepository testReportLogRepository;
    private final ErrorTopRepository errorTopRepository;

    @Override
    public List<ProjectDto> queryAll() {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        return projectMapper.toDto(projectRepository.findAll(sort));
    }

    @Override
    public List<ProjectDto> queryAll(ProjectQueryCriteria criteria) {
        return projectMapper.toDto(projectRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
    }

    @Override
    public List<ProjectDto> queryByUser(Long userId) {
        return projectRepository.findByUserId(userId)
                .stream()
                .map(projectMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ProjectSmallDto queryLastByUser(Long userId) {
        Project project = projectRepository.findLastByUserId(userId);
        if (project == null) {
            List<Project> projects = projectRepository.findByUserId(userId);
            project = projects.get(0);
            projectRepository.saveLast(project.getId(), userId);
        }
        Long projectID = project.getId();
        int runCount = projectRepository.runCount(projectID);
        int caseCount = projectRepository.coutByProject(projectID);
        TestCaseQueryCriteria criteria = new TestCaseQueryCriteria();
        criteria.setProjectID(projectID);
        JSONObject days = getDayStartEnd();
        List<Timestamp> startTime = (List<Timestamp>) days.get("day1");
        List<Timestamp> startTime2 = (List<Timestamp>) days.get("day7");
        criteria.setCreateTime(startTime);
        List<TestCase> testCaseList = testCaseRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder));
        ProjectSmallDto projectSmallDto = projectSmallMapper.toDto(project);
        projectSmallDto.setCaseCount(caseCount);
        projectSmallDto.setTodayIncrease(testCaseList.size());
        projectSmallDto.setRunCount(runCount);
        List<Integer[]> sevenStatics = new ArrayList<>();
        Integer[] successStatic = new Integer[7];
        Integer[] failStatic = new Integer[7];
        Integer[] errorStatic = new Integer[7];
        for(int i=7;i>=1;i--){
            TestReportLogQueryCriteria testReportLogQueryCriteria = new TestReportLogQueryCriteria();
            testReportLogQueryCriteria.setProjectID(projectID);
            testReportLogQueryCriteria.setResult("pass");
            testReportLogQueryCriteria.setCreateTime((List<Timestamp>) days.get("day" + i));
            List<TestReportLog> testSuccess = testReportLogRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,testReportLogQueryCriteria,criteriaBuilder));
            testReportLogQueryCriteria.setResult("fail");
            List<TestReportLog> testFail = testReportLogRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,testReportLogQueryCriteria,criteriaBuilder));
            testReportLogQueryCriteria.setResult("error");
            List<TestReportLog> testError = testReportLogRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,testReportLogQueryCriteria,criteriaBuilder));
            successStatic[7-i] = testSuccess.size();
            failStatic[7-i] = testFail.size();
            errorStatic[7-i] = testError.size();
        }
        Timestamp errorCountStartTime1 = startTime2.get(0);
        Timestamp errorCountStartTime2 = startTime.get(1);
        List<ErrorTopCount> errorTopCountList = errorTopRepository.errorTopCountByPid(projectID, errorCountStartTime1, errorCountStartTime2);
        int eCount = errorTopCountList.size();
        Integer[] errorTopStatic = new Integer[eCount];
        String[] errorTopNameStatic = new String[eCount];
        for(int i=0;i<eCount;i++){
            ErrorTopCount errorTopCount = errorTopCountList.get(i);
            errorTopStatic[i] = errorTopCount.getCount();
            errorTopNameStatic[i] = String.format("%s(%s)", errorTopCount.getCaseNo(), errorTopCount.getCaseTitle());
        }
        sevenStatics.add(successStatic);
        sevenStatics.add(failStatic);
        sevenStatics.add(errorStatic);
        sevenStatics.add(errorTopStatic);
        projectSmallDto.setSevenRunStatic(sevenStatics);
        projectSmallDto.setErrorTopNameStatic(errorTopNameStatic);
        return projectSmallDto;
    }

    @Override
    public Project getById(Long id) {
        return projectRepository.findById(id).orElse(null);
    }

    @Override
    public ProjectSmallDto saveLast(Long userID, Long projectID) {
        Project project = projectRepository.findLastByUserId(userID);
        if (project == null) {
            projectRepository.saveLast(projectID, userID);
        }else {
            projectRepository.updateLast(projectID, userID);
        }
        ProjectSmallDto projectSmallDto = queryLastByUser(userID);
        return projectSmallDto;
    }

    @Override
    public void deleteLast(Long userId) {
        projectRepository.deleteLast(userId);
    }

    @Override
    public PageResult<ProjectDto> queryAll(ProjectQueryCriteria criteria, Pageable pageable) {
        Page<Project> page = projectRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(projectMapper::toDto));
    }

    @Override
    @Cacheable(key = "'id:' + #p0")
    @Transactional(rollbackFor = Exception.class)
    public ProjectDto findById(long id) {
        Project project = projectRepository.findById(id).orElseGet(Project::new);
        ValidationUtil.isNull(project.getId(), "Project", "id", id);
        return projectMapper.toDto(project);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Project resources) {
        if (projectRepository.findByName(resources.getName()) != null) {
            throw new EntityExistException(Project.class, "username", resources.getName());
        }
        projectRepository.save(resources);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(Project resources) {
        Project project = projectRepository.findById(resources.getId()).orElseGet(Project::new);
        ValidationUtil.isNull(project.getId(), "Project", "id", resources.getId());
        Project project1 = projectRepository.findByName(resources.getName());
        if (project1 != null && !project1.getId().equals(project.getId())) {
            throw new EntityExistException(Project.class, "username", resources.getName());
        }
        project.setName(resources.getName());
        project.setDescription(resources.getDescription());
        project.setUsers(resources.getUsers());
        projectRepository.save(project);
        Long projectID = project.getId();
        Set<Long> projectUserIds = projectRepository.findPorjectUserIds(projectID);
        Set<Long> userIds = projectRepository.findLastUserIds(projectID);
        for (Long userId : userIds) {
            if (!projectUserIds.contains(userId)) {
                projectRepository.deleteLast(userId);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id: ids){
            if(id==1){
                throw new BadRequestException("内部项目，不能删除！");
            }
            int count =  testCaseRepository.countByProject(id);
            if(count > 0){
                throw new BadRequestException("项目下存在用例，请先删除相关数据！");
            }
            projectRepository.deleteCaseCategory(id);
            projectRepository.deleteEnv(id);
            projectRepository.deleteCommonParam(id);
            projectRepository.deleteMachine(id);
            projectRepository.deleteNotifications(id);
            projectRepository.deleteTestTasks(id);
            projectRepository.deleteRunTasks(id);
            projectRepository.deleteReport(id);
            projectRepository.deleteReportLogs(id);
            projectRepository.deleteRelate(id);
            projectRepository.deleteRelates(id);
            projectRepository.deleteById(id);
        }
    }

    private JSONObject getDayStartEnd(){
        Calendar calendar = Calendar.getInstance();
        JSONObject days = new JSONObject();
        for (int i = 1; i <= 7; i++) {
            List<Timestamp> date = new ArrayList<>();
            // 设置当天日期
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            date.add(new Timestamp(calendar.getTimeInMillis()));
            // 输出当天开始时间
//            System.out.println("Day " + (i + 1) + " start: " + calendar.getTime());
            // 设置当天结束时间
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            date.add(new Timestamp(calendar.getTimeInMillis()));
            // 输出当天结束时间
//            System.out.println("Day " + (i + 1) + " end: " + calendar.getTime());
            // 设置为前一天
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            days.put("day" + i, date);
        }
        return days;
    }

    class CreatorCount implements Comparable<CreatorCount> {
        String creator;
        int count;

        public CreatorCount(String creator, int count) {
            this.creator = creator;
            this.count = count;
        }

        @Override
        public int compareTo(CreatorCount c) {
            return c.count - this.count;
        }
    }

    class ErrorCaseCount implements Comparable<ErrorCaseCount> {
        String caseNo;
        int count;

        public ErrorCaseCount(String caseNo, int count) {
            this.caseNo = caseNo;
            this.count = count;
        }
        @Override
        public int compareTo(ErrorCaseCount c) {
            return c.count - this.count;
        }
    }
}
