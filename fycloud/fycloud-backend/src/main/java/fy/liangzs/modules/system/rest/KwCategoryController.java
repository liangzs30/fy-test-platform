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
import fy.liangzs.modules.system.service.KwCategoryService;
import fy.liangzs.modules.system.service.dto.KwCategoryDto;
import fy.liangzs.modules.system.service.dto.KwCategoryQueryCriteria;
import fy.liangzs.modules.system.domain.KwCategory;
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
import java.util.stream.Collectors;


/**
* @author liangzisheng
* &#064;date  2024-11-14
 **/
@RestController
@RequiredArgsConstructor
@Api(tags = "关键字模块管理")
@RequestMapping("/api/kwCategory")
public class KwCategoryController {

    private final KwCategoryService KwCategoryService;

    @GetMapping
    @ApiOperation("查询关键字模块")
    @PreAuthorize("@el.check('kwCategory:list')")
    public ResponseEntity<PageResult<KwCategoryDto>> queryKwCategory(KwCategoryQueryCriteria criteria){
        List<KwCategoryDto> KwCategorys = KwCategoryService.queryAll(criteria);
        return new ResponseEntity<>(PageUtil.toPage(KwCategorys, KwCategorys.size()),HttpStatus.OK);
    }

    @ApiOperation("查询关键字模块:根据ID获取同级与上级数据")
    @PostMapping("/superior")
    @PreAuthorize("@el.check('kwCategory:list')")
    public ResponseEntity<Object> getCategorySuperior(@RequestBody List<Long> ids,
                                                  @RequestParam(defaultValue = "false") Boolean exclude) {
        Set<KwCategoryDto> categorySet  = new LinkedHashSet<>();
        for (Long id : ids) {
            KwCategoryDto KwCategoryDto = KwCategoryService.findById(id);
            List<KwCategoryDto> KwCategorys = KwCategoryService.getSuperior(KwCategoryDto, new ArrayList<>());
            if(exclude){
                for (KwCategoryDto category : KwCategorys) {
                    if(category.getId().equals(KwCategoryDto.getPid())) {
                        category.setSubCount(category.getSubCount() - 1);
                    }
                }
                // 编辑部门时不显示自己以及自己下级的数据，避免出现PID数据环形问题
                KwCategorys = KwCategorys.stream().filter(i -> !ids.contains(i.getId())).collect(Collectors.toList());
            }
            categorySet.addAll(KwCategorys);
        }
        return new ResponseEntity<>(KwCategoryService.buildTree(new ArrayList<>(categorySet)),HttpStatus.OK);
    }

    @PostMapping
    @Log("新增关键字模块")
    @ApiOperation("新增关键字模块")
    @PreAuthorize("@el.check('kwCategory:add')")
    public ResponseEntity<Object> createKwCategory(@Validated @RequestBody KwCategory resources){
        KwCategoryService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改关键字模块")
    @ApiOperation("修改关键字模块")
    @PreAuthorize("@el.check('kwCategory:edit')")
    public ResponseEntity<Object> updateKwCategory(@Validated @RequestBody KwCategory resources){
        KwCategoryService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除关键字模块")
    @ApiOperation("删除关键字模块")
    @PreAuthorize("@el.check('kwCategory:del')")
    public ResponseEntity<Object> deleteKwCategory(@RequestBody Set<Long> ids) {
        Set<KwCategoryDto> KwCategoryDtos = new HashSet<>();
        for (Long id : ids) {
            List<KwCategory> categoryList = KwCategoryService.findByPid(id);
            KwCategoryDtos.add(KwCategoryService.findById(id));
            if(CollectionUtil.isNotEmpty(categoryList)){
                KwCategoryDtos = KwCategoryService.getDeleteKwCategorys(categoryList, KwCategoryDtos);
            }
        }
        KwCategoryService.delete(KwCategoryDtos);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}