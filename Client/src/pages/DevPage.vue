<template>
  <h1>🛠 Development</h1>
  <hr class="dev_title" />
  <Gantt />
  <Notice />
</template>

<script setup>
import Gantt from "@/components/Gantt.vue";
import Notice from "@/components/Notice.vue";
import { onMounted } from "vue";
import { useRouter } from "vue-router";
import Swal from "sweetalert2";

const router = useRouter();

onMounted(() => {
  window.scrollTo(0, 0);
  const projectId = sessionStorage.getItem("projectId");
  if (projectId === null) {
    Swal.fire({
      icon: "error",
      title: "프로젝트를 선택하세요.", // Alert 제목
    });
    router.push("/project-list");
  }
});
</script>

<style scoped>
.dev_title {
  margin-bottom: 40px;
}
</style>
