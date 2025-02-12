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

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import fy.liangzs.base.BaseDTO;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
* @website https://eladmin.vip
* @description /
* @author liangzisheng
* @date 2024-11-14
**/
@Data
public class TaskCaseDto extends BaseDTO implements Serializable {

    /** ID */
    private Long id;

    private Long taskId;

    private Long caseId;

    /** 模块 */
    private CaseCategorySmallDto category;

    private Long categoryId;

    private String caseNo;

    private String caseTitle;

    /** 用例说明 */
    private String desc;

    private Integer executeSort;

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
        TaskCaseDto taskCaseDto = (TaskCaseDto) o;
        return Objects.equals(id, taskCaseDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}