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
package fy.liangzs.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import lombok.RequiredArgsConstructor;
import fy.liangzs.exception.BadRequestException;
import fy.liangzs.modules.system.domain.ElementCategory;
import fy.liangzs.modules.system.repository.ElementCategoryRepository;
import fy.liangzs.modules.system.service.ElCategoryService;
import fy.liangzs.modules.system.service.dto.ElCategoryDto;
import fy.liangzs.modules.system.service.dto.ElCategoryQueryCriteria;
import fy.liangzs.modules.system.service.mapstruct.ElCategoryMapper;
import fy.liangzs.utils.FileUtil;
import fy.liangzs.utils.QueryHelp;
import fy.liangzs.utils.SecurityUtils;
import fy.liangzs.utils.ValidationUtil;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
* @website https://eladmin.vip
* @description 服务实现
* @author liangzisheng
* @date 2024-11-14
**/
@Service
@RequiredArgsConstructor
public class ElCategoryServiceImpl implements ElCategoryService {

    private final ElementCategoryRepository elementCategoryRepository;
    private final ElCategoryMapper elCategoryMapper;

    @Override
    public List<ElCategoryDto> queryAll(ElCategoryQueryCriteria criteria){
        Sort sort = Sort.by(Sort.Direction.ASC, "categorySort");
        if(criteria.getPid()==null && criteria.getName()==null){
            criteria.setPidIsNull(true);
        }
        return elCategoryMapper.toDto(elementCategoryRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),sort));
    }

    @Override
    @Transactional
    public ElCategoryDto findById(Long categoryId) {
        ElementCategory caseCategory = elementCategoryRepository.findById(categoryId).orElseGet(ElementCategory::new);
        ValidationUtil.isNull(caseCategory.getId(),"CaseCategory","categoryId",categoryId);
        return elCategoryMapper.toDto(caseCategory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(ElementCategory resources) {
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        resources.setCreateBy(userDetails.getUsername());
        resources.setUpdateBy(userDetails.getUsername());
        elementCategoryRepository.save(resources);
        // 计算子节点数目
        resources.setSubCount(0);
        // 清理缓存
        updateSubCnt(resources.getPid());
        // 清理自定义角色权限的datascope缓存
//        delCaches(resources.getPid());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ElementCategory resources) {
        // 旧的模块
        Long oldPid = findById(resources.getId()).getPid();
        Long newPid = resources.getPid();
        if(resources.getPid() != null && resources.getId().equals(resources.getPid())) {
            throw new BadRequestException("上级不能为自己");
        }
        ElementCategory category = elementCategoryRepository.findById(resources.getId()).orElseGet(ElementCategory::new);
        ValidationUtil.isNull( category.getId(),"ElementCategory","id",resources.getId());
        resources.setId(category.getId());
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        resources.setUpdateBy(userDetails.getUsername());
        elementCategoryRepository.save(resources);
        // 更新父节点中子节点数目
        updateSubCnt(oldPid);
        updateSubCnt(newPid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(Long[] ids) {
        for (Long categoryId : ids) {
            ElCategoryDto elCategoryDto = findById(categoryId);
            if(elCategoryDto.getSubCount()>0){
                throw new BadRequestException("存在子节点，不能删除");
            }
            elementCategoryRepository.deleteById(categoryId);
            updateSubCnt(elCategoryDto.getPid());
        }
    }

    @Override
    public void download(List<ElCategoryDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (ElCategoryDto elCategoryDto : all) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("上级模块", elCategoryDto.getPid());
            map.put("子模块数目", elCategoryDto.getSubCount());
            map.put("名称", elCategoryDto.getName());
            map.put("排序", elCategoryDto.getCategorySort());
            map.put("创建者", elCategoryDto.getCreateBy());
            map.put("更新者", elCategoryDto.getUpdateBy());
            map.put("创建日期", elCategoryDto.getCreateTime());
            map.put("更新时间", elCategoryDto.getUpdateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public List<ElCategoryDto> getSuperior(ElCategoryDto elCategoryDto, List<ElementCategory> elementCategories) {
        if(elCategoryDto.getPid() == null){
            elementCategories.addAll(elementCategoryRepository.findByPidIsNull());
            return elCategoryMapper.toDto(elementCategories);
        }
        elementCategories.addAll(elementCategoryRepository.findByPid(elCategoryDto.getPid()));
        return getSuperior(findById(elCategoryDto.getPid()), elementCategories);
    }

    @Override
    public Object buildTree(List<ElCategoryDto> elCategoryDtos) {
        Set<ElCategoryDto> trees = new LinkedHashSet<>();
        Set<ElCategoryDto> categorys= new LinkedHashSet<>();
        List<String> deptNames = elCategoryDtos.stream().map(ElCategoryDto::getName).collect(Collectors.toList());
        boolean isChild;
        for (ElCategoryDto categoryDto : elCategoryDtos) {
            isChild = false;
            if (categoryDto.getPid() == null) {
                trees.add(categoryDto);
            }
            for (ElCategoryDto it : elCategoryDtos) {
                if (it.getPid() != null && categoryDto.getId().equals(it.getPid())) {
                    isChild = true;
                    if (categoryDto.getChildren() == null) {
                        categoryDto.setChildren(new ArrayList<>());
                    }
                    categoryDto.getChildren().add(it);
                }
            }
            if(isChild) {
                categorys.add(categoryDto);
            } else if(categoryDto.getPid() != null &&  !deptNames.contains(findById(categoryDto.getPid()).getName())) {
                categorys.add(categoryDto);
            }
        }

        if (CollectionUtil.isEmpty(trees)) {
            trees = categorys;
        }
        Map<String,Object> map = new HashMap<>(2);
        map.put("totalElements", elCategoryDtos.size());
        map.put("content",CollectionUtil.isEmpty(trees)? elCategoryDtos :trees);
        return map;
    }

    @Override
    public List<ElementCategory> findByPid(Long pid) {
        return elementCategoryRepository.findByPid(pid);
    }

    @Override
    public Set<ElCategoryDto> getDeleteElCategorys(List<ElementCategory> categoryList, Set<ElCategoryDto> elCategoryDtos) {
        for (ElementCategory category : categoryList) {
            elCategoryDtos.add(elCategoryMapper.toDto(category));
            List<ElementCategory> elementCategories = elementCategoryRepository.findByPid(category.getId());
            if(elementCategories!=null && elementCategories.size()!=0){
                getDeleteElCategorys(elementCategories, elCategoryDtos);
            }
        }
        return elCategoryDtos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<ElCategoryDto> elCategoryDtos) {
        for (ElCategoryDto categoryDto : elCategoryDtos) {
            if(categoryDto.getSubCount()>0){
                throw new BadRequestException("存在子节点，不能删除");
            }
            elementCategoryRepository.deleteById(categoryDto.getId());
            updateSubCnt(categoryDto.getPid());
        }
    }

    private List<ElCategoryDto> deduplication(List<ElCategoryDto> list) {
        List<ElCategoryDto> elCategoryDtos = new ArrayList<>();
        for (ElCategoryDto categoryDto : list) {
            boolean flag = true;
            for (ElCategoryDto dto : list) {
                if (dto.getId().equals(categoryDto.getPid())) {
                    flag = false;
                    break;
                }
            }
            if (flag){
                elCategoryDtos.add(categoryDto);
            }
        }
        return elCategoryDtos;
    }

    private void updateSubCnt(Long categoryId){
        if(categoryId != null){
            int count = elementCategoryRepository.countByPid(categoryId);
            elementCategoryRepository.updateSubCntById(count, categoryId);
        }
    }
}