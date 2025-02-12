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

import fy.liangzs.modules.system.service.dto.CaseCategoryDto;
import fy.liangzs.modules.system.domain.CaseCategory;
import fy.liangzs.modules.system.service.dto.CaseCategoryQueryCriteria;
import java.util.List;
import java.io.IOException;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;

/**
* @website https://eladmin.vip
* @description 服务接口
* @author liangzisheng
* @date 2024-11-14
**/
public interface CaseCategoryService {
    /**
    * 查询所有数据不分页
    * @param criteria 条件参数
    * @return List<CaseCategoryDto>
    */
    List<CaseCategoryDto> queryAll(CaseCategoryQueryCriteria criteria);

    /**
     * 根据ID查询
     * @param categoryId ID
     * @return CaseCategoryDto
     */
    CaseCategoryDto findById(Long categoryId);

    /**
    * 创建
    * @param resources /
    */
    void create(CaseCategory resources);

    /**
    * 编辑
    * @param resources /
    */
    void update(CaseCategory resources);

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
    void download(List<CaseCategoryDto> all, HttpServletResponse response) throws IOException;

    /**
     * 根据ID获取同级与上级数据
     * @param caseCategoryDto /
     * @param caseCategories /
     * @return /
     */
    List<CaseCategoryDto> getSuperior(CaseCategoryDto caseCategoryDto, List<CaseCategory> caseCategories);

    /**
     * 构建树形数据
     * @param caseCategoryDtos /
     * @return /
     */
    Object buildTree(List<CaseCategoryDto> caseCategoryDtos);

    List<CaseCategory> findByPid(Long id);

    Set<CaseCategoryDto> getDeleteCaseCategorys(List<CaseCategory> categoryList, Set<CaseCategoryDto> caseCategoryDtos);

    void delete(Set<CaseCategoryDto> caseCategoryDtos);
}