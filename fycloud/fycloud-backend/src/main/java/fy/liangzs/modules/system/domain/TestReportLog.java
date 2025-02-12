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
import java.io.Serializable;

/**
* @website https://eladmin.vip
* @description /测试任务
* @author liangzisheng
* @date 2024-11-14
**/
@Entity
@Getter
@Setter
@Table(name="test_report_log")
public class TestReportLog extends BaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "`id`")
    @ApiModelProperty(value = "ID")
    private Long id;

    @Column(name = "`run_task_id`")
    @ApiModelProperty(value = "所属任务")
    private Long runTaskId;

    @Column(name = "`case_no`")
    @ApiModelProperty(value = "用例编号")
    private String caseNo;

    @Column(name = "`case_title`")
    @ApiModelProperty(value = "用例标题")
    private String caseTitle;

    @Column(name = "`case_desc`")
    @ApiModelProperty(value = "用例描述")
    private String caseDesc;

    @Column(name = "`result`")
    @ApiModelProperty(value = "执行结果")
    private String result;

    @Column(name = "`step_log`")
    @ApiModelProperty(value = "批量测试用例id")
//    @JSONField(serialize = false)
    private String stepLog;

    @ApiModelProperty(value = "执行顺序")
    @Column(name = "`exe_sort`")
    private Integer executeSort;

    @Column(name = "`cost_time`")
    @ApiModelProperty(value = "执行结果")
    private String costTime;

    @Column(name = "`project_id`")
    @ApiModelProperty(value = "所属任务")
    private Long projectID;


    public void copy(TestReportLog source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
