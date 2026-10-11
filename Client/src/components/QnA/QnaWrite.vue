<template>
  <form @submit.prevent="validateAndSave">
    <h2>1:1 문의</h2>
    <br>   
    <div class="mb-3">
      <label for="title" class="form-label">제목: </label>
      <input type="text" v-model="title" class="form-control" placeholder="제목을 입력하세요.">
      <div v-if="!title" class="error-message"></div>
  
    </div>
    <br>
    <div class="mb-3">
      <label for="content" class="form-label">내용: </label>
      <textarea name="" id="" cols="90" rows="10" v-model="content" class="form-control" style="resize: none;"></textarea>
      <div v-if="!content" class="error-message"></div>
    </div>

    <div class="d-grid gap-3 d-md-flex justify-content-md-end">
      <button type="button" class="btn btn-outline-dark text-white bg-dark" v-on:click="validateAndSave">저장</button>
      <button type="button" class="btn btn-outline-dark text-black bg-white" v-on:click="fnList">목록</button>
    </div>
  </form>
</template>

<script>

import Swal from 'sweetalert2';
import { expireToken } from "@/api/config";
import axios from 'axios'
export default {
  data(){
    return {
      requestBody: this.$route.query,
      id: this.$route.query.id,
      errors: [],
      title: '',
      userId: '',
      content: '',
      createdDate: ''
    }
  },
  mounted(){
    this.fnGetView()
  },
  methods: {
    validateAndSave() {
      this.errors = [];
      if (!this.title){
        this.errors.push("제목을 입력하세요")
      }
      if (!this.content) {
        this.errors.push("내용을 입력하세요")
      }

      if (this.errors.length === 0) {
        this.fnSave();
      }
    },
    fnGetView: function(){
      if (this.id !== undefined) {
        axios.get("http://localhost:8090/api/question/"+this.id,{
          params: this.requestBody
        }).then((res) => {
        this.title = res.data.title
        this.userId = res.data.userId
        this.content = res.data.content
        this.createdDate = res.data.createdDate
      })
      }
    },
    fnList() {
      delete this.requestBody.id
      this.$router.push({
        path: './question',
        query: this.requestBody
      })
    },
    fnView(id){
      this.requestBody.id = id
      this.$router.push({
        path: './detail',
        query: this.requestBody
      })
    },
    fnSave(){
      let apiUrl = "http://localhost:8090/api/question"
      this.form = {
        "id": this.id,
        "title": this.title,
        "content": this.content
      } 

      if (this.id === undefined){
        console.log(this.form)
        axios.post(apiUrl, this.form, {headers: { 
          "Authorization" : sessionStorage.getItem("access-token") }
        })
        .then((res) => {
          if (res.status == 200){
            alert("저장되었습니다.")
            this.fnList(res.data.id)
          }
          
        }).catch(error => {
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
        })
      } else {
        axios.put(apiUrl, this.form)
        .then((res) => {
          alert("수정되었습니다.")
          this.fnView(res.data.id)
        }).catch(error => {
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
        })
      }
    }
  }
}
</script>


<style scoped>
.error-message {
  color: red;
  font-size: 12px
}
</style>
