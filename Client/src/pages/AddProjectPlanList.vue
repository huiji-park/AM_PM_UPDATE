<template>
  <h2 class="" style="text-align: center;">프로젝트 계획 목록</h2>
  <div class="center">
    <div class="container">
      <div class="list">
        <ul>
          <div class="project-plan-list" v-for="plan in projectPlanList" :key="plan.id">
            <label :for="plan.id">{{ plan.title }}</label>
          </div>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import axios from 'axios';
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { expireToken } from "../api/config";
const router = useRouter();

const projectPlanList = ref([]);
const projectId = sessionStorage.getItem("projectId");

const loadData = () => {
  axios.get(`http://localhost:8090/api/plan/etc-pages/${projectId}`, {
    headers: { 
        "Authorization" : sessionStorage.getItem("access-token") 
    }
  }).then(response => {
    projectPlanList.value = response.data;
  }).catch(err => {
    console.log(err);
    expireToken(err, loadData)
  });
};

loadData();
</script>

<style scoped>
@import "@/assets/css/projectplanlist.css"
</style>
