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

import cn.hutool.core.collection.CollectionUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import fy.liangzs.annotation.Log;
import fy.liangzs.modules.system.domain.CaseCategory;
import fy.liangzs.modules.system.domain.ElementCategory;
import fy.liangzs.modules.system.service.CaseCategoryService;
import fy.liangzs.modules.system.service.ElCategoryService;
import fy.liangzs.modules.system.service.dto.CaseCategoryDto;
import fy.liangzs.modules.system.service.dto.CaseCategoryQueryCriteria;
import fy.liangzs.modules.system.service.dto.ElCategoryDto;
import fy.liangzs.modules.system.service.dto.ElCategoryQueryCriteria;
import fy.liangzs.utils.PageResult;
import fy.liangzs.utils.PageUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;


/**
* @author liangzisheng
* &#064;date  2024-11-14
 **/
@RestController
@RequiredArgsConstructor
@Api(tags = "WEB元素管理")
@RequestMapping("/api/elementCategory")
public class ElCategoryController {

    private final ElCategoryService elCategoryService;

    @GetMapping
    @ApiOperation("查询WEB元素模块")
    @PreAuthorize("@el.check('elementCategory:list')")
    public ResponseEntity<PageResult<ElCategoryDto>> queryElCategory(ElCategoryQueryCriteria criteria){
        List<ElCategoryDto> elCategoryDtos = elCategoryService.queryAll(criteria);
        return new ResponseEntity<>(PageUtil.toPage(elCategoryDtos, elCategoryDtos.size()),HttpStatus.OK);
    }

    @ApiOperation("查询WEB元素模块:根据ID获取同级与上级数据")
    @PostMapping("/superior")
    @PreAuthorize("@el.check('elementCategory:list')")
    public ResponseEntity<Object> getCategorySuperior(@RequestBody List<Long> ids,
                                                  @RequestParam(defaultValue = "false") Boolean exclude) {
        Set<ElCategoryDto> categorySet  = new LinkedHashSet<>();
        for (Long id : ids) {
            ElCategoryDto elCategoryDto = elCategoryService.findById(id);
            List<ElCategoryDto> elCategoryDtos = elCategoryService.getSuperior(elCategoryDto, new ArrayList<>());
            if(exclude){
                for (ElCategoryDto category : elCategoryDtos) {
                    if(category.getId().equals(elCategoryDto.getPid())) {
                        category.setSubCount(category.getSubCount() - 1);
                    }
                }
                // 编辑部门时不显示自己以及自己下级的数据，避免出现PID数据环形问题
                elCategoryDtos = elCategoryDtos.stream().filter(i -> !ids.contains(i.getId())).collect(Collectors.toList());
            }
            categorySet.addAll(elCategoryDtos);
        }
        return new ResponseEntity<>(elCategoryService.buildTree(new ArrayList<>(categorySet)),HttpStatus.OK);
    }

    @PostMapping
    @Log("新增用例模块")
    @ApiOperation("新增WEB元素模块")
    @PreAuthorize("@el.check('elementCategory:add')")
    public ResponseEntity<Object> createCaseCategory(@Validated @RequestBody ElementCategory resources){
        elCategoryService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改WEB元素模块")
    @ApiOperation("修改WEB元素模块")
    @PreAuthorize("@el.check('elementCategory:edit')")
    public ResponseEntity<Object> updateCaseCategory(@Validated @RequestBody ElementCategory resources){
        elCategoryService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除WEB元素模块")
    @ApiOperation("删除WEB元素模块")
    @PreAuthorize("@el.check('elementCategory:del')")
    public ResponseEntity<Object> deleteCaseCategory(@RequestBody Set<Long> ids) {
        Set<ElCategoryDto> elCategoryDtos = new HashSet<>();
        for (Long id : ids) {
            List<ElementCategory> categoryList = elCategoryService.findByPid(id);
            elCategoryDtos.add(elCategoryService.findById(id));
            if(CollectionUtil.isNotEmpty(categoryList)){
                elCategoryDtos = elCategoryService.getDeleteElCategorys(categoryList, elCategoryDtos);
            }
        }
        elCategoryService.delete(elCategoryDtos);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}