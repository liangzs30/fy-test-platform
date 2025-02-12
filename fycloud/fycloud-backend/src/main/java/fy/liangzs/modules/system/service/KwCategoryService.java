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

import fy.liangzs.modules.system.service.dto.KwCategoryDto;
import fy.liangzs.modules.system.service.dto.KwCategoryQueryCriteria;
import fy.liangzs.modules.system.domain.KwCategory;

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
public interface KwCategoryService {

    /**
    * 查询数据分页
    * @param criteria 条件
    * @return Map<String,Object>
    */
    List<KwCategoryDto> queryAll(KwCategoryQueryCriteria criteria);


    /**
     * 根据ID查询
     * @param categoryId ID
     * @return KwCategoryDto
     */
    KwCategoryDto findById(Long categoryId);

    /**
    * 创建
    * @param resources /
    */
    void create(KwCategory resources);

    /**
    * 编辑
    * @param resources /
    */
    void update(KwCategory resources);

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
    void download(List<KwCategoryDto> all, HttpServletResponse response) throws IOException;

    /**
     * 根据ID获取同级与上级数据
     * @param KwCategoryDto /
     * @param KwCategories /
     * @return /
     */
    List<KwCategoryDto> getSuperior(KwCategoryDto KwCategoryDto, List<KwCategory> KwCategories);

    /**
     * 构建树形数据
     * @param KwCategoryDtos /
     * @return /
     */
    Object buildTree(List<KwCategoryDto> KwCategoryDtos);

    List<KwCategory> findByPid(Long id);

    Set<KwCategoryDto> getDeleteKwCategorys(List<KwCategory> categoryList, Set<KwCategoryDto> KwCategoryDtos);

    void delete(Set<KwCategoryDto> KwCategoryDtos);
}