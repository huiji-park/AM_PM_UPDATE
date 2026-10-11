<template>
  <div v-if="modalData != null" class="modal">
    <span class="xButton" @click="close">X</span>
    <div class="imgDiv">
      <img v-if="modalData.profileImg" :src="modalData.profileImg">
    </div>
    <div class="textDiv">
      <span class="nicknameModal">{{ modalData.nickname }}</span>
      <span class="emailModal">{{ modalData.email }}</span>
    </div>
    <div class="routerDiv">
      <RouterLink class="router" :to="{name: 'Chat', query: {user: modalData.userId}}">정보 확인</RouterLink>
    </div>
  </div>
  <div class="chatContainer">
    <div class="msgContainer" id="msgScroll">
      <div id="msgArea" v-for="data in chatList">
        <div v-if="userId == data.userId" class="contentContainerMy">
          <div class="message">
            <div class="details">
              <span class="myname">나</span>
              <span class="date">{{ data.createdDate.substring(0, 10) }} {{data.createdDate.substring(11, 16)}}</span>
              <span v-if="data.unread > 0" class="unread" v-text="data.unread"></span>
            </div>
            <span v-text="data.message"></span>
          </div>
        </div>
        <div v-else="userId == data.userId" class="contentContainer">
          <div @click="show(data.userId)" class="imgContainer">
            <img v-if="data.user" :src="data.user.profileImg"/>
            <img v-else="data.user" :src="data.profileImg"/>
          </div>
          <div class="message">
            <div class="details">
              <span v-if="data.user" @click="show(data.userId)" class="nickname" v-text="data.user.nickname"></span>
              <span v-else="data.user" @click="show(data.userId)" class="nickname" v-text="data.nickName"></span>
              <span class="date">{{ data.createdDate.substring(0, 10) }} {{data.createdDate.substring(11, 16)}}</span>
              <span v-if="data.unread > 0" class="unread" v-text="data.unread"></span>
            </div>
            <span v-text="data.message"></span>
          </div>
        </div>
      </div>
    </div>
    <div class="inputContainer">
      <input class="inputBox" @keyup.enter="send" type="text" placeholder="메시지 작성" v-model="msg" maxlength="250">
      <button class="sendButton" @click="send">전송</button>
    </div>
  </div>
</template>

<script setup>
import { RouterLink } from 'vue-router';
import { ref, onMounted } from 'vue';
import { onBeforeRouteLeave  } from 'vue-router';
import Stomp from 'webstomp-client';
import axios from 'axios';
import { expireToken } from "../api/config";
import Swal from 'sweetalert2';

const msg = ref(null);
const projectId = sessionStorage.getItem("projectId");
const userId = ref(null);
const chatList = ref([]);
const modalData = ref(null);

onMounted(() => {
  window.scrollTo(0, 0);
  const projectId = sessionStorage.getItem("projectId");
  if (projectId === null) {
    Swal.fire({
      icon: 'warning',
      title: '프로젝트 선택 안 됨',
      text: '프로젝트를 선택하여주세요.',
    });
    router.push("project-list");
  }
});

function decodeToken(token) {
  const base64Url = token.split('.')[1];
  const base64 = base64Url.replace('-', '+').replace('_', '/');
  return JSON.parse(atob(base64));
}

function show(userId) {
  axios.get('http://localhost:8090/api/user/' + userId, {
    headers: { 
        "Authorization" : sessionStorage.getItem("access-token") 
    }
  })
  .then((response) => {
    if(response.status == 200) {
      modalData.value = response.data;
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
    expireToken(err, show(userId));
  });
}

function close() {
  modalData.value = null;
}

const decodedPayload = decodeToken(sessionStorage.getItem("access-token"));
userId.value = decodedPayload.id;

let websocket = new WebSocket('ws://localhost:8090/api/chat');
let stomp = Stomp.over(websocket);
stomp.connect({}, function() {
  stomp.subscribe("/sub/load/" + projectId, function(chat) {
    let data = JSON.parse(chat.body);
    chatList.value = data;
  });
  loadData();
  setTimeout(function () {
    let msgArea = document.getElementById("msgScroll");
    msgArea.scrollTop = msgArea.scrollHeight;
  }, 300);
  
  stomp.subscribe("/sub/chat/" + projectId, function(chat) {
    let data = JSON.parse(chat.body);
    chatList.value.push(data);
    setTimeout(function () {
      let msgArea = document.getElementById("msgScroll");
      msgArea.scrollTop = msgArea.scrollHeight;
    }, 100);
    loadData();
  });
});

onBeforeRouteLeave(() => {
  stomp.disconnect();
});

function loadData() {
  let data = {
    message : null, 
    projectId : projectId, 
    userId : userId.value
  };
  stomp.send("/pub/chat/enter", JSON.stringify(data), {});
}

function send() {
  let data = {
    message : msg.value, 
    projectId : projectId, 
    userId : userId.value
  };

  stomp.send("/pub/chat/message", JSON.stringify(data), {});
  msg.value = '';
}
</script>

<style scoped>
@import '@/assets/css/teamChat.css';
</style>