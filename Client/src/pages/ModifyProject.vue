<template>
  <div class="container">
    <div class="inputBox">
      <input type="text" :placeholder="projectTitle" v-model="title">
      <textarea :placeholder="projectContent" v-model="content"></textarea>
    </div>
    <div class="dateBox">
      <label>시작 날짜<input type="date" v-model="startDate"></label>
      <label>종료 날짜<input type="date" v-model="endDate"></label>
    </div>
    <div class="buttonBox">
      <button class="modifyButton" @click="modifyProject">수정</button>
      <button class="deleteButton" @click="deleteProject">삭제</button>
    </div>
  </div>
</template>
<script setup>
import axios from 'axios';
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { expireToken } from "../api/config";
import Swal from 'sweetalert2';

const router = useRouter();
const projectId = sessionStorage.getItem("projectId");
const projectTitle = ref(null);
const projectContent = ref(null);

const title = ref(null);
const content = ref(null);
const startDate = ref(null);
const endDate = ref(null);

onMounted(() => {
  window.scrollTo(0, 0);
  const projectId = sessionStorage.getItem("projectId");
  if (projectId === null) {
    Swal.fire({
      icon: 'warning',
      title: '프로젝트 선택 안 됨',
      text: '프로젝트를 선택하여주세요.',
    });
    router.push("/project-list");
  }
});

function modifyProject() {
  Swal.fire({
    icon: 'question',
    title: '프로젝트 수정하기',
    text: `프로젝트를 수정하시겠습니까?`,
    showCancelButton: true,
    confirmButtonText: '수정',
    cancelButtonText: '취소'
  }).then((result) => {
    if(result.isConfirmed) {
      var requestProject = {
        title:title.value,
        content:content.value,
        startDate:startDate.value,
        endDate:endDate.value
      }
      axios.put(`http://localhost:8090/api/project/` + projectId, 
      requestProject,
      {
        headers: { 
            "Authorization" : sessionStorage.getItem("access-token") 
        },
      })
      .then((response) => {
        if(response.status == 205) {
          Swal.fire({
            icon: 'success',
            title: '수정이 완료되었습니다.',
            text: '프로젝트가 성공적으로 수정되었습니다.',
          });
          router.push("project-list");
        }
        else {
          console.log(response.status);
          Swal.fire({
            icon: 'warning',
            title: '오류 발생',
            text: '권한이 없거나 수정 중 오류가 발생했습니다.',
          });
        }
      })
      .catch((err) => {
        if(err.response.status == 403) {
          Swal.fire({
            icon: 'warning',
            title: '권한이 없습니다.',
            text: '프로젝트 대표 멤버만 프로젝트를 수정할 수 있습니다.',
          });
        }
        else {
          expireToken(err, modifyProject);
        }
      });
    } else {
      Swal.fire('취소되었습니다.', '', 'info');
    }
  });
}
function loadData() {
  axios.get(`http://localhost:8090/api/project/` + projectId, 
    {
      headers: { 
          "Authorization" : sessionStorage.getItem("access-token") 
      },
    })
    .then((response) => {
      projectTitle.value = response.data.title;
      projectContent.value = response.data.content;
      startDate.value = response.data.startDate;
      endDate.value = response.data.endDate;
    })
    .catch((err) => {
      console.log(err)
      expireToken(err, loadData);
    });
}
function deleteProject() {
  Swal.fire({
    icon: 'question',
    title: '프로젝트 삭제하기',
    text: `정말로 "${projectTitle.value}" 프로젝트를 삭제하시겠습니까?`,
    showCancelButton: true,
    confirmButtonText: '삭제',
    cancelButtonText: '취소'
  }).then((result) => {
    if(result.isConfirmed) {
      axios.delete(`http://localhost:8090/api/project/` + projectId,
      {
        headers: { 
              "Authorization" : sessionStorage.getItem("access-token") 
          },
      }).then((response) => {
        if(response.status == 205) {
          Swal.fire({
            icon: 'success',
            title: '삭제되었습니다.',
            text: '프로젝트가 성공적으로 삭제되었습니다.',
          });
          router.push("project-list");
        }
        else {
          Swal.fire({
            icon: 'warning',
            title: '오류 발생',
            text: '권한이 없거나 삭제 중 오류가 발생했습니다.',
          });
        }
      }).catch((err) => {
        if(err.response.status == 403) {
          Swal.fire({
            icon: 'warning',
            title: '권한이 없습니다.',
            text: '프로젝트 대표 멤버만 프로젝트를 삭제할 수 있습니다.',
          });
        }
        else if(err.response.status == 400) {
          Swal.fire({
            icon: 'info',
            title: '멤버 제거 필요',
            text: '프로젝트를 삭제하기 위해서는 다른 멤버들을 모두 프로젝트에서 제거해야합니다.',
          });
        }
        else {
          expireToken(err, deleteProject);
        }
      });
    } else {
      Swal.fire('취소되었습니다.', '', 'info');
    }
  })
}
loadData();
</script>
<style scoped>
@import '@/assets/css/modifyProject.css';
</style>