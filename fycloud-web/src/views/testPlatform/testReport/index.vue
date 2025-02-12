<template>
  <div class="app-container">
    <!--工具栏-->
    <div class="head-container">
      <div v-if="crud.props.searchToggle">
        <!-- 搜索 -->
        <el-input
          v-model="query.blurry"
          clearable
          size="small"
          placeholder="输入名称搜索"
          style="width: 200px;"
          class="filter-item"
          @keyup.enter.native="crud.toQuery"
        />
        <date-range-picker
          v-model="query.createTime"
          class="date-item"
        />
        <rrOperation />
      </div>
      <crudOperation
        show=""
        :permission="permission"
      />
    </div>
    <!--表格渲染-->
    <el-table
      ref="table"
      v-loading="crud.loading"
      :data="crud.data"
      style="width: 100%;"
      @selection-change="crud.selectionChangeHandler"
    >
      <el-table-column label="序号" type="index" width="50" />
      <el-table-column
        prop="name"
        label="任务名称"
        width="300"
      />
      <el-table-column
        :show-overflow-tooltip="true"
        prop="runEnv"
        label="执行环境">
        <template slot-scope="scope">
          <span>{{ scope.row.runEnv.name }}</span>
        </template>
      </el-table-column>
      <el-table-column
        :show-overflow-tooltip="true"
        prop="createBy"
        label="执行人"
        width="100"/>
      <el-table-column
        :show-overflow-tooltip="true"
        prop="createTime"
        label="开始执行时间"
        sortable
      />
      <el-table-column
        :show-overflow-tooltip="true"
        prop="status"
        label="任务状态"
        width="100">
        <template slot-scope="scope">
          <span v-if="scope.row.status === 'running'">执行中<i class="el-icon-loading" style="color: red; font-size: larger; margin-left: 5px;" /></span>
          <span v-if="scope.row.status === 'termination'">任务终止<svg-icon icon-class="stop" style="margin-left: 5px; font-size: larger;" /></span>
          <span v-if="scope.row.status === 'timeout'">执行超时<svg-icon icon-class="timeout" style="margin-left: 5px; font-size: larger;" /></span>
          <span v-if="scope.row.status === 'complete' && scope.row.result === 1">完成<svg-icon icon-class="complete" style="margin-left: 5px; font-size: larger;" /></span>
          <span v-if="scope.row.status === 'complete' && scope.row.result === 0">完成<svg-icon icon-class="fail" style="margin-left: 5px; font-size: larger;" /></span>
          <span v-if="scope.row.status === 'complete' && scope.row.result === 2">完成<svg-icon icon-class="exception" style="margin-left: 5px; font-size: larger;" /></span>
        </template>
      </el-table-column>
      <el-table-column
        :show-overflow-tooltip="true"
        prop="progress"
        label="进度">
        <template slot-scope="scope">
          <el-progress
           :text-inside="true"
           :stroke-width="15"
           :percentage="scope.row.progress"
           :status="scope.row.result===1?'success':scope.row.result===0?'exception':'warning'" />
        </template>
      </el-table-column>
      <el-table-column
        v-if="checkPer(['admin','report:detail'])"
        label="查看报告"
        width="160px"
        align="center"
        fixed="right"
      >
        <template slot-scope="scope">
          <div style="display: inline-flex">
            <el-button
              style="margin-left: 2px"
              type="success"
              size="mini"
              class="report-detail-button"
              @click="reportDetailView(scope.row)"
            >
              <svg-icon icon-class="detail" style="display: block;font-size: 15px;" />
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
    <!--分页组件-->
    <pagination />
    <el-dialog
        v-if="reportDetailVisible"
        title="报告详情"
        :visible.sync="reportDetailVisible"
        width="60%"
        height="500px"
        :modal="false"
        :fullscreen="true">
        <ReportComponent :reportid="currentReportID" />
    </el-dialog>
  </div>
</template>

<script>
import crudTask from '@/api/testPlatform/testReport'
import CRUD, { presenter, header, form, crud } from '@crud/crudnew'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import pagination from '@crud/Pagination'
import DateRangePicker from '@/components/DateRangePicker'
import { mapGetters } from 'vuex'
const defaultForm = {id: null, name: '', progress: 0, projectID: null, result: null, runEnv: {name: ''}, runType: '', status: '', taskId: null}
export default {
  name: 'TestReport',
  components: {
    crudOperation,
    rrOperation,
    pagination,
    DateRangePicker,
    ReportComponent: () => import('./report.vue')
  },
  cruds() {
    return CRUD({
      title: '测试报告',
      url: 'api/testReport',
      crudMethod: { ...crudTask },
      optShow: {
        add: false,
        edit: false,
        del: false,
        download: false,
        reset: true
      }
    })
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  data() {
    return {
      height: document.documentElement.clientHeight - 180 + 'px;',
      reportDetailVisible: false,
      currentReportID: null,
      permission: {
        // add: ['admin', 'testTask:add'],
        // edit: ['admin', 'testTask:edit'],
        // detail: ['admin', 'testTask:detail'],
        // del: ['admin', 'testTask:del']
      }
    }
  },
  computed: {
    ...mapGetters(['testReport'])
  },
  created() {
    this.crud.msg.add = '新增成功'
  },
  mounted: function() {
    rrOperation
    const that = this
    window.onresize = function temp() {
      that.height = document.documentElement.clientHeight - 180 + 'px;'
    }
  },
  methods: {
    // 禁止输入空格
    keydown(e) {
      if (e.keyCode === 32) {
        e.returnValue = false
      }
    },
    reportDetailView(data) {
      this.currentReportID = data.id
      this.reportDetailVisible = true
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
::v-deep .vue-treeselect__control,
::v-deep .vue-treeselect__placeholder,
::v-deep .vue-treeselect__single-value {
  height: 30px;
  line-height: 30px;
}
::v-deep .el-divider__text {
  font-weight: 700;
  color: #606266;
}
.addStepButton {
  margin-top: 10px;
}
.kw-popover {
  display: flex;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.tag-round {
  border-radius: 50%; /* 将border-radius设置为50%，使得Tag变成圆形 */
  padding: 0; /* 移除padding，使得Tag内容与边缘重合 */
  width: 12px; /* 设置宽度 */
  height: 12px; /* 设置高度，确保宽高相等以形成完美圆形 */
}
.report-detail-button{
  text-align: center;
  margin-top: 10px;
  padding: 0;
  border: none;
  background-color: white;
  color: white;
  width: 15px; /* 按钮宽度 */
  height: 15px; /* 按钮高度 */
  line-height: 10px; /* 行高与按钮高度相同 */
  border-radius: 2px; /* 圆角边框 */
  font-size: 15px; /* 图标大小 */
}
</style>
