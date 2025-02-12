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
package fy.liangzs.modules.system.service;

import fy.liangzs.modules.system.domain.ElementCategory;
import fy.liangzs.modules.system.service.dto.ElCategoryDto;
import fy.liangzs.modules.system.service.dto.ElCategoryQueryCriteria;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
* @website https://eladmin.vip
* @description 服务接口
* @author liangzisheng
* @date 2024-11-14
**/
public interface ElCategoryService {
    /**
    * 查询所有数据不分页
    * @param criteria 条件参数
    * @return List<CaseCategoryDto>
    */
    List<ElCategoryDto> queryAll(ElCategoryQueryCriteria criteria);

    /**
     * 根据ID查询
     * @param categoryId ID
     * @return CaseCategoryDto
     */
    ElCategoryDto findById(Long categoryId);

    /**
    * 创建
    * @param resources /
    */
    void create(ElementCategory resources);

    /**
    * 编辑
    * @param resources /
    */
    void update(ElementCategory resources);

    /**
    * 多选删除
    * @param ids /
    */
    void deleteAll(Long[] ids);

    /**
    * 导出数据
    * @param all 待导出的数据
    * @param response /
    * @throws IOException /
    */
    void download(List<ElCategoryDto> all, HttpServletResponse response) throws IOException;

    /**
     * 根据ID获取同级与上级数据
     * @param elCategoryDto /
     * @param elementCategories /
     * @return /
     */
    List<ElCategoryDto> getSuperior(ElCategoryDto elCategoryDto, List<ElementCategory> elementCategories);

    /**
     * 构建树形数据
     * @param elCategoryDtos /
     * @return /
     */
    Object buildTree(List<ElCategoryDto> elCategoryDtos);

    List<ElementCategory> findByPid(Long id);

    Set<ElCategoryDto> getDeleteElCategorys(List<ElementCategory> categoryList, Set<ElCategoryDto> elCategoryDtos);

    void delete(Set<ElCategoryDto> elCategoryDtos);
}