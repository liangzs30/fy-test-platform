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
package fy.liangzs.modules.quartz.task;

import lombok.extern.slf4j.Slf4j;
import fy.liangzs.modules.system.domain.RunTask;
import fy.liangzs.modules.system.service.RunTaskService;
import fy.liangzs.modules.system.service.TestTaskService;
import fy.liangzs.modules.system.service.dto.TestTaskDto;
import fy.liangzs.utils.SpringContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 测试用
 * @author Zheng Jie
 * @date 2019-01-08
 */
@Slf4j
@Service
public class TestTaskDemo {

    @Autowired
    private TestTaskService testTaskService;
    @Autowired
    private RunTaskService runTaskService;

//    public void run(){
//        log.info("run 执行成功");
//    }
//
//    public void run1(String str){
//        log.info("run1 执行成功，参数为： {}" + str);
//    }
//
//    public void run2(){
//        log.info("run2 执行成功");
//    }

    public void createRunTask(String tid){
        RunTask runTask = new RunTask();
        Long id = Long.parseLong(tid);
        TestTaskDto testTask = testTaskService.findById(id);
        runTask.setRunType("task");
        runTask.setStatus("pending");
        runTask.setTaskId(testTask.getId());
        runTask.setRunEnv(testTask.getTestEnv());
        runTask.setName("【定时任务】" + testTask.getName());
        runTask.setRunMachine(testTask.getExecuteMachine());
        runTask.setStartTime(System.currentTimeMillis());
        runTask.setProjectID(testTask.getProjectID());
        runTask.setProgress(0);
        runTask.setResult(0);
//        log.info("任务创建人是：{}", testTask.getCreateBy());
        runTask.setCreateBy(testTask.getCreateBy());
        runTaskService.createScdJob(runTask);
    }
}
