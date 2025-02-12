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
    <el-dialog append-to-body :close-on-click-modal="false" :before-close="crud.cancelCU" :visible.sync="crud.status.cu > 0" :title="crud.status.title" width="800px">
      <el-form ref="form" inline :model="form" :rules="rules" size="small" label-width="80px">
        <el-form-item label="通知名称" prop="name">
          <el-input v-model="form.name" style="width: 370px;" />
        </el-form-item>
        <el-form-item label="通知类型" prop="nType">
          <el-radio-group v-model="form.nType">
            <el-radio :label="1">邮件</el-radio>
            <el-radio :label="2">微信</el-radio>
            <el-radio :label="3">飞书</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.nType !== 1" label="Webhook" prop="webhook">
          <el-input v-model="form.webhook" style="width: 500px;" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item v-if="form.nType === 1" label="发送者" prop="sender">
          <el-input v-model="form.sender" style="width: 500px;" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item v-if="form.nType === 1" label="接收者" prop="receiver">
          <el-input v-model="form.receiver" style="width: 500px;" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item v-if="form.nType === 1" label="抄送" prop="cc">
          <el-input v-model="form.cc" style="width: 500px;" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item v-if="form.nType === 1" label="密送" prop="bcc">
          <el-input v-model="form.bcc" style="width: 500px;" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="text" @click="crud.cancelCU">取消</el-button>
        <el-button :loading="crud.status.cu === 2" type="primary" @click="crud.submitCU">确认</el-button>
      </div>
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
      @selection-change="crud.selectionChangeHandler">
      <el-table-column :selectable="checkboxT" type="selection" width="55" />
      <el-table-column label="序号" type="index" width="50" />
      <el-table-column label="名称" prop="name" />
      <el-table-column label="通知类型" prop="nType">
        <template slot-scope="scope">
          <div v-if="scope.row.nType===1" class="filter-item">
            <span>邮件</span>
          </div>
          <div v-if="scope.row.nType===2" class="filter-item">
            <span>微信</span>
          </div>
          <div v-if="scope.row.nType===3" class="filter-item">
            <span>飞书</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column :show-overflow-tooltip="true" prop="createBy" label="创建人" />
      <el-table-column :show-overflow-tooltip="true" prop="createTime" label="创建日期" sortable />
      <el-table-column :show-overflow-tooltip="true" prop="updateBy" label="更新人" />
      <el-table-column :show-overflow-tooltip="true" prop="updateTime" label="更新日期" sortable />
      <el-table-column v-if="checkPer(['admin','notification:edit','notification:del'])" label="操作" width="130px" align="center" fixed="right">
        <template slot-scope="scope">
          <udOperation
            :data="scope.row"
            :permission="permission"
            msg="确定删除吗?此操作不能撤销！" />
        </template>
      </el-table-column>
    </el-table>
    <pagination />
  </div>
</template>

<script>
import crudNotification from '@/api/testPlatform/notification'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import CRUD, { presenter, header, form, crud } from '@crud/crudnew'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import udOperation from '@crud/UD.operation'
import DateRangePicker from '@/components/DateRangePicker'
import pagination from '@crud/Pagination'

const defaultForm = { id: null, name: null, nType: 1, webhook: '', sender: '', receiver: '', cc: '', bcc: '' }
export default {
  name: 'Notification',
  components: { crudOperation, rrOperation, udOperation, DateRangePicker, pagination },
  cruds() {
    return CRUD({ title: '通知', url: '/api/notification', crudMethod: { ...crudNotification }})
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  // 设置数据字典
  data() {
    return {
      rules: {
        name: [
          { required: true, message: '请输入名称', trigger: 'blur' }
        ],
        nType: [
          { required: true, message: '请选择通知类型', trigger: 'blur' }
        ]
      },
      permission: {
        add: ['admin', 'notification:add'],
        edit: ['admin', 'notification:edit'],
        del: ['admin', 'notification:del']
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
</style>
