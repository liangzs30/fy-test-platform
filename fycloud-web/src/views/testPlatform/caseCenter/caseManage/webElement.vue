<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!--侧边模块数据-->
      <el-col :xs="9" :sm="6" :md="5" :lg="4" :xl="4">
        <div class="head-container">
          <el-input
            v-model="categoryName"
            clearable
            size="small"
            placeholder="输入模块名称搜索"
            prefix-icon="el-icon-search"
            class="filter-item"
            @input="getCategoryDatas"
          />
        </div>
        <el-tree
          :data="categoryDatas"
          :load="getCategoryDatas"
          :props="defaultProps"
          :expand-on-click-node="false"
          lazy
          @node-click="handleNodeClick"
        />
      </el-col>
      <!--关键字数据-->
      <el-col :xs="15" :sm="18" :md="19" :lg="20" :xl="20">
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
            <date-range-picker v-model="query.createTime" class="date-item" />
            <rrOperation />
          </div>
          <crudOperation show="" :permission="permission" />
        </div>
        <!--表单渲染-->
        <el-dialog append-to-body :close-on-click-modal="false" :before-close="crud.cancelCU" :visible.sync="crud.status.cu > 0" :title="crud.status.title" width="700px">
          <el-form ref="form" :inline="true" :model="form" :rules="rules" disabled="true" size="medium" label-width="100px">
            <el-form-item label="元素名称" prop="name">
              <el-input v-model="form.name" style="width: 200px;" @keydown.native="keydown($event)" />
            </el-form-item>
            <el-form-item label="模块" prop="category.id">
              <treeselect
                v-model="form.category.id"
                :options="Categorys"
                :load-options="loadCategorys"
                style="width: 200px"
                placeholder="选择模块"
              />
            </el-form-item>
            <el-form-item label="定位方式" prop="findType">
              <el-select v-model="form.findType" placeholder="选择定位方式" style="width: 200px;">
                <el-option
                  v-for="item in dict.webel_find_type"
                  :key="item.id"
                  :label="item.label"
                  :value="item.value">{{ item.label }}</el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="表达式" prop="expression">
              <el-input v-model="form.expression" style="width: 200px;" @keydown.native="keydown($event)" />
            </el-form-item>
            <el-form-item label="元素描述" prop="desc">
              <el-input v-model="form.desc"  style="width: 500px;" />
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="text" @click="crud.cancelCU">取消</el-button>
            <el-button :loading="crud.status.cu === 2" type="primary" @click="crud.submitCU">确认</el-button>
          </div>
        </el-dialog>
        <!--表格渲染-->
        <el-table ref="table"
          v-loading="crud.loading"
          :data="crud.data"
          style="width: 100%;"
          :header-cell-class-name="cellClass"
          @selection-change="handleSelectionChange"
          @select="handleSelect"
          @row-click="handleRowClick">
          <el-table-column type="selection" width="50" />
          <el-table-column :show-overflow-tooltip="true" prop="name" label="元素名称" sortable>
            <template slot-scope="scope">
              <a style="color:#458BE0;" @click="crud.toEdit(scope.row)">{{ scope.row.name }}</a>
            </template>
          </el-table-column>
          <el-table-column :show-overflow-tooltip="true" prop="category.name" label="模块" />
          <el-table-column :show-overflow-tooltip="true" prop="desc" label="元素说明" />
          <el-table-column :show-overflow-tooltip="true" prop="createBy" label="创建人" />
          <el-table-column :show-overflow-tooltip="true" prop="createTime" label="创建日期" sortable />
          <!-- <el-table-column :show-overflow-tooltip="true" prop="updateBy" label="更新人" />
          <el-table-column :show-overflow-tooltip="true" prop="updateTime" label="更新日期" sortable /> -->
        </el-table>
        <!--分页组件-->
        <pagination />
      </el-col>
      <div style="margin-left: 660px;">
        <el-button @click="closeDialog('close')">取 消</el-button>
        <el-button type="primary" @click="closeDialog('send')">确 定</el-button>
      </div>
    </el-row>
  </div>
</template>

