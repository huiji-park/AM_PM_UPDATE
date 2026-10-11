<template>
  <div>
    <project-plan :page-type="'USECASE'" :page-id="1"></project-plan>
  </div>
</template>

<script>
import ProjectPlan from '@/components/ProjectPlan.vue';

export default {
  components: {
    ProjectPlan
  },
};
</script>


<style scoped>

</style>