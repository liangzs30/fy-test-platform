<template>
  <div class="app-container">
    <!--工具栏-->
    <div class="head-container">
      <div v-if="crud.props.searchToggle">
        <!-- 搜索 -->
        <el-input v-model="query.blurry" clearable size="small" placeholder="输入名称搜索" style="width: 200px;" class="filter-item" @keyup.enter.native="crud.toQuery" />
        <date-range-picker v-model="query.createTime" class="date-item" />
        <rrOperation />
      </div>
      <crudOperation :permission="permission" />
    </div>
    <!--表单组件-->
    <el-dialog append-to-body :close-on-click-modal="false" :before-close="crud.cancelCU" :visible.sync="crud.status.cu > 0" :title="crud.status.title" width="500px">
      <el-form ref="form" inline :model="form" :rules="rules" size="small" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" style="width: 200px;" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="text" @click="crud.cancelCU">取消</el-button>
        <el-button :loading="crud.status.cu === 2" type="primary" @click="crud.submitCU">确认</el-button>
      </div>
    </el-dialog>
    <!--详情组件-->
    <el-dialog append-to-body :close-on-click-modal="false" :visible.sync="detailFormVisible" title="执行机详情" width="600px">
      <span>唯一码：{{ uniqueCode }}</span>
      <el-button
        v-if="taskData.length > 0"
        type="primary"
        size="mini"
        style="margin-left: 50px;"
        @click="cancelAllTask">一键取消
      </el-button>
      <el-divider content-position="center">测试任务</el-divider>
      <el-table
        :data="taskData"
        :default-sort = "{prop: 'createTime', order: 'ascending'}"
        row-key="id">
        <el-table-column label="序号" type="index" width="50" />
        <el-table-column label="任务名称" :show-overflow-tooltip="true" prop="name" />
        <el-table-column label="任务状态" :show-overflow-tooltip="true" prop="status">
           <template slot-scope="scope">
            <span v-if="scope.row.status === 'running'">执行中<i class="el-icon-loading" style="color: red; font-size: larger; margin-left: 5px;" /></span>
            <span v-if="scope.row.status === 'pending'">待执行<svg-icon icon-class="waitForExe" style="margin-left: 5px; font-size: larger;" /></span>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" prop="createTime" />
        <el-table-column
          fixed="right"
          label="操作"
          width="100">
          <template slot-scope="scope">
            <el-button v-if="scope.row.status === 'running'" @click="handleStopTask(scope.row)" type="text" size="small">终止</el-button>
            <el-button v-if="scope.row.status === 'pending'" @click="handleCancelTask(scope.row)" type="text" size="small">取消</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page.sync="currentPage"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pageSize"
        layout="total, prev, pager, next, sizes, jumper"
        :total="totalRecords">
      </el-pagination>
    </el-dialog>
    <!--表格渲染-->
    <el-table
      ref="table"
      v-loading="crud.loading"
      lazy
      :data="crud.data"
      row-key="id"
      @select="crud.selectChange"
      @select-all="crud.selectAllChange"
      @selection-change="crud.selectionChangeHandler"
    >
      <el-table-column :selectable="checkboxT" type="selection" width="55" />
      <el-table-column label="序号" type="index" width="50" />
      <el-table-column label="名称" prop="name">
        <template slot-scope="scope">
          <a style="color:#458BE0;" @click="machineDetail(scope.row)">{{ scope.row.name }}</a>
        </template>
      </el-table-column>
      <el-table-column label="IP地址" prop="ipAddr" />
      <el-table-column label="状态" prop="status">
        <template slot-scope="scope">
          <div v-if="scope.row.status===1" class="filter-item">
            <span>在线</span>
            <el-tag type="success" effect="dark" class="tag-round"></el-tag>
          </div>
          <div v-if="scope.row.status===0" class="filter-item">
            <span>离线</span>
            <el-tag type="info" effect="dark" class="tag-round"></el-tag>
          </div>
          <div v-if="scope.row.status===2" class="filter-item">
            <span>忙碌</span>
            <el-tag type="danger" effect="dark" class="tag-round"></el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column :show-overflow-tooltip="true" prop="createBy" label="创建人" />
      <el-table-column :show-overflow-tooltip="true" prop="createTime" label="创建日期" sortable />
      <!-- <el-table-column :show-overflow-tooltip="true" prop="updateBy" label="更新人" />
      <el-table-column :show-overflow-tooltip="true" prop="updateTime" label="更新日期" sortable /> -->
      <el-table-column v-if="checkPer(['admin','machine:edit','machine:del'])" label="操作" width="130px" align="center" fixed="right">
        <template slot-scope="scope">
          <udOperation
            :data="scope.row"
            :permission="permission"
            msg="确定删除吗?此操作不能撤销！"
          />
        </template>
      </el-table-column>
    </el-table>
    <pagination />
  </div>
