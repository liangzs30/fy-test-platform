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
import java.util.List;

/**
* @website https://eladmin.vip
* @description /测试任务
* @author liangzisheng
* @date 2024-11-14
**/
@Entity
@Getter
@Setter
@Table(name="test_task")
public class TestTask extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`task_id`")
    @ApiModelProperty(value = "ID")
    private Long id;

    @Column(name = "`name`")
    @ApiModelProperty(value = "任务名称")
    private String name;

    @Column(name = "`time_expression`")
    @ApiModelProperty(value = "定时表达式")
    private String tExpression;

    @Column(name = "`description`")
    @ApiModelProperty(value = "任务描述")
    private String desc;

    @ManyToOne
    @JoinColumn(name = "env_id")
    @ApiModelProperty(value = "环境")
    private TestEnv testEnv;

    @ManyToOne
    @JoinColumn(name = "machine_id")
    @ApiModelProperty(value = "执行机")
    private ExecuteMachine executeMachine;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "`task_id`")
    @OrderBy("executeSort ASC")
    private List<TaskCase> taskCases;

    @Column(name = "`project_id`")
    @ApiModelProperty(value = "项目ID")
    private Long projectID;

    @Column(name = "`sched_id`")
    @ApiModelProperty(value = "定时任务ID")
    private Long schedId;

    @ApiModelProperty(value = "状态，停用或启动")
    private Boolean isPause = false;

    @ApiModelProperty(value = "是否通知")
    private Boolean isNotify = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "tasks_notifications",
            joinColumns = {@JoinColumn(name = "task_id",referencedColumnName = "task_id")},
            inverseJoinColumns = {@JoinColumn(name = "nid",referencedColumnName = "id")})
    @ApiModelProperty(value = "通知", hidden = true)

    private List<Notification> notifications;

    public void copy(TestTask source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
