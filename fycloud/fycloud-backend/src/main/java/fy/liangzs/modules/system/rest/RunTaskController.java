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
import fy.liangzs.modules.system.domain.RunTask;
import fy.liangzs.modules.system.domain.TestCase;
import fy.liangzs.modules.system.domain.TestTask;
import fy.liangzs.modules.system.service.RunTaskService;
import fy.liangzs.modules.system.service.TestTaskService;
import fy.liangzs.modules.system.service.dto.RunTaskDto;
import fy.liangzs.modules.system.service.dto.RunTaskQueryCriteria;
import fy.liangzs.modules.system.service.dto.TestTaskDto;
import fy.liangzs.modules.system.service.dto.TestTaskQueryCriteria;
import fy.liangzs.utils.PageResult;
import fy.liangzs.utils.PageUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;


/**
* @author liangzisheng
* @date 2024-11-14
**/
@RestController
@RequiredArgsConstructor
@Api(tags = "测试任务")
@RequestMapping("/api/runTask")
public class RunTaskController {

    private final RunTaskService runTaskService;

//    @GetMapping
//    @ApiOperation("查询任务")
//    @PreAuthorize("@el.check('runTask:list')")
//    public ResponseEntity<List<RunTaskDto>> queryRunTask(RunTaskQueryCriteria criteria){
//        List<RunTaskDto> runTaskDtoList = runTaskService.queryAll(criteria, true);
//        return new ResponseEntity<>(runTaskDtoList, HttpStatus.OK);
//    }

    @PostMapping
    @Log("新增任务")
    @ApiOperation("新增任务")
    @PreAuthorize("@el.check('runTask:add')")
    public ResponseEntity<Long> createRunTask(@Validated @RequestBody RunTask resources){
        Long taskId = runTaskService.create(resources);
        return new ResponseEntity<>(taskId, HttpStatus.CREATED);
    }
}