<template>
  <div>
    <div @click="closeModalFunction" class="black_bg"></div>
    <div class="alert">
      <p class="alert_title">Notice</p>
      <div class="top_btn_container">
        <div>
          <p class="modifiers_text">Modifiers : {{ DetailModalDTO.userId }}</p>
        </div>
        <div>
          <button @click="editNotiHandler" class="close_btn bg_yellow">
            수정
          </button>
          <button @click="deleteNotiHandler" class="close_btn bg_red">
            삭제
          </button>
        </div>
      </div>
      <div class="input_container">
        <label for="title">Title</label>
        <input
          v-model="DetailModalDTO.title"
          class="mb20"
          name="title"
          id="title"
        />
        <label for="content">Content</label>
        <textarea
          v-model="DetailModalDTO.content"
          name="content"
          id="content"
        />
      </div>
      <button @click="closeModalFunction" class="close_btn">닫기</button>
    </div>
  </div>
</template>
<script setup>
import { defineProps } from "vue";
import { deleteNoti, editNoti } from "../api/common";
import Swal from "sweetalert2";

const Toast = Swal.mixin({
  toast: true,
  position: "top-end",
  showConfirmButton: false,
  timer: 2000,
  timerProgressBar: true,
  didOpen: (toast) => {
    toast.addEventListener("mouseenter", Swal.stopTimer);
    toast.addEventListener("mouseleave", Swal.resumeTimer);
  },
});

const props = defineProps([
  "closeDetailModal",
  "getNotiList",
  "DetailModalDTO",
]);

const closeModalFunction = () => {
  props.getNotiList();
  props.closeDetailModal();
};

const deleteNotiHandler = () => {
  console.log(props.DetailModalDTO.id);
  deleteNoti(props.DetailModalDTO.id)
    .then(() => {
      Toast.fire({
        icon: "success",
        title: "해당 Notice가 삭제 되었습니다.",
      });
      closeModalFunction();
    })
    .catch((err) => {
      alert(err);
    });
};

const editNotiHandler = () => {
  const data = {
    title: props.DetailModalDTO.title,
    content: props.DetailModalDTO.content,
  };
  if (!validation(data)) {
    Toast.fire({
      icon: "error",
      title: "내용을 입력해 주세요.",
    });
  } else {
    editNoti(props.DetailModalDTO.id, data)
      .then(() => {
        Toast.fire({
          icon: "success",
          title: "해당 Notice가 수정 되었습니다.",
        });
        closeModalFunction();
      })
      .catch((err) => {
        alert(err);
      });
  }
};

const validation = (data) => {
  if (data.content.trim() === "" || data.title.trim() === "") {
    return false;
  } else return true;
};
</script>

<style scoped>
@import "../assets/css/notiModle.css";
</style>
