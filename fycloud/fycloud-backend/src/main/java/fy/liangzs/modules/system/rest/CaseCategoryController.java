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
import fy.liangzs.annotation.Log;
import fy.liangzs.modules.system.service.CaseCategoryService;
import fy.liangzs.modules.system.service.dto.CaseCategoryDto;
import fy.liangzs.modules.system.service.dto.CaseCategoryQueryCriteria;
import fy.liangzs.modules.system.domain.CaseCategory;
import fy.liangzs.utils.PageResult;
import fy.liangzs.utils.PageUtil;
import org.springframework.data.domain.Pageable;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;



/**
* @author liangzisheng
* &#064;date  2024-11-14
 **/
@RestController
@RequiredArgsConstructor
@Api(tags = "用例模块管理")
@RequestMapping("/api/caseCategory")
public class CaseCategoryController {

    private final CaseCategoryService caseCategoryService;

    @GetMapping
    @ApiOperation("查询用例模块")
    @PreAuthorize("@el.check('caseCategory:list')")
    public ResponseEntity<PageResult<CaseCategoryDto>> queryCaseCategory(CaseCategoryQueryCriteria criteria){
        List<CaseCategoryDto> caseCategorys = caseCategoryService.queryAll(criteria);
        return new ResponseEntity<>(PageUtil.toPage(caseCategorys, caseCategorys.size()),HttpStatus.OK);
    }

    @ApiOperation("查询用例模块:根据ID获取同级与上级数据")
    @PostMapping("/superior")
    @PreAuthorize("@el.check('caseCategory:list')")
    public ResponseEntity<Object> getCategorySuperior(@RequestBody List<Long> ids,
                                                  @RequestParam(defaultValue = "false") Boolean exclude) {
        Set<CaseCategoryDto> categorySet  = new LinkedHashSet<>();
        for (Long id : ids) {
            CaseCategoryDto caseCategoryDto = caseCategoryService.findById(id);
            List<CaseCategoryDto> caseCategorys = caseCategoryService.getSuperior(caseCategoryDto, new ArrayList<>());
            if(exclude){
                for (CaseCategoryDto category : caseCategorys) {
                    if(category.getId().equals(caseCategoryDto.getPid())) {
                        category.setSubCount(category.getSubCount() - 1);
                    }
                }
                // 编辑部门时不显示自己以及自己下级的数据，避免出现PID数据环形问题
                caseCategorys = caseCategorys.stream().filter(i -> !ids.contains(i.getId())).collect(Collectors.toList());
            }
            categorySet.addAll(caseCategorys);
        }
        return new ResponseEntity<>(caseCategoryService.buildTree(new ArrayList<>(categorySet)),HttpStatus.OK);
    }

    @PostMapping
    @Log("新增用例模块")
    @ApiOperation("新增用例模块")
    @PreAuthorize("@el.check('caseCategory:add')")
    public ResponseEntity<Object> createCaseCategory(@Validated @RequestBody CaseCategory resources){
        caseCategoryService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改用例模块")
    @ApiOperation("修改用例模块")
    @PreAuthorize("@el.check('caseCategory:edit')")
    public ResponseEntity<Object> updateCaseCategory(@Validated @RequestBody CaseCategory resources){
        caseCategoryService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除用例模块")
    @ApiOperation("删除用例模块")
    @PreAuthorize("@el.check('caseCategory:del')")
    public ResponseEntity<Object> deleteCaseCategory(@RequestBody Set<Long> ids) {
        Set<CaseCategoryDto> caseCategoryDtos = new HashSet<>();
        for (Long id : ids) {
            List<CaseCategory> categoryList = caseCategoryService.findByPid(id);
            caseCategoryDtos.add(caseCategoryService.findById(id));
            if(CollectionUtil.isNotEmpty(categoryList)){
                caseCategoryDtos = caseCategoryService.getDeleteCaseCategorys(categoryList, caseCategoryDtos);
            }
        }
        caseCategoryService.delete(caseCategoryDtos);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}