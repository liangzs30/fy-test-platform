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
import fy.liangzs.exception.BadRequestException;
import fy.liangzs.modules.system.repository.CaseCategoryRepository;
import fy.liangzs.modules.system.service.CaseCategoryService;
import fy.liangzs.modules.system.service.dto.CaseCategoryDto;
import fy.liangzs.modules.system.service.mapstruct.CaseCategoryMapper;
import fy.liangzs.modules.system.domain.CaseCategory;
import fy.liangzs.utils.FileUtil;
import fy.liangzs.utils.QueryHelp;
import fy.liangzs.utils.SecurityUtils;
import fy.liangzs.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import fy.liangzs.modules.system.service.dto.CaseCategoryQueryCriteria;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.io.IOException;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;

/**
* @website https://eladmin.vip
* @description 服务实现
* @author liangzisheng
* @date 2024-11-14
**/
@Service
@RequiredArgsConstructor
public class CaseCategoryServiceImpl implements CaseCategoryService {

    private final CaseCategoryRepository caseCategoryRepository;
    private final CaseCategoryMapper caseCategoryMapper;

    @Override
    public List<CaseCategoryDto> queryAll(CaseCategoryQueryCriteria criteria){
        Sort sort = Sort.by(Sort.Direction.ASC, "categorySort");
        if(criteria.getPid()==null && criteria.getName()==null){
            criteria.setPidIsNull(true);
        }
        return caseCategoryMapper.toDto(caseCategoryRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),sort));
    }

    @Override
    @Transactional
    public CaseCategoryDto findById(Long categoryId) {
        CaseCategory caseCategory = caseCategoryRepository.findById(categoryId).orElseGet(CaseCategory::new);
        ValidationUtil.isNull(caseCategory.getId(),"CaseCategory","categoryId",categoryId);
        return caseCategoryMapper.toDto(caseCategory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(CaseCategory resources) {
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        resources.setCreateBy(userDetails.getUsername());
        resources.setUpdateBy(userDetails.getUsername());
        caseCategoryRepository.save(resources);
        // 计算子节点数目
        resources.setSubCount(0);
        // 清理缓存
        updateSubCnt(resources.getPid());
        // 清理自定义角色权限的datascope缓存
//        delCaches(resources.getPid());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CaseCategory resources) {
//        CaseCategory caseCategory = caseCategoryRepository.findById(resources.getId()).orElseGet(CaseCategory::new);
//        ValidationUtil.isNull( caseCategory.getId(),"CaseCategory","id",resources.getId());
//        caseCategory.copy(resources);
//        caseCategoryRepository.save(caseCategory);
        // 旧的模块
        Long oldPid = findById(resources.getId()).getPid();
        Long newPid = resources.getPid();
        if(resources.getPid() != null && resources.getId().equals(resources.getPid())) {
            throw new BadRequestException("上级不能为自己");
        }
        CaseCategory category = caseCategoryRepository.findById(resources.getId()).orElseGet(CaseCategory::new);
        ValidationUtil.isNull( category.getId(),"CaseCategory","id",resources.getId());
        resources.setId(category.getId());
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        resources.setUpdateBy(userDetails.getUsername());
        caseCategoryRepository.save(resources);
        // 更新父节点中子节点数目
        updateSubCnt(oldPid);
        updateSubCnt(newPid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(Long[] ids) {
        for (Long categoryId : ids) {
            CaseCategoryDto caseCategoryDto = findById(categoryId);
            if(caseCategoryDto.getSubCount()>0){
                throw new BadRequestException("存在子节点，不能删除");
            }
            caseCategoryRepository.deleteById(categoryId);
            updateSubCnt(caseCategoryDto.getPid());
        }
    }

    @Override
    public void download(List<CaseCategoryDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (CaseCategoryDto caseCategory : all) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("上级模块", caseCategory.getPid());
            map.put("子模块数目", caseCategory.getSubCount());
            map.put("名称", caseCategory.getName());
            map.put("排序", caseCategory.getCategorySort());
            map.put("创建者", caseCategory.getCreateBy());
            map.put("更新者", caseCategory.getUpdateBy());
            map.put("创建日期", caseCategory.getCreateTime());
            map.put("更新时间", caseCategory.getUpdateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public List<CaseCategoryDto> getSuperior(CaseCategoryDto caseCategoryDto, List<CaseCategory> caseCategories) {
        if(caseCategoryDto.getPid() == null){
            caseCategories.addAll(caseCategoryRepository.findByPidIsNull());
            return caseCategoryMapper.toDto(caseCategories);
        }
        caseCategories.addAll(caseCategoryRepository.findByPid(caseCategoryDto.getPid()));
        return getSuperior(findById(caseCategoryDto.getPid()), caseCategories);
    }

    @Override
    public Object buildTree(List<CaseCategoryDto> caseCategoryDtos) {
        Set<CaseCategoryDto> trees = new LinkedHashSet<>();
        Set<CaseCategoryDto> categorys= new LinkedHashSet<>();
        List<String> deptNames = caseCategoryDtos.stream().map(CaseCategoryDto::getName).collect(Collectors.toList());
        boolean isChild;
        for (CaseCategoryDto categoryDto : caseCategoryDtos) {
            isChild = false;
            if (categoryDto.getPid() == null) {
                trees.add(categoryDto);
            }
            for (CaseCategoryDto it : caseCategoryDtos) {
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
        map.put("totalElements",caseCategoryDtos.size());
        map.put("content",CollectionUtil.isEmpty(trees)? caseCategoryDtos :trees);
        return map;
    }

    @Override
    public List<CaseCategory> findByPid(Long pid) {
        return caseCategoryRepository.findByPid(pid);
    }

    @Override
    public Set<CaseCategoryDto> getDeleteCaseCategorys(List<CaseCategory> categoryList, Set<CaseCategoryDto> caseCategoryDtos) {
        for (CaseCategory category : categoryList) {
            caseCategoryDtos.add(caseCategoryMapper.toDto(category));
            List<CaseCategory> caseCategories = caseCategoryRepository.findByPid(category.getId());
            if(caseCategories!=null && caseCategories.size()!=0){
                getDeleteCaseCategorys(caseCategories, caseCategoryDtos);
            }
        }
        return caseCategoryDtos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<CaseCategoryDto> caseCategoryDtos) {
        for (CaseCategoryDto categoryDto : caseCategoryDtos) {
            if(categoryDto.getSubCount()>0){
                throw new BadRequestException("存在子节点，不能删除");
            }
            caseCategoryRepository.deleteById(categoryDto.getId());
            updateSubCnt(categoryDto.getPid());
        }
    }

    private List<CaseCategoryDto> deduplication(List<CaseCategoryDto> list) {
        List<CaseCategoryDto> caseCategoryDtos = new ArrayList<>();
        for (CaseCategoryDto categoryDto : list) {
            boolean flag = true;
            for (CaseCategoryDto dto : list) {
                if (dto.getId().equals(categoryDto.getPid())) {
                    flag = false;
                    break;
                }
            }
            if (flag){
                caseCategoryDtos.add(categoryDto);
            }
        }
        return caseCategoryDtos;
    }

    private void updateSubCnt(Long caseCategoryId){
        if(caseCategoryId != null){
            int count = caseCategoryRepository.countByPid(caseCategoryId);
            caseCategoryRepository.updateSubCntById(count, caseCategoryId);
        }
    }
}