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
package fy.liangzs.modules.system.repository;

import fy.liangzs.modules.system.domain.RunTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;

/**
* @website https://eladmin.vip
* @author liangzisheng
* @date 2024-11-14
**/
public interface RunTaskRepository extends JpaRepository<RunTask, Long>, JpaSpecificationExecutor<RunTask> {

    @Query(value = "select * from `run_task` where `run_machine` =?1 and status='pending' ", nativeQuery = true)
    List<RunTask> findByMachineId(Long macId);

    @Query(value = "select * from `run_task` where `run_type` !='task' and status in ('complete', 'stopped') ", nativeQuery = true)
    List<RunTask> findDebugTasks();

    @Query(value = "select * from `run_task` where  status = 'running' ", nativeQuery = true)
    List<RunTask> findRunningTasks();
}