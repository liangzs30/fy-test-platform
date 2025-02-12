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
package fy.liangzs.modules.system.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import fy.liangzs.annotation.Log;
import fy.liangzs.modules.system.domain.Notification;
import fy.liangzs.modules.system.service.NotificationService;
import fy.liangzs.modules.system.service.dto.NotificationDto;
import fy.liangzs.modules.system.service.dto.NotificationQueryCriteria;
import fy.liangzs.utils.PageResult;
import fy.liangzs.utils.PageUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;


/**
* @website https://eladmin.vip
* @author liangzisheng
* @date 2024-11-14
**/
@RestController
@RequiredArgsConstructor
@Api(tags = "通知管理")
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @ApiOperation("查询通知")
    @PreAuthorize("@el.check('notification:list')")
    public ResponseEntity<PageResult<NotificationDto>> queryNotification(NotificationQueryCriteria criteria, Pageable pageable){
        PageResult<NotificationDto> notificationDtos = notificationService.queryAll(criteria, pageable);
        return new ResponseEntity<>(notificationDtos, HttpStatus.OK);
    }

    @PostMapping
    @Log("新增通知")
    @ApiOperation("新增通知")
    @PreAuthorize("@el.check('notification:add')")
    public ResponseEntity<Object> createNotification(@Validated @RequestBody Notification resources){
        notificationService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改通知")
    @ApiOperation("修改通知")
    @PreAuthorize("@el.check('notification:edit')")
    public ResponseEntity<Object> updateNotification(@Validated @RequestBody Notification resources){
        notificationService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除环境")
    @ApiOperation("删除环境")
    @PreAuthorize("@el.check('notification:del')")
    public ResponseEntity<Object> deleteNotification(@RequestBody Set<Long> ids) {
        notificationService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}