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
          @keyup.enter.native="crud.toQuery" />
        <date-range-picker v-model="query.createTime" class="date-item" />
        <rrOperation />
      </div>
      <crudOperation show="" :permission="permission" />
    </div>
    <!--表单渲染-->
    <el-dialog
      append-to-body
      :close-on-click-modal="false"
      :before-close="crud.cancelCU"
      :visible.sync="crud.status.cu > 0"
      :title="crud.status.title"
      width="870px">
      <el-form ref="form" :inline="true" :model="form" :rules="rules" size="medium" label-width="100px">
        <el-form-item label="任务名称" prop="name">
          <el-input v-model="form.name" style="width: 260px" @keydown.native="keydown($event)" />
        </el-form-item>
        <el-form-item label="任务描述" prop="desc">
          <el-input v-model="form.desc" type="textarea" style="width: 500px;" :rows="3" />
        </el-form-item>
        <el-form-item label="环境" prop="testEnv">
          <el-select v-model="form.testEnv.id" placeholder="请选择环境" style="width: 500px;">
            <el-option v-for="(item, index) in envs" :key="index" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行机" prop="executeMachine">
          <el-select v-model="form.executeMachine.id" placeholder="请选择执行机" style="width: 500px;">
            <el-option v-for="(item, index) in options" :key="index" :label="item.name" :value="item.id">
              <span style="float: left">{{ item.name }} (IP:{{ item.ipAddr }})</span>
              <span style="float: right; color: #8492a6; margin-left: 20px">
                <span v-if="item.status === 1">在线</span>
                <span v-if="item.status === 0">离线</span>
                <span v-if="item.status === 2">忙碌</span>
                <el-tag v-if="item.status === 1" type="success" effect="dark" class="tag-round" />
                <el-tag v-if="item.status === 0" type="info" effect="dark" class="tag-round" />
                <el-tag v-if="item.status === 2" type="success" effect="dark" class="tag-round" />
              </span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="定时策略" prop="tExpression">
          <el-input
            v-model="form.tExpression"
            placeholder="请输入定时策略"
            readonly
            style="width: 500px;" />
          <el-button
            type="primary"
            @click="openCronDialog">
            生成cron表达式
          </el-button>
        </el-form-item>
        <el-form-item label="通知" prop="notification">
          <el-select
            v-if="form.isNotify"
            v-model="notificationData"
            style="width: 500px;"
            multiple
            placeholder="请选择"
            @remove-tag="deleteTag"
            @change="changeNotify">
            <el-option
              v-for="item in notificationOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id" />
          </el-select>
          <el-switch
            v-model="form.isNotify"
            active-color="#13ce66"
            inactive-color="#ff4949"
            active-text="启用"
            inactive-text="停用" />
        </el-form-item>
        <el-divider style="font-weight: 700;" size="medium">测试用例</el-divider>
        <el-table
          :data="pageTaskCases"
          style="width: 95%"
          border
          row-key="id"
          class="sort-table"
          :header-cell-style="{ background: '#f5f7fa', color: '#606266' }">
          <el-table-column label="" width="40px">
            <template>
              <svg-icon icon-class="list" style="font-size: 14px" @mousedown="rowDrop" />
            </template>
          </el-table-column>
          <el-table-column prop="executeSort" label="序号" width="45" style="text-align: center; font-weight: 700;" />
          <el-table-column prop="category.name" label="模块" width="100" />
          <el-table-column prop="caseNo" label="用例编号" width="150" />
          <el-table-column prop="caseTitle" label="用例名称" width="180" :show-overflow-tooltip="true">
            <template slot-scope="scope">
              <a href="#">{{ scope.row.caseTitle }}</a>
            </template>
          </el-table-column>
          <el-table-column prop="desc" label="用例说明" width="180" :show-overflow-tooltip="true" />
          <el-table-column label="操作" header-align="center">
            <template slot-scope="scope">
              <el-button
                size="mini"
                type="danger"
                icon="el-icon-delete"
                @click="handleDeleteCase(scope.$index, scope.row)" />
            </template>
          </el-table-column>
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
        <el-button size="mini" type="primary" class="addStepButton" @click="handleRelateCase">关联用例</el-button>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="text" @click="crud.cancelCU">取消</el-button>
        <el-button :loading="crud.status.cu === 2" type="primary" @click="crud.submitCU">确认</el-button>
      </div>
    </el-dialog>
    <el-dialog title="关联用例" :visible.sync="relateCasesVisible" width="60%" height="500px" :modal="false">
      <caseTableComponent @handleSelectCases="selectCases" />
    </el-dialog>
    <!--表格渲染-->
    <el-table
      ref="table"
      v-loading="crud.loading"
      :data="crud.data"
      style="width: 100%;"
      @selection-change="crud.selectionChangeHandler">
      <el-table-column :selectable="checkboxT" type="selection" width="55" />
      <el-table-column label="序号" type="index" width="50" />
      <el-table-column :show-overflow-tooltip="true" prop="name" label="名称" />
      <el-table-column :show-overflow-tooltip="true" prop="desc" label="描述" />
      <el-table-column :show-overflow-tooltip="true" prop="createBy" label="创建人" />
      <el-table-column label="启停" align="center" prop="isPause">
        <template slot-scope="scope">
          <el-switch
            v-model="scope.row.isPause"
            active-color="#F56C6C"
            inactive-color="#409EFF"
            @change="changeEnabled(scope.row, scope.row.isPause)" />
        </template>
      </el-table-column>
      <el-table-column :show-overflow-tooltip="true" prop="createTime" label="创建日期" sortable />
      <el-table-column :show-overflow-tooltip="true" prop="updateBy" label="更新人" />
      <el-table-column :show-overflow-tooltip="true" prop="updateTime" label="更新日期" sortable />
      <el-table-column
        v-if="checkPer(['admin', 'kw:edit', 'kw:del'])"
        label="操作"
        width="160px"
        align="center"
        fixed="right">
        <template slot-scope="scope">
          <div style="display: inline-flex">
            <udOperation
              :data="scope.row"
              :permission="permission"
              msg="确定删除吗，此操作不能撤销！" />
            <el-button
              style="margin-left: 2px"
              type="success"
              icon="el-icon-caret-right"
              size="mini"
              @click="toRun(scope.row)" />
          </div>
        </template>
      </el-table-column>
    </el-table>
    <!--分页组件-->
    <pagination />
    <el-dialog title="执行任务" :visible.sync="runFormVisible" :modal="false" width="30%" size="small">
      <el-form :model="runConf" label-position="right" label-width="60px">
        <el-form-item label="环境" prop="runEnv">
          <el-select v-model="runConf.env.id" placeholder="请选择环境" style="width: 300px">
            <el-option v-for="(item, index) in envs" :key="index" :value="item.id" :label="item.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行机" prop="runMachine">
          <el-select v-model="runConf.machine.id" placeholder="请选择执行机" style="width: 300px;">
            <el-option
              v-for="(item, index) in options"
              :key="index"
              :value="item.id"
              :label="item.name"
              :disabled="item.status === 0">
              <span style="float: left">{{ item.name }} (IP:{{ item.ipAddr }})</span>
              <span style="float: right; color: #8492a6; margin-left: 20px">
                <span v-if="item.status === 0">离线</span>
                <span v-if="item.status === 1">在线</span>
                <span v-if="item.status === 2">忙碌</span>
                <el-tag v-if="item.status === 0" type="info" effect="dark" class="tag-round" />
                <el-tag v-if="item.status === 1" type="success" effect="dark" class="tag-round" />
                <el-tag v-if="item.status === 2" type="danger" effect="dark" class="tag-round" />
              </span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="runFormVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="startRun">确 定</el-button>
      </div>
    </el-dialog>
    <el-dialog
      title="生成cron表达式"
      :visible.sync="showCron">
      <vcrontab
        :expression="cronExpression"
        @fill="changeCron"
        @hide="showCron=false" />
    </el-dialog>
  </div>
