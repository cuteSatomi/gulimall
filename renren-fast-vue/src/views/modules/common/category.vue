<template>
  <el-tree
    :data="menus"
    :props="defaultProps"
    node-key="catId"
    ref="menuTree"
    @node-click="nodeClick">
  </el-tree>
</template>

<script>
  export default {
    name: "category",
    data() {
      return {
        menus: [],
        defaultProps: {
          children: 'children',
          label: 'name'
        },
        expandedKey: []         // 删除节点后，记录该节点的父节点，再次请求时让该父节点保持展开
      };
    },
    created() {
      this.getMenus();
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
      // 子组件分类节点点击事件
      nodeClick(data, node, component) {
        // 向父组件发送事件
        this.$emit('tree-node-click', data, node, component);
      }
    }
  }
</script>

<style scoped>

</style>
