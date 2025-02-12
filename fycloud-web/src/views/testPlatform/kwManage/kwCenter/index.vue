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
        <el-dialog append-to-body :close-on-click-modal="false" :before-close="crud.cancelCU" :visible.sync="crud.status.cu > 0" :title="crud.status.title" width="900px">
          <el-form ref="form" :inline="true" :model="form" :rules="rules" :disabled="formDisable" size="medium" label-width="100px">
            <el-form-item label="关键字名称" prop="name">
              <el-input v-model="form.name" style="width: 300px;" @keydown.native="keydown($event)" />
            </el-form-item>
            <el-form-item label="模块" prop="category">
              <treeselect
                :disabled="formDisable"
                v-model="form.category.id"
                :options="Categorys"
                :load-options="loadCategorys"
                style="width: 300px"
                placeholder="选择模块"
              />
            </el-form-item>
            <el-form-item label="类名" prop="className">
              <el-input v-model="form.className" style="width: 300px;" @keydown.native="keydown($event)" />
            </el-form-item>
            <el-form-item label="函数名称" prop="funcName">
              <el-input v-model="form.funcName" style="width: 300px;" @keydown.native="keydown($event)" />
            </el-form-item>
            <el-form-item label="返回值" prop="ifResp">
              <el-switch
                v-model="form.ifResp"
                active-color="#13ce66"
                inactive-color="#ff4949" />
            </el-form-item>
            <el-form-item label="关键字描述" prop="desc">
              <el-input v-model="form.desc"  style="width: 560px;" />
            </el-form-item>
            <el-form-item label="参数配置">
              <el-button type="primary" size="mini" @click.prevent="addKwParamConf">新增+</el-button>
            </el-form-item>
            <el-form-item
              v-for="(kwParamConf, index) in form.kwParamConfs"
              :key="index"
              :label="`参数${index+1}`"
              prop="kwParamConfs"
            >
              <el-input v-model="kwParamConf.title" type="input" placeholder="参数标题" style="width: 120px;" />
              <el-input v-model="kwParamConf.name" type="input" placeholder="参数名称" style="width: 120px;" />
              <el-select v-model="kwParamConf.inputType" placeholder="前端组件" style="width: 120px;">
                <el-option
                  v-for="item in dict.kw_input"
                  :key="item.id"
                  :label="item.label"
                  :value="item.value"
                >{{ item.label }}</el-option>
              </el-select>
              <el-input v-if="kwParamConf.inputType === 'select'" v-model="kwParamConf.pDict" type="input" placeholder="下拉框字典" style="width: 100px;" />
              <el-input v-model="kwParamConf.desc" type="input" placeholder="参数说明" style="width: 180px;" />
              <el-button type="danger" @click.prevent="rmKwParamConf(index)">删除</el-button>
            </el-form-item>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="text" @click="crud.cancelCU">取消</el-button>
            <el-button :loading="crud.status.cu === 2" type="primary" @click="crud.submitCU">确认</el-button>
          </div>
        </el-dialog>
        <!--表格渲染-->
        <el-table ref="table" v-loading="crud.loading" :data="crud.data" style="width: 100%;" @selection-change="crud.selectionChangeHandler">
          <el-table-column :selectable="checkboxT" type="selection" width="55" />
          <el-table-column :show-overflow-tooltip="true" prop="name" label="关键字名称" sortable>
            <template slot-scope="scope">
              <a style="color:#458BE0;" @click="crud.toEdit(scope.row)">{{ scope.row.name }}</a>
            </template>
          </el-table-column>
          <el-table-column :show-overflow-tooltip="true" prop="category.name" label="模块" />
          <el-table-column :show-overflow-tooltip="true" prop="desc" label="关键字说明" />
          <el-table-column :show-overflow-tooltip="true" prop="createBy" label="创建人" />
          <el-table-column :show-overflow-tooltip="true" prop="createTime" label="创建日期" sortable />
          <el-table-column :show-overflow-tooltip="true" prop="updateBy" label="更新人" />
          <el-table-column :show-overflow-tooltip="true" prop="updateTime" label="更新日期" sortable />
          <el-table-column v-if="checkPer(['admin','kw:edit','kw:del'])" label="操作" width="130px" align="center" fixed="right">
            <template slot-scope="scope">
              <udOperation
                :data="scope.row"
                :permission="permission"
                :disabled-dle="scope.row.createBy === 'admin' && !user.isAdmin"
                :disabled-edit="scope.row.createBy === 'admin' && !user.isAdmin"
                msg="确定删除吗，此操作不能撤销！"
              />
            </template>
          </el-table-column>
        </el-table>
        <!--分页组件-->
        <pagination />
      </el-col>
    </el-row>
  </div>
