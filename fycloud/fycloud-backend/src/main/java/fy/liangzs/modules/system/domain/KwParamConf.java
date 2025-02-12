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
import java.sql.Timestamp;

/**
* @website https://eladmin.vip
* @description /
* @author liangzisheng
* @date 2024-11-14
**/
@Entity
@Getter
@Setter
@Table(name="kw_param_conf")
public class KwParamConf extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`param_id`")
    @ApiModelProperty(value = "ID")
    private Long id;

    @Column(name = "`kw_id`")
    @ApiModelProperty(value = "关键字ID")
    private Long kwId;

    @Column(name = "`title`",nullable = false)
    @NotBlank
    @ApiModelProperty(value = "参数标题")
    private String title;

    @Column(name = "`name`",nullable = false)
    @NotBlank
    @ApiModelProperty(value = "参数名称")
    private String name;

    @Column(name = "`input_type`")
    @ApiModelProperty(value = "数据类型")
    private String inputType;

    @Column(name = "`pdict`")
    @ApiModelProperty(value = "下拉选项字典")
    private String pDict;

    @Column(name = "`description`")
    @ApiModelProperty(value = "参数说明")
    private String desc;

    public void copy(KwParamConf source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
