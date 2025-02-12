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
import fy.liangzs.exception.EntityNotFoundException;
import fy.liangzs.modules.system.domain.Kw;
import fy.liangzs.modules.system.domain.KwParamConf;
import fy.liangzs.modules.system.repository.KwParamConfRepository;
import fy.liangzs.modules.system.repository.KwRepository;
import fy.liangzs.modules.system.service.KwService;
import fy.liangzs.modules.system.service.dto.KwDto;
import fy.liangzs.modules.system.service.dto.KwQueryCriteria;
import fy.liangzs.modules.system.service.mapstruct.KwMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Set;

/**
* @website https://eladmin.vip
* @description 服务实现
* @author liangzisheng
* @date 2024-11-14
**/
@Service
@RequiredArgsConstructor
public class KwServiceImpl implements KwService {

    private final KwRepository kwRepository;
    private final KwMapper kwMapper;
    private final KwParamConfRepository kwParamConfRepository;


    @Override
    public PageResult<KwDto> queryAll(KwQueryCriteria criteria, Pageable pageable) {
        Page<Kw> page = kwRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(kwMapper::toDto));
    }

    @Override
    public KwDto findById(Long kwId) {
        Kw kw = kwRepository.findById(kwId).orElseGet(Kw::new);
        ValidationUtil.isNull(kw.getId(),"Kw","kwId", kwId);
        return kwMapper.toDto(kw);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Kw resources) {
        int exist = kwRepository.countForAdd(resources.getName());
        if (exist > 0) {
            throw new BadRequestException("关键字名称已存在，请修改关键字名称");
        }
        UserDetails userDtails = SecurityUtils.getCurrentUser();
        resources.setCreateBy(userDtails.getUsername());
        resources.setUpdateBy(userDtails.getUsername());
        kwRepository.save(resources);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Kw resources) {
        int exist = kwRepository.countForUpdate(resources.getName(), resources.getId());
        if (exist > 0) {
            throw new BadRequestException("关键字名称已存在，请修改关键字名称");
        }
        UserDetails userDetails = SecurityUtils.getCurrentUser();
        Kw kw = kwRepository.findById(resources.getId()).orElseGet(Kw::new);
        kw.getKwParamConfs().clear();
        kw.setUpdateBy(userDetails.getUsername());
        kw.setName(resources.getName());
        kw.setClassName(resources.getClassName());
        kw.setFuncName(resources.getFuncName());
        kw.setIfResp(resources.getIfResp());
        kw.setDesc(resources.getDesc());
        List<KwParamConf> kwParamConfs = resources.getKwParamConfs();
        for(KwParamConf kwParamConf : kwParamConfs) {
            kwParamConf.setKwId(kw.getId());
        }
        kw.getKwParamConfs().addAll(kwParamConfs);
        kwRepository.save(kw);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(Set<Long> ids) {
        for (Long id : ids) {
            if (!kwRepository.findById(id).isPresent()) {
                throw new EntityNotFoundException(Kw.class, "id", id.toString());
            }else if(kwRepository.caseStepRelate(id)>0){
                throw new BadRequestException("关键字正在被使用，不能删除");
            }
//            kwParamConfRepository.deleteByKwId(id);
            kwRepository.deleteById(id);
        }
    }
}