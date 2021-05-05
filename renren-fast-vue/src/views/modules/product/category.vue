<template>
  <div>
    <el-switch v-model="draggable" active-text="开启拖拽" inactive-text="关闭拖拽"></el-switch>
    <el-button size="mini" v-if="draggable" @click="batchSave">批量保存</el-button>
    <el-button size="mini" type="danger" @click="batchDelete">批量删除</el-button>
    <el-tree :data="menus" :props="defaultProps" :expand-on-click-node="false"
             show-checkbox node-key="catId" :default-expanded-keys="expandedKey" :draggable="draggable"
             :allow-drop="allowDrop" @node-drop="handleDrop" ref="menuTree">
    <span class="custom-tree-node" slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
        <span>
          <el-button v-if="node.level<=2" type="text" size="mini" @click="() => append(data)">Append</el-button>
          <el-button type="text" size="mini" @click="edit(data)">Edit</el-button>
          <el-button v-if="node.childNodes.length===0" type="text" size="mini"
                     @click="() => remove(node, data)">Delete</el-button>
        </span>
      </span>
    </el-tree>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="30%"
               :close-on-click-modal="false">
      <el-form :model="category">
        <el-form-item label="分类名称">
          <el-input v-model="category.name" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="category.icon" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="计量单位">
          <el-input v-model="category.productUnit" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitData">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
  export default {
    name: "category",
    data() {
      return {
        pCid: [],
        draggable: false,       // 是否开启拖拽功能
        updateNodes: [],        // 拖拽后需要保存的节点
        maxLevel: 0,
        dialogTitle: '',        // dialog的标题
        dialogType: '',         // dialog是新增还是更新
        category: {
          name: '',
          parentCid: 0,
          catLevel: 0,          // 层级
          showStatus: 1,
          sort: 0,
          productUnit: 0,       // 计量单位
          icon: '',
          catId: null
        },
        dialogVisible: false,   // dialog对话框默认关闭
        menus: [],
        defaultProps: {
          children: 'children',
          label: 'name'
        },
        expandedKey: []         // 删除节点后，记录该节点的父节点，再次请求时让该父节点保持展开
      };
    },
    methods: {
      // 请求分类
      getMenus() {
        this.$http({
          url: this.$http.adornUrl('/product/category/list/tree'),
          method: 'get'
        }).then(({data}) => {
          this.menus = data.data;
        });
      },
      // 如果dialogType是add则当前操作是新增，如果是edit则是更新
      submitData() {
        if (this.dialogType === 'add') {
          this.addCategory();
        } else if (this.dialogType === 'edit') {
          this.editCategory();
        }
      },
      // 修改分类
      editCategory() {
        let {catId, name, icon, productUnit} = this.category;
        this.$http({
          url: this.$http.adornUrl('/product/category/update'),
          method: 'post',
          data: this.$http.adornData({catId, name, icon, productUnit}, false)
        }).then(({data}) => {
          this.$message.success('修改分类成功');
          // 关闭对话框
          this.dialogVisible = false;
          // 刷新菜单
          this.getMenus();
          // 设置需要默认展开的菜单
          this.expandedKey = [this.category.parentCid];
        });
      },
      // 添加节点按钮
      append(data) {
        this.dialogType = 'add';
        this.dialogTitle = '添加分类';
        this.dialogVisible = true;
        this.category.parentCid = data.catId;
        this.category.catLevel = data.catLevel * 1 + 1;

        // 清空更新后的表单
        this.category.catId = null;
        this.category.name = '';
        this.category.icon = '';
        this.category.productUnit = '';
        this.category.sort = 0;
        this.category.showStatus = 1;
      },
      // 添加分类
      addCategory() {
        this.$http({
          url: this.$http.adornUrl('/product/category/save'),
          method: 'post',
          data: this.$http.adornData(this.category, false)
        }).then(({data}) => {
          this.$message.success('菜单保存成功');
          // 关闭对话框
          this.dialogVisible = false;
          // 刷新菜单
          this.getMenus();
          // 设置需要默认展开的菜单
          this.expandedKey = [this.category.parentCid];
        });
      },
      // 编辑按钮
      edit(data) {
        this.dialogType = 'edit';
        this.dialogTitle = '修改分类';
        this.dialogVisible = true;

        // 发送请求获取当前节点最新的数据
        this.$http({
          url: this.$http.adornUrl(`/product/category/info/${data.catId}`),
          method: 'get'
        }).then(({data}) => {
          // 请求成功
          this.category = data.data;
        });
      },
      // 删除节点按钮
      remove(node, data) {
        let ids = [data.catId];

        this.$confirm(`是否删除『${data.name}』菜单?`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$http({
            url: this.$http.adornUrl('/product/category/delete'),
            method: 'post',
            data: this.$http.adornData(ids, false)
          }).then(({data}) => {
            // 删除成功提示消息
            this.$message.success('菜单删除成功');
            // 刷新菜单
            this.getMenus();
            // 设置需要默认展开的菜单
            this.expandedKey = [node.parent.data.catId];
          });
        }).catch(() => {
          this.$message.info('操作已取消');
        });
      },
      // 节点是否允许拖拽
      allowDrop(draggingNode, dropNode, type) {
        this.calNodeLevel(draggingNode);
        // 当前节点深度等于子节点最大深度减去当前节点level再加1
        let deep = this.maxLevel - draggingNode.level + 1;
        // console.log(`deep: ${deep}; dropNode.level: ${dropNode.level}; dropNode.parent.level: ${dropNode.parent.level}`);
        if (type === 'inner') {
          // 如果是拖到某个节点内部，则当前深度加上要拖到的目标节点的深度小于等于3则拖动成功
          return deep + dropNode.level <= 3;
        } else {
          // 如果是拖到某个节点平级，则当前深度加上要拖到的目标节点的父节点的深度小于等于3则拖动成功
          return deep + dropNode.parent.level <= 3;
        }
      },
      // 计算所有子节点的最大深度
      calNodeLevel(node) {
        if (node.childNodes && node.childNodes.length > 0) {
          for (let i = 0; i < node.childNodes.length; i++) {
            if (node.childNodes[i].level > this.maxLevel) {
              this.maxLevel = node.childNodes[i].level;
            }
            this.calNodeLevel(node.childNodes[i]);
          }
        }
      },
      // 拖拽成功后触发的回调函数
      handleDrop(draggingNode, dropNode, dropType, ev) {
        console.log('tree drop: ', draggingNode, dropNode, dropType);
        // 当前节点的最新父节点id
        let pCid = 0;
        let siblings = [];
        if (dropType === 'inner') {
          // 如果拖拽到某个节点里面，此时的父id就是目标id，兄弟姐妹就是目标节点的childrenNodes
          pCid = dropNode.data.catId;
          siblings = dropNode.childNodes;
        } else {
          // 如果拖拽到某个节点前或者后，则此时的父id是目标id的父id，兄弟姐妹就是目标节点的父节点的childrenNodes
          pCid = dropNode.parent.data.catId === undefined ? 0 : dropNode.parent.data.catId;
          siblings = dropNode.parent.childNodes;
        }
        this.pCid.push(pCid);

        // 当前节点的最新顺序
        for (let i = 0; i < siblings.length; i++) {
          if (siblings[i].data.catId === draggingNode.data.catId) {
            // 如果是当前拖拽的节点
            let catLevel = draggingNode.level;
            if (draggingNode.level !== siblings[i].level) {
              // 如果当前节点层级发生变化
              catLevel = siblings[i].level;
              // 递归修改子节点层级
              this.updateChildrenLevel(siblings[i]);
            }
            // 则需要加上最新的父节点的id
            this.updateNodes.push({catId: siblings[i].data.catId, sort: i, parentCid: pCid, catLevel});
          } else {
            this.updateNodes.push({catId: siblings[i].data.catId, sort: i});
          }
        }
        console.log('要更新的节点数组: ', this.updateNodes);
      },
      // 递归修改子节点的catLevel
      updateChildrenLevel(node) {
        if (node.childNodes.length > 0) {
          for (let i = 0; i < node.childNodes.length; i++) {
            let cNode = node.childNodes[i].data;
            this.updateNodes.push({catId: cNode.catId, catLevel: node.childNodes[i].level, sort: i});
            this.updateChildrenLevel(node.childNodes[i]);
          }
        }
      },
      // 多次拖拽菜单以后批量保存
      batchSave() {
        this.$http({
          url: this.$http.adornUrl('/product/category/update/sort'),
          method: 'post',
          data: this.$http.adornData(this.updateNodes, false)
        }).then(({data}) => {
          this.$message.success('菜单顺序修改成功');
          // 刷新菜单
          this.getMenus();
          // 设置需要默认展开的菜单
          this.expandedKey = this.pCid;
          // 清空数据
          this.maxLevel = 0;
          this.updateNodes = [];
          //this.pCid = 0;
        });
      },
      // 批量删除的方法
      batchDelete(){
        let catIds = [];
        let checkedNodes = this.$refs.menuTree.getCheckedNodes();
        for (let i = 0; i < checkedNodes.length; i++) {
          catIds.push(checkedNodes[i].catId);
        }
        this.$confirm(`是否批量删除『${catIds}』菜单?`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$http({
              url: this.$http.adornUrl('/product/category/delete'),
              method: 'post',
              data: this.$http.adornData(catIds, false)
          }).then(({data}) => {
            this.$message.success('批量删除成功');
            // 刷新菜单
            this.getMenus();
          });
        }).catch(() => {
          this.$message.info('操作已取消');
        });
      }
    },
    created() {
      this.getMenus();
    }
  }
</script>

<style scoped>

</style>
