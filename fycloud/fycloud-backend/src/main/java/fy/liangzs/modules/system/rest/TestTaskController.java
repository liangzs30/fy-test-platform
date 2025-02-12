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

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import fy.liangzs.annotation.Log;
import fy.liangzs.modules.system.domain.ExecuteMachine;
import fy.liangzs.modules.system.domain.TestTask;
import fy.liangzs.modules.system.service.ExecuteMachineService;
import fy.liangzs.modules.system.service.TestTaskService;
import fy.liangzs.modules.system.service.dto.ExecuteMachineDto;
import fy.liangzs.modules.system.service.dto.ExecuteMachineQueryCriteria;
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
* @website https://eladmin.vip
* @author liangzisheng
* @date 2024-11-14
**/
@RestController
@RequiredArgsConstructor
@Api(tags = "测试任务")
@RequestMapping("/api/testTask")
public class TestTaskController {

    private final TestTaskService testTaskService;

    @GetMapping
    @ApiOperation("查询任务")
    @PreAuthorize("@el.check('testTask:list')")
    public ResponseEntity<PageResult<TestTaskDto>> queryTestTask(TestTaskQueryCriteria criteria, Pageable pageable){
        List<TestTaskDto> testTaskDtoList = testTaskService.queryAll(criteria, true);
        return new ResponseEntity<>(PageUtil.toPage(testTaskDtoList, testTaskDtoList.size()),HttpStatus.OK);
    }

    @PostMapping
    @Log("新增任务")
    @ApiOperation("新增任务")
    @PreAuthorize("@el.check('testTask:add')")
    public ResponseEntity<Object> createTestTask(@Validated @RequestBody TestTask resources){
        testTaskService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("更新任务")
    @ApiOperation("更新任务")
    @PreAuthorize("@el.check('testTask:edit')")
    public ResponseEntity<Object> updateTestTask(@Validated @RequestBody TestTask resources){
        testTaskService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除任务")
    @ApiOperation("删除任务")
    @PreAuthorize("@el.check('testTask:del')")
    public ResponseEntity<Object> deleteTestTask(@RequestBody Set<Long> ids) {
        testTaskService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}