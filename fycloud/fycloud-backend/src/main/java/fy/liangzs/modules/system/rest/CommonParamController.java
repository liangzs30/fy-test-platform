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
import fy.liangzs.modules.system.domain.CommonParam;
import fy.liangzs.modules.system.service.CommonParamService;
import fy.liangzs.modules.system.service.dto.CommonParamDto;
import fy.liangzs.modules.system.service.dto.CommonParamQueryCriteria;
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
import java.util.List;
import java.util.Set;


/**
* @author liangzisheng
* &#064;date  2024-11-14
 **/
@RestController
@RequiredArgsConstructor
@Api(tags = "公共参数")
@RequestMapping("/api/commonParam")
public class CommonParamController {

    private final CommonParamService commonParamService;

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('commonParam:list')")
    public void exportCommonParam(HttpServletResponse response, CommonParamQueryCriteria criteria) throws IOException {
        commonParamService.download(commonParamService.queryAll(criteria, false), response);
    }

    @GetMapping
    @ApiOperation("查询环境")
    @PreAuthorize("@el.check('commonParam:list')")
    public ResponseEntity<PageResult<CommonParamDto>> queryCommonParam(CommonParamQueryCriteria criteria, Pageable pageable){
        List<CommonParamDto> commonParamDtoList = commonParamService.queryAll(criteria, true);
        return new ResponseEntity<>(PageUtil.toPage(commonParamDtoList, commonParamDtoList.size()),HttpStatus.OK);
    }

    @PostMapping
    @Log("新增参数")
    @ApiOperation("新增参数")
    @PreAuthorize("@el.check('commonParam:add')")
    public ResponseEntity<Object> createCommonParam(@Validated @RequestBody CommonParam resources){
        commonParamService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改参数")
    @ApiOperation("修改参数")
    @PreAuthorize("@el.check('commonParam:edit')")
    public ResponseEntity<Object> updateCommonParam(@Validated @RequestBody CommonParam resources){
        commonParamService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除参数")
    @ApiOperation("删除参数")
    @PreAuthorize("@el.check('commonParam:del')")
    public ResponseEntity<Object> deleteCommonParam(@RequestBody Set<Long> ids) {
        commonParamService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}