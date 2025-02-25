<template>
  <div class="app-container">
    <el-row :gutter="20">
      <!--侧边模块数据-->
      <el-col
        :xs="9"
        :sm="6"
        :md="5"
        :lg="4"
        :xl="4">
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
      <el-col
        :xs="15"
        :sm="18"
        :md="19"
        :lg="20"
        :xl="20">
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
            <date-range-picker
              v-model="query.createTime"
              class="date-item" />
            <rrOperation />
          </div>
          <crudOperation
            show=""
            :permission="permission">
            <el-button
              slot="right"
              v-permission="['admin','runTask:add']"
              :disabled="crud.selections.length === 0"
              class="filter-item"
              size="mini"
              type="success"
              plain
              @click="toRun('batch')"><svg-icon icon-class="debug" style="margin-right: 5px;" />调试
            </el-button>
          </crudOperation>
        </div>
        <!--表单渲染-->
        <el-dialog
          append-to-body
          :close-on-click-modal="false"
          :before-close="crud.cancelCU"
          :visible.sync="crud.status.cu>0"
          :title="crud.status.title"
          width="870px">
          <el-form
            ref="form"
            :inline="true"
            :model="form"
            :rules="rules"
            size="medium"
            label-width="100px">
            <el-form-item
              label="用例编号"
              prop="caseNo">
              <el-input
                v-model="form.caseNo"
                style="width: 260px"
                :disabled="caseNoDisable"
                @keydown.native="keydown($event)" />
            </el-form-item>
            <el-form-item
              label="用例标题"
              prop="caseTitle">
              <el-input
                v-model="form.caseTitle"
                style="width: 260px"
                @keydown.native="keydown($event)" />
            </el-form-item>
            <el-form-item
              label="模块"
              prop="category">
              <treeselect
                v-model="form.category.id"
                :options="Categorys"
                :load-options="loadCategorys"
                style="width: 178px"
                placeholder="选择模块" />
            </el-form-item>
            <el-form-item
              label="用例描述"
              prop="desc">
              <el-input
                v-model="form.desc"
                type="textarea"
                style="width: 500px;"
                :rows="3" />
            </el-form-item>
            <el-divider
              style="font-weight: 700;"
              size="medium">测试步骤</el-divider>
            <el-table
              :data="form.testSteps"
              style="width: 95%"
              border
              row-key="id"
              class="sort-table"
              :header-cell-style="{ background: '#f5f7fa', color: '#606266' }">
              <el-table-column
                label=""
                width="40px">
                <template>
                  <svg-icon
                    icon-class="list"
                    style="font-size: 14px"
                    @mousedown="rowDrop" />
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
                    <el-popover
                      trigger="click"
                      placement="right">
                      <p style="font-size: smaller;">关键字名称: {{ scope.row.kw.name }}</p>
                      <p style="font-size: smaller;">关键字说明: {{ scope.row.kw.desc }}</p>
                      <p
                        v-for="(kwParamConf, index) in scope.row.kw.kwParamConfs"
                        :key="index"
                        style="font-size: smaller;">
                        参数{{ index+1 }}: {{ kwParamConf.title }}, 参数说明: {{ kwParamConf.desc }}
                      </p>
                      <div
                        slot="reference"
                        class="name-wrapper">
                        <i
                          class="el-icon-info"
                          style="font-size: 15px; color:orange" />
                      </div>
                    </el-popover>
                  </div>
                </template>
              </el-table-column>
              <el-table-column
                prop="desc"
                label="步骤说明"
                width="250" />

              <el-table-column
                prop="enabled"
                label="启用"
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
              <el-table-column
                label="操作"
                header-align="center">
                <template slot-scope="scope">
                  <el-button-group>
                    <el-button
                      size="mini"
                      type="primary"
                      icon="el-icon-edit"
                      @click="handleEditStep(scope.$index, scope.row)" />
                    <el-button
                      size="mini"
                      type="warning"
                      icon="el-icon-copy-document"
                      plain
                      @click="handleCopyStep(scope.$index, scope.row)" />
                    <el-button
                      size="mini"
                      type="danger"
                      icon="el-icon-delete"
                      @click="handleDeleteStep(scope.$index, scope.row)" />
                  </el-button-group>
                </template>
              </el-table-column>
            </el-table>
            <el-button
              size="mini"
              type="primary"
              class="addStepButton"
              @click="handleAddStep">新增步骤</el-button>
            <el-button
              size="mini"
              type="success"
              plain
              class="addStepButton"
              @click="toRun('sigleCase')">
              <svg-icon icon-class="debug" style="margin-right: 5px;" />调试
            </el-button>
          </el-form>
          <div
            slot="footer"
            class="dialog-footer">
            <el-button
              type="text"
              @click="crud.cancelCU">取消</el-button>
            <el-button
              :loading="crud.status.cu === 2"
              type="primary"
              @click="crud.submitCU">确认</el-button>
          </div>
        </el-dialog>
        <!--新增编辑步骤-->
        <el-dialog
          v-if="addStepFormVisible"
          :title="stepTitle"
          :visible.sync="addStepFormVisible"
          width="60%"
          height="500px"
          :modal="false">
          <el-form
            :model="step"
            label-position="top">
            <el-form-item label="步骤说明:">
              <el-input
                v-model="step.desc"
                type="textarea"
                autocomplete="off"
                style="width: 300px;"
                :autosize="{ minRows: 1, maxRows: 3}" />
            </el-form-item>
            <el-form-item label="关键字:">
                <el-tag size="medium" v-if="step.kw.name !== ''" v-model="step.kw.name">{{ step.kw.name }}</el-tag>
              <a
                style="color: #458BE0;"
                @click.prevent="addStepKw">选择</a>
            </el-form-item>
            <el-form-item
              v-if="step.kw.name !== '' && step.kw.kwParamConfs.length > 0"
              label="参数设置">
              <el-tabs type="border-card" v-model="firstParam">
                <el-tab-pane
                  v-for="(sparam, index) in step.stepParam"
                  :key="index"
                  :label="sparam.title"
                  :name="sparam.title">
                  <el-input
                    v-if="sparam.inputType==='textarea'"
                    v-model="sparam.value"
                    type="textarea"
                    style="width: 800px;"
                    :autosize="{ minRows: 10, maxRows: 30}" />
                  <el-input
                    v-if="sparam.inputType==='input'"
                    v-model="sparam.value"
                    style="width: 700px;" />
                  <el-input
                    v-if="sparam.inputType==='numberInput'"
                    type="number"
                    v-model="sparam.value"
                    style="width: 100px;" />
                  <div v-if="sparam.inputType ==='webElement'">
                    <div v-if="sparam.value !== null && sparam.value !== undefined && sparam.value !== ''">
                      <span>模块: </span>
                      <el-tag effect="dark" type='warning'>{{ stepElement[sparam.key].category.name }}</el-tag>
                      <span>元素: </span>
                      <el-tag effect="dark" type='success'>{{ stepElement[sparam.key].name }}</el-tag>
                      <el-button type="primary" size="mini" icon="el-icon-edit" @click="openWebElementDialog(sparam.key, index)" />  
                    </div>
                    <el-button v-if="sparam.value === null || sparam.value === '' || sparam.value === undefined" type="primary" size="mini" icon="el-icon-plus" @click="openWebElementDialog(sparam.key, index)" />               
                  </div>
                  <el-select
                    v-if="sparam.inputType ==='select'"
                    v-model="sparam.value" style="width: 300px;">
                    <el-option
                      v-for="item in caseDicts[sparam.pDict]"
                      :key="item.id"
                      :label="item.label"
                      :value="item.value"
                    >{{ item.label }}</el-option>
                  </el-select>
                </el-tab-pane>
              </el-tabs>
            </el-form-item>
            <div v-if="step.kw.name !== null && step.kw.name !== '' && step.kw.ifResp" style="display: flex">
              <h4>提取响应值:</h4>
              <el-button size="mini" type="primary" style="height: 30px;margin-top: 12px;margin-left: 10px;" @click.prevent="addStepResp">添加+</el-button>
            </div>
            <el-form-item
              v-for="(stepResp, index) in step.stepResps"
              :key="index + 'only'"
              prop="stepResps">
              <el-select
                v-model="stepResp.respType"
                placeholder="选择响应类型"
                @change="() => changeRespType(stepResp.respType, index)"
                style="width: 100px;">
                <el-option
                  v-for="item in dict.resp_type"
                  :key="item.id"
                  :label="item.label"
                  :value="item.value">
                  {{ item.label }}
                </el-option>
              </el-select>
              <el-select
                v-if="stepResp.respType==='api'"
                v-model="stepResp.path"
                placeholder="选择获取途径"
                style="width: 100px;">
                <el-option
                  v-for="item in dict.resp_path"
                  :key="item.id"
                  :label="item.label"
                  :value="item.value">
                  {{ item.label }}
                </el-option>
              </el-select>
              <el-select v-model="stepResp.findType" placeholder="选择提取方式" style="width: 150px;">
                <el-option
                  v-for="item in findRespType"
                  :key="item.id"
                  :label="item.label"
                  :value="item.value">{{ item.label }}</el-option>
              </el-select>
              <el-input v-model="stepResp.expression" type="input" placeholder="输入表达式" style="width: 200px;" />
              <el-input v-model="stepResp.varName" type="input" placeholder="输入变量名" style="width: 200px;" />
              <el-button type="danger" @click.prevent="rmStepResp(index)">删除</el-button>
            </el-form-item>
          </el-form>
          <div
            slot="footer"
            class="dialog-footer"
            style="margin-top: 10px;">
            <el-button @click="addStepFormVisible = false">取 消</el-button>
            <el-button
              type="primary"
              @click="addOrEditStep">确 定</el-button>
          </div>
        </el-dialog>
        <el-dialog
          v-if="addStepKwVisible"
          title="选择关键字"
          :visible.sync="addStepKwVisible"
          width="60%"
          height="500px"
          :modal="false">
          <kwTableComponent @handleSelectKw="selectKw" />
        </el-dialog>
        <el-dialog
          v-if="addWebElementVisible"
          title="选择元素"
          :visible.sync="addWebElementVisible"
          width="60%"
          height="500px"
          :modal="false">
          <WebElementComponent @handleSelectWebelement="selWebElement" :elementType="elementType" />
        </el-dialog>
        <!--表格渲染-->
        <el-table
          ref="table"
          v-loading="crud.loading"
          :data="crud.data"
          style="width: 100%;"
          @selection-change="crud.selectionChangeHandler">
          <el-table-column
            :selectable="checkboxT"
            type="selection"
            width="55" />
          <el-table-column
            :show-overflow-tooltip="true"
            prop="caseNo"
            label="用例编号"
            sortable />
          <el-table-column
            :show-overflow-tooltip="true"
            prop="caseTitle"
            label="用例标题">
            <template slot-scope="scope">
              <a
                style="color:#458BE0;"
                @click="crud.toEdit(scope.row)">{{ scope.row.caseTitle }}</a>
            </template>
          </el-table-column>
          <el-table-column
            :show-overflow-tooltip="true"
            prop="category.name"
            label="模块" />
          <el-table-column
            :show-overflow-tooltip="true"
            prop="desc"
            label="用例说明" />
          <el-table-column
            :show-overflow-tooltip="true"
            prop="createBy"
            label="创建人" />
          <el-table-column
            :show-overflow-tooltip="true"
            prop="createTime"
            label="创建日期"
            sortable />
          <el-table-column
            :show-overflow-tooltip="true"
            prop="updateBy"
            label="更新人" />
          <el-table-column
            :show-overflow-tooltip="true"
            prop="updateTime"
            label="更新日期"
            sortable />
          <el-table-column
            v-if="checkPer(['admin','kw:edit','kw:del'])"
            label="操作"
            width="160px"
            align="center"
            fixed="right">
            <template slot-scope="scope">
              <div style="display: inline-flex">
                <el-button
                  size="mini"
                  type="warning"
                  icon="el-icon-copy-document"
                  @click="handleCopyCase(scope.$index, scope.row)" />
                <udOperation
                  style="margin-left: 2px"
                  :data="scope.row"
                  :permission="permission"
                  msg="确定删除吗，此操作不能撤销！" />
              </div>
            </template>
          </el-table-column>
        </el-table>
        <!--分页组件-->
        <pagination />
      </el-col>
    </el-row>
    <el-dialog
      title="用例调试"
      :visible.sync="runFormVisible"
      :modal="false"
      width="30%"
      size="small">
      <el-form
        :model="runConf"
        label-position="right"
        label-width="60px">
        <el-form-item
          label="环境"
          prop="runEnv">
          <el-select
            v-model="runConf.env.id"
            placeholder="请选择环境"
            style="width: 300px">
            <el-option
              v-for="(item, index) in envs"
              :key="index"
              :value="item.id"
              :label="item.name" />
          </el-select>
        </el-form-item>
        <el-form-item
          label="执行机"
          prop="runMachine">
          <el-select
            v-model="runConf.machine.id"
            placeholder="请选择执行机"
            style="width: 300px;">
            <el-option
              v-for="(item, index) in machines"
              :key="index"
              :value="item.id"
              :label="item.name"
              :disabled="item.status===0">
              <span style="float: left">{{ item.name }} (IP:{{ item.ipAddr }})</span>
              <span style="float: right; color: #8492a6; margin-left: 20px">
                <span v-if="item.status===0">离线</span>
                <span v-if="item.status===1">在线</span>
                <span v-if="item.status===2">忙碌</span>
                <el-tag
                  v-if="item.status===0"
                  type="info"
                  effect="dark"
                  class="tag-round" />
                <el-tag
                  v-if="item.status===1"
                  type="success"
                  effect="dark"
                  class="tag-round" />
                <el-tag
                  v-if="item.status===2"
                  type="danger"
                  effect="dark"
                  class="tag-round" />
              </span>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div
        slot="footer"
        class="dialog-footer">
        <el-button @click="runFormVisible = false">取 消</el-button>
        <el-button
          type="primary"
          :loading="submitLoading"
          @click="runTask">确 定
        </el-button>
      </div>
    </el-dialog>
    <el-dialog
      v-if="reportDetailVisible"
      title="调试结果"
      :visible.sync="reportDetailVisible"
      width="70%"
      height="700px"
      :modal="false">
      <DebugReportComponent :reportid="currentReportID" />
    </el-dialog>
  </div>
