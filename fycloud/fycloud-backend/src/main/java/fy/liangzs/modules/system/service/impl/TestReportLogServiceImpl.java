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
package fy.liangzs.modules.system.service.impl;

import com.alibaba.fastjson.JSONObject;
import fy.liangzs.modules.system.domain.Notification;
import fy.liangzs.modules.system.domain.TestTask;
import fy.liangzs.modules.system.repository.*;
import lombok.RequiredArgsConstructor;
import fy.liangzs.config.FileProperties;
import fy.liangzs.exception.BadRequestException;
import fy.liangzs.modules.system.domain.RunTask;
import fy.liangzs.modules.system.domain.TestReportLog;
import fy.liangzs.modules.system.service.TestReportLogService;
import fy.liangzs.utils.FileUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
* @website https://eladmin.vip
* @description 服务实现
* @author liangzisheng
* @date 2024-11-14
**/
@Service
@RequiredArgsConstructor
public class TestReportLogServiceImpl implements TestReportLogService {

    private static final Logger log = LogManager.getLogger(TestReportLogServiceImpl.class);
    private final TestReportLogRepository testReportLogRepository;
    private final TestTaskRepository testTaskRepository;
    private final RunTaskRepository runTaskRepository;
    private final TaskCaseRepository taskCaseRepository;
    private final FileProperties properties;
    private final NotifyTemplateRepository notifyTemplateRepository;
    private final JavaMailSender javaMailSender;
    private final RestTemplate restTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uploadLogs(List<TestReportLog> resources, Long tid) {
        testReportLogRepository.saveAllAndFlush(resources);
        RunTask runTask = runTaskRepository.getById(tid);
        String taskType = runTask.getRunType();
        if(taskType.equals("task")){
            int total = taskCaseRepository.countByTaskId(runTask.getTaskId());
            int runCount = testReportLogRepository.runCountByTaskId(tid);
            int failCount = testReportLogRepository.failCountByTaskId(tid);
            int errorCount = testReportLogRepository.errorCountByTaskId(tid);
            double calcProgress = (double) runCount / (double) total * 100;
            int progress = (int) calcProgress;
            int result = failCount == 0 ?  (errorCount != 0 ? 2 : 1) : 0;
            if(runTask.getResult() != 4 && runTask.getResult() != 3){
                runTask.setResult(result);
            }
            runTask.setProgress(progress);
            if (runCount==total) {
                runTask.setStatus("complete");
                runTask.setEndTime(System.currentTimeMillis());
                int successCount = testReportLogRepository.successCountByTaskId(tid);
                TestTask testTask = testTaskRepository.getById(runTask.getTaskId());
                JSONObject report = new JSONObject();
                report.put("taskName", runTask.getName());
                report.put("projectName", testTaskRepository.getTaskPname(runTask.getProjectID()));
                report.put("envName", runTask.getRunEnv().getName());
                report.put("runner", runTask.getCreateBy());
                report.put("startTime", getFormatTime(runTask.getStartTime()));
                report.put("endTime", getFormatTime(runTask.getEndTime()));
                report.put("cost", getCost(runTask.getStartTime(), runTask.getEndTime()));
                report.put("result", getResult(result));
                report.put("total", String.valueOf(total));
                report.put("successCount", String.valueOf(successCount));
                report.put("failCount", String.valueOf(failCount));
                report.put("errorCount", String.valueOf(errorCount));
                report.put("rate", getRate(total, successCount));
                Boolean isNotify = testTask.getIsNotify();
                if (isNotify) {
                    List<Notification> notifications = testTask.getNotifications();
                    for (Notification notification : notifications) {
                        if (notification.getNType() == 1) {
                            sendEmail(report, notification);
                        } else if (notification.getNType() == 2) {
                            sendWx(report, notification);
                        } else if (notification.getNType() == 3) {
                            sendFh(report, notification);
                        }
                    }
                }
            }
            runTaskRepository.save(runTask);
        }
    }

    @Override
    public void uploadImages(List<MultipartFile> files) {
        if (files.isEmpty()) {
            throw new BadRequestException("文件不能为空");
        }
        for (MultipartFile file : files) {
            FileUtil.uploadTestImage(file, properties.getPath().getTestImage());
        }
    }

    public void sendEmail(JSONObject report, Notification notification){
        String content = notifyTemplateRepository.getById(1L).getContent();
        content = replaceContent(content, report);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();;
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("自动化测试平台管理员<" + notification.getSender() +">");
            helper.setSubject("自动化测试执行结果通知,任务名称: "+ report.getString("taskName"));
            helper.setTo(notification.getReceiver().split(","));
            if(notification.getBcc() != null && !notification.getBcc().isEmpty()){
                helper.setBcc(notification.getBcc().split(","));
            }
            if(notification.getCc() != null && !notification.getCc().isEmpty()){
                helper.setCc(notification.getCc().split(","));
            }
            helper.setText(content, true); // true indicates text/html
            javaMailSender.send(message);
        } catch (Exception e) {
            log.error("发送测试结果到邮件异常：{}", e.getMessage());
        }
    }

    private void sendWx(JSONObject report, Notification notification) {
        String url = notification.getWebhook();
        String content = notifyTemplateRepository.getById(2L).getContent();
        content = replaceContent(content, report);
        JSONObject requestBody = new JSONObject();
        JSONObject markdown = new JSONObject();
        markdown.put("content", content);
        requestBody.put("msgtype", "markdown");
        requestBody.put("markdown", markdown);
        try {
            restTemplate.postForEntity(url, requestBody, JSONObject.class);
        } catch (Exception e) {
            log.error("发送测试结果到微信异常：{}", e.getMessage());
        }
    }

    private void sendFh(JSONObject report, Notification notification) {
        String url = notification.getWebhook();
        String content = notifyTemplateRepository.getById(3L).getContent();
        content = replaceContent(content, report);
        JSONObject requestBody = JSONObject.parseObject(content);
        try {
            restTemplate.postForEntity(url, requestBody, JSONObject.class);
        } catch (Exception e) {
            log.error("发送测试结果到飞书异常：{}", e.getMessage());
        }
    }

    private String getFormatTime(Long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    private String getCost(Long startTime, Long endTime) {
        long timePassed = endTime - startTime;
        String costTime = "";
        if (timePassed < 60000) {
            costTime = String.format("%.2f", timePassed / 1000.0) + "秒";
        } else {
            String min = String.valueOf(timePassed / 60000) + "分";
            String sec = String.format("%.2f", (timePassed % 6000) / 1000.0) + "秒";
            costTime = min + sec;
        }
        return costTime;
    }

    private String getRate(double total, double pass) {

        return String.format("%.2f", pass / total * 100.0) + "%";
    }

    private String getResult(Integer res) {
        String result = "成功";
        if(res == 0){
            result = "失败";
        }else if(res == 2){
            result = "异常";
        }
        return result;
    }

    private String replaceContent(String content, JSONObject report) {
        return content.replace("taskName", report.getString("taskName"))
                .replace("testProject", report.getString("projectName"))
                .replace("testEnv", report.getString("envName"))
                .replace("runner", report.getString("runner"))
                .replace("startTime", report.getString("startTime"))
                .replace("endTime", report.getString("endTime"))
                .replace("cost", report.getString("cost"))
                .replace("result", report.getString("result"))
                .replace("total", report.getString("total"))
                .replace("successCount", report.getString("successCount"))
                .replace("failCount", report.getString("failCount"))
                .replace("errorCount", report.getString("errorCount"))
                .replace("rate", report.getString("rate"));
    }
}