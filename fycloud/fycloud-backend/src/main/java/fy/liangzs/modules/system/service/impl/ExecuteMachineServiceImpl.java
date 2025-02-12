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
import fy.liangzs.modules.system.service.dto.*;
import fy.liangzs.utils.*;
import lombok.RequiredArgsConstructor;
import fy.liangzs.exception.BadRequestException;
import fy.liangzs.modules.system.domain.ExecuteMachine;
import fy.liangzs.modules.system.domain.StopRunTask;
import fy.liangzs.modules.system.repository.ExecuteMachineRepository;
import fy.liangzs.modules.system.repository.StopRunTaskRepository;
import fy.liangzs.modules.system.service.ExecuteMachineService;
import fy.liangzs.modules.system.service.RunTaskService;
import fy.liangzs.modules.system.service.mapstruct.ExecuteMachineMapper;
import fy.liangzs.modules.system.service.mapstruct.MachineRunTaskMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

/**
* @website https://eladmin.vip
* @description 服务实现
* @author liangzisheng
* @date 2024-11-14
**/
@Service
@RequiredArgsConstructor
public class ExecuteMachineServiceImpl implements ExecuteMachineService {

    private final ExecuteMachineRepository executeMachineRepository;
    private final ExecuteMachineMapper executeMachineMapper;
    private final RunTaskService runTaskService;
    private final MachineRunTaskMapper machineRunTaskMapper;
    private final StopRunTaskRepository stopRunTaskRepository;

    @Override
    public PageResult<ExecuteMachineDto> queryAll(ExecuteMachineQueryCriteria criteria, Pageable pageable){
        Page<ExecuteMachine> page = executeMachineRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(executeMachineMapper::toDto));
    }

    @Override
    public List<ExecuteMachineDto> queryAll(ExecuteMachineQueryCriteria criteria, Boolean isQuery){
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        return executeMachineMapper
                .toDto(executeMachineRepository
                        .findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),sort));
    }

    @Override
    public PageResult<MachineRunTaskDto> queryMachineTask(MachineRunTaskQueryCriteria criteria, Pageable pageable) {
        PageResult<MachineRunTaskDto> page = runTaskService.queryMachineAll(criteria, pageable);
        return page;
    }

    @Override
    @Transactional
    public ExecuteMachineDto findById(Long mId) {
        ExecuteMachine executeMachine = executeMachineRepository.getById(mId);
        ValidationUtil.isNull(executeMachine.getId(),"ExecuteMachine","mId", mId);
        return executeMachineMapper.toDto(executeMachine);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ExecuteMachine resources) {
        String uuid = UUID.randomUUID().toString();
        resources.setUniqueCode(uuid);
        resources.setStatus(0);
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        resources.setCreateBy(userDetails.getUsername());
        resources.setUpdateBy(userDetails.getUsername());
        executeMachineRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(JSONObject mac) {
        ExecuteMachine executeMachine = executeMachineRepository.findByUniqeCode(mac.getString("uniqueCode"));
        if(executeMachine == null){
            throw new BadRequestException("执行机不存在，请检查");
        }
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        executeMachine.setStatus(mac.getInteger("status"));
        executeMachine.setIpAddr(mac.getString("ipAddr"));
        executeMachine.setTerminalSerial(mac.getString("terminalSerial"));
        executeMachine.setUpdateBy(userDetails.getUsername());
        executeMachine.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        executeMachineRepository.save(executeMachine);
    }

    @Override
    public void deleteAll(Set<Long> ids) {
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        for (Long mId : ids) {
            ExecuteMachine executeMachine = executeMachineRepository.getById(mId);
            if(executeMachine.getCreateBy().equals(userDetails.getUsername()) || userDetails.getUsername().equals("admin")){
                executeMachineRepository.deleteById(mId);
            }else {
                throw new BadRequestException("需要管理员或执行机的创建用户才可以删除");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<ExecuteMachineDto> machines) {
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        for (ExecuteMachineDto executeMachineDto : machines) {
            if(executeMachineDto.getCreateBy().equals(userDetails.getUsername()) || userDetails.getUsername().equals("admin")){
                executeMachineRepository.deleteById(executeMachineDto.getId());
            }else {
                throw new BadRequestException("需要管理员或执行机的创建用户才可以删除");
            }
        }
    }

    @Override
    public ExecuteMachine getByUniqueCode(String uniqueCode) {
        return executeMachineRepository.findByUniqeCode(uniqueCode);
    }

    @Override
    public List<ExecuteMachine> queryAllMachine() {
        return executeMachineRepository.findAllMacine();
    }

    @Override
    public void machineOffLine(String uniqeCode, String user) {
        executeMachineRepository.machineOffLine(uniqeCode, user);
    }

    @Override
    public void cancelTaskById(Long taskId) {
        runTaskService.deleteById(taskId);
    }

    @Override
    public void cancelAllTask(Set<Long> ids) {
        for (Long id : ids) {
            runTaskService.deleteById(id);
        }
    }

    @Override
    public void stopTask(StopRunTask stopRunTask) {
        Long tid = stopRunTask.getTid();
        RunTaskDto runTask = runTaskService.findById(tid);
        if(!runTask.getStatus().equals("running")){
            throw new BadRequestException("此任务没有在执行，终止失败");
        }else {
            JSONObject task = new JSONObject();
            task.put("taskId", tid);
            task.put("status", "termination");
            task.put("endTime", System.currentTimeMillis());
            runTaskService.updateStatus(task);
            stopRunTaskRepository.save(stopRunTask);
        }
    }
}