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
import fy.liangzs.modules.system.domain.TestCase;
import fy.liangzs.modules.system.service.TestCaseService;
import fy.liangzs.modules.system.service.dto.TestCaseDto;
import fy.liangzs.modules.system.service.dto.TestCaseQueryCriteria;
import fy.liangzs.utils.PageResult;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;


/**
* @website https://eladmin.vip
* @author liangzisheng
* @date 2024-11-14
**/
@RestController
@RequiredArgsConstructor
@Api(tags = "测试用例")
@RequestMapping("/api/caseCenter")
public class TestCaseController {

    private final TestCaseService testCaseService;

    @GetMapping
    @ApiOperation("查询测试用例")
    @PreAuthorize("@el.check('testCase:list')")
    public ResponseEntity<PageResult<TestCaseDto>> queryTestCase(TestCaseQueryCriteria criteria, Pageable pageable){
        PageResult<TestCaseDto> testCases = testCaseService.queryAll(criteria, pageable);
        return new ResponseEntity<>(testCases, HttpStatus.OK);
    }

    @PostMapping
    @Log("新增测试用例")
    @ApiOperation("新增测试用例")
    @PreAuthorize("@el.check('testCase:add')")
    public ResponseEntity<Object> createTestCase(@Validated @RequestBody TestCase resources){
        testCaseService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改测试用例")
    @ApiOperation("修改测试用例")
    @PreAuthorize("@el.check('testCase:edit')")
    public ResponseEntity<Object> updateTestCase(@Validated @RequestBody TestCase resources){
        testCaseService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除测试用例")
    @ApiOperation("删除测试用例")
    @PreAuthorize("@el.check('testCase:del')")
    public ResponseEntity<Object> deleteTestCase(@RequestBody Set<Long> ids) {
        testCaseService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}