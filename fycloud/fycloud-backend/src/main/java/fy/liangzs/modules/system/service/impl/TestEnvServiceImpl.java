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

import fy.liangzs.utils.*;
import lombok.RequiredArgsConstructor;
import fy.liangzs.exception.BadRequestException;
import fy.liangzs.modules.system.domain.TestEnv;
import fy.liangzs.modules.system.repository.TestEnvRepository;
import fy.liangzs.modules.system.service.TestEnvService;
import fy.liangzs.modules.system.service.dto.TestEnvDto;
import fy.liangzs.modules.system.service.dto.TestEnvQueryCriteria;
import fy.liangzs.modules.system.service.mapstruct.TestEnvMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
* @website https://eladmin.vip
* @description 服务实现
* @author liangzisheng
* @date 2024-11-14
**/
@Service
@RequiredArgsConstructor
public class TestEnvServiceImpl implements TestEnvService {

    private final TestEnvRepository testEnvRepository;
    private final TestEnvMapper testEnvMapper;

    @Override
    public PageResult<TestEnvDto> queryAll(TestEnvQueryCriteria criteria, Pageable pageable){
        Page<TestEnv> page = testEnvRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(testEnvMapper::toDto));
    }

    @Override
    public List<TestEnvDto> queryAll(TestEnvQueryCriteria criteria, Boolean isQuery){
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        List<TestEnvDto> testEnvList = testEnvMapper
                .toDto(testEnvRepository
                        .findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),sort));
        return testEnvList;
    }

    @Override
    @Transactional
    public TestEnvDto findById(Long envId) {
        TestEnv testEnv = testEnvRepository.findById(envId).orElseGet(TestEnv::new);
        ValidationUtil.isNull(testEnv.getId(),"TestEnv","envId", envId);
        return testEnvMapper.toDto(testEnv);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(TestEnv resources) {
        int isExsit = testEnvRepository.countForCreate(resources.getName(), resources.getProjectID());
        if(isExsit>0){
            throw new BadRequestException("当前环境已存在环境名称为：【" + resources.getName() + "】的环境，请修改");
        }
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        resources.setCreateBy(userDetails.getUsername());
        resources.setUpdateBy(userDetails.getUsername());
        testEnvRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TestEnv resources) {
        int isExsit = testEnvRepository.countForUpdate(resources.getName(), resources.getId(), resources.getProjectID());
        if(isExsit>0){
            throw new BadRequestException("当前环境已存在环境名称为：【'"+resources.getName()+"'】的环境，请修改");
        }
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        resources.setUpdateBy(userDetails.getUsername());
        testEnvRepository.save(resources);
    }

    @Override
    public void deleteAll(Set<Long> ids) {
        for (Long envId : ids) {
            testEnvRepository.deleteById(envId);
        }
    }

    @Override
    public void download(List<TestEnvDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (TestEnvDto testEnv : all) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("名称", testEnv.getName());
            map.put("说明", testEnv.getDesc());
            map.put("创建者", testEnv.getCreateBy());
            map.put("更新者", testEnv.getUpdateBy());
            map.put("创建日期", testEnv.getCreateTime());
            map.put("更新时间", testEnv.getUpdateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<TestEnvDto> testEnvDtos) {
        for (TestEnvDto testEnvDto : testEnvDtos) {
            testEnvRepository.deleteById(testEnvDto.getId());
        }
    }
}