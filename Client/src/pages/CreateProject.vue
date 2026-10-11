<template>
  <div class="container">
    <div class="inputBox">
      <input type="text" placeholder="제목" v-model="title">
      <textarea placeholder="설명" v-model="content"></textarea>
    </div>
    <div class="dateBox">
      <label>시작 날짜<input type="date" v-model="startDate"></label>
      <label>종료 날짜<input type="date" v-model="endDate"></label>
    </div>
    <div class="buttonBox">
      <button class="createButton" @click="createProject">프로젝트 생성</button>
      <button class="backButton" @click="$router.go(-1)">뒤로 가기</button>
    </div>
  </div>
</template>
<script setup>
import axios from 'axios';
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { expireToken } from "../api/config";
import Swal from 'sweetalert2';

const router = useRouter();
const title = ref(null);
const content = ref(null);
const startDate = ref(null);
const endDate = ref(null);
function createProject() {
  if(title.value == null) {
    Swal.fire({
      icon: 'warning',
      title: '제목을 입력하세요.',
      text: '제목이 비어 있습니다.',
    });
  }
  else if(startDate.value == null || endDate.value == null) {
    Swal.fire({
      icon: 'warning',
      title: '날짜를 입력하세요.',
      text: '날짜가 지정되지 않았습니다.',
    });
  }
  else {
    var requestProject = {
      title:title.value,
      content:content.value,
      startDate:startDate.value,
      endDate:endDate.value
    }
    axios.post(`http://localhost:8090/api/project`, 
    requestProject,
    {
      headers: { 
          "Authorization" : sessionStorage.getItem("access-token") 
      },
    })
    .then((response) => {
      if(response.status == 201) {
        Swal.fire({
          icon: 'success',
          title: '생성이 완료되었습니다.',
          text: '프로젝트가 성공적으로 생성되었습니다.',
        });
      router.push({path: "/project-list"});
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
      expireToken(err, createProject);
    });
  }
}
</script>
<style scoped>
@import "@/assets/css/createProject.css";
</style>