<script>
import crudWebElement from '@/api/testPlatform/webElement'
import { getCategorys, getCategorySuperior } from '@/api/testPlatform/webElementCategory'
import CRUD, { presenter, header, form, crud } from '@crud/crudnew'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import udOperation from '@crud/UD.operation'
import pagination from '@crud/Pagination'
import DateRangePicker from '@/components/DateRangePicker'
import Treeselect from '@riophae/vue-treeselect'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import { LOAD_CHILDREN_OPTIONS } from '@riophae/vue-treeselect'
const defaultForm = { id: null, name: null, findType: null, expression: null, desc: null, category: { id: null }, projectID: null }
export default {
  name: 'Webelement',
  components: { Treeselect, crudOperation, rrOperation, udOperation, pagination, DateRangePicker },
  cruds() {
    return CRUD({ title: 'WEB元素', url: '/api/web/element', crudMethod: { ...crudWebElement }, optShow: {
      add: false,
      edit: false,
      del: false,
      download: false,
      reset: true
    }})
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  dicts: ['webel_find_type'],
  data() {
    return {
      height: document.documentElement.clientHeight - 180 + 'px;',
      categoryName: '', Categorys: [], categoryDatas: [],
      selectedData: [],
      isRadioMode: true, // 是否为单选模式
      defaultProps: { children: 'children', label: 'name', isLeaf: 'leaf' },
      permission: {
        add: ['admin', 'webelement:add'],
        edit: ['admin', 'webelement:edit'],
        del: ['admin', 'webelement:del']
      },
      rules: {
        name: [
          { required: true, message: '请输入元素名称', trigger: 'blur' },
          { min: 2, max: 32, message: '长度在 2 到 32 个字符', trigger: 'blur' }
        ],
        findType: [
          { required: true, message: '请选择定位方式', trigger: 'blur' },
          { min: 2, max: 64, message: '长度在 2 到 64 个字符', trigger: 'blur' }
        ],
        expression: [
          { required: true, message: '请输入表达式', trigger: 'blur' },
          { min: 5, max: 20, message: '长度在 5 到 20 个字符', trigger: 'blur' }
        ],
        categoryId: [
          { required: true, message: '请选择模块', trigger: 'blur' }
        ]
      }
    }
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
    // 新增与编辑前做的操作
    [CRUD.HOOK.afterToCU](crud, form) {
      this.form.projectID = sessionStorage.getItem('userProjectID')
      if (form.id == null) {
        this.getCategorys()
      } else {
        this.getSupCategorys(form.category.id)
      }
    },
    // 提交前做的操作
    [CRUD.HOOK.afterValidateCU](crud) {
      if (!crud.form.category.id) {
        this.$message({
          message: '模块不能为空',
          type: 'warning'
        })
        return false
      } else if (!crud.form.name) {
        this.$message({
          message: '元素名称不能为空',
          type: 'warning'
        })
        return false
      } else if (!crud.form.findType) {
        this.$message({
          message: '定位方式不能为空',
          type: 'warning'
        })
        return false
      } else if (!crud.form.expression) {
        this.$message({
          message: '表达式不能为空',
          type: 'warning'
        })
        return false
      }
      return true
    },
    // 获取左侧部门数据
    getCategoryDatas(node, resolve) {
      const sort = 'id,desc'
      const params = { sort: sort, projectID: sessionStorage.getItem('userProjectID') }
      if (typeof node !== 'object') {
        if (node) {
          params['name'] = node
        }
      } else if (node.level !== 0) {
        params['pid'] = node.data.id
      }
      setTimeout(() => {
        getCategorys(params).then(res => {
          if (resolve) {
            resolve(res.content)
          } else {
            this.categoryDatas = res.content
          }
        })
      }, 100)
    },
    getCategorys() {
      getCategorys({}).then(res => {
        this.Categorys = res.content.map(function(obj) {
          if (obj.hasChildren) {
            obj.children = null
          }
          return obj
        })
      })
    },
    getSupCategorys(categoryId) {
      getCategorySuperior(categoryId).then(res => {
        const date = res.content
        this.buildCategorys(date)
        this.Categorys = date
      })
    },
    buildCategorys(Categorys) {
      Categorys.forEach(data => {
        if (data.children) {
          this.buildCategorys(data.children)
        }
        if (data.hasChildren && !data.children) {
          data.children = null
        }
      })
    },
    // 获取弹窗内部门数据
    loadCategorys({ action, parentNode, callback }) {
      if (action === LOAD_CHILDREN_OPTIONS) {
        getCategorys({ pid: parentNode.id }).then(res => {
          parentNode.children = res.content.map(function(obj) {
            if (obj.hasChildren) {
              obj.children = null
            }
            return obj
          })
          setTimeout(() => {
            callback()
          }, 200)
        })
      }
    },
    // 切换模块
    handleNodeClick(data) {
      if (data.pid === 0) {
        this.query.categoryId = null
      } else {
        this.query.categoryId = data.id
      }
      this.crud.toQuery()
    },
    handleSelectionChange(val) {
      this.selectedData = val // 保存选定数据的数据
    },
    // 列表勾选框勾选
    handleSelect(selection, row) {
      if (!this.isRadioMode) return
      this.$refs['table'].clearSelection()
      if (selection.length != 0) {
        row.flag = true
        this.$refs['table'].toggleRowSelection(row, true)
      } else {
        row.flag = false
        this.$refs['table'].toggleRowSelection(row, false)
      }
    },
    // 行被点击
    handleRowClick(row) {
      const flag = !row.flag
      if (this.isRadioMode) {
        this.$refs['table'].clearSelection()
        this.crud.data.forEach((v) => {
          v.flag = false
        })
      }
      row.flag = flag
      this.$refs['table'].toggleRowSelection(row, flag)
    },
    // 单选模式下隐藏全选勾选框
    cellClass(row) {
      if (this.isRadioMode && row.columnIndex === 0) {
        return 'hideCheckbox'
      }
    },
    // 查看详情
    handleView(data) {
      this.crud.status.cu = 2
    },
    closeDialog(action) {
      if (action === 'close') {
        this.$emit('handleSelectWebelement', { action: 'close', element: null })
      } else {
        this.$emit('handleSelectWebelement', { action: 'sendElement', element: this.selectedData[0] })
      }
      this.$refs['table'].clearSelection()
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
  ::v-deep .vue-treeselect__control,::v-deep .vue-treeselect__placeholder,::v-deep .vue-treeselect__single-value {
    height: 30px;
    line-height: 30px;
  }
  .el-table {
    ::v-deep {
      .hideCheckbox {
        pointer-events: none;
        .el-checkbox {
          display: none;
        }
      }
    }
  }
</style>
