<template>
  <h2 class="" style="text-align: center;">멤버 추가</h2>
  <div class = "center">
  <div class="container">
    <div class="search">
      <form class="d-flex" @submit.prevent="submitForm">
        <input class="form-control me-2 bg-light" v-model="searchMember" placeholder="Search" >
        <button class="btn btn-outline-primary" type="submit">Search</button>
      </form>
    </div>
    <div class="list">
        <div class ="search-user" v-for="item in filteredData" :key="item.id">
            <input class="checkbox" type="checkbox" v-model="userform" :value="item.id">
            <label :for="item.id"> {{ item.email }}</label>
        </div>
    </div>
  </div>
  <div class="add">
    <button @click="memberList" class="btn btn-primary">멤버 추가하기</button>
  </div>
  </div>
</template>

<script setup>
import axios from 'axios';
import { ref } from 'vue';
import { refresh } from "@/api/refresh";
import { useRouter } from 'vue-router';
import { expireToken } from "../api/config";
const router = useRouter();

const projectId = sessionStorage.getItem("projectId");
const userId = ref(null);
const roles = ref(null);
const searchMember = ref('');
const filteredData = ref([]);
let userform = ref([]);
let memberIdList = ref([]);

const memberList = () => {
  let formdata = []
  for(let o of userform.value){
    formdata.push(o);
  }
  console.log("FormData " + formdata);
  axios.put("http://localhost:8090/api/members/" + projectId, {"members":formdata}, {
    headers: {
      "Authorization" : sessionStorage.getItem("access-token") 
    }
  }).then(response => {
    console.log(response.status)
    alert("맴버 추가 완료!")
    router.push("/member-list")

  })
  .catch((err) => {
    console.log(err)
    expireToken(err, memberList)
  });
}

const submitForm = () => {
  // console.log(searchMember.value);
  axios.get(`http://localhost:8090/api/user/nickname?nickname=${searchMember.value}`)
  .then(request => {
    filteredData.value = [];
    let checkid = [];
    for (let o of memberIdList.value){
      checkid.push(o.userId)
    }
    for(let o of request.data){
      // console.log(memberIdList.value)
      if(o.roles == "ROLE_USER" && !checkid.includes(o.id)){
        filteredData.value.push({"id":o.id, "nickname":o.nickname, "email":o.email})
      }
    }
  })
}

function loadData(){
  axios.get(`http://localhost:8090/api/members/` + projectId, {
    headers: { 
        "Authorization" : sessionStorage.getItem("access-token") 
    }
  })
  .then((response) => {
    memberIdList.value = response.data;
  })
  .catch((err) => {
    console.log(err)
    expireToken(err, loadData)
  });
}
loadData();

</script>

<style scoped>
@import "@/assets/css/memberadd.css";
</style>
