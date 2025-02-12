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
import fy.liangzs.modules.system.domain.ExecuteMachine;
import fy.liangzs.modules.system.domain.StopRunTask;
import fy.liangzs.modules.system.service.dto.ExecuteMachineDto;
import fy.liangzs.modules.system.service.dto.ExecuteMachineQueryCriteria;
import fy.liangzs.modules.system.service.dto.MachineRunTaskDto;
import fy.liangzs.modules.system.service.dto.MachineRunTaskQueryCriteria;
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
public interface ExecuteMachineService {

    /**
    * 查询数据分页
    * @param criteria 条件
    * @param pageable 分页参数
    * @return Map<String,Object>
    */
    PageResult<ExecuteMachineDto> queryAll(ExecuteMachineQueryCriteria criteria, Pageable pageable);

    /**
     * 查询所有数据不分页
     * @param criteria 条件参数
     * @return List<CaseCategoryDto>
     */
    List<ExecuteMachineDto> queryAll(ExecuteMachineQueryCriteria criteria, Boolean isQuery);

    /**
     * 查询执行机在执行和未执行的任务集合
     * @param criteria, pageable 条件参数
     * @return List<CaseCategoryDto>
     */
    PageResult<MachineRunTaskDto> queryMachineTask(MachineRunTaskQueryCriteria criteria, Pageable pageable);

    /**
     * 根据ID查询
     * @param mId ID
     * @return CaseCategoryDto
     */
    ExecuteMachineDto findById(Long mId);

    /**
    * 创建
    * @param resources /
    */
    void create(ExecuteMachine resources);

    /**
    * 编辑
    * @param mac/
    */
    void update(JSONObject mac);

    /**
    * 多选删除
    * @param ids /
    */
    void deleteAll(Set<Long> ids);


    void delete(Set<ExecuteMachineDto> executeMachineDtos);

    ExecuteMachine getByUniqueCode(String uniqueCode);

    List<ExecuteMachine> queryAllMachine();

    void machineOffLine(String uniqeCode, String user);

    void cancelTaskById(Long taskId);

    void cancelAllTask(Set<Long> ids);

    void stopTask(StopRunTask stopRunTask);
}