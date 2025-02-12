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
package fy.liangzs.modules.system.service;

import fy.liangzs.modules.system.domain.TestReportLog;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
* @website https://eladmin.vip
* @description 服务接口
* @author liangzisheng
* @date 2024-11-14
**/
public interface TestReportLogService {

    void uploadLogs(List<TestReportLog> resources, Long tid);

    void uploadImages(List<MultipartFile> files);
}