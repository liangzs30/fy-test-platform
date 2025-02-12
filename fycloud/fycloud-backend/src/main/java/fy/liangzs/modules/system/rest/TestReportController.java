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
import fy.liangzs.config.FileProperties;
import fy.liangzs.modules.system.domain.TestReport;
import fy.liangzs.modules.system.service.RunTaskService;
import fy.liangzs.modules.system.service.TestReportService;
import fy.liangzs.modules.system.service.dto.RunTaskDto;
import fy.liangzs.modules.system.service.dto.RunTaskQueryCriteria;
import fy.liangzs.modules.system.service.dto.TestReportDto;
import fy.liangzs.utils.PageResult;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
* @website https://eladmin.vip
* @author liangzisheng
* @date 2024-11-14
**/
@RestController
@RequiredArgsConstructor
@Api(tags = "测试报告")
@RequestMapping("/api/testReport")
public class TestReportController {

    private final RunTaskService runTaskService;
    private final TestReportService testReportService;
    private final FileProperties properties;

    @GetMapping
    @ApiOperation("查询报告列表")
    @PreAuthorize("@el.check('report:list')")
    public ResponseEntity<PageResult<RunTaskDto>> queryReportList(RunTaskQueryCriteria criteria, Pageable pageable){
        PageResult<RunTaskDto> reportList = runTaskService.queryAll(criteria, pageable);
        return new ResponseEntity<>(reportList, HttpStatus.OK);
    }

    @GetMapping("/detail/{id}")
    @ApiOperation("查询报告详情")
    @PreAuthorize("@el.check('report:detail')")
    public ResponseEntity<TestReportDto> queryReporDetail(@PathVariable Long id){
        TestReportDto testReport = testReportService.findById(id);
        return new ResponseEntity<>(testReport, HttpStatus.OK);
    }

}