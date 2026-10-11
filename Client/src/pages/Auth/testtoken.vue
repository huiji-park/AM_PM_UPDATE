<template>
    <h3> {{ accesstoken }} </h3>
    <h3> {{ refreshtoken }} </h3>

    <button @click="checkToken"> refresh token 재발급 확인 토큰</button>
</template>
<script setup>

import axios from 'axios';

// console.log(sessionStorage.getItem("atoken"))
// console.log(sessionStorage.getItem("rtoken"))

let accesstoken = sessionStorage.getItem("access-token")
let refreshtoken = sessionStorage.getItem("refresh-token")

let checkToken = () => {
    // 클라이언트에서 accesstoken 유효시간 검증
    // const token = sessionStorage.getItem('access-token');
    // console.log(token)
    // if (token) {
    // const tokenPayload = token.split('.')[1]; // 토큰의 페이로드에 접근
    // console.log(tokenPayload)
    // const decodedPayload = JSON.parse(atob(tokenPayload)); // 페이로드 디코딩
    // console.log(decodedPayload)
    // const expirationTimeInSeconds = decodedPayload.exp; // 만료 시간(초 단위)

    // console.log(expirationTimeInSeconds)
    // const currentTimestampInSeconds = Math.floor(Date.now() / 1000); // 현재 타임스탬프(초 단위)

    // console.log(currentTimestampInSeconds)
    // if (expirationTimeInSeconds && expirationTimeInSeconds > currentTimestampInSeconds) {
    //     // 토큰이 아직 만료되지 않았습니다
    //     const timeUntilExpiration = expirationTimeInSeconds - currentTimestampInSeconds;
    //     console.log(`토큰 만료까지 ${timeUntilExpiration}초 남았습니다.`);
    // } else {
    //     // 토큰이 만료되었습니다
    //     console.log('토큰이 만료되었습니다.');
    //     // 사용자 로그아웃 또는 토큰 갱신과 같은 필요한 작업 수행
    // }
    // } else {
    // // 세션 스토리지에서 토큰을 찾을 수 없습니다
    //     console.log('토큰을 찾을 수 없습니다.');
    // // 로그인 페이지로 리다이렉트하는 등 필요한 작업 수행
    // }

    axios.get("http://localhost:8090/api/atoken", {
            headers: { 
                "Authorization" : sessionStorage.getItem("access-token") }
            })
        .then(response => {
            if (response.status == 200) {
                // 로그인 성공 시 메인 페이지로
                console.log(response.headers.authorization);
                // console.log(response.headers.refreshtoken);

                // sessionStorage.setItem("access-token", response.headers.authorization);
                // sessionStorage.setItem("refresh-token", response.headers.refreshtoken);
                // router.push("/testtoken")
            } 
            else {
                console.log(response)
                // 로그인 실패 시 오류 메시지를 팝업 창으로 표시
                response.text().then(errorMessage => {
                    alert(errorMessage);
                });
            }
        })
        .catch(error => {
            console.error(error);
            if(error.response.status == 401) {
                console.log("토큰 만료");

                axios.get("http://localhost:8090/api/rtoken", {
                    headers: { 
                        "RefreshToken" : sessionStorage.getItem("refresh-token"),
                        "Authorization" : sessionStorage.getItem("access-token") }
                    }).then(response => {
                        console.log(response)
                        if(response.status == 200){
                            console.log("토큰 재발급");
                            console.log(response.headers.authorization);
                            sessionStorage.setItem("access-token", response.headers.authorization);
                        } else {
                            console.log("토큰 재발급 실패");
                        }
                    }).catch(error => {console.error(error);})
            } 
        });
}

</script>