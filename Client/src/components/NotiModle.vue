<template>
  <div>
    <div @click="closeModalFunction" class="black_bg"></div>
    <div class="alert">
      <p class="alert_title">Add Notice</p>
      <div class="input_container">
        <label for="title">Title</label>
        <input v-model="formData.title" name="title" id="title" type="text" />
        <label for="content">Content</label>
        <textarea
          v-model="formData.content"
          name="content"
          id="content"
          cols="30"
          rows="10"
        ></textarea>
      </div>
      <button @click="addNotiFunction" class="alert_btn">생성</button>
    </div>
  </div>
</template>
<script setup>
import { defineProps, ref } from "vue";
import { addNoti } from "../api/common";
import { expireToken } from "../api/config";
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

const props = defineProps(["closeModal", "getNotiList"]);

const formData = ref({
  projectId: "",
  content: "",
  title: "",
});

const closeModalFunction = () => {
  props.getNotiList();
  props.closeModal();
};

const addNotiFunction = () => {
  const data = {
    projectId: sessionStorage.getItem("projectId"),
    content: formData.value.content,
    title: formData.value.title,
  };

  if (validation(data)) {
    addNoti(data)
      .then((res) => {
        Toast.fire({
          icon: "success",
          title: "Notice 생성 완료",
        });
        props.getNotiList();
        props.closeModal();
      })
      .catch((err) => {
        expireToken(err, addNotiFunction);
      });
  } else {
    Toast.fire({
      icon: "error",
      title: "Notice 생성 실패",
    });
  }
};

const validation = (data) => {
  if (
    data.content.trim() === "" ||
    data.projectId.trim() === "" ||
    data.title.trim() === ""
  ) {
    return false;
  } else return true;
};
</script>

<style scoped>
@import "../assets/css/notiModle.css";
</style>
