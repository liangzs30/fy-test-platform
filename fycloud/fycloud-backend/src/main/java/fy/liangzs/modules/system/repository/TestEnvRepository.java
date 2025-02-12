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

import fy.liangzs.modules.system.domain.TestEnv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
* @website https://eladmin.vip
* @author liangzisheng
* @date 2024-11-14
**/
public interface TestEnvRepository extends JpaRepository<TestEnv, Long>, JpaSpecificationExecutor<TestEnv> {
    @Query(value = "select count(*) from test_sys_env where name=?1 and project_id=?2 ", nativeQuery = true)
    int countForCreate(String name, Long pid);

    @Query(value = "select count(*) from test_sys_env where name=?1 and env_id !=?2 and project_id=?3", nativeQuery = true)
    int countForUpdate(String name, Long id, Long pid);
}