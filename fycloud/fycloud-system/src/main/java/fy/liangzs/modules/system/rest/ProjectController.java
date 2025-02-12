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

import cn.hutool.core.lang.Dict;
import com.alibaba.fastjson.JSONObject;
import fy.liangzs.modules.system.service.dto.ProjectDto;
import fy.liangzs.modules.system.service.dto.ProjectQueryCriteria;
import fy.liangzs.modules.system.service.dto.ProjectSmallDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import fy.liangzs.annotation.Log;
import fy.liangzs.exception.BadRequestException;
import fy.liangzs.modules.system.domain.Project;
import fy.liangzs.modules.system.service.ProjectService;
import fy.liangzs.modules.system.service.dto.*;
import fy.liangzs.utils.PageResult;
import fy.liangzs.utils.SecurityUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Set;

/**
 * @author liangzisheng
 * @date 2018-12-03
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "系统：项目管理")
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    private static final String ENTITY_NAME = "project";

    @ApiOperation("获取单个project")
    @GetMapping(value = "/{id}")
    @PreAuthorize("@el.check('projects:list')")
    public ResponseEntity<ProjectDto> findProjectById(@PathVariable Long id){
        return new ResponseEntity<>(projectService.findById(id), HttpStatus.OK);
    }

    @ApiOperation("返回全部的项目")
    @GetMapping(value = "/all")
    @PreAuthorize("@el.check('projects:list')")
    public ResponseEntity<List<ProjectDto>> queryAllProject(){
        return new ResponseEntity<>(projectService.queryAll(),HttpStatus.OK);
    }

    @ApiOperation("查询项目")
    @GetMapping
    @PreAuthorize("@el.check('projects:list')")
    public ResponseEntity<PageResult<ProjectDto>> queryProject(ProjectQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(projectService.queryAll(criteria,pageable),HttpStatus.OK);
    }

    @ApiOperation("查询用户项目集合")
    @GetMapping("/user/projecets/{userId}")
    @PreAuthorize("@el.check('testCase:list')")
    public ResponseEntity<List<ProjectDto>> queryUserProjects(@PathVariable Long userId){
        return new ResponseEntity<>(projectService.queryByUser(userId),HttpStatus.OK);
    }

    @Log("新增项目")
    @ApiOperation("新增项目")
    @PostMapping
    @PreAuthorize("@el.check('projects:add')")
    public ResponseEntity<Object> createProject(@Validated @RequestBody Project resources){
        if (resources.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        projectService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改项目")
    @ApiOperation("修改项目")
    @PutMapping
    @PreAuthorize("@el.check('projects:edit')")
    public ResponseEntity<Object> updateProject(@Validated(Project.Update.class) @RequestBody Project resources){
        projectService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除项目")
    @ApiOperation("删除项目")
    @DeleteMapping
    @PreAuthorize("@el.check('projects:del')")
    public ResponseEntity<Object> deleteProject(@RequestBody Set<Long> ids){
        // 验证是否被用户关联
//        roleService.verification(ids);
        projectService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @ApiOperation("查询用户当前项目")
    @GetMapping("/user/projecet/{userId}")
    @PreAuthorize("@el.check('testCase:list')")
    public ResponseEntity<ProjectSmallDto> queryUserProject(@PathVariable Long userId){
        return new ResponseEntity<>(projectService.queryLastByUser(userId),HttpStatus.OK);
    }

    @ApiOperation("修改用户当前项目")
    @PutMapping("/user/projecet")
    @PreAuthorize("@el.check('testCase:list')")
    public ResponseEntity<ProjectSmallDto> saveLastProject(@RequestBody JSONObject req){
        ProjectSmallDto project = projectService.saveLast(req.getLong("userID"), req.getLong("projectID"));
        return new ResponseEntity<>(project, HttpStatus.OK);
    }
}
