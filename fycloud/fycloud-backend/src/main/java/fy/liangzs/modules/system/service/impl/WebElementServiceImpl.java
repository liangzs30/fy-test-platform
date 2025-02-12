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

import fy.liangzs.modules.system.service.dto.WebElementDto;
import fy.liangzs.modules.system.service.dto.WebElementQueryCriteria;
import fy.liangzs.modules.system.service.dto.WebElementSmallDto;
import fy.liangzs.utils.*;
import lombok.RequiredArgsConstructor;
import fy.liangzs.exception.EntityNotFoundException;
import fy.liangzs.modules.system.domain.WebElement;
import fy.liangzs.modules.system.repository.WebElementRepository;
import fy.liangzs.modules.system.service.WebElementService;
import fy.liangzs.modules.system.service.mapstruct.WebElementMapper;
import fy.liangzs.modules.system.service.mapstruct.WebElementSmallMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class WebElementServiceImpl implements WebElementService {

    private final WebElementRepository webElementRepository;
    private final WebElementMapper webElementMapper;
    private final WebElementSmallMapper webElementSmallMapper;


    @Override
    public PageResult<WebElementDto> queryAll(WebElementQueryCriteria criteria, Pageable pageable) {
        Page<WebElement> page = webElementRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(webElementMapper::toDto));
    }

    @Override
    public List<WebElementSmallDto> queryByProjectID(Long id) {
        List<WebElement> webElements = webElementRepository.findByPid(id);
        return webElementSmallMapper.toDto(webElements);
    }

    @Override
    public WebElementDto findById(Long id) {
        WebElement element = webElementRepository.findById(id).orElseGet(WebElement::new);
        ValidationUtil.isNull(element.getId(),"WebElement","id", id);
        return webElementMapper.toDto(element);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(WebElement resources) {
        UserDetails userDtails = SecurityUtils.getCurrentUser();
        resources.setCreateBy(userDtails.getUsername());
        resources.setUpdateBy(userDtails.getUsername());
        webElementRepository.save(resources);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(WebElement resources) {
        UserDetails userDtails = SecurityUtils.getCurrentUser();
        resources.setUpdateBy(userDtails.getUsername());
        webElementRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(Set<Long> ids) {
        for (Long id : ids) {
            if (!webElementRepository.findById(id).isPresent()) {
                throw new EntityNotFoundException(WebElement.class, "id", id.toString());
            }
            webElementRepository.deleteById(id);
        }
    }
}