</template>

<script>
import crudKw from '@/api/testPlatform/kw'
import { getCategorys, getCategorySuperior } from '@/api/testPlatform/kwCategory'
import CRUD, { presenter, header, form, crud } from '@crud/crud'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import udOperation from '@crud/UD.operationKw'
import pagination from '@crud/Pagination'
import DateRangePicker from '@/components/DateRangePicker'
import Treeselect from '@riophae/vue-treeselect'
import { mapGetters } from 'vuex'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import { LOAD_CHILDREN_OPTIONS } from '@riophae/vue-treeselect'
import { title } from 'process'
const defaultForm = { id: null, name: null, className: null, funcName: null, ifResp: false, desc: null, category: { id: null }, kwParamConfs: [] }
export default {
  name: 'Kw',
  components: { Treeselect, crudOperation, rrOperation, udOperation, pagination, DateRangePicker },
  cruds() {
    return CRUD({ title: '关键字', url: 'api/kw', crudMethod: { ...crudKw }, optShow: {
      add: true,
      edit: true,
      del: true,
      download: false,
      reset: true
    }})
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  dicts: ['kw_input', 'kw_is_resp'],
  data() {
    return {
      height: document.documentElement.clientHeight - 180 + 'px;',
      categoryName: '', Categorys: [], categoryDatas: [],
      defaultProps: { children: 'children', label: 'name', isLeaf: 'leaf' },
      formDisable: false,
      permission: {
        add: ['admin', 'kw:add'],
        edit: ['admin', 'kw:edit'],
        del: ['admin', 'kw:del']
      },
      rules: {
        name: [
          { required: true, message: '请输入关键字名称', trigger: 'blur' },
          { min: 2, max: 32, message: '长度在 2 到 32 个字符', trigger: 'blur' }
        ],
        className: [
          { required: true, message: '请输入类名', trigger: 'blur' },
          { min: 5, max: 64, message: '长度在 5 到 64 个字符', trigger: 'blur' }
        ],
        funcName: [
          { required: true, message: '请输入函数名称', trigger: 'blur' },
          { min: 5, max: 64, message: '长度在 5 到 64 个字符', trigger: 'blur' }
        ],
        categoryId: [
          { required: true, message: '请选择模块', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapGetters([
      'user'
    ])
  },
  created() {
    // console.log(this.user)
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
    // 新增前做的操作
    [CRUD.HOOK.beforeToAdd](crud, form) {
      this.formDisable = false
    },
    // 编辑前做的操作
    [CRUD.HOOK.beforeToEdit](crud, form) {
      if(form.createBy === 'admin' && !this.user.isAdmin){
        this.formDisable = true
      }else{
        this.formDisable = false
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
      } else if (!crud.form.className) {
        this.$message({
          message: '类名不能为空',
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
    checkboxT(row, rowIndex) {
      return 1 === 1
    },
    // 查看详情
    handleView(data) {
      this.crud.status.cu = 2
    },
    addKwParamConf() {
      this.form.kwParamConfs.push({ name: '', title: '', desc: '', inputType: '',  pDict: ''})
    },
    rmKwParamConf(index) {
      this.form.kwParamConfs.splice(index, 1)
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
