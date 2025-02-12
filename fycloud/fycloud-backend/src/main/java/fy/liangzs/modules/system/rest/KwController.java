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
import fy.liangzs.modules.system.domain.Kw;
import fy.liangzs.modules.system.service.dto.KwDto;
import fy.liangzs.modules.system.service.dto.KwQueryCriteria;
import fy.liangzs.utils.PageResult;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import fy.liangzs.modules.system.service.KwService;

import java.util.*;


/**
* @author liangzisheng
* &#064;date  2024-11-14
 **/
@RestController
@RequiredArgsConstructor
@Api(tags = "关键字管理")
@RequestMapping("/api/kw")
public class KwController {

    private final KwService kwService;

    @GetMapping
    @ApiOperation("查询关键字")
    @PreAuthorize("@el.check('kw:list')")
    public ResponseEntity<PageResult<KwDto>> queryKw(KwQueryCriteria criteria, Pageable pageable){
        PageResult<KwDto> kws= kwService.queryAll(criteria, pageable);
        return new ResponseEntity<>(kws, HttpStatus.OK);
    }

    @PostMapping
    @Log("新增关键字")
    @ApiOperation("新增关键字")
    @PreAuthorize("@el.check('kw:add')")
    public ResponseEntity<Object> createKw(@Validated @RequestBody Kw resources){
        kwService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改关键字")
    @ApiOperation("修改关键字")
    @PreAuthorize("@el.check('kw:edit')")
    public ResponseEntity<Object> updateKw(@Validated @RequestBody Kw resources){
        kwService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除关键字")
    @ApiOperation("删除关键字")
    @PreAuthorize("@el.check('kw:del')")
    public ResponseEntity<Object> deleteKw(@RequestBody Set<Long> ids) {
        kwService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}