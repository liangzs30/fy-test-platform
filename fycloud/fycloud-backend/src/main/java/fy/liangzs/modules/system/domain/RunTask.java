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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
* @website https://eladmin.vip
* @description /测试任务
* @author liangzisheng
* @date 2024-11-14
**/
@Entity
@Getter
@Setter
@Table(name="run_task")
public class RunTask implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @ApiModelProperty(value = "ID")
    private Long id;

    @Column(name = "`name`")
    @ApiModelProperty(value = "任务名称")
    private String name;

    @Column(name = "`run_type`")
    @ApiModelProperty(value = "执行类型")
    private String runType;

    @OneToOne
    @JoinColumn(name = "run_env")
    @ApiModelProperty(value = "执行环境")
    private TestEnv runEnv;

    @OneToOne
    @JoinColumn(name = "run_machine")
    @ApiModelProperty(value = "执行机器")
    private ExecuteMachine runMachine;

    @Column(name = "`task_id`")
    @ApiModelProperty(value = "所属任务")
    private Long taskId;

    @Column(name = "`case_ids`")
    @ApiModelProperty(value = "批量测试用例id")
    private String caseIds;

    @Column(name = "`case`")
    @ApiModelProperty(value = "单个用例")
    @JSONField(serialize = false)
    private String runCase;

    @Column(name = "`status`")
    @ApiModelProperty(value = "任务状态")
    private String status;

    @Column(name = "`start_time`")
    @ApiModelProperty(value = "开始执行时间")
    private Long startTime;

    @Column(name = "`end_time`")
    @ApiModelProperty(value = "执行结束时间")
    private Long endTime;

    @Column(name = "`progress`")
    @ApiModelProperty(value = "执行进度")
    private Integer progress;

    @Column(name = "`result`")
    @ApiModelProperty(value = "执行结果，1:成功，0:失败，2:异常，3:超时, 4:终止")
    private Integer result;

    @Column(name = "`project_id`")
    @ApiModelProperty(value = "项目ID")
    private Long projectID;

    @Column(name = "create_by", updatable = false)
    @ApiModelProperty(value = "创建人", hidden = true)
    private String createBy;

    @LastModifiedBy
    @Column(name = "update_by")
    @ApiModelProperty(value = "更新人", hidden = true)
    private String updateBy;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    @ApiModelProperty(value = "创建时间", hidden = true)
    private Timestamp createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    @ApiModelProperty(value = "更新时间", hidden = true)
    private Timestamp updateTime;

    public void copy(RunTask source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
