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
@Table(name="test_notification")
public class Notification extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @ApiModelProperty(value = "ID")
    private Long id;

    @Column(name = "`name`")
    @ApiModelProperty(value = "名称")
    private String name;

    @Column(name = "`type`")
    @ApiModelProperty(value = "通知类型")
    private Integer nType;

    @Column(name = "`webhook`")
    @ApiModelProperty(value = "通知地址")
    private String webhook;

    @Column(name = "`sender`")
    @ApiModelProperty(value = "发送者")
    private String sender;

    @Column(name = "`receiver`")
    @ApiModelProperty(value = "接收者")
    private String receiver;

    @Column(name = "`cc`")
    @ApiModelProperty(value = "抄送")
    private String cc;

    @Column(name = "`bcc`")
    @ApiModelProperty(value = "密送")
    private String bcc;

    @Column(name = "`project_id`")
    @ApiModelProperty(value = "项目ID")
    private Long projectID;

    public void copy(Notification source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
