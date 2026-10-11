<template>
  <h1 class="">대표변경</h1>
  <div class="center">
  <div class="container">
    <div class="list">
      <ul>
        <form class ="member-list" v-for="item in memberList" :key="item.id">
          <div v-if="item.roles=='representative_member'" class="representative">
            <label :for="item.id"> {{ item.nickName }}</label>
          </div>
          <div v-else>
            <input class="checkbox" type="radio" v-model="userform" :value="item.userId">
            <label :for="item.id"> {{ item.nickName }}</label>
          </div>
        </form>
      </ul>
    </div>
  </div>
  <div class="clickbutton">
    <button @click="leaderChange" class="btn btn-primary">프로젝트 대표 변경</button>
  </div>
  </div>
</template>

<script setup>
import axios from 'axios';
import { ref } from 'vue';
import { expireToken } from "../api/config";
import { useRouter } from 'vue-router';
const router = useRouter();

const memberList = ref([]);
let userform = ref();

const projectId = sessionStorage.getItem("projectId");

const leaderChange = () => {
  console.log(userform.value)
  if(userform){
    axios.put("http://localhost:8090/api/representative_member/" + projectId, {"members":[userform.value]}, {
      headers: {
        "Authorization" : sessionStorage.getItem("access-token") 
      }
    }).then(response => {
      console.log(response.status)
      alert("프로젝트 대표를 변경했습니다.");
      router.push("/member-list")
    })
    .catch((err) => {
      console.log(err)
      expireToken(err, leaderChange, userform)
    });
  } else {
    alert("체크한 유저가 없습니다!");
  }
}

function loadData(){
  axios.get(`http://localhost:8090/api/members/` + projectId, {
    headers: { 
        "Authorization" : sessionStorage.getItem("access-token") 
    }
  })
  .then((response) => {
    memberList.value = response.data.sort((a, b) => {
        // roles 값을 기준으로 정렬 (알파벳순으로 정렬)
        if (a.roles < b.roles) {
          return 1;
        }
        if (a.roles > b.roles) {
          return -1;
        }
        return 0;
      });
  })
  .catch((err) => {
    console.log(err);
    expireToken(err, loadData)
  });
}
loadData();
</script>
<style scoped>
@import "@/assets/css/memberlist.css";
</style>
