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
package fy.liangzs.modules.system.rest;

import com.alibaba.fastjson.JSONObject;
import fy.liangzs.modules.system.domain.ExecuteMachine;
import fy.liangzs.modules.system.domain.RunTask;
import fy.liangzs.modules.system.domain.TestReport;
import fy.liangzs.modules.system.domain.TestReportLog;
import fy.liangzs.modules.system.service.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import fy.liangzs.exception.BadRequestException;
import fy.liangzs.modules.system.repository.StopRunTaskRepository;
import fy.liangzs.modules.system.service.dto.CommonParamSmallDto;
import fy.liangzs.modules.system.service.dto.ExecuteMachineDto;
import fy.liangzs.modules.system.service.dto.TestCaseDtoSort;
import fy.liangzs.modules.system.service.dto.WebElementSmallDto;
import fy.liangzs.utils.SecurityUtils;
import fy.liangzs.utils.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
* @website https://eladmin.vip
* @author liangzisheng
* @date 2024-11-14
**/
@RestController
@RequiredArgsConstructor
@Api(tags = "执行任务")
@RequestMapping("/api/executeApi")
public class ExecuteApiController {

    private final ExecuteMachineService executeMachineService;
    private final RunTaskService runTaskService;
    private final CommonParamService commonParamService;
    private final TestReportService testReportService;
    private final TestReportLogService testReportLogService;
    private final WebElementService webElementService;
    private final StopRunTaskRepository stopRunTaskRepository;

    @GetMapping("/queryTaskList/{uniqueCode}")
    @ApiOperation("查询执行机任务")
    public ResponseEntity<List<RunTask>> queryMachineTask(@PathVariable String uniqueCode){
        checkUser();
        ExecuteMachine machine = executeMachineService.getByUniqueCode(uniqueCode);
        Long macId = machine.getId();
        List<RunTask> runTaskList = runTaskService.getPendingTaskByMachine(macId);
        return new ResponseEntity<>(runTaskList, HttpStatus.OK);
    }

    @GetMapping("/queryTaskCaseByTaskId/{tId}")
    @ApiOperation("查询任务用例")
    public ResponseEntity<List<TestCaseDtoSort>> queryTaskCaseByTaskId(@PathVariable Long tId){
        checkUser();
        List<TestCaseDtoSort> testCaseList = runTaskService.getRunTestCase(tId);
        return new ResponseEntity<>(testCaseList, HttpStatus.OK);
    }

    @GetMapping("/queryTaskSysParams/{envId}")
    @ApiOperation("查询任务变量")
    public ResponseEntity<List<CommonParamSmallDto>> queryTaskSysParamsByenvId(@PathVariable Long envId){
        checkUser();
        List<CommonParamSmallDto> commonParamList = commonParamService.queryByEnvId(envId);
        return new ResponseEntity<>(commonParamList, HttpStatus.OK);
    }

    @GetMapping("/queryProjectWebElements/{id}")
    @ApiOperation("查询项目元素")
    public ResponseEntity<List<WebElementSmallDto>> queryWebelements(@PathVariable Long id){
        checkUser();
        List<WebElementSmallDto> webElementSmallDtoList = webElementService.queryByProjectID(id);
        return new ResponseEntity<>(webElementSmallDtoList, HttpStatus.OK);
    }

    @PostMapping("/task/update")
    @ApiOperation("更改任务状态")
    public ResponseEntity<Object> updateTaskStatus(@RequestBody JSONObject task){
        checkUser();
        runTaskService.updateStatus(task);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/machineInfo/{uniqueCode}")
    @ApiOperation("查询执行机信息")
    public ResponseEntity<ExecuteMachineDto> getMachineInfo(@PathVariable String uniqueCode){
        checkUser();
        ExecuteMachine machine = executeMachineService.getByUniqueCode(uniqueCode);
        if(machine == null){
            throw new BadRequestException("无此执行机，请确认uniqueCode");
        }
        Long macId = machine.getId();
        ExecuteMachineDto executeMachineDto = executeMachineService.findById(macId);
        return new ResponseEntity<>(executeMachineDto, HttpStatus.OK);
    }

    @GetMapping("/query/machine/stopTask/{id}")
    @ApiOperation("查询执行机需要停止的任务")
    public ResponseEntity<Long> getStopTask(@PathVariable Long id){
        checkUser();
        Long taskId = stopRunTaskRepository.findTidByMid(id);
        return new ResponseEntity<>(taskId, HttpStatus.OK);
    }

    @PostMapping("/machine/heartBeat")
    @ApiOperation("执行机心跳")
    public ResponseEntity<Object> machineHeartBeat(@RequestBody JSONObject mac){
        checkUser();
        executeMachineService.update(mac);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/report/create")
    @ApiOperation("创建测试报告")
    public ResponseEntity<Object> createReport(@RequestBody TestReport resource){
        checkUser();
        testReportService.create(resource);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/report/upload/log/{tid}")
    @ApiOperation("上报执行日志")
    public ResponseEntity<Object> uploadReportLog(@RequestBody List<TestReportLog> resources, @PathVariable Long tid){
        checkUser();
        testReportLogService.uploadLogs(resources, tid);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/report/upload/image")
    @ApiOperation("上传图片")
    public ResponseEntity<Object> uploadReportImage(List<MultipartFile> files){
        checkUser();
        testReportLogService.uploadImages(files);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/machine/getLocalIp")
    @ApiOperation("查询执行机本地ip地址")
    public ResponseEntity<JSONObject> getMachineIp(HttpServletRequest request){
        checkUser();
        String ip = StringUtils.getIp(request);
        JSONObject resp = new JSONObject();
        resp.put("localIp", ip);
        return new ResponseEntity<>(resp, HttpStatus.OK);
    }

    private void checkUser(){
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        if(userDetails == null || !userDetails.getUsername().equals("execMachine")){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}