<template>
  <div class="app-container">
    <!--工具栏-->
    <div class="head-container">
      <div v-if="crud.props.searchToggle">
        <!-- 搜索 -->
        <el-input v-model="query.blurry" clearable size="small" placeholder="输入名称搜索" style="width: 200px;" class="filter-item" @keyup.enter.native="crud.toQuery" />
        <el-select v-model="selectedEnvId" class="filter-item" placeholder="环境选择" @change="changeEnv">
          <el-option
            v-for="(item, index) in envDatas"
            :key="index"
            :label="item.name"
            :value="item.id"
            style="font-weight: 700;" />
        </el-select>
        <date-range-picker v-model="query.createTime" class="date-item" />
        <rrOperation />
      </div>
      <crudOperation :permission="permission" />
    </div>
    <!--表单组件-->
    <el-dialog append-to-body :close-on-click-modal="false" :before-close="crud.cancelCU" :visible.sync="crud.status.cu > 0" :title="crud.status.title" width="500px">
      <el-form ref="form" inline :model="form" :rules="rules" size="small" label-width="80px">
        <el-form-item label="环境" prop="testEnv">
          <el-select v-model="form.testEnv.id" placeholder="环境选择">
            <el-option
              v-for="(item, index) in envDatas"
              :key="index"
              :label="item.name"
              :value="item.id"
              style="font-weight: 700;" />
          </el-select>
        </el-form-item>
        <el-form-item label="参数名称" prop="name">
          <el-input v-model="form.name" style="width: 370px;" />
        </el-form-item>
        <el-form-item label="参数值" prop="value">
          <el-input v-model="form.value" style="width: 370px;" type="textarea" :autosize="{ minRows: 1, maxRows: 10}" />
        </el-form-item>
        <el-form-item label="参数说明" prop="desc">
          <el-input v-model="form.desc" style="width: 370px;" type="textarea" :autosize="{ minRows: 1, maxRows: 10}" />
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
      <el-table-column label="环境" prop="testEnv.name" />
      <el-table-column label="参数名称" prop="name" />
      <el-table-column :show-overflow-tooltip="true" label="参数值" prop="value" />
      <el-table-column :show-overflow-tooltip="true" label="参数说明" prop="desc" />
      <el-table-column :show-overflow-tooltip="true" prop="createBy" label="创建人" />
      <el-table-column :show-overflow-tooltip="true" prop="createTime" label="创建日期" sortable />
      <el-table-column :show-overflow-tooltip="true" prop="updateBy" label="更新人" />
      <el-table-column :show-overflow-tooltip="true" prop="updateTime" label="更新日期" sortable />
      <el-table-column v-if="checkPer(['admin','commonParam:edit','commonParam:del'])" label="操作" width="130px" align="center" fixed="right">
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
import crudCommonParam from '@/api/testPlatform/commonParam'
import { getTestEnv } from '@/api/testPlatform/testEnv'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import CRUD, { presenter, header, form, crud } from '@crud/crudnew'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import udOperation from '@crud/UD.operation'
import DateRangePicker from '@/components/DateRangePicker'
import pagination from '@crud/Pagination'

const defaultForm = { id: null, name: null, value: '', testEnv: {}, desc: '' }
export default {
  name: 'ConmmonParam',
  components: { crudOperation, rrOperation, udOperation, DateRangePicker, pagination },
  cruds() {
    return CRUD({ title: '参数', url: '/api/commonParam', crudMethod: { ...crudCommonParam }})
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  // 设置数据字典
  data() {
    return {
      envDatas: [], selectedEnvId: null,
      rules: {
        name: [
          { required: true, message: '请输入名称', trigger: 'blur' }
        ],
        value: [
          { required: true, message: '请输入参数值', trigger: 'blur' }
        ],
        testEnv: [
          { required: true, message: '请选择环境', trigger: 'blur' }
        ]
      },
      permission: {
        add: ['admin', 'commonParam:add'],
        edit: ['admin', 'commonParam:edit'],
        del: ['admin', 'commonParam:del']
      }
    }
  },
  mounted: function() {
    this.getTestEnv()
    // rrOperation
    const that = this
    window.onresize = function temp() {
      that.height = document.documentElement.clientHeight - 180 + 'px;'
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
          message: '参数名称不能为空',
          type: 'warning'
        })
        return false
      }
      if (!this.form.value) {
        this.$message({
          message: '参数值不能为空',
          type: 'warning'
        })
        return false
      }
      if (!this.form.testEnv.id) {
        this.$message({
          message: '环境不能为空',
          type: 'warning'
        })
        return false
      }
      return true
    },
    checkboxT(row, rowIndex) {
      return 1 === 1
    },
    getTestEnv() {
      const sort = 'id,dsc'
      const params = { sort: sort, projectID: sessionStorage.getItem('userProjectID') }
      setTimeout(() => {
        getTestEnv(params).then(res => {
          this.envDatas = res.content
        })
      }, 100)
    },
    changeEnv(value) {
      this.crud.query.envId = value
      this.crud.toQuery()
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
  .env-group{
    display: table-row;
    margin-top: 10px;
    margin-bottom: 10px;
  }
  .button-margin {
  margin-right: 10px; /* 调整间距的大小 */
  margin-bottom: 10px; /* 调整间距的大小 */
  }
  .button-margin:last-child {
    margin-right: 0; /* 最后一个按钮不需要右间距 */
  }
</style>
