<template>
  <div class="mypage_container">
    <div class="mypage_wrapper">
      <img
        :src="editMyInfo.profileImg"
        alt="profile_img"
        class="profile_img_tag"
      />
      <label for="fileInput" class="custom-file-input-label">
        이미지 변경
      </label>
      <input
        style="display: none"
        type="file"
        id="fileInput"
        ref="fileInput"
        @change="editProfileImgHandler"
        class="custom-file-input"
      />
      <form @submit.prevent="submitForm" class="form_container">
        <div class="input_container">
          <label for="nickname">닉네임</label>
          <input
            v-model="editMyInfo.nickname"
            id="nickname"
            name="nickname"
            type="text"
          />
        </div>
        <div class="input_container">
          <label for="email">이메일</label>
          <input
            v-model="editMyInfo.email"
            id="email"
            name="email"
            type="email"
          />
        </div>
        <div class="input_container">
          <label for="password">비밀번호</label>
          <input
            v-model="editMyInfo.password"
            id="password"
            name="password"
            type="password"
          />
        </div>
        <div class="input_container">
          <label for="confirmPassword">비밀번호 확인</label>
          <input
            v-model="editMyInfo.confirmPassword"
            id="confirmPassword"
            name="confirmPassword"
            type="password"
          />
        </div>
        <button class="edit_btn">수정</button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { getMyInfoReq, editMyInfoReq, editProfile } from "../api/common";
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

const editMyInfo = ref({
  nickname: "",
  email: "",
  profileImg: "",
  password: "",
  confirmPassword: "",
});

onMounted(() => {
  window.scrollTo(0, 0);
  getMyInfo();
});

const editProfileImgHandler = (event) => {
  const selectedFile = event.target.files[0];
  const formData = new FormData();
  formData.append("file", selectedFile);
  editProfile(formData)
    .then(() => {
      Toast.fire({
        icon: "success",
        title: "프로필 이미지 수정 완료",
      });
      window.location.reload();
    })
    .catch((error) => {
      expireToken(error, editProfileImgHandler, event);
    });
};

const getMyInfo = () => {
  getMyInfoReq()
    .then((res) => res.data)
    .then((data) => {
      const { nickname, email, profileImg } = data;
      editMyInfo.value = {
        nickname,
        email,
        profileImg,
        password: "",
        confirmPassword: "",
      };
    })
    .catch((error) => {
      expireToken(error, getMyInfo);
      console.log(error);
    });
};

const submitForm = () => {
  if (validateForm()) {
    editMyInfoReq(editMyInfo.value)
      .then((res) => {
        Toast.fire({
          icon: "success",
          title: "회원정보 수정 완료",
        });
      })
      .catch((error) => {
        expireToken(error, submitForm);
        window.scrollTo(0, 0);
        Toast.fire({
          icon: "error",
          title: "회원정보 수정 실패",
        });
      });
  } else {
    Toast.fire({
      icon: "error",
      title: "회원정보 수정 실패",
    });
  }
};

const validateForm = () => {
  const { nickname, email, password, confirmPassword } = editMyInfo.value;

  if (password !== confirmPassword) return false;
  if (
    nickname.trim() === "" ||
    email.trim() === "" ||
    password.trim() === "" ||
    confirmPassword.trim() === ""
  ) {
    return false;
  } else return true;
};
</script>

<style scoped>
@import "../assets/css/mypage.css";
</style>
