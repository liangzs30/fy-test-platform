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
import fy.liangzs.modules.system.domain.CustomerFunction;
import fy.liangzs.modules.system.service.CommonParamService;
import fy.liangzs.modules.system.service.CustomerFunctionService;
import fy.liangzs.modules.system.service.dto.CommonParamDto;
import fy.liangzs.modules.system.service.dto.CommonParamQueryCriteria;
import fy.liangzs.modules.system.service.dto.CustomerFunctionDto;
import fy.liangzs.modules.system.service.dto.CustomerFunctionQueryCriteria;
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
@Api(tags = "函数管理")
@RequestMapping("/api/customer/function")
public class CustomerFunctionController {

    private final CustomerFunctionService customerFunctionService;

    @GetMapping
    @ApiOperation("查询函数列表")
    @PreAuthorize("@el.check('customerFunc:list')")
    public ResponseEntity<PageResult<CustomerFunctionDto>> queryFunctionList(CustomerFunctionQueryCriteria criteria, Pageable pageable){
        List<CustomerFunctionDto> customerFunctionDtos = customerFunctionService.queryAll(criteria, true);
        return new ResponseEntity<>(PageUtil.toPage(customerFunctionDtos, customerFunctionDtos.size()),HttpStatus.OK);
    }

    @PostMapping
    @Log("新增函数")
    @ApiOperation("新增函数")
    @PreAuthorize("@el.check('customerFunc:add')")
    public ResponseEntity<Object> createFunction(@Validated @RequestBody CustomerFunction resources){
        customerFunctionService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改函数")
    @ApiOperation("修改函数")
    @PreAuthorize("@el.check('customerFunc:edit')")
    public ResponseEntity<Object> updateFunction(@Validated @RequestBody CustomerFunction resources){
        customerFunctionService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除函数")
    @ApiOperation("删除函数")
    @PreAuthorize("@el.check('customerFunc:del')")
    public ResponseEntity<Object> deleteFunction(@RequestBody Set<Long> ids) {
        customerFunctionService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}