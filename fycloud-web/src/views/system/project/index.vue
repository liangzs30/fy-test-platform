<template>
  <div class="app-container">
    <!--工具栏-->
    <div class="head-container">
      <div v-if="crud.props.searchToggle">
        <!-- 搜索 -->
        <el-input v-model="query.blurry" size="small" clearable placeholder="输入名称或者描述搜索" style="width: 200px;" class="filter-item" @keyup.enter.native="crud.toQuery" />
        <date-range-picker v-model="query.createTime" class="date-item" />
        <rrOperation />
      </div>
      <crudOperation :permission="permission" />
    </div>
    <!-- 表单渲染 -->
    <el-dialog append-to-body :close-on-click-modal="false" :before-close="crud.cancelCU" :visible.sync="crud.status.cu > 0" :title="crud.status.title" width="520px">
      <el-form ref="form" :inline="true" :model="form" :rules="rules" size="small" label-width="80px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="form.name" style="width: 380px;" />
        </el-form-item>
        <el-form-item label="描述信息" prop="description">
          <el-input v-model="form.description" style="width: 380px;" rows="5" type="textarea" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="text" @click="crud.cancelCU">取消</el-button>
        <el-button :loading="crud.status.cu === 2" type="primary" @click="crud.submitCU">确认</el-button>
      </div>
    </el-dialog>
    <el-row :gutter="15">
      <!--项目管理-->
      <el-col :xs="24" :sm="24" :md="16" :lg="16" :xl="17" style="margin-bottom: 10px">
        <el-card class="box-card" shadow="never">
          <div slot="header" class="clearfix">
            <span class="role-span">项目列表</span>
          </div>
          <el-table ref="table" v-loading="crud.loading" highlight-current-row style="width: 100%;" :data="crud.data" @selection-change="crud.selectionChangeHandler" @current-change="handleCurrentChange">
            <el-table-column :selectable="checkboxT" type="selection" width="55" />
            <el-table-column prop="name" label="名称" />
            <el-table-column :show-overflow-tooltip="true" prop="description" label="描述" />
            <el-table-column :show-overflow-tooltip="true" width="135px" prop="createTime" label="创建日期" />
            <el-table-column v-if="checkPer(['admin','roles:edit','roles:del'])" label="操作" width="130px" align="center" fixed="right">
              <template slot-scope="scope">
                <udOperation
                  :data="scope.row"
                  :permission="permission" />
              </template>
            </el-table-column>
          </el-table>
          <!--分页组件-->
          <pagination />
        </el-card>
      </el-col>
      <!-- 用户授权 -->
      <el-col :xs="24" :sm="24" :md="8" :lg="8" :xl="7">
        <el-card v-if="cardShow" class="box-card" shadow="never">
          <div slot="header" class="clearfix">
            <el-tooltip class="item" effect="dark" content="选择用户" placement="top">
              <span class="role-span">项目用户</span>
            </el-tooltip>
            <el-button
              v-permission="['admin']"
              :loading="menuLoading"
              size="mini"
              style="float: right; padding: 6px 9px;margin-right: 10px;"
              type="primary"
              @click="addUsers">添加</el-button>
          </div>
          <el-table
            :data="projectUsers">
            <el-table-column
              label="序号"
              type="index"
              width="50" />
            <el-table-column
              prop="username"
              label="用户名"
              width="100" />
            <el-table-column
              prop="nickName"
              label="昵称"
              width="100" />
            <el-table-column
              prop="dept"
              label="部门"
              width="100">
              <template slot-scope="scope">
                <div>{{ scope.row.dept.name }}</div>
              </template>
            </el-table-column>
            <el-table-column
              fixed="right"
              label="操作"
              width="50">
              <template slot-scope="scope">
                <el-button
                  type="text"
                  size="small"
                  @click.native.prevent="deleteUser(scope.row)">
                  移除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            :current-page.sync="currentPage"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="pageSize"
            layout="total, prev, pager, next, sizes, jumper"
            :total="totalRecords"
            @size-change="handleSizeChange"
            @current-change="handleChangePage" />
        </el-card>
      </el-col>
    </el-row>
    <el-dialog
      v-if="userListVisible"
      title="添加用户"
      :visible.sync="userListVisible"
      width="70%"
      height="700px"
      :modal="false">
      <UserListComponent :user-i-ds="userIDs" @handleSelectUsers="saveUsers" />
    </el-dialog>
  </div>
</template>

