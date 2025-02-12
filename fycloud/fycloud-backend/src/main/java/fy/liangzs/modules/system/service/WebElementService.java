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

import fy.liangzs.modules.system.domain.WebElement;
import fy.liangzs.modules.system.service.dto.WebElementDto;
import fy.liangzs.modules.system.service.dto.WebElementQueryCriteria;
import fy.liangzs.modules.system.service.dto.WebElementSmallDto;
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
public interface WebElementService {

    /**
    * 查询数据分页
    * @param criteria 条件
    * @param pageable 分页参数
    * @return Map<String,Object>
    */
    PageResult<WebElementDto> queryAll(WebElementQueryCriteria criteria, Pageable pageable);

    /***
     * 根据项目ID查询
     * @param id ID
     * @return
     *
     */
    List<WebElementSmallDto> queryByProjectID(Long id);

    /**
     * 根据ID查询
     * @param id ID
     * @return WebElementDto
     */
    WebElementDto findById(Long id);

    /**
    * 创建
    * @param resources /
    */
    void create(WebElement resources);

    /**
    * 编辑
    * @param resources /
    */
    void update(WebElement resources);

    /**
    * 多选删除
    * @param ids /
    */
    void deleteAll(Set<Long> ids);
}