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

import fy.liangzs.modules.system.service.dto.CommonParamDto;
import fy.liangzs.modules.system.service.dto.CommonParamQueryCriteria;
import fy.liangzs.modules.system.service.dto.CommonParamSmallDto;
import fy.liangzs.utils.*;
import lombok.RequiredArgsConstructor;
import fy.liangzs.exception.BadRequestException;
import fy.liangzs.exception.EntityNotFoundException;
import fy.liangzs.modules.system.domain.CommonParam;
import fy.liangzs.modules.system.repository.CommonParamRepository;
import fy.liangzs.modules.system.service.CommonParamService;
import fy.liangzs.modules.system.service.mapstruct.CommonParamMapper;
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
public class CommonParamServiceImpl implements CommonParamService {

    private final CommonParamRepository commonParamRepository;
    private final CommonParamMapper commonParamMapper;


    @Override
    public PageResult<CommonParamDto> queryAll(CommonParamQueryCriteria criteria, Pageable pageable) {
        Page<CommonParam> page = commonParamRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(commonParamMapper::toDto));
    }

    @Override
    public List<CommonParamDto> queryAll(CommonParamQueryCriteria criteria, Boolean isQuery){
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        List<CommonParamDto> commonParams = commonParamMapper
                .toDto(commonParamRepository
                        .findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),sort));
        return commonParams;
    }

    @Override
    public CommonParamDto findById(Long pId) {
        CommonParam commonParam = commonParamRepository.findById(pId).orElseGet(CommonParam::new);
        return commonParamMapper.toDto(commonParam);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(CommonParam resources) {
        int count = commonParamRepository.countByNameAndEnv(resources.getName(), resources.getTestEnv().getId());
        if (count > 0) {
            throw new BadRequestException("环境下已存在参数名为`" + resources.getName() + "`的参数，请修改");
        }
        UserDetails userDtails = SecurityUtils.getCurrentUser();
        resources.setCreateBy(userDtails.getUsername());
        resources.setUpdateBy(userDtails.getUsername());
        commonParamRepository.save(resources);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CommonParam resources) {
        int count = commonParamRepository.countForUpdate(resources.getName(), resources.getId(), resources.getTestEnv().getId());
        if (count > 0) {
            throw new BadRequestException("环境下已存在参数名为`" + resources.getName() + "`的参数，请修改");
        }
        UserDetails userDtails = SecurityUtils.getCurrentUser();
        resources.setUpdateBy(userDtails.getUsername());
        commonParamRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(Set<Long> ids) {
        for (Long id : ids) {
            if (!commonParamRepository.findById(id).isPresent()) {
                throw new EntityNotFoundException(CommonParam.class, "id", id.toString());
            }
//            TestCaseParamConfRepository.deleteByTestCaseId(id);
            commonParamRepository.deleteById(id);
        }
    }

    @Override
    public void download(List<CommonParamDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (CommonParamDto commonParamDto : all) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("环境", commonParamDto.getTestEnv().getName());
            map.put("参数名称", commonParamDto.getName());
            map.put("参数值", commonParamDto.getValue());
            map.put("参数说明", commonParamDto.getDesc());
            map.put("创建者", commonParamDto.getCreateBy());
            map.put("更新者", commonParamDto.getUpdateBy());
            map.put("创建日期", commonParamDto.getCreateTime());
            map.put("更新时间", commonParamDto.getUpdateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public List<CommonParamSmallDto> queryByEnvId(Long envId) {
        List<CommonParam> commonParamList = commonParamRepository.queryByEnvId(envId);
        List<CommonParamSmallDto> commonParamSmallDtoList = new ArrayList<>();
        for (CommonParam commonParam : commonParamList) {
            CommonParamSmallDto commonParamSmallDto = new CommonParamSmallDto();
            commonParamSmallDto.setId(commonParam.getId());
            commonParamSmallDto.setName(commonParam.getName());
            commonParamSmallDto.setValue(commonParam.getValue());
            commonParamSmallDtoList.add(commonParamSmallDto);
        }
        return commonParamSmallDtoList;
    }
}