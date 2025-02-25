<template>
  <div class="app-container" v-loading="loading">
    <el-row :gutter="20">
      <el-col :span="17">
        <h3 style=" text-align: center;margin-top: 5px;">{{ report.taskName }}</h3>
      </el-col>
      <el-col :span="3">
        <el-button
          type="primary"
          size="small"
          icon="el-icon-refresh"
          style="height: 30px; margin-top: 20px;margin-left: 20px;"
          @click="freshData">刷新
      </el-button>
      </el-col>
    </el-row>
    <el-row :gutter="20">
      <el-table
        border
        :header-cell-style="{ background: '#00BFFF', color: '#606266' }"
        :data="reportLogs"
        style="width: 100%">
        <el-table-column
          type="expand"
          style="width: 30px;">
          <template slot-scope="props">
            <el-table
              border
              :header-cell-style="{ background: '#96A5FF', color: '#606266' }"
              :data="JSON.parse(props.row.stepLog)"
              style="width: 100%; margin-left: 50px;">
              <el-table-column
                label="序号"
                type="index"
                width="50" />
              <el-table-column
                prop="stepName"
                label="步骤名称"
                width="200" 
                :show-overflow-tooltip="true" />
              <el-table-column
                prop="stepDesc"
                label="步骤描述"
                width="300" 
                :show-overflow-tooltip="true" />
              <el-table-column
                prop="result"
                label="结果"
                width="100">
                <template slot-scope="scope">
                  <span v-if="scope.row.result === 'pass'"><svg-icon icon-class="complete" style="margin-left: 5px; font-size: larger;" />成功</span>
                  <span v-if="scope.row.result === 'fail'"><svg-icon icon-class="fail" style="margin-left: 5px; font-size: larger;" />失败</span>
                  <span v-if="scope.row.result === 'error'"><svg-icon icon-class="exception" style="margin-left: 5px; font-size: larger;" />异常</span>
                </template>
              </el-table-column>  
              <el-table-column
                prop="cost_time"
                label="耗时"
                width="100" />
              <el-table-column
                prop="logDetail"
                label="查看日志"
                width="100"
                align="center">
                <template slot-scope="scope">
                  <el-button
                    class="icon-center"
                    style="border: none !important;"
                    @click="viewLogDetail(scope.row.logDetail)">
                    <svg-icon icon-class="detail" />
                  </el-button>
                </template>
              </el-table-column>
              <el-table-column
                prop="screen_shot"
                label="截图"
                width="100"
                align="center">
                <template slot-scope="scope">
                  <el-image
                    v-if="scope.row.screen_shot"
                    style="width: 60px; height: 25px"
                    fit="fill"
                    :src="baseApi + '/testImages' + scope.row.screen_shot"
                    :preview-src-list="[baseApi + '/testImages' + scope.row.screen_shot]">
                  </el-image>
                </template>
              </el-table-column>
            </el-table>
          </template>
        </el-table-column>
        <el-table-column
          label="序号"
          type="index"
          width="50" />
        <el-table-column
          label="用例编号"
          width="180">    
          <template slot-scope="scope">
            <div class="bold-cell">
              {{ scope.row.caseNo }}
            </div>
          </template>
        </el-table-column>
        <el-table-column
          label="用例标题"
          width="180">
          <template slot-scope="scope">
            <div class="case-title-cell">
              {{ scope.row.caseTitle }}
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="caseDesc"
          label="用例说明" />
        <el-table-column
          prop="result"
          label="测试结果">
          <template slot-scope="scope">
            <span v-if="scope.row.result === 'pass'"><svg-icon icon-class="complete" style="margin-left: 5px; font-size: larger;" />成功</span>
            <span v-if="scope.row.result === 'fail'"><svg-icon icon-class="fail" style="margin-left: 5px; font-size: larger;" />失败</span>
            <span v-if="scope.row.result === 'error'"><svg-icon icon-class="exception" style="margin-left: 5px; font-size: larger;" />异常</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="costTime"
          label="耗时" />
      </el-table>
    </el-row>
    <el-dialog
      title="日志详情"
      :visible.sync="logDetailVisible"
      width="80%"
      :modal="false">
      <div>
        <el-timeline>
          <el-timeline-item
            v-for="(logActivity, index) in logDetail"
            :key="index"
            :timestamp="logActivity.time">
            <h4>{{ logActivity.title }}</h4>
            <pre v-if="!logActivity.title.includes('错误')" style="white-space: pre-wrap;">{{ logActivity.content }}</pre>
            <pre v-if="logActivity.title.includes('错误')" style="white-space: pre-wrap; color: brown;">{{ logActivity.content }}</pre>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getReportDetail } from '@/api/testPlatform/testReport'
import { mapGetters } from 'vuex'
export default {
  name: 'DebugReportComponent',
  props: {
    reportid: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      report: {},
      reportLogs: [],
      logDetailVisible: false,
      logDetail: '',
      loading: false
    }
  },
  computed: {
    ...mapGetters([
      'baseApi'
    ])
  },
  mounted() {
    const that = this
    window.onresize = function temp() {
      that.height = document.documentElement.clientHeight - 180 + 'px;'
    }
    this.getReportDetail(15000)
  },
  methods: {
    // 获取报告详情
    getReportDetail(timeout) {
      this.loading = true
      setTimeout(async () => {
        await getReportDetail(this.reportid).then((res) => {
          this.report = res
          this.reportLogs = res.testReportLogList
        })
        this.loading = false
      }, timeout)
    },
    viewLogDetail(scope) {
      this.logDetail = scope
      this.logDetailVisible = true
    },
    freshData() {
      this.getReportDetail(5000)
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
  ::v-deep .vue-treeselect__control,::v-deep .vue-treeselect__placeholder,::v-deep .vue-treeselect__single-value {
    height: 30px;
    line-height: 30px;
  }
  ::v-deep .el-divider__text{
    font-weight:700;
    color: #606266;
  }
  .icon-center {
    display: block;
    text-align: center;
    margin: 0 auto;
    font-size: 15px;
  }
  .bold-cell {
    font-weight: bold;
  }
  .case-title-cell {
    color:#1890ff
  }
</style>
