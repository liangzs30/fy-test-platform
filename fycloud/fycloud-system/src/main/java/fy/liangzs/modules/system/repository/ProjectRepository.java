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

import fy.liangzs.modules.system.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

/**
 * @author liangzisheng
 * @date 2018-12-03
 */
public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {

    /**
     * 根据名称查询
     * @param name /
     * @return /
     */
    Project findByName(String name);
    /**
     * 根据用户ID查询
     * @param id 用户ID
     * @return /
     */
    @Query(value = "SELECT r.* FROM sys_project r, sys_projects_users u WHERE " +
            "r.project_id = u.project_id AND u.user_id = ?1",nativeQuery = true)
    List<Project> findByUserId(Long id);

    @Query(value = "SELECT user_id FROM sys_project_user where project_id=?1 ",nativeQuery = true)
    Set<Long> findLastUserIds(Long id);

    @Query(value = "SELECT user_id FROM sys_projects_users where project_id=?1 ",nativeQuery = true)
    Set<Long> findPorjectUserIds(Long id);

    @Query(value = "SELECT r.* FROM sys_project r, sys_project_user u WHERE " +
            "r.project_id = u.project_id AND u.user_id = ?1 limit 1",nativeQuery = true)
    Project findLastByUserId(Long id);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO sys_project_user (project_id, user_id) values (?1, ?2)",nativeQuery = true)
    void saveLast(Long pid, Long uid);

    @Modifying
    @Transactional
    @Query(value = "UPDATE sys_project_user SET project_id=?1 where user_id=?2",nativeQuery = true)
    void updateLast(Long pid, Long uid);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM sys_project_user where user_id=?1",nativeQuery = true)
    void deleteLast(Long uid);

    @Query(value = "select count(*) from test_case where project_id=?1 ", nativeQuery = true)
    int coutByProject(Long pid);

    @Query(value = "select count(*) from test_report_log where project_id=?1 ", nativeQuery = true)
    int runCount(Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM case_category where project_id=?1",nativeQuery = true)
    void deleteCaseCategory(Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM test_sys_env where project_id=?1",nativeQuery = true)
    void deleteEnv(Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM test_sys_param where project_id=?1",nativeQuery = true)
    void deleteCommonParam(Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM test_exe_machine where project_id=?1",nativeQuery = true)
    void deleteMachine(Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM test_notification where project_id=?1",nativeQuery = true)
    void deleteNotifications(Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM test_task where project_id=?1",nativeQuery = true)
    void deleteTestTasks(Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM run_task where project_id=?1",nativeQuery = true)
    void deleteRunTasks(Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM test_report where project_id=?1",nativeQuery = true)
    void deleteReport(Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM test_report_log where project_id=?1",nativeQuery = true)
    void deleteReportLogs(Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM sys_project_user where project_id=?1",nativeQuery = true)
    void deleteRelate(Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM sys_projects_users where project_id=?1",nativeQuery = true)
    void deleteRelates(Long id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM sys_project where project_id=?1",nativeQuery = true)
    void deleteById(Long id);

}
