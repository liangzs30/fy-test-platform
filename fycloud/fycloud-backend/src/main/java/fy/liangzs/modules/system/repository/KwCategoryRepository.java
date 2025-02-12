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

import fy.liangzs.modules.system.domain.KwCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

/**
* @website https://eladmin.vip
* @author liangzisheng
* @date 2024-11-14
**/
public interface KwCategoryRepository extends JpaRepository<KwCategory, Long>, JpaSpecificationExecutor<KwCategory> {
    /**
     *
     * @param id
     * @return
     */
    List<KwCategory> findByPid(Long id);

    /**
     *
     * @return
     */
    List<KwCategory> findByPidIsNull();

    int countByPid(Long pid);

    @Modifying
    @Query(value = " update kw_category set sub_count = ?1 where category_id = ?2 ", nativeQuery = true)
    @Transactional(rollbackOn = Exception.class)
    void updateSubCntById(Integer count, Long id);

    @Query(value = " select count(*) from key_word where category_id = ?1 ", nativeQuery = true)
    int countByKw(Long id);
}