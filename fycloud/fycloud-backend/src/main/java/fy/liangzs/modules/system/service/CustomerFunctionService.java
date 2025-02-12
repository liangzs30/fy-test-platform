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

import fy.liangzs.modules.system.domain.CustomerFunction;
import fy.liangzs.modules.system.service.dto.CustomerFunctionDto;
import fy.liangzs.modules.system.service.dto.CustomerFunctionQueryCriteria;
import fy.liangzs.utils.PageResult;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

/**
* @website https://eladmin.vip
* @description 服务接口
* @author liangzisheng
* @date 2024-11-14
**/
public interface CustomerFunctionService {

    /**
    * 查询数据分页
    * @param criteria 条件
    * @param pageable 分页参数
    * @return Map<String,Object>
    */
    PageResult<CustomerFunctionDto> queryAll(CustomerFunctionQueryCriteria criteria, Pageable pageable);

    /**
     * 查询所有数据不分页
     * @param criteria 条件参数
     * @return List<CaseCategoryDto>
     */
    List<CustomerFunctionDto> queryAll(CustomerFunctionQueryCriteria criteria, Boolean isQuery);

    /**
     * 根据ID查询
     * @param pId ID
     * @return KwCategoryDto
     */
    CustomerFunctionDto findById(Long pId);

    /**
    * 创建
    * @param resources /
    */
    void create(CustomerFunction resources);

    /**
    * 编辑
    * @param resources /
    */
    void update(CustomerFunction resources);

    /**
    * 多选删除
    * @param ids /
    */
    void deleteAll(Set<Long> ids);
}