</template>

<script>
import crudTask from '@/api/testPlatform/testTask'
import CRUD, { presenter, header, form, crud } from '@crud/crudnew'
import { getMachines } from '@/api/testPlatform/executeMachine'
import { getTestEnv } from '@/api/testPlatform/testEnv'
import { getNotification } from '@/api/testPlatform/notification'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import udOperation from '@crud/UD.operation'
import pagination from '@crud/Pagination'
import DateRangePicker from '@/components/DateRangePicker'
import { mapGetters } from 'vuex'
import Sortable from 'sortablejs'
import { createRunTask } from '@/api/testPlatform/runTask'
import vcrontab from 'vcrontab'
let userNotifications = []
const defaultForm = {
  id: null,
  name: '',
  desc: '',
  executeMachine: { id: null },
  testEnv: { id: null },
  tExpression: '',
  schedId: null,
  isPause: false,
  isNotify: false,
  projectID: null,
  isNotify: false,
  notifications: [],
  taskCases: [
    {
      caseId: null,
      caseNo: '',
      caseTitle: '',
      desc: '',
      category: null,
      executeSort: null
    }
  ]
}
export default {
  name: 'TestTask',
  components: {
    crudOperation,
    rrOperation,
    udOperation,
    pagination,
    DateRangePicker,
    vcrontab,
    caseTableComponent: () => import('./selectCase.vue')
  },
  cruds() {
    return CRUD({
      title: '测试任务',
      url: 'api/testTask',
      crudMethod: { ...crudTask },
      optShow: {
        add: true,
        edit: false,
        del: false,
        download: false,
        reset: true
      }
    })
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  // 数据字典
  dicts: ['enable_status'],
  data() {
    return {
      height: document.documentElement.clientHeight - 180 + 'px;',
      relateCasesVisible: false,
      formLabelWidth: '100px',
      stepTitle: '关联用例',
      options: [],
      envs: [],
      notificationData: [],
      notificationOptions: [],
      submitLoading: false,
      runConf: { env: { id: null, name: '' }, machine: { id: null, name: '' }},
      runFormVisible: false,
      runTask: null,
      showCron: false,
      cronExpression: '',
      pageTaskCases: [],
      totalRecords: 0,
      currentPage: 1,
      pageSize: 10,
      defaultProps: { children: 'children', label: 'name', isLeaf: 'leaf' },
      permission: {
        add: ['admin', 'testTask:add'],
        edit: ['admin', 'testTask:edit'],
        del: ['admin', 'testTask:del']
      },
      rules: {
        name: [
          { required: true, message: '请输入名称', trigger: 'blur' },
          {
            min: 6,
            max: 32,
            message: '长度在 6 到 32 个字符',
            trigger: 'blur'
          }
        ],
        executeMachine: [
          { required: true, message: '请选择执行机', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapGetters(['testTask'])
  },
  created() {
    this.crud.msg.add = '新增成功'
  },
  mounted() {
    rrOperation
    const that = this
    window.onresize = function temp() {
      that.height = document.documentElement.clientHeight - 180 + 'px;'
    }
  },
  methods: {
    //获取某页用例
    getPageCase(currentPage, pageSize){
      setTimeout(() => {
        this.totalRecords = this.form.taskCases.length
        if(currentPage * pageSize > this.totalRecords){
          this.pageTaskCases = [...this.form.taskCases.slice((currentPage - 1) * pageSize, this.totalRecords)]
        }else{
          this.pageTaskCases = [...this.form.taskCases.slice((currentPage - 1) * pageSize, currentPage * pageSize)]
        }
      }, 100) // 1000毫秒 = 1秒
    },
    //获取最后一页用例
    getLastPageCase(){
      setTimeout(() => {
        this.totalRecords = this.form.taskCases.length
        // 计算总页数
        const totalPages = Math.ceil(this.totalRecords / this.pageSize);
        this.currentPage = totalPages
        if(this.currentPage * this.pageSize > this.totalRecords){
          this.pageTaskCases = [...this.form.taskCases.slice((this.currentPage - 1) * this.pageSize, this.totalRecords)]
        }else{
          this.pageTaskCases = [...this.form.taskCases.slice((this.currentPage - 1) * this.pageSize, this.currentPage * this.pageSize)]
        }
      }, 100) // 1000毫秒 = 1秒
    },
    handleSizeChange(val){
      this.pageSize = val
      this.currentPage = 1
      if(this.pageSize <= this.totalRecords){
        this.pageTaskCases = [...this.form.taskCases.slice(0, val)]
      }else{
        this.pageTaskCases = this.form.taskCases
      }
    },
    handleCurrentChange(val){
      this.currentPage = val
      const offSet1 = (val - 1) * this.pageSize
      const offSet2 = val * this.pageSize
      this.pageTaskCases = [...this.form.taskCases.slice(offSet1, offSet2)]
    },
    // 禁止输入空格
    keydown(e) {
      if (e.keyCode === 32) {
        e.returnValue = false
      }
    },
    changeNotify(value) {
      userNotifications = []
      value.forEach(function(data, index) {
        const notify = { id: data }
        userNotifications.push(notify)
      })
    },
    deleteTag(value) {
      userNotifications.forEach(function(data, index) {
        if (data.id === value) {
          userNotifications.splice(index, value)
        }
      })
    },
    // 新增前做的操作
    [CRUD.HOOK.beforeToAdd](crud, form) {
      this.form.projectID = sessionStorage.getItem('userProjectID')
      userNotifications = []
      form.taskCases = []
      this.getEnvs()
      this.getMachines()
      this.getNotifications()
      this.pageSize = 10
      this.currentPage = 1
      this.getPageCase(this.currentPage, this.pageSize)
    },
    // 编辑前做的操作
    [CRUD.HOOK.beforeToEdit](crud, form) {
      userNotifications = []
      this.notificationData = []
      this.getEnvs()
      this.getMachines()
      this.getNotifications()
      this.pageSize = 10
      this.currentPage = 1
      this.getPageCase(this.currentPage, this.pageSize)
      const _this = this
      form.notifications.forEach(function(notify, index) {
        _this.notificationData.push(notify.id)
        const noti = { id: notify.id }
        userNotifications.push(noti)
      })
    },
    // 提交前做的操作
    [CRUD.HOOK.afterValidateCU](crud) {
      if (!crud.form.name) {
        this.$message({
          message: '名称不能为空',
          type: 'warning'
        })
        return false
      } else if (!crud.form.tExpression) {
        this.$message({
          message: '执行时间不能为空',
          type: 'warning'
        })
        return false
      } else if (crud.form.taskCases.length < 1) {
        this.$message({
          message: '请关联测试用例',
          type: 'warning'
        })
        return false
      } else if (!crud.form.tExpression) {
        this.$message({
          message: '定时策略不能为空',
          type: 'warning'
        })
        return false
      } else if (crud.form.isNotify && userNotifications.length === 0) {
        this.$message({
          message: '请选择通知',
          type: 'warning'
        })
        return false
      }
      crud.form.notifications = userNotifications
      return true
    },
    changeEnabled(data, val) {
      this.$confirm('此操作将' + this.dict.label.enable_status[!val] + '此任务, 是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        crudTask.edit(data).then(res => {
          this.crud.notify(this.dict.label.enable_status[!val] + '成功', CRUD.NOTIFICATION_TYPE.SUCCESS)
        }).catch(() => {
          data.isPause = !data.isPause
        })
      }).catch(() => {
        data.isPause = !data.isPause
      })
    },
    checkboxT(row, rowIndex) {
      return row.id !== 1
    },
    openCron() {
      this.cronPopover = true
    },
    // 查询执行机
    getMachines() {
      this.options = []
      const sort = 'id,desc'
      const params = { sort: sort, projectID: sessionStorage.getItem('userProjectID') }
      let machine = {}
      setTimeout(() => {
        getMachines(params).then((res) => {
          for (let i = 0; i < res.content.length; i++) {
            machine = res.content[i]
            this.options.push(machine)
          }
        })
      }, 100)
    },
    // 查询环境
    getEnvs() {
      this.envs = []
      const sort = 'id,asc'
      const params = { sort: sort, projectID: sessionStorage.getItem('userProjectID') }
      let t_env = {}
      setTimeout(() => {
        getTestEnv(params).then((res) => {
          for (let i = 0; i < res.content.length; i++) {
            t_env = res.content[i]
            this.envs.push(t_env)
          }
        })
      }, 100)
    },
    // 查询通知
    getNotifications() {
      this.notificationOptions = []
      const sort = 'id,asc'
      const params = { sort: sort, projectID: sessionStorage.getItem('userProjectID') }
      setTimeout(() => {
        getNotification(params).then((res) => {
          // console.log('res', res)
          this.notificationOptions = res.content
        })
      }, 100)
    },
    openCronDialog() {
      this.showCron = true
      this.cronExpression = this.form.tExpression
    },
    changeCron(val) {
      this.form.tExpression = val
    },
    // 关联用例
    handleRelateCase() {
      this.relateCasesVisible = true
      this.stepTitle = '关联用例'
    },
    addStepKw() {
      this.addStepKwVisible = true
    },
    //删除用例
    handleDeleteCase(index, data) {
      this.form.taskCases.splice(index, 1)
      this.sortCases()
      this.getLastPageCase()
    },
    selectCases(data) {
      // console.log('action', data)
      if (data.action === 'close') {
        this.relateCasesVisible = false
      } else {
        const count = this.form.taskCases.length
        const addList = data.cases
        for (let i = 0; i < addList.length; i++) {
          addList[i]['executeSort'] = count + i + 1
          this.form.taskCases.push(addList[i])
        }
        this.relateCasesVisible = false
        this.getLastPageCase()
      }
    },
    // 行拖拽
    rowDrop() {
      // 此时找到的元素是要拖拽元素的父容器
      const tbody = document.querySelector('.sort-table tbody')
      const _this = this
      Sortable.create(tbody, {
        //  指定父元素下可被拖拽的子元素
        draggable: '.el-table__row',
        onEnd({ newIndex, oldIndex }) {
          newIndex = _this.pageSize * (_this.currentPage -1) + newIndex
          oldIndex = _this.pageSize * (_this.currentPage -1) + oldIndex
          const currRow = _this.form.taskCases.splice(oldIndex, 1)[0]
          _this.form.taskCases.splice(newIndex, 0, currRow)
          _this.sortCases()
        }
      })
      this.getPageCase(this.currentPage, this.pageSize)
    },
    // 重新排序
    sortCases() {
      for (let i = 0; i < this.form.taskCases.length; i++) {
        this.form.taskCases[i].executeSort = i + 1
      }
    },
    toRun(data) {
      // console.log('data: ', data)
      this.runConf.env.id = null
      this.runConf.machine.id = null
      this.getMachines()
      this.getEnvs()
      this.runFormVisible = true
      this.runTask = data
    },
    startRun() {
      // console.log('task: ', task)
      if (!this.runConf.env.id) {
        this.$message.error('请选择环境')
        return false
      } else if (!this.runConf.machine.id) {
        this.$message.error('请选择执行机')
        return false
      }
      this.submitLoading = true
      const now = new Date().getTime()
      const data = {
        name: '【手动执行】' + this.runTask.name,
        runType: 'task',
        runEnv: this.runConf.env,
        runMachine: this.runConf.machine,
        taskId: this.runTask.id,
        status: 'pending',
        projectID: sessionStorage.getItem('userProjectID'),
        startTime: now
      }
      setTimeout(() => {
        createRunTask(data).then((res) => {
          if (typeof res === 'number') {
            this.$message({
              message: '任务执行成功',
              type: 'success'
            })
            this.submitLoading = false
            this.runFormVisible = false
          } else {
            this.$message({
              message: '任务执行失败',
              type: 'danger'
            })
          }
        })
      }, 300)
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
  border-radius: 50%;
  /* 将border-radius设置为50%，使得Tag变成圆形 */
  padding: 0;
  /* 移除padding，使得Tag内容与边缘重合 */
  width: 12px;
  /* 设置宽度 */
  height: 12px;
  /* 设置高度，确保宽高相等以形成完美圆形 */
}
</style>
