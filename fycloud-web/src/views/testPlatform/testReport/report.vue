<template>
  <div class="app-container" v-loading="loading">
    <div style="display: inline-flex; margin-left: 500px;margin-right: 100px;">
      <h2 style="display: inline;">{{ report.taskName }}</h2>
      <el-button
        type="primary"
        size="small"
        icon="el-icon-refresh"
        style="height: 30px; margin-top: 15px;margin-left: 15px;"
        @click="freshData">刷新
      </el-button>
    </div>
    <el-row :gutter="20">
      <el-col :span="10">
        <el-descriptions title="执行信息" direction="vertical" :column="5" border>
          <el-descriptions-item label="开始时间">{{ report.startTime }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ report.endTime }}</el-descriptions-item>
          <el-descriptions-item label="测试进度">
             <el-progress
              :text-inside="true"
              :stroke-width="15"
              :percentage="report.progress"
              :status="report.result===1?'success':report.result===0?'exception':'warning'" />
          </el-descriptions-item>
          <el-descriptions-item label="测试耗时">{{ report.costTime }}</el-descriptions-item>
          <el-descriptions-item label="测试结果">
            <span v-if="report.result=='1'" style="color: #1890ff; font-weight: 700;">成功</span>
            <span v-if="report.result=='0'" style="color: red;">失败</span>
            <span v-if="report.result=='2'" style="color: burlywood;">异常</span>
            <span v-if="report.result=='3'" style="color: burlywood;">执行超时</span>
            <span v-if="report.result=='4'" style="color: burlywood;">终止执行</span>
          </el-descriptions-item>
        </el-descriptions>
      </el-col>
      <el-col :span="6">
        <el-descriptions title="执行统计" direction="vertical" :column="5" border>
          <el-descriptions-item label="用例总数">{{ report.total }}</el-descriptions-item>
          <el-descriptions-item label="通过数">{{ report.successCount }}</el-descriptions-item>
          <el-descriptions-item label="失败数">{{ report.failCount }}</el-descriptions-item>
          <el-descriptions-item label="异常数">{{ report.errorCount }}</el-descriptions-item>
          <el-descriptions-item label="通过率">{{ ((report.successCount / report.total) * 100).toFixed(2) }} %</el-descriptions-item>
        </el-descriptions>
      </el-col>
      <el-col :span="8">
        <div id="ringChart" style="width: 300px;height:150px;margin-left: 200px;" />
      </el-col>
    </el-row>
    <el-divider content-position="center">执行记录</el-divider>
    <el-row :gutter="20">
      <el-input v-model="search" size="mini" style="width: 200px;" placeholder="输入用例编号或标题" />
      <el-button size="mini" type="primary" icon="el-icon-search" @click="searchLog">搜索</el-button>
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
              style="width: 90%; margin-left: 50px;">
              <el-table-column
                label="序号"
                type="index"
                width="50" />
              <el-table-column
                prop="stepName"
                label="步骤名称"
                width="250" />
              <el-table-column
                prop="stepDesc"
                label="步骤描述"
                width="410" />
              <el-table-column
                prop="result"
                label="结果"
                width="150">
                <template slot-scope="scope">
                  <span v-if="scope.row.result === 'pass'"><svg-icon icon-class="complete" style="margin-left: 5px; font-size: larger;" />成功</span>
                  <span v-if="scope.row.result === 'fail'"><svg-icon icon-class="fail" style="margin-left: 5px; font-size: larger;" />失败</span>
                  <span v-if="scope.row.result === 'error'"><svg-icon icon-class="exception" style="margin-left: 5px; font-size: larger;" />异常</span>
                </template>
              </el-table-column>  
              <el-table-column
                prop="cost_time"
                label="耗时"
                width="150" />
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
      <div class="block">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page.sync="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, prev, pager, next, sizes, jumper"
          :total="totalRecords">
        </el-pagination>
      </div>
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
import echarts from 'echarts'
export default {
  name: 'ReportComponent',
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
      allReportLogs: [],
      search: '',
      logDetailVisible: false,
      logDetail: '',
      totalRecords: 0,
      currentPage: 1,
      pageSize: 10,
      loading: false
    }
  },
  computed: {
    ...mapGetters([
      'baseApi'
    ])
  },
  mounted: function() {
    const that = this
    window.onresize = function temp() {
      that.height = document.documentElement.clientHeight - 180 + 'px;'
    }
    this.getReportDetail()
  },
  methods: {
    // 获取报告详情
    getReportDetail() {
      setTimeout(() => {
        getReportDetail(this.reportid).then((res) => {
          this.report = res
          this.totalRecords = res.testReportLogList.length
          this.allReportLogs = res.testReportLogList
          if(this.totalRecords>this.pageSize){
            this.reportLogs = res.testReportLogList.slice(0, this.pageSize)
          }else{
            this.reportLogs = res.testReportLogList
          }
          const myChart = echarts.init(document.getElementById('ringChart'))
          const option = {
            color: ['green', '#B9062F', '#FFD732'],
            tooltip: {
              trigger: 'item'
            },
            legend: {
              orient: 'vertical',
              left: 'left'
            },
            series: [
              {
                name: '执行统计',
                type: 'pie', // 指定图表类型为饼图
                radius: ['50%', '80%'], // 环形图的内外半径
                avoidLabelOverlap: false,
                label: {
                  show: false,
                  position: 'center'
                },
                emphasis: {
                  label: {
                    show: true,
                    fontSize: '30',
                    fontWeight: 'bold'
                  }
                },
                labelLine: {
                  show: false
                },
                data: [
                  { value: res.successCount, name: '成功' },
                  { value: res.failCount, name: '失败' },
                  { value: res.errorCount, name: '异常' }
                ]
              }
            ]
          }
          option && myChart.setOption(option)
        })
      }, 100)
    },
    searchLog() {
      this.reportLogs = this.report.testReportLogList
      this.allReportLogs = this.report.testReportLogList
      if (this.search !== null && this.search !== '') {
        const tempLogs = []
        for (let i = 0; i < this.reportLogs.length; i++) {
          if (this.reportLogs[i]['caseNo'].includes(this.search) || this.reportLogs[i]['caseTitle'].includes(this.search)) {
            tempLogs.push(this.reportLogs[i])
          }
        }
        this.reportLogs = tempLogs
        this.allReportLogs = tempLogs
      }
      this.totalRecords = this.reportLogs.length
      if(this.pageSize <= this.totalRecords){
        this.reportLogs = this.reportLogs.slice(0, this.pageSize)
      }else{
        this.reportLogs = this.allReportLogs
      }
    },
    viewLogDetail(scope) {
      this.logDetail = scope
      this.logDetailVisible = true
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
      if(this.pageSize <= this.totalRecords){
        this.reportLogs = this.allReportLogs.slice(0, val)
      }else{
        this.reportLogs = this.allReportLogs
      }
    },
    handleCurrentChange(val) {
      this.searchLog()
      this.currentPage = val
      const offSet1 = (val - 1) * this.pageSize
      const offSet2 = val * this.pageSize
      this.reportLogs = this.allReportLogs.slice(offSet1, offSet2)
    },
    freshData() {
      this.loading = true
      setTimeout(() => {
        this.loading = false
      }, 1000)
      this.getReportDetail()
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
