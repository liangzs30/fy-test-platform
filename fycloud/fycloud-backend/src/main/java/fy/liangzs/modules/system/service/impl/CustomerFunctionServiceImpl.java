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

import fy.liangzs.modules.system.service.dto.CustomerFunctionDto;
import fy.liangzs.modules.system.service.dto.CustomerFunctionQueryCriteria;
import fy.liangzs.utils.PageResult;
import fy.liangzs.utils.PageUtil;
import fy.liangzs.utils.QueryHelp;
import fy.liangzs.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import fy.liangzs.exception.BadRequestException;
import fy.liangzs.exception.EntityNotFoundException;
import fy.liangzs.modules.system.domain.CommonParam;
import fy.liangzs.modules.system.domain.CustomerFunction;
import fy.liangzs.modules.system.repository.CustomerFunctionRepository;
import fy.liangzs.modules.system.service.CustomerFunctionService;
import fy.liangzs.modules.system.service.mapstruct.CustomerFunctionMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
* @website https://eladmin.vip
* @description 服务实现
* @author liangzisheng
* @date 2024-11-14
**/
@Service
@RequiredArgsConstructor
public class CustomerFunctionServiceImpl implements CustomerFunctionService {

    private final CustomerFunctionRepository customerFunctionRepository;
    private final CustomerFunctionMapper customerFunctionMapper;


    @Override
    public PageResult<CustomerFunctionDto> queryAll(CustomerFunctionQueryCriteria criteria, Pageable pageable) {
        Page<CustomerFunction> page = customerFunctionRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(customerFunctionMapper::toDto));
    }

    @Override
    public List<CustomerFunctionDto> queryAll(CustomerFunctionQueryCriteria criteria, Boolean isQuery){
        Sort sort = Sort.by(Sort.Direction.DESC, "id");
        return customerFunctionMapper
                .toDto(customerFunctionRepository
                        .findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),sort));
    }

    @Override
    public CustomerFunctionDto findById(Long pId) {
        CustomerFunction customerFunction = customerFunctionRepository.findById(pId).orElseGet(CustomerFunction::new);
        return customerFunctionMapper.toDto(customerFunction);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(CustomerFunction resources) {
        int count = customerFunctionRepository.countByName(resources.getName());
        if (count > 0) {
            throw new BadRequestException("已存在函数名名为`" + resources.getName() + "`的记录，请修改");
        }
        UserDetails userDtails = SecurityUtils.getCurrentUser();
        resources.setCreateBy(userDtails.getUsername());
        resources.setUpdateBy(userDtails.getUsername());
        customerFunctionRepository.save(resources);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CustomerFunction resources) {
        int count = customerFunctionRepository.countForUpdate(resources.getName(), resources.getId());
        if (count > 0) {
            throw new BadRequestException("已存在函数名为`" + resources.getName() + "`的记录，请修改");
        }
        UserDetails userDtails = SecurityUtils.getCurrentUser();
        resources.setUpdateBy(userDtails.getUsername());
        customerFunctionRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(Set<Long> ids) {
        for (Long id : ids) {
            if (!customerFunctionRepository.findById(id).isPresent()) {
                throw new EntityNotFoundException(CommonParam.class, "id", id.toString());
            }
            customerFunctionRepository.deleteById(id);
        }
    }
}