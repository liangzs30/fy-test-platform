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
import fy.liangzs.annotation.DataPermission;
import fy.liangzs.annotation.Query;

import java.sql.Timestamp;
import java.util.List;

/**
* @website https://eladmin.vip
* @author liangzisheng
* @date 2024-11-14
**/
@Data
@DataPermission(fieldName = "id")
public class KwQueryCriteria {

    @Query
    private Long id;

    @Query(blurry = "name")
    private String blurry;

    @Query(joinName = "category", propName = "id")
    private Long categoryId;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;

}