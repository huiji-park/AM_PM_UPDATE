<template>
  <NotiModle
    v-if="showModal"
    :closeModal="closeModal"
    :getNotiList="getNotiList"
  />
  <NotiDetailModal
    v-if="showDetailModal"
    :closeDetailModal="closeDetailModal"
    :getNotiList="getNotiList"
    :DetailModalDTO="DetailModalDTO"
  />
  <h1 class="notice_title">Notice</h1>
  <div class="add_btn_container">
    <button class="add_btn" @click="openModal">Add Noti</button>
  </div>
  <div class="notice_container">
    <NoticeItem
      v-for="(item, index) in noticeList"
      :key="index"
      :noticeItem="item"
      :openDetailModal="openDetailModal"
    />
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import NoticeItem from "./NoticeItem.vue";
import NotiDetailModal from "./NotiDetailModal.vue";
import NotiModle from "./NotiModle.vue";
import { getNoti } from "../api/common";

const noticeList = ref([]);

const showModal = ref(false);
const showDetailModal = ref(false);
const DetailModalDTO = ref({
  id: "",
  projectId: sessionStorage.getItem("projectId"),
  userId: "",
  title: "",
  content: "",
});

const openDetailModal = (data) => {
  DetailModalDTO.value.id = data.id;
  DetailModalDTO.value.userId = data.userId;
  DetailModalDTO.value.projectId = data.projectId;
  DetailModalDTO.value.title = data.title;
  DetailModalDTO.value.content = data.content;
  showDetailModal.value = true;
};
const closeDetailModal = () => {
  showDetailModal.value = false;
};

const openModal = () => {
  showModal.value = true;
};
const closeModal = () => {
  showModal.value = false;
};

onMounted(() => {
  getNotiList();
});

const getNotiList = () => {
  const projectId = sessionStorage.getItem("projectId");
  getNoti(projectId)
    .then((res) => {
      return res.data;
    })
    .then((data) => {
      noticeList.value = data.sort((a, b) => {
        return b.id - a.id;
      });
    })
    .catch((err) => {
      console.log(err);
    });
};
</script>
<style scoped>
@import "../assets/css/notice.css";
</style>
