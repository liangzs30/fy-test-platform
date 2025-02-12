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
@Table(name="test_sys_param")
public class CommonParam extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`sp_id`")
    @ApiModelProperty(value = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "`env_id`")
    @ApiModelProperty(value = "环境关键字ID")
    private TestEnv testEnv;

    @Column(name = "`name`")
    @ApiModelProperty(value = "参数名称")
    private String name;

    @Column(name = "`value`")
    @ApiModelProperty(value = "参数值")
    private String value;

    @Column(name = "`description`")
    @ApiModelProperty(value = "参数说明")
    private String desc;

    @Column(name = "`project_id`")
    @ApiModelProperty(value = "项目ID")
    private Long projectID;

    public void copy(CommonParam source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
