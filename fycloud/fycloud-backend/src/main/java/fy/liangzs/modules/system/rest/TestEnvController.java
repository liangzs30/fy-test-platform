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
import fy.liangzs.modules.system.domain.TestEnv;
import fy.liangzs.modules.system.service.TestEnvService;
import fy.liangzs.modules.system.service.dto.TestEnvDto;
import fy.liangzs.modules.system.service.dto.TestEnvQueryCriteria;
import fy.liangzs.utils.PageResult;
import fy.liangzs.utils.PageUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;


/**
* @website https://eladmin.vip
* @author liangzisheng
* @date 2024-11-14
**/
@RestController
@RequiredArgsConstructor
@Api(tags = "环境管理")
@RequestMapping("/api/testEnv")
public class TestEnvController {

    private final TestEnvService testEnvService;

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('testEnv:list')")
    public void exportTestEnv(HttpServletResponse response, TestEnvQueryCriteria criteria) throws IOException {
        testEnvService.download(testEnvService.queryAll(criteria, false), response);
    }

    @GetMapping
    @ApiOperation("查询环境")
    @PreAuthorize("@el.check('testEnv:list')")
    public ResponseEntity<PageResult<TestEnvDto>> queryTestEnv(TestEnvQueryCriteria criteria, Pageable pageable){
        List<TestEnvDto> TestEnvs = testEnvService.queryAll(criteria, true);
        return new ResponseEntity<>(PageUtil.toPage(TestEnvs, TestEnvs.size()),HttpStatus.OK);
    }

    @PostMapping
    @Log("新增环境")
    @ApiOperation("新增环境")
    @PreAuthorize("@el.check('testEnv:add')")
    public ResponseEntity<Object> createTestEnv(@Validated @RequestBody TestEnv resources){
        testEnvService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改环境")
    @ApiOperation("修改环境")
    @PreAuthorize("@el.check('testEnv:edit')")
    public ResponseEntity<Object> updateTestEnv(@Validated @RequestBody TestEnv resources){
        testEnvService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除环境")
    @ApiOperation("删除环境")
    @PreAuthorize("@el.check('testEnv:del')")
    public ResponseEntity<Object> deleteTestEnv(@RequestBody Set<Long> ids) {
        testEnvService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}