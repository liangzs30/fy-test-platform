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
import java.util.List;

/**
* @website https://eladmin.vip
* @description /
* @author liangzisheng
* @date 2024-11-14
**/
@Entity
@Getter
@Setter
@Table(name="key_word")
public class Kw extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`kw_id`")
    @ApiModelProperty(value = "ID")
    private Long id;

    @OneToOne
    @JoinColumn(name = "category_id")
    @ApiModelProperty(value = "关键字模块")
    private KwCategory category;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "`kw_id`")
    @OrderBy("id ASC")
    private List<KwParamConf> kwParamConfs;

    @Column(name = "`name`",nullable = false, unique = true)
    @NotBlank
    @ApiModelProperty(value = "关键字名称")
    private String name;

    @Column(name = "`class_name`")
    @ApiModelProperty(value = "module路径")
    private String className;

    @Column(name = "`func_name`")
    @ApiModelProperty(value = "函数名称")
    private String funcName;

    @Column(name = "`if_resp`")
    @ApiModelProperty(value = "是否有返回值")
    private Boolean ifResp;

    @Column(name = "`description`")
    @ApiModelProperty(value = "关键字说明")
    private String desc;

    public void copy(Kw source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
