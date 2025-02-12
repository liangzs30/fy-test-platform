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

import fy.liangzs.modules.system.domain.TaskCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

/**
* @website https://eladmin.vip
* @author liangzisheng
* @date 2024-11-14
**/
public interface TaskCaseRepository extends JpaRepository<TaskCase, Long>, JpaSpecificationExecutor<TaskCase> {

    @Query(value = "select * from task_case where case_id=?1 ", nativeQuery = true)
    List<TaskCase> getByCaseId(Long caseId);

    @Query(value = "select * from task_case where task_id=?1 ORDER BY `exe_sort` ASC", nativeQuery = true)
    List<TaskCase> getByTaskId(Long tId);

    @Query(value = "select count(*) from task_case where task_id=?1 ", nativeQuery = true)
    int countByTaskId(Long tId);

    @Query(value = "select distinct task_id from task_case where case_id=?1 ", nativeQuery = true)
    Set<Long> getTaskIdsByCaseId(Long caseId);

    @Transactional(rollbackOn = Exception.class)
    @Modifying
    @Query(value = "update task_case set case_no=?2, case_title=?3, case_desc=?4, case_cid=?5 where case_id=?1 ", nativeQuery = true)
    void updateByCaseId(Long caseId, String caseNo, String caseTitle, String desc, Long categoryId);
}