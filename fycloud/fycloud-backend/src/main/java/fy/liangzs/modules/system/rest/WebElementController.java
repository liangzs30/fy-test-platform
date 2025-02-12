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
import fy.liangzs.modules.system.domain.WebElement;
import fy.liangzs.modules.system.service.KwService;
import fy.liangzs.modules.system.service.WebElementService;
import fy.liangzs.modules.system.service.dto.KwDto;
import fy.liangzs.modules.system.service.dto.KwQueryCriteria;
import fy.liangzs.modules.system.service.dto.WebElementDto;
import fy.liangzs.modules.system.service.dto.WebElementQueryCriteria;
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
@Api(tags = "WEB元素管理")
@RequestMapping("/api/web/element")
public class WebElementController {

    private final WebElementService webElementService;

    @GetMapping
    @ApiOperation("查询WEB元素")
    @PreAuthorize("@el.check('webelement:list')")
    public ResponseEntity<PageResult<WebElementDto>> queryElement(WebElementQueryCriteria criteria, Pageable pageable){
        PageResult<WebElementDto> elementDtos = webElementService.queryAll(criteria, pageable);
        return new ResponseEntity<>(elementDtos, HttpStatus.OK);
    }

    @GetMapping("get/{id}")
    @ApiOperation("查询WEB元素")
    @PreAuthorize("@el.check('webelement:list')")
    public ResponseEntity<WebElementDto> findById(@PathVariable Long id){
        WebElementDto elementDto = webElementService.findById(id);
        return new ResponseEntity<>(elementDto, HttpStatus.OK);
    }

    @PostMapping
    @Log("新增WEB元素")
    @ApiOperation("新增WEB元素")
    @PreAuthorize("@el.check('webelement:add')")
    public ResponseEntity<Object> createElement(@Validated @RequestBody WebElement resources){
        webElementService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改WEB元素")
    @ApiOperation("修改WEB元素")
    @PreAuthorize("@el.check('webelement:edit')")
    public ResponseEntity<Object> updateElement(@Validated @RequestBody WebElement resources){
        webElementService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除WEB元素")
    @ApiOperation("删除WEB元素")
    @PreAuthorize("@el.check('webelement:del')")
    public ResponseEntity<Object> deleteElement(@RequestBody Set<Long> ids) {
        webElementService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}