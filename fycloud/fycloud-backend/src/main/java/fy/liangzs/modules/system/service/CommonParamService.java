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

import fy.liangzs.modules.system.domain.CommonParam;
import fy.liangzs.modules.system.service.dto.CommonParamDto;
import fy.liangzs.modules.system.service.dto.CommonParamQueryCriteria;
import fy.liangzs.modules.system.service.dto.CommonParamSmallDto;
import fy.liangzs.utils.PageResult;
import org.springframework.data.domain.Pageable;

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
public interface CommonParamService {

    /**
    * 查询数据分页
    * @param criteria 条件
    * @param pageable 分页参数
    * @return Map<String,Object>
    */
    PageResult<CommonParamDto> queryAll(CommonParamQueryCriteria criteria, Pageable pageable);

    /**
     * 查询所有数据不分页
     * @param criteria 条件参数
     * @return List<CaseCategoryDto>
     */
    List<CommonParamDto> queryAll(CommonParamQueryCriteria criteria, Boolean isQuery);

    /**
     * 根据ID查询
     * @param pId ID
     * @return KwCategoryDto
     */
    CommonParamDto findById(Long pId);

    /**
    * 创建
    * @param resources /
    */
    void create(CommonParam resources);

    /**
    * 编辑
    * @param resources /
    */
    void update(CommonParam resources);

    /**
    * 多选删除
    * @param ids /
    */
    void deleteAll(Set<Long> ids);

    /**
     * 导出数据
     * @param all 待导出的数据
     * @param response /
     * @throws IOException /
     */
    void download(List<CommonParamDto> all, HttpServletResponse response) throws IOException;

    List<CommonParamSmallDto> queryByEnvId(Long envId);

}