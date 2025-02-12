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

import lombok.RequiredArgsConstructor;
import fy.liangzs.exception.BadRequestException;
import fy.liangzs.modules.system.domain.Notification;
import fy.liangzs.modules.system.repository.NotificationRepository;
import fy.liangzs.modules.system.service.NotificationService;
import fy.liangzs.modules.system.service.dto.NotificationDto;
import fy.liangzs.modules.system.service.dto.NotificationQueryCriteria;
import fy.liangzs.modules.system.service.mapstruct.NotificationMapper;
import fy.liangzs.utils.PageResult;
import fy.liangzs.utils.PageUtil;
import fy.liangzs.utils.QueryHelp;
import fy.liangzs.utils.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    public PageResult<NotificationDto> queryAll(NotificationQueryCriteria criteria, Pageable pageable) {
        Page<Notification> page = notificationRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(notificationMapper::toDto));
    }

    @Override
    public List<NotificationDto> queryAll(NotificationQueryCriteria criteria, Boolean isQuery) {
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        List<NotificationDto> notificationDtos = notificationMapper
                .toDto(notificationRepository
                        .findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),sort));
        return notificationDtos;
    }

    @Override
    public Notification findById(Long id) {
        return notificationRepository.findById(id).orElseGet(Notification::new);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Notification resources) {
        int exist = notificationRepository.countByprojectAndName(resources.getProjectID(), resources.getName());
        if(exist > 0){
            throw new BadRequestException("项目下已存在相同名称的通知");
        }
        UserDetails userDtails = SecurityUtils.getCurrentUser();
        resources.setCreateBy(userDtails.getUsername());
        resources.setUpdateBy(userDtails.getUsername());
        notificationRepository.save(resources);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Notification resources) {
        int exist = notificationRepository.countForUpdate(resources.getProjectID(), resources.getId(), resources.getName());
        if(exist > 0){
            throw new BadRequestException("项目下已存在相同名称的通知");
        }
        UserDetails userDtails = SecurityUtils.getCurrentUser();
        resources.setUpdateBy(userDtails.getUsername());
        notificationRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(Set<Long> ids) {
        for(Long id:ids){
            notificationRepository.deleteById(id);
        }
    }

}