</template>

<script>
import crudExecuteMachine from '@/api/testPlatform/executeMachine'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import CRUD, { presenter, header, form, crud } from '@crud/crudnew'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import udOperation from '@crud/UD.operationMachine'
import DateRangePicker from '@/components/DateRangePicker'
import pagination from '@crud/Pagination'

const defaultForm = { id: null, name: null, ipAddr: '', uniqueCode: '', status: 0 }
export default {
  name: 'executeMachine',
  components: {crudOperation, rrOperation, udOperation, DateRangePicker, pagination },
  cruds() {
    return CRUD({ title: '执行机', url: '/api/executeMachine', crudMethod: { ...crudExecuteMachine }, optShow: {
      add: true,
      edit: false,
      del: false,
      download: false,
      reset: true
    }})
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  // 设置数据字典
  data() {
    return {
      detailFormVisible: false, uniqueCode: '', taskData:[], detailMid: null,
      totalRecords: 0,
      currentPage: 1,
      pageSize: 10,
      rules: {
        name: [
          { required: true, message: '请输入名称', trigger: 'blur' }
        ]
      },
      permission: {
        add: ['admin', 'machine:add'],
        edit: ['admin', 'machine:edit'],
        del: ['admin', 'machine:del']
      }
    }
  },
  methods: {
    // 新增前做的操作
    [CRUD.HOOK.beforeToAdd](crud, form) {
      this.form.projectID = sessionStorage.getItem('userProjectID')
    },
    // 提交前的验证
    [CRUD.HOOK.afterValidateCU]() {
      if (!this.form.name) {
        this.$message({
          message: '名称不能为空',
          type: 'warning'
        })
        return false
      }
      return true
    },
    checkboxT(row, rowIndex) {
      return row.id !== 1
    },
    machineDetail(data){
      this.detailFormVisible = true
      this.uniqueCode = data.uniqueCode
      this.detailMid = data.id
      this.getTask()
    },
    getTask(){
      const params = {mid: this.detailMid, page: this.currentPage - 1, size: this.pageSize}
      setTimeout(() => {
        crudExecuteMachine.getMachineTask(params).then((res) => {
          if (res.status === 400) {
            this.$message({
            message: '获取任务失败',
            type: 'danger'
          })} else {
            this.taskData = res.content
            this.totalRecords = res.totalElements
          }
        })
      }, 100)
    },
    handleCancelTask(row){
      this.$confirm('此操作将取消执行此任务', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          setTimeout(() => {
            crudExecuteMachine.cancelTaskById(row.id).then((res) => {
              if (res) {
                this.$message({
                message: '取消任务失败',
                type: 'danger'
              })} else {
                this.$message({
                type: 'success',
                message: '取消成功!'
              })
                this.getTask()
              }
            })
          }, 100)
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '未执行取消操作'
          })   
        })    
    },
    handleStopTask(row){
      this.$confirm('此操作将终止执行此任务', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          const task = {id: null, tid: row.id, mid: this.detailMid}
          setTimeout(() => {
            crudExecuteMachine.stopTask(task).then((res) => {
              if (res) {
                this.$message({
                  message: '终止任务失败',
                  type: 'danger'
                })
              } else {
                this.$message({
                  type: 'success',
                  message: '操作成功!'
                })
                this.getTask()
              }
            })
          }, 100)
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '取消操作'
          })         
        })    
    },
    cancelAllTask(){
      this.$confirm('将取消此执行机所有待执行任务', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          const ids = []
          this.taskData.forEach((task) => {
            if(task.status==='pending'){
              ids.push(task.id)
            }
          })
          setTimeout(() => {
            crudExecuteMachine.cancelAllTask(ids).then((res) => {
              if (res) {
                this.$message({
                message: '一键取消失败',
                type: 'danger'
              })} else {
                this.$message({
                type: 'success',
                message: '取消成功!'
              })
                this.getTask()
              }
            })
          }, 100)
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '未执行取消操作'
          })         
        })    
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
      this.getTask()
    },
    handleCurrentChange(val) {
      this.currentPage = val
      this.getTask()
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
 ::v-deep .vue-treeselect__control,::v-deep .vue-treeselect__placeholder,::v-deep .vue-treeselect__single-value {
    height: 30px;
    line-height: 30px;
  }
</style>
<style rel="stylesheet/scss" lang="scss" scoped>
 ::v-deep .el-input-number .el-input__inner {
    text-align: left;
  }
.tag-round {
  border-radius: 50%; /* 将border-radius设置为50%，使得Tag变成圆形 */
  padding: 0; /* 移除padding，使得Tag内容与边缘重合 */
  width: 12px; /* 设置宽度 */
  height: 12px; /* 设置高度，确保宽高相等以形成完美圆形 */
}
</style>
