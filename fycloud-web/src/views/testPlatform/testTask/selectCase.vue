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
            @input="getCategoryDatas" />
        </div>
        <el-tree
          :data="categoryDatas"
          :load="getCategoryDatas"
          :props="defaultProps"
          :expand-on-click-node="false"
          lazy
          @node-click="handleNodeClick" />
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
              placeholder="输入用例标题、编号搜索"
              style="width: 200px;"
              class="filter-item"
              @keyup.enter.native="crud.toQuery" />
            <date-range-picker v-model="query.createTime" class="date-item" />
            <rrOperation />
          </div>
          <crudOperation show="" :permission="permission" />
        </div>
        <!--表单渲染-->
        <el-dialog append-to-body :close-on-click-modal="false" :before-close="crud.cancelCU" :visible.sync="crud.status.cu > 0" title="用例详情" width="870px">
          <el-form ref="form" :inline="true" :model="form" :rules="rules" size="medium" label-width="100px" :disabled="true">
            <el-form-item label="用例编号" prop="caseNo">
              <el-input v-model="form.caseNo" style="width: 260px" @keydown.native="keydown($event)" />
            </el-form-item>
            <el-form-item label="用例标题" prop="caseTitle">
              <el-input v-model="form.caseTitle" style="width: 260px" @keydown.native="keydown($event)" />
            </el-form-item>
            <el-form-item label="模块" prop="category">
              <treeselect
                v-model="form.category.id"
                :options="Categorys"
                :load-options="loadCategorys"
                style="width: 178px"
                placeholder="选择模块" />
            </el-form-item>
            <el-form-item label="用例描述" prop="desc">
              <el-input v-model="form.desc" type="textarea" style="width: 500px;" :rows="3" />
            </el-form-item>
            <el-divider style="font-weight: 700;" size="medium">测试步骤</el-divider>
            <el-table
              :data="form.testSteps"
              style="width: 95%"
              border
              row-key="id"
              class="sort-table"
              :header-cell-style="{ background: '#f5f7fa', color: '#606266' }">
              <el-table-column label="" width="40px">
                <template>
                  <svg-icon icon-class="list" style="font-size: 14px" />
                </template>
              </el-table-column>
              <el-table-column
                prop="stepSort"
                label="序号"
                width="45"
                style="text-align: center; font-weight: 700;" />
              <el-table-column
                prop="kw"
                label="关键字"
                width="220">
                <template slot-scope="scope">
                  <div class="kw-popover">
                    <el-tag size="medium">{{ scope.row.kw.name }}</el-tag>
                    <el-popover trigger="click" placement="right">
                      <p style="font-size: smaller;">关键字名称: {{ scope.row.kw.name }}</p>
                      <p style="font-size: smaller;">关键字说明: {{ scope.row.kw.desc }}</p>
                      <p v-for="(kwParamConf, index) in scope.row.kw.kwParamConfs" style="font-size: smaller;">参数{{ index+1 }}: {{ kwParamConf.name }}, 数据类型:{{ kwParamConf.dataType }}, 参数说明: {{ kwParamConf.desc }}</p>
                      <div slot="reference" class="name-wrapper">
                        <i class="el-icon-info" style="font-size: 15px; color:orange" />
                      </div>
                    </el-popover>
                  </div>
                </template>
              </el-table-column>
              <!-- <el-table-column
                prop="stepParam"
                label="步骤参数"
                width="180"
              /> -->
              <el-table-column
                prop="desc"
                label="步骤说明"
                width="250" />

              <el-table-column
                prop="enabled"
                label="禁用"
                width="70">
                <template slot-scope="scope">
                  <el-switch
                    v-model="scope.row.enabled"
                    :enabled="scope.row.id === 1"
                    active-color="#409EFF"
                    inactive-color="#F56C6C"
                    @change="changeEnabled(scope.$index, scope.row.enabled)" />
                </template>
              </el-table-column>
              <el-table-column label="操作" header-align="center">
                <template slot-scope="scope">
                  <el-button
                    size="mini"
                    type="primary"
                    @click="handleEditStep(scope.$index, scope.row)">编辑</el-button>
                  <el-button
                    size="mini"
                    type="danger"
                    @click="handleDeleteStep(scope.$index, scope.row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-form>
          <div slot="footer" class="dialog-footer">
            <el-button type="text" @click="crud.cancelCU">取消</el-button>
          </div>
        </el-dialog>
        <!--表格渲染-->
        <el-table ref="table" v-loading="crud.loading" :data="crud.data" style="width: 100%;" @selection-change="crud.selectionChangeHandler">
          <el-table-column :selectable="checkboxT" type="selection" width="55" />
          <el-table-column :show-overflow-tooltip="true" prop="caseNo" label="用例编号" sortable />
          <el-table-column :show-overflow-tooltip="true" prop="caseTitle" label="用例标题">
            <template slot-scope="scope">
              <a style="color:#458BE0;" @click="crud.toEdit(scope.row)">{{ scope.row.caseTitle }}</a>
            </template>
          </el-table-column>
          <el-table-column :show-overflow-tooltip="true" prop="category" label="模块">
            <template slot-scope="scope">
              <div>{{ scope.row.category.name }}</div>
            </template>
          </el-table-column>
          <el-table-column :show-overflow-tooltip="true" prop="desc" label="用例说明" />
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
import crudCase from '@/api/testPlatform/testCase'
import { isvalidPhone } from '@/utils/validate'
// import { kwTableComponent } from './selectKw.vue'
import { getCategorys, getCategorySuperior } from '@/api/testPlatform/caseCategory'
import CRUD, { presenter, header, form, crud } from '@crud/crudnew'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import pagination from '@crud/Pagination'
import DateRangePicker from '@/components/DateRangePicker'
import Treeselect from '@riophae/vue-treeselect'
import { mapGetters } from 'vuex'
import Sortable from 'sortablejs'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import { LOAD_CHILDREN_OPTIONS } from '@riophae/vue-treeselect'
const defaultForm = { id: null, caseNo: null, caseTitle: null, desc: null, category: { id: null }, testSteps: [{ kw: { name: '' }, desc: '', stepParam: null, enabled: true, stepSort: null }] }
export default {
  name: 'CaseTableComponent',
  components: { Treeselect, crudOperation, rrOperation, pagination, DateRangePicker },
  cruds() {
    return CRUD({ title: '测试用例', url: 'api/caseCenter', crudMethod: { ...crudCase }, optShow: {
      add: false,
      edit: false,
      del: false,
      download: false,
      reset: true
    }})
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  dicts: ['enable_status'],
  data() {
    return {
      height: document.documentElement.clientHeight - 180 + 'px;',
      categoryName: '', Categorys: [], categoryDatas: [],
      formLabelWidth: '100px',
      stepTitle: '新增测试步骤',
      defaultProps: { children: 'children', label: 'name', isLeaf: 'leaf' },
      permission: {
        add: ['admin', 'testCase:add'],
        edit: ['admin', 'testCase:edit'],
        del: ['admin', 'testCase:del']
      },
      rules: {
      }
    }
  },
  computed: {
    ...mapGetters([
      'testCase'
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
      getCategorys({ projectID: sessionStorage.getItem('userProjectID') }).then(res => {
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
    // 获取弹窗内模块数据
    loadCategorys({ action, parentNode, callback }) {
      if (action === LOAD_CHILDREN_OPTIONS) {
        getCategorys({ enabled: true, pid: parentNode.id }).then(res => {
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
      return row.id !== 1
    },
    closeDialog(action) {
      if (action === 'close') {
        this.$emit('handleSelectCases', { action: 'close', cases: null })
      } else {
        if (this.crud.selections.length < 1) {
          this.$message({
            message: '您还没有选择用例',
            type: 'warning'
          })
        } else {
          const send_cases = []
          this.crud.selections.forEach(function(item) {
            send_cases.push({ caseId: item.id, caseNo: item.caseNo, caseTitle: item.caseTitle, desc: item.desc, category: item.category, executeSort: null })
          })
          this.$emit('handleSelectCases', { action: 'sendCases', cases: send_cases })
        }
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
  ::v-deep .el-divider__text{
    font-weight:700;
    color: #606266;
  }
  .addStepButton{
    margin-top: 10px;
  }
  .kw-popover{
    display: flex;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
</style>
