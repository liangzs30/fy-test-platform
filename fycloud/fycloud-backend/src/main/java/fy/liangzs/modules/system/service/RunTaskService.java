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

import com.alibaba.fastjson.JSONObject;
import fy.liangzs.modules.system.domain.RunTask;
import fy.liangzs.modules.system.service.dto.*;
import fy.liangzs.utils.PageResult;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
* @website https://eladmin.vip
* @description 服务接口
* @author liangzisheng
* @date 2024-11-14
**/
public interface RunTaskService {
    PageResult<RunTaskDto> queryAll(RunTaskQueryCriteria criteria, Pageable pageable);
    /**
     * 查询所有数据不分页
     * @param criteria 条件参数
     * @return List<CaseCategoryDto>
     */
    List<RunTaskDto> queryAll(RunTaskQueryCriteria criteria, Boolean isQuery);

    PageResult<MachineRunTaskDto> queryMachineAll(MachineRunTaskQueryCriteria criteria, Pageable pageable);
    /**
     * 根据ID查询
     * @param tId ID
     * @return CaseCategoryDto
     */
    RunTaskDto findById(Long tId);

    /**
    * 创建
    * @param resources /
    */
    Long create(RunTask resources);

    void createScdJob(RunTask resources);

    void updateStatus(JSONObject task);

    List<TestCaseDtoSort> getRunTestCase(Long rId);

    List<RunTask> getPendingTaskByMachine(Long macId);

    void deleteById(Long id);
}