</template>

<script>
import crudCase from '@/api/testPlatform/testCase'
import {
  getCategorys,
  getCategorySuperior
} from '@/api/testPlatform/caseCategory'
import { getElement } from '@/api/testPlatform/webElement'
import CRUD, { presenter, header, form, crud } from '@crud/crudnew'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import udOperation from '@crud/UD.operation'
import pagination from '@crud/Pagination'
import DateRangePicker from '@/components/DateRangePicker'
import Treeselect from '@riophae/vue-treeselect'
import { mapGetters } from 'vuex'
import Sortable from 'sortablejs'
import '@riophae/vue-treeselect/dist/vue-treeselect.css'
import { LOAD_CHILDREN_OPTIONS } from '@riophae/vue-treeselect'
import { createRunTask } from '@/api/testPlatform/runTask'
import { getTestEnv } from '@/api/testPlatform/testEnv'
import { getMachines } from '@/api/testPlatform/executeMachine'
import { getDicts } from '@/api/system/dict'
const defaultForm = {
  id: null,
  caseNo: null,
  caseTitle: null,
  desc: null,
  category: { id: null },
  testSteps: []
}
export default {
  name: 'CaseManager',
  components: {
    Treeselect,
    crudOperation,
    rrOperation,
    udOperation,
    pagination,
    DateRangePicker,
    kwTableComponent: () => import('./selectKw.vue'),
    DebugReportComponent: () => import('./debugReport.vue'),
    WebElementComponent: () => import('./webElement.vue')
  },
  cruds() {
    return CRUD({
      title: '测试用例',
      url: 'api/caseCenter',
      crudMethod: { ...crudCase },
      optShow: {
        add: true,
        edit: true,
        del: true,
        download: false,
        reset: true
      }
    })
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  dicts: ['enable_status', 'find_resp_type', 'resp_path', 'resp_type'],
  data() {
    return {
      height: document.documentElement.clientHeight - 180 + 'px;',
      caseDicts: {},
      categoryName: '',
      Categorys: [],
      categoryDatas: [],
      kwCategorys: [],
      kwCategoryDatas: [],
      caseNoDisable: false,
      runAs: '',
      machines: [],
      envs: [],
      runConf: { env: { id: null, name: '' }, machine: { id: null, name: '' }},
      runFormVisible: false,
      submitLoading: false,
      currentReportID: null,
      reportDetailVisible: false,
      elementType: 'Selenium',
      findRespType: [{label: 'jsonpath', value: 'jsonpath', label: '正则', value: 'regex'}],
      step: {
        kw: { name: '' },
        desc: '',
        stepParam: null,
        enabled: true,
        stepSort: null,
        stepResps: []
      },
      addStepFormVisible: false,
      addStepKwVisible: false,
      formLabelWidth: '100px',
      stepTitle: '新增测试步骤',
      edit_index: null,
      step_param_index: null,
      addWebElementVisible: false,
      stepElement: {},
      firstParam: '',
      defaultProps: { children: 'children', label: 'name', isLeaf: 'leaf' },
      is_child_category: false,
      permission: {
        add: ['admin', 'testCase:add'],
        edit: ['admin', 'testCase:edit'],
        copy: ['admin', 'testCase:add'],
        del: ['admin', 'testCase:del'],
        debug: ['admin', 'runTask:add']
      },
      rules: {
        caseNo: [
          { required: true, message: '请输入用例编号', trigger: 'blur' },
          { validator: this.validateCaseNoInput, trigger: 'blur' },
          {
            min: 6,
            max: 32,
            message: '长度在 6 到 32 个字符',
            trigger: 'blur'
          }
        ],
        caseTitle: [
          { required: true, message: '请输入用例标题', trigger: 'blur' },
          {
            min: 2,
            max: 32,
            message: '长度在 2 到 32 个字符',
            trigger: 'blur'
          }
        ],
        categoryId: [
          { required: true, message: '请选择模块', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapGetters(['testCase'])
  },
  created() {
    this.crud.msg.add = '新增成功'
    this.getSysDict()
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
    validateCaseNoInput(rule, value, callback) {
        const regex = /^[a-zA-Z].*\d$/;
        if (!value || !regex.test(value)) {
          return callback(new Error('请输入以字母开头并以数字结尾的字符串'))
        }
        return callback();
    },
    getSysDict(){
      getDicts().then(data => {
        data.forEach((item) => {
          let dictionary = []
          item.dictDetails.forEach((dictDetail) => {
            dictionary.push(
              {label: dictDetail.label, value: dictDetail.value}
            )
          })
          this.caseDicts[item.name] = dictionary  
        })
      })
    },
    // 新增前做的操作
    [CRUD.HOOK.afterToAdd](crud, form) {
      this.form.category.id = this.query.categoryId
      if(this.is_child_category){
        this.getSupCategorys(form.category.id)
      }
    },
    // 新增前做的操作
    [CRUD.HOOK.beforeToAdd](crud, form) {
      this.form.projectID = sessionStorage.getItem('userProjectID')
      form.testSteps = []
      this.caseNoDisable = false
      if (form.id == null) {
        this.getCategorys()
      } else {
        this.getSupCategorys(form.category.id)
      }
    },
    // 编辑前做的操作
    [CRUD.HOOK.beforeToEdit](crud, form) {
      console.log('this.query.categoryId:', this.query.categoryId)
      this.form.projectID = sessionStorage.getItem('userProjectID')
      this.caseNoDisable = true
      if (form.id == null) {
        this.getCategorys()
      } else {
        this.getSupCategorys(form.category.id)
      }
    },
    // 复制前做的操作
    [CRUD.HOOK.beforeToCopy](crud, form) {
      this.form.projectID = sessionStorage.getItem('userProjectID')
      this.caseNoDisable = false
      if (form.id == null) {
        this.getCategorys()
      } else {
        this.getSupCategorys(form.category.id)
      }
    },
    // 取消复制后的的操作
    [CRUD.HOOK.afterCopyCancel](crud, form) {
      location.reload()
    },
    // 提交前做的操作
    [CRUD.HOOK.afterValidateCU](crud) {
      if (!crud.form.category.id) {
        this.$message({
          message: '模块不能为空',
          type: 'warning'
        })
        return false
      } else if (!crud.form.caseTitle) {
        this.$message({
          message: '用例名称不能为空',
          type: 'warning'
        })
        return false
      } else if (!crud.form.caseNo) {
        this.$message({
          message: '用例编号不能为空',
          type: 'warning'
        })
        return false
      } else if (crud.form.testSteps.length < 1) {
        this.$message({
          message: '至少添加一个测试步骤',
          type: 'warning'
        })
        return false
      } else if (crud.form.testSteps.length > 0) {
        let count = 0
        const s_steps = crud.form.testSteps
        for (var i = 0; i < s_steps.length; i++) {
          if (s_steps[i].enabled) {
            count++
            break
          }
        }
        if (count < 1) {
          this.$message({
            message: '至少添加一个非禁止的测试步骤',
            type: 'warning'
          })
          return false
        }
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
        getCategorys(params).then((res) => {
          if (resolve) {
            // console.log('resolve: ', resolve)
            resolve(res.content)
          } else {
            this.categoryDatas = res.content
          }
        })
      }, 100)
    },
    getCategorys() {
      getCategorys({ projectID: sessionStorage.getItem('userProjectID')}).then((res) => {
        this.Categorys = res.content.map(function(obj) {
          if (obj.hasChildren) {
            obj.children = null
          }
          return obj
        })
      })
    },
    getSupCategorys(categoryId) {
      getCategorySuperior(categoryId).then((res) => {
        const date = res.content
        this.buildCategorys(date)
        this.Categorys = date
      })
    },
    buildCategorys(Categorys) {
      Categorys.forEach((data) => {
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
        getCategorys({ pid: parentNode.id }).then((res) => {
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
        this.is_child_category = false
      } else {
        this.query.categoryId = data.id
        this.is_child_category = true
      }
      this.crud.toQuery()
    },
    checkboxT(row, rowIndex) {
      return row.id !== 1
    },
    changeRespType(selectValue, index){
      if(selectValue === 'api'){
        this.findRespType = [{label: 'jsonpath', value: 'jsonpath', label: '正则', value: 'regex'}]
        this.step.stepResps[index].findType = 'jsonpath'
      }else if(selectValue === 'dict'){
        this.findRespType = [{label: 'jsonpath', value: 'jsonpath'}]
        this.step.stepResps[index].findType = 'jsonpath'
      }else{
        this.findRespType = [{label: '正则', value: 'regex'}]
        this.step.stepResps[index].findType = 'regex'
      }
    },
    // 新增步骤
    handleAddStep() {
      this.addStepFormVisible = true
      this.stepTitle = '新增测试步骤'
      this.step = { id: null, kw: { name: '' }, stepParam: null, stepResps: [] }
      this.stepElement = {}
    },
    // 编辑步骤
    async handleEditStep(index, data) {
      this.edit_index = index
      const plist = []
      this.stepTitle = '编辑测试步骤'
      this.step['id'] = data.id
      this.step['kw'] = data.kw
      this.step['desc'] = data.desc
      this.step['stepSort'] = data.stepSort
      this.step['enabled'] = data.enabled
      let step_param = null
      if (typeof data.stepParam === 'object') {
        step_param = data.stepParam
      } else {
        step_param = JSON.parse(data.stepParam)
      }
      for(let key in step_param){
        if(key.includes('element')){
          await getElement(step_param[key]).then((res) => {
            this.stepElement[key] = res
          })
          // this.getStepElement(key, step_param[key])
        }
      }
      if (data.stepResps !== null && data.stepResps !== '' && data.stepResps !== undefined) {
        if (typeof data.stepResps === 'object') {
          this.step['stepResps'] = data.stepResps
        } else {
          this.step['stepResps'] = JSON.parse(data.stepResps)
        }
      } else {
        this.step['stepResps'] = []
      }
      const paramConfList = data.kw.kwParamConfs
      paramConfList.forEach(function(item) {
        const p_value = step_param[item.name]
        const tempParam = { value:  p_value}
        tempParam['title'] = item.title
        tempParam['key'] = item.name
        tempParam['desc'] = item.desc
        tempParam['inputType'] = item.inputType
        if(item.pDict !== '' && item.pDict !== undefined){
          tempParam['pDict'] = item.pDict
        }
        plist.push(tempParam)
      })
      this.step['stepParam'] = plist
      if(plist.length > 0){
        this.firstParam = plist[0]['title']
      } 
      setTimeout(() => {
        this.addStepFormVisible = true
      }, 300)
    },
    addStepKw() {
      this.addStepKwVisible = true
    },
    handleDeleteStep(index, data) {
      this.form.testSteps.splice(index, 1)
      this.sortCaseStep()
    },
    changeEnabled(index, enabled) {
      this.form.testSteps[index]['enabled'] = enabled
    },
    selWebElement(data){
      if (data.action === 'close') {
        this.addWebElementVisible = false
      }
      else{
        this.stepElement[this.step_param_title] = data.element
        this.step.stepParam[this.step_param_index].value = data.element.id
        this.addWebElementVisible = false
        // console.log('stepElement', this.stepElement)
      }
    },
    selectKw(data) {
      // console.log('action', data)
      const plist = []
      if (data.action === 'close') {
        this.addStepKwVisible = false
      } else {
        this.step['kw'] = data.kw
        // console.log('step.kw', this.step)
        const paramConfList = data.kw.kwParamConfs
        paramConfList.forEach(function(item) {
          const tempParam = { value: '' }
          tempParam['title'] = item.title
          tempParam['key'] = item.name
          tempParam['inputType'] = item.inputType
          tempParam['pDict'] = item.pDict
          tempParam['desc'] = item.desc
          plist.push(tempParam)
        })
        this.step.stepParam = plist
        if(plist.length > 0){
          this.firstParam = plist[0]['title']
        }  
        this.addStepKwVisible = false
      }
    },
    addOrEditStep() {
      if(this.step.kw.name === ''){
        this.$message({
          message: '请选择关键字',
          type: 'warning'
        })
        return false
      }
      const count = this.form.testSteps.length + 1
      const plist = this.step.stepParam
      const t_param = {}
      plist.forEach(function(item) {
        t_param[item.key] = item.value
      })
      if (this.stepTitle === '新增测试步骤') {
        const t_step = {
          id: null,
          enabled: true,
          stepSort: count,
          kw: this.step.kw,
          desc: this.step.desc,
          stepParam: t_param,
          stepResps: this.step.stepResps
        }
        this.form.testSteps.push(t_step)
      } else {
        const t_step = {
          id: this.step.id,
          desc: this.step.desc,
          enabled: this.step.enabled,
          stepSort: this.step.stepSort,
          kw: this.step.kw,
          stepParam: t_param,
          stepResps: this.step.stepResps
        }
        this.$set(this.form.testSteps, this.edit_index, t_step)
      }
      this.addStepFormVisible = false
    },
    addStepResp() {
      // console.log('this.step', this.step)
      this.step.stepResps.push({ respType: '', path: '', findType: '', expression: '', varName: '' })
    },
    rmStepResp(index) {
      this.step.stepResps.splice(index, 1)
    },
    handleCopyCase(index, data) {
      data.caseNo = 'copy' + data.caseNo
      if(data.caseNo.length > 32){
        data.caseNo = data.caseNo.substr(0, 32)
      }
      let _steps = []
      data.testSteps.forEach((item) => {
        item.id = null
        _steps.push(item)
      })
      data.testSteps = _steps
      this.crud.toCopy(data)
    },
    handleCopyStep(index, data) {
      const last_sort = this.form.testSteps.length + 1
      this.form.testSteps.push({
        id: null,
        kw: data.kw,
        stepParam: data.stepParam,
        stepResps: data.stepResps,
        desc: data.desc,
        enabled: data.enabled,
        stepSort: last_sort
      })
    },
    getNowString() {
      const now = new Date()
      const year = now.getFullYear()
      const month = (now.getMonth() + 1).toString().padStart(2, '0')
      const day = now.getDate().toString().padStart(2, '0')
      const hours = now.getHours().toString().padStart(2, '0')
      const minutes = now.getMinutes().toString().padStart(2, '0')
      const seconds = now.getSeconds().toString().padStart(2, '0')
      return `${year}${month}${day}${hours}${minutes}${seconds}`
    },
    // 查询执行机
    getMachines() {
      this.machines = []
      const sort = 'id,desc'
      const params = { sort: sort, projectID: sessionStorage.getItem('userProjectID') }
      let machine = {}
      setTimeout(() => {
        getMachines(params).then((res) => {
          for (let i = 0; i < res.content.length; i++) {
            machine = res.content[i]
            this.machines.push(machine)
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
    toRun(type) {
      // console.log('data: ', data)
      if (type === 'batch') {
        this.runAs = 'batchCase'
      } else {
        this.runAs = 'sigleCase'
      }
      this.runConf.env.id = null
      this.runConf.machine.id = null
      this.getMachines()
      this.getEnvs()
      this.runFormVisible = true
    },
    runTask() {
      // console.log('selects', this.crud.selections)
      if (!this.runConf.env.id) {
        this.$message.error('请选择环境')
        return false
      } else if (!this.runConf.machine.id) {
        this.$message.error('请选择执行机')
        return false
      }
      this.submitLoading = true
      const now = new Date().getTime()
      let data = null
      if (this.runAs === 'batchCase') {
        const selected = this.crud.selections
        if (selected.length < 1) {
          this.$message.error('请选择用例')
          return false
        }
        let caseIds = ''
        let reportName = ''
        for (let i = 0; i < selected.length; i++) {
          caseIds = caseIds + selected[i].id.toString() + ','
          reportName = reportName + selected[i].caseTitle + '、'
        }
        caseIds = caseIds.slice(0, -1)
        if(reportName.length > 64){
          reportName = reportName.substring(0, 64) + '...'
        }else{
          reportName = reportName + '...'
        }
        data = {
          name: '【用例】' + reportName,
          runType: 'batchCase',
          runEnv: this.runConf.env,
          runMachine: this.runConf.machine,
          caseIds: caseIds,
          status: 'pending',
          projectID: sessionStorage.getItem('userProjectID'),
          startTime: now
        }
      } else {
        if (!this.form.id) {
          this.$message.error('请进入用例详情进行调试')
          return false
        }
        data = {
          projectID: sessionStorage.getItem('userProjectID'),
          name: '【用例】' + this.form.caseTitle,
          runType: 'sigleCase',
          runEnv: this.runConf.env,
          runMachine: this.runConf.machine,
          runCase: this.form,
          status: 'pending',
          startTime: now
        }
      }
      setTimeout(() => {
        createRunTask(data).then((res) => {
          if (typeof res === 'number') {
            this.$message({
              message: '执行调试成功',
              type: 'success'
            })
            this.currentReportID = res
            this.submitLoading = false
            this.runFormVisible = false
            this.reportDetailVisible = true
          } else {
            this.$message({
              message: '执行调试失败',
              type: 'danger'
            })
          }
        })
      }, 200)
    },
    openWebElementDialog(title, index){
      if(this.step.kw.className === 'Browser'){
        this.elementType = 'Selenium'
      }else if(this.step.kw.className === 'Playwright'){
        this.elementType = 'Playwright'
      }else if(this.step.kw.className === 'AndroidApp'){
        this.elementType = 'Android'
      }else if(this.step.kw.className === 'IOS'){
        this.elementType = 'ios'
      }
      this.step_param_title = title
      this.step_param_index = index
      this.addWebElementVisible = true
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
          const currRow = _this.form.testSteps.splice(oldIndex, 1)[0]
          _this.form.testSteps.splice(newIndex, 0, currRow)
          _this.sortCaseStep()
        }
      })
    },
    // 重新排序
    sortCaseStep() {
      for (let i = 0; i < this.form.testSteps.length; i++) {
        this.form.testSteps[i].stepSort = i + 1
      }
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
  border-radius: 50%; /* 将border-radius设置为50%，使得Tag变成圆形 */
  padding: 0; /* 移除padding，使得Tag内容与边缘重合 */
  width: 12px; /* 设置宽度 */
  height: 12px; /* 设置高度，确保宽高相等以形成完美圆形 */
}
</style>
