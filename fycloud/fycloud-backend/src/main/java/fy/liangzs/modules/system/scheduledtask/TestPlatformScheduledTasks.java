package fy.liangzs.modules.system.scheduledtask;

import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import fy.liangzs.annotation.Log;
import fy.liangzs.modules.system.domain.ExecuteMachine;
import fy.liangzs.modules.system.domain.RunTask;
import fy.liangzs.modules.system.repository.RunTaskRepository;
import fy.liangzs.modules.system.repository.TaskCaseRepository;
import fy.liangzs.modules.system.repository.TestReportLogRepository;
import fy.liangzs.modules.system.service.ExecuteMachineService;
import fy.liangzs.modules.system.service.dto.ExecuteMachineDto;
import fy.liangzs.modules.system.service.dto.ExecuteMachineQueryCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
@EnableScheduling
@RequiredArgsConstructor
public class TestPlatformScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(TestPlatformScheduledTasks.class);
    private final ExecuteMachineService executeMachineService;
    private final RunTaskRepository runTaskRepository;
    private final TaskCaseRepository taskCaseRepository;
    private final TestReportLogRepository testReportLogRepository;

    @Scheduled(cron = "0 0/3 * * * ?")
    public void checkMachineOffLine(){
//        log.info("查询执行机上报心跳");
        List<ExecuteMachine> executeMachineList = executeMachineService.queryAllMachine();
        for (ExecuteMachine executeMachine : executeMachineList) {
            Timestamp updateTime = executeMachine.getUpdateTime();
            long timePassed = getPassTime(updateTime);
            //3分钟不上报心跳则离线
            if(executeMachine.getStatus() == 1 && timePassed > 180000){
                executeMachineService.machineOffLine(executeMachine.getUniqueCode(), "system");
                log.info("执行机id: {}, name: {}==>离线", executeMachine.getId(), executeMachine.getName());
            }
        }
    }

    //每天凌晨清理debug任务
    @Scheduled(cron = "0 0 0 * * ?")
    public void clearDebugTasks(){
        List<RunTask> runTasks = runTaskRepository.findDebugTasks();
        runTaskRepository.deleteAll(runTasks);
        log.info("删除{}个debug任务", runTasks.size());
    }

    //每小时检查任务执行是否超时
    @Scheduled(cron = "0 0 * * * ?")
    public void checkTimeoutTasks(){
        List<RunTask> runTasks = runTaskRepository.findRunningTasks();
        for(RunTask task:runTasks){
            long timePass = getPassTime(task.getCreateTime());
            Long tid = task.getId();
            if(timePass > 1000 * 60 * 60 * 3){
                task.setStatus("timeout");
                int total = taskCaseRepository.countByTaskId(task.getTaskId());
                int runCount = testReportLogRepository.runCountByTaskId(tid);
                double calcProgress = (double) runCount / (double) total * 100;
                int progress = (int) calcProgress;
                task.setEndTime(System.currentTimeMillis());
                task.setResult(3);
                task.setProgress(progress);
                runTaskRepository.save(task);
                log.info("任务ID：{}，任务名称：{} 执行超时", tid, task.getName());
            }
        }
    }

    private Long getPassTime(Timestamp timestamp){
        Timestamp now = new Timestamp(System.currentTimeMillis());
        return now.getTime() - timestamp.getTime();
    }

}
