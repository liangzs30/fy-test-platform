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
import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import fy.liangzs.base.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
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
@Table(name="test_step")
public class TestStep extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`step_id`")
    @ApiModelProperty(value = "ID")
    private Long id;

    @Column(name = "`case_id`")
    @ApiModelProperty(value = "用例ID")
    private Long caseId;

    @ManyToOne
    @JoinColumn(name = "`kw_id`")
    @ApiModelProperty(value = "关键字ID")
    private Kw kw;

    @Column(name = "`step_param`", nullable = false)
    @JSONField(serialize = false)
    @NotBlank
    @ApiModelProperty(value = "步骤参数")
    private String stepParam;

    @Column(name = "`step_resps`", nullable = false)
    @JSONField(serialize = false)
    @NotBlank
    @ApiModelProperty(value = "提取返回值")
    private String stepResps;

    @Column(name = "`description`")
    @ApiModelProperty(value = "步骤说明")
    private String desc;

    @Column(name = "`step_sort`")
    @ApiModelProperty(value = "步骤排序")
    private Long stepSort;

    @Column(name = "`enabled`")
    @NotNull
    @ApiModelProperty(value = "是否禁用")
    private Boolean enabled;

    public void copy(TestStep source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
