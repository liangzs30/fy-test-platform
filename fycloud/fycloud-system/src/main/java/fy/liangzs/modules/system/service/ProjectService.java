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

import fy.liangzs.modules.system.domain.Project;
import fy.liangzs.modules.system.service.dto.ProjectDto;
import fy.liangzs.modules.system.service.dto.ProjectQueryCriteria;
import fy.liangzs.modules.system.service.dto.ProjectSmallDto;
import fy.liangzs.modules.system.service.dto.*;
import fy.liangzs.utils.PageResult;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;

/**
 * @author Zheng Jie
 * @date 2018-12-03
 */
public interface ProjectService {

    /**
     * 查询全部数据
     * @return /
     */
    List<ProjectDto> queryAll();
    /**
     * 根据ID查询
     * @param id /
     * @return /
     */
    ProjectDto findById(long id);

    /**
     * 创建
     * @param resources /
     */
    void create(Project resources);

    /**
     * 编辑
     * @param resources /
     */
    void update(Project resources);

    /**
     * 删除
     * @param ids /
     */
    void delete(Set<Long> ids);

    /**
     * 待条件分页查询
     * @param criteria 条件
     * @param pageable 分页参数
     * @return /
     */
    PageResult<ProjectDto> queryAll(ProjectQueryCriteria criteria, Pageable pageable);

    /**
     * 查询全部
     * @param criteria 条件
     * @return /
     */
    List<ProjectDto> queryAll(ProjectQueryCriteria criteria);

    List<ProjectDto> queryByUser(Long userId);

    ProjectSmallDto queryLastByUser(Long userId);

    Project getById(Long id);

    ProjectSmallDto saveLast(Long userID, Long projectID);

    void deleteLast(Long userId);
}
