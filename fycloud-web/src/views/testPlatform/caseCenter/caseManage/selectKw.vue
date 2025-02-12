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
        <el-dialog append-to-body :close-on-click-modal="false" :before-close="crud.cancelCU" :visible.sync="crud.status.cu > 0" title="关键字详情" width="800px">
          <el-form ref="form" :inline="true" :model="form" :rules="rules" size="medium" label-width="100px" :disabled="true">
            <el-form-item label="关键字名称" prop="name">
              <el-input v-model="form.name" @keydown.native="keydown($event)" />
            </el-form-item>
            <el-form-item label="模块" prop="category">
              <el-input v-model="form.category.name" @keydown.native="keydown($event)" />
            </el-form-item>
            <el-form-item label="module路径" prop="moudlePath">
              <el-input v-model="form.moudlePath" @keydown.native="keydown($event)" />
            </el-form-item>
            <el-form-item label="函数名称" prop="funcName">
              <el-input v-model="form.funcName" @keydown.native="keydown($event)" />
            </el-form-item>
            <el-form-item label="关键字描述" prop="desc">
              <el-input v-model="form.desc" type="textarea" style="width: 500px;" :rows="3" />
            </el-form-item>
            <el-form-item
              v-for="(kwParamConf, index) in form.kwParamConfs"
              :key="index"
              :label="`参数${index+1}`"
              prop="kwParamConfs"
            >
              <el-input v-model="kwParamConf.name" type="input" placeholder="输入参数名称" style="width: 150px;" />
              <el-select v-model="kwParamConf.dataType" placeholder="输入数据类型" style="width: 150px;">
                <el-option
                  v-for="item in dict.kw_data_type"
                  :key="item.id"
                  :label="item.label"
                  :value="item.value"
                >{{ item.label }}</el-option>
              </el-select>
              <el-input v-model="kwParamConf.desc" type="input" placeholder="输入参数说明" style="width: 200px;" />
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="text" @click="crud.cancelCU">取消</el-button>
          </div>
        </el-dialog>
        <!--表格渲染-->
        <el-table
          ref="table"
          v-loading="crud.loading"
          :data="crud.data"
          style="width: 100%;"
          :header-cell-class-name="cellClass"
          @selection-change="handleSelectionChange"
          @select="handleSelect"
          @row-click="handleRowClick">
          <el-table-column type="selection" width="50" align="center" />
          <el-table-column :show-overflow-tooltip="true" prop="name" label="名称" sortable>
            <template slot-scope="scope">
              <a style="color:#458BE0;" @click="crud.toEdit(scope.row)">{{ scope.row.name }}</a>
            </template>
          </el-table-column>
          <el-table-column :show-overflow-tooltip="true" prop="category" label="模块">
            <template slot-scope="scope">
              <div>{{ scope.row.category.name }}</div>
            </template>
          </el-table-column>
          <el-table-column :show-overflow-tooltip="true" prop="desc" label="说明" />
          <!-- <el-table-column :show-overflow-tooltip="true" prop="createBy" label="创建人" />
          <el-table-column :show-overflow-tooltip="true" prop="createTime" label="创建日期" sortable />
          <el-table-column :show-overflow-tooltip="true" prop="updateBy" label="更新人" /> -->
          <el-table-column :show-overflow-tooltip="true" prop="updateTime" label="更新日期" sortable />
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
import crudKw from '@/api/testPlatform/kw'
import { getCategorys, getCategorySuperior } from '@/api/testPlatform/kwCategory'
import CRUD, { presenter, header, form, crud } from '@crud/crud'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import udOperation from '@crud/UD.operation'
import pagination from '@crud/Pagination'
import DateRangePicker from '@/components/DateRangePicker'
import Treeselect from '@riophae/vue-treeselect'
import { mapGetters } from 'vuex'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import { LOAD_CHILDREN_OPTIONS } from '@riophae/vue-treeselect'
const defaultForm = { id: null, name: null, moudlePath: null, funcName: null, desc: null, category: { id: null }, kwParamConfs: [{ name: '', dataType: '', desc: '' }] }
export default {
  name: 'KwTableComponent',
  components: { Treeselect, crudOperation, rrOperation, udOperation, pagination, DateRangePicker },
  cruds() {
    return CRUD({ title: '关键字', url: 'api/kw', crudMethod: { ...crudKw }, optShow: {
      add: false,
      edit: false,
      del: false,
      download: false,
      reset: true
    }})
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  dicts: ['kw_data_type'],
  data() {
    return {
      height: document.documentElement.clientHeight - 180 + 'px;',
      categoryName: '', Categorys: [], categoryDatas: [],
      defaultProps: { children: 'children', label: 'name', isLeaf: 'leaf' },
      selectedData: [],
      isRadioMode: true, // 是否为单选模式
      permission: {
        add: ['admin', 'kw:add'],
        edit: ['admin', 'kw:edit'],
        del: ['admin', 'kw:del']
      },
      rules: {
        name: [
          { required: true, message: '请输入关键字名称', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        moudlePath: [
          { required: true, message: '请输入module路径', trigger: 'blur' },
          { min: 5, max: 20, message: '长度在 5 到 20 个字符', trigger: 'blur' }
        ],
        funcName: [
          { required: true, message: '请输入函数名称', trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ],
        categoryId: [
          { required: true, message: '请选择模块', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapGetters([
      'kw'
    ])
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
          message: '关键字名称不能为空',
          type: 'warning'
        })
        return false
      } else if (!crud.form.moudlePath) {
        this.$message({
          message: 'moudlePath不能为空',
          type: 'warning'
        })
        return false
      } else if (!crud.form.funcName) {
        this.$message({
          message: 'funcName不能为空',
          type: 'warning'
        })
        return false
      }
      return true
    },
    // 获取左侧部门数据
    getCategoryDatas(node, resolve) {
      const sort = 'id,desc'
      const params = { sort: sort }
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
    closeDialog(action) {
      if (action === 'close') {
        this.$emit('handleSelectKw', { action: 'close', kw: null })
      } else {
        this.$emit('handleSelectKw', { action: 'sendKw', kw: this.selectedData[0] })
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
