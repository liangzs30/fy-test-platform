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
import fy.liangzs.modules.system.domain.TestEnv;

import java.io.Serializable;

/**
* @website https://eladmin.vip
* @description /
* @author liangzisheng
* @date 2024-11-14
**/
@Data
public class CustomerFunctionDto extends BaseDTO implements Serializable {

    /** ID */
    private Long id;

    /** 名称 */
    private String name;

    private String callSample;

    /** 参数说明 */
    private String desc;
}