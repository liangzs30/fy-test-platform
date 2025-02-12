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
package fy.liangzs.modules.system.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import fy.liangzs.base.BaseEntity;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
* @website https://eladmin.vip
* @description /
* @author liangzisheng
* @date 2024-11-14
**/
@Entity
@Getter
@Setter
@Table(name="test_exe_machine")
public class ExecuteMachine extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`m_id`")
    @ApiModelProperty(value = "ID")
    private Long id;

    @Column(name = "`name`")
    @ApiModelProperty(value = "机器名称")
    private String name;

    @Column(name = "`ip_addr`")
    @ApiModelProperty(value = "所在ip地址")
    private String ipAddr;

    @Column(name = "`status`",nullable = false)
    @NotBlank
    @ApiModelProperty(value = "状态")
    private Integer status;

    @Column(name = "`unique_code`", unique = true)
    @ApiModelProperty(value = "唯一码")
    private String uniqueCode;

    @Column(name = "`project_id`")
    @ApiModelProperty(value = "项目ID")
    private Long projectID;

    @Column(name = "`t_serial`")
    @ApiModelProperty(value = "唯一码")
    private String terminalSerial;

    public void copy(ExecuteMachine source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
