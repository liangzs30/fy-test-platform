<template>
  <div class="app-container">
    <!--工具栏-->
    <div class="head-container">
      <div v-if="crud.props.searchToggle">
        <!-- 搜索 -->
        <el-input v-model="query.name" clearable size="small" placeholder="输入名称搜索" style="width: 200px;" class="filter-item" @keyup.enter.native="crud.toQuery" />
        <date-range-picker v-model="query.createTime" class="date-item" />
        <rrOperation />
      </div>
      <crudOperation :permission="permission" />
    </div>
    <!--表单组件-->
    <el-dialog append-to-body :close-on-click-modal="false" :before-close="crud.cancelCU" :visible.sync="crud.status.cu > 0" :title="crud.status.title" width="500px">
      <el-form ref="form" inline :model="form" :rules="rules" size="small" label-width="80px">
        <el-form-item label="模块名称" prop="name">
          <el-input v-model="form.name" style="width: 370px;" />
        </el-form-item>
        <el-form-item label="模块排序" prop="categorySort">
          <el-input-number
            v-model.number="form.categorySort"
            :min="0"
            :max="999"
            controls-position="right"
            style="width: 370px;"
          />
        </el-form-item>
        <el-form-item label="顶级模块">
          <el-radio-group v-model="form.isTop" style="width: 140px">
            <el-radio label="1">是</el-radio>
            <el-radio label="0">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.isTop === '0'" style="margin-bottom: 0;" label="上级模块" prop="pid">
          <treeselect
            v-model="form.pid"
            :load-options="loadCategorys"
            :options="categorys"
            style="width: 370px;"
            placeholder="选择上级模块"
          />
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
      :load="getCategoryDatas"
      :tree-props="{children: 'children', hasChildren: 'hasChildren'}"
      :data="crud.data"
      row-key="id"
      @select="crud.selectChange"
      @select-all="crud.selectAllChange"
      @selection-change="crud.selectionChangeHandler"
    >
      <el-table-column :selectable="checkboxT" type="selection" width="55" />
      <el-table-column label="名称" prop="name" />
      <el-table-column label="排序" prop="categorySort" />
      <el-table-column :show-overflow-tooltip="true" prop="createBy" label="创建人" />
      <el-table-column :show-overflow-tooltip="true" prop="createTime" label="创建日期" sortable />
      <el-table-column :show-overflow-tooltip="true" prop="updateBy" label="更新人" />
      <el-table-column :show-overflow-tooltip="true" prop="updateTime" label="更新日期" sortable />
      <el-table-column v-if="checkPer(['admin','kwCategory:edit','kwCategory:del'])" label="操作" width="130px" align="center" fixed="right">
        <template slot-scope="scope">
          <udOperation
            :data="scope.row"
            :permission="permission"
            msg="确定删除吗?，此操作不能撤销！"
          />
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import crudElCategory from '@/api/testPlatform/webElementCategory'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import { LOAD_CHILDREN_OPTIONS } from '@riophae/vue-treeselect'
import CRUD, { presenter, header, form, crud } from '@crud/crudnew'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import udOperation from '@crud/UD.operation'
import DateRangePicker from '@/components/DateRangePicker'

const defaultForm = { id: null, name: null, isTop: '1', subCount: 0, pid: null, castegorySort: 999, projectID: null }
export default {
  name: 'KwCategory',
  components: { Treeselect, crudOperation, rrOperation, udOperation, DateRangePicker },
  cruds() {
    return CRUD({ title: '模块', url: '/api/elementCategory', crudMethod: { ...crudElCategory }})
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  // 设置数据字典
  dicts: ['case_category_status'],
  data() {
    return {
      categorys: [],
      rules: {
        name: [
          { required: true, message: '请输入名称', trigger: 'blur' }
        ],
        castegorySort: [
          { required: true, message: '请输入序号', trigger: 'blur', type: 'number' }
        ]
      },
      permission: {
        add: ['admin', 'webelement:add'],
        edit: ['admin', 'webelement:edit'],
        del: ['admin', 'webelement:del']
      }
    }
  },
  methods: {
    getCategoryDatas(tree, treeNode, resolve) {
      const params = { pid: tree.id }
      setTimeout(() => {
        crudElCategory.getCategorys(params).then(res => {
          resolve(res.content)
        })
      }, 100)
    },
    // 新增与编辑前做的操作
    [CRUD.HOOK.afterToCU](crud, form) {
      this.form.projectID = sessionStorage.getItem('userProjectID')
      if (form.pid !== null) {
        form.isTop = '0'
      } else if (form.id !== null) {
        form.isTop = '1'
      }
      form.enabled = `${form.enabled}`
      if (form.id != null) {
        this.getSupcategorys(form.id)
      } else {
        this.getCategorys()
      }
    },
    getSupcategorys(id) {
      crudElCategory.getCategorySuperior(id, true).then(res => {
        const data = res.content
        this.buildCategorys(data)
        this.categorys = data
      })
    },
    buildCategorys(categorys) {
      categorys.forEach(data => {
        if (data.children) {
          this.buildCategorys(data.children)
        }
        if (data.hasChildren && !data.children) {
          data.children = null
        }
      })
    },
    getCategorys() {
      crudElCategory.getCategorys({}).then(res => {
        this.categorys = res.content.map(function(obj) {
          if (obj.hasChildren) {
            obj.children = null
          }
          return obj
        })
      })
    },
    // 获取弹窗内部门数据
    loadCategorys({ action, parentNode, callback }) {
      if (action === LOAD_CHILDREN_OPTIONS) {
        crudElCategory.getCategorys({ pid: parentNode.id }).then(res => {
          parentNode.children = res.content.map(function(obj) {
            if (obj.hasChildren) {
              obj.children = null
            }
            return obj
          })
          setTimeout(() => {
            callback()
          }, 100)
        })
      }
    },
    // 提交前的验证
    [CRUD.HOOK.afterValidateCU]() {
      if (this.form.pid !== null && this.form.pid === this.form.id) {
        this.$message({
          message: '上级模块不能为空',
          type: 'warning'
        })
        return false
      }
      if (this.form.isTop === '1') {
        this.form.pid = null
      }
      return true
    },
    checkboxT(row, rowIndex) {
      return 1 === 1
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
