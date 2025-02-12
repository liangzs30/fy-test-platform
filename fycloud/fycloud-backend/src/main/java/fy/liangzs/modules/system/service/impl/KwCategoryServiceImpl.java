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
import fy.liangzs.utils.FileUtil;
import fy.liangzs.utils.QueryHelp;
import fy.liangzs.utils.SecurityUtils;
import fy.liangzs.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import fy.liangzs.exception.BadRequestException;
import fy.liangzs.modules.system.repository.KwCategoryRepository;
import fy.liangzs.modules.system.service.KwCategoryService;
import fy.liangzs.modules.system.service.dto.KwCategoryDto;
import fy.liangzs.modules.system.service.dto.KwCategoryQueryCriteria;
import fy.liangzs.modules.system.service.mapstruct.KwCategoryMapper;
import fy.liangzs.modules.system.domain.KwCategory;
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
public class KwCategoryServiceImpl implements KwCategoryService {

    private final KwCategoryRepository kwCategoryRepository;
    private final KwCategoryMapper kwCategoryMapper;

    @Override
    public List<KwCategoryDto> queryAll(KwCategoryQueryCriteria criteria) {
        Sort sort = Sort.by(Sort.Direction.ASC, "categorySort");
        if(criteria.getPid()==null && criteria.getName()==null){
            criteria.setPidIsNull(true);
        }
        return kwCategoryMapper.toDto(kwCategoryRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),sort));
    }

    @Override
    @Transactional
    public KwCategoryDto findById(Long categoryId) {
        KwCategory kwCategory = kwCategoryRepository.findById(categoryId).orElseGet(KwCategory::new);
        ValidationUtil.isNull(kwCategory.getId(),"KwCategory","categoryId",categoryId);
        return kwCategoryMapper.toDto(kwCategory);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(KwCategory resources) {
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        resources.setCreateBy(userDetails.getUsername());
        resources.setUpdateBy(userDetails.getUsername());
        kwCategoryRepository.save(resources);
        // 计算子节点数目
        resources.setSubCount(0);
        // 清理缓存
        updateSubCnt(resources.getPid());
        // 清理自定义角色权限的datascope缓存
//        delCaches(resources.getPid());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(KwCategory resources) {
//        KwCategory KwCategory = kwCategoryRepository.findById(resources.getId()).orElseGet(KwCategory::new);
//        ValidationUtil.isNull( kwCategory.getId(),"KwCategory","id",resources.getId());
//        kwCategory.copy(resources);
//        kwCategoryRepository.save(KwCategory);
        // 旧的模块
        Long oldPid = findById(resources.getId()).getPid();
        Long newPid = resources.getPid();
        if(resources.getPid() != null && resources.getId().equals(resources.getPid())) {
            throw new BadRequestException("上级不能为自己");
        }
        KwCategory category = kwCategoryRepository.findById(resources.getId()).orElseGet(KwCategory::new);
        ValidationUtil.isNull( category.getId(),"KwCategory","id",resources.getId());
        resources.setId(category.getId());
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        resources.setUpdateBy(userDetails.getUsername());
        kwCategoryRepository.save(resources);
        // 更新父节点中子节点数目
        updateSubCnt(oldPid);
        updateSubCnt(newPid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(Long[] ids) {
        for (Long categoryId : ids) {
            KwCategoryDto KwCategoryDto = findById(categoryId);
            if(KwCategoryDto.getSubCount()>0){
                throw new BadRequestException("存在子节点，不能删除");
            }
            kwCategoryRepository.deleteById(categoryId);
            updateSubCnt(KwCategoryDto.getPid());
        }
    }

    @Override
    public void download(List<KwCategoryDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (KwCategoryDto kwCategory : all) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("上级模块", kwCategory.getPid());
            map.put("子模块数目", kwCategory.getSubCount());
            map.put("名称", kwCategory.getName());
            map.put("排序", kwCategory.getCategorySort());
            map.put("创建者", kwCategory.getCreateBy());
            map.put("更新者", kwCategory.getUpdateBy());
            map.put("创建日期", kwCategory.getCreateTime());
            map.put("更新时间", kwCategory.getUpdateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public List<KwCategoryDto> getSuperior(KwCategoryDto KwCategoryDto, List<KwCategory> KwCategories) {
        if(KwCategoryDto.getPid() == null){
            KwCategories.addAll(kwCategoryRepository.findByPidIsNull());
            return kwCategoryMapper.toDto(KwCategories);
        }
        KwCategories.addAll(kwCategoryRepository.findByPid(KwCategoryDto.getPid()));
        return getSuperior(findById(KwCategoryDto.getPid()), KwCategories);
    }

    @Override
    public Object buildTree(List<KwCategoryDto> KwCategoryDtos) {
        Set<KwCategoryDto> trees = new LinkedHashSet<>();
        Set<KwCategoryDto> categorys= new LinkedHashSet<>();
        List<String> deptNames = KwCategoryDtos.stream().map(KwCategoryDto::getName).collect(Collectors.toList());
        boolean isChild;
        for (KwCategoryDto categoryDto : KwCategoryDtos) {
            isChild = false;
            if (categoryDto.getPid() == null) {
                trees.add(categoryDto);
            }
            for (KwCategoryDto it : KwCategoryDtos) {
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
        map.put("totalElements",KwCategoryDtos.size());
        map.put("content",CollectionUtil.isEmpty(trees)? KwCategoryDtos :trees);
        return map;
    }

    @Override
    public List<KwCategory> findByPid(Long pid) {
        return kwCategoryRepository.findByPid(pid);
    }

    @Override
    public Set<KwCategoryDto> getDeleteKwCategorys(List<KwCategory> categoryList, Set<KwCategoryDto> KwCategoryDtos) {
        for (KwCategory category : categoryList) {
            KwCategoryDtos.add(kwCategoryMapper.toDto(category));
            List<KwCategory> KwCategories = kwCategoryRepository.findByPid(category.getId());
            if(KwCategories!=null && KwCategories.size()!=0){
                getDeleteKwCategorys(KwCategories, KwCategoryDtos);
            }
        }
        return KwCategoryDtos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<KwCategoryDto> KwCategoryDtos) {
        for (KwCategoryDto categoryDto : KwCategoryDtos) {
            if(categoryDto.getSubCount()>0){
                throw new BadRequestException("存在子节点，不能删除");
            } else if (kwCategoryRepository.countByKw(categoryDto.getId())>0) {
                throw new BadRequestException("节点存在关键字，不能删除");
            }
            kwCategoryRepository.deleteById(categoryDto.getId());
            updateSubCnt(categoryDto.getPid());
        }
    }

    private List<KwCategoryDto> deduplication(List<KwCategoryDto> list) {
        List<KwCategoryDto> KwCategoryDtos = new ArrayList<>();
        for (KwCategoryDto categoryDto : list) {
            boolean flag = true;
            for (KwCategoryDto dto : list) {
                if (dto.getId().equals(categoryDto.getPid())) {
                    flag = false;
                    break;
                }
            }
            if (flag){
                KwCategoryDtos.add(categoryDto);
            }
        }
        return KwCategoryDtos;
    }

    private void updateSubCnt(Long KwCategoryId){
        if(KwCategoryId != null){
            int count = kwCategoryRepository.countByPid(KwCategoryId);
            kwCategoryRepository.updateSubCntById(count, KwCategoryId);
        }
    }
}