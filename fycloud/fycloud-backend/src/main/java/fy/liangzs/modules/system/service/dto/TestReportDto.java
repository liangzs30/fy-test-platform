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
import fy.liangzs.modules.system.domain.TestReportLog;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

/**
* @website https://eladmin.vip
* @description /
* @author liangzisheng
* @date 2024-11-14
**/
@Data
public class TestReportDto extends BaseDTO implements Serializable {

    /** ID */
    private Long id;

    /** ID */
    private Long runTaskId;

    private String taskName;

    private Integer total;

    private Integer successCount;

    private Integer failCount;

    private Integer errorCount;

    private Integer progress;

    /** 执行耗时 */
    private String costTime;

    private String status;

    private Integer result;

    private Timestamp startTime;

    private Timestamp endTime;

    /** 执行记录 */
    List<TestReportLog> testReportLogList;

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
        TestReportDto testReportDto = (TestReportDto) o;
        return Objects.equals(id, testReportDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}