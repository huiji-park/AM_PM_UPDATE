<template>
  <div class="container">
    <div v-for="data in projectList">
      <div class="project" @click="go(data.id)">
        <span class="title">{{ data.title }}</span>
        <span class="content">{{ data.content }}</span>
        <span v-if="data.startDate" class="startDate">시작 날짜 : {{ data.startDate }}</span>
        <span v-if="data.endDate" class="endDate">종료 날짜 : {{ data.endDate }}</span>
      </div>
    </div>
    <div class="buttonBox">
      <RouterLink class="button" to="/create-project">+새 프로젝트</RouterLink>
    </div>
  </div>
</template>
<script setup>
import axios from 'axios';
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { expireToken } from "../api/config";
import Swal from 'sweetalert2';

const projectList = ref([]);
const router = useRouter();
function go(projectId) {
  sessionStorage.setItem("projectId", projectId);
  router.push({ name: "ProjectPlanPage", params: { pageType: 'srs' } });
}

function checkAdmin() {
  axios.get(`http://localhost:8090/api/user`, {
    headers: { 
        "Authorization" : sessionStorage.getItem("access-token") 
    }
  })
  .then((response) => {
    if(response.data.role == "ROLE_ADMIN"){
      router.push("/question")
    }

  })
  .catch((err) => {
    console.log(err)
    expireToken(err, checkAdmin);
  });
}

function loadData() {
  axios.get(`http://localhost:8090/api/project`, {
    headers: { 
        "Authorization" : sessionStorage.getItem("access-token") 
    }
  })
  .then((response) => {
    if(response.status == 200) {
      projectList.value = response.data;
    }
    else {
      Swal.fire({
        icon: 'warning',
        title: '오류 발생',
        text: '다시 시도해주세요.',
      });
    }
  })
  .catch((err) => {
    console.log(err)
    expireToken(err, loadData);
  });
}
loadData();
checkAdmin();
</script>
<style scoped>
@import '@/assets/css/projectList.css';
</style>