<script>
import crudProjects from '@/api/system/project'
import CRUD, { presenter, header, form, crud } from '@crud/crud'
import rrOperation from '@crud/RR.operation'
import crudOperation from '@crud/CRUD.operation'
import udOperation from '@crud/UD.operation'
import pagination from '@crud/Pagination'
import DateRangePicker from '@/components/DateRangePicker'

const defaultForm = { id: null, name: null, description: null, users: [] }
export default {
  name: 'Project',
  components: {
    pagination,
    crudOperation,
    rrOperation,
    udOperation,
    DateRangePicker,
    UserListComponent: () => import('./userList.vue')
  },
  cruds() {
    return CRUD({ title: '项目',
      url: 'api/projects',
      sort: 'id,asc',
      crudMethod: { ...crudProjects },
      optShow: {
        add: true,
        edit: true,
        del: true,
        download: false,
        reset: true
      }})
  },
  mixins: [presenter(), header(), form(defaultForm), crud()],
  data() {
    return {
      defaultProps: { children: 'children', label: 'label', isLeaf: 'leaf' },
      currentId: 0, menuLoading: false, showButton: false,
      userListVisible: false,
      projectUsers: [],
      selectProject: { users: [] },
      cardShow: false,
      totalRecords: 0,
      currentPage: 1,
      pageSize: 10,
      projectAllUsers: [],
      userIDs: [],
      permission: {
        add: ['admin'],
        edit: ['admin'],
        del: ['admin']
      },
      rules: {
        name: [
          { required: true, message: '请输入名称', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    checkboxT(row) {
      return 1 === 1
    },
    // 删除后刷新页面
    [CRUD.HOOK.afterCrudDelete](crud, form) {
      window.location.reload(true)
    },
    // 触发单选
    handleCurrentChange(val) {
      if (val) {
        this.projectUsers = val.users
        this.projectAllUsers = val.users
        this.selectProject = val
        this.totalRecords = val.users.length
        this.cardShow = true
      }
    },
    addUsers() {
      for (let i = 0; i < this.projectAllUsers.length; i++) {
        this.userIDs.push(this.projectAllUsers[i]['id'])
      }
      this.userListVisible = true
    },
    updateProject(operType) {
      setTimeout(() => {
        crudProjects.edit(this.selectProject).then((res) => {
          if (!res) {
            this.$message({
              message: operType + '成功',
              type: 'success'
            })
          } else {
            this.$message({
              message: operType + '失败',
              type: 'danger'
            })
          }
        })
      }, 100)
    },
    saveUsers(data) {
      const ulist = this.projectUsers
      if (data.action === 'close') {
        this.userListVisible = false
      } else {
        const newUsers = data.users
        this.projectUsers = [...ulist, ...newUsers]
      }
      // console.log('projectUsers', this.projectUsers)
      this.userListVisible = false
      this.totalRecords = this.projectUsers.length
      this.currentPage = 1
      this.projectAllUsers = this.projectUsers
      this.selectProject.users = this.projectUsers
      this.updateProject('添加')
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
      if (this.pageSize <= this.projectAllUsers.length) {
        this.projectUsers = this.projectAllUsers.slice(0, val)
      } else {
        this.projectUsers = this.projectAllUsers
      }
    },
    handleChangePage(val) {
      this.currentPage = val
      const offSet1 = (val - 1) * this.pageSize
      const offSet2 = val * this.pageSize
      this.projectUsers = this.projectAllUsers.slice(offSet1, offSet2)
    },
    deleteUser(user) {
      this.$confirm('确定移除此用户?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        for (let i = 0; i < this.totalRecords; i++) {
          if (this.projectAllUsers[i]['id'] === user.id) {
            this.projectAllUsers.splice(i, 1)
            break
          }
        }
        this.totalRecords = this.totalRecords - 1
        this.currentPage = 1
        if (this.pageSize <= this.totalRecords) {
          this.projectUsers = this.projectAllUsers.slice(0, val)
        } else {
          this.projectUsers = this.projectAllUsers
        }
        this.selectProject.users = this.projectAllUsers
        this.updateProject('移除')
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消移除'
        })
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss">
  .role-span {
    font-weight: bold;color: #303133;
    font-size: 15px;
  }
</style>

<style rel="stylesheet/scss" lang="scss" scoped>
 ::v-deep .el-input-number .el-input__inner {
    text-align: left;
  }
 ::v-deep .vue-treeselect__multi-value{
    margin-bottom: 0;
  }
 ::v-deep .vue-treeselect__multi-value-item{
    border: 0;
    padding: 0;
  }
</style>
