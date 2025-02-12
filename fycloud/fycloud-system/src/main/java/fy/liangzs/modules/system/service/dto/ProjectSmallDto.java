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

import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import fy.liangzs.base.BaseDTO;
import org.apache.poi.ss.formula.functions.T;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * @author liangzisheng
 * @date 2018-11-23
 */
@Getter
@Setter
public class ProjectSmallDto extends BaseDTO implements Serializable {

    private Long id;

    private String name;

    private String description;

    private Integer runCount;

    private Integer caseCount;

    private Integer todayIncrease;

    private List<Integer[]> sevenRunStatic;

    private String[] errorTopNameStatic;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProjectSmallDto projectDto = (ProjectSmallDto) o;
        return Objects.equals(id, projectDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
