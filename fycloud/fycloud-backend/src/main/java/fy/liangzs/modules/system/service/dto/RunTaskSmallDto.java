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
package fy.liangzs.modules.system.service.dto;

import lombok.Data;
import fy.liangzs.base.BaseDTO;
import fy.liangzs.modules.system.domain.ExecuteMachine;
import fy.liangzs.modules.system.domain.TestEnv;

import java.io.Serializable;
import java.util.Objects;

/**
* @website https://eladmin.vip
* @description /
* @author liangzisheng
* @date 2024-11-14
**/
@Data
public class RunTaskSmallDto extends BaseDTO implements Serializable {

    /** ID */
    private Long id;

    private String name;

    private String runType;

    private TestEnv runEnv;

    private String status;

    private Long startTime;

    private Long endTime;

    /** 项目ID */
    private Long projectID;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RunTaskSmallDto runTaskDto = (RunTaskSmallDto) o;
        return Objects.equals(id, runTaskDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}