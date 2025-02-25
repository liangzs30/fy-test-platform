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

import fy.liangzs.modules.system.domain.Kw;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
* @website https://eladmin.vip
* @author liangzisheng
* @date 2024-11-14
**/
public interface KwRepository extends JpaRepository<Kw, Long>, JpaSpecificationExecutor<Kw> {

    @Query(value = "select * from key_word where name = ?1 limit 1", nativeQuery = true)
    Kw findByName(String name);

    @Query(value = "select count(*) from key_word where name = ?1 and category_id=?2", nativeQuery = true)
    int countForAdd(String name, Long cid);

    @Query(value = "select count(*) from key_word where name = ?1 and kw_id != ?2  and category_id=?3", nativeQuery = true)
    int countForUpdate(String name, long id, Long cid);

    @Query(value = "select count(*) from test_step where kw_id = ?1", nativeQuery = true)
    int caseStepRelate(Long kwId);

}