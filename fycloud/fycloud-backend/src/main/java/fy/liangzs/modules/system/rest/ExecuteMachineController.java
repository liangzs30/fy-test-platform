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
package fy.liangzs.modules.system.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import fy.liangzs.annotation.Log;
import fy.liangzs.modules.system.domain.ExecuteMachine;
import fy.liangzs.modules.system.domain.StopRunTask;
import fy.liangzs.modules.system.service.ExecuteMachineService;
import fy.liangzs.modules.system.service.dto.ExecuteMachineDto;
import fy.liangzs.modules.system.service.dto.ExecuteMachineQueryCriteria;
import fy.liangzs.modules.system.service.dto.MachineRunTaskDto;
import fy.liangzs.modules.system.service.dto.MachineRunTaskQueryCriteria;
import fy.liangzs.utils.PageResult;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Set;


/**
* @author liangzisheng
* &#064;date  2024-11-14
 **/
@RestController
@RequiredArgsConstructor
@Api(tags = "执行机")
@RequestMapping("/api/executeMachine")
public class ExecuteMachineController {

    private final ExecuteMachineService executeMachineService;

    @GetMapping
    @ApiOperation("查询执行机")
    @PreAuthorize("@el.check('machine:list')")
    public ResponseEntity<PageResult<ExecuteMachineDto>> queryExecuteMachine(ExecuteMachineQueryCriteria criteria, Pageable pageable){
        PageResult<ExecuteMachineDto> executeMachineDtoList = executeMachineService.queryAll(criteria, pageable);
        return new ResponseEntity<>(executeMachineDtoList, HttpStatus.OK);
    }

    @GetMapping("futrue/tasks")
    @ApiOperation("查询执行机在执行和未执行任务")
    @PreAuthorize("@el.check('machine:list')")
    public ResponseEntity<PageResult<MachineRunTaskDto>> queryMachineTask(MachineRunTaskQueryCriteria criteria, Pageable pageable){
        PageResult<MachineRunTaskDto> runTaskList = executeMachineService.queryMachineTask(criteria, pageable);
        return new ResponseEntity<>(runTaskList,HttpStatus.OK);
    }

    @PostMapping
    @Log("新增执行机")
    @ApiOperation("新增执行机")
    @PreAuthorize("@el.check('machine:add')")
    public ResponseEntity<Object> createExecuteMachine(@Validated @RequestBody ExecuteMachine resources){
        executeMachineService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping
    @Log("删除执行机")
    @ApiOperation("删除执行机")
    @PreAuthorize("@el.check('machine:del')")
    public ResponseEntity<Object> deleteMachine(@RequestBody Set<Long> ids) {
        executeMachineService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("task/cancel/{id}")
    @Log("取消任务")
    @ApiOperation("取消任务")
    @PreAuthorize("@el.check('runTask:add')")
    public ResponseEntity<Object> cancelTask(@PathVariable Long id) {
        executeMachineService.cancelTaskById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("task/cancelAll")
    @Log("一键取消任务")
    @ApiOperation("一键取消任务")
    @PreAuthorize("@el.check('runTask:add')")
    public ResponseEntity<Object> cancelTaskAll(@RequestBody Set<Long> ids) {
        executeMachineService.cancelAllTask(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("task/stop")
    @Log("终止任务")
    @ApiOperation("终止任务")
    @PreAuthorize("@el.check('runTask:add')")
    public ResponseEntity<Object> stopTask(@RequestBody StopRunTask stopRunTask) {
        executeMachineService.stopTask(stopRunTask);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}