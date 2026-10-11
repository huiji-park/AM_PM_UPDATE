<template>
    <body class="bg-light">
        
    <div class="modal fade" id="exampleModal" tabindex="-1" aria-labelledby="exampleModalLabel" aria-hidden="false">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title fs-5" id="exampleModalLabel">알림!</h1>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <p v-if="registrationResult == 'success'">회원 가입이 성공적으로 완료되었습니다.</p>
                    <p v-else-if="registrationResult == 'failure'">회원 가입에 실패했습니다. 다시 시도해주세요.</p>
                    <p v-else-if="registrationResult == 'duplication'">중복 확인 후 회원 가입 가능합니다.</p>
                    <p v-else>입력을 확인해주세요.</p>
                </div>
                <div class="modal-footer" v-if="registrationResult === 'success'">
                    <button type="button" class="btn btn-primary" data-bs-dismiss="modal" aria-label="Close" @click="redirectToLogin">확인</button>
                </div>
            </div>
        </div>
    </div>

    <div class="logo-container">
        <img src="@/assets/images/main_logo.png" alt="">
    </div>
    <div id="register_container" class="container">
        <div class="row justify-content-center">
            <div class="col-md-6">
                <div class="card">
                    <div class="card-header text-center">
                        <h2 id="title_text">회원가입</h2>
                    </div>
                    <div class="card-body">
                        <form @submit.prevent="submitForm">

                            <div class="form-group">
                                <label for="userId">아이디</label>
                                <div class="input-group">
                                    <input v-model="formData.userId" type="text" class="form-control" id="userId"
                                        placeholder="아이디를 입력해 주세요" required>
                                    <div class="input-group-append">
                                        <button type="button" class="btn btn-primary" @click="checkUserId">중복확인</button>
                                    </div>
                                </div>

                                <small id="availability-message" class="form-text text-info">{{ availabilityMessage
                                }}</small>
                            </div>

                            <div class="form-group">
                                <label for="password">비밀번호</label>
                                <input v-model="formData.password" type="password" class="form-control" id="password"
                                    placeholder="비밀번호를 입력해 주세요" required>
                            </div>

                            <div class="form-group">
                                <label for="nickname">닉네임</label>
                                <div class="input-group">
                                    <input v-model="formData.nickname" type="text" class="form-control" id="nickname"
                                        placeholder="닉네임을 입력해 주세요" required>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="email">이메일</label>
                                <div class="input-group">
                                    <input v-model="formData.email" type="email" class="form-control" id="email" placeholder="이메일을 입력해 주세요" required>
                                    <div class="input-group-append">
                                        <button type="button" class="btn btn-primary" @click="checkEmail">중복확인</button>
                                    </div>
                                </div>
                                <small id="email-availability-message" class="form-text text-info">{{
                                    emailAvailabilityMessage }}</small>
                            </div>
                            <div class="form-group row">
                                <div class="col-sm-10">
                                    <button type="submit" class="btn btn-primary btn-block" data-bs-toggle="modal" data-bs-target="#exampleModal">회원가입</button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
    </body>
</template>
<script setup>
import { ref, reactive, watch } from 'vue'; 
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const logo = '@/assets/images/main_logo.png';
// let showModal = false
let registrationResult =  ref("")

let formData = reactive({
    userId: '',
    password: '',
    nickname: '',
    email: '',
})
let availabilityMessage = ref('')
let emailAvailabilityMessage = ref('')
let idavailable = false
let emailavaliable = false

watch(formData, (newValue, oldValue) => {
  if (newValue.email !== oldValue.email) {
    checkEmail(newValue.email);
  }
});

const checkUserId = () => {
    const user_id = formData.userId;
    if (!user_id) {
        availabilityMessage.value = "아이디를 입력해주세요.";
        return;
    }
    axios.get(`http://localhost:8090/api/user/user_id/${user_id}`)
        .then(response => {
            if (response.data) {
                availabilityMessage.value = "사용 가능한 아이디입니다.";
                idavailable = true
            } else {
                availabilityMessage.value = "중복된 아이디입니다. 다른 아이디를 입력해주세요.";
            }
        })
        .catch(error => {
            availabilityMessage.value = "요청에 실패했습니다. 다시 시도해주세요.";
        })
}

let checkEmail = () => {
    const email = formData.email;

    if (!email) {
        emailAvailabilityMessage.value = "이메일을 입력해주세요.";
        return;
    }
    
    if (!validateEmail(email)) {
        emailAvailabilityMessage.value = "올바른 이메일 주소 형식이 아닙니다.";
        return;
    }
    
    axios.get(`http://localhost:8090/api/user/email/${email}`)
        .then(response => {
            if (response.data) {
                emailAvailabilityMessage.value = "사용 가능한 이메일입니다.";
                emailavaliable = true
            } else {
                emailAvailabilityMessage.value = "중복된 이메일입니다. 다른 이메일를 입력해주세요.";
            }
        })
        .catch(error => {
            emailAvailabilityMessage.value = "요청에 실패했습니다. 다시 시도해주세요.";
        })
}

const submitForm = () => {
    if(emailavaliable & idavailable){
        axios.post('http://localhost:8090/api/user', formData)
        .then(response => {
            console.log('Server Response:', response);
            console.log(response.status==201 )
            if (response.data ==="성공적으로 회원 가입이 진행되었습니다.") {
                console.log('가입 성공');
                registrationResult.value = 'success';
            } else {
                console.error('아이디 또는 비밀번호가 잘못 입력되었습니다.', response.data);
                registrationResult.value = 'failure';
            }
        }).catch(error => {
            console.error('서버와의 통신 중 오류가 발생했습니다.', error);
        })
    } else {
        registrationResult.value = "duplication";
    }
}

const redirectToLogin = () => {
    router.push("/login");
}

const validateEmail = (email) => {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return re.test(String(email).toLowerCase());
}
</script>

<style scoped>
@import "@/assets/css/userRegister.